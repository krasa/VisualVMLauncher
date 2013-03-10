package krasa.visualvm.runner;

import com.intellij.debugger.engine.DebuggerUtils;
import com.intellij.debugger.impl.DebuggerManagerImpl;
import com.intellij.debugger.settings.DebuggerSettings;
import com.intellij.execution.configurations.ConfigurationInfoProvider;
import com.intellij.execution.configurations.JavaCommandLine;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.openapi.util.text.StringUtil;
import krasa.visualvm.ApplicationSettingsComponent;
import krasa.visualvm.LogHelper;
import krasa.visualvm.VisualVMContext;
import krasa.visualvm.VisualVMHelper;
import krasa.visualvm.executor.DebugVisualVMExecutor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.debugger.impl.GenericDebuggerRunner;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.ModuleRunProfile;
import com.intellij.execution.configurations.PatchedRunnableState;
import com.intellij.execution.configurations.RemoteConnection;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.remote.RemoteConfiguration;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

public class DebugVisualVMRunner extends GenericDebuggerRunner {
	private static final Logger log = Logger.getInstance(DebugVisualVMRunner.class.getName());

	@NotNull
	public String getRunnerId() {
		return DebugVisualVMExecutor.EXECUTOR_ID;
	}

	@Override
	public void execute(@NotNull Executor executor, @NotNull ExecutionEnvironment environment)
			throws ExecutionException {
		LogHelper.print("#execute", this);
		final VisualVMGenericDebuggerRunnerSettings debuggerSettings = ((VisualVMGenericDebuggerRunnerSettings) environment.getRunnerSettings().getData());
		debuggerSettings.generateId();
		new VisualVMContext(debuggerSettings).save();

		boolean b = ApplicationSettingsComponent.openSettingsIfNotConfigured(environment.getProject());
		if (!b) {
			return;
		}
		super.execute(executor, environment);
	}

	public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
		return executorId.equals(DebugVisualVMExecutor.EXECUTOR_ID) && profile instanceof ModuleRunProfile
				&& !(profile instanceof RemoteConfiguration);
	}

	@Override
	public VisualVMGenericDebuggerRunnerSettings createConfigurationData(ConfigurationInfoProvider settingsProvider) {
		return new VisualVMGenericDebuggerRunnerSettings();
	}

	private RemoteConnection doPatch(final JavaParameters javaParameters, final RunnerSettings settings) throws ExecutionException {
		final VisualVMGenericDebuggerRunnerSettings debuggerSettings = addVisualVMIdToJavaParameterEE(javaParameters, settings);
		return DebuggerManagerImpl.createDebugParameters(javaParameters, debuggerSettings, false);
	}

	@Override
	public void patch(JavaParameters javaParameters, RunnerSettings settings, boolean beforeExecution)
			throws ExecutionException {
		LogHelper.print("#patch", this);
		doPatch(javaParameters, settings);
		runCustomPatchers(javaParameters, settings, Executor.EXECUTOR_EXTENSION_NAME.findExtension(DefaultDebugExecutor.class));
	}


	@Nullable
	@Override
	protected RunContentDescriptor createContentDescriptor(Project project, Executor executor, RunProfileState state,
			RunContentDescriptor contentToReuse, ExecutionEnvironment env) throws ExecutionException {
		LogHelper.print("#createContentDescriptor", this);
		addVisualVMIdToJavaParameter(state);
		return super.createContentDescriptor(project, executor, state, contentToReuse, env);
	}

	/*is called for normal application*/
	private void addVisualVMIdToJavaParameter(RunProfileState state) throws ExecutionException {
		final VisualVMGenericDebuggerRunnerSettings debuggerSettings = ((VisualVMGenericDebuggerRunnerSettings) state.getRunnerSettings().getData());
		// tomcat uses PatchedRunnableState, so do not run it at #attachVirtualMachine
		if (state instanceof JavaCommandLine) {
			final JavaParameters parameters = ((JavaCommandLine) state).getJavaParameters();
			LogHelper.print("#createContentDescriptor -Dvisualvm.id=" + debuggerSettings.getVisualVMId(), this);
			parameters.getVMParametersList().add("-Dvisualvm.id=" + debuggerSettings.getVisualVMId());

		}
	}


	/*is called for tomcat, but not normal application*/
	private VisualVMGenericDebuggerRunnerSettings addVisualVMIdToJavaParameterEE(JavaParameters javaParameters, RunnerSettings settings) throws ExecutionException {
		final VisualVMGenericDebuggerRunnerSettings debuggerSettings = ((VisualVMGenericDebuggerRunnerSettings) settings.getData());
		if (StringUtil.isEmpty(debuggerSettings.getDebugPort())) {
			debuggerSettings.setDebugPort(DebuggerUtils.getInstance().findAvailableDebugAddress(debuggerSettings.getTransport() == DebuggerSettings.SOCKET_TRANSPORT));
		}
		LogHelper.print("#doPatch -Dvisualvm.id=" + debuggerSettings.getVisualVMId(), this);
		javaParameters.getVMParametersList().add("-Dvisualvm.id=" + debuggerSettings.getVisualVMId());
		return debuggerSettings;
	}

	@Nullable
	@Override
	protected RunContentDescriptor attachVirtualMachine(Project project, Executor executor, RunProfileState state,
			RunContentDescriptor contentToReuse, ExecutionEnvironment env, RemoteConnection connection,
			boolean pollConnection) throws ExecutionException {
		RunContentDescriptor runContentDescriptor = super.attachVirtualMachine(project, executor, state,
				contentToReuse, env, connection, pollConnection);
		runVisualVM(state);
		return runContentDescriptor;
	}

	private void runVisualVM(RunProfileState state) throws ExecutionException {
		LogHelper.print("#attachVirtualMachine", this);
		final VisualVMGenericDebuggerRunnerSettings debuggerSettings = ((VisualVMGenericDebuggerRunnerSettings) state.getRunnerSettings().getData());
		if (state instanceof PatchedRunnableState) {
			LogHelper.print("#attachVirtualMachine !run", this);
			new Thread() {
				@Override
				public void run() {
					LogHelper.print("#Thread run", this);
					try {
						Thread.sleep(ApplicationSettingsComponent.getInstance().getState().getDelayForVisualVMStartAsLong());
						VisualVMHelper.startVisualVM(debuggerSettings, DebugVisualVMRunner.this);
					} catch (Exception e) {
						log.error(e);
					}
				}
			}.start();
		} else {
			VisualVMHelper.startVisualVM(debuggerSettings, this);
		}

	}

}

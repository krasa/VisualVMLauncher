package krasa.visualvm.runner;

import com.intellij.debugger.engine.DebuggerUtils;
import com.intellij.debugger.impl.DebuggerManagerImpl;
import com.intellij.debugger.impl.GenericDebuggerRunner;
import com.intellij.debugger.settings.DebuggerSettings;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationInfoProvider;
import com.intellij.execution.configurations.JavaCommandLine;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.ModuleRunProfile;
import com.intellij.execution.configurations.RemoteConnection;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.remote.RemoteConfiguration;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import krasa.visualvm.ApplicationSettingsComponent;
import krasa.visualvm.Hacks;
import krasa.visualvm.LogHelper;
import krasa.visualvm.VisualVMContext;
import krasa.visualvm.VisualVMHelper;
import krasa.visualvm.executor.DebugVisualVMExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DebugVisualVMRunner extends GenericDebuggerRunner {
	private static final Logger log = Logger.getInstance(DebugVisualVMRunner.class.getName());

	@NotNull
	public String getRunnerId() {
		return DebugVisualVMExecutor.EXECUTOR_ID;
	}

	@Override
	public void execute(@NotNull final ExecutionEnvironment environment)
			throws ExecutionException {
		LogHelper.print("#execute", this);
		final VisualVMGenericDebuggerRunnerSettings debuggerSettings = ((VisualVMGenericDebuggerRunnerSettings) environment.getRunnerSettings());
		debuggerSettings.generateId();
		new VisualVMContext(debuggerSettings).save();

		boolean b = ApplicationSettingsComponent.openSettingsIfNotConfigured(environment.getProject());
		if (!b) {
			return;
		}
		super.execute(environment);
	}

	public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
		return executorId.equals(DebugVisualVMExecutor.EXECUTOR_ID) && profile instanceof ModuleRunProfile
				&& !(profile instanceof RemoteConfiguration);
	}

	@Override
	public VisualVMGenericDebuggerRunnerSettings createConfigurationData(ConfigurationInfoProvider settingsProvider) {
		return new VisualVMGenericDebuggerRunnerSettings();
	}


	@Override
	public void patch(JavaParameters javaParameters, RunnerSettings settings, RunProfile runProfile, boolean beforeExecution)
			throws ExecutionException {
		LogHelper.print("#patch", this);
		doPatch(javaParameters, settings);
		runCustomPatchers(javaParameters, Executor.EXECUTOR_EXTENSION_NAME.findExtension(DefaultDebugExecutor.class), runProfile);
	}

	/*is called for tomcat, but not normal application*/
	private RemoteConnection doPatch(final JavaParameters javaParameters, final RunnerSettings settings) throws ExecutionException {
		final VisualVMGenericDebuggerRunnerSettings debuggerSettings = ((VisualVMGenericDebuggerRunnerSettings) settings);
		if (StringUtil.isEmpty(debuggerSettings.getDebugPort())) {
			debuggerSettings.setDebugPort(DebuggerUtils.getInstance().findAvailableDebugAddress(debuggerSettings.getTransport() == DebuggerSettings.SOCKET_TRANSPORT));
		}
		LogHelper.print("#doPatch -Dvisualvm.id=" + debuggerSettings.getVisualVMId(), this);
		javaParameters.getVMParametersList().add("-Dvisualvm.id=" + debuggerSettings.getVisualVMId());
		return DebuggerManagerImpl.createDebugParameters(javaParameters, debuggerSettings, false);
	}

	@Nullable
	@Override
	protected RunContentDescriptor createContentDescriptor(@NotNull RunProfileState state,
			@NotNull ExecutionEnvironment environment) throws ExecutionException {
		LogHelper.print("#createContentDescriptor", this);
		addVisualVMIdToJavaParameter(environment, state);
		return super.createContentDescriptor(state, environment);
	}

	/*is called for normal application*/
	private void addVisualVMIdToJavaParameter(ExecutionEnvironment executionEnvironment, RunProfileState runProfileState) throws ExecutionException {
		final VisualVMGenericDebuggerRunnerSettings debuggerSettings = ((VisualVMGenericDebuggerRunnerSettings) executionEnvironment.getRunnerSettings());
		if (runProfileState instanceof JavaCommandLine) {
			final JavaParameters parameters = ((JavaCommandLine) runProfileState).getJavaParameters();
			LogHelper.print("#createContentDescriptor -Dvisualvm.id=" + debuggerSettings.getVisualVMId(), this);
			parameters.getVMParametersList().add("-Dvisualvm.id=" + debuggerSettings.getVisualVMId());

		}
	}

	@Nullable
	@Override
	protected RunContentDescriptor attachVirtualMachine(RunProfileState state, @NotNull ExecutionEnvironment env,
			RemoteConnection connection, boolean pollConnection) throws ExecutionException {
		RunContentDescriptor runContentDescriptor = super.attachVirtualMachine(state, env, connection, pollConnection);
		LogHelper.print("#attachVirtualMachine", this);
		runVisualVM(env, state);
		return runContentDescriptor;
	}

	private void runVisualVM(ExecutionEnvironment env, RunProfileState state) throws ExecutionException {
		final VisualVMGenericDebuggerRunnerSettings debuggerSettings = ((VisualVMGenericDebuggerRunnerSettings) env.getRunnerSettings());
		// tomcat uses PatchedLocalState
		if (state.getClass().getSimpleName().equals(Hacks.BUNDLED_SERVERS_RUN_PROFILE_STATE)) {
			LogHelper.print("#runVisualVM ExecutionEnvironment", this);
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

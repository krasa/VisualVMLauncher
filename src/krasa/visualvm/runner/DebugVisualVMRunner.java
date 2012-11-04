package krasa.visualvm.runner;

import krasa.visualvm.ApplicationSettingsComponent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.debugger.impl.GenericDebuggerRunner;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.JavaCommandLine;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.ModuleRunProfile;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.remote.RemoteConfiguration;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.project.Project;

public class DebugVisualVMRunner extends GenericDebuggerRunner {

	@NotNull
	public String getRunnerId() {
		return DebugVisualVMExecutor.EXECUTOR_ID;
	}

	@Override
	public void execute(@NotNull Executor executor, @NotNull ExecutionEnvironment environment)
			throws ExecutionException {
		boolean b = ApplicationSettingsComponent.openSettingsIfNotConfigured(environment);
		if (!b) {
			return;
		}
		super.execute(executor, environment);
	}

	@Override
	public void execute(@NotNull Executor executor, @NotNull ExecutionEnvironment env, @Nullable Callback callback)
			throws ExecutionException {
		super.execute(executor, env, callback);
	}

	public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
		return executorId.equals(DebugVisualVMExecutor.EXECUTOR_ID) && profile instanceof ModuleRunProfile
				&& !(profile instanceof RemoteConfiguration);
	}

	@Nullable
	@Override
	protected RunContentDescriptor createContentDescriptor(Project project, Executor executor, RunProfileState state,
			RunContentDescriptor contentToReuse, ExecutionEnvironment env) throws ExecutionException {

		Long appId = null;
		String jdkPath = null;
		if (state instanceof JavaCommandLine) {
			final JavaParameters parameters = ((JavaCommandLine) state).getJavaParameters();
			appId = VisualVMJavaProgramPatcher.patchJavaParameters(parameters);
			jdkPath = parameters.getJdkPath();

		}
		RunContentDescriptor contentDescriptor = super.createContentDescriptor(project, executor, state,
				contentToReuse, env);

		if (jdkPath != null && appId != null) {
			StartVisualVMAction startVisualVMAction = new StartVisualVMAction(null, appId, jdkPath);
			startVisualVMAction.startVisualVM();
		}
		return contentDescriptor;
	}

}

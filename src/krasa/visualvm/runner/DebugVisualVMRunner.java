package krasa.visualvm.runner;

import krasa.visualvm.ApplicationSettingsComponent;
import krasa.visualvm.VisualVMHelper;
import krasa.visualvm.executor.DebugVisualVMExecutor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.debugger.impl.GenericDebuggerRunner;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ModuleRunProfile;
import com.intellij.execution.configurations.RemoteConnection;
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

	@Nullable
	@Override
	protected RunContentDescriptor attachVirtualMachine(Project project, Executor executor, RunProfileState state,
			RunContentDescriptor contentToReuse, ExecutionEnvironment env, RemoteConnection connection,
			boolean pollConnection) throws ExecutionException {
		RunContentDescriptor runContentDescriptor = super.attachVirtualMachine(project, executor, state,
				contentToReuse, env, connection, pollConnection);
		VisualVMHelper.startVisualVM();
		return runContentDescriptor;
	}

}

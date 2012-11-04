/*
 * This file is part of VisualVM for IDEA
 *
 * Copyright (c) 2008, Esko Luontola. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *
 *     * Neither the name of the copyright holder nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package krasa.visualvm.runner;

import krasa.visualvm.ApplicationSettingsComponent;

import org.jetbrains.annotations.NotNull;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.JavaCommandLine;
import com.intellij.execution.configurations.JavaCommandLineState;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.ModuleRunProfile;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.impl.DefaultJavaProgramRunner;
import com.intellij.execution.remote.RemoteConfiguration;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProcessProxy;
import com.intellij.execution.runners.ProcessProxyFactory;
import com.intellij.execution.runners.RunContentBuilder;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;

public class RunVisualVMRunner extends DefaultJavaProgramRunner {

	@NotNull
	public String getRunnerId() {
		return RunVisualVMExecutor.RUN_WITH_VISUAL_VM;
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

	public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
		return executorId.equals(RunVisualVMExecutor.RUN_WITH_VISUAL_VM) && profile instanceof ModuleRunProfile
				&& !(profile instanceof RemoteConfiguration);
	}

	@Override
	protected RunContentDescriptor doExecute(Project project, Executor executor, RunProfileState state,
			RunContentDescriptor contentToReuse, ExecutionEnvironment env) throws ExecutionException {
		String jdkHome = null;
		long appId = -1;
		FileDocumentManager.getInstance().saveAllDocuments();

		ExecutionResult executionResult;
		boolean shouldAddDefaultActions = true;
		if (state instanceof JavaCommandLine) {
			final JavaParameters parameters = ((JavaCommandLine) state).getJavaParameters();

			this.patch(parameters, state.getRunnerSettings(), true);

			appId = VisualVMJavaProgramPatcher.patchJavaParameters(parameters);
			jdkHome = parameters.getJdkPath();

			final ProcessProxy proxy = ProcessProxyFactory.getInstance().createCommandLineProxy((JavaCommandLine) state);
			executionResult = state.execute(executor, this);
			if (proxy != null && executionResult != null) {
				proxy.attach(executionResult.getProcessHandler());
			}
			if (state instanceof JavaCommandLineState
					&& !((JavaCommandLineState) state).shouldAddJavaProgramRunnerActions()) {
				shouldAddDefaultActions = false;
			}
		} else {
			executionResult = state.execute(executor, this);
		}

		if (executionResult == null) {
			return null;
		}

		onProcessStarted(env.getRunnerSettings(), executionResult);

		final RunContentBuilder contentBuilder = new RunContentBuilder(project, this, executor);
		Disposer.register(project, contentBuilder);
		contentBuilder.setExecutionResult(executionResult);
		contentBuilder.setEnvironment(env);
		if (shouldAddDefaultActions) {
			addDefaultActions(contentBuilder);
		}

		StartVisualVMAction startVisualVMAction = new StartVisualVMAction(
				contentBuilder.getExecutionResult().getProcessHandler(), appId, jdkHome);
		startVisualVMAction.startVisualVM();
		AnAction[] actions = new AnAction[] { startVisualVMAction };

		for (AnAction action : actions) {
			contentBuilder.addAction(action);
		}

		RunContentDescriptor runContent = contentBuilder.showRunContent(contentToReuse);

		return runContent;
	}

}

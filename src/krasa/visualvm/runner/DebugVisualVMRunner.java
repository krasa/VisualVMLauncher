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

import com.intellij.execution.configurations.ModuleRunProfile;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.remote.RemoteConfiguration;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.notification.impl.NotificationsConfigurable;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.roots.ui.configuration.ProjectSettingsService;
import krasa.visualvm.ApplicationSettingsComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.debugger.impl.GenericDebuggerRunner;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.JavaCommandLine;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.project.Project;

public class DebugVisualVMRunner extends GenericDebuggerRunner {

	@NotNull
	public String getRunnerId() {
		return DebugVisualVMExecutor.EXECUTOR_ID;
	}

    @Override
    public void execute(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) throws ExecutionException {
        boolean b = ApplicationSettingsComponent.openSettingsIfNotConfigured(environment);
        if (!b) {
            return;
        }
        super.execute(executor, environment);
    }

    @Override
    public void execute(@NotNull Executor executor, @NotNull ExecutionEnvironment env, @Nullable Callback callback) throws ExecutionException {
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

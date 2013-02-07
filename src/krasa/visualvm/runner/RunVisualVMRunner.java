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

import com.intellij.debugger.engine.DebuggerUtils;
import com.intellij.debugger.settings.DebuggerSettings;
import com.intellij.execution.configurations.ConfigurationInfoProvider;
import com.intellij.execution.configurations.PatchedRunnableState;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import krasa.visualvm.ApplicationSettingsComponent;
import krasa.visualvm.LogHelper;
import krasa.visualvm.VisualVMContext;
import krasa.visualvm.VisualVMHelper;
import krasa.visualvm.executor.RunVisualVMExecutor;

import org.jetbrains.annotations.NotNull;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.ModuleRunProfile;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.impl.DefaultJavaProgramRunner;
import com.intellij.execution.remote.RemoteConfiguration;
import com.intellij.execution.runners.ExecutionEnvironment;

public class RunVisualVMRunner extends DefaultJavaProgramRunner {
	private static final Logger log = Logger.getInstance(DebugVisualVMRunner.class.getName());

	@NotNull
	public String getRunnerId() {
		return RunVisualVMExecutor.RUN_WITH_VISUAL_VM;
	}

	public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
		return executorId.equals(RunVisualVMExecutor.RUN_WITH_VISUAL_VM) && profile instanceof ModuleRunProfile
				&& !(profile instanceof RemoteConfiguration);
	}

	@Override
	public void execute(@NotNull Executor executor, @NotNull ExecutionEnvironment environment)
			throws ExecutionException {
		final VisualVMGenericDebuggerRunnerSettings debuggerSettings = ((VisualVMGenericDebuggerRunnerSettings) environment.getRunnerSettings().getData());
		debuggerSettings.generateId();
		new VisualVMContext(debuggerSettings).save();

		LogHelper.print("#execute", this);

		boolean b = ApplicationSettingsComponent.openSettingsIfNotConfigured(environment.getProject());
		if (!b) {
			return;
		}
		super.execute(executor, environment);
	}


	@Override
	public AnAction[] createActions(ExecutionResult executionResult) {
		return super.createActions(executionResult);
	}

	@Override
	protected RunContentDescriptor doExecute(Project project, Executor executor, RunProfileState state, RunContentDescriptor contentToReuse, ExecutionEnvironment env) throws ExecutionException {
//		addVisualVMIdToJavaParameter(state);
		RunContentDescriptor runContentDescriptor = super.doExecute(project, executor, state, contentToReuse, env);
		runVisualVM(state);
		return runContentDescriptor;
	}

	@Override
	public void onProcessStarted(RunnerSettings settings, ExecutionResult executionResult) {
		super.onProcessStarted(settings, executionResult);
		LogHelper.print("#onProcessStarted", this);

	}

	@Override
	public void patch(JavaParameters javaParameters, RunnerSettings settings, boolean beforeExecution)
			throws ExecutionException {
		addVisualVMIdToJavaParameter(javaParameters, settings);
		super.patch(javaParameters, settings, beforeExecution);
	}


	/*used for tomcat and normal applications*/
	private void addVisualVMIdToJavaParameter(JavaParameters javaParameters, RunnerSettings settings) throws ExecutionException {
		final VisualVMGenericDebuggerRunnerSettings debuggerSettings = ((VisualVMGenericDebuggerRunnerSettings) settings.getData());
		if (StringUtil.isEmpty(debuggerSettings.getDebugPort())) {
			debuggerSettings.setDebugPort(DebuggerUtils.getInstance().findAvailableDebugAddress(debuggerSettings.getTransport() == DebuggerSettings.SOCKET_TRANSPORT));
		}
		LogHelper.print("#addVisualVMIdToJavaParameter -Dvisualvm.id=" + debuggerSettings.getVisualVMId(), this);
		javaParameters.getVMParametersList().add("-Dvisualvm.id=" + debuggerSettings.getVisualVMId());
	}

	@Override
	public VisualVMGenericDebuggerRunnerSettings createConfigurationData(ConfigurationInfoProvider settingsProvider) {
		return new VisualVMGenericDebuggerRunnerSettings();
	}

	private void runVisualVM(RunProfileState state) throws ExecutionException {
		final VisualVMGenericDebuggerRunnerSettings debuggerSettings = ((VisualVMGenericDebuggerRunnerSettings) state.getRunnerSettings().getData());
		if (state instanceof PatchedRunnableState) {
			LogHelper.print("#runVisualVMAsync " + debuggerSettings.getVisualVMId(), this);
			new Thread() {
				@Override
				public void run() {
					try {
						Thread.sleep(ApplicationSettingsComponent.getInstance().getState().getDelayForVisualVMStartAsLong());
						VisualVMHelper.startVisualVM(debuggerSettings, RunVisualVMRunner.this);
					} catch (Exception e) {
						log.error(e);
					}
				}
			}.run();
		} else {
			VisualVMHelper.startVisualVM(debuggerSettings, this);
		}
	}

}

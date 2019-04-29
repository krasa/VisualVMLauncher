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

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.*;
import com.intellij.execution.impl.DefaultJavaProgramRunner;
import com.intellij.execution.jar.JarApplicationConfiguration;
import com.intellij.execution.remote.RemoteConfiguration;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.diagnostic.Logger;
import krasa.visualvm.*;
import krasa.visualvm.executor.RunVisualVMExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RunVisualVMRunner extends DefaultJavaProgramRunner {
	private static final Logger log = Logger.getInstance(DebugVisualVMRunner.class.getName());

	@NotNull
	public String getRunnerId() {
		return RunVisualVMExecutor.RUN_WITH_VISUAL_VM;
	}

	public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
		return executorId.equals(RunVisualVMExecutor.RUN_WITH_VISUAL_VM) && (profile instanceof ModuleRunProfile || profile instanceof JarApplicationConfiguration) && !(profile instanceof RemoteConfiguration);
	}

	@Override
	public void execute(@NotNull final ExecutionEnvironment env, @Nullable final Callback callback)

			throws ExecutionException {
		final VisualVMGenericRunnerSettings settings = ((VisualVMGenericRunnerSettings) env.getRunnerSettings());
		if (settings != null) {
			settings.generateId();
			new VisualVMContext(settings).save();
		}

		LogHelper.print("#execute", this);

		boolean b = MyConfigurable.openSettingsIfNotConfigured(env.getProject());
		if (!b) {
			return;
		}
		super.execute(env, callback);
	}

	@Override
	protected RunContentDescriptor doExecute(@NotNull RunProfileState state, @NotNull ExecutionEnvironment env)
			throws ExecutionException {
		RunContentDescriptor runContentDescriptor = super.doExecute(state, env);
		runVisualVM(env, state);
		return runContentDescriptor;
	}
	
	@Override
	public void onProcessStarted(RunnerSettings settings, ExecutionResult executionResult) {
		super.onProcessStarted(settings, executionResult);
		LogHelper.print("#onProcessStarted", this);

	}

	@Override
	public void patch(JavaParameters javaParameters, RunnerSettings settings, RunProfile runProfile, final boolean beforeExecution) throws ExecutionException {

		addVisualVMIdToJavaParameter(javaParameters, settings);
		super.patch(javaParameters, settings, runProfile, beforeExecution);
	}


	/*used for tomcat and normal applications*/
	private void addVisualVMIdToJavaParameter(JavaParameters javaParameters, RunnerSettings settings) throws ExecutionException {
		final VisualVMGenericRunnerSettings runnerSettings = ((VisualVMGenericRunnerSettings) settings);
		LogHelper.print("#addVisualVMIdToJavaParameter -Dvisualvm.id=" + runnerSettings.getVisualVMId(), this);
		javaParameters.getVMParametersList().add("-Dvisualvm.id=" + runnerSettings.getVisualVMId());
	}

	@Override
	@Nullable
	public RunnerSettings createConfigurationData(final ConfigurationInfoProvider settingsProvider) {
		return new VisualVMGenericRunnerSettings();
	}

	private void runVisualVM(ExecutionEnvironment env, RunProfileState state) throws ExecutionException {
		final VisualVMGenericRunnerSettings settings = ((VisualVMGenericRunnerSettings) env.getRunnerSettings());
		// tomcat uses PatchedLocalState
		if (state.getClass().getSimpleName().equals(Hacks.BUNDLED_SERVERS_RUN_PROFILE_STATE)) {
			LogHelper.print("#runVisualVMAsync " + settings.getVisualVMId(), this);
			new Thread() {
				@Override
				public void run() {
					try {
						Thread.sleep(ApplicationSettingsComponent.getInstance().getState().getDelayForVisualVMStartAsLong());
						VisualVMHelper.startVisualVM(settings, RunVisualVMRunner.this);
					} catch (Exception e) {
						log.error(e);
					}
				}
			}.start();
		} else {
			VisualVMHelper.startVisualVM(settings, this);
		}
	}

}

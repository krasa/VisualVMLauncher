package krasa.visualvm.runner;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.JvmPatchableProgramRunner;
import com.intellij.openapi.diagnostic.Logger;
import krasa.visualvm.ApplicationSettingsComponent;
import krasa.visualvm.Hacks;
import krasa.visualvm.LogHelper;
import krasa.visualvm.VisualVMHelper;

public class RunnerUtils {
	private static final Logger log = Logger.getInstance(RunnerUtils.class.getName());

	static void runVisualVM(final JvmPatchableProgramRunner runner, ExecutionEnvironment env, RunProfileState state) throws ExecutionException {
		try {
			final VisualVMRunnerSettings runnerSettings = ((VisualVMRunnerSettings) env.getRunnerSettings());
			// tomcat uses PatchedLocalState
			if (state.getClass().getSimpleName().equals(Hacks.BUNDLED_SERVERS_RUN_PROFILE_STATE)) {
				LogHelper.print("#runVisualVM ExecutionEnvironment", runner);
				new Thread() {
					@Override
					public void run() {
						LogHelper.print("#Thread run", this);
						try {
							Thread.sleep(ApplicationSettingsComponent.getInstance().getState().getDelayForVisualVMStartAsLong());
							VisualVMHelper.startVisualVM(env, runnerSettings, runner);
						} catch (Throwable e) {
							log.error(e);
						}
					}
				}.start();
			} else {
				VisualVMHelper.startVisualVM(env, runnerSettings, runner);
			}
		} catch (Throwable e) {
			log.error(e);
		}
	}
}

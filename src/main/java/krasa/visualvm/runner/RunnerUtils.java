package krasa.visualvm.runner;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.JvmPatchableProgramRunner;
import com.intellij.openapi.diagnostic.Logger;
import krasa.visualvm.ApplicationSettingsService;
import krasa.visualvm.Hacks;
import krasa.visualvm.LogHelper;
import krasa.visualvm.integration.VisualVMContext;
import krasa.visualvm.integration.VisualVMHelper;

public class RunnerUtils {
	private static final Logger log = Logger.getInstance(RunnerUtils.class.getName());

	static void runVisualVM(final JvmPatchableProgramRunner runner, ExecutionEnvironment env, RunProfileState state) throws ExecutionException {
		try {
			// tomcat uses PatchedLocalState
			if (state.getClass().getSimpleName().equals(Hacks.BUNDLED_SERVERS_RUN_PROFILE_STATE)) {
				LogHelper.print("#runVisualVM ExecutionEnvironment", runner);
				new Thread() {
					@Override
					public void run() {
						LogHelper.print("#Thread run", this);
						try {
							Thread.sleep(ApplicationSettingsService.getInstance().getState().getDelayForVisualVMStartAsLong());
							VisualVMHelper.startVisualVM(VisualVMContext.load(), env.getProject(), runner);
						} catch (Throwable e) {
							log.error(e);
						}
					}
				}.start();
			} else {
				VisualVMHelper.startVisualVM(VisualVMContext.load(), env.getProject(), runner);
			}
		} catch (Throwable e) {
			log.error(e);
		}
	}
}

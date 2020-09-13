package krasa.visualvm;

import com.intellij.execution.CantRunException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.runners.JavaProgramPatcher;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkTypeId;
import krasa.visualvm.action.StartVisualVMConsoleAction;
import org.jetbrains.annotations.Nullable;

public class VisualVMJavaProgramPatcher extends JavaProgramPatcher {
	private static final Logger log = Logger.getInstance(VisualVMJavaProgramPatcher.class.getName());
	long lastExecution;

	@Override
	public void patchJavaParameters(Executor executor, RunProfile configuration, JavaParameters javaParameters) {
		LogHelper.print("#patchJavaParameters start", this);

		if (Hacks.BUNDLED_SERVERS_RUN_PROFILE.equals(configuration.getClass().getName())) {
			LogHelper.print("patchJavaParameters com.intellij.javaee.run.configuration.CommonStrategy", this);

			if (System.currentTimeMillis() - lastExecution > 1000) {
				LogHelper.print("patchJavaParameters com.intellij.javaee.run.configuration.CommonStrategy patching", this);

				VisualVMContext visualVMContext = patch(configuration, javaParameters);
				StartVisualVMConsoleAction.setVisualVMContextToRecentlyCreated(visualVMContext);
				lastExecution = System.currentTimeMillis();
			}
		} else {
			patch(configuration, javaParameters);
		}
	}

	private VisualVMContext patch(RunProfile configuration, JavaParameters javaParameters) {
		String jdkPath = getJdkPath(javaParameters);

		VisualVMContext load = VisualVMContext.load();
		String param = null;
		if (load != null) {
			Long appId = load.getAppId();
			param = "-Dvisualvm.id=" + appId;
		}
		if (param != null && javaParameters.getVMParametersList().getParametersString().contains(param)) {
			return load;
		} else {
			final Long appId = VisualVMHelper.getNextID();

			LogHelper.print("Patching: jdkPath=" + jdkPath + "; appId=" + appId, this);
			javaParameters.getVMParametersList().prepend("-Dvisualvm.id=" + appId);


			VisualVMContext visualVMContext = new VisualVMContext(appId, jdkPath, VisualVMHelper.resolveModule(configuration));
			visualVMContext.save();
			return visualVMContext;
		}
	}

	@Nullable
	public static String getJdkPath(JavaParameters javaParameters) {
		String jdkPath = null;
		try {
			if (javaParameters.getJdk() != null && javaParameters.getJdk().getHomeDirectory() != null) {
				Sdk jdk = javaParameters.getJdk();
				SdkTypeId sdkType = jdk.getSdkType();
				if ("JavaSDK".equals(sdkType.getName())) {
					jdkPath = javaParameters.getJdkPath();
				}
			}
		} catch (CantRunException e) {
			// return;
		} catch (Throwable e) {
			log.error(e);
		}
		return jdkPath;
	}

}

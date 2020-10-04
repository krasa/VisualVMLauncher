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

		String name = configuration.getClass().getName();
		if (name.startsWith(Hacks.BUNDLED_SERVERS_RUN_PROFILE)) {
			LogHelper.print("patchJavaParameters " + name, this);

			if (System.currentTimeMillis() - lastExecution > 1000) {
				LogHelper.print("patchJavaParameters " + name + " patching", this);

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
		String id = null;
		if (load != null) {
			Long appId = load.getAppId();
			id = "-Dvisualvm.id=" + appId;
		}
		if (id != null && javaParameters.getVMParametersList().getParametersString().contains(id)) {
			return load;
		} else {
			final Long appId = VisualVMHelper.getNextID();

			LogHelper.print("Patching: jdkPath=" + jdkPath + "; appId=" + appId, this);
			for (String arg : VisualVMHelper.getJvmArgs(appId)) {
				javaParameters.getVMParametersList().prepend(arg);
			}


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

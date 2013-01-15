package krasa.visualvm;

import com.intellij.execution.CantRunException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.runners.JavaProgramPatcher;
import com.intellij.openapi.diagnostic.Logger;

public class VisualVMJavaProgramPatcher extends JavaProgramPatcher {
	private static final Logger log = Logger.getInstance(VisualVMJavaProgramPatcher.class.getName());

	@Override
	public void patchJavaParameters(Executor executor, RunProfile configuration, JavaParameters javaParameters) {
		String jdkPath;
		try {
			jdkPath = javaParameters.getJdkPath();
		} catch (CantRunException e) {
			return;
		}

		Long appId = VisualVMHelper.getNextID();

		log.info("Patching: jdkPath=" + jdkPath + "; appId=" + appId);
		for (String arg : VisualVMHelper.getJvmArgs(appId)) {
			javaParameters.getVMParametersList().prepend(arg);
		}

		new VisualVMContext(appId, jdkPath).save();

	}

}

package krasa.visualvm;

import com.intellij.execution.CantRunException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.JavaProgramPatcher;

public class VisualVMJavaProgramPatcher extends JavaProgramPatcher {

	public static long patchJavaParameters(JavaParameters javaParameters) {
		Long appId = VisualVMHelper.getNextID();
		for (String arg : VisualVMHelper.getJvmArgs(appId)) {
			javaParameters.getVMParametersList().prepend(arg);
		}
		return appId;
	}

	@Override
	public void patchJavaParameters(Executor executor, RunProfile configuration, JavaParameters javaParameters) {
		try {
			String jdkPath = javaParameters.getJdkPath();
			Long appId = VisualVMHelper.getNextID();

			for (String arg : VisualVMHelper.getJvmArgs(appId)) {
				javaParameters.getVMParametersList().prepend(arg);
			}

			new VisualVMContext(appId, jdkPath).save();
		} catch (CantRunException e) {
			e.printStackTrace();
		}
	}

}

package krasa.visualvm.runner;

import krasa.visualvm.VisualVMHelper;

import com.intellij.execution.configurations.JavaParameters;

public class VisualVMJavaProgramPatcher {

	public static long patchJavaParameters(JavaParameters javaParameters) {
		Long appId = VisualVMHelper.getNextID();
		for (String arg : VisualVMHelper.getJvmArgs(appId)) {
			javaParameters.getVMParametersList().prepend(arg);
		}
		return appId;
	}
}

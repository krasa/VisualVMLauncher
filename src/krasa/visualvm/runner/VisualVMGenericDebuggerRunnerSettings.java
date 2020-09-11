package krasa.visualvm.runner;

import com.intellij.debugger.impl.GenericDebuggerRunnerSettings;
import krasa.visualvm.VisualVMHelper;

public class VisualVMGenericDebuggerRunnerSettings extends GenericDebuggerRunnerSettings implements VisualVMRunnerSettings {
	protected long visualVMId;
	protected String jdkHome;

	public VisualVMGenericDebuggerRunnerSettings() {
		visualVMId = VisualVMHelper.getNextID();
	}

	@Override
	public String getJdkHome() {
		return jdkHome;
	}

	@Override
	public void setJdkHome(String jdkHome) {
		this.jdkHome = jdkHome;
	}

	@Override
	public long getVisualVMId() {
		return visualVMId;
	}

	@Override
	public void setVisualVMId(long visualVMId) {
		this.visualVMId = visualVMId;
	}

	public void generateId() {
		visualVMId = VisualVMHelper.getNextID();
	}


	@Override
	public String toString() {
		return "VisualVMGenericDebuggerRunnerSettings{" +
			"visualVMId=" + visualVMId +
			", jdkHome='" + jdkHome + '\'' +
			"} " + super.toString();
	}
}
			
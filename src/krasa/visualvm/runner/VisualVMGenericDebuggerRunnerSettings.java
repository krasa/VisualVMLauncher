package krasa.visualvm.runner;

import com.intellij.debugger.impl.GenericDebuggerRunnerSettings;
import krasa.visualvm.VisualVMHelper;

public class VisualVMGenericDebuggerRunnerSettings extends GenericDebuggerRunnerSettings {
	protected long visualVMId;

	public VisualVMGenericDebuggerRunnerSettings() {
		visualVMId = VisualVMHelper.getNextID();
	}

	public long getVisualVMId() {
		return visualVMId;
	}

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
			"} " + super.toString();
	}
}

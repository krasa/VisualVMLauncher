package krasa.visualvm;

import org.apache.commons.lang.StringUtils;

import java.io.File;

public class PluginSettings {

	private String visualVmExecutable;
	private boolean debug;
	private String durationToSetContextToButton = "5000";

	public String getVisualVmExecutable() {
		return visualVmExecutable;
	}

	public void setVisualVmExecutable(final String visualVmExecutable) {
		this.visualVmExecutable = visualVmExecutable;
	}

	public boolean getDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}


	public static boolean isValid(PluginSettings state) {
		boolean result = true;
		if (state == null || StringUtils.isBlank(state.getVisualVmExecutable()) || !new File(state.getVisualVmExecutable()).exists()) {
			result = false;
		}
		return result;
	}

	public String getDurationToSetContextToButton() {
		return durationToSetContextToButton;
	}

	public void setDurationToSetContextToButton(final String durationToSetContextToButton) {
		this.durationToSetContextToButton = durationToSetContextToButton;
	}
}

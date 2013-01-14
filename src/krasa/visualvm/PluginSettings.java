package krasa.visualvm;

import org.apache.commons.lang.StringUtils;

import java.io.File;

public class PluginSettings {

	private String visualVmExecutable;

	public String getVisualVmExecutable() {
		return visualVmExecutable;
	}

	public void setVisualVmExecutable(final String visualVmExecutable) {
		this.visualVmExecutable = visualVmExecutable;
	}

	public static boolean isValid(PluginSettings state) {
		boolean result = true;
		if (state == null || StringUtils.isBlank(state.getVisualVmExecutable()) || !new File(state.getVisualVmExecutable()).exists()) {
			result = false;
		}
		return result;
	}
}

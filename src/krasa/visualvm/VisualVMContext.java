package krasa.visualvm;

import org.apache.commons.lang.StringUtils;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

/*dirty, but works*/
public class VisualVMContext {
	private static VisualVMContext currentlyExecuted;

	protected Long appId;
	protected String jdkPath;

	public VisualVMContext(Long appId, String jdkPath) {
		this.appId = appId;
		this.jdkPath = jdkPath;
	}

	public Long getAppId() {
		return appId;
	}

	public String getJdkPath() {
		return jdkPath;
	}

	public void save() {
		VisualVMContext.currentlyExecuted = this;
	}

	public static VisualVMContext load() {
		return currentlyExecuted;
	}

	public static boolean isValid(VisualVMContext visualVMContext) {
		return visualVMContext != null && isNotBlank(visualVMContext.getJdkPath()) && visualVMContext.getAppId() != null;
	}
}

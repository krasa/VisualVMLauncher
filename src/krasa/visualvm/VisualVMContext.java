package krasa.visualvm;

import static org.apache.commons.lang.StringUtils.isNotBlank;

import com.intellij.openapi.diagnostic.Logger;

/*dirty, but works*/
public class VisualVMContext {
	private static final Logger log = Logger.getInstance(VisualVMContext.class.getName());
	private static volatile VisualVMContext currentlyExecuted;

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
		log.info("saving context: " + this.toString());
		VisualVMContext.currentlyExecuted = this;
	}

	public static VisualVMContext load() {
		return currentlyExecuted;
	}

	public static boolean isValid(VisualVMContext visualVMContext) {
		return visualVMContext != null && isNotBlank(visualVMContext.getJdkPath())
				&& visualVMContext.getAppId() != null;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("VisualVMContext");
		sb.append("{appId=").append(appId);
		sb.append(", jdkPath='").append(jdkPath).append('\'');
		sb.append('}');
		return sb.toString();
	}
}

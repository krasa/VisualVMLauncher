package krasa.visualvm.integration;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;

/*dirty, but works*/
public class VisualVMContext {
	private static final Logger log = Logger.getInstance(VisualVMContext.class.getName());
	private static volatile VisualVMContext currentlyExecuted;

	protected Long appId;
	protected Module module;
	protected String jdkPath;

	public VisualVMContext(Long appId, String jdkPath, Module module) {
		this.appId = appId;
		this.jdkPath = jdkPath;
		this.module = module;
	}

	public Long getAppId() {
		return appId;
	}

	public String getJdkPath() {
		return jdkPath;
	}

	public void save() {
		if (log.isDebugEnabled()) {
			log.debug("saving context: " + this.toString());
		}
		VisualVMContext.currentlyExecuted = this;
	}

	public static VisualVMContext load() {
		return currentlyExecuted;
	}

	public static boolean isValid(VisualVMContext visualVMContext) {
		return visualVMContext != null && visualVMContext.getAppId() != null;
	}

	public Module getModule() {
		return module;
	}

	public void setModule(Module module) {
		this.module = module;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("VisualVMContext");
		sb.append("{appId=").append(appId);
		sb.append(", module='").append(module).append('\'');
//		sb.append(", jdkPath='").append(jdkPath).append('\'');
		sb.append('}');
		return sb.toString();
	}
}

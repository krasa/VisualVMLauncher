package krasa.visualvm;

public class VisualVMContext {
	private static VisualVMContext actuallyExecuted;

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
		VisualVMContext.actuallyExecuted = this;
	}

	public static VisualVMContext load() {
		return actuallyExecuted;
	}

	public static boolean isValid(VisualVMContext visualVMContext) {
		return visualVMContext != null && visualVMContext.getJdkPath() != null && visualVMContext.getAppId() != null;
	}
}

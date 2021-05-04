package krasa.visualvm;

import com.intellij.openapi.diagnostic.Logger;

public class LogHelper {
	private static final Logger log = Logger.getInstance(LogHelper.class.getName());

	public static void print(String x, Object thisInstance) {
		if (log.isDebugEnabled()) {
			String simpleName = "LogHelper : ";
			if (thisInstance != null) {
				simpleName = thisInstance.getClass().getSimpleName() + ": ";
			}
			log.debug(simpleName + x);
		}
	}

}

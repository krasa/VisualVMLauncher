package krasa.visualvm;

import com.intellij.openapi.diagnostic.Logger;

public class LogHelper {
	private static final Logger log = Logger.getInstance(LogHelper.class.getName());

	public static void print(String x, Object thisInstance) {
		if (log.isDebugEnabled()) {
			System.out.println(thisInstance.getClass().getSimpleName() + ": " + x);
			log.debug(thisInstance.getClass().getSimpleName() + ": " + x);
		}
	}

}

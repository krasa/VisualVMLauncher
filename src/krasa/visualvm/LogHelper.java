package krasa.visualvm;

import com.intellij.openapi.diagnostic.Logger;

public class LogHelper {
	private static final Logger log = Logger.getInstance(LogHelper.class.getName());
	public static boolean debug = false;

	public static void print(String x, Object thisInstance) {
		if (debug) {
			System.out.println(thisInstance.getClass().getSimpleName() + ": " + x);
			log.debug(thisInstance.getClass().getSimpleName() + ": " + x);
		}
	}

}

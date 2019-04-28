/*
 * Copyright (c) 2007, 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package krasa.visualvm;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import krasa.visualvm.runner.VisualVMGenericDebuggerRunnerSettings;
import krasa.visualvm.runner.VisualVMGenericRunnerSettings;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.util.StringTokenizer;

public final class VisualVMHelper {
	private static final Logger log = Logger.getInstance(VisualVMHelper.class.getName());

	public static void startVisualVM(long appId, String jdkHome, final Object thisInstance) {
		String debug = "appId=" + appId + ", jdkHome=" + jdkHome;
		try {
			VisualVMHelper.openInVisualVM(appId, jdkHome, thisInstance);
		} catch (IOException e) {
			throw new RuntimeException(debug, e);
		}
	}

	public static void startVisualVM(VisualVMGenericDebuggerRunnerSettings genericDebuggerRunnerSettings, Object thisInstance) {
		String debug = "appId=" + genericDebuggerRunnerSettings.getVisualVMId();
		try {
			VisualVMHelper.openInVisualVM(genericDebuggerRunnerSettings.getVisualVMId(), null, thisInstance);
		} catch (IOException e) {
			throw new RuntimeException(debug, e);
		}
	}

	public static void startVisualVM(VisualVMGenericRunnerSettings runnerSettings, Object thisInstance) {
		String debug = "appId=" + runnerSettings.getVisualVMId();
		try {
			VisualVMHelper.openInVisualVM(runnerSettings.getVisualVMId(), null, thisInstance);
		} catch (IOException e) {
			throw new RuntimeException(debug, e);
		}
	}

	private static class SpecVersion {
		int major, minor;

		public SpecVersion(String specString) {
			StringTokenizer st = new StringTokenizer(specString, ".");
			if (st.hasMoreTokens()) {
				major = Integer.parseInt(st.nextToken());
			}
			if (st.hasMoreTokens()) {
				minor = Integer.parseInt(st.nextToken());
			}
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			sb.append("SpecVersion");
			sb.append("{major=").append(major);
			sb.append(", minor=").append(minor);
			sb.append('}');
			return sb.toString();
		}
	}

	public static long getNextID() {
		return System.nanoTime();
	}

	public static String[] getJvmArgs(long id) {
		return new String[]{"-Dvisualvm.id=" + id};
	}

	public static void openInVisualVM(long id, String jdkHome, Object thisInstance) throws IOException {
		String visualVmPath = ApplicationSettingsComponent.getInstance().getState().getVisualVmExecutable();
		String configuredJdkHome = ApplicationSettingsComponent.getInstance().getState().getJdkHome();
		if (StringUtils.isNotBlank(configuredJdkHome)) {
			jdkHome = configuredJdkHome;
		}
		
		if (!isValidPath(visualVmPath)) {
			final Notification notification = new Notification("VisualVMLauncher", "",
				"Path to VisualVM is not valid, path='" + visualVmPath + "'",
				NotificationType.ERROR);
			ApplicationManager.getApplication().invokeLater(new Runnable() {
				@Override
				public void run() {
					Notifications.Bus.notify(notification);
				}
			});
		} else {
			LogHelper.print("starting VisualVM with id=" + id, thisInstance);
			if (StringUtils.isBlank(jdkHome)) {
				Runtime.getRuntime().exec(new String[]{visualVmPath, "--openid", String.valueOf(id)});
			} else {
				Runtime.getRuntime().exec(
					new String[]{visualVmPath, "--jdkhome", jdkHome, "--openid", String.valueOf(id)});
			}
		}
	}

	public static boolean isValidPath(String visualVmPath) {
		return !StringUtils.isBlank(visualVmPath) && new File(visualVmPath).exists();
	}

	private static SpecVersion getJavaVersion(String jdkHome) {
		try {
			String javaCmd = jdkHome + File.separator + "bin" + File.separator + "java";
			Process prc = Runtime.getRuntime().exec(new String[]{javaCmd, "-version"});

			String version = getJavaVersion(prc.getErrorStream());
			if (version == null) {
				version = getJavaVersion(prc.getInputStream());
			}
			return new SpecVersion(version);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String getJavaVersion(InputStream is) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		try {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("java version")) {
					int start = line.indexOf("\"");
					int end = line.lastIndexOf("\"");
					if (start > -1 && end > -1) {
						return line.substring(start + 1, end);
					}
				}
			}
		} finally {
			br.close();
		}
		return null;
	}
}

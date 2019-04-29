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

import java.io.File;
import java.io.IOException;

public final class VisualVMHelper {
	private static final Logger log = Logger.getInstance(VisualVMHelper.class.getName());

	public static void startVisualVM(long appId, String jdkHome, final Object thisInstance) {
		VisualVMHelper.openInVisualVM(appId, jdkHome, thisInstance);
	}

	public static void startVisualVM(VisualVMGenericDebuggerRunnerSettings genericDebuggerRunnerSettings, Object thisInstance) {
		VisualVMHelper.openInVisualVM(genericDebuggerRunnerSettings.getVisualVMId(), null, thisInstance);
	}

	public static void startVisualVM(VisualVMGenericRunnerSettings runnerSettings, Object thisInstance) {
		VisualVMHelper.openInVisualVM(runnerSettings.getVisualVMId(), null, thisInstance);

	}

	public static long getNextID() {
		return System.nanoTime();
	}

	public static String[] getJvmArgs(long id) {
		return new String[]{"-Dvisualvm.id=" + id};
	}

	public static void openInVisualVM(long id, String jdkHome, Object thisInstance) {
		PluginSettings state = ApplicationSettingsComponent.getInstance().getState();

		String visualVmPath = state.getVisualVmExecutable();
		String configuredJdkHome = state.getJdkHome();
		if (StringUtils.isNotBlank(configuredJdkHome)) {
			jdkHome = configuredJdkHome;
		}

		String idString = String.valueOf(id);
		if (state.isUseTabIndex()) {
			idString += "@" + state.getTabIndex();
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
			LogHelper.print("starting VisualVM with id=" + idString, thisInstance);
			try {
				if (StringUtils.isBlank(jdkHome)) {
					Runtime.getRuntime().exec(new String[]{visualVmPath, "--openid", String.valueOf(id)});
				} else {
					Runtime.getRuntime().exec(
						new String[]{visualVmPath, "--jdkhome", jdkHome, "--openid", idString});
				}
			} catch (IOException e) {
				throw new RuntimeException("visualVmPath=" + visualVmPath + " appId=" + idString + " jdkHome=" + jdkHome, e);
			}
		}


	}

	public static boolean isValidPath(String visualVmPath) {
		return !StringUtils.isBlank(visualVmPath) && new File(visualVmPath).exists();
	}


}

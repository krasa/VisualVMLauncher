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
package krasa.visualvm.integration;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import krasa.visualvm.ApplicationSettingsService;
import krasa.visualvm.LogHelper;
import krasa.visualvm.PluginSettings;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class VisualVMHelper {
	private static final Logger log = Logger.getInstance(VisualVMHelper.class.getName());

	public static void startVisualVM(VisualVMContext vmContext, Project project, Object thisInstance) {
		VisualVMHelper.openInVisualVM(vmContext.getAppId(), vmContext.getJdkPath(), vmContext.getModule(), project, thisInstance);
	}

	public static long getNextID() {
		return System.nanoTime();
	}

	public static void startVisualVM(String jdkHome_doNotOverride) {
		PluginSettings state = ApplicationSettingsService.getInstance().getState();

		String visualVmPath = state.getVisualVmExecutable();
		if (!isValidPath(visualVmPath)) {
			final Notification notification = new Notification("VisualVMLauncher", "",
				"Path to VisualVM is not valid, path='" + visualVmPath + "'",
				NotificationType.ERROR);
			ApplicationManager.getApplication().invokeLater(() -> Notifications.Bus.notify(notification));
		} else {
			try {
				if (StringUtils.isBlank(jdkHome_doNotOverride)) {
					new ProcessBuilder(visualVmPath).start();
				} else {
					new ProcessBuilder(visualVmPath, "--jdkhome", jdkHome_doNotOverride).start();
				}
			} catch (IOException e) {
				throw new RuntimeException("visualVmPath=" + visualVmPath + "; jdkHome=" + jdkHome_doNotOverride, e);
			}
		}

	}

	public static void openInVisualVM(long id, String jdkHome, Module module, Project project, Object thisInstance) {
		PluginSettings pluginSettings = ApplicationSettingsService.getInstance().getState();

		String visualVmPath = pluginSettings.getVisualVmExecutable();
		String customJdkHome = pluginSettings.getJdkHome();
		boolean useModuleJdk = pluginSettings.isUseModuleJdk();
		boolean sourceConfig = pluginSettings.isSourceConfig();

		if (useModuleJdk) {
			if (StringUtils.isBlank(jdkHome)) {
				jdkHome = customJdkHome;
			}
		} else {
			jdkHome = customJdkHome;
		}

		String idString = String.valueOf(id);
		if (pluginSettings.isUseTabIndex()) {
			idString += "@" + pluginSettings.getTabIndex();
		}

		if (!isValidPath(visualVmPath)) {
			final Notification notification = new Notification("VisualVMLauncher", "",
				"Path to VisualVM is not valid, path='" + visualVmPath + "'",
				NotificationType.ERROR);
			ApplicationManager.getApplication().invokeLater(() -> Notifications.Bus.notify(notification));
		} else {
			run(jdkHome, project, visualVmPath, idString, sourceConfig, module, thisInstance);
		}
	}

	private static void run(String jdkHome, Project project, String visualVmPath, String idString, boolean sourceConfig, Module module, Object thisInstance) {
		LogHelper.print("starting VisualVM with id=" + idString, thisInstance);
		List<String> cmds = new ArrayList<>();
		try {
			cmds.add(visualVmPath);
			if (!StringUtils.isBlank(jdkHome)) {
				cmds.add("--jdkhome");
				cmds.add(jdkHome);
			}
			cmds.add("--openid");
			cmds.add(idString);
			if (sourceConfig) {
				try {
					addSourceConfig(project, cmds, module);
				} catch (Throwable e) {
					log.error(e);
				}
			}
//			if (sourceRoots) {
//				try {
//					addSourcePluginParameters(project, cmds, module);
//				} catch (Throwable e) {
//					log.error(e);
//				}
//			}

			log.info("Starting VisualVM with parameters:" + cmds);
			new ProcessBuilder(cmds).start();
		} catch (IOException e) {
			if (sourceConfig) {
				boolean contains = e.getMessage().contains("The filename or extension is too long");
				if (contains) {
					log.error("Please disable 'Integrate with VisualVM-GoToSource plugin' option at 'File | Settings | Other Settings | VisualVM Launcher'.\nThe command was too long: " + cmds.toString().length(), e);
					run(jdkHome, project, visualVmPath, idString, false, module, thisInstance);
					return;
				}
			}
			throw new RuntimeException(cmds.toString(), e);
		}
	}

	private static void addSourceConfig(Project project, List<String> cmds, Module runConfigurationModule) throws IOException {
		Properties props = new Properties();
		props.setProperty("source-roots", SourceRoots.resolve(project, runConfigurationModule));
		props.setProperty("source-viewer", getIdeaExe());

		File tempFile = FileUtil.createTempFile("visualVmConfig", ".properties");
		try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(tempFile), "UTF-8")) {
			props.store(osw, null);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		cmds.add("--source-config");
		cmds.add(tempFile.getAbsolutePath());
	}

	public static void addSourcePluginParameters(Project project, List<String> cmds, Module runConfigurationModule) {
		cmds.add("--source-roots");
		cmds.add(SourceRoots.resolve(project, runConfigurationModule));
		// --source-viewer="c:\NetBeans\bin\netbeans {file}:{line}"
		//https://www.jetbrains.com/help/idea/opening-files-from-command-line.html
		cmds.add("--source-viewer");
		cmds.add(getIdeaExe());
	}

	@NotNull
	private static String getIdeaExe() {
		String homePath = PathManager.getHomePath();
		String s;
		if (SystemInfo.isWindows) {
			//idea.bat --line 42 C:\MyProject\scripts\numbers.js
			s = "\"" + homePath + "\\bin\\idea.bat\" --line {line} {file}";
		} else if (SystemInfo.isMac) {
			//idea --line <number> <path>
			s = "\"" + homePath + "/bin/idea\" --line {line} {file}";
		} else {
			//idea.sh --line <number> <path>
			s = "\"" + homePath + "/bin/idea.sh\" --line {line} {file}";
		}
		return s;
	}


	public static boolean isValidPath(String visualVmPath) {
		return !StringUtils.isBlank(visualVmPath) && new File(visualVmPath).exists();
	}

}

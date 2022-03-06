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

import com.intellij.notification.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ApplicationNamesInfo;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.system.CpuArch;
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
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public final class VisualVMHelper {
	private static final Logger log = Logger.getInstance(VisualVMHelper.class.getName());

	public static void startVisualVM(VisualVMContext vmContext, Project project, Object thisInstance) {
		if (vmContext == null) {
			log.warn("VisualVMContext is null");
			return;
		}
		VisualVMHelper.openInVisualVM(vmContext.getAppId(), vmContext.getJdkPath(), vmContext.getModule(), project, thisInstance);
	}

	public static long getNextID() {
		return System.nanoTime();
	}

	public static void startVisualVM(Project project, String jdkHome_doNotOverride) {
		PluginSettings state = ApplicationSettingsService.getInstance().getState();

		String visualVmPath = state.getVisualVmExecutable();
		if (!isValidPath(visualVmPath)) {
			NotificationGroup group = NotificationGroupManager.getInstance().getNotificationGroup("VisualVMLauncher");
			Notification notification = group.createNotification("Path to VisualVM is not valid, path='" + visualVmPath + "'", NotificationType.ERROR);
			ApplicationManager.getApplication().invokeLater(() -> Notifications.Bus.notify(notification));
		} else {
			try {
				if (StringUtils.isBlank(jdkHome_doNotOverride)) {
					new VisualVMProcess(project, visualVmPath).run();
				} else {
					new VisualVMProcess(project, visualVmPath, "--jdkhome", jdkHome_doNotOverride).run();
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
			NotificationGroup group = NotificationGroupManager.getInstance().getNotificationGroup("VisualVMLauncher");
			Notification myNotification = group.createNotification("Path to VisualVM is not valid, path='" + visualVmPath + "'", NotificationType.ERROR);
			ApplicationManager.getApplication().invokeLater(() -> Notifications.Bus.notify(myNotification));
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
					cmds.add("--source-config");
					cmds.add(createSourceConfig(project, module).getAbsolutePath());
				} catch (Throwable e) {
					log.error(e);
				}
			}
			new VisualVMProcess(project, cmds.toArray(new String[0])).run();
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

	@NotNull
	private static File createSourceConfig(Project project, Module runConfigurationModule) throws IOException {
		Properties props = new Properties();
		props.setProperty("source-roots", SourceRoots.resolve(project, runConfigurationModule));

		File ideExecutable = getIdeExecutable();
		if (ideExecutable != null) {
			if (ideExecutable.exists()) {
				props.setProperty("source-viewer", "\"" + ideExecutable.getAbsolutePath() + "\" --line {line} {file}");
			} else {
				log.warn("Bin file not exists: " + ideExecutable.getAbsolutePath());
			}
		}

		File tempFile = FileUtil.createTempFile("visualVmConfig", ".properties");
		try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(tempFile), "UTF-8")) {
			props.store(osw, null);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return tempFile;
	}


	protected static File getIdeExecutable() {
		String scriptName = ApplicationNamesInfo.getInstance().getScriptName();
		if (SystemInfo.isWindows) {
			String bits = CpuArch.CURRENT.width == 64 ? "64" : "";
			return new File(PathManager.getBinPath(), scriptName + bits + ".exe");
		} else if (SystemInfo.isMac) {
			File appDir = new File(PathManager.getHomePath(), "MacOS");
			return new File(appDir, scriptName);
		} else if (SystemInfo.isUnix) {
			return new File(PathManager.getBinPath(), scriptName + ".sh");
		} else {
			log.error("invalid OS: " + SystemInfo.getOsNameAndVersion());
			return null;
		}
	}

	public static boolean isValidPath(String visualVmPath) {
		return !StringUtils.isBlank(visualVmPath) && new File(visualVmPath).exists();
	}


	static class VisualVMProcess {

		private final Project project;
		private final String[] cmds;

		public VisualVMProcess(Project project, String... cmds) {
			this.project = project;
			this.cmds = cmds;
		}

		public void run() throws IOException {
			PluginSettings settings = ApplicationSettingsService.getInstance().getState();

			List<String> cmd = new ArrayList<>(Arrays.asList(cmds));
			if (StringUtils.isNotBlank(settings.getLaf())) {
				cmd.add("--laf");
				cmd.add(settings.getLaf());
			}

			log.info("Starting VisualVM with parameters:" + cmd);

			ProcessBuilder processBuilder = new ProcessBuilder(cmd);
			//todo does not work
//			if (disableProcessDialog) {
//				File file = new File(PathManager.getLogPath(), "visualVMLauncher.log");
//				file.createNewFile();
//				if (file.exists()) {
//					processBuilder.redirectErrorStream(true);
//					processBuilder.redirectOutput(file);
//				}
////			}
			Process process = processBuilder.start();
//			if (!disableProcessDialog) {
//				process.toHandle().onExit().whenCompleteAsync((processHandle, throwable) -> accept(project, process, processHandle, throwable));
//			}
		}

//		private static void accept(Project project, Process process, ProcessHandle processHandle, Throwable throwable) {
//			try {
//				if (!processHandle.isAlive()) {
//					if (process.exitValue() != 0) {
//						String err = new String(process.getErrorStream().readAllBytes(), "UTF-8");
//						if (StringUtils.isNotBlank(err)) {
//							String message = "VisualVM exited with code: " + process.exitValue() + ".\nError: " + err;
//							SwingUtilities.invokeLater(() ->
//								Messages.showErrorDialog(project, message, "VisualVM Launcher"));
//							log.warn(message);
//						}
//
//					}
//				}
//			} catch (Throwable e) {
//				log.warn(e);
//			}
//		}

	}

	public static void executeVisualVM(Project project, @NotNull String commandLineAction) {
		PluginSettings state = ApplicationSettingsService.getInstance().getState();
//todo needs sdk
		String visualVmPath = state.getVisualVmExecutable();
		if (!isValidPath(visualVmPath)) {
			NotificationGroup group = NotificationGroupManager.getInstance().getNotificationGroup("VisualVMLauncher");
			Notification notification = group.createNotification("Path to VisualVM is not valid, path='" + visualVmPath + "'", NotificationType.ERROR);
			ApplicationManager.getApplication().invokeLater(() -> Notifications.Bus.notify(notification));
		} else {
			try {
				new VisualVMProcess(project, visualVmPath, commandLineAction).run();
			} catch (IOException e) {
				throw new RuntimeException("visualVmPath=" + visualVmPath + "; commandLineAction=" + commandLineAction, e);
			}
		}

	}
}

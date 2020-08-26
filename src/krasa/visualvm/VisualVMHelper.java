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
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.SourceFolder;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import krasa.visualvm.runner.VisualVMGenericDebuggerRunnerSettings;
import krasa.visualvm.runner.VisualVMGenericRunnerSettings;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.jps.model.module.JpsModuleSourceRootType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class VisualVMHelper {
	private static final Logger log = Logger.getInstance(VisualVMHelper.class.getName());

	public static void startVisualVM(long appId, String jdkHome, final Object thisInstance, Project project) {
		VisualVMHelper.openInVisualVM(appId, jdkHome, thisInstance, project);
	}

	public static void startVisualVM(VisualVMGenericDebuggerRunnerSettings genericDebuggerRunnerSettings, Object thisInstance, Project project) {
		VisualVMHelper.openInVisualVM(genericDebuggerRunnerSettings.getVisualVMId(), null, thisInstance, project);
	}

	public static void startVisualVM(VisualVMGenericRunnerSettings runnerSettings, Object thisInstance, Project project) {
		VisualVMHelper.openInVisualVM(runnerSettings.getVisualVMId(), null, thisInstance, project);

	}

	public static long getNextID() {
		return System.nanoTime();
	}

	public static String[] getJvmArgs(long id) {
		return new String[]{"-Dvisualvm.id=" + id};
	}

	public static void startVisualVM(String jdkHome) {
		PluginSettings state = ApplicationSettingsComponent.getInstance().getState();

		String visualVmPath = state.getVisualVmExecutable();
		if (!isValidPath(visualVmPath)) {
			final Notification notification = new Notification("VisualVMLauncher", "",
					"Path to VisualVM is not valid, path='" + visualVmPath + "'",
					NotificationType.ERROR);
			ApplicationManager.getApplication().invokeLater(() -> Notifications.Bus.notify(notification));
		} else {
			try {
				if (StringUtils.isBlank(jdkHome)) {
					Runtime.getRuntime().exec(new String[]{visualVmPath});
				} else {
					Runtime.getRuntime().exec(
							new String[]{visualVmPath, "--jdkhome", jdkHome});
				}
			} catch (IOException e) {
				throw new RuntimeException("visualVmPath=" + visualVmPath + "; jdkHome=" + jdkHome, e);
			}
		}

	}

	public static void openInVisualVM(long id, String jdkHome, Object thisInstance, Project project) {
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
			run(id, jdkHome, thisInstance, project, visualVmPath, idString, state.isSourceRoots());
		}


	}

	private static void run(long id, String jdkHome, Object thisInstance, Project project, String visualVmPath, String idString, boolean sourceRoots) {
		LogHelper.print("starting VisualVM with id=" + idString, thisInstance);
		List<String> cmds = new ArrayList<>();
		try {
			cmds.add(visualVmPath);
			if (!StringUtils.isBlank(jdkHome)) {
				cmds.add("--jdkhome");
				cmds.add(jdkHome);
			}
			cmds.add("--openid");
			cmds.add(String.valueOf(id));
			if (sourceRoots) {
				try {
					addSourceRoots(project, cmds);
				} catch (Throwable e) {
					log.error(e);
				}
			}

			Runtime.getRuntime().exec(cmds.toArray(new String[0]));
		} catch (IOException e) {
			if (sourceRoots) {
				boolean contains = e.getMessage().contains("The filename or extension is too long");
				if (contains) {
					log.error("Please disable 'Integrate with VisualVM-GoToSource plugin' option at 'File | Settings | Other Settings | VisualVM Launcher'", e);
					run(id, jdkHome, thisInstance, project, visualVmPath, idString, false);
					return;
				}
			}
			throw new RuntimeException(cmds.toString(), e);
		}
	}

	public static void addSourceRoots(Project project, List<String> cmds) {
		//https://visualvm.github.io/sourcessupport.html
		// --source-roots="c:\sources\root1;c:\sources\root2[subpaths=src:test\src]"
		StringBuilder sb = new StringBuilder();

		ModuleManager manager = ModuleManager.getInstance(project);
		Module[] modules = manager.getModules();

		for (Module module : modules) {
			ModuleRootManager root = ModuleRootManager.getInstance(module);
			ContentEntry[] contentEntries = root.getContentEntries();
			for (ContentEntry contentEntry : contentEntries) {
				new SourceRoots(contentEntry).appendTo(sb);
			}
		}
		cmds.add("--source-roots");
		cmds.add("\"" + sb.toString() + "\"");
		// --source-viewer="c:\NetBeans\bin\netbeans {file}:{line}"
		//https://www.jetbrains.com/help/idea/opening-files-from-command-line.html
		cmds.add("--source-viewer");
		String homePath = PathManager.getHomePath();
		if (SystemInfo.isWindows) {
			//idea.bat --line 42 C:\MyProject\scripts\numbers.js
			cmds.add("\"" + homePath + "/bin/idea.bat --line {line} {file}" + "\"");
		} else if (SystemInfo.isMac) {
			//idea --line <number> <path>
			cmds.add("\"" + homePath + "/bin/idea --line {line} {file}" + "\"");
		} else {
			//idea.sh --line <number> <path>
			cmds.add("\"" + homePath + "/bin/idea.sh --line {line} {file}" + "\"");
		}
	}

	public static boolean isValidPath(String visualVmPath) {
		return !StringUtils.isBlank(visualVmPath) && new File(visualVmPath).exists();
	}


	private static class SourceRoots {
		private final ContentEntry contentEntry;
		private final VirtualFile contentEntryFile;
		private final List<VirtualFile> paths = new ArrayList<>();

		public SourceRoots(ContentEntry contentEntry) {
			this.contentEntry = contentEntry;
			contentEntryFile = contentEntry.getFile();

			SourceFolder[] sourceFolders = contentEntry.getSourceFolders();
			for (SourceFolder sourceFolder : sourceFolders) {
				JpsModuleSourceRootType<?> rootType = sourceFolder.getRootType();
				if (rootType.getClass().getName().contains("ResourceRootType")) {
					continue;
				}
				VirtualFile file = sourceFolder.getFile();
				if (file != null) {
					paths.add(file);
				}
			}
		}

		public void appendTo(StringBuilder sb) {
			if (paths.isEmpty()) {
				return;
			} else if (paths.size() == 1) {
				sb.append(paths.get(0).getPath());
			} else {
				sb.append(contentEntryFile.getPath());
				sb.append("[subpaths=");
				for (VirtualFile file : paths) {
					String relativePath = VfsUtilCore.getRelativePath(file, contentEntryFile);
					sb.append(relativePath);
					sb.append(":");
				}
				sb.append("]");
			}
			sb.append(";");
		}
	}
}

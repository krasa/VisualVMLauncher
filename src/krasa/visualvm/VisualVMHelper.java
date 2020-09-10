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

import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.execution.configurations.RunConfigurationModule;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.MultiMap;
import krasa.visualvm.runner.VisualVMGenericDebuggerRunnerSettings;
import krasa.visualvm.runner.VisualVMGenericRunnerSettings;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.module.JpsModuleSourceRootType;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class VisualVMHelper {
	private static final Logger log = Logger.getInstance(VisualVMHelper.class.getName());

	public static void startVisualVM(long appId, String jdkHome, final Object thisInstance, Project project, Module runConfigurationModule) {
		VisualVMHelper.openInVisualVM(appId, jdkHome, thisInstance, project, runConfigurationModule);
	}

	public static void startVisualVM(ExecutionEnvironment env, VisualVMGenericDebuggerRunnerSettings genericDebuggerRunnerSettings, Object thisInstance, Project project) {
		startVisualVM(genericDebuggerRunnerSettings.getVisualVMId(), null, thisInstance, project, resolveModule(env));
	}

	public static void startVisualVM(ExecutionEnvironment env, VisualVMGenericRunnerSettings runnerSettings, Object thisInstance, Project project) {
		startVisualVM(runnerSettings.getVisualVMId(), null, thisInstance, project, resolveModule(env));

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

	public static void openInVisualVM(long id, String jdkHome, Object thisInstance, Project project, Module module) {
		PluginSettings pluginSettings = ApplicationSettingsComponent.getInstance().getState();

		String visualVmPath = pluginSettings.getVisualVmExecutable();
		String configuredJdkHome = pluginSettings.getJdkHome();
		if (StringUtils.isNotBlank(configuredJdkHome)) {
			jdkHome = configuredJdkHome;
		}

		String idString = String.valueOf(id);
		if (pluginSettings.isUseTabIndex()) {
			idString += "@" + pluginSettings.getTabIndex();
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
			run(id, jdkHome, thisInstance, project, visualVmPath, idString, pluginSettings.isSourceRoots(), module);
		}


	}

	private static void run(long id, String jdkHome, Object thisInstance, Project project, String visualVmPath, String idString, boolean sourceRoots, Module module) {
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
					addSourcePluginParameters(project, cmds, module);
				} catch (Throwable e) {
					log.error(e);
				}
			}

			log.info("Starting VisualVM with parameters:" + cmds);
			Runtime.getRuntime().exec(cmds.toArray(new String[0]));
		} catch (IOException e) {
			if (sourceRoots) {
				boolean contains = e.getMessage().contains("The filename or extension is too long");
				if (contains) {
					log.error("Please disable 'Integrate with VisualVM-GoToSource plugin' option at 'File | Settings | Other Settings | VisualVM Launcher'.\nThe command was too long: " + cmds.toString().length(), e);
					run(id, jdkHome, thisInstance, project, visualVmPath, idString, false, module);
					return;
				}
			}
			throw new RuntimeException(cmds.toString(), e);
		}
	}

	public static void addSourcePluginParameters(Project project, List<String> cmds, Module runConfigurationModule) {

		cmds.add("--source-roots");
		cmds.add("\"" + resolveSourceRoots(project, runConfigurationModule) + "\"");
		// --source-viewer="c:\NetBeans\bin\netbeans {file}:{line}"
		//https://www.jetbrains.com/help/idea/opening-files-from-command-line.html
		cmds.add("--source-viewer");
		String homePath = PathManager.getHomePath();
		if (SystemInfo.isWindows) {
			//idea.bat --line 42 C:\MyProject\scripts\numbers.js
			cmds.add("\"" + homePath + "\\bin\\idea.bat --line {line} {file}" + "\"");
		} else if (SystemInfo.isMac) {
			//idea --line <number> <path>
			cmds.add("\"" + homePath + "/bin/idea --line {line} {file}" + "\"");
		} else {
			//idea.sh --line <number> <path>
			cmds.add("\"" + homePath + "/bin/idea.sh --line {line} {file}" + "\"");
		}
	}

	@NotNull
	private static String resolveSourceRoots(Project project, Module runConfigurationModule) {

		//https://visualvm.github.io/sourcessupport.html
		// --source-roots="c:\sources\root1;c:\sources\root2[subpaths=src:test\src]"
		StringBuilder sb = new StringBuilder();

		Dependencies dependencies = new Dependencies();

		HashSet<Module> cycleProtection = new HashSet<>();
		if (runConfigurationModule != null) {
			addModuleDependencies(dependencies, runConfigurationModule, cycleProtection);
		} else {
			ModuleManager manager = ModuleManager.getInstance(project);
			Module[] modules = manager.getModules();
			for (Module module : modules) {
				addModuleDependencies(dependencies, module, cycleProtection);
			}
		}

		dependencies.appendTo(sb);

		String sourceRoots = removeLastSeparator(sb.toString());
		if (!SystemInfo.isWindows) {
			sourceRoots = sourceRoots.replace(";", ":");
		}
		if (SystemInfo.isWindows) {
			sourceRoots = sourceRoots.replace("/", "\\");
		}
		return sourceRoots;
	}

	public static Module resolveModule(ExecutionEnvironment env) {
		Module runConfigurationModule = null;
		if (env != null) {
			runConfigurationModule = resolveModule(env.getRunProfile());
		}
		return runConfigurationModule;
	}

	public static Module resolveModule(RunProfile runProfile) {
		Module runConfigurationModule = null;
		if (runProfile instanceof ModuleBasedConfiguration) {
			ModuleBasedConfiguration runProfilConfiguration = (ModuleBasedConfiguration) runProfile;
			RunConfigurationModule configurationModule = runProfilConfiguration.getConfigurationModule();
			if (configurationModule != null) {
				runConfigurationModule = configurationModule.getModule();
			}
		}
		return runConfigurationModule;
	}

	private static void addModuleDependencies(Dependencies dependencies, Module module, Set<Module> cycleProtection) {
		if (cycleProtection.contains(module)) {
			return;
		} else {
			cycleProtection.add(module);
		}

		ModuleRootManager root = ModuleRootManager.getInstance(module);
		OrderEntry[] orderEntries = root.getOrderEntries();
		for (OrderEntry orderEntry : orderEntries) {
			if (orderEntry instanceof ModuleOrderEntry) {
				Module moduleDep = ((ModuleOrderEntry) orderEntry).getModule();
				dependencies.addModule(moduleDep);
				addModuleDependencies(dependencies, moduleDep, cycleProtection);
			} else if (orderEntry instanceof ModuleSourceOrderEntry) {
				Module module1 = ((ModuleSourceOrderEntry) orderEntry).getRootModel().getModule();
				dependencies.addModule(module1);
			} else {
//				if (orderEntry instanceof LibraryOrderEntry || orderEntry instanceof InheritedJdkOrderEntry) {
//
//				} else {
//					System.err.println();
//				}
				VirtualFile[] sources = orderEntry.getFiles(OrderRootType.SOURCES);
				for (VirtualFile virtualFile : sources) {
					dependencies.add(virtualFile);
				}
			}

		}
	}

	private static String removeLastSeparator(String toString) {
		if (toString.endsWith(";")) {
			return toString.substring(0, toString.length() - 1);
		}
		return toString;
	}

	public static boolean isValidPath(String visualVmPath) {
		return !StringUtils.isBlank(visualVmPath) && new File(visualVmPath).exists();
	}


	private static class Dependencies {
		MultiMap<String, String> map = new MultiMap<>();
		Set<String> jars = new HashSet<>();
		Set<SourceRoots> sourceRoots = new HashSet<>();

		public void add(VirtualFile root) {
			String path = root.getPath();
			if (path.contains("!/")) {
				String subpath = StringUtil.substringAfter(path, "!/");
				String jar = StringUtil.substringBefore(path, "!/");
				map.putValue(jar, subpath);
			} else {
				jars.add(path);
			}
		}

		public void addModule(Module module) {
			ModuleRootManager root = ModuleRootManager.getInstance(module);
			ContentEntry[] contentEntries = root.getContentEntries();
			for (ContentEntry contentEntry : contentEntries) {
				sourceRoots.add(new SourceRoots(contentEntry));
			}
		}

		public void appendTo(StringBuilder sb) {
			for (SourceRoots sourceRoot : sourceRoots) {
				sourceRoot.appendTo(sb);
			}

			for (String jar : jars) {
				sb.append(jar);
				sb.append(";");
			}

			for (Map.Entry<String, Collection<String>> stringCollectionEntry : map.entrySet()) {
				String key = stringCollectionEntry.getKey();
				Collection<String> value = stringCollectionEntry.getValue();
				appendTo(sb, key, new HashSet<>(value));
			}
		}


		private void appendTo(StringBuilder sb, String key, Set<String> set) {
			set.remove("");

			if (set.isEmpty()) {
				sb.append(key);
			} else {
				sb.append(key);

				sb.append("[subpaths=");
				for (String s : set) {
					sb.append(s);
					sb.append(":");
				}
				removeLastSeparator(sb, ":");
				sb.append("]");
			}
			sb.append(";");
		}


		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder("Dependencies{" +
				"map=\n");

			sb.append("\nsourceRoot=");
			for (SourceRoots sourceRoot : sourceRoots) {
				sb.append("\n\t-");
				sourceRoot.appendTo(sb);
			}

			for (Map.Entry<String, Collection<String>> stringCollectionEntry : map.entrySet()) {
				sb.append("\n")
					.append(stringCollectionEntry.getKey());
				Collection<String> value = stringCollectionEntry.getValue();
				HashSet<String> strings = new HashSet<>(value);
				strings.remove("");
				for (String s : strings) {
					sb.append("\n\t-").append(s);
				}
			}
			sb.append("\njars=");
			for (String jar : jars) {
				sb.append("\n\t-").append(jar);
			}

			return sb.toString();
		}


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
				removeLastSeparator(sb, ":");
				sb.append("]");
			}
			sb.append(";");
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			SourceRoots that = (SourceRoots) o;

			return contentEntry != null ? contentEntry.equals(that.contentEntry) : that.contentEntry == null;
		}

		@Override
		public int hashCode() {
			return contentEntry != null ? contentEntry.hashCode() : 0;
		}
	}

	private static void removeLastSeparator(StringBuilder sb, String suffix) {
		if (sb.toString().endsWith(suffix)) {
			sb.setLength(sb.length() - 1);
		}
	}

}

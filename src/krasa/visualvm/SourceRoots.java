package krasa.visualvm;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.MultiMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.module.JpsModuleSourceRootType;

import java.io.File;
import java.util.*;

public class SourceRoots {

	@NotNull
	static String resolve(Project project, Module runConfigurationModule) {
		//https://visualvm.github.io/sourcessupport.html
		// --source-roots="c:\sources\root1;c:\sources\root2[subpaths=src:test\src]"

		SourceRoots sourceRoots = new SourceRoots();

		if (runConfigurationModule != null) {
			sourceRoots.addModuleDependencies(runConfigurationModule);
		} else {
			ModuleManager manager = ModuleManager.getInstance(project);
			Module[] modules = manager.getModules();
			for (Module module : modules) {
				sourceRoots.addModuleDependencies(module);
			}
		}

		return sourceRoots.getVisualVmParameter();
	}


	private MultiMap<String, String> jarsWithSubpaths = new MultiMap<>();
	private Set<String> jars = new HashSet<>();
	private Set<ModuleContentRoots> moduleContentRoots = new HashSet<>();

	private Set<Module> cycleProtection = new HashSet<>();

	private void addModuleDependencies(Module module) {
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
				addModule(moduleDep);
				addModuleDependencies(moduleDep);
			} else if (orderEntry instanceof ModuleSourceOrderEntry) {
				Module module1 = ((ModuleSourceOrderEntry) orderEntry).getRootModel().getModule();
				addModule(module1);
			} else {
				//				if (orderEntry instanceof LibraryOrderEntry || orderEntry instanceof InheritedJdkOrderEntry) {
				//
				//				} else {
				//					System.err.println();
				//				}
				VirtualFile[] sources = orderEntry.getFiles(OrderRootType.SOURCES);
				for (VirtualFile virtualFile : sources) {
					add(virtualFile);
				}
			}

		}
	}

	public void add(VirtualFile root) {
		String path = root.getPath();
		if (path.contains("!/")) {
			String jar = StringUtil.substringBefore(path, "!/");
			String subpath = StringUtil.substringAfter(path, "!/");
			jarsWithSubpaths.putValue(jar, subpath);
		} else {
			jars.add(path);
		}
	}

	public void addModule(Module module) {
		ModuleRootManager root = ModuleRootManager.getInstance(module);
		ContentEntry[] contentEntries = root.getContentEntries();
		for (ContentEntry contentEntry : contentEntries) {
			moduleContentRoots.add(new ModuleContentRoots(contentEntry));
		}
	}

	public String getVisualVmParameter() {
		StringBuilder sb = new StringBuilder();
		for (ModuleContentRoots sourceRoot : moduleContentRoots) {
			sourceRoot.appendTo(sb);
		}

		for (String jar : jars) {
			sb.append(jar);
			sb.append(File.pathSeparator);
		}

		for (Map.Entry<String, Collection<String>> stringCollectionEntry : jarsWithSubpaths.entrySet()) {
			String key = stringCollectionEntry.getKey();
			Collection<String> value = stringCollectionEntry.getValue();
			appendTo(sb, key, new HashSet<>(value));
		}


		String sourceRootsText = removeLastSeparator(sb.toString());
		if (SystemInfo.isWindows) {
			sourceRootsText = sourceRootsText.replace("/", "\\");
		}
		return sourceRootsText;
	}


	private void appendTo(StringBuilder sb, String key, Set<String> subpaths) {
		subpaths.remove("");

		if (subpaths.isEmpty()) {
			sb.append(key);
		} else {
			sb.append(key);

			sb.append("[subpaths=");
			for (String s : subpaths) {
				sb.append(s);
				sb.append(":");
			}
			removeLastSeparator(sb, ":");
			sb.append("]");
		}
		sb.append(File.pathSeparator);
	}


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Dependencies{" +
			"map=\n");

		sb.append("\nsourceRoot=");
		for (ModuleContentRoots sourceRoot : moduleContentRoots) {
			sb.append("\n\t-");
			sourceRoot.appendTo(sb);
		}

		for (Map.Entry<String, Collection<String>> stringCollectionEntry : jarsWithSubpaths.entrySet()) {
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

	private static class ModuleContentRoots {
		private final ContentEntry contentEntry;
		private final VirtualFile contentEntryFile;
		private final List<VirtualFile> paths = new ArrayList<>();

		public ModuleContentRoots(ContentEntry contentEntry) {
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
					sb.append(VfsUtilCore.getRelativePath(file, contentEntryFile));
					sb.append(":");
				}
				removeLastSeparator(sb, ":");
				sb.append("]");
			}
			sb.append(File.pathSeparator);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			ModuleContentRoots that = (ModuleContentRoots) o;

			return contentEntry != null ? contentEntry.equals(that.contentEntry) : that.contentEntry == null;
		}

		@Override
		public int hashCode() {
			return contentEntry != null ? contentEntry.hashCode() : 0;
		}
	}

	private static String removeLastSeparator(String toString) {
		if (toString.endsWith(File.pathSeparator)) {
			return toString.substring(0, toString.length() - 1);
		}
		return toString;
	}

	private static void removeLastSeparator(StringBuilder sb, String suffix) {
		if (sb.substring(sb.length() - 1).equals(suffix)) {
			sb.setLength(sb.length() - 1);
		}
	}

}

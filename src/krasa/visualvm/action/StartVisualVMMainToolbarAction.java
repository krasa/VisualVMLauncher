package krasa.visualvm.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.vfs.VirtualFile;
import krasa.visualvm.ApplicationSettingsService;
import krasa.visualvm.PluginSettings;
import krasa.visualvm.integration.VisualVMHelper;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.event.InputEvent;
import java.util.HashSet;
import java.util.Set;

public class StartVisualVMMainToolbarAction extends DumbAwareAction {

	public void actionPerformed(AnActionEvent e) {
		boolean ok = checkVisualVmExecutable();
		if (!ok) {
			return;
		}

		DefaultActionGroup defaultActionGroup = new DefaultActionGroup();
		defaultActionGroup.add(new MyDumbAwareAction("No JDK (system default)", null));
		defaultActionGroup.add(new Separator());

		Set<String> homes = jdkHomes();

		homes.stream().sorted().forEach(o -> defaultActionGroup.add(new MyDumbAwareAction(o, o)));

		ListPopup popup = JBPopupFactory.getInstance().createActionGroupPopup("Select JDK for --jdkhome VisualVM parameter", defaultActionGroup, e.getDataContext(), JBPopupFactory.ActionSelectionAid.ALPHA_NUMBERING, true, (Runnable) null, -1);
		InputEvent inputEvent = e.getInputEvent();
		if (inputEvent != null) {
			popup.showInCenterOf(inputEvent.getComponent());
		} else {
			popup.showInBestPositionFor(e.getDataContext());

		}
	}

	@NotNull
	private Set<String> jdkHomes() {
		Set<String> homes = new HashSet<>();

		PluginSettings state = ApplicationSettingsService.getInstance().getState();
		String configuredJdkHome = state.getJdkHome();
		if (StringUtils.isNotBlank(configuredJdkHome)) {
			homes.add(configuredJdkHome);
		}

		ProjectJdkTable projectJdkTable = ProjectJdkTable.getInstance();
		JavaSdk javaSdk = JavaSdk.getInstance();
		for (Sdk sdk : projectJdkTable.getAllJdks()) {
			if (sdk.getSdkType() == javaSdk) {
				homes.add(sdk.getHomePath());
			}
		}
		return homes;
	}

	private boolean checkVisualVmExecutable() {
		PluginSettings state = ApplicationSettingsService.getInstance().getState();
		String visualVmPath = state.getVisualVmExecutable();
		if (org.apache.commons.lang.StringUtils.isBlank(visualVmPath)) {
			final FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor();
			descriptor.setHideIgnored(true);

			descriptor.setTitle("Select VisualVM Executable");
			Project defaultProject = ProjectManager.getInstance().getDefaultProject();
			VirtualFile virtualFile = FileChooser.chooseFile(descriptor, defaultProject, null);
			if (virtualFile != null) {
				String path = virtualFile.getPath();
				state.setVisualVmExecutable(path);
			} else {
				return false;
			}
		}
		return true;
	}


	private static class MyDumbAwareAction extends DumbAwareAction {
		private final String homePath;

		public MyDumbAwareAction(String name, String homePath) {
			super(name);
			this.homePath = homePath;
		}

		@Override
		public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
			VisualVMHelper.startVisualVM(anActionEvent.getProject(), homePath);
		}
	}
}

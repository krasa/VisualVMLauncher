package krasa.visualvm;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

public class SettingsDialog {
	private JTextField visualVmExecutable;
	private JComponent rootComponent;
	private JButton browseButton;

	public SettingsDialog() {
		browseButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				browseForFile(visualVmExecutable);
			}
		});
	}

	private void browseForFile(@NotNull final JTextField target) {
		final FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor();
		descriptor.setHideIgnored(true);

		descriptor.setTitle("Select VisualVM home");
		String text = target.getText();
		final VirtualFile toSelect = text == null || text.isEmpty() ? null
				: LocalFileSystem.getInstance().findFileByPath(text);

		// 10.5 does not have #chooseFile
        Project defaultProject = ProjectManager.getInstance().getDefaultProject();
        VirtualFile[] virtualFile = FileChooser.chooseFiles(defaultProject, descriptor, toSelect);
		if (virtualFile != null && virtualFile.length > 0) {
			target.setText(virtualFile[0].getPath());
		}

	}

	public void setData(PluginSettings data) {
		visualVmExecutable.setText(data.getVisualVmExecutable());
	}

	public void getData(PluginSettings data) {
		data.setVisualVmExecutable(visualVmExecutable.getText());
	}

	public boolean isModified(PluginSettings data) {
		if (visualVmExecutable.getText() != null ? !visualVmExecutable.getText().equals(data.getVisualVmExecutable())
				: data.getVisualVmExecutable() != null)
			return true;
		return false;
	}

	public JComponent getRootComponent() {
		return rootComponent;
	}
}

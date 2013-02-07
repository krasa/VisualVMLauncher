package krasa.visualvm;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import java.text.NumberFormat;
import java.text.ParseException;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

public class SettingsDialog {
	private JTextField visualVmExecutable;
	private JComponent rootComponent;
	private JButton browseButton;
	private JLabel validationMessageLabel;
	private JCheckBox debugCheckBox;
	private JFormattedTextField duration;
	private JLabel durationLabel;

	public SettingsDialog() {
		super();

		NumberFormatter defaultFormat = new NumberFormatter();
		NumberFormat integerInstance = NumberFormat.getIntegerInstance();
		integerInstance.setGroupingUsed(false);
		defaultFormat.setFormat(integerInstance
		);
		DefaultFormatterFactory tf = new DefaultFormatterFactory(defaultFormat);
		duration.setFormatterFactory(tf
		);
//		duration.setInputVerifier(new InputVerifier() {
//			@Override
//			public boolean verify(JComponent input) {
//				if (input instanceof JFormattedTextField) {
//					JFormattedTextField ftf = (JFormattedTextField) input;
//					JFormattedTextField.AbstractFormatter formatter = ftf.getFormatter();
//					if (formatter != null) {
//						String text = ftf.getText();
//						try {
//							formatter.stringToValue(text);
//						} catch (ParseException e) {
//							return false;
//						}
//						return true;
//					}
//				}
//				return true;
//			}
//		});
		browseButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				browseForFile(visualVmExecutable);
			}
		});
		visualVmExecutable.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void changedUpdate(DocumentEvent e) {
				updateLabel(e);
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				updateLabel(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				updateLabel(e);
			}

			private void updateLabel(DocumentEvent e) {
				java.awt.EventQueue.invokeLater(new Runnable() {

					@Override
					public void run() {
						setValidationMessage(visualVmExecutable.getText());
					}
				});
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

	private void setValidationMessage(String visualVmExecutable1) {
		if (StringUtils.isBlank(visualVmExecutable1)) {
			validationMessageLabel.setText("Path is required");
		} else if (!new File(visualVmExecutable1).exists()) {
			validationMessageLabel.setText("File does not exists");
		} else {
			validationMessageLabel.setText("");
		}
	}

	public JComponent getRootComponent() {
		return rootComponent;
	}

	public void setData(PluginSettings data) {
		visualVmExecutable.setText(data.getVisualVmExecutable());
		debugCheckBox.setSelected(data.getDebug());
		duration.setText(data.getDurationToSetContextToButton());
	}

	public void getData(PluginSettings data) {
		data.setVisualVmExecutable(visualVmExecutable.getText());
		data.setDebug(debugCheckBox.isSelected());
		data.setDurationToSetContextToButton(duration.getText());
	}

	public boolean isModified(PluginSettings data) {
		if (visualVmExecutable.getText() != null ? !visualVmExecutable.getText().equals(data.getVisualVmExecutable()) : data.getVisualVmExecutable() != null)
			return true;
		if (debugCheckBox.isSelected() != data.getDebug()) return true;
		if (duration.getText() != null ? !duration.getText().equals(data.getDurationToSetContextToButton()) : data.getDurationToSetContextToButton() != null)
			return true;
		return false;
	}

}

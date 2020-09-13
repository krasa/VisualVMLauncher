package krasa.visualvm;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class MyConfigurable implements Configurable {
	private SettingsDialog form;

	public static boolean openSettingsIfNotConfigured(Project project) {
		ApplicationSettingsService instance = ApplicationSettingsService.getInstance();
		PluginSettings state = instance.getState();
		boolean ok = true;
		if (!PluginSettings.isValid(state)) {
			ok = ShowSettingsUtil.getInstance().editConfigurable(project, new MyConfigurable());
		}
		return ok;
	}

	@Nls
	public String getDisplayName() {
		return "VisualVM Launcher";
	}

	@Nullable
	public Icon getIcon() {
		return null;
	}

	@Nullable
	@NonNls
	public String getHelpTopic() {
		return null;
	}

	public JComponent createComponent() {
		if (form == null) {
			form = new SettingsDialog();
		}
		return form.getRootComponent();
	}

	public boolean isModified() {
		return form.isModified(ApplicationSettingsService.getInstance().getState());
	}

	public void apply() throws ConfigurationException {
		PluginSettings settings = ApplicationSettingsService.getInstance().getState();
		if (form != null) {
			form.getData(settings);
		}
	}

	public void reset() {
		PluginSettings settings = ApplicationSettingsService.getInstance().getState();
		if (form != null) {
			form.setDataCustom(settings);
		}
	}

	public void disposeUIResources() {
		form = null;
	}
}

package krasa.visualvm;

import javax.swing.*;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.ShowSettingsUtil;

@State(name = "VisualVMLauncher", storages = { @Storage(id = "VisualVMLauncher", file = "$APP_CONFIG$/VisualVMLauncher.xml") })
public class ApplicationSettingsComponent implements ApplicationComponent, Configurable,
		PersistentStateComponent<PluginSettings> {
	private static final Logger log = Logger.getInstance(ApplicationSettingsComponent.class.getName());

	private PluginSettings settings = new PluginSettings();
	private SettingsDialog form;

	public static ApplicationSettingsComponent getInstance() {
		return ApplicationManager.getApplication().getComponent(ApplicationSettingsComponent.class);
	}

	public static boolean openSettingsIfNotConfigured(Project project) {
		ApplicationSettingsComponent instance = getInstance();
		PluginSettings state = instance.getState();
		boolean b = true;
		if (state == null || state.getVisualVmExecutable() == null || state.getVisualVmExecutable().isEmpty()) {
			b = ShowSettingsUtil.getInstance().editConfigurable(project, instance);
		}
		return b;
	}

	public String getVisualVmHome() {
		return settings.getVisualVmExecutable();
	}

	// ApplicationComponent

	@NotNull
	public String getComponentName() {
		return "VisualVMLauncher";
	}

	@Override
	public void initComponent() {
	}

	public void disposeComponent() {
	}

	// Configurable

	@Nls
	public String getDisplayName() {
		return "VisualVM Launcher";
	}

	@Nullable
	public Icon getIcon() {
		return Resources.LOGO_32;
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
		return getForm().isModified(settings);
	}

	public void apply() throws ConfigurationException {
		if (form != null) {
			settings = form.exportDisplayedSettings();
		}
	}

	public void reset() {
		if (form != null) {
			form.importFrom(settings);
		}
	}

	private SettingsDialog getForm() {
		if (form == null) {
			form = new SettingsDialog();
		}
		return form;
	}

	public void disposeUIResources() {
		form = null;
	}

	public PluginSettings getState() {
		return settings;
	}

	public void loadState(PluginSettings state) {
		this.settings = state;
	}
}

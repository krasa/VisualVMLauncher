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

@State(name = "VisualVMLauncher", storages = {@Storage(id = "VisualVMLauncher", file = "$APP_CONFIG$/VisualVMLauncher.xml")})
public class ApplicationSettingsComponent implements ApplicationComponent,
		PersistentStateComponent<PluginSettings> {
	private static final Logger log = Logger.getInstance(ApplicationSettingsComponent.class.getName());

	private PluginSettings settings = new PluginSettings();

	public static ApplicationSettingsComponent getInstance() {
		return ApplicationManager.getApplication().getComponent(ApplicationSettingsComponent.class);
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


	@NotNull
	public PluginSettings getState() {

		if (settings == null) {
			settings = new PluginSettings();
		}
		return settings;
	}

	public void loadState(PluginSettings state) {
		this.settings = state;
		LogHelper.debug = settings.getDebug();
	}
}

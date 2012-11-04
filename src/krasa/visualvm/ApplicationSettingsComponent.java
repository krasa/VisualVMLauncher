/*
 * This file is part of VisualVM for IDEA
 *
 * Copyright (c) 2008, Esko Luontola. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *
 *     * Neither the name of the copyright holder nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package krasa.visualvm;

import javax.swing.*;

import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.ShowSettingsUtil;
import krasa.visualvm.SettingsDialog;
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

@State(name = "VisualVMLauncher", storages = { @Storage(id = "VisualVMLauncher", file = "$APP_CONFIG$/VisualVMLauncher.xml") })
public class ApplicationSettingsComponent implements ApplicationComponent, Configurable,
		PersistentStateComponent<PluginSettings> {
	private static final Logger log = Logger.getInstance(ApplicationSettingsComponent.class.getName());

	private PluginSettings settings = new PluginSettings();
	private SettingsDialog form;

	public static ApplicationSettingsComponent getInstance() {
		return ApplicationManager.getApplication().getComponent(ApplicationSettingsComponent.class);
	}

    public static boolean openSettingsIfNotConfigured(ExecutionEnvironment environment) {
        ApplicationSettingsComponent instance = getInstance();
        PluginSettings state = instance.getState();
        boolean b = true;
        if (state == null || state.getVisualVmExecutable() == null || state.getVisualVmExecutable().isEmpty()) {
            b = ShowSettingsUtil.getInstance().editConfigurable(environment.getProject(), instance);
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

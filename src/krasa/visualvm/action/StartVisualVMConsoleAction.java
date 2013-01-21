package krasa.visualvm.action;

import krasa.visualvm.ApplicationSettingsComponent;
import krasa.visualvm.Resources;
import krasa.visualvm.VisualVMContext;
import krasa.visualvm.VisualVMHelper;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;

public class StartVisualVMConsoleAction extends AnAction {
	private VisualVMContext visualVMContext;

	public StartVisualVMConsoleAction(VisualVMContext visualVMContext) {
		super("Start VisualVM", null, Resources.CONSOLE_RUN);
		this.visualVMContext = visualVMContext;
	}

	@Override
	public void update(AnActionEvent e) {
		super.update(e);
		final Presentation presentation = e.getPresentation();
		if (!VisualVMContext.isValid(visualVMContext)) {
			presentation.setVisible(false);
			presentation.setEnabled(false);
		}
	}

	@Override
	public void actionPerformed(final AnActionEvent e) {
		if (!ApplicationSettingsComponent.openSettingsIfNotConfigured(e.getProject())) {
			return;
		}
		VisualVMHelper.startVisualVM(visualVMContext.getAppId(), visualVMContext.getJdkPath());
	}

}

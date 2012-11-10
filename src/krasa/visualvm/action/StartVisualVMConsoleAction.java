package krasa.visualvm.action;

import com.intellij.openapi.actionSystem.Presentation;
import krasa.visualvm.Resources;
import krasa.visualvm.VisualVMContext;
import krasa.visualvm.VisualVMHelper;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class StartVisualVMConsoleAction extends AnAction {
	private VisualVMContext visualVMContext;

	public StartVisualVMConsoleAction(VisualVMContext visualVMContext) {
		super("Start VisualVM", null, Resources.RUN);
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
		VisualVMHelper.startVisualVM(visualVMContext.getAppId(), visualVMContext.getJdkPath());
	}

}

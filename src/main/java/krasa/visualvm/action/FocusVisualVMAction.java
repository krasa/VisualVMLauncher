package krasa.visualvm.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.util.NlsActions;
import krasa.visualvm.integration.VisualVMHelper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class FocusVisualVMAction extends MyDumbAwareAction {
	public FocusVisualVMAction() {
	}

	public FocusVisualVMAction(@Nullable @NlsActions.ActionText String text, @Nullable @NlsActions.ActionDescription String description, @Nullable Icon icon) {
		super(text, description, icon);
	}

	public void actionPerformed(AnActionEvent e) {
		VisualVMHelper.executeVisualVM(e.getProject(),  "--window-to-front");
	}

}

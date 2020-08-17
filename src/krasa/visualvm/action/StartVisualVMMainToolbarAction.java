package krasa.visualvm.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import krasa.visualvm.VisualVMHelper;

public class StartVisualVMMainToolbarAction extends DumbAwareAction {

	public void actionPerformed(AnActionEvent e) {
		VisualVMHelper.startVisualVM();

	}


}

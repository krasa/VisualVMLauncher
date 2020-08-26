package krasa.visualvm.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import krasa.visualvm.VisualVMHelper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class DummyTestAction extends AnAction {
	@Override
	public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
		VisualVMHelper.addSourceRoots(anActionEvent.getProject(), new ArrayList<>());
	}
}

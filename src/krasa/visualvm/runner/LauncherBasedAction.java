package krasa.visualvm.runner;

import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ProcessProxyFactory;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;

import javax.swing.*;

public abstract class LauncherBasedAction extends AnAction {
    protected final ProcessHandler myProcessHandler;

    LauncherBasedAction(String text, String description, Icon icon, ProcessHandler processHandler) {
        super(text, description, icon);
        myProcessHandler = processHandler;
    }

    @Override
    public void update(final AnActionEvent event) {
        final Presentation presentation = event.getPresentation();
        if (!isVisible()) {
            presentation.setVisible(false);
            presentation.setEnabled(false);
            return;
        }
        presentation.setVisible(true);
        presentation.setEnabled(!myProcessHandler.isProcessTerminated());
    }

    protected boolean isVisible() {
        return ProcessProxyFactory.getInstance().getAttachedProxy(myProcessHandler) != null;
    }
}

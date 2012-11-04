package krasa.visualvm.runner;

import java.io.IOException;

import krasa.visualvm.ApplicationSettingsComponent;
import krasa.visualvm.ApplicationSettingsComponent;
import krasa.visualvm.Resources;
import krasa.visualvm.VisualVMHelper;

import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ProcessProxy;
import com.intellij.execution.runners.ProcessProxyFactory;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class StartVisualVMAction extends LauncherBasedAction {

	protected final long appId;
    protected String jdkHome;

    public StartVisualVMAction(final ProcessHandler processHandler, long appId, String jdkHome1) {
		super("StartVisualVM", null, Resources.LOGO_16, processHandler);
		this.appId = appId;
		this.jdkHome = jdkHome1;
	}

	@Override
	public void actionPerformed(final AnActionEvent e) {
		ProcessProxy proxy = ProcessProxyFactory.getInstance().getAttachedProxy(myProcessHandler);
		if (proxy != null) {
			startVisualVM();
		}
	}

	protected void startVisualVM() {
		try {
            VisualVMHelper.openInVisualVM(appId, ApplicationSettingsComponent.getInstance().getVisualVmHome(),
                    jdkHome);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}

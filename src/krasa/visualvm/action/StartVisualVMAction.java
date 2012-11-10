package krasa.visualvm.action;

import krasa.visualvm.Resources;
import krasa.visualvm.VisualVMHelper;

import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ProcessProxy;
import com.intellij.execution.runners.ProcessProxyFactory;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class StartVisualVMAction extends LauncherBasedAction {

	protected final long appId;
	protected String jdkHome;

	public StartVisualVMAction(final ProcessHandler processHandler, long appId, String jdkHome) {
		super("Start VisualVM", null, Resources.RUN, processHandler);
		this.appId = appId;
		this.jdkHome = jdkHome;
	}

	@Override
	public void actionPerformed(final AnActionEvent e) {
		ProcessProxy proxy = ProcessProxyFactory.getInstance().getAttachedProxy(myProcessHandler);
		if (proxy != null) {
			VisualVMHelper.startVisualVM(appId, jdkHome);
		}
	}
}

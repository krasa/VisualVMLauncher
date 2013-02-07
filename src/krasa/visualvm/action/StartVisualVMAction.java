package krasa.visualvm.action;

import com.intellij.debugger.DebuggerBundle;
import com.intellij.debugger.DebuggerManagerEx;
import com.intellij.debugger.engine.DebugProcessImpl;
import com.intellij.debugger.engine.events.DebuggerCommandImpl;
import com.intellij.debugger.impl.DebuggerContextImpl;
import com.intellij.debugger.impl.DebuggerSession;
import com.intellij.debugger.jdi.VirtualMachineProxyImpl;
import com.intellij.debugger.ui.DebuggerPanelsManager;
import com.intellij.debugger.ui.DebuggerSessionTab;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.unscramble.ThreadDumpParser;
import com.intellij.unscramble.ThreadState;
import com.intellij.util.SmartList;
import gnu.trove.TIntObjectHashMap;
import java.lang.management.MonitorInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import krasa.visualvm.Resources;
import krasa.visualvm.VisualVMHelper;

import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ProcessProxy;
import com.intellij.execution.runners.ProcessProxyFactory;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jdesktop.swingx.renderer.BooleanValue;
import org.picocontainer.defaults.ObjectReference;

public class StartVisualVMAction extends AnAction implements AnAction.TransparentUpdate {

	public void actionPerformed(AnActionEvent e) {
		final Project project = PlatformDataKeys.PROJECT.getData(e.getDataContext());
		if (project == null) {
			return;
		}
		DebuggerContextImpl context = (DebuggerManagerEx.getInstanceEx(project)).getContext();

		final DebuggerSession session = context.getDebuggerSession();
		if (session != null && session.isAttached()) {
			final DebugProcessImpl process = context.getDebugProcess();
		}
	}


	public void update(AnActionEvent event) {
		Presentation presentation = event.getPresentation();
		Project project = PlatformDataKeys.PROJECT.getData(event.getDataContext());
		if (project == null) {
			presentation.setEnabled(false);
			return;
		}
		DebuggerSession debuggerSession = (DebuggerManagerEx.getInstanceEx(project)).getContext().getDebuggerSession();
		presentation.setEnabled(debuggerSession != null && debuggerSession.isAttached());
	}
}

package krasa.visualvm.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import krasa.visualvm.*;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class StartVisualVMConsoleAction extends AnAction {
	private VisualVMContext visualVMContext;
	private boolean postConstructContextSet;
	private long created;

	public static volatile List<StartVisualVMConsoleAction> currentlyExecuted = new LinkedList<StartVisualVMConsoleAction>();

	public StartVisualVMConsoleAction() {
	}

	public StartVisualVMConsoleAction(VisualVMContext visualVMContext) {
		super("Start VisualVM", null, Resources.CONSOLE_RUN);
		this.visualVMContext = visualVMContext;
		created = System.currentTimeMillis();
		currentlyExecuted.add(this);
		LogHelper.print("created with " + visualVMContext, this);
	}

	@Override
	public void update(AnActionEvent e) {
		super.update(e);
		final Presentation presentation = e.getPresentation();
		if (!VisualVMContext.isValid(visualVMContext)) {
//			presentation.setVisible(false);
			presentation.setEnabled(false);
		}
	}

	@Override
	public void actionPerformed(final AnActionEvent e) {
		if (!MyConfigurable.openSettingsIfNotConfigured(e.getProject())) {
			return;
		}
		VisualVMHelper.startVisualVM(visualVMContext.getAppId(), visualVMContext.getJdkPath(), e.getProject(), visualVMContext.getModule(), this);
	}

	public void setVisualVMContext(VisualVMContext visualVMContext) {
		if (postConstructContextSet) {
			LogHelper.print("setVisualVMContext false with " + visualVMContext, this);
		} else {
			postConstructContextSet = true;
			LogHelper.print("setVisualVMContext " + visualVMContext, this);
			this.visualVMContext = visualVMContext;
		}
	}

	public long getCreated() {
		return created;
	}

	public static void setVisualVMContextToRecentlyCreated(VisualVMContext visualVMContext) {
		LogHelper.print("#setVisualVMContextToRecentlyCreated" + visualVMContext, null);
		Iterator<StartVisualVMConsoleAction> iterator = currentlyExecuted.iterator();
		while (iterator.hasNext()) {
			StartVisualVMConsoleAction next = iterator.next();
			if (isRecentlyCreated(next)) {
				next.setVisualVMContext(visualVMContext);
			} else {
				LogHelper.print("#setVisualVMContextToRecentlyCreated remove", null);
				iterator.remove();
			}
		}
	}

	private static boolean isRecentlyCreated(StartVisualVMConsoleAction next) {
		long l = System.currentTimeMillis() - next.getCreated();
		LogHelper.print("#isRecentlyCreated " + l + " " + next, null);
		return l < ApplicationSettingsComponent.getInstance().getState().getDurationToSetContextToButtonAsLong();
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("StartVisualVMConsoleAction");
		sb.append("{visualVMContext=").append(visualVMContext);
		sb.append(", created=").append(created);
		sb.append('}');
		return sb.toString();
	}
}

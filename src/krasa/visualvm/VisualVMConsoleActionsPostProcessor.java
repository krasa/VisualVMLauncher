package krasa.visualvm;

import java.util.ArrayList;

import krasa.visualvm.action.StartVisualVMConsoleAction;

import org.jetbrains.annotations.NotNull;

import com.intellij.execution.actions.ConsoleActionsPostProcessor;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.AnAction;

public class VisualVMConsoleActionsPostProcessor implements ConsoleActionsPostProcessor {

	@NotNull
	@Override
	public AnAction[] postProcess(@NotNull ConsoleView console, @NotNull AnAction[] actions) {
		VisualVMContext context = VisualVMContext.load();
		if (VisualVMContext.isValid(context)) {
			ArrayList<AnAction> anActions = new ArrayList<AnAction>();
			anActions.add(new StartVisualVMConsoleAction(context));
			return anActions.toArray(new AnAction[anActions.size()]);
		}
		return actions;
	}
}

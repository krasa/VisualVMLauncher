package krasa.visualvm.integration;

import com.intellij.execution.actions.ConsoleActionsPostProcessor;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.diagnostic.Logger;
import krasa.visualvm.action.StartVisualVMConsoleAction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

public class VisualVMConsoleActionsPostProcessor extends ConsoleActionsPostProcessor {
	private static final Logger log = Logger.getInstance(VisualVMConsoleActionsPostProcessor.class.getName());

	@NotNull
	@Override
	public AnAction[] postProcess(@NotNull ConsoleView console, @NotNull AnAction[] actions) {
		VisualVMContext context = VisualVMContext.load();
		ArrayList<AnAction> anActions = new ArrayList<AnAction>();
		anActions.add(new StartVisualVMConsoleAction(context));
		anActions.addAll(Arrays.asList(actions));
		return anActions.toArray(new AnAction[anActions.size()]);
	}
}

package krasa.visualvm.runner;

import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.openapi.util.DefaultJDOMExternalizer;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import krasa.visualvm.VisualVMHelper;
import org.jdom.Element;

public class VisualVMGenericRunnerSettings implements RunnerSettings {
	protected long visualVMId;

	public VisualVMGenericRunnerSettings() {
		visualVMId = VisualVMHelper.getNextID();
	}

	public long getVisualVMId() {
		return visualVMId;
	}

	public void setVisualVMId(long visualVMId) {
		this.visualVMId = visualVMId;
	}

	public void generateId() {
		visualVMId = VisualVMHelper.getNextID();
	}

	@Override
	public void readExternal(Element element) throws InvalidDataException {
		DefaultJDOMExternalizer.readExternal(this, element);
	}

	@Override
	public void writeExternal(Element element) throws WriteExternalException {
		DefaultJDOMExternalizer.writeExternal(this, element);
	}
}

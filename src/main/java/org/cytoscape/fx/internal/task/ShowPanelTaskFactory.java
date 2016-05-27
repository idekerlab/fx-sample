package org.cytoscape.fx.internal.task;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.fx.internal.ws.ProcessManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class ShowPanelTaskFactory extends AbstractTaskFactory {

	private final CySwingApplication cySwingApplicationServiceRef;
	private final CyServiceRegistrar registrar;
	private final CyApplicationConfiguration appConfig;
	private final OpenBrowser browser;
	private final ProcessManager pm;

	public ShowPanelTaskFactory(final CyServiceRegistrar registrar,
			final CySwingApplication cySwingApplicationServiceRef, 
			final CyApplicationConfiguration appConfig,
			final OpenBrowser browser, ProcessManager pm) {
		this.cySwingApplicationServiceRef = cySwingApplicationServiceRef;
		this.registrar = registrar;
		this.appConfig = appConfig;
		this.browser = browser;
		this.pm = pm;
	}

	@Override
	public TaskIterator createTaskIterator() {
//		return new TaskIterator(new OpenNdexValet(appConfig, browser));
		return new TaskIterator(new ShowPanelTask(registrar, cySwingApplicationServiceRef, appConfig, pm));
	}
}
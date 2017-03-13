package de.unipaderborn.visuflow.debug.handlers;

import java.util.Collections;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import de.unipaderborn.visuflow.VisuflowConstants;
import de.unipaderborn.visuflow.util.ServiceUtil;

public class DebugStepOverHandler extends org.eclipse.core.commands.AbstractHandler implements VisuflowConstants {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		EventAdmin ea = ServiceUtil.getService(EventAdmin.class);
		Event stepOver = new Event(EA_TOPIC_DEBUGGING_ACTION_STEP_OVER, Collections.emptyMap());
		ea.sendEvent(stepOver);

		// has to be null (see javadoc)
		return null;
	}
}

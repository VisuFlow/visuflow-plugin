package de.unipaderborn.visuflow.debug.handlers;

import java.util.Collections;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import de.unipaderborn.visuflow.util.ServiceUtil;

public class DebugStepOverHandler extends org.eclipse.core.commands.AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		EventAdmin ea = ServiceUtil.getService(EventAdmin.class);
		Event stepOver = new Event("de/unipaderborn/visuflow/debug/stepOver", Collections.emptyMap());
		ea.sendEvent(stepOver);

		// has to be null (see javadoc)
		return null;
	}
}

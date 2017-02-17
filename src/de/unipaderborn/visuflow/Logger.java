package de.unipaderborn.visuflow;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Status;

public class Logger {
	private ILog log;

	public Logger(ILog iLog) {
		this.log = iLog;
	}

	public void info(String msg) {
		log(Status.INFO, msg);
	}

	public void warn(String msg) {
		log(Status.WARNING, msg);
	}

	public void warn(String msg, Throwable exception) {
		log(Status.WARNING, msg, exception);
	}

	public void error(String msg) {
		log(Status.ERROR, msg);
	}

	public void error(String msg, Throwable exception) {
		log(Status.ERROR, msg, exception);
	}

	private void log(int level, String msg) {
		log.log(new Status(level, Visuflow.getDefault().getBundle().getSymbolicName(), msg));
	}

	private void log(int level, String msg, Throwable exception) {
		log.log(new Status(level, Visuflow.getDefault().getBundle().getSymbolicName(), msg, exception));
	}
}

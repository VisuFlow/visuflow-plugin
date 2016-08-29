package de.visuflow.reporting;

import soot.SootMethod;
import soot.Unit;

public interface IReporter {

	public void report(SootMethod method, Unit statement);

	public void report(SootMethod method, Unit source, Unit sink);

}

package de.unipaderborn.visuflow.ui.graph.formatting;

import soot.Unit;
import soot.jimple.InvokeStmt;

public class InvokeStmtFormatter implements UnitFormatter {

	@Override
	public String format(Unit u, int maxLength) {
		InvokeStmt invoke = (InvokeStmt) u;
		return ValueFormatter.format(invoke.getInvokeExpr());
	}

}

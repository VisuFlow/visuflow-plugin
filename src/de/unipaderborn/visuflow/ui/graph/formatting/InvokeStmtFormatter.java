package de.unipaderborn.visuflow.ui.graph.formatting;

import soot.Unit;
import soot.jimple.InvokeStmt;

public class InvokeStmtFormatter implements UnitFormatter {

	@Override
	public String format(Unit u, int maxLength) {
		InvokeStmt invoke = (InvokeStmt) u;
		String s = ValueFormatter.format(invoke.getInvokeExpr());

		if(s.length() > maxLength) {
			s = s.substring(0, maxLength) + "\u2026";
		}

		return s;
	}

}

package de.unipaderborn.visuflow.ui.graph.formatting;

import soot.Unit;
import soot.jimple.AssignStmt;

public class AssignStmtFormatter implements UnitFormatter {

	@Override
	public String format(Unit u, int maxLength) {
		AssignStmt assignStmt = (AssignStmt) u;
		String s = ValueFormatter.format(assignStmt.getLeftOp()) + " = " + ValueFormatter.format(assignStmt.getRightOp());

		if(s.length() > maxLength) {
			s = s.substring(0, maxLength) + "\u2026";
		}

		return s;
	}
}

package de.unipaderborn.visuflow.ui.graph.formatting;

import soot.Unit;
import soot.jimple.IdentityStmt;

public class IdentityStmtFormatter implements UnitFormatter {

	@Override
	public String format(Unit u, int maxLength) {
		IdentityStmt identityStmt = (IdentityStmt) u;
		String s = ValueFormatter.format(identityStmt.getLeftOp()) + " := " + ValueFormatter.format(identityStmt.getRightOp());

		if(s.length() > maxLength) {
			s = s.substring(0, maxLength) + "\u2026";
		}

		return s;
	}
}

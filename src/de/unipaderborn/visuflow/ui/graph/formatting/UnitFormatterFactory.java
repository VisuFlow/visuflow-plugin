package de.unipaderborn.visuflow.ui.graph.formatting;

import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeStmt;

public class UnitFormatterFactory {

	public static UnitFormatter createFormatter(Unit u) {
		if(u instanceof AssignStmt) {
			return new AssignStmtFormatter();
		} else if (u instanceof InvokeStmt) {
			return new InvokeStmtFormatter();
		}

		// no special formatter found, return the default one
		return new DefaultFormatter();
	}
}

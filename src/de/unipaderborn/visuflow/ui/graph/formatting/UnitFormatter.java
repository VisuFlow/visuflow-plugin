package de.unipaderborn.visuflow.ui.graph.formatting;

import soot.Unit;

public interface UnitFormatter {
	public String format(Unit u, int maxLength);
}

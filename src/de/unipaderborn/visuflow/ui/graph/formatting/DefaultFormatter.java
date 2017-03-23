package de.unipaderborn.visuflow.ui.graph.formatting;

import soot.Unit;

public class DefaultFormatter implements UnitFormatter {

	@Override
	public String format(Unit u, int maxLength) {
		String s = u.toString();
		if(s.length() > maxLength) {
			s = s.substring(0, maxLength) + "\u2026";
		}
		return s;
	}
}

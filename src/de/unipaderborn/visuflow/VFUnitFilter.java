package de.unipaderborn.visuflow;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import de.unipaderborn.visuflow.model.VFUnit;

public class VFUnitFilter extends ViewerFilter {

	private String searchString;

	public void setSearchText(String s) {
		// ensure that the value can be used for matching
		this.searchString = ".*" + s + ".*";
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (searchString == null || searchString.length() == 0) {
			return true;
		}
		VFUnit unit = (VFUnit) element;
		if (unit.getUnit().toString().matches(searchString)) {
			return true;
		}
		if (unit.getUnit().getClass().getName().matches(searchString)) {
			return true;
		}

		return false;
	}
}
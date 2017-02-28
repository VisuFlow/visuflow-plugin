package de.unipaderborn.visuflow.ui.view.filter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;

import de.unipaderborn.visuflow.Visuflow;
import de.unipaderborn.visuflow.model.VFUnit;

public class ReturnPathFilter extends FilteredItemsSelectionDialog {
	
	List<VFUnit> paths = new ArrayList<>();
	
	public void setPaths(List paths){
		this.paths = paths;
	}
	
	public ReturnPathFilter(Shell shell) {
		   super(shell);
		   setTitle("Return Path Selection");
		   setSelectionHistory(new PathSelectionHistory());
	}
	
	protected Control createExtendedContentArea(Composite arg0) {
		return null;
	}

	@Override
	protected ItemsFilter createFilter() {
		return new ItemsFilter() {
	         public boolean matchItem(Object item) {
	            return matches(item.toString());
	         }
	         public boolean isConsistentItem(Object item) {
	            return true;
	         }
	      };
	}

	@Override
	protected void fillContentProvider(AbstractContentProvider contentProvider, ItemsFilter itemsFilter, IProgressMonitor progressMonitor) throws CoreException {
		      progressMonitor.beginTask("Searching", paths.size());
		      for (Iterator iter = paths.iterator(); iter.hasNext();) {
		         contentProvider.add(((VFUnit)iter.next()), itemsFilter);
		         progressMonitor.worked(1);
		      }
		      progressMonitor.done();
	}
	
	   protected IDialogSettings getDialogSettings() {
	      	IDialogSettings settings = Visuflow.getDefault().getDialogSettings();
			return settings;
	   }

	@Override
	public String getElementName(Object item) {
		return item.toString();
	}

	protected Comparator getItemsComparator() {
	      return new Comparator() {
	         public int compare(Object arg0, Object arg1) {
	            return arg0.toString().compareTo(arg1.toString());
	         }
	      };
	   }

	@Override
	protected IStatus validateItem(Object item) {
		return Status.OK_STATUS;
	}
	
	private class PathSelectionHistory extends SelectionHistory {
		   protected Object restoreItemFromMemento(IMemento element) {
			  return null; 
		   }
		   protected void storeItemToMemento(Object item, IMemento element) {
		   }
	}

}

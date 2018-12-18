package de.unipaderborn.visuflow.ui.view;

import java.util.Hashtable;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import de.unipaderborn.visuflow.VisuflowConstants;
import de.unipaderborn.visuflow.model.DataModel;
import de.unipaderborn.visuflow.model.impl.EventDatabase;
import de.unipaderborn.visuflow.util.ServiceUtil;

public class TimelineView extends ViewPart implements EventHandler{
	
	private TableViewer viewer;
	private List<de.unipaderborn.visuflow.model.impl.Event> events;
	private Color debugHighlight  = new Color(Display.getCurrent(), new RGB(198, 219, 174));
	private Color defaultColor = new Color(Display.getCurrent(), new RGB(255, 255, 255));

	@Override
	public void createPartControl(Composite parent) {
		GridLayout layout = new GridLayout(2, false);
		parent.setLayout(layout);
		this.events = EventDatabase.getInstance().getAllEvents();
		createViewer(parent);
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	private void createViewer(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER | SWT.READ_ONLY | SWT.PUSH);
		createColumns(parent, viewer);
		final Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setInput(this.events);
		getSite().setSelectionProvider(viewer);

		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		viewer.getControl().setLayoutData(gridData);

		Hashtable<String, Object> properties = new Hashtable<>();
		String[] topics = new String[] { DataModel.EA_TOPIC_DATA_SELECTION, DataModel.EA_TOPIC_DATA_UNIT_CHANGED, DataModel.EA_TOPIC_DATA_VIEW_REFRESH, VisuflowConstants.EA_TOPIC_DEBUGGING_ACTION_ALL};
		properties.put(EventConstants.EVENT_TOPIC, topics);
		ServiceUtil.registerService(EventHandler.class, this, properties);
	}

	private void createColumns(final Composite parent, final TableViewer viewer) {
		String[] titles = {"Unit", "In-Set", "Out-Set"};
		de.unipaderborn.visuflow.model.impl.Event currentEvent = EventDatabase.getInstance().getCurrentEvent();
		// Unit
		TableViewerColumn col = createTableViewerColumn(titles[0], 400, 0);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				de.unipaderborn.visuflow.model.impl.Event event = (de.unipaderborn.visuflow.model.impl.Event) element;
				return event.getUnit();
			}
			
			@Override
			public Color getBackground(Object element) {
				de.unipaderborn.visuflow.model.impl.Event event = (de.unipaderborn.visuflow.model.impl.Event) element;
				if(currentEvent != null && currentEvent.getId() == event.getId()) {
					return debugHighlight;
				} else {
					return defaultColor;
				}
			}
		});
		// In-Set
		col = createTableViewerColumn(titles[1], 400, 1);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				de.unipaderborn.visuflow.model.impl.Event event = (de.unipaderborn.visuflow.model.impl.Event) element;
				return event.getInSet();
			}
			
			@Override
			public Color getBackground(Object element) {
				de.unipaderborn.visuflow.model.impl.Event event = (de.unipaderborn.visuflow.model.impl.Event) element;
				if(currentEvent != null && currentEvent.getId() == event.getId()) {
					return debugHighlight;
				} else {
					return defaultColor;
				}
			}
		});
		// Out-Set
		col = createTableViewerColumn(titles[2], 400, 2);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				de.unipaderborn.visuflow.model.impl.Event event = (de.unipaderborn.visuflow.model.impl.Event) element;
				return event.getOutSet();
			}
			
			@Override
			public Color getBackground(Object element) {
				de.unipaderborn.visuflow.model.impl.Event event = (de.unipaderborn.visuflow.model.impl.Event) element;
				if(currentEvent != null && currentEvent.getId() == event.getId()) {
					return debugHighlight;
				} else {
					return defaultColor;
				}
			}
		});

		Menu menu = new Menu(parent);
		viewer.getControl().setMenu(menu);

		MenuItem menuItemStepHere = new MenuItem(menu, SWT.None);
		menuItemStepHere.setText("Step here");
		menuItemStepHere.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				int selection = viewer.getTable().getSelectionIndices()[0];
				String vfUnit = viewer.getTable().getItem(selection).getText(0);
				DataModel dataModel = ServiceUtil.getService(DataModel.class);
				boolean direction;
				if(selection < EventDatabase.getInstance().getBackwardsMarker()) {
					direction = false;
				} else {
					direction = true;
				}
				dataModel.stepToUnit(vfUnit, direction);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
	}

	private TableViewerColumn createTableViewerColumn(String title, int bound, final int colNumber) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		return viewerColumn;
	}

	@Override
	public void handleEvent(Event event) {
		if (viewer != null && !viewer.getControl().isDisposed()) {
			viewer.getTable().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					events = EventDatabase.getInstance().getAllEvents();
					viewer.setInput(events);
				}
			});
		}
	}
}
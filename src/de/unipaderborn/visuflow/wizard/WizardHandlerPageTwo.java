package de.unipaderborn.visuflow.wizard;

import java.util.HashMap;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class WizardHandlerPageTwo extends WizardPage {
//	private Text containerSourceText,containerTargetText,containerProjectName,containerPackageName;
	private Text classFirst, classSecond;
	private Combo flowSet,flowSetType1,flowSetType2;

	@SuppressWarnings("unused")
	private Text fileText;

	private ISelection selection;

	/**
	 * Constructor for SampleNewWizardPage.
	 * 
	 * @param pageName
	 */
	public WizardHandlerPageTwo(ISelection selection) {
		super("wizardPage");
		setTitle("Link Analysis and Target Project");
		setDescription("This wizard links the Target Java project with the Analysis project");
		this.selection = selection;
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 4;
		layout.verticalSpacing = 15;
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		Label flowSetLabel = new Label(container, SWT.NULL);
		flowSetLabel.setText("Input/Output Flow sets: ");
		flowSetLabel.setLayoutData(gd);
		
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		flowSet = new Combo(container, SWT.DROP_DOWN);
		flowSet.setItems(new String[]{"Select","Set", "Map", "HashMap"});
		flowSet.select(0);
		flowSet.setLayoutData(gd);		
		
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		flowSetType1 = new Combo(container, SWT.DROP_DOWN);
		flowSetType1.setItems(new String[]{"Select","Integer", "Long", "Float", "Double", "Boolean", "Char", "Custom"});
		flowSetType1.select(0);
		flowSetType1.setLayoutData(gd);
		flowSetType1.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				String selectedType = flowSetType1.getText();
				if(selectedType.equalsIgnoreCase("Custom"))
				{
					classFirst.setEnabled(true);
				}
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		flowSetType2 = new Combo(container, SWT.DROP_DOWN);
		flowSetType2.setItems(new String[]{"Select","Integer", "Long", "Float", "Double", "Boolean", "Char", "Custom"});
		flowSetType2.select(0);
		flowSetType2.setLayoutData(gd);
		flowSetType2.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				String selectedType = flowSetType2.getText();
				if(selectedType.equalsIgnoreCase("Custom"))
				{
					classSecond.setEnabled(true);
				}
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		new Label(container, SWT.NONE).setLayoutData(gd);
		
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		new Label(container, SWT.NONE).setLayoutData(gd);
		
		classFirst = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		classFirst.setLayoutData(gd);
		classFirst.setEnabled(false);
		
		classSecond = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		classSecond.setLayoutData(gd);
		classSecond.setEnabled(false);
		
		flowSet.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				String selectedType = flowSet.getText();
				if(selectedType.equalsIgnoreCase("Set"))
				{
					flowSetType2.setEnabled(false);
					classSecond.setEnabled(false);
				}
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		initialize();
		//dialogChanged();
		setControl(container);
	}

	/**
	 * Tests if the current workbench selection is a suitable container to use.
	 */

	private void initialize() {
//		if (selection != null && selection.isEmpty() == false
//				&& selection instanceof IStructuredSelection) {
//			IStructuredSelection ssel = (IStructuredSelection) selection;
//			if (ssel.size() > 1)
//				return;
//			Object obj = ssel.getFirstElement();
//			if (obj instanceof IResource) {
//				IContainer container;
//				if (obj instanceof IContainer)
//					container = (IContainer) obj;
//				else
//					container = ((IResource) obj).getParent();
//				containerSourceText.setText(container.getFullPath().toString());
//			}
//		}
		
	}

	public HashMap<String, String> getContainerName() {
		HashMap<String, String> containerMap = new HashMap<>();
		containerMap.put("FlowSet", flowSet.getText());
		containerMap.put("Type1", flowSetType1.getText());
		if(flowSetType2.isEnabled())
		{
		containerMap.put("Type2", flowSetType2.getText());
		}
		
		if(classFirst.isEnabled() && classFirst.getText()!=null)
		{
		containerMap.put("CustomType1", classFirst.getText());
		}
		if(classSecond.isEnabled() && classSecond.getText()!=null)
		{
		containerMap.put("CustomType2", classSecond.getText());
		}
		return containerMap;
	}
}

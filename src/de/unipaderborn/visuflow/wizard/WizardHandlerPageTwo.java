package de.unipaderborn.visuflow.wizard;

import java.util.HashMap;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class WizardHandlerPageTwo extends WizardPage {

	private Text classFirst, classSecond, containerSootLocation;
	private Combo flowSet,flowSetType1,flowSetType2;
	private Button[] analysisDirection = new Button[2];

	@SuppressWarnings("unused")
	private Text fileText;

	
	/**
	 * Constructor for SampleNewWizardPage.
	 * 
	 * @param pageName
	 */
	public WizardHandlerPageTwo(ISelection selection) {
		super("wizardPage");
		setTitle("Create New Analysis Project");
		setDescription("This wizard creates new analysis project based on user-inputs");
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
		Label analysisDirectionlabel = new Label(container, SWT.NULL);
		analysisDirectionlabel.setText("Choose analysis direction: ");
		analysisDirectionlabel.setLayoutData(gd);
		
		analysisDirection[0] = new Button(container, SWT.RADIO);
		analysisDirection[0].setSelection(true);
		analysisDirection[0].setText("Forward");
		//analysisType[0].setBounds(10, 5, 75, 30);
		
		analysisDirection[1] = new Button(container, SWT.RADIO);
		analysisDirection[1].setSelection(false);
		analysisDirection[1].setText("Backward");
		//analysisType[1].setBounds(10, 50, 75, 30);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		new Label(container, SWT.NONE).setLayoutData(gd);
		
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		Label flowSetLabel = new Label(container, SWT.NULL);
		flowSetLabel.setText("Input/Output Flow sets: ");
		flowSetLabel.setLayoutData(gd);
		
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		flowSet = new Combo(container, SWT.DROP_DOWN);
		flowSet.setItems(new String[]{"Select","Set", "Map", "HashMap", "ArrayList"});
		flowSet.select(0);
		flowSet.setLayoutData(gd);		
		
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		flowSetType1 = new Combo(container, SWT.DROP_DOWN);
		flowSetType1.setItems(new String[]{"Select","Integer", "Long", "Float", "Double", "Boolean", "Char", "String", "Custom"});
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
		flowSetType2.setItems(new String[]{"Select","Integer", "Long", "Float", "Double", "Boolean", "Char", "String", "Custom"});
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
		
		Label label = new Label(container, SWT.NULL);
		label.setText("Choose Soot jar : ");

		containerSootLocation = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		containerSootLocation.setLayoutData(gd);		

		Button button = new Button(container, SWT.PUSH);
		button.setText("Browse...");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleFileBrowse();
			}
		});	
		
		flowSet.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				String selectedType = flowSet.getText();
				if(selectedType.equalsIgnoreCase("Set"))
				{
					flowSetType2.setEnabled(false);
					classSecond.setEnabled(false);
				}
				
				if(selectedType.equalsIgnoreCase("HashMap"))
				{
					flowSetType2.setEnabled(true);
				}
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		setControl(container);
	}

	/**
	 * Tests if the current workbench selection is a suitable container to use.
	 */

	private void handleFileBrowse() {
		FileDialog fileDialog = new FileDialog(getShell(),SWT.OPEN);
		fileDialog.setFilterExtensions(new String[]{"*.jar"});
		 containerSootLocation.setText(fileDialog.open());
	}

	public HashMap<String, String> getContainerName() {
		HashMap<String, String> containerMap = new HashMap<>();
		if(analysisDirection[0].getSelection())
		{
			containerMap.put("AnalysisDirection", analysisDirection[0].getText());
		}
		if(analysisDirection[1].getSelection())
		{
			containerMap.put("AnalysisDirection", analysisDirection[1].getText());
		}
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
		containerMap.put("sootLocation", containerSootLocation.getText());
		return containerMap;
	}
}

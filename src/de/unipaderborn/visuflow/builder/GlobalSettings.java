package de.unipaderborn.visuflow.builder;

import java.io.IOException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

public final  class GlobalSettings {
	
	private static DialogSettings settings = new DialogSettings("ProjectOutputFolder");
	IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject();
    IPath path =project.getProject().getLocation();
    String filename = path.append("settings.xml").toOSString();
	   protected GlobalSettings() {
		// TODO Auto-generated constructor stub
	}
	
	   public static void put(String key,String value){
		    IPath path =getProject().getLocation();
		    String filename = path.append("settings.xml").toOSString();
		   try {
				settings.load(filename);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			   settings.put(key,value);  
			   try {
				settings.save(filename);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	   }
	
	public static String get(String key){
	    IPath path =getProject().getLocation();
	    String filename = path.append("settings.xml").toOSString();
	   try {
			settings.load(filename);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	   return settings.get(key);
	}
	private static IProject getProject(){
		
		IWorkbenchPart workbenchPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart(); 
		IFile file = (IFile) workbenchPart.getSite().getPage().getActiveEditor().getEditorInput().getAdapter(IFile.class);
		return file.getProject();
	}
       
}

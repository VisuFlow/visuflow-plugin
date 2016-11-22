package de.unipaderborn.visuflow.builder;

import java.io.IOException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.DialogSettings;

public final  class GlobalSettings {
	final static String fileName = "settings.xml";
	
	private static DialogSettings settings = new DialogSettings("ProjectOutputFolder");
	IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject();
    IPath path =project.getProject().getLocation();
    String filename = path.append(fileName).toOSString();
	   protected GlobalSettings() {
		// TODO Auto-generated constructor stub
	}
	
	   public static void put(IResource resource,String key,String value){
		    IPath path =resource.getProject().getLocation();
		    String filename = path.append(fileName).toOSString();
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
	
	public static String get(IResource resource,String key){
	    IPath path =resource.getProject().getLocation();
	    String filename = path.append(fileName).toOSString();
	   try {
			settings.load(filename);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	   return settings.get(key);
	}
	public static String get(IJavaProject resource,String key){
	    IPath path = resource.getProject().getLocation();
	    String filename = path.append(fileName).toOSString();
	   try {
			settings.load(filename);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	   return settings.get(key);
	}
       
}

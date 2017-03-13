package de.unipaderborn.visuflow.builder;

import java.io.IOException;

import org.eclipse.core.internal.runtime.Activator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.prefs.BackingStoreException;

import de.unipaderborn.visuflow.Visuflow;

public final class GlobalSettings {
	final static String fileName = "settings.xml";

	private static DialogSettings settings = new DialogSettings("ProjectOutputFolder");
	IPath path = Visuflow.getDefault().getStateLocation();
	String filename = path.append(fileName).toOSString();

	protected GlobalSettings() {
		// TODO Auto-generated constructor stub
		System.out.println("Settings filename is" + filename);
	}

	public static void put(String key, String value) {
		IPath path = Visuflow.getDefault().getStateLocation();
		String filename = path.append(fileName).toOSString();
		try {
			settings.load(filename);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		settings.put(key, value);
		try {
			settings.save(filename);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public static String get(String key) {
		IPath path = Visuflow.getDefault().getStateLocation();
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

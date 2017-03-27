package de.unipaderborn.visuflow.builder;

import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.DialogSettings;
import de.unipaderborn.visuflow.Visuflow;

/**
 * @author kaarthuk
 *
 */
public final class GlobalSettings {
	
	/**
	 * Filename of the settings xml that stores settings.
	 */
	final static String fileName = "settings.xml";
	/**
	 * Creates a dialogsettings object.
	 */
	private static DialogSettings settings = new DialogSettings("ProjectOutputFolder");
	
	/**
	 * Gets the execution path of the plugin.
	 */
	IPath path = Visuflow.getDefault().getStateLocation();
	
	/**
	 * The filename of the settings file appended to the path.
	 */
	String filename = path.append(fileName).toOSString();

	protected GlobalSettings() {
		System.out.println("Settings filename is" + filename);
	}

	/**
	 * This function writes a key/value pair to the settings file
	 * @param key The key name.
	 * @param value The value for the key.
	 */
	public static void put(String key, String value) {
		IPath path = Visuflow.getDefault().getStateLocation();
		String filename = path.append(fileName).toOSString();
		try {
			settings.load(filename);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		settings.put(key, value);
		try {
			settings.save(filename);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Get the value of the key being passed.
	 * @param key The key whose value is required.
	 * @return The value as a string.
	 */
	public static String get(String key) {
		IPath path = Visuflow.getDefault().getStateLocation();
		String filename = path.append(fileName).toOSString();
		try {
			settings.load(filename);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return settings.get(key);
	}

}

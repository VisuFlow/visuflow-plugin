package de.unipaderborn.visuflow;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class Visuflow extends AbstractUIPlugin {
	public static Visuflow plugin;
	public static BundleContext context;

	public Visuflow() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void start(BundleContext context) {

		Visuflow.context = context;
		System.out.println("---InStart---");
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) {

		Visuflow.context = null;
	}

	public static Visuflow getDefault() {

		return plugin;
	}
}

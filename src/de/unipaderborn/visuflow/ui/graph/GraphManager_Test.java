package de.unipaderborn.visuflow.ui.graph;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class GraphManager_Test {
	
	GraphManager manager = null;
	
	GraphManager_Test()
	{
		GraphManager manager = new GraphManager("test", "url('file:styles/stylesheet.css')");
	}
	
	@Test
	public void evaluatesExpression() {
		assertUiApplet();
	}

	private void assertUiApplet()
	{
		assertNotNull(manager.getApplet());
	}
	
//	private void assertZoomIn()
//	{
//		
//	}
}

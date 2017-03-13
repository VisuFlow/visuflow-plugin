package de.unipaderborn.visuflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.internal.runtime.Activator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.osgi.service.prefs.BackingStoreException;

import de.unipaderborn.visuflow.builder.GlobalSettings;

public class ProjectPreferences {

	public void createPreferences() {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(GlobalSettings.get("AnalysisProject"));
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences pref = projectScope.getNode(Activator.PLUGIN_ID);
		for (String stmt : getStatementTypes()) {
			pref.putInt(stmt, 38536);
		}
		try {
			pref.flush();
		} catch (BackingStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void updateColorPreferences(String stmtType, int color) {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(GlobalSettings.get("AnalysisProject"));
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences pref = projectScope.getNode(Activator.PLUGIN_ID);
		pref.putInt(stmtType, color);
		try {
			pref.flush();
		} catch (BackingStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Map<String, Integer> getAllNodeColors() {
		HashMap<String, Integer> res = new HashMap<>();
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(GlobalSettings.get("AnalysisProject"));
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences pref = projectScope.getNode(Activator.PLUGIN_ID);
		for (String stmt : getStatementTypes()) {
			res.put(stmt, pref.getInt(stmt, 0));
		}
		return res;
	}
	
	public Integer getColorForNode(String node) {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(GlobalSettings.get("AnalysisProject"));
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences pref = projectScope.getNode(Activator.PLUGIN_ID);
		return pref.getInt(node, 0);
	}
	private List<String> getStatementTypes() {
		List<String> stmtTypes = new ArrayList<String>();
		stmtTypes.add("soot.jimple.internal.JNopStmt");
		stmtTypes.add("soot.jimple.internal.JIdentityStmt");
		stmtTypes.add("soot.jimple.internal.JAssignStmt");
		stmtTypes.add("soot.jimple.internal.JIfStmt");
		stmtTypes.add("soot.jimple.internal.JGotoStmt");
		stmtTypes.add("soot.jimple.internal.JTableSwitchStmt");
		stmtTypes.add("soot.jimple.internal.JLookupSwitchStmt");
		stmtTypes.add("soot.jimple.internal.JInvokeStmt");
		stmtTypes.add("soot.jimple.internal.JReturnStmt");
		stmtTypes.add("soot.jimple.internal.JReturnVoidStmt");
		stmtTypes.add("soot.jimple.internal.JThrowStmt");
		stmtTypes.add("soot.jimple.internal.JRetStmt");
		stmtTypes.add("soot.jimple.internal.JEnterMonitorStmt");
		stmtTypes.add("soot.jimple.internal.JExitMonitorStmt");
		return stmtTypes;
	}

}

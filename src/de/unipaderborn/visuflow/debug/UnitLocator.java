package de.unipaderborn.visuflow.debug;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import de.unipaderborn.visuflow.VisuflowConstants;
import de.unipaderborn.visuflow.builder.GlobalSettings;
import de.unipaderborn.visuflow.model.VFClass;
import de.unipaderborn.visuflow.model.VFUnit;

public class UnitLocator implements VisuflowConstants {

	public static UnitLocation locateUnit(VFUnit unit) throws CoreException, IOException {
		//List<IProject> analysisProjects = getAnalysisProjects();
		VFClass vfClass = unit.getVfMethod().getVfClass();
		String className = vfClass.getSootClass().getName();

		String projectName = GlobalSettings.get("AnalysisProject");
		IPath path = new Path("/sootOutput/" + className + ".jimple");
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		File file = project.getFile(path).getLocation().toFile();

		UnitLocation location = new UnitLocation();
		int lineNumber = 1;
		int accumulatedCharacters = 0;
		List<String> lines = Files.readAllLines(file.toPath());
		for (String line : lines) {
			if(!line.trim().isEmpty()) {
				String preparedLine = line.replace(";", "").trim();
				if(!preparedLine.isEmpty() && unit.getUnit().toString().equals(preparedLine)) {
					int charStart = line.indexOf(preparedLine);
					location.jimpleFile = path;
					location.line = lineNumber;
					location.charStart = accumulatedCharacters + charStart;
					location.charEnd = location.charStart + preparedLine.length();
					location.project = project;
					location.vfUnit = unit;
					return location;
				}
			}
			lineNumber++;
			accumulatedCharacters += line.length() + 1; // length + linebreak; TODO check, if the linebreak is different on different platforms
		}

		throw new NoSuchElementException("Unit not found: " + unit.getFullyQualifiedName());
	}
}

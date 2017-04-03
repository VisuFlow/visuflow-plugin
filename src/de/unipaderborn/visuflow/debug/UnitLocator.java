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
		VFClass vfClass = unit.getVfMethod().getVfClass();
		String className = vfClass.getSootClass().getName();
		String projectName = GlobalSettings.get("AnalysisProject");
		IPath path = new Path("/sootOutput/" + className + ".jimple");
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		File file = project.getFile(path).getLocation().toFile();
		List<String> lines = Files.readAllLines(file.toPath());

		// determine method offset
		int[] methodPosition = find(lines, unit.getVfMethod().getSootMethod().getDeclaration(), 0);

		String toFind = unit.getUnit().toString() + ";";
		int[] position = find(lines, toFind, methodPosition[1]);

		UnitLocation location = new UnitLocation();
		location.jimpleFile = path;
		location.charStart = position[0];
		location.charEnd = position[1];
		location.line = position[2];
		location.project = project;
		location.vfUnit = unit;
		return location;
	}

	static int[] find(List<String> hayStack, String needle, int offset) {
		int[] charStartAndEnd = new int[3];
		int lineNumber = 1;
		int accumulatedCharacters = 0;
		for (String line : hayStack) {
			if(!line.trim().isEmpty()) {
				String preparedLine = line.trim();
				if(!preparedLine.isEmpty() && needle.equals(preparedLine) && accumulatedCharacters >= offset) {
					int startInLine = line.indexOf(preparedLine);
					int charStart = accumulatedCharacters + startInLine;
					int charEnd = charStart + preparedLine.length();
					charStartAndEnd[0] = charStart;
					charStartAndEnd[1] = charEnd;
					charStartAndEnd[2] = lineNumber;
					return charStartAndEnd;
				}
			}
			lineNumber++;
			accumulatedCharacters += line.length() + 1; // length + linebreak; TODO check, if the linebreak is different on different platforms
		}

		throw new NoSuchElementException("String " + needle + " not found");
	}
}

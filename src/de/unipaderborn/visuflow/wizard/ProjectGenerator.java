package de.unipaderborn.visuflow.wizard;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;

import com.sun.codemodel.JClassAlreadyExistsException;

public class ProjectGenerator {
	
	public IJavaProject createProject(String projectName, WizardInput wizardInput) throws CoreException, IOException
	{
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(projectName);
		project.create(null);
		project.open(null);		
		IProjectDescription description = project.getDescription();
		description.setNatureIds(new String[] { JavaCore.NATURE_ID });
		project.setDescription(description, null);
		IJavaProject javaProject = JavaCore.create(project); 
		IFolder binFolder = project.getFolder("bin");
		binFolder.create(false, true, null);
		javaProject.setOutputLocation(binFolder.getFullPath(), null);
		List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
		IVMInstall vmInstall = JavaRuntime.getDefaultVMInstall();
		LibraryLocation[] locations = JavaRuntime.getLibraryLocations(vmInstall);
		for (LibraryLocation element : locations) {
		 entries.add(JavaCore.newLibraryEntry(element.getSystemLibraryPath(), null, null));
		}
		InputStream is = new BufferedInputStream(new FileInputStream("C:\\Users\\Habeeb\\Documents\\soot-trunk.jar"));
	    IFile jarFile = project.getFile("soot-trunk.jar");
	    jarFile.create(is, false, null);
	    IPath path = jarFile.getFullPath();
	    entries.add(JavaCore.newLibraryEntry(path, null, null));
		//add libs to project class path
		javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), null);
		
		IFolder sourceFolder = project.getFolder("src");
		sourceFolder.create(false, true, null);
		IPackageFragmentRoot root1 = javaProject.getPackageFragmentRoot(sourceFolder);
		IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
		IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1];
		System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
		newEntries[oldEntries.length] = JavaCore.newSourceEntry(root1.getPath());
		javaProject.setRawClasspath(newEntries, null);
		//IPackageFragment pack = javaProject.getPackageFragmentRoot(sourceFolder).createPackageFragment("de.com.visuflow", false, null);
//		StringBuffer buffer = new StringBuffer();
//		buffer.append("package " + pack.getElementName() + ";\n");
//		buffer.append("\n");
//		buffer.append("System.out.println(\"Sample Test\");");
//		ICompilationUnit cu = pack.createCompilationUnit("Sample", buffer.toString(), false, null);
//		cu.open(null);
		String filepath = sourceFolder.getLocation().toOSString();
		File file = new File(filepath);
		try {
			CodeGenerator.generateSource(file);
		} catch (JClassAlreadyExistsException e) {
			e.printStackTrace();
		}
		sourceFolder.refreshLocal(1, null);
		javaProject.open(null);
		System.out.println(wizardInput.flowType);
		System.out.println(wizardInput.flowType1);
		System.out.println(wizardInput.flowtype2);
		System.out.println(wizardInput.classNameFirst);
		System.out.println(wizardInput.classNameSecond);
		return javaProject;
	}

}

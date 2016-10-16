package de.unipaderborn.visuflow.builder;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import soot.G;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

public class JimpleBuilder extends IncrementalProjectBuilder {

	private String classpath;

	protected String getClassFilesLocation(IJavaProject javaProject) throws JavaModelException {
		String path = javaProject.getOutputLocation().toString();
		IResource binFolder = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
		if (binFolder != null)
			return binFolder.getLocation().toString();
		throw new RuntimeException("Could not retrieve Soot classpath for project " + javaProject.getElementName());
	}

	private String getSootCP(IJavaProject javaProject) {
		String sootCP = "";
		try {
			// sootCP = getClassFilesLocation(javaProject);
			for (String resource : getJarFilesLocation(javaProject))
				sootCP = sootCP + File.pathSeparator + resource;
		} catch (JavaModelException e) {
		}
		sootCP = sootCP + File.pathSeparator;
		return sootCP;
	}

	protected Set<String> getJarFilesLocation(IJavaProject javaProject) throws JavaModelException {
		Set<String> jars = new HashSet<String>();
		IClasspathEntry[] resolvedClasspath = javaProject.getResolvedClasspath(true);
		for (IClasspathEntry classpathEntry : resolvedClasspath) {
			String path = classpathEntry.getPath().toOSString();
			if (path.endsWith(".jar")) {
				IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(classpathEntry.getPath());
				if (file != null && file.getRawLocation() != null)
					path = file.getRawLocation().toOSString();
				jars.add(path);
			}
		}
		return jars;
	}
	
	private String getOutputLocation(IJavaProject project) {
		String outputLocation = "";
		IPath path;
		try {
			path = project.getOutputLocation();
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IFolder folder = root.getFolder(path);
			outputLocation = folder.getLocation().toOSString();
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return outputLocation;
	}

	public static final String BUILDER_ID = "JimpleBuilder.JimpleBuilder";

	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		System.out.println("Build Start");
		IJavaProject project = JavaCore.create(getProject());
		classpath = getSootCP(project);
		String location = getOutputLocation(project);
		System.out.println(location);
		classpath = location + File.pathSeparator + classpath;
		G.v().reset();
		soot.Main.main(new String[] { "-cp", ".;" + classpath, "-allow-phantom-refs", "-src-prec", "class",
				"-keep-line-number", "-f","J" ,"-d", location + File.separator + "Jimple","-process-dir",location});
		//call update
		return null;
	}

}

package de.unipaderborn.visuflow.builder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import de.unipaderborn.visuflow.model.DataModel;
import de.unipaderborn.visuflow.model.VFClass;
import de.unipaderborn.visuflow.model.graph.ICFGStructure;
import de.unipaderborn.visuflow.model.graph.JimpleModelAnalysis;
import de.unipaderborn.visuflow.util.ServiceUtil;

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
            for (String resource : getJarFilesLocation(javaProject))
                sootCP = sootCP + File.pathSeparator + resource;
        } catch (JavaModelException e) {
        }
        sootCP = sootCP + File.pathSeparator;
        return sootCP;
    }

    protected Set<String> getJarFilesLocation(IJavaProject javaProject) throws JavaModelException {
        Set<String> jars = new HashSet<>();
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

	@SuppressWarnings("unused")
	private String getOutputLocation(IJavaProject project) {
		String outputLocation = "";
		IPath path;
		try {
			path = project.getOutputLocation();
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IFolder folder = root.getFolder(path);
			outputLocation = folder.getLocation().toOSString();
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return outputLocation;
	}

    private void fillDataModel(ICFGStructure icfg, List<VFClass> jimpleClasses){
        DataModel data = ServiceUtil.getService(DataModel.class);
        data.setIcfg(icfg);
        data.setClassList(jimpleClasses);
        //		data.setSelectedClass(jimpleClasses.get(0));
    }

    public static final String BUILDER_ID = "JimpleBuilder.JimpleBuilder";


    @Override
    protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
        System.out.println("Build Start");
        IJavaProject project = JavaCore.create(getProject());
        classpath = getSootCP(project);
        String location = GlobalSettings.get(project, "TargetFolder");
        IFolder folder = project.getProject().getFolder("output");
        //at this point, no resources have been created
        if (!folder.exists()) {
            folder.create(IResource.NONE, true, null);
        }
        location = "/home/henni/devel/pg/workspace-plugin/visuflow-workspace/VisuFlowSheet1/targetBin2";
        //location = "/home/henni/devel/pg/workspace-plugin/visuflow-plugin-workspace/dfa17/targetsBin";
        System.out.println(location);
        classpath = location + File.pathSeparator + classpath;
        String[] sootString = new String[] { "-cp", classpath, "-exclude", "javax", "-allow-phantom-refs", "-no-bodies-for-excluded", 
    			"-process-dir", location, "-src-prec", "only-class", "-w", "-output-format", 
    			"J", "-keep-line-number" ,"-output-dir",folder.getLocation().toOSString()/*,"tag.ln","on"*/ };
        ICFGStructure icfg = new ICFGStructure();
        JimpleModelAnalysis analysis = new JimpleModelAnalysis();
        analysis.setSootString(sootString);
        List<VFClass> jimpleClasses = new ArrayList<>();
        analysis.createICFG(icfg, jimpleClasses);
        fillDataModel(icfg, jimpleClasses);
        return null;
    }
}

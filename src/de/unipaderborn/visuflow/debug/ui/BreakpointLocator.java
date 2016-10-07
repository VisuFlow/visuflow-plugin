package de.unipaderborn.visuflow.debug.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.debug.core.JDIDebugModel;

public class BreakpointLocator {
	
	private List<String> flowFunctionNames = Arrays.asList(new String[] { "getNormalFlowFunction",
			"getCallFlowFunction", "getReturnFlowFunction", "getCallToReturnFlowFunction", "union" });

	public void findFlowFunctions() throws JavaModelException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot workspaceRoot = workspace.getRoot();
		IJavaModel model = JavaCore.create(workspaceRoot);
		
		List<IJavaElement> javaProjects = getJavaProjects(model);
		List<IJavaElement> sourceFolders = getSourceFolders(javaProjects);
		
		List<IJavaElement> createFlowFunctionsFactory = new ArrayList<IJavaElement>();
		for (IJavaElement sourceFolder : sourceFolders) {
			findRecursive(createFlowFunctionsFactory, sourceFolder, "createFlowFunctionsFactory");
		}
		
		List<IJavaElement> flowFunctions = new ArrayList<IJavaElement>();
		for (IJavaElement factory : createFlowFunctionsFactory) {
			for (String functionName : flowFunctionNames) {
				findRecursive(flowFunctions, factory, functionName);
			}
		}
		
		for (IJavaElement flowFunction : flowFunctions) {
			IMethod method = (IMethod) flowFunction;
			ICompilationUnit cu = method.getCompilationUnit();
			String sourceCode = cu.getSource();
			
			ASTParser parser = ASTParser.newParser(AST.JLS8);

		    // Parse the class as a compilation unit.
		    parser.setKind(ASTParser.K_COMPILATION_UNIT);
		    parser.setSource(sourceCode.toCharArray());
		    parser.setResolveBindings(true);

		    // Return the compiled class as a compilation unit
		    CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);
		    compilationUnit.accept(new ASTVisitor() {
		        public boolean visit(MethodDeclaration node) {       
		            int lineNumber = compilationUnit.getLineNumber(node.getStartPosition());
		            for (String flowFunctionName : flowFunctionNames) {
		            	if(node.getName().toString().equals(flowFunctionName)) {
		            		System.out.println(lineNumber + " [" + node.getName() + "]");
		            		IResource res = cu.getResource();
		            		String className = method.getDeclaringType().getElementName();
		            		try {
		            			System.out.println(res.getName());
		            			System.out.println(className);
								JDIDebugModel.createLineBreakpoint(res, className, lineNumber, -1, -1, 0, true, null);
							} catch (CoreException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
		            	}
					}
		            
		            return true;
		        }
		    });
		}
	}
	
	private List<IJavaElement> getSourceFolders(List<IJavaElement> javaProjects) throws JavaModelException {
		List<IJavaElement> folders = new ArrayList<IJavaElement>();
		for (IJavaElement project : javaProjects) {
			 IJavaElement[] children = ((IParent)project).getChildren();
			 for (IJavaElement child : children) {
				if(child instanceof IPackageFragmentRoot) {
					IPackageFragmentRoot packageFragmentRoot = (IPackageFragmentRoot) child;
					if(packageFragmentRoot.getKind() == IPackageFragmentRoot.K_SOURCE) {
						folders.add(packageFragmentRoot);
					}
				}
			}
		}
		return folders;
	}

	private List<IJavaElement> getJavaProjects(IJavaModel model) throws JavaModelException {
		List<IJavaElement> projects = new ArrayList<IJavaElement>();
		for (IJavaElement project : model.getChildren()) {
			if(project instanceof IJavaProject) {
				projects.add(project);
			}
		}
		return projects;
	}

	void findRecursive(List<IJavaElement> result, IJavaElement parent, String name) throws JavaModelException {
		if(parent.getElementName().equals(name)) {
			result.add(parent);
		}
		
		if(parent instanceof IParent) {
			IJavaElement[] children = ((IParent) parent).getChildren();
			for (IJavaElement child : children) {
				findRecursive(result, child, name);
			}
		}
	}
}

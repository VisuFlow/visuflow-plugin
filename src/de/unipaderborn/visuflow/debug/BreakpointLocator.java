package de.unipaderborn.visuflow.debug;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class BreakpointLocator {

    // @formatter:off
    private static List<String> flowFunctionNames = Arrays.asList(new String[] {
            "getNormalFlowFunction",
            "getCallFlowFunction",
            "getReturnFlowFunction",
            "getCallToReturnFlowFunction",
            "union",
            "flowThrough"
    });

    private static Map<String, int[]> methodParameterIndices = new HashMap<>();
    static {
        methodParameterIndices.put("getNormalFlowFunction", 	  new int[] {0, 1});
        methodParameterIndices.put("getCallFlowFunction", 		  new int[] {0, 1});
        methodParameterIndices.put("getReturnFlowFunction", 	  new int[] {0, 1});
        methodParameterIndices.put("getCallToReturnFlowFunction", new int[] {0, 1});
        methodParameterIndices.put("union", 					  new int[] {0, 1});

    }
    // @formatter:on

    private CoreException exception = null;
    public List<BreakpointLocation> findFlowFunctions() throws JavaModelException {
        List<BreakpointLocation> locations = new ArrayList<>();

        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceRoot workspaceRoot = workspace.getRoot();
        IJavaModel model = JavaCore.create(workspaceRoot);

        List<IJavaElement> javaProjects = getJavaProjects(model);
        List<IJavaElement> sourceFolders = getSourceFolders(javaProjects);

        List<IJavaElement> flowFunctions = new ArrayList<>();
        for (IJavaElement packageFragment : sourceFolders) { // TODO make sure to only use the target code
            for (String functionName : flowFunctionNames) {
                findRecursive(flowFunctions, packageFragment, functionName);
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
                @Override
                public boolean visit(MethodDeclaration node) {
                    int lineNumber = compilationUnit.getLineNumber(node.getName().getStartPosition());
                    if (node.getName().toString().equals(method.getElementName())) {
                        IResource res = cu.getResource();
                        try {
                            BreakpointLocation location = new BreakpointLocation();
                            location.method = method;
                            location.resource = res;
                            location.className = method.getDeclaringType().getFullyQualifiedName();
                            location.methodName = method.getElementName();
                            location.methodSignature = resolveMethodSignature(method);
                            location.lineNumber = lineNumber;
                            location.offset = node.getName().getStartPosition();
                            location.length = node.getName().getLength();
                            locations.add(location);
                        } catch (CoreException e) {
                            exception = e;
                            return false;
                        }
                    }

                    return true;
                }
            });
            
            if(exception != null) {
            	throw new JavaModelException(exception);
            }
        }

        return locations;
    }

    private List<IJavaElement> getSourceFolders(List<IJavaElement> javaProjects) throws JavaModelException {
        List<IJavaElement> folders = new ArrayList<>();
        for (IJavaElement project : javaProjects) {
            IJavaElement[] children = ((IParent) project).getChildren();
            for (IJavaElement child : children) {
                if (child instanceof IPackageFragmentRoot) {
                    IPackageFragmentRoot packageFragmentRoot = (IPackageFragmentRoot) child;
                    if (packageFragmentRoot.getKind() == IPackageFragmentRoot.K_SOURCE) {
                        folders.add(packageFragmentRoot);
                    }
                }
            }
        }
        return folders;
    }

    private List<IJavaElement> getJavaProjects(IJavaModel model) throws JavaModelException {
        List<IJavaElement> projects = new ArrayList<>();
        for (IJavaElement project : model.getChildren()) {
            if (project instanceof IJavaProject) {
                projects.add(project);
            }
        }
        return projects;
    }

    void findRecursive(List<IJavaElement> result, IJavaElement parent, String name) throws JavaModelException {
        if (parent.getElementName().equals(name)) {
            result.add(parent);
        }

        if (parent instanceof IParent) {
            IJavaElement[] children = ((IParent) parent).getChildren();
            for (IJavaElement child : children) {
                findRecursive(result, child, name);
            }
        }
    }

    /**
     * Copied from org.eclipse.jdt.internal.debug.ui.actions.ToggleBreakpointAdapter
     * TODO: is there a public API to do this?
     *
     * Returns the resolved signature of the given method
     * @param method method to resolve
     * @return the resolved method signature or <code>null</code> if none
     * @throws JavaModelException
     * @since 3.4
     */
    public static String resolveMethodSignature(IMethod method) throws JavaModelException {
        String signature = method.getSignature();
        String[] parameterTypes = Signature.getParameterTypes(signature);
        int length = parameterTypes.length;
        String[] resolvedParameterTypes = new String[length];
        for (int i = 0; i < length; i++) {
            resolvedParameterTypes[i] = resolveTypeSignature(method, parameterTypes[i]);
            if (resolvedParameterTypes[i] == null) {
                return null;
            }
        }
        String resolvedReturnType = resolveTypeSignature(method, Signature.getReturnType(signature));
        if (resolvedReturnType == null) {
            return null;
        }
        return Signature.createMethodSignature(resolvedParameterTypes, resolvedReturnType);
    }

    /**
     * Returns the resolved type signature for the given signature in the given
     * method, or <code>null</code> if unable to resolve.
     *
     * @param method method containing the type signature
     * @param typeSignature the type signature to resolve
     * @return the resolved type signature
     * @throws JavaModelException
     */
    private static String resolveTypeSignature(IMethod method, String typeSignature) throws JavaModelException {
        int count = Signature.getArrayCount(typeSignature);
        String elementTypeSignature = Signature.getElementType(typeSignature);
        if (elementTypeSignature.length() == 1) {
            // no need to resolve primitive types
            return typeSignature;
        }
        String elementTypeName = Signature.toString(elementTypeSignature);
        IType type = method.getDeclaringType();
        String[][] resolvedElementTypeNames = type.resolveType(elementTypeName);
        if (resolvedElementTypeNames == null || resolvedElementTypeNames.length != 1) {
            // check if type parameter
            ITypeParameter typeParameter = method.getTypeParameter(elementTypeName);
            if (!typeParameter.exists()) {
                typeParameter = type.getTypeParameter(elementTypeName);
            }
            if (typeParameter.exists()) {
                String[] bounds = typeParameter.getBounds();
                if (bounds.length == 0) {
                    return "Ljava/lang/Object;"; //$NON-NLS-1$
                }
                String bound = Signature.createTypeSignature(bounds[0], false);
                return Signature.createArraySignature(resolveTypeSignature(method, bound), count);
            }
            // the type name cannot be resolved
            return null;
        }

        String[] types = resolvedElementTypeNames[0];
        types[1] = types[1].replace('.', '$');

        String resolvedElementTypeName = Signature.toQualifiedName(types);
        String resolvedElementTypeSignature = "";
        if(types[0].equals("")) {
            resolvedElementTypeName = resolvedElementTypeName.substring(1);
            resolvedElementTypeSignature = Signature.createTypeSignature(resolvedElementTypeName, true);
        }
        else {
            resolvedElementTypeSignature = Signature.createTypeSignature(resolvedElementTypeName, true).replace('.', '/');
        }

        return Signature.createArraySignature(resolvedElementTypeSignature, count);
    }

    public static class BreakpointLocation {
        public IMethod method;
        public IResource resource;
        public String className;
        public String methodName;
        public String methodSignature;
        public int lineNumber;
        public int offset;
        public int length;
        
        @Override
        public String toString() {
        	return className + " . " + methodName + " (" + lineNumber + ")";
        }
    }
}

package de.unipaderborn.visuflow.model.graph;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.unipaderborn.visuflow.model.VFClass;
import de.unipaderborn.visuflow.model.VFMethod;
import de.unipaderborn.visuflow.model.VFMethodEdge;
import de.unipaderborn.visuflow.model.VFUnit;
import soot.Body;
import soot.G;
import soot.Main;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Targets;
import soot.util.Chain;

public class JimpleModelAnalysis {

	private int methodcount = 0;
	private int edgeCount = 0;

	private String[] sootString;

	public void setSootString(String[] s){
		this.sootString = s;
	}

	public void createICFG(final ICFGStructure methodGraph, List<VFClass> vfClasses) {
		G.reset();
		Transform transform = new Transform("wjtp.myTransform", new SceneTransformer() {
			@Override
			protected void internalTransform(String phase, Map<String, String> arg1) {
				createJimpleHierarchyWithCfgs(vfClasses);
				createICFG();
			}

			private void createICFG() {
				CallGraph cg = Scene.v().getCallGraph();
				SootMethod entryMethod = null;
				java.util.List<SootMethod> listMethod = Scene.v().getEntryPoints();
				Iterator<SootMethod> iterEntryMethod = listMethod.iterator();
				while(iterEntryMethod.hasNext())
				{
					entryMethod = iterEntryMethod.next();
					if(entryMethod.isMain())
					{
						methodcount++;
						VFMethod vfmethod = new VFMethod(entryMethod);
						methodGraph.listMethods.add(vfmethod);
						break;
					}
				}

				traverseMethods(entryMethod, cg);
			}

			private void createJimpleHierarchyWithCfgs(List<VFClass> vfClasses) {
				Chain<SootClass> classes = Scene.v().getClasses();
				for (SootClass sootClass : classes) {
					if(sootClass.isJavaLibraryClass()) {
						continue;
					}

					VFClass currentClass = new VFClass(sootClass);
					vfClasses.add(currentClass);

					for (SootMethod sootMethod : sootClass.getMethods()) {
						VFMethod currentMethod = new VFMethod(sootMethod);
						Body body = sootMethod.retrieveActiveBody();
						currentMethod.setBody(body);
						currentMethod.setControlFlowGraph(new ControlFlowGraphGenerator().generateControlFlowGraph(body));
						currentClass.getMethods().add(currentMethod);

						for (Unit unit : body.getUnits()) {
							VFUnit currentUnit = new VFUnit(unit);
							currentMethod.getUnits().add(currentUnit);
						}
					}
				}
			}

			private void traverseMethods(SootMethod source, CallGraph cg)
			{			
				Targets tc = new Targets(cg.edgesOutOf(source));
				while(tc.hasNext())
				{
					SootMethod destination = (SootMethod)tc.next();	
					//					System.out.println(destination+" is java library "+destination.isJavaLibraryMethod());
					if(!destination.isJavaLibraryMethod())
					{
						boolean methodPresent = false;
						Iterator<VFMethod> iteratorMethod = methodGraph.listMethods.iterator();
						while(iteratorMethod.hasNext())
						{
							if(iteratorMethod.next().getSootMethod().equals(destination))
							{
								methodPresent = true;
								break;
							}
						}

						if(!methodPresent)
						{
							methodcount++;
							VFMethod method = new VFMethod(methodcount, destination);
							methodGraph.listMethods.add(method);
						}
						VFMethod sourceMethod = null;
						VFMethod destinationMethod = null;
						Iterator<VFMethod> iteratorMethods = methodGraph.listMethods.iterator();
						while(iteratorMethods.hasNext())
						{
							VFMethod method = iteratorMethods.next();
							if(method.getSootMethod().equals(source))
							{
								sourceMethod = method;
							}
							if(method.getSootMethod().equals(destination))
							{
								destinationMethod = method;
							}
						}
						edgeCount++;
						VFMethodEdge edge = new VFMethodEdge(edgeCount, sourceMethod, destinationMethod);
						methodGraph.listEdges.add(edge);
						traverseMethods(destination, cg);
					}
				}
			}
		});

		PackManager.v().getPack("wjtp").add(transform);
		// Run Soot
		Main.main(sootString);
	}
}


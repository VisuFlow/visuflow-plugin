package de.visuflow.callgraph;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import de.visuflow.callgraph.ICFGStructure;
import de.visuflow.callgraph.Method;
import de.visuflow.callgraph.Edge;
import soot.G;
import soot.Main;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootMethod;
import soot.Transform;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Targets;
import soot.options.Options;

public class InterProceduralGraph {
	
	public static int methodcount = 0;
	public static int edgeCount = 0;
	public static int methodNumber = 0;
	public static List<Method> listMethods = new ArrayList<>();
	public static List<Edge> listEdges = new ArrayList<>();
	public static List<SootMethod> list = new ArrayList<>();
	public static HashMap<Integer, SootMethod> methodMap = new HashMap<Integer, SootMethod>();
	
	public static void main(String args[])
	{
//		ICFGStructure graph = new ICFGStructure();
//		runAnalysis(graph);
	}
	public void createICFG(final ICFGStructure methodGraph) {
		G.reset();

		Transform transform = new Transform("wjtp.myTransform", new SceneTransformer() {
			@Override
			protected void internalTransform(String phase, Map<String, String> arg1) {
				Options.v().set_keep_line_number(true);
				Options.v().set_print_tags_in_output(true);
				SootMethod entryMethod = null;				
				CallGraph cg =Scene.v().getCallGraph();
				Scene.v().getClasses();
				java.util.List<SootMethod> listMethod = Scene.v().getEntryPoints();
				Iterator<SootMethod> iterEntryMethod = listMethod.iterator();
				while(iterEntryMethod.hasNext())
				{
					entryMethod = iterEntryMethod.next();
					if(entryMethod.isMain())
					{						
						methodcount++;
						Method method = new Method(methodcount, entryMethod);
						listMethods.add(method);
						break;
					}
				}

				traverseMethods(entryMethod, cg);				
				
/*				Chain<SootClass> classChain = Scene.v().getApplicationClasses();
				Iterator<SootClass> classIterator = classChain.iterator();
				while(classIterator.hasNext())
				{
					SootClass sootClass = classIterator.next();
					
					System.out.println(sootClass.getJavaSourceStartLineNumber());
					System.out.println("======================="+sootClass+"===========================");
					List<SootMethod> methodChain = sootClass.getMethods();
					Iterator<SootMethod> methodIterator = methodChain.iterator();
					while(methodIterator.hasNext())
					{
						SootMethod sootMethod = methodIterator.next();						
						if(sootMethod.hasActiveBody())
						{
							Body body = sootMethod.getActiveBody();
							System.out.println(body);
							methodNumber++;
							System.out.println(methodNumber);
							System.out.println(sootMethod);
							methodMap.put(methodNumber, sootMethod);
//							Chain<Unit> unitChain = body.getUnits();
//							Iterator<Unit> unitIterator = unitChain.iterator();
//							while(unitIterator.hasNext())
//							{
//								Unit unit = unitIterator.next();
//								System.out.println("Unit is =============>"+unit);
//								LineNumberTag tag = (LineNumberTag)unit.getTag("LineNumberTag");								
//								if(tag!=null)
//								{
//									System.out.println("Unit number is "+tag.getLineNumber());
//								}
//								List<Tag> listTags = unit.getTags();
//								Iterator it = listTags.iterator();
//								while(it.hasNext())
//								{
//									Tag ta = (Tag)it.next();
//									System.out.println(ta.getName()+"<<<<===============>>>>"+ta.getValue());
//								}
//							}
						}
					}
				}*/
				
				//GraphStructure g = new GraphStructure();
				//MainClass.runAnalysis(g, methodMap.get(1));
			}
		});

		PackManager.v().getPack("wjtp").add(transform);

		String rtJar = System.getProperty("java.home") + File.separator + "lib" + File.separator + "rt.jar";

		// Run Soot
		Main.main(new String[] { "-cp", "./bin" + File.pathSeparator + rtJar, "-exclude", "javax",
				"-allow-phantom-refs", "-no-bodies-for-excluded", "-process-dir", "./targetBin1", "-src-prec",
				"only-class", "-w", "-output-format", "J", "-keep-line-number","tag.ln","on","de.visuflow.analyzeMe.ex1.TargetClass1" });
	}
	
	public static void traverseMethods(SootMethod source, CallGraph cg)
	{			
		Targets tc = new Targets(cg.edgesOutOf(source));		
		while(tc.hasNext())
		{
			SootMethod destination = (SootMethod)tc.next();			
			if(!destination.isJavaLibraryMethod())
			{
				System.out.println(destination+" has active body "+destination.hasActiveBody());
				boolean methodPresent = false;
				Iterator<Method> iteratorMethod = listMethods.iterator();
				while(iteratorMethod.hasNext())
				{
					if(iteratorMethod.next().getMethod().equals(destination))
					{
						methodPresent = true;
						break;
					}
				}
				
				if(!methodPresent)
				{
					methodcount++;
					Method method = new Method(methodcount, destination);
					listMethods.add(method);
				}
				Method sourceMethod = null, destinationMethod = null;
				Iterator<Method> iteratorMethods = listMethods.iterator();
				while(iteratorMethods.hasNext())
				{
					Method method = iteratorMethods.next();
					if(method.getMethod().equals(source))
					{
						sourceMethod = method;
					}
					if(method.getMethod().equals(destination))
					{
						destinationMethod = method;
					}
				}
				edgeCount++;
				Edge edge = new Edge(edgeCount, sourceMethod, destinationMethod);
				listEdges.add(edge);
				traverseMethods(destination, cg);
			}
		}
	}	

}


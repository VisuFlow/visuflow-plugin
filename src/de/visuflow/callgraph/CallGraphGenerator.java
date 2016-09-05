package de.visuflow.callgraph;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.BodyTransformer;
import soot.G;
import soot.Main;
import soot.PackManager;
import soot.SootMethod;
import soot.Transform;
import soot.options.Options;

public class CallGraphGenerator {

public static void main(String[] args) {
		
//		HashMap<SootMethod, GraphStructure> hashMap = new HashMap<>();
//		runAnalysis(hashMap);
//		System.out.println(hashMap);
//		Set<SootMethod> keys = hashMap.keySet();
//
//		   for (Iterator<SootMethod> i = keys.iterator(); i.hasNext(); ) {
//		       SootMethod m = (SootMethod) i.next();
//		       System.out.println(m);		       
//		   }
	}

	public static void runAnalysis(final HashMap<SootMethod, GraphStructure> hashMap) {
		G.reset();
		Transform transform = new Transform("jtp.analysis", new BodyTransformer() {

			@Override
			protected void internalTransform(Body b, String phaseName, Map<String, String> options) {

				Options.v().set_keep_line_number(true);
				Options.v().debug();
				IntraproceduralAnalysis ipa = new IntraproceduralAnalysis(b, hashMap);
				ipa.doAnalyis();
			}

		});
		PackManager.v().getPack("jtp").add(transform);
		String rtJar = System.getProperty("java.home") + File.separator + "lib" + File.separator + "rt.jar";
		Main.main(new String[] { "-cp", "./bin" + File.pathSeparator + rtJar, "-exclude", "javax",
				"-allow-phantom-refs", "-no-bodies-for-excluded", "-process-dir", "./targetBin2", "-src-prec",
				"only-class", "-w", "-output-format", "J", "-keep-line-number","tag.ln","on","de.visuflow.analyzeMe.ex2.TargetClass2" });
	}
}

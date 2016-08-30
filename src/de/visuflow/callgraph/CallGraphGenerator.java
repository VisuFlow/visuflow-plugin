package de.visuflow.callgraph;

import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.G;
import soot.Main;
import soot.PackManager;
import soot.Transform;
import soot.options.Options;

public class CallGraphGenerator {

	/*public static void main(String[] args) {
		GraphStructure g = new GraphStructure();
		runAnalysis(g);
		System.out.println(g.listNodes);
		System.out.println(g.listEdges);
	}*/

	public void runAnalysis(final GraphStructure g) {
		G.reset();

		// Register the transform
		Transform transform = new Transform("jtp.analysis", new BodyTransformer() {

			@Override
			protected void internalTransform(Body b, String phaseName, Map<String, String> options) {
				
				Options.v().set_keep_line_number(true);
				Options.v().debug();
				IntraproceduralAnalysis ipa = new IntraproceduralAnalysis(b,g);
				ipa.doAnalyis();
				
			}

		});
		PackManager.v().getPack("jtp").add(transform);

		// Run Soot
		Main.main(
				new String[] { "-pp", "-process-dir", "C:/Users/Shashank B S/Documents/Projects/GraphStreamFeasibility/targetBin2", "-src-prec", "class", "-output-format", "none", "-keep-line-number","de.visuflow.analyzeMe.ex2.TargetClass2" });
	}

}

package de.visuflow.ex2;

import java.util.Map;

import de.visuflow.reporting.EmptyReporter;
import de.visuflow.reporting.IReporter;
import soot.Body;
import soot.BodyTransformer;
import soot.G;
import soot.Main;
import soot.PackManager;
import soot.PhaseOptions;
import soot.Transform;
import soot.options.Options;

public class MainClass {

	public static void main(String[] args) {
		GraphStructure g = new GraphStructure();
		runAnalysis(new EmptyReporter(), 3, g);
		System.out.println("Displaying in main method");
		System.out.println(g.edgesMap);
		System.out.println(g.nodesMap);
	}

	public static void runAnalysis(final IReporter reporter, final int exercisenumber, final GraphStructure g) {
		G.reset();

		// Register the transform
		Transform transform = new Transform("jtp.analysis", new BodyTransformer() {
			@Override
			protected void internalTransform(Body b, String phaseName, Map<String, String> options) {
				
				Options.v().set_keep_line_number(true);
				Options.v().debug();
				IntraproceduralAnalysis ipa = new IntraproceduralAnalysis(b, reporter, g);
				ipa.doAnalyis();
				
			}

		});
		PackManager.v().getPack("jtp").add(transform);

		// Run Soot
		Main.main(
				new String[] { "-pp", "-process-dir", "./targetBin2", "-src-prec", "class", "-output-format", "none", "-keep-line-number","de.visuflow.analyzeMe.ex2.TargetClass2" });
	}

}

package de.visuflow.callgraph;

import java.util.List;
import java.util.Map;

import de.unipaderborn.visuflow.model.VFClass;
import soot.Body;
import soot.BodyTransformer;
import soot.G;
import soot.Main;
import soot.PackManager;
import soot.Transform;
import soot.options.Options;

public class CallGraphGenerator {

    public void runAnalysis(final List<VFClass> vfClasses) {
        G.reset();
        Transform transform = new Transform("jtp.analysis", new BodyTransformer() {

            @Override
            protected void internalTransform(Body b, String phaseName, Map<String, String> options) {

                Options.v().set_keep_line_number(true);
                Options.v().debug();
                IntraproceduralAnalysis ipa = new IntraproceduralAnalysis(b, vfClasses);
                ipa.doAnalyis();
            }

        });
        PackManager.v().getPack("jtp").add(transform);
        //		String rtJar = System.getProperty("java.home") + File.separator + "lib" + File.separator + "rt.jar";
        Main.main(new String[] { "-pp", "-process-dir", "../../targetBin2", "-src-prec", "class", "-output-format", "none", "-keep-line-number","de.visuflow.analyzeMe.ex2.TargetClass2" });
    }
}

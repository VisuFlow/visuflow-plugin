package de.unipaderborn.visuflow.wizard;

import java.io.IOException;
//import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//import java.util.Set;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

import heros.FlowFunctions;
import soot.G;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;
import soot.toolkits.graph.ExceptionalUnitGraph;

import heros.InterproceduralCFG;
import heros.solver.IFDSSolver;

/**
 * 
 * @author kaarthik
 *
 */
public class CodeGenerator {

	/**
	 * @param input
	 * @throws JClassAlreadyExistsException
	 * @throws IOException
	 */
	public static void generateSource(WizardInput input) throws JClassAlreadyExistsException, IOException {
		if (input.AnalysisFramework.equals("Soot")) {
			generateGeneralClass(input);

		} else {
			generateIFDSClass(input);
		}
		generateMain(input);
	}

	/**
	 * @param input
	 * @throws JClassAlreadyExistsException
	 * @throws IOException
	 */
	public static void generateGeneralClass(WizardInput input) throws JClassAlreadyExistsException, IOException {
		JCodeModel codeModel = new JCodeModel();
		JPackage jp = codeModel._package(input.getPackageName());
		JDefinedClass classToBeCreated = jp._class(input.getAnalysisType());

		classToBeCreated.field(JMod.PRIVATE, Integer.class, "flowThroughCount").assign(JExpr.lit(0));
		classToBeCreated.field(JMod.PRIVATE | JMod.FINAL, soot.SootMethod.class, "method");

		JClass flowAbstraction = null;
		try {
			flowAbstraction = codeModel.ref(Class.forName("java.util." + input.getFlowType()));

			if (input.getFlowType1() != null && !input.getFlowType1().equals("Custom")) {
				flowAbstraction = flowAbstraction.narrow(Class.forName("java.lang." + input.getFlowType1()));
			} else if (input.getCustomClassFirst() != null) {
				JDefinedClass firstClass = jp._class(input.getCustomClassFirst());
				flowAbstraction = flowAbstraction.narrow(firstClass);
			}

			if (input.getFlowtype2() != null && !input.getFlowtype2().equals("Custom")) {
				flowAbstraction = flowAbstraction.narrow(Class.forName("java.lang." + input.getFlowtype2()));
			} else if (input.getCustomClassSecond() != null) {
				JDefinedClass secondClass = jp._class(input.getCustomClassSecond());
				flowAbstraction = flowAbstraction.narrow(secondClass);
			}

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// JClass flowAbstraction = codeModel.ref(HashSet.class).narrow(Integer.class);
		JClass jClassExtends = null;
		try {
			jClassExtends = codeModel.ref(Class.forName("soot.toolkits.scalar." + input.getAnalysisDirection() + "FlowAnalysis")).narrow(soot.Unit.class)
					.narrow(flowAbstraction);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		classToBeCreated._extends(jClassExtends);
		JMethod ctor = classToBeCreated.constructor(JMod.PUBLIC);
		ctor.param(soot.Body.class, "body");
		// ctor.param(Integer.class, "reporter");
		JBlock ctorBlock = ctor.body();
		JType exceptionalType = codeModel.ref(ExceptionalUnitGraph.class);
		ctorBlock.invoke("super").arg(JExpr._new(exceptionalType).arg(JExpr.ref("body")));

		ctorBlock.assign(JExpr._this().ref("method"), JExpr.ref("body").invoke("getMethod"));

		// Flow through function
		JMethod flowThrough = classToBeCreated.method(JMod.PUBLIC, void.class, "flowThrough");
		flowThrough.param(flowAbstraction, "in");
		flowThrough.param(soot.Unit.class, "unit");
		flowThrough.param(flowAbstraction, "out");
		flowThrough.annotate(codeModel.ref(Override.class));

		// InitialFlow
		JMethod newInitialFlow = classToBeCreated.method(JMod.PROTECTED, flowAbstraction, "newInitialFlow");
		JBlock newInitialFlowBlock = newInitialFlow.body();
		newInitialFlow.annotate(codeModel.ref(Override.class));
		JVar newInitialFlowImpl = newInitialFlowBlock.decl(flowAbstraction, "varName");
		// newInitialFlowImpl.init(JExpr._new(flowAbstraction));
		newInitialFlowBlock._return(newInitialFlowImpl);

		JMethod entryInitialFlow = classToBeCreated.method(JMod.PROTECTED, flowAbstraction, "entryInitialFlow");
		JBlock entryInitialFlowBlock = entryInitialFlow.body();
		entryInitialFlow.annotate(codeModel.ref(Override.class));
		JVar entryInitialFlowImpl = entryInitialFlowBlock.decl(flowAbstraction, "varName");
		// entryInitialFlowImpl.init(JExpr._new(flowAbstraction));
		entryInitialFlowBlock._return(entryInitialFlowImpl);

		JMethod merge = classToBeCreated.method(JMod.PROTECTED, void.class, "merge");
		merge.param(flowAbstraction, "in1");
		merge.param(flowAbstraction, "in2");
		merge.param(flowAbstraction, "out");
		merge.annotate(codeModel.ref(Override.class));
		if (input.getFlowType() != "Set") {
			merge.body().invoke(JExpr.ref("out"), "putAll").arg(JExpr.ref("in1"));
			merge.body().invoke(JExpr.ref("out"), "putAll").arg(JExpr.ref("in2"));
		} else {
			merge.body().invoke(JExpr.ref("out"), "addAll").arg(JExpr.ref("in1"));
			merge.body().invoke(JExpr.ref("out"), "addAll").arg(JExpr.ref("in2"));
		}

		JMethod copy = classToBeCreated.method(JMod.PROTECTED, void.class, "copy");
		copy.param(flowAbstraction, "source");
		copy.param(flowAbstraction, "dest");
		copy.annotate(codeModel.ref(Override.class));
		copy.body().invoke(JExpr.ref("dest"), "clear");
		if (input.getFlowType() != "Set") {
			copy.body().invoke(JExpr.ref("dest"), "putAll").arg(JExpr.ref("source"));
		} else {
			copy.body().invoke(JExpr.ref("dest"), "addAll").arg(JExpr.ref("source"));
		}

		JMethod doAnalysis = classToBeCreated.method(JMod.PROTECTED, void.class, "doAnalysis");
		doAnalysis.body().invoke(JExpr._super(), "doAnalysis");
		codeModel.build(input.getFile());
		// generateMain(input);
	}

	/**
	 * @param input
	 * @throws JClassAlreadyExistsException
	 * @throws IOException
	 */
	public static void generateIFDSClass(WizardInput input) throws JClassAlreadyExistsException, IOException {
		JCodeModel codeModel = new JCodeModel();
		JPackage jp = codeModel._package(input.getPackageName());
		JDefinedClass classToBeCreated = jp._class(input.getAnalysisType());

		JClass flowAbstraction = null;
		try {
			if(!input.getFlowType().equals("Select")){
			flowAbstraction = codeModel.ref(Class.forName("java.util." + input.getFlowType()));

			if (input.getFlowType1() != null && !input.getFlowType1().equals("Custom")) {
				flowAbstraction = flowAbstraction.narrow(Class.forName("java.lang." + input.getFlowType1()));
			} else if (input.getCustomClassFirst() != null) {
				JDefinedClass firstClass = jp._class(input.getCustomClassFirst());
				flowAbstraction = flowAbstraction.narrow(firstClass);
			}

			if (input.getFlowtype2() != null && !input.getFlowtype2().equals("Custom")) {
				flowAbstraction = flowAbstraction.narrow(Class.forName("java.lang." + input.getFlowtype2()));
			} else if (input.getCustomClassSecond() != null) {
				JDefinedClass secondClass = jp._class(input.getCustomClassSecond());
				flowAbstraction = flowAbstraction.narrow(secondClass);
			}
			}
			else{
				if (input.getFlowType1() != null && !input.getFlowType1().equals("Custom")) {
					flowAbstraction = codeModel.ref(Class.forName("java.lang." + input.getFlowType1()));
				} else if (input.getCustomClassFirst() != null) {
					flowAbstraction = jp._class(input.getCustomClassFirst());
				}
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		JClass interproceduralCFG = codeModel.ref(heros.InterproceduralCFG.class);
		interproceduralCFG = interproceduralCFG.narrow(soot.Unit.class);
		interproceduralCFG = interproceduralCFG.narrow(soot.SootMethod.class);

		JClass extendsClass = codeModel.ref(soot.jimple.toolkits.ide.DefaultJimpleIFDSTabulationProblem.class);
		extendsClass = extendsClass.narrow(flowAbstraction);
		extendsClass = extendsClass.narrow(interproceduralCFG);

		classToBeCreated._extends(extendsClass);

		JMethod ctor = classToBeCreated.constructor(JMod.PUBLIC);
		ctor.param(interproceduralCFG, "icfg");
		JBlock ctorBlock = ctor.body();
		ctorBlock.invoke("super").arg(JExpr.ref("icfg"));

		JClass mapParam = codeModel.ref(Map.class);
		mapParam = mapParam.narrow(soot.Unit.class);
		JClass setParam = codeModel.ref(Set.class).narrow(flowAbstraction);
		mapParam = mapParam.narrow(setParam);

		JMethod initialSeeds = classToBeCreated.method(JMod.PUBLIC, mapParam, "initialSeeds");
		initialSeeds.annotate(codeModel.ref(Override.class));
		initialSeeds.body()._return(JExpr._null());

		JClass flowFunctions = codeModel.ref(FlowFunctions.class);
		flowFunctions = flowFunctions.narrow(soot.Unit.class);
		flowFunctions = flowFunctions.narrow(flowAbstraction);
		flowFunctions = flowFunctions.narrow(soot.SootMethod.class);

		JMethod createFlowFunctionsFactory = classToBeCreated.method(JMod.PROTECTED, flowFunctions, "createFlowFunctionsFactory");
		createFlowFunctionsFactory.annotate(codeModel.ref(Override.class));
		createFlowFunctionsFactory.body()._return(JExpr._null());

		JMethod createZeroValue = classToBeCreated.method(JMod.PROTECTED, flowAbstraction, "createZeroValue");
		createZeroValue.annotate(codeModel.ref(Override.class));
		createZeroValue.body()._return(JExpr._null());
		codeModel.build(input.getFile());
	}

	/**
	 * @param input
	 * @throws JClassAlreadyExistsException
	 * @throws IOException
	 */
	public static void generateMain(WizardInput input) throws JClassAlreadyExistsException, IOException {
		JCodeModel codeModel = new JCodeModel();
		JPackage jp = codeModel._package(input.getPackageName());
		JDefinedClass classToBeCreated = jp._class(input.getClassName());
		JMethod runAnalysis = classToBeCreated.method(JMod.PUBLIC | JMod.STATIC, void.class, "runAnalysis");
		JBlock runAnalysisBody = runAnalysis.body();
		JClass sootG = codeModel.ref(G.class);
		runAnalysisBody.staticInvoke(sootG, "reset");
		
		if (input.AnalysisFramework.equals("Soot")) {
			JClass transform = codeModel.ref(soot.Transform.class);
			JDefinedClass anonymousBodyTransformer = codeModel.anonymousClass(soot.BodyTransformer.class);
			JMethod internalTrasnform = anonymousBodyTransformer.method(JMod.PROTECTED, void.class, "internalTransform");
			internalTrasnform.param(soot.Body.class, "b");
			internalTrasnform.param(String.class, "phaseName");

			JClass jClassExtends = codeModel.ref(Map.class).narrow(String.class).narrow(String.class);
			internalTrasnform.param(jClassExtends, "options");
			internalTrasnform.annotate(codeModel.ref(Override.class));
			JBlock internalTrasnformBlock = internalTrasnform.body();

			JClass ipa = codeModel.ref(input.getAnalysisType());
			JVar newInitialFlowImpl = internalTrasnformBlock.decl(ipa, "ipa");
			newInitialFlowImpl.init(JExpr._new(ipa).arg(JExpr.ref("b")));
			internalTrasnformBlock.invoke(JExpr.ref("ipa"), "doAnalysis");
			runAnalysisBody.decl(transform, "transform").init(JExpr._new(transform).arg(JExpr.lit("jtp.analysis")).arg(JExpr._new(anonymousBodyTransformer)));
			codeModel.ref(soot.PackManager.class).staticRef("v()").invoke("getPath");
			runAnalysisBody
					.add(codeModel.ref(soot.PackManager.class).staticRef("v()").invoke("getPack").arg(JExpr.lit("jtp")).invoke("add").arg(JExpr.ref("transform")));

			JClass sootMain = codeModel.ref(soot.Main.class);
			runAnalysisBody.staticInvoke(sootMain, "main").arg(JExpr.newArray(codeModel.ref(String.class)).add(JExpr.lit("-pp")).add(JExpr.lit("-process-dir")));
			JMethod mainMethod = classToBeCreated.method(JMod.PUBLIC | JMod.STATIC, void.class, "main");
			mainMethod.param(String[].class, "args");
			JBlock mainMethodBlock = mainMethod.body();
			mainMethodBlock.invoke("runAnalysis");

		} else {
			JClass transform = codeModel.ref(soot.Transform.class);
			JDefinedClass anonymousBodyTransformer = codeModel.anonymousClass(soot.SceneTransformer.class);
			JMethod internalTrasnform = anonymousBodyTransformer.method(JMod.PROTECTED, void.class, "internalTransform");
			internalTrasnform.param(String.class, "phaseName");

			JClass jClassExtends = codeModel.ref(Map.class).narrow(String.class).narrow(String.class);
			internalTrasnform.param(jClassExtends, "options");
			internalTrasnform.annotate(codeModel.ref(Override.class));
			JBlock internalTrasnformBlock = internalTrasnform.body();
			
			JClass ipa = codeModel.ref(InterproceduralCFG.class);
			ipa = ipa.narrow(soot.Unit.class);
			ipa = ipa.narrow(soot.SootMethod.class);
			JVar newInitialFlowImpl = internalTrasnformBlock.decl(ipa, "icfg");
			JClass jimpleICFG = codeModel.ref(JimpleBasedInterproceduralCFG.class);
			newInitialFlowImpl.init(JExpr._new(jimpleICFG));
			
			JClass ipaproblem = codeModel.ref(input.getAnalysisType());
			JVar ipaproblemImpl = internalTrasnformBlock.decl(ipaproblem, "problem");
			ipaproblemImpl.init(JExpr._new(ipaproblem).arg(JExpr.ref("icfg")));
			
			JClass flowAbstraction = null;
			try {
				if(!input.getFlowType().equals("Select")){
				flowAbstraction = codeModel.ref(Class.forName("java.util." + input.getFlowType()));

				if (input.getFlowType1() != null && !input.getFlowType1().equals("Custom")) {
					flowAbstraction = flowAbstraction.narrow(Class.forName("java.lang." + input.getFlowType1()));
				} else if (input.getCustomClassFirst() != null) {
					JDefinedClass firstClass = jp._class(input.getCustomClassFirst());
					flowAbstraction = flowAbstraction.narrow(firstClass);
				}

				if (input.getFlowtype2() != null && !input.getFlowtype2().equals("Custom")) {
					flowAbstraction = flowAbstraction.narrow(Class.forName("java.lang." + input.getFlowtype2()));
				} else if (input.getCustomClassSecond() != null) {
					JDefinedClass secondClass = jp._class(input.getCustomClassSecond());
					flowAbstraction = flowAbstraction.narrow(secondClass);
				}
				}else{
					if (input.getFlowType1() != null && !input.getFlowType1().equals("Custom")) {
						flowAbstraction = codeModel.ref(Class.forName("java.lang." + input.getFlowType1()));
					} else if (input.getCustomClassFirst() != null) {
						flowAbstraction = jp._class(input.getCustomClassFirst());
					}
				}

			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			JClass ifdsSolver = codeModel.ref(IFDSSolver.class);
			ifdsSolver = ifdsSolver.narrow(soot.Unit.class);
			ifdsSolver = ifdsSolver.narrow(flowAbstraction);
			ifdsSolver = ifdsSolver.narrow(soot.SootMethod.class);
			ifdsSolver = ifdsSolver.narrow(ipa);
			JVar ifdsSolverImpl = internalTrasnformBlock.decl(ifdsSolver, "solver");
			ifdsSolverImpl.init(JExpr._new(ifdsSolver).arg(JExpr.ref("problem")));

			internalTrasnformBlock.invoke(JExpr.ref("solver"), "solve");
			
			runAnalysisBody.decl(transform, "transform").init(JExpr._new(transform).arg(JExpr.lit("wjtp.analysis")).arg(JExpr._new(anonymousBodyTransformer)));
			codeModel.ref(soot.PackManager.class).staticRef("v()").invoke("getPath");
			runAnalysisBody
					.add(codeModel.ref(soot.PackManager.class).staticRef("v()").invoke("getPack").arg(JExpr.lit("wjtp")).invoke("add").arg(JExpr.ref("transform")));

			JClass sootMain = codeModel.ref(soot.Main.class);
			runAnalysisBody.staticInvoke(sootMain, "main").arg(JExpr.newArray(codeModel.ref(String.class)).add(JExpr.lit("-pp")).add(JExpr.lit("-process-dir")));
			JMethod mainMethod = classToBeCreated.method(JMod.PUBLIC | JMod.STATIC, void.class, "main");
			mainMethod.param(String[].class, "args");
			JBlock mainMethodBlock = mainMethod.body();
			mainMethodBlock.invoke("runAnalysis");
		}


		codeModel.build(input.getFile());
	}
}

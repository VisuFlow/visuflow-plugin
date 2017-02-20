package de.unipaderborn.visuflow.wizard;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

import soot.G;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;

public class CodeGenerator {

	public static void generateSource(WizardInput input) throws JClassAlreadyExistsException, IOException {
		JCodeModel codeModel = new JCodeModel();

		JDefinedClass classToBeCreated = codeModel._class(input.getPackageName() + "."+input.getAnalysisType());

		classToBeCreated.field(JMod.PRIVATE, Integer.class, "flowThroughCount").assign(JExpr.lit(0));
		//JExpr.assign(field1, JExpr.lit(0));
		// field1.assign(JExpr.lit(0));

		classToBeCreated.field(JMod.PRIVATE | JMod.FINAL, soot.SootMethod.class, "method");

		JClass flowAbstraction = codeModel.ref(Set.class).narrow(Integer.class);
		JClass flowAbstractionInit = codeModel.ref(HashSet.class).narrow(Integer.class);
		JClass jClassExtends = codeModel.ref(ForwardFlowAnalysis.class).narrow(soot.Unit.class).narrow(flowAbstraction);
		classToBeCreated._extends(jClassExtends);
		JMethod ctor = classToBeCreated.constructor(JMod.PUBLIC);
		ctor.param(soot.Body.class, "body");
		ctor.param(Integer.class, "reporter");
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
		newInitialFlowImpl.init(JExpr._new(flowAbstractionInit));
		newInitialFlowBlock._return(newInitialFlowImpl);

		JMethod entryInitialFlow = classToBeCreated.method(JMod.PROTECTED, flowAbstraction, "entryInitialFlow");
		JBlock entryInitialFlowBlock = entryInitialFlow.body();
		entryInitialFlow.annotate(codeModel.ref(Override.class));
		JVar entryInitialFlowImpl = entryInitialFlowBlock.decl(flowAbstraction, "varName");
		entryInitialFlowImpl.init(JExpr._new(flowAbstractionInit));
		entryInitialFlowBlock._return(newInitialFlowImpl);

		JMethod merge = classToBeCreated.method(JMod.PROTECTED, void.class, "merge");
		merge.param(flowAbstraction, "in1");
		merge.param(flowAbstraction, "in2");
		merge.param(flowAbstraction, "out");
		merge.annotate(codeModel.ref(Override.class));
		merge.body().invoke(JExpr.ref("out"), "addAll").arg(JExpr.ref("in1"));
		merge.body().invoke(JExpr.ref("out"), "addAll").arg(JExpr.ref("in2"));

		JMethod copy = classToBeCreated.method(JMod.PROTECTED, void.class, "copy");
		copy.param(flowAbstraction, "source");
		copy.param(flowAbstraction, "dest");
		copy.annotate(codeModel.ref(Override.class));
		copy.body().invoke(JExpr.ref("dest"), "clear");
		copy.body().invoke(JExpr.ref("dest"), "addAll").arg(JExpr.ref("source"));

		JMethod doAnalysis = classToBeCreated.method(JMod.PROTECTED, void.class, "doAnalysis");
		doAnalysis.body().invoke(JExpr._super(), "doAnalysis");
		generateMain(input);
		codeModel.build(input.getFile());

	}

	public static void generateMain(WizardInput input) throws JClassAlreadyExistsException, IOException {
		JCodeModel codeModel = new JCodeModel();

		JDefinedClass classToBeCreated = codeModel._class(input.getPackageName() +"."+ input.getClassName());
		JMethod runAnalysis = classToBeCreated.method(JMod.PUBLIC | JMod.STATIC, void.class, "runAnalysis");
		JBlock runAnalysisBody = runAnalysis.body();
		JClass sootG = codeModel.ref(G.class);
		runAnalysisBody.staticInvoke(sootG, "reset");

		JClass transform = codeModel.ref(soot.Transform.class);

		JDefinedClass anonymousBodyTransformer = codeModel.anonymousClass(soot.BodyTransformer.class);
		JMethod internalTrasnform = anonymousBodyTransformer.method(JMod.PROTECTED, void.class, "internalTransform");
		internalTrasnform.param(soot.Body.class, "b");
		internalTrasnform.param(String.class, "phaseName");
		JClass jClassExtends = codeModel.ref(Map.class).narrow(String.class).narrow(String.class);
		internalTrasnform.param(jClassExtends, "options");
		internalTrasnform.annotate(codeModel.ref(Override.class));
		JBlock internalTrasnformBlock = internalTrasnform.body();
		JClass ipa = codeModel.ref("IntraproceduralAnalysis");
		JVar newInitialFlowImpl = internalTrasnformBlock.decl(ipa, "ipa");
		newInitialFlowImpl.init(JExpr._new(ipa).arg(JExpr.ref("b")).arg(JExpr.lit(0)));
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
		codeModel.build(input.getFile());
	}
}

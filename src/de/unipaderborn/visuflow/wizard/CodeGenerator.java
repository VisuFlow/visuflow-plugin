package de.unipaderborn.visuflow.wizard;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;

public class CodeGenerator {
	
	public static void generateSource(File src) throws JClassAlreadyExistsException, IOException {
        //Instantiate an instance of the JCodeModel class
        JCodeModel codeModel = new JCodeModel();
        
 
        //JDefinedClass will let you create a class in a specified package.
        
        
        JDefinedClass classToBeCreated = codeModel._class("de.com.visuflow.bar");
        
        JFieldVar field1 = classToBeCreated.field(JMod.PRIVATE, Integer.class, "flowThroughCount");
        JExpr.assign(field1, JExpr.lit(0));
        //field1.assign(JExpr.lit(0));
        
        JFieldVar field2 =classToBeCreated.field(JMod.PRIVATE|JMod.FINAL,soot.SootMethod.class,"method");
        
        JClass flowAbstraction = codeModel.ref(Set.class).narrow(Integer.class);
        JClass flowAbstractionInit = codeModel.ref(HashSet.class).narrow(Integer.class);
        JClass jClassExtends = codeModel.ref(ForwardFlowAnalysis.class).narrow(soot.Unit.class).narrow(flowAbstraction);
        classToBeCreated._extends(jClassExtends);
        JMethod ctor =  classToBeCreated.constructor(JMod.PUBLIC);
        ctor.param(soot.Body.class, "body");
        ctor.param(Integer.class, "reporter");
        JBlock ctorBlock = ctor.body();
        JType exceptionalType = codeModel.ref(ExceptionalUnitGraph.class);
        ctorBlock.invoke("super").arg(JExpr._new(exceptionalType).arg(JExpr.ref("body")));
        
        ctorBlock.assign(JExpr._this().ref("method"),JExpr.ref("body").invoke("getMethod"));
        
        //Flow through function
        JMethod flowThrough = classToBeCreated.method(JMod.PUBLIC, void.class, "flowThrough");
        flowThrough.param(flowAbstraction, "in");
        flowThrough.param(soot.Unit.class, "unit");
        flowThrough.param(flowAbstraction, "out");
        flowThrough.annotate(codeModel.ref(Override.class));
        
        //InitialFlow
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
        merge.body().invoke(JExpr.ref("out"),"addAll").arg(JExpr.ref("in1"));
        merge.body().invoke(JExpr.ref("out"),"addAll").arg(JExpr.ref("in2"));
        
        
        
        JMethod copy = classToBeCreated.method(JMod.PROTECTED, void.class, "copy");
        copy.param(flowAbstraction, "source");
        copy.param(flowAbstraction, "dest");
        copy.annotate(codeModel.ref(Override.class));
        copy.body().invoke(JExpr.ref("dest"),"clear");
        copy.body().invoke(JExpr.ref("dest"),"addAll").arg(JExpr.ref("source"));
        
        
        JMethod doAnalysis = classToBeCreated.method(JMod.PROTECTED, void.class, "doAnalysis");
        doAnalysis.body().invoke(JExpr._super(),"doAnalysis");
//        //Creating private fields in the class
//        JFieldVar field1 = classToBeCreated.field(JMod.PRIVATE, Long.class, "foo");
// 
//        //The codeModel instance will have a list of Java primitives which can be
//        //used to create a primitive field in the new class
//        JFieldVar field2 = classToBeCreated.field(JMod.PRIVATE, codeModel.DOUBLE, "bar");
//        
//               
// 
//        //Create getter and setter methods for the fields
//        JMethod field1GetterMethod = classToBeCreated.method(JMod.PUBLIC, field1.type(), "getFoo");
//        //code to create a return statement with the field1
//        field1GetterMethod.body()._return(field1);
//        JMethod field1SetterMethod = classToBeCreated.method(JMod.PUBLIC, codeModel.VOID, "setFoo");
//        
//        //code to create an input parameter to the setter method which will take a variable of type field1
//        field1SetterMethod.param(field1.type(), "inputFoo");
//        //code to create an assignment statement to assign the input argument to the field1
//        field1SetterMethod.body().assign(JExpr._this().ref ("foo"), JExpr.ref ("inputFoo"));
// 
//        JMethod field2GetterMethod = classToBeCreated.method(JMod.PUBLIC, field2.type(), "getBar");
//        field2GetterMethod.body()._return(field2);
//        JMethod field2SetterMethod = classToBeCreated.method(JMod.PUBLIC, codeModel.VOID, "setBar");
//        field2SetterMethod.param(field2.type(), "inputBar");
//        field2SetterMethod.body().assign(JExpr._this().ref ("bar"), JExpr.ref ("inputBar"));
// 
//        //creating an enum class within our main class
//        JDefinedClass enumClass = classToBeCreated._enum(JMod.PUBLIC, "REPORT_COLUMNS");
//        //This code creates field within the enum class
//        JFieldVar columnField = enumClass.field(JMod.PRIVATE|JMod.FINAL, String.class, "column");
//        JFieldVar filterableField = enumClass.field(JMod.PRIVATE|JMod.FINAL, codeModel.BOOLEAN, "filterable");
// 
//        //Define the enum constructor
//        JMethod enumConstructor = enumClass.constructor(JMod.PRIVATE);
//        enumConstructor.param(String.class, "column");
//        enumConstructor.param(codeModel.BOOLEAN, "filterable");
//        enumConstructor.body().assign(JExpr._this().ref ("column"), JExpr.ref("column"));
//        enumConstructor.body().assign(JExpr._this().ref ("filterable"), JExpr.ref("filterable"));
// 
//        JMethod getterColumnMethod = enumClass.method(JMod.PUBLIC, String.class, "getColumn");
//        getterColumnMethod.body()._return(columnField);
//        JMethod getterFilterMethod = enumClass.method(JMod.PUBLIC, codeModel.BOOLEAN, "isFilterable");
//        getterFilterMethod.body()._return(filterableField);
// 
//        JEnumConstant enumConst = enumClass.enumConstant("FOO_BAR");
//        enumConst.arg(JExpr.lit("fooBar"));
//        enumConst.arg(JExpr.lit(true));
 
        //This will generate the code in the specified file path.
        codeModel.build(src);

}
}

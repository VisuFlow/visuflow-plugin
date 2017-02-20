package de.unipaderborn.visuflow.wizard;

import java.io.File;

import org.eclipse.core.runtime.Path;

public class WizardInput {
	
	String flowType;
	String flowType1;
	String flowtype2;
	String customClassFirst;
	String customClassSecond;
	String ProjectPath;
	String TargetPath;
	String ProjectName;
	String PackageName;
	String ClassName;
	String AnalysisType;
	String AnalysisFramework;
	String AnalysisDirection;
	Path sootPath;
	public Path getSootPath() {
		return sootPath;
	}
	public void setSootPath(Path sootPath) {
		this.sootPath = sootPath;
	}
	File file;
	
	
	public File getFile() {
		return file;
	}
	public void setFile(File file) {
		this.file = file;
	}
	public String getFlowType() {
		return flowType;
	}
	public void setFlowType(String flowType) {
		this.flowType = flowType;
	}
	public String getFlowType1() {
		return flowType1;
	}
	public void setFlowType1(String flowType1) {
		this.flowType1 = flowType1;
	}
	public String getFlowtype2() {
		return flowtype2;
	}
	public void setFlowtype2(String flowtype2) {
		this.flowtype2 = flowtype2;
	}
	public String getCustomClassFirst() {
		return customClassFirst;
	}
	public void setCustomClassFirst(String customClassFirst) {
		this.customClassFirst = customClassFirst;
	}
	public String getCustomClassSecond() {
		return customClassSecond;
	}
	public void setCustomClassSecond(String customClassSecond) {
		this.customClassSecond = customClassSecond;
	}
	public String getProjectPath() {
		return ProjectPath;
	}
	public void setProjectPath(String projectPath) {
		ProjectPath = projectPath;
	}
	public String getTargetPath() {
		return TargetPath;
	}
	public void setTargetPath(String targetPath) {
		TargetPath = targetPath;
	}
	public String getProjectName() {
		return ProjectName;
	}
	public void setProjectName(String projectName) {
		ProjectName = projectName;
	}
	public String getPackageName() {
		return PackageName;
	}
	public void setPackageName(String packageName) {
		PackageName = packageName;
	}
	public String getClassName() {
		return ClassName;
	}
	public void setClassName(String className) {
		ClassName = className;
	}
	public String getAnalysisType() {
		return AnalysisType;
	}
	public void setAnalysisType(String analysisType) {
		AnalysisType = analysisType;
	}
	public String getAnalysisFramework() {
		return AnalysisFramework;
	}
	public void setAnalysisFramework(String analysisFramework) {
		AnalysisFramework = analysisFramework;
	}
	public String getAnalysisDirection() {
		return AnalysisDirection;
	}
	public void setAnalysisDirection(String analysisDirection) {
		AnalysisDirection = analysisDirection;
	}

}

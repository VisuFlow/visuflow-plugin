package de.unipaderborn.visuflow;

public interface VisuflowConstants {

	public static final String JAVA_BREAKPOINT_LISTENER = "visuflow.javabreakpoint.listener";
	public static final String JIMPLE_BREAKPOINT_MARKER = "visuflow.debug.breakpoint.marker";
	public static final String JIMPLE_INSTRUCTIONPOINTER_MARKER = "visuflow.debug.instructionPointer.marker";
	public static final String VISUFLOW_NATURE = "JimpleBuilder.VisuFlowNature";
	public static final String VISUFLOW_TOGGLE_BREAKPOINTS_TARGET = "jimple";

	public static final String EA_TOPIC_DEBUGGING_ACTION_BASE = "de/unipaderborn/visuflow/debug";
	public static final String EA_TOPIC_DEBUGGING_ACTION_ALL = EA_TOPIC_DEBUGGING_ACTION_BASE + "/*";
	public static final String EA_TOPIC_DEBUGGING_ACTION_RESUME = EA_TOPIC_DEBUGGING_ACTION_BASE + "/resume";
	public static final String EA_TOPIC_DEBUGGING_ACTION_STEP_OVER = EA_TOPIC_DEBUGGING_ACTION_BASE + "/stepOver";

}

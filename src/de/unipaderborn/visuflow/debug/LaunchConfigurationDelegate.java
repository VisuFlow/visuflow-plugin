package de.unipaderborn.visuflow.debug;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.RuntimeProcess;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.debug.eval.EvaluationManager;
import org.eclipse.jdt.debug.eval.IAstEvaluationEngine;
import org.eclipse.jdt.debug.eval.ICompiledExpression;
import org.eclipse.jdt.debug.eval.IEvaluationListener;
import org.eclipse.jdt.debug.eval.IEvaluationResult;
import org.eclipse.jdt.launching.JavaLaunchDelegate;

import de.unipaderborn.visuflow.debug.ui.BreakpointLocator;
import de.unipaderborn.visuflow.debug.ui.BreakpointLocator.BreakpointLocation;
import de.unipaderborn.visuflow.model.DataModel;
import de.unipaderborn.visuflow.util.ServiceUtil;

public class LaunchConfigurationDelegate extends JavaLaunchDelegate {

    private DataModel dataModel = ServiceUtil.getService(DataModel.class);
    private IDebugTarget target;
    private List<IBreakpoint> breakpoints = new ArrayList<>();

    private String inset;

    private IDebugEventSetListener listener = new IDebugEventSetListener() {
        @Override
        public void handleDebugEvents(DebugEvent[] events) {
            // System.out.println("Events " + events.length);
            for (int i = 0; i < events.length; i++) {
                DebugEvent debugEvent = events[i];
                if (debugEvent.getKind() == DebugEvent.SUSPEND) {
                    IJavaThread thread = (IJavaThread) debugEvent.getSource();
                    if (debugEvent.getDetail() == DebugEvent.BREAKPOINT) {
                        IBreakpoint[] breakpoints = thread.getBreakpoints();
                        for (IBreakpoint breakpoint : breakpoints) {
                            IJavaMethodBreakpoint methodBreakpoint = breakpoint.getAdapter(IJavaMethodBreakpoint.class);
                            if (methodBreakpoint != null && methodBreakpoint.getMarker() instanceof VisuflowMarkerWrapper) {
                                try {
                                    IJavaStackFrame top = (IJavaStackFrame) thread.getTopStackFrame();
                                    if (top == null) {
                                        continue;
                                    }

                                    IJavaProject project = getJavaProject(target.getLaunch().getLaunchConfiguration());
                                    IJavaDebugTarget javaDebugTarget = target.getAdapter(IJavaDebugTarget.class);
                                    IJavaVariable var;
                                    if (methodBreakpoint.isEntrySuspend(target)) {
                                        // method entry

                                        if (javaDebugTarget != null && thread.isSuspended()) {
                                            IAstEvaluationEngine engine = EvaluationManager.newAstEvaluationEngine(project, javaDebugTarget);
                                            ICompiledExpression compiledExpression = engine.getCompiledExpression("in.toString()", top);

                                            engine.evaluateExpression(compiledExpression, top, new IEvaluationListener() {
                                                @Override
                                                public void evaluationComplete(IEvaluationResult result) {
                                                    try {
                                                        if (result.getException() != null) {
                                                            result.getException().printStackTrace();
                                                        } else {
                                                            inset = result.getValue().getValueString();
                                                            thread.suspend();
                                                            ICompiledExpression compiledExpression = engine.getCompiledExpression("new String(d.getTag(\"Fully Qualified Name\").getValue())", top);
                                                            engine.evaluateExpression(compiledExpression, top, new IEvaluationListener() {
                                                                @Override
                                                                public void evaluationComplete(IEvaluationResult result) {
                                                                    try {
                                                                        if (result.getException() != null) {
                                                                            result.getException().printStackTrace();
                                                                        } else {
                                                                            String fqn = result.getValue().getValueString();
                                                                            dataModel.setInSet(fqn, "in", inset);
                                                                        }
                                                                    } catch (Throwable t) {
                                                                        t.printStackTrace();
                                                                    } finally {
                                                                        try {
                                                                            thread.resume();
                                                                        } catch (DebugException e) {
                                                                            // TODO Auto-generated catch block
                                                                            e.printStackTrace();
                                                                        }
                                                                    }
                                                                }
                                                            }, DebugEvent.EVALUATION, false);
                                                        }
                                                    } catch (Throwable t) {
                                                        t.printStackTrace();
                                                    } finally {
                                                        // if(i < events.length - 1 ) {
                                                        // try {
                                                        // thread.resume();
                                                        // } catch (DebugException e) {
                                                        // e.printStackTrace();
                                                        // }
                                                        // }
                                                    }

                                                }
                                            }, DebugEvent.EVALUATION, false);


                                        }

                                        // var = top.findVariable("in"); // TODO determine the variable names in BreakpointLocator or so
                                        // if (var != null) {
                                        // // for (IVariable variable : var.getValue().getVariables()) {
                                        // // dataModel.setInSet(null, variable.getName(), variable.getValue().getValueString());
                                        // // variable.getValue().getVariables();
                                        // // }
                                        // dataModel.setInSet(null, var.getName(), var.getValue().getValueString());
                                        // }
                                    } else {
                                        // method exit
                                        if (javaDebugTarget != null && thread.isSuspended()) {
                                            IAstEvaluationEngine engine = EvaluationManager.newAstEvaluationEngine(project, javaDebugTarget);
                                            ICompiledExpression compiledExpression = engine.getCompiledExpression("out.toString()", top);
                                            engine.evaluateExpression(compiledExpression, top, new IEvaluationListener() {
                                                @Override
                                                public void evaluationComplete(IEvaluationResult result) {
                                                    try {
                                                        if (result.getException() != null) {
                                                            result.getException().printStackTrace();
                                                        } else {
                                                            dataModel.setOutSet(null, "out", result.getValue().toString());
                                                        }
                                                    } catch (Throwable t) {
                                                        t.printStackTrace();
                                                    } finally {
                                                        try {
                                                            thread.resume();
                                                        } catch (DebugException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }

                                                }
                                            }, DebugEvent.EVALUATION, false);
                                        }
                                    }
                                } catch (Throwable t) {
                                    // TODO replace with proper logging
                                    t.printStackTrace();
                                } finally {
                                    // if (thread.isSuspended()) {
                                    // try {
                                    // thread.resume();
                                    // } catch (DebugException e) {
                                    // // TODO Auto-generated catch block
                                    // e.printStackTrace();
                                    // }
                                    // }
                                }
                            }
                        }
                    }
                } else if (debugEvent.getKind() == DebugEvent.TERMINATE) {
                    // this event is fired for each thread and stuff, but we only want to remove our breakpoints,
                    // when the JVM process terminates
                    if (debugEvent.getSource() instanceof RuntimeProcess) {
                        // remove this debug event listener to release it for garbage collection
                        DebugPlugin.getDefault().removeDebugEventListener(this);

                        // remove breakpoints
                        System.out.println("Removing visuflow breakpoints");
                        for (IBreakpoint breakpoint : breakpoints) {
                            try {
                                DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(breakpoint, true);
                            } catch (CoreException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                        breakpoints.clear();
                    }
                }
            }
        }
    };

    @Override
    public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
        String attr = configuration.getAttribute("test.attr", "fallback");
        System.out.println("Launching: " + attr);

        Map<String, Object> launchAttrs = configuration.getAttributes();
        for (Entry<String, Object> entry : launchAttrs.entrySet()) {
            System.out.println(entry.getKey() + "=" + entry.getValue());
        }

        // find flow functions to observe in-sets and out-sets
        BreakpointLocator locator = new BreakpointLocator();
        List<BreakpointLocation> locations = locator.findFlowFunctions();
        for (BreakpointLocation breakpointLocation : locations) {
            createMethodEntryBreapoint(breakpointLocation);
        }

        // add DebugEventListener, so that we can inspect variable values
        // at the breakpoints
        DebugPlugin.getDefault().addDebugEventListener(listener);

        // launch the program
        super.launch(configuration, mode, launch, monitor);
        target = launch.getDebugTarget();

    }

    private IBreakpoint createMethodEntryBreapoint(BreakpointLocation location) throws CoreException {
        int charStart = location.offset;
        int charEnd = charStart + location.length;
        int hitCount = 0; // no hit count
        boolean register = true; // register at BreakpointManager
        boolean entry = true; // suspend at method entry
        boolean exit = true; // suspend at method exit
        boolean nativeOnly = false; // suspend for native methods
        Map<String, Object> attrs = null;

        System.out.println("resource: " + location.resource);
        System.out.println("class: " + location.className);
        System.out.println("method: " + location.methodName);
        System.out.println("signature: " + location.methodSignature);
        System.out.println("resource: " + location.resource);
        System.out.println("entry: " + entry);
        System.out.println("exit: " + exit);
        System.out.println("nativeOnly: " + nativeOnly);
        System.out.println("line: " + location.lineNumber);
        System.out.println("charStart: " + charStart);
        System.out.println("charEnd: " + charEnd);
        System.out.println("hit count: " + hitCount);
        System.out.println("register: " + register);
        System.out.println("attrs: " + attrs);

        IJavaMethodBreakpoint breakpoint = JDIDebugModel.createMethodBreakpoint(location.resource, location.className, location.methodName,
                location.methodSignature, entry, exit, nativeOnly, location.lineNumber, charStart, charEnd, hitCount, register, attrs);
        breakpoint.setMarker(new VisuflowMarkerWrapper(breakpoint.getMarker()));

        breakpoints.add(breakpoint);
        return breakpoint;
    }
}

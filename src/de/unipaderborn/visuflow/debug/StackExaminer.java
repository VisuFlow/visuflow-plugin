package de.unipaderborn.visuflow.debug;

import java.util.List;

import org.apache.commons.lang.ClassUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;

import de.unipaderborn.visuflow.Logger;
import de.unipaderborn.visuflow.Visuflow;
import de.unipaderborn.visuflow.model.DataModel;
import de.unipaderborn.visuflow.model.VFUnit;
import de.unipaderborn.visuflow.util.ServiceUtil;
import soot.tagkit.Host;

public class StackExaminer {
	private static final transient Logger logger = Visuflow.getDefault().getLogger();

	public static VFUnit getUnitFromStack(IJavaThread thread) throws CoreException {
		String fqn = getUnitFromTopStackFrame(thread);
		if(fqn == null) {
			throw new RuntimeException("No unit found on top stackframe");
		} else {
			DataModel model = ServiceUtil.getService(DataModel.class);
			VFUnit unit = model.getVFUnit(fqn);
			if(unit == null) {
				throw new RuntimeException("Unit not found in jimple model ["+fqn+"]");
			}
			return unit;
		}
	}

	/*
	 * TODO make sure, this returns the right unit at the moment we look at all
	 * variables in the top stackframe and just return the first one, which implements the TagHost interface.
	 * But the stack might contain several units and we might return the wrong one.
	 */
	private static String getUnitFromTopStackFrame(IJavaThread thread) throws CoreException {
		if (!thread.hasStackFrames()) {
			return null;
		}

		IStackFrame top = thread.getTopStackFrame();
		if (top.getVariables().length > 0) {
			for (IVariable var : top.getVariables()) {
				try {
					IValue value = var.getValue();
					if (value instanceof IJavaValue) {
						IJavaObject javaValue = (IJavaObject) value;
						IJavaDebugTarget debugTarget = thread.getDebugTarget().getAdapter(IJavaDebugTarget.class);
						IJavaValue arg = debugTarget.newValue("Fully Qualified Name");
						// the signature (2nd argument) can be retrieved with javap. Unit extends soot.tagkit.Host for the tag support
						// -> javap -cp soot-trunk.jar -s soot.tagkit.Host
						// the signature is in the output under "descriptor"
						IJavaType type = javaValue.getJavaType();
						if (isTagHost(type)) { // check, if this is a unit, which contains Tags
							IJavaValue fqnTag = javaValue.sendMessage("getTag", "(Ljava/lang/String;)Lsoot/tagkit/Tag;", new IJavaValue[] { arg }, thread,
									false);
							IJavaValue tagValue = ((IJavaObject) fqnTag).sendMessage("getValue", "()[B", new IJavaValue[0], thread, false);
							IJavaArray byteArray = (IJavaArray) tagValue;
							byte[] b = new byte[byteArray.getLength()];
							for (int i = 0; i < b.length; i++) {
								IJavaPrimitiveValue byteValue = (IJavaPrimitiveValue) byteArray.getValue(i);
								b[i] = byteValue.getByteValue();
							}
							String currentUnitFqn = new String(b);
							return currentUnitFqn;
						}
					}
				} catch (Exception e) {
					logger.error("Couldn't retrieve variable " + var.getName() + " from top stack frame", e);
				}
			}
		}
		return null;
	}


	private static boolean isTagHost(IJavaType type) throws ClassNotFoundException, DebugException {
		try {
			Class<?> clazz = Class.forName(type.getName());
			List<?> interfaces = ClassUtils.getAllInterfaces(clazz);
			return interfaces.contains(Host.class);
		} catch(ClassNotFoundException e) {
			// outside of scope, we can ignore this
			return false;
		}
	}
}

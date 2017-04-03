package de.unipaderborn.visuflow.ui.graph.formatting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import soot.Value;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.internal.JInstanceFieldRef;

public class ValueFormatter {

	public static String format(Value v) {
		String s = v.toString();
		if(v instanceof InvokeExpr) {
			s = formatInvoke(v);
		} else if(v instanceof JInstanceFieldRef) {
			s = formatFieldRef(v);
		}
		return s;
	}

	static String formatFieldRef(Value v) {
		JInstanceFieldRef fieldRef = (JInstanceFieldRef) v;
		return fieldRef.getBase().toString() + ".<" +
		shorten(fieldRef.getBase().getType().toString()) + ": " +
		shorten(fieldRef.getFieldRef().type().toString()) + " " +
		fieldRef.getFieldRef().name() + ">";
	}

	static String formatInvoke(Value v) {
		if(v instanceof InstanceInvokeExpr) {
			return formatInstanceInvoke((InstanceInvokeExpr) v);
		}

		String s = v.toString();
		s = s.replaceAll("virtualinvoke ", "");
		s = s.replaceAll("specialinvoke ", "");
		s = s.replaceAll("staticinvoke ", "");
		s = shortenClassNames(s);
		return s;
	}

	static String formatInstanceInvoke(InstanceInvokeExpr v) {
		StringBuilder sb = new StringBuilder();
		sb.append(v.getBase().toString()).append(".<")
		.append(shorten(v.getBase().getType().toString()))
		.append(": ").append(shorten(v.getMethod().getReturnType().toString()))
		.append(' ').append(v.getMethod().getName()).append('(');
		for (int i = 0; i < v.getArgCount(); i++) {
			if (i != 0) {
				sb.append(", ");
			}
			sb.append(shorten(v.getArg(i).getType().toString()));
		}
		sb.append(")>(");
		for (int i = 0; i < v.getArgCount(); i++) {
			if (i != 0) {
				sb.append(", ");
			}
			sb.append(format(v.getArg(i)));
		}
		sb.append(')');
		return sb.toString();
	}

	static String shortenClassNames(String s) {
		Matcher m = Pattern.compile("<(.*?): (.*?)>").matcher(s);
		if (m.find()) {
			String className = m.group(1);
			String shortenedClassName = shorten(className);
			String replacement = '<' + shortenedClassName + ": " + m.group(2) + '>';
			String escapedReplacement = Matcher.quoteReplacement(replacement);
			s = m.replaceAll(escapedReplacement);
		}
		return s;
	}

	static String shorten(String className) {
		String[] parts = className.split("\\.");
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < parts.length-1; i++) {
			//			sb.append(parts[i].charAt(0)).append('.');
		}
		sb.append(parts[parts.length-1]);
		return sb.toString();
	}
}

package de.unipaderborn.visuflow.debug.ui;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.graphics.Image;

/**
 * Special annotation, which makes it possible to have a little
 * image in the ruler of the JimpleEditor at the line of the
 * current unit while debugging.
 *
 * @author henni@upb.de
 *
 */
public class JimpleInstructionPointerAnnotation extends Annotation {
	private Image image;

	public JimpleInstructionPointerAnnotation() {
		super(false);
	}

	public JimpleInstructionPointerAnnotation(Image image) {
		this();
		this.image = image;
	}

	public Image getImage() {
		return image;
	}
}

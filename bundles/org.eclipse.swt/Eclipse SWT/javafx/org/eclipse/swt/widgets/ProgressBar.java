/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;

/**
 * Instances of the receiver represent an unselectable user interface object
 * that is used to display progress, typically in the form of a bar.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>SMOOTH, HORIZONTAL, VERTICAL, INDETERMINATE</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * <p>
 * Note: Only one of the styles HORIZONTAL and VERTICAL may be specified.
 * </p>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 * 
 * @see <a href="http://www.eclipse.org/swt/snippets/#progressbar">ProgressBar
 *      snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example:
 *      ControlExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further
 *      information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ProgressBar extends Control {

	private int maximum;
	private int minimum;
	private int actualValue;

	/**
	 * Constructs a new instance of this class given its parent and a style
	 * value describing its behavior and appearance.
	 * <p>
	 * The style value is either one of the style constants defined in class
	 * <code>SWT</code> which is applicable to instances of this class, or must
	 * be built by <em>bitwise OR</em>'ing together (that is, using the
	 * <code>int</code> "|" operator) two or more of those <code>SWT</code>
	 * style constants. The class description lists the style constants that are
	 * applicable to the class. Style bits are also inherited from superclasses.
	 * </p>
	 * 
	 * @param parent
	 *            a composite control which will be the parent of the new
	 *            instance (cannot be null)
	 * @param style
	 *            the style of control to construct
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the parent</li>
	 *                <li>ERROR_INVALID_SUBCLASS - if this class is not an
	 *                allowed subclass</li>
	 *                </ul>
	 * 
	 * @see SWT#SMOOTH
	 * @see SWT#HORIZONTAL
	 * @see SWT#VERTICAL
	 * @see SWT#INDETERMINATE
	 * @see Widget#checkSubclass
	 * @see Widget#getStyle
	 */
	public ProgressBar(Composite parent, int style) {
		super(parent, style);
		javafx.scene.control.ProgressBar pBar = new javafx.scene.control.ProgressBar();
		if ((style&SWT.INDETERMINATE) !=0) pBar.setProgress(-1);
		setNode(pBar);
	}

	/**
	 * Returns the maximum value which the receiver will allow.
	 * 
	 * @return the maximum
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public int getMaximum() {
		return this.maximum;
	}

	/**
	 * Returns the minimum value which the receiver will allow.
	 * 
	 * @return the minimum
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public int getMinimum() {
		return this.minimum;
	}

	/**
	 * Returns the single 'selection' that is the receiver's position.
	 * 
	 * @return the selection
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public int getSelection() {
		
		return this.actualValue;
	}

	/**
	 * Returns the state of the receiver. The value will be one of:
	 * <ul>
	 * <li>{@link SWT#NORMAL}</li>
	 * <li>{@link SWT#ERROR}</li>
	 * <li>{@link SWT#PAUSED}</li>
	 * </ul>
	 * 
	 * @return the state
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @since 3.4
	 */
	public int getState() {
		// TODO
		return 0;
	}

	/**
	 * Sets the maximum value that the receiver will allow. This new value will
	 * be ignored if it is not greater than the receiver's current minimum
	 * value. If the new maximum is applied then the receiver's selection value
	 * will be adjusted if necessary to fall within its new range.
	 * 
	 * @param value
	 *            the new maximum, which must be greater than the current
	 *            minimum
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setMaximum(int value) {
		this.maximum = value;
		updateProgress();
	}

	private void updateProgress() {
		double x1 = minimum;
		double x2 = maximum;
		double x = actualValue;
		if (x==0) getNode().setProgress(0); //protect from / 0
		getNode().setProgress((x2-x1)/x);
		
	}

	/**
	 * Sets the minimum value that the receiver will allow. This new value will
	 * be ignored if it is negative or is not less than the receiver's current
	 * maximum value. If the new minimum is applied then the receiver's
	 * selection value will be adjusted if necessary to fall within its new
	 * range.
	 * 
	 * @param value
	 *            the new minimum, which must be nonnegative and less than the
	 *            current maximum
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setMinimum(int value) {
		this.minimum = value;
		updateProgress();
	}

	/**
	 * Sets the single 'selection' that is the receiver's position to the
	 * argument which must be greater than or equal to zero.
	 * 
	 * @param value
	 *            the new selection (must be zero or greater)
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setSelection(int value) {
		this.actualValue = value;
		updateProgress();
	}

	/**
	 * Sets the state of the receiver. The state must be one of these values:
	 * <ul>
	 * <li>{@link SWT#NORMAL}</li>
	 * <li>{@link SWT#ERROR}</li>
	 * <li>{@link SWT#PAUSED}</li>
	 * </ul>
	 * <p>
	 * Note: This operation is a hint and is not supported on platforms that do
	 * not have this concept.
	 * </p>
	 * 
	 * @param state
	 *            the new state
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @since 3.4
	 */
	public void setState(int state) {
		// TODO
	}
	
	private javafx.scene.control.ProgressBar getNode(){
		return (javafx.scene.control.ProgressBar) node;
	}

}

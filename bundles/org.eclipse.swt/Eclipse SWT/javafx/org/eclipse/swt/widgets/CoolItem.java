/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Instances of this class are selectable user interface objects that represent
 * the dynamically positionable areas of a <code>CoolBar</code>.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>DROP_DOWN</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection</dd>
 * </dl>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 * 
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further
 *      information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CoolItem extends Item {

	private Control control;
	private CoolBar parent;
	boolean ideal;
	
	private int minimumWidth;
	private int minimumHeight;
	int prefWidth;
	int prefHeight;
	int requestedWidth;
	
	static final int MARGIN_WIDTH = 4;
	static final int GRABBER_WIDTH = 2;
	static final int MINIMUM_WIDTH = (2 * MARGIN_WIDTH) + GRABBER_WIDTH;
	
	private Rectangle rect = new Rectangle(0, 0, 0, 0);
	
	private Point fixPoint(int x, int y) {
		if( (parent.style & SWT.VERTICAL) == SWT.VERTICAL ) {
			return new Point(y, x);
		}
		return new Point(x, y);
	}
	
	Rectangle fixRectangle (int x, int y, int width, int height) {
		if ((parent.style & SWT.VERTICAL) != 0) {
			return new Rectangle(y, x, height, width);
		}
		return new Rectangle(x, y, width, height);
	}

	/**
	 * Constructs a new instance of this class given its parent (which must be a
	 * <code>CoolBar</code>) and a style value describing its behavior and
	 * appearance. The item is added to the end of the items maintained by its
	 * parent.
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
	 * @see SWT#DROP_DOWN
	 * @see Widget#checkSubclass
	 * @see Widget#getStyle
	 */
	public CoolItem(CoolBar parent, int style) {
		super(parent, style);
		this.parent = parent;
		this.parent.internal_registerItem(this);
	}

	/**
	 * Constructs a new instance of this class given its parent (which must be a
	 * <code>CoolBar</code>), a style value describing its behavior and
	 * appearance, and the index at which to place it in the items maintained by
	 * its parent.
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
	 * @param index
	 *            the zero-relative index at which to store the receiver in its
	 *            parent
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
	 *                <li>ERROR_INVALID_RANGE - if the index is not between 0
	 *                and the number of elements in the parent (inclusive)</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the parent</li>
	 *                <li>ERROR_INVALID_SUBCLASS - if this class is not an
	 *                allowed subclass</li>
	 *                </ul>
	 * 
	 * @see SWT#DROP_DOWN
	 * @see Widget#checkSubclass
	 * @see Widget#getStyle
	 */
	public CoolItem(CoolBar parent, int style, int index) {
		super(parent, style);
		this.parent = parent;
		this.parent.internal_registerItem(this, index);
	}

	/**
	 * Adds the listener to the collection of listeners that will be notified
	 * when the control is selected by the user, by sending it one of the
	 * messages defined in the <code>SelectionListener</code> interface.
	 * <p>
	 * If <code>widgetSelected</code> is called when the mouse is over the
	 * drop-down arrow (or 'chevron') portion of the cool item, the event object
	 * detail field contains the value <code>SWT.ARROW</code>, and the x and y
	 * fields in the event object represent the point at the bottom left of the
	 * chevron, where the menu should be popped up.
	 * <code>widgetDefaultSelected</code> is not called.
	 * </p>
	 * 
	 * @param listener
	 *            the listener which should be notified when the control is
	 *            selected by the user
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @see SelectionListener
	 * @see #removeSelectionListener
	 * @see SelectionEvent
	 * 
	 * @since 2.0
	 */
	public void addSelectionListener(SelectionListener listener) {
		checkWidget();
		if (listener == null)
			error(SWT.ERROR_NULL_ARGUMENT);
		TypedListener typedListener = new TypedListener(listener);
		addListener(SWT.Selection, typedListener);
		addListener(SWT.DefaultSelection, typedListener);
	}

	@Override
	protected void checkSubclass() {
		if (!isValidSubclass())
			error(SWT.ERROR_INVALID_SUBCLASS);
	}

	/**
	 * Returns the preferred size of the receiver.
	 * <p>
	 * The <em>preferred size</em> of a <code>CoolItem</code> is the size that
	 * it would best be displayed at. The width hint and height hint arguments
	 * allow the caller to ask the instance questions such as "Given a
	 * particular width, how high does it need to be to show all of the
	 * contents?" To indicate that the caller does not wish to constrain a
	 * particular dimension, the constant <code>SWT.DEFAULT</code> is passed for
	 * the hint.
	 * </p>
	 * 
	 * @param wHint
	 *            the width hint (can be <code>SWT.DEFAULT</code>)
	 * @param hHint
	 *            the height hint (can be <code>SWT.DEFAULT</code>)
	 * @return the preferred size
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @see Layout
	 * @see #getBounds
	 * @see #getSize
	 * @see Control#getBorderWidth
	 * @see Scrollable#computeTrim
	 * @see Scrollable#getClientArea
	 */
	public Point computeSize(int wHint, int hHint) {
		checkWidget();
		int width = wHint, height = hHint;
		if (wHint == SWT.DEFAULT)
			width = 32;
		if (hHint == SWT.DEFAULT)
			height = 32;
		if ((parent.style & SWT.VERTICAL) != 0) {
			height += MINIMUM_WIDTH;
		} else {
			width += MINIMUM_WIDTH;
		}
		return new Point(width, height);
	}

	/**
	 * Returns a rectangle describing the receiver's size and location relative
	 * to its parent.
	 * 
	 * @return the receiver's bounding rectangle
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public Rectangle getBounds() {
		checkWidget();
		return rect;
	}

	/**
	 * Returns the control that is associated with the receiver.
	 * 
	 * @return the control that is contained by the receiver
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public Control getControl() {
		checkWidget();
		return control;
	}

	/**
	 * Returns the minimum size that the cool item can be resized to using the
	 * cool item's gripper.
	 * 
	 * @return a point containing the minimum width and height of the cool item,
	 *         in pixels
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @since 2.0
	 */
	public Point getMinimumSize() {
		return fixPoint(minimumWidth, minimumHeight);
	}

	/**
	 * Returns the receiver's parent, which must be a <code>CoolBar</code>.
	 * 
	 * @return the receiver's parent
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public CoolBar getParent() {
		checkWidget();
		return parent;
	}

	/**
	 * Returns a point describing the receiver's ideal size. The x coordinate of
	 * the result is the ideal width of the receiver. The y coordinate of the
	 * result is the ideal height of the receiver.
	 * 
	 * @return the receiver's ideal size
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public Point getPreferredSize() {
		checkWidget();
		return fixPoint(prefWidth, prefHeight);
	}

	/**
	 * Returns a point describing the receiver's size. The x coordinate of the
	 * result is the width of the receiver. The y coordinate of the result is
	 * the height of the receiver.
	 * 
	 * @return the receiver's size
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public Point getSize() {
		checkWidget();
		return new Point(rect.width, rect.height);
	}

	/**
	 * Removes the listener from the collection of listeners that will be
	 * notified when the control is selected by the user.
	 * 
	 * @param listener
	 *            the listener which should no longer be notified
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @see SelectionListener
	 * @see #addSelectionListener
	 * 
	 * @since 2.0
	 */
	public void removeSelectionListener(SelectionListener listener) {
		checkWidget();
		if (listener == null)
			error(SWT.ERROR_NULL_ARGUMENT);
		if (eventTable == null)
			return;
		eventTable.unhook(SWT.Selection, listener);
		eventTable.unhook(SWT.DefaultSelection, listener);
	}

	/**
	 * Sets the control that is associated with the receiver to the argument.
	 * 
	 * @param control
	 *            the new control that will be contained by the receiver
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_ARGUMENT - if the control has been
	 *                disposed</li>
	 *                <li>ERROR_INVALID_PARENT - if the control is not in the
	 *                same widget tree</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setControl(Control control) {
		checkWidget();
		this.control = control;
		if( ideal ) {
//			control.internal_getNativeObject().setManaged(true);
//			control.internal_getNativeObject().setVisible(true);
//			Region r = (Region)control.internal_getNativeObject();
//			r.setPrefSize(prefWidth, prefHeight);
		}
		parent.internal_controlUpdated(this);
	}

	/**
	 * Sets the minimum size that the cool item can be resized to using the cool
	 * item's gripper, to the point specified by the arguments.
	 * 
	 * @param width
	 *            the minimum width of the cool item, in pixels
	 * @param height
	 *            the minimum height of the cool item, in pixels
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @since 2.0
	 */
	public void setMinimumSize(int width, int height) {
		checkWidget();
		Point p = fixPoint(width, height);
		minimumWidth = p.x;
		minimumHeight = p.y;
	}

	/**
	 * Sets the minimum size that the cool item can be resized to using the cool
	 * item's gripper, to the point specified by the argument.
	 * 
	 * @param size
	 *            a point representing the minimum width and height of the cool
	 *            item, in pixels
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the point is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @since 2.0
	 */
	public void setMinimumSize(Point size) {
		checkWidget();
		if (size == null)
			error(SWT.ERROR_NULL_ARGUMENT);
		setMinimumSize(size.x, size.y);
	}

	/**
	 * Sets the receiver's ideal size to the point specified by the arguments.
	 * 
	 * @param width
	 *            the new ideal width for the receiver
	 * @param height
	 *            the new ideal height for the receiver
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setPreferredSize(int width, int height) {
		checkWidget();
		ideal = true;
		Point p = fixPoint(width, height);
		prefWidth = p.x;
		prefHeight = p.y;
		if (control != null) {
			control.nativeControl.setPrefSize(prefWidth, prefHeight);
		}
	}

	/**
	 * Sets the receiver's ideal size to the point specified by the argument.
	 * 
	 * @param size
	 *            the new ideal size for the receiver
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the point is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setPreferredSize(Point size) {
		checkWidget();
		if (size == null)
			error(SWT.ERROR_NULL_ARGUMENT);
		setPreferredSize(size.x, size.y);
	}

	/**
	 * Sets the receiver's size to the point specified by the arguments.
	 * <p>
	 * Note: Attempting to set the width or height of the receiver to a negative
	 * number will cause that value to be set to zero instead.
	 * </p>
	 * 
	 * @param width
	 *            the new width for the receiver
	 * @param height
	 *            the new height for the receiver
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setSize(int width, int height) {
		checkWidget();
		Point p = fixPoint(width, height);
		width = Math.max(p.x, minimumWidth+MINIMUM_WIDTH);
		height = p.y;
		if(!ideal) {
			prefWidth = width;
			prefHeight = height;
		}
		rect.width = requestedWidth = width;
		rect.height = height;
	}

	/**
	 * Sets the receiver's size to the point specified by the argument.
	 * <p>
	 * Note: Attempting to set the width or height of the receiver to a negative
	 * number will cause them to be set to zero instead.
	 * </p>
	 * 
	 * @param size
	 *            the new size for the receiver
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the point is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setSize(Point size) {
		checkWidget();
		if (size == null)
			error(SWT.ERROR_NULL_ARGUMENT);
		setSize(size.x, size.y);
	}

}

/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.widgets;

import javafx.scene.Cursor;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Instances of the receiver represent a selectable user interface object that
 * allows the user to drag a rubber banded outline of the sash within the parent
 * control.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>HORIZONTAL, VERTICAL, SMOOTH</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection</dd>
 * </dl>
 * <p>
 * Note: Only one of the styles HORIZONTAL and VERTICAL may be specified.
 * </p>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 * 
 * @see <a href="http://www.eclipse.org/swt/snippets/#sash">Sash snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example:
 *      ControlExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further
 *      information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class Sash extends Control {

	private final static int INCREMENT = 1;
	private final static int PAGE_INCREMENT = 9;
	
	private int lastX;
	private int lastY;
	private int startX;
	private int startY;
	private boolean inDrag;
	
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
	 * @see SWT#HORIZONTAL
	 * @see SWT#VERTICAL
	 * @see SWT#SMOOTH
	 * @see Widget#checkSubclass
	 * @see Widget#getStyle
	 */
	public Sash(Composite parent, int style) {
		super(parent, style);
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified
	 * when the control is selected by the user, by sending it one of the
	 * messages defined in the <code>SelectionListener</code> interface.
	 * <p>
	 * When <code>widgetSelected</code> is called, the x, y, width, and height
	 * fields of the event object are valid. If the receiver is being dragged,
	 * the event object detail field contains the value <code>SWT.DRAG</code>.
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
	 */
	public void addSelectionListener(SelectionListener listener) {
		checkWidget();
		if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
		TypedListener typedListener = new TypedListener(listener);
		addListener(SWT.Selection,typedListener);
		addListener(SWT.DefaultSelection,typedListener);
	}

	@Override
	void createHandle() {
		nativeControl = new Region();
//		nativeControl.setStyle("-fx-background-color: red;");
//		nativeControl.setFocusTraversable(true);
		nativeControl.setCursor((style & SWT.VERTICAL) == SWT.VERTICAL ? Cursor.H_RESIZE : Cursor.V_RESIZE );
	}

	public Point computeSize (int wHint, int hHint, boolean changed) {
		checkWidget();
		int width = 0, height = 0;
		if ((style & SWT.HORIZONTAL) != 0) {
			width += DEFAULT_WIDTH;  height += 5;
		} else {
			width += 5; height += DEFAULT_HEIGHT;
		}
		if (wHint != SWT.DEFAULT) width = wHint;
		if (hHint != SWT.DEFAULT) height = hHint;
		return new Point (width, height);
	}

	/**
	 * Removes the listener from the collection of listeners who will be
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
	 */
	public void removeSelectionListener(SelectionListener listener) {
		checkWidget();
		if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
		removeListener(SWT.Selection, listener);
		removeListener(SWT.DefaultSelection,listener);
	}

	@Override
	void sendKeyEvent(KeyEvent event) {
		super.sendKeyEvent(event);
		
		if( event.getEventType() == KeyEvent.KEY_RELEASED ) {
			return;
		}
		
		if( event.getCode().isArrowKey() ) {
			int stepSize = PAGE_INCREMENT;
			if( event.isControlDown()  ) {
				stepSize = INCREMENT;
			}
			
			int xChange = 0;
			int yChange = 0;
			
			switch (event.getCode()) {
			case UP:
				yChange = -stepSize;
				break;
			case DOWN:
				yChange = stepSize;
				break;
			case LEFT:
				xChange = -stepSize;
				break;
			case RIGHT:
				xChange = stepSize;
				break;
			default:
				break;
			}
			
			Rectangle bounds = getBounds ();
			int width = bounds.width;
			int height = bounds.height;
			
			Rectangle parentBounds = getParent().getBounds ();
			int parentWidth = parentBounds.width;
			int parentHeight = parentBounds.height;
			
			int newX = lastX;
			int newY = lastY;
			if( (getStyle() & SWT.VERTICAL) == SWT.VERTICAL ) {
				newX = Math.min (Math.max (0, lastX + xChange), parentWidth - width);
			} else {
				newY = Math.min (Math.max (0, lastY + yChange), parentHeight - height);
			}
			if (newX == lastX && newY == lastY) return;
			
			Event evt = new Event();
			evt.x = newX;
			evt.y = newY;
			evt.width = width;
			evt.height = height;
			sendEvent(SWT.Selection, evt, true);
			if( evt.doit ) {
				setBounds(evt.x, evt.y, evt.width, evt.height);
				int cursorX = evt.x;
				int cursorY = evt.y;
				if ((getStyle() & SWT.VERTICAL) != 0) {
					cursorX += 1;
					cursorY += height / 2;
				} else {
					cursorX += width / 2;
					cursorY += 1;
				}
				getDisplay().setCursorLocation(getParent().toDisplay(cursorX, cursorY));
			}
		}
		
		
	}

	@Override
	void sendMouseEvent(int type, MouseEvent event) {
		super.sendMouseEvent(type, event);
		
		if( event.getEventType() == MouseEvent.MOUSE_PRESSED ) {
			startX = (int) event.getX();
			startY = (int) event.getY();
			Event evt = new Event();
			Rectangle b = getBounds(); 
			evt.x = b.x;
			evt.y = b.y;
			evt.width = b.width;
			evt.height = b.height;
			sendEvent(SWT.Selection, evt, true);
			if( evt.doit ) {
				lastX = evt.x;
				lastY = evt.y;
				inDrag = true;
			}
		} else if( event.getEventType() == MouseEvent.MOUSE_DRAGGED ) {
			if( ! inDrag ) {
				return;
			}
			int newX = lastX;
			int newY = lastY;
			Rectangle b = getBounds(); 
			if ((style & SWT.VERTICAL) != 0) {
				newX = Math.min (Math.max (0, (int)(event.getX() + b.x - startX)), (int)(getParent().getBounds().width - b.width));
			} else {
				newY = Math.min (Math.max (0, (int)(event.getY() + b.y - startY)), (int)(getParent().getBounds().height - b.height));
			}
			if (newX == lastX && newY == lastY) return;
			Event evt = new Event ();
			evt.x = newX;
			evt.y = newY;
			evt.width = (int)b.width;
			evt.height = (int)b.height;
			
			sendEvent(SWT.Selection, evt, true);
			if (evt.doit) {
				lastX = evt.x;
				lastY = evt.y;
				setBounds (evt.x, evt.y, (int)b.width, (int)b.height);
			}
		} else if( event.getEventType() == MouseEvent.MOUSE_RELEASED ) {
			inDrag = false;
			Rectangle b = getBounds(); 
			Event evt = new Event();
			evt.x = lastX;
			evt.y = lastY;
			evt.width = b.width;
			evt.height = b.height;
			sendEvent(SWT.Selection, evt, true);
			if( evt.doit ) {
				setBounds(evt.x, evt.y, evt.width, evt.height);
			}
		}
	}

	@Override
	public void setLocation(int x, int y) {
		this.lastX = x;
		this.lastY = y;
		super.setLocation(x, y);
	}

	@Override
	public void setBounds(int x, int y, int width, int height) {
		this.lastX = x;
		this.lastY = y;
		super.setBounds(x, y, width, height);
	}

}

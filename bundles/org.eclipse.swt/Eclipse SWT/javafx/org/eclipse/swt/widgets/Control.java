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

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.accessibility.Accessible;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DragDetectListener;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.GestureListener;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.TouchListener;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Drawable;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.GCData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.internal.MeasureGC;
import org.eclipse.swt.internal.Util;

/**
 * Control is the abstract superclass of all windowed user interface classes.
 * <p>
 * <dl>
 * <dt><b>Styles:</b>
 * <dd>BORDER</dd>
 * <dd>LEFT_TO_RIGHT, RIGHT_TO_LEFT, FLIP_TEXT_DIRECTION</dd>
 * <dt><b>Events:</b>
 * <dd>DragDetect, FocusIn, FocusOut, Help, KeyDown, KeyUp, MenuDetect,
 * MouseDoubleClick, MouseDown, MouseEnter, MouseExit, MouseHover, MouseUp,
 * MouseMove, MouseWheel, MouseHorizontalWheel, MouseVerticalWheel, Move, Paint,
 * Resize, Traverse</dd>
 * </dl>
 * </p>
 * <p>
 * Only one of LEFT_TO_RIGHT or RIGHT_TO_LEFT may be specified.
 * </p>
 * <p>
 * IMPORTANT: This class is intended to be subclassed <em>only</em> within the
 * SWT implementation.
 * </p>
 * 
 * @see <a href="http://www.eclipse.org/swt/snippets/#control">Control
 *      snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example:
 *      ControlExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further
 *      information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public abstract class Control extends Widget implements Drawable {

	public javafx.scene.layout.Region nativeControl;
	private Object layoutData;
	Composite parent;
	private Color foreground;
	private Color background;
	private Image backgroundImage;
	private Font font;

	private static Control LAST_ENTER;
	// private static InvalidationListener FOCUS_LISTENER;
	// TODO we should make those a stack because if someone e.g. pressed A+S we
	// miss the A keyup event
	private Event lastTypedDown;
	private Event lastLetterDown;
	private Menu menu;
	private Tooltip tooltip;
	private String tooltipText;
	private Cursor cursor;
	private Region region;

	// private static Control CURRENT_FOCUS_CONTROL;

	private EventHandler<ContextMenuEvent> contextMenuHandler = new EventHandler<ContextMenuEvent>() {
		@Override
		public void handle(ContextMenuEvent event) {
			Event evt = new Event();
			evt.x = (int) event.getScreenX();
			evt.y = (int) event.getScreenY();
			sendEvent(SWT.MenuDetect, evt, true);
		}
	};
	
	private EventHandler<javafx.scene.input.KeyEvent> keyEventHandler = new EventHandler<javafx.scene.input.KeyEvent>() {
		@Override
		public void handle(javafx.scene.input.KeyEvent event) {
			sendKeyEvent(event);
			event.consume();
		}
	};

	private EventHandler<javafx.scene.input.MouseEvent> mouseHandler = new EventHandler<javafx.scene.input.MouseEvent>() {
		@Override
		public void handle(javafx.scene.input.MouseEvent event) {
			if (isDisposed())
				return;
			int type = SWT.None;

			if (event.getEventType() == javafx.scene.input.MouseEvent.MOUSE_EXITED) {
				type = SWT.MouseExit;
			} else if (event.getEventType() == javafx.scene.input.MouseEvent.MOUSE_ENTERED) {
				type = SWT.MouseEnter;
			} else if (event.getEventType() == javafx.scene.input.MouseEvent.MOUSE_MOVED
					|| event.getEventType() == javafx.scene.input.MouseEvent.MOUSE_DRAGGED) {
				type = SWT.MouseMove;
			} else if (event.getEventType() == javafx.scene.input.MouseEvent.MOUSE_PRESSED) {
				type = SWT.MouseDown;
			} else if (event.getEventType() == javafx.scene.input.MouseEvent.MOUSE_RELEASED) {
				type = SWT.MouseUp;
			}

			if (type != SWT.None) {
				if (type == SWT.MouseEnter) {
					display.setHoverControl(Control.this);
				} else if (type == SWT.MouseExit) {
					display.setHoverControl(null);
				} else if (type == SWT.MouseMove) {
					display.setHoverControl(Control.this);
				}

				sendMouseEvent(type, event);

				if (type == SWT.MouseUp && event.getClickCount() > 1) {
					sendMouseEvent(SWT.MouseDoubleClick, event);
				}
			}
		}
	};

	/**
	 * Prevents uninitialized instances from being created outside the package.
	 */
	Control () {
	}

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
	 * @see SWT#BORDER
	 * @see SWT#LEFT_TO_RIGHT
	 * @see SWT#RIGHT_TO_LEFT
	 * @see Widget#checkSubclass
	 * @see Widget#getStyle
	 */
	public Control(Composite parent, int style) {
		super (parent, style);
		this.parent = parent;
		createWidget ();
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified
	 * when the control is moved or resized, by sending it one of the messages
	 * defined in the <code>ControlListener</code> interface.
	 * 
	 * @param listener
	 *            the listener which should be notified
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
	 * @see ControlListener
	 * @see #removeControlListener
	 */
	public void addControlListener(ControlListener listener) {
		checkWidget ();
		if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
		TypedListener typedListener = new TypedListener (listener);
		addListener (SWT.Resize,typedListener);
		addListener (SWT.Move,typedListener);
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified
	 * when a drag gesture occurs, by sending it one of the messages defined in
	 * the <code>DragDetectListener</code> interface.
	 * 
	 * @param listener
	 *            the listener which should be notified
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
	 * @see DragDetectListener
	 * @see #removeDragDetectListener
	 * 
	 * @since 3.3
	 */
	public void addDragDetectListener(DragDetectListener listener) {
		checkWidget ();
		if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
		TypedListener typedListener = new TypedListener (listener);
		addListener (SWT.DragDetect,typedListener);
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified
	 * when the control gains or loses focus, by sending it one of the messages
	 * defined in the <code>FocusListener</code> interface.
	 * 
	 * @param listener
	 *            the listener which should be notified
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
	 * @see FocusListener
	 * @see #removeFocusListener
	 */
	public void addFocusListener(FocusListener listener) {
		checkWidget ();
		if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
		TypedListener typedListener = new TypedListener (listener);
		addListener (SWT.FocusIn,typedListener);
		addListener (SWT.FocusOut,typedListener);
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified
	 * when gesture events are generated for the control, by sending it one of
	 * the messages defined in the <code>GestureListener</code> interface.
	 * <p>
	 * NOTE: If <code>setTouchEnabled(true)</code> has previously been invoked
	 * on the receiver then <code>setTouchEnabled(false)</code> must be invoked
	 * on it to specify that gesture events should be sent instead of touch
	 * events.
	 * </p>
	 * 
	 * @param listener
	 *            the listener which should be notified
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
	 * @see GestureListener
	 * @see #removeGestureListener
	 * @see #setTouchEnabled
	 * 
	 * @since 3.7
	 */
	public void addGestureListener(GestureListener listener) {
		checkWidget();
		if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
		TypedListener typedListener = new TypedListener (listener);
		addListener (SWT.Gesture, typedListener);
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified
	 * when help events are generated for the control, by sending it one of the
	 * messages defined in the <code>HelpListener</code> interface.
	 * 
	 * @param listener
	 *            the listener which should be notified
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
	 * @see HelpListener
	 * @see #removeHelpListener
	 */
	public void addHelpListener(HelpListener listener) {
		checkWidget ();
		if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
		TypedListener typedListener = new TypedListener (listener);
		addListener (SWT.Help, typedListener);
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified
	 * when keys are pressed and released on the system keyboard, by sending it
	 * one of the messages defined in the <code>KeyListener</code> interface.
	 * <p>
	 * When a key listener is added to a control, the control will take part in
	 * widget traversal. By default, all traversal keys (such as the tab key and
	 * so on) are delivered to the control. In order for a control to take part
	 * in traversal, it should listen for traversal events. Otherwise, the user
	 * can traverse into a control but not out. Note that native controls such
	 * as table and tree implement key traversal in the operating system. It is
	 * not necessary to add traversal listeners for these controls, unless you
	 * want to override the default traversal.
	 * </p>
	 * 
	 * @param listener
	 *            the listener which should be notified
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
	 * @see KeyListener
	 * @see #removeKeyListener
	 */
	public void addKeyListener(KeyListener listener) {
		checkWidget ();
		if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
		TypedListener typedListener = new TypedListener (listener);
		addListener (SWT.KeyUp,typedListener);
		addListener (SWT.KeyDown,typedListener);
	}

	@Override
	public void addListener(int eventType, Listener listener) {
		super.addListener(eventType, listener);
		if (eventType == SWT.Resize
				&& (state & RESIZE_ATTACHED) == 0) {
			InvalidationListener l = new InvalidationListener() {
				@Override
				public void invalidated(Observable observable) {
					Event evt = new Event();
					sendEvent(SWT.Resize, evt, true);
				}
			};
			nativeControl.widthProperty().addListener(l);
			nativeControl.heightProperty().addListener(l);
		}
	}
	
	/**
	 * Adds the listener to the collection of listeners who will be notified
	 * when the platform-specific context menu trigger has occurred, by sending
	 * it one of the messages defined in the <code>MenuDetectListener</code>
	 * interface.
	 * 
	 * @param listener
	 *            the listener which should be notified
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
	 * @see MenuDetectListener
	 * @see #removeMenuDetectListener
	 * 
	 * @since 3.3
	 */
	public void addMenuDetectListener(MenuDetectListener listener) {
		checkWidget ();
		if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
		TypedListener typedListener = new TypedListener (listener);
		addListener (SWT.MenuDetect, typedListener);
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified
	 * when mouse buttons are pressed and released, by sending it one of the
	 * messages defined in the <code>MouseListener</code> interface.
	 * 
	 * @param listener
	 *            the listener which should be notified
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
	 * @see MouseListener
	 * @see #removeMouseListener
	 */
	public void addMouseListener(MouseListener listener) {
		checkWidget ();
		if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
		TypedListener typedListener = new TypedListener (listener);
		addListener (SWT.MouseDown,typedListener);
		addListener (SWT.MouseUp,typedListener);
		addListener (SWT.MouseDoubleClick,typedListener);
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified
	 * when the mouse passes or hovers over controls, by sending it one of the
	 * messages defined in the <code>MouseTrackListener</code> interface.
	 * 
	 * @param listener
	 *            the listener which should be notified
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
	 * @see MouseTrackListener
	 * @see #removeMouseTrackListener
	 */
	public void addMouseTrackListener(MouseTrackListener listener) {
		checkWidget ();
		if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
		TypedListener typedListener = new TypedListener (listener);
		addListener (SWT.MouseEnter,typedListener);
		addListener (SWT.MouseExit,typedListener);
		addListener (SWT.MouseHover,typedListener);
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified
	 * when the mouse moves, by sending it one of the messages defined in the
	 * <code>MouseMoveListener</code> interface.
	 * 
	 * @param listener
	 *            the listener which should be notified
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
	 * @see MouseMoveListener
	 * @see #removeMouseMoveListener
	 */
	public void addMouseMoveListener(MouseMoveListener listener) {
		checkWidget ();
		if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
		TypedListener typedListener = new TypedListener (listener);
		addListener (SWT.MouseMove,typedListener);
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified
	 * when the mouse wheel is scrolled, by sending it one of the messages
	 * defined in the <code>MouseWheelListener</code> interface.
	 * 
	 * @param listener
	 *            the listener which should be notified
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
	 * @see MouseWheelListener
	 * @see #removeMouseWheelListener
	 * 
	 * @since 3.3
	 */
	public void addMouseWheelListener(MouseWheelListener listener) {
		checkWidget ();
		if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
		TypedListener typedListener = new TypedListener (listener);
		addListener (SWT.MouseWheel, typedListener);
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified
	 * when the receiver needs to be painted, by sending it one of the messages
	 * defined in the <code>PaintListener</code> interface.
	 * 
	 * @param listener
	 *            the listener which should be notified
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
	 * @see PaintListener
	 * @see #removePaintListener
	 */
	public void addPaintListener(PaintListener listener) {
		checkWidget ();
		if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
		TypedListener typedListener = new TypedListener (listener);
		addListener (SWT.Paint,typedListener);
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified
	 * when touch events occur, by sending it one of the messages defined in the
	 * <code>TouchListener</code> interface.
	 * <p>
	 * NOTE: You must also call <code>setTouchEnabled(true)</code> to specify
	 * that touch events should be sent, which will cause gesture events to not
	 * be sent.
	 * </p>
	 * 
	 * @param listener
	 *            the listener which should be notified
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
	 * @see TouchListener
	 * @see #removeTouchListener
	 * @see #setTouchEnabled
	 * 
	 * @since 3.7
	 */
	public void addTouchListener(TouchListener listener) {
		checkWidget();
		if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
		TypedListener typedListener = new TypedListener (listener);
		addListener (SWT.Touch,typedListener);
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified
	 * when traversal events occur, by sending it one of the messages defined in
	 * the <code>TraverseListener</code> interface.
	 * 
	 * @param listener
	 *            the listener which should be notified
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
	 * @see TraverseListener
	 * @see #removeTraverseListener
	 */
	public void addTraverseListener(TraverseListener listener) {
		checkWidget ();
		if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
		TypedListener typedListener = new TypedListener (listener);
		addListener (SWT.Traverse,typedListener);
	}

	String calculateStyleString() {
		StringBuffer b = new StringBuffer();
		if (font != null) {
			b.append(font.internal_getAsCSSString());
		}
		
		if (foreground != null) {
			String rgb = "rgb(" + foreground.getRed() + ","
					+ foreground.getGreen() + "," + foreground.getBlue() + ")";
			b.append("-fx-text-inner-color: " + rgb
					+ "; -fx-text-background-color: " + rgb + ";");
		}
		
		if( background != null ) {
			String rgb = "rgb(" + background.getRed() + ","
					+ background.getGreen() + "," + background.getBlue() + ")";
			b.append("-fx-background-color: " + rgb);
		}
		
		return b.toString();
	}

	void checkBackground () {
		Shell shell = getShell ();
		if (this == shell) return;
		state &= ~PARENT_BACKGROUND;
		Composite composite = parent;
		do {
			int mode = composite.backgroundMode;
			if (mode != 0) {
				if (mode == SWT.INHERIT_DEFAULT) {
					Control control = this;
					do {
						if ((control.state & THEME_BACKGROUND) == 0) {
							return;
						}
						control = control.parent;
					} while (control != composite);
				}
				state |= PARENT_BACKGROUND;
				return;
			}
			if (composite == shell) break;
			composite = composite.parent;
		} while (true);	
	}

	void checkBorder () {
		if (getBorderWidth () == 0) style &= ~SWT.BORDER;
	}

	void checkBuffered () {
		style &= ~SWT.DOUBLE_BUFFERED;
	}

	void checkComposited () {
		/* Do nothing */
	}

	void checkMirrored () {
		if ((style & SWT.RIGHT_TO_LEFT) != 0) {
			Util.logNotImplemented();
		}
	}

	/**
	 * Returns the preferred size of the receiver.
	 * <p>
	 * The <em>preferred size</em> of a control is the size that it would best
	 * be displayed at. The width hint and height hint arguments allow the
	 * caller to ask a control questions such as "Given a particular width, how
	 * high does the control need to be to show all of the contents?" To
	 * indicate that the caller does not wish to constrain a particular
	 * dimension, the constant <code>SWT.DEFAULT</code> is passed for the hint.
	 * </p>
	 * 
	 * @param wHint
	 *            the width hint (can be <code>SWT.DEFAULT</code>)
	 * @param hHint
	 *            the height hint (can be <code>SWT.DEFAULT</code>)
	 * @return the preferred size of the control
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
	 * @see #getBorderWidth
	 * @see #getBounds
	 * @see #getSize
	 * @see #pack(boolean)
	 * @see "computeTrim, getClientArea for controls that implement them"
	 */
	public Point computeSize(int wHint, int hHint) {
		return computeSize (wHint, hHint, true);
	}

	/**
	 * Returns the preferred size of the receiver.
	 * <p>
	 * The <em>preferred size</em> of a control is the size that it would best
	 * be displayed at. The width hint and height hint arguments allow the
	 * caller to ask a control questions such as "Given a particular width, how
	 * high does the control need to be to show all of the contents?" To
	 * indicate that the caller does not wish to constrain a particular
	 * dimension, the constant <code>SWT.DEFAULT</code> is passed for the hint.
	 * </p>
	 * <p>
	 * If the changed flag is <code>true</code>, it indicates that the
	 * receiver's <em>contents</em> have changed, therefore any caches that a
	 * layout manager containing the control may have been keeping need to be
	 * flushed. When the control is resized, the changed flag will be
	 * <code>false</code>, so layout manager caches can be retained.
	 * </p>
	 * 
	 * @param wHint
	 *            the width hint (can be <code>SWT.DEFAULT</code>)
	 * @param hHint
	 *            the height hint (can be <code>SWT.DEFAULT</code>)
	 * @param changed
	 *            <code>true</code> if the control's contents have changed, and
	 *            <code>false</code> otherwise
	 * @return the preferred size of the control.
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
	 * @see #getBorderWidth
	 * @see #getBounds
	 * @see #getSize
	 * @see #pack(boolean)
	 * @see "computeTrim, getClientArea for controls that implement them"
	 */
	public Point computeSize(int wHint, int hHint, boolean changed) {
		checkWidget();
		forceSizeProcessing();
		// TODO really? the win32 and cocoa port doesn't do this. gtk does I guess
		int width = (int) nativeControl.prefWidth(
				javafx.scene.control.Control.USE_COMPUTED_SIZE);
		int height = (int) nativeControl.prefHeight(
				javafx.scene.control.Control.USE_COMPUTED_SIZE);
		
		if (width <= 0) {
			width = DEFAULT_WIDTH;
		}

		if (height <= 0) {
			height = DEFAULT_HEIGHT;
		}

		if (wHint != SWT.DEFAULT)
			width = wHint;
		if (hHint != SWT.DEFAULT)
			height = hHint;
		int border = getBorderWidth();
		width += border * 2;
		height += border * 2;
		return new Point(width, height);
	}

	void createHandle() {
	}

	void createWidget () {
		state |= DRAG_DETECT;
		checkOrientation (parent);
		createHandle ();
		checkBackground ();
		checkBuffered ();
		checkComposited ();
		registerHandle ();
		checkMirrored ();
		checkBorder ();
		if ((state & PARENT_BACKGROUND) != 0) {
			Util.logNotImplemented();
		}
	}

	Font defaultFont() {
		return display.getSystemFont();
	}

	/**
	 * Detects a drag and drop gesture. This method is used to detect a drag
	 * gesture when called from within a mouse down listener.
	 * 
	 * <p>
	 * By default, a drag is detected when the gesture occurs anywhere within
	 * the client area of a control. Some controls, such as tables and trees,
	 * override this behavior. In addition to the operating system specific drag
	 * gesture, they require the mouse to be inside an item. Custom widget
	 * writers can use <code>setDragDetect</code> to disable the default
	 * detection, listen for mouse down, and then call <code>dragDetect()</code>
	 * from within the listener to conditionally detect a drag.
	 * </p>
	 * 
	 * @param event
	 *            the mouse down event
	 * 
	 * @return <code>true</code> if the gesture occurred, and <code>false</code>
	 *         otherwise.
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT if the event is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @see DragDetectListener
	 * @see #addDragDetectListener
	 * 
	 * @see #getDragDetect
	 * @see #setDragDetect
	 * 
	 * @since 3.3
	 */
	public boolean dragDetect(Event event) {
		Util.logNotImplemented();
		return false;
	}

	/**
	 * Detects a drag and drop gesture. This method is used to detect a drag
	 * gesture when called from within a mouse down listener.
	 * 
	 * <p>
	 * By default, a drag is detected when the gesture occurs anywhere within
	 * the client area of a control. Some controls, such as tables and trees,
	 * override this behavior. In addition to the operating system specific drag
	 * gesture, they require the mouse to be inside an item. Custom widget
	 * writers can use <code>setDragDetect</code> to disable the default
	 * detection, listen for mouse down, and then call <code>dragDetect()</code>
	 * from within the listener to conditionally detect a drag.
	 * </p>
	 * 
	 * @param event
	 *            the mouse down event
	 * 
	 * @return <code>true</code> if the gesture occurred, and <code>false</code>
	 *         otherwise.
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT if the event is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @see DragDetectListener
	 * @see #addDragDetectListener
	 * 
	 * @see #getDragDetect
	 * @see #setDragDetect
	 * 
	 * @since 3.3
	 */
	public boolean dragDetect(MouseEvent event) {
		Util.logNotImplemented();
		return false;
	}

	/**
	 * Forces the receiver to have the <em>keyboard focus</em>, causing all
	 * keyboard events to be delivered to it.
	 * 
	 * @return <code>true</code> if the control got focus, and
	 *         <code>false</code> if it was unable to.
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @see #setFocus
	 */
	public boolean forceFocus() {
		return setFocus();
	}

	void forceSizeProcessing() {
		if ((state & CSS_PROCESSED) == 0
				&& (nativeControl.getScene() == null
						|| nativeControl.getScene().getWindow() == null
						|| !nativeControl.getScene().getWindow().isShowing())) {
			state |= CSS_PROCESSED;
			nativeControl.impl_processCSS(true);
		}
	}
	
	/**
	 * Returns the accessible object for the receiver.
	 * <p>
	 * If this is the first time this object is requested, then the object is
	 * created and returned. The object returned by getAccessible() does not
	 * need to be disposed.
	 * </p>
	 * 
	 * @return the accessible object
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @see Accessible#addAccessibleListener
	 * @see Accessible#addAccessibleControlListener
	 * 
	 * @since 2.0
	 */
	public Accessible getAccessible() {
		// TODO
		return new Accessible(null);
	}

	/**
	 * Returns the receiver's background color.
	 * <p>
	 * Note: This operation is a hint and may be overridden by the platform. For
	 * example, on some versions of Windows the background of a TabFolder, is a
	 * gradient rather than a solid color.
	 * </p>
	 * 
	 * @return the background color
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public Color getBackground() {
		return this.background == null ? getDisplay().getSystemColor(
				SWT.COLOR_WIDGET_BACKGROUND) : background;
	}

	/**
	 * Returns the receiver's background image.
	 * 
	 * @return the background image
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @since 3.2
	 */
	public Image getBackgroundImage() {
		return backgroundImage;
	}

	/**
	 * Returns the receiver's border width.
	 * 
	 * @return the border width
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public int getBorderWidth() {
		// TODO
		return 0;
	}

	/**
	 * Returns a rectangle describing the receiver's size and location relative
	 * to its parent (or its display if its parent is null), unless the receiver
	 * is a shell. In this case, the location is relative to the display.
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
		Point location = getLocation();
		Point size = getSize();
		return new Rectangle(location.x, location.y, size.x, size.y);
	}

	/**
	 * Returns the receiver's cursor, or null if it has not been set.
	 * <p>
	 * When the mouse pointer passes over a control its appearance is changed to
	 * match the control's cursor.
	 * </p>
	 * 
	 * @return the receiver's cursor or <code>null</code>
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @since 3.3
	 */
	public Cursor getCursor() {
		return cursor;
	}

	protected Font getDefaultFont() {
		return getDisplay().getSystemFont();
	}

	/**
	 * Returns <code>true</code> if the receiver is detecting drag gestures, and
	 * <code>false</code> otherwise.
	 * 
	 * @return the receiver's drag detect state
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @since 3.3
	 */
	public boolean getDragDetect() {
		// TODO
		return false;
	}

	/**
	 * Returns <code>true</code> if the receiver is enabled, and
	 * <code>false</code> otherwise. A disabled control is typically not
	 * selectable from the user interface and draws with an inactive or "grayed"
	 * look.
	 * 
	 * @return the receiver's enabled state
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @see #isEnabled
	 */
	public boolean getEnabled() {
		checkWidget();
		return !nativeControl.isDisabled();
	}

	/**
	 * Returns the font that the receiver will use to paint textual information.
	 * 
	 * @return the receiver's font
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public Font getFont() {
		checkWidget();
		return font == null ? font = getDefaultFont() : font;
	}

	/**
	 * Returns the foreground color that the receiver will use to draw.
	 * 
	 * @return the receiver's foreground color
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public Color getForeground() {
		return this.foreground == null ? getDisplay().getSystemColor(
				SWT.COLOR_WIDGET_FOREGROUND) : foreground;
	}

	/**
	 * Returns layout data which is associated with the receiver.
	 * 
	 * @return the receiver's layout data
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public Object getLayoutData() {
		checkWidget();
		return layoutData;
	}

	/**
	 * Returns a point describing the receiver's location relative to its parent
	 * (or its display if its parent is null), unless the receiver is a shell.
	 * In this case, the point is relative to the display.
	 * 
	 * @return the receiver's location
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public Point getLocation() {
		checkWidget();
		return new Point((int) nativeControl.getLayoutX(),
				(int) nativeControl.getLayoutY());
	}

	/**
	 * Returns the receiver's pop up menu if it has one, or null if it does not.
	 * All controls may optionally have a pop up menu that is displayed when the
	 * user requests one for the control. The sequence of key strokes, button
	 * presses and/or button releases that are used to request a pop up menu is
	 * platform specific.
	 * 
	 * @return the receiver's menu
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public Menu getMenu() {
		checkWidget();
		return menu;
	}

	/**
	 * Returns the receiver's monitor.
	 * 
	 * @return the receiver's monitor
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @since 3.0
	 */
	public Monitor getMonitor() {
		Monitor[] monitors = getDisplay().getMonitors();
		if (monitors.length == 1) {
			return monitors[0];

		}
		Rectangle bounds = getBounds();
		if (!(this instanceof Shell)) {
			bounds = getDisplay().map(getParent(), null, bounds);
		}
		
		int index = -1, value = -1;
		for (int i = 0; i < monitors.length; i++) {
			Rectangle rect = bounds.intersection(monitors[i].getBounds());
			int area = rect.width * rect.height;
			if (area > 0 && area > value) {
				index = i;
				value = area;
			}
		}
		if (index >= 0)
			return monitors[index];
		int centerX = bounds.x + bounds.width / 2, centerY = bounds.y
				+ bounds.height / 2;
		for (int i = 0; i < monitors.length; i++) {
			Rectangle rect = monitors[i].getBounds();
			int x = centerX < rect.x ? rect.x - centerX : centerX > rect.x
					+ rect.width ? centerX - rect.x - rect.width : 0;
			int y = centerY < rect.y ? rect.y - centerY : centerY > rect.y
					+ rect.height ? centerY - rect.y - rect.height : 0;
			int distance = x * x + y * y;
			if (index == -1 || distance < value) {
				index = i;
				value = distance;
			}
		}
		return monitors[index];
	}

	/**
	 * Returns the orientation of the receiver, which will be one of the
	 * constants <code>SWT.LEFT_TO_RIGHT</code> or
	 * <code>SWT.RIGHT_TO_LEFT</code>.
	 * 
	 * @return the orientation style
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @since 3.7
	 */
	public int getOrientation() {
		if ((style & SWT.RIGHT_TO_LEFT) == SWT.RIGHT_TO_LEFT) {
			return SWT.RIGHT_TO_LEFT;
		}
		return SWT.LEFT_TO_RIGHT;
	}

	/**
	 * Returns the receiver's parent, which must be a <code>Composite</code> or
	 * null when the receiver is a shell that was created with null or a display
	 * for a parent.
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
	public Composite getParent() {
		checkWidget();
		return parent;
	}

	/**
	 * Returns the region that defines the shape of the control, or null if the
	 * control has the default shape.
	 * 
	 * @return the region that defines the shape of the shell (or null)
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
	public Region getRegion() {
		checkWidget();
		return region;
	}

	/**
	 * Returns the receiver's shell. For all controls other than shells, this
	 * simply returns the control's nearest ancestor shell. Shells return
	 * themselves, even if they are children of other shells.
	 * 
	 * @return the receiver's shell
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @see #getParent
	 */
	public Shell getShell() {
		checkWidget();
		return parent.getShell();
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
		forceSizeProcessing();
		return new Point((int) internal_getWidth(), (int) internal_getHeight());
	}

	/**
	 * Returns the text direction of the receiver, which will be one of the
	 * constants <code>SWT.LEFT_TO_RIGHT</code> or
	 * <code>SWT.RIGHT_TO_LEFT</code>.
	 * 
	 * @return the text direction style
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @since 3.102
	 */
	public int getTextDirection() {
		checkWidget ();
		/* return the widget orientation */
		return style & (SWT.LEFT_TO_RIGHT | SWT.RIGHT_TO_LEFT);
	}

	/**
	 * Returns the receiver's tool tip text, or null if it has not been set.
	 * 
	 * @return the receiver's tool tip text
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public String getToolTipText() {
		checkWidget();
		return tooltipText;
	}

	/**
	 * Returns <code>true</code> if this control is set to send touch events, or
	 * <code>false</code> if it is set to send gesture events instead. This
	 * method also returns <code>false</code> if a touch-based input device is
	 * not detected (this can be determined with
	 * <code>Display#getTouchEnabled()</code>). Use
	 * {@link #setTouchEnabled(boolean)} to switch the events that a control
	 * sends between touch events and gesture events.
	 * 
	 * @return <code>true</code> if the control is set to send touch events, or
	 *         <code>false</code> otherwise
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @see #setTouchEnabled
	 * @see Display#getTouchEnabled
	 * 
	 * @since 3.7
	 */
	public boolean getTouchEnabled() {
		checkWidget();
		return false;
	}

	/**
	 * Returns <code>true</code> if the receiver is visible, and
	 * <code>false</code> otherwise.
	 * <p>
	 * If one of the receiver's ancestors is not visible or some other condition
	 * makes the receiver not visible, this method may still indicate that it is
	 * considered visible even though it may not actually be showing.
	 * </p>
	 * 
	 * @return the receiver's visibility state
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public boolean getVisible() {
		checkWidget();
		return nativeControl.isVisible();
	}

	protected double internal_getHeight() {
		return nativeControl.getHeight();
	}

	protected double internal_getWidth() {
		return nativeControl.getWidth();
	}

	boolean internal_mouseAsFilter() {
		return false;
	}

	@Override
	public DrawableGC internal_new_GC() {
		return new MeasureGC(this);
	}
	
	@Override
	public void internal_dispose_GC(DrawableGC gc) {
		gc.dispose();
	}

	protected final void internal_reapplyStyle() {
		nativeControl.setStyle(calculateStyleString());
	}

	/**
	 * Returns <code>true</code> if the receiver is enabled and all ancestors up
	 * to and including the receiver's nearest ancestor shell are enabled.
	 * Otherwise, <code>false</code> is returned. A disabled control is
	 * typically not selectable from the user interface and draws with an
	 * inactive or "grayed" look.
	 * 
	 * @return the receiver's enabled state
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @see #getEnabled
	 */
	public boolean isEnabled() {
		checkWidget();
		return getEnabled() && parent.isEnabled();
	}

	/**
	 * Returns <code>true</code> if the receiver has the user-interface focus,
	 * and <code>false</code> otherwise.
	 * 
	 * @return the receiver's focus state
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public boolean isFocusControl() {
		return nativeControl.isFocused();
	}

	/**
	 * Returns <code>true</code> if the underlying operating system supports
	 * this reparenting, otherwise <code>false</code>
	 * 
	 * @return <code>true</code> if the widget can be reparented, otherwise
	 *         <code>false</code>
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public boolean isReparentable() {
		return true;
	}

	/**
	 * Returns <code>true</code> if the receiver is visible and all ancestors up
	 * to and including the receiver's nearest ancestor shell are visible.
	 * Otherwise, <code>false</code> is returned.
	 * 
	 * @return the receiver's visibility state
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @see #getVisible
	 */
	public boolean isVisible() {
		return getVisible() && parent.isVisible();
	}

	void markLayout(boolean changed, boolean all) {
		// Do nothing
	}
	
	Decorations menuShell () {
		return parent.menuShell ();
	}

	/**
	 * Moves the receiver above the specified control in the drawing order. If
	 * the argument is null, then the receiver is moved to the top of the
	 * drawing order. The control at the top of the drawing order will not be
	 * covered by other controls even if they occupy intersecting areas.
	 * 
	 * @param control
	 *            the sibling control (or null)
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_ARGUMENT - if the control has been
	 *                disposed</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @see Control#moveBelow
	 * @see Composite#getChildren
	 */
	public void moveAbove(Control control) {
		parent.moveChildAbove(this, control);
	}

	/**
	 * Moves the receiver below the specified control in the drawing order. If
	 * the argument is null, then the receiver is moved to the bottom of the
	 * drawing order. The control at the bottom of the drawing order will be
	 * covered by all other controls which occupy intersecting areas.
	 * 
	 * @param control
	 *            the sibling control (or null)
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_ARGUMENT - if the control has been
	 *                disposed</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @see Control#moveAbove
	 * @see Composite#getChildren
	 */
	public void moveBelow(Control control) {
		parent.moveChildBelow(this, control);
	}

	/**
	 * Causes the receiver to be resized to its preferred size. For a composite,
	 * this involves computing the preferred size from its layout, if there is
	 * one.
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @see #computeSize(int, int, boolean)
	 */
	public void pack() {
		forceSizeProcessing();
		// TODO is it min size??
		setSize((int) nativeControl.prefWidth(-1),
				(int) nativeControl.prefHeight(-1));
	}

	/**
	 * Causes the receiver to be resized to its preferred size. For a composite,
	 * this involves computing the preferred size from its layout, if there is
	 * one.
	 * <p>
	 * If the changed flag is <code>true</code>, it indicates that the
	 * receiver's <em>contents</em> have changed, therefore any caches that a
	 * layout manager containing the control may have been keeping need to be
	 * flushed. When the control is resized, the changed flag will be
	 * <code>false</code>, so layout manager caches can be retained.
	 * </p>
	 * 
	 * @param changed
	 *            whether or not the receiver's contents have changed
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @see #computeSize(int, int, boolean)
	 */
	public void pack(boolean changed) {
		forceSizeProcessing();
		// TODO is it min size??
		setSize((int) nativeControl.prefWidth(-1),
				(int) nativeControl.prefHeight(-1));	
	}

	/**
	 * Prints the receiver and all children.
	 * 
	 * @param gc
	 *            the gc where the drawing occurs
	 * @return <code>true</code> if the operation was successful and
	 *         <code>false</code> otherwise
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the gc is null</li>
	 *                <li>ERROR_INVALID_ARGUMENT - if the gc has been disposed</li>
	 *                </ul>
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
	public boolean print(GC gc) {
		// TODO
		return false;
	}

	/**
	 * Causes the entire bounds of the receiver to be marked as needing to be
	 * redrawn. The next time a paint request is processed, the control will be
	 * completely painted, including the background.
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @see #update()
	 * @see PaintListener
	 * @see SWT#Paint
	 * @see SWT#NO_BACKGROUND
	 * @see SWT#NO_REDRAW_RESIZE
	 * @see SWT#NO_MERGE_PAINTS
	 * @see SWT#DOUBLE_BUFFERED
	 */
	public void redraw() {
		// TODO
	}

	/**
	 * Causes the rectangular area of the receiver specified by the arguments to
	 * be marked as needing to be redrawn. The next time a paint request is
	 * processed, that area of the receiver will be painted, including the
	 * background. If the <code>all</code> flag is <code>true</code>, any
	 * children of the receiver which intersect with the specified area will
	 * also paint their intersecting areas. If the <code>all</code> flag is
	 * <code>false</code>, the children will not be painted.
	 * 
	 * @param x
	 *            the x coordinate of the area to draw
	 * @param y
	 *            the y coordinate of the area to draw
	 * @param width
	 *            the width of the area to draw
	 * @param height
	 *            the height of the area to draw
	 * @param all
	 *            <code>true</code> if children should redraw, and
	 *            <code>false</code> otherwise
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @see #update()
	 * @see PaintListener
	 * @see SWT#Paint
	 * @see SWT#NO_BACKGROUND
	 * @see SWT#NO_REDRAW_RESIZE
	 * @see SWT#NO_MERGE_PAINTS
	 * @see SWT#DOUBLE_BUFFERED
	 */
	public void redraw(int x, int y, int width, int height, boolean all) {
		// TODO
	}

	@Override
	void registerHandle() {
		super.registerHandle();
		
		nativeControl.setUserData(this);
		parent.addChild(this);

		if (internal_mouseAsFilter()) {
			nativeControl.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_ENTERED, mouseHandler);
			nativeControl.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_EXITED, mouseHandler);
			nativeControl.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_MOVED, mouseHandler);
		} else {
			nativeControl.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_ENTERED, mouseHandler);
			nativeControl.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_EXITED, mouseHandler);
			nativeControl.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_MOVED, mouseHandler);
		}

		nativeControl.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_PRESSED, mouseHandler);
		nativeControl.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_RELEASED, mouseHandler);
		nativeControl.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_DRAGGED, mouseHandler);

		// TODO Better do it on the scene???
		// n.focusedProperty().addListener(getFocusListener());
		// TODO Should we do it lazy??
		nativeControl.addEventHandler(javafx.scene.input.KeyEvent.KEY_RELEASED, keyEventHandler);
		nativeControl.addEventHandler(javafx.scene.input.KeyEvent.KEY_TYPED, keyEventHandler);
		nativeControl.addEventHandler(javafx.scene.input.KeyEvent.KEY_PRESSED, keyEventHandler);

		nativeControl.addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED, contextMenuHandler);
	}

	void releaseHandle () {
		super.releaseHandle ();
		nativeControl = null;
	}

	void releaseParent () {
		parent.removeChild(this);
		parent = null;
	}

	void releaseWidget () {
		super.releaseWidget ();
		if (menu != null && !menu.isDisposed ()) {
			menu.dispose ();
		}
		menu = null;
		cursor = null;
		layoutData = null;
		region = null;
		font = null;
	}

	/**
	 * Removes the listener from the collection of listeners who will be
	 * notified when the control is moved or resized.
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
	 * @see ControlListener
	 * @see #addControlListener
	 */
	public void removeControlListener(ControlListener listener) {
		checkWidget();
		if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
		if (eventTable == null) return;
		eventTable.unhook (SWT.Move, listener);
		eventTable.unhook (SWT.Resize, listener);
	}

	/**
	 * Removes the listener from the collection of listeners who will be
	 * notified when a drag gesture occurs.
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
	 * @see DragDetectListener
	 * @see #addDragDetectListener
	 * 
	 * @since 3.3
	 */
	public void removeDragDetectListener(DragDetectListener listener) {
		checkWidget ();
		if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
		if (eventTable == null) return;
		eventTable.unhook (SWT.DragDetect, listener);
	}

	/**
	 * Removes the listener from the collection of listeners who will be
	 * notified when the control gains or loses focus.
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
	 * @see FocusListener
	 * @see #addFocusListener
	 */
	public void removeFocusListener(FocusListener listener) {
		checkWidget();
		if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
		if (eventTable == null) return;
		eventTable.unhook (SWT.FocusIn, listener);
		eventTable.unhook (SWT.FocusOut, listener);
	}

	/**
	 * Removes the listener from the collection of listeners who will be
	 * notified when gesture events are generated for the control.
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
	 * @see GestureListener
	 * @see #addGestureListener
	 * 
	 * @since 3.7
	 */
	public void removeGestureListener(GestureListener listener) {
		checkWidget();
		if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
		if (eventTable == null) return;
		eventTable.unhook(SWT.Gesture, listener);
	}

	/**
	 * Removes the listener from the collection of listeners who will be
	 * notified when the help events are generated for the control.
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
	 * @see HelpListener
	 * @see #addHelpListener
	 */
	public void removeHelpListener(HelpListener listener) {
		checkWidget();
		if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
		if (eventTable == null) return;
		eventTable.unhook (SWT.Help, listener);
	}

	/**
	 * Removes the listener from the collection of listeners who will be
	 * notified when keys are pressed and released on the system keyboard.
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
	 * @see KeyListener
	 * @see #addKeyListener
	 */
	public void removeKeyListener(KeyListener listener) {
		checkWidget();
		if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
		if (eventTable == null) return;
		eventTable.unhook (SWT.KeyUp, listener);
		eventTable.unhook (SWT.KeyDown, listener);
	}

	/**
	 * Removes the listener from the collection of listeners who will be
	 * notified when the platform-specific context menu trigger has occurred.
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
	 * @see MenuDetectListener
	 * @see #addMenuDetectListener
	 * 
	 * @since 3.3
	 */
	public void removeMenuDetectListener(MenuDetectListener listener) {
		checkWidget ();
		if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
		if (eventTable == null) return;
		eventTable.unhook (SWT.MenuDetect, listener);
	}

	/**
	 * Removes the listener from the collection of listeners who will be
	 * notified when mouse buttons are pressed and released.
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
	 * @see MouseListener
	 * @see #addMouseListener
	 */
	public void removeMouseListener(MouseListener listener) {
		checkWidget();
		if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
		if (eventTable == null) return;
		eventTable.unhook (SWT.MouseDown, listener);
		eventTable.unhook (SWT.MouseUp, listener);
		eventTable.unhook (SWT.MouseDoubleClick, listener);
	}

	/**
	 * Removes the listener from the collection of listeners who will be
	 * notified when the mouse moves.
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
	 * @see MouseMoveListener
	 * @see #addMouseMoveListener
	 */
	public void removeMouseMoveListener(MouseMoveListener listener) {
		checkWidget();
		if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
		if (eventTable == null) return;
		eventTable.unhook (SWT.MouseMove, listener);
	}

	/**
	 * Removes the listener from the collection of listeners who will be
	 * notified when the mouse passes or hovers over controls.
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
	 * @see MouseTrackListener
	 * @see #addMouseTrackListener
	 */
	public void removeMouseTrackListener(MouseTrackListener listener) {
		checkWidget();
		if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
		if (eventTable == null) return;
		eventTable.unhook (SWT.MouseEnter, listener);
		eventTable.unhook (SWT.MouseExit, listener);
		eventTable.unhook (SWT.MouseHover, listener);
	}

	/**
	 * Removes the listener from the collection of listeners who will be
	 * notified when the mouse wheel is scrolled.
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
	 * @see MouseWheelListener
	 * @see #addMouseWheelListener
	 * 
	 * @since 3.3
	 */
	public void removeMouseWheelListener(MouseWheelListener listener) {
		checkWidget ();
		if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
		if (eventTable == null) return;
		eventTable.unhook (SWT.MouseWheel, listener);
	}

	/**
	 * Removes the listener from the collection of listeners who will be
	 * notified when the receiver needs to be painted.
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
	 * @see PaintListener
	 * @see #addPaintListener
	 */
	public void removePaintListener(PaintListener listener) {
		checkWidget();
		if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
		if (eventTable == null) return;
		eventTable.unhook(SWT.Paint, listener);
	}

	/**
	 * Removes the listener from the collection of listeners who will be
	 * notified when touch events occur.
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
	 * @see TouchListener
	 * @see #addTouchListener
	 * 
	 * @since 3.7
	 */
	public void removeTouchListener(TouchListener listener) {
		checkWidget();
		if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
		if (eventTable == null) return;
		eventTable.unhook (SWT.Touch, listener);
	}

	/**
	 * Removes the listener from the collection of listeners who will be
	 * notified when traversal events occur.
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
	 * @see TraverseListener
	 * @see #addTraverseListener
	 */
	public void removeTraverseListener(TraverseListener listener) {
		checkWidget ();
		if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
		if (eventTable == null) return;
		eventTable.unhook (SWT.Traverse, listener);
	}

	void sendKeyEvent(javafx.scene.input.KeyEvent event) {
		Event evt = new Event();

		if (event.getEventType() == javafx.scene.input.KeyEvent.KEY_TYPED) {
			if (event.isAltDown())
				evt.stateMask |= SWT.ALT;
			if (event.isShiftDown())
				evt.stateMask |= SWT.SHIFT;
			if (event.isControlDown())
				evt.stateMask |= SWT.CONTROL;
			if (event.isMetaDown())
				evt.stateMask |= SWT.COMMAND;

			// FIXME Alt/Control modifiers don't yet work
			evt.keyCode = lastLetterDown != null ? lastLetterDown.keyCode : 0;
			evt.character = event.getCharacter() != javafx.scene.input.KeyEvent.CHAR_UNDEFINED
					&& event.getCharacter().length() > 0 ? event.getCharacter()
					.charAt(0) : (char) lastLetterDown.keyCode;
			sendEvent(SWT.KeyDown, evt, true);
			if (!evt.doit) {
				event.consume();
				lastTypedDown = null;
			} else {
				lastTypedDown = evt;
			}
		} else {
			evt.keyCode = translateKey(event.getCode());

			switch (evt.keyCode) {
			case SWT.LF:
				evt.keyCode = SWT.KEYPAD_CR;
				evt.character = '\r';
				break;
			case SWT.BS:
				evt.character = '\b';
				break;
			case SWT.CR:
				evt.character = '\r';
				break;
			case SWT.DEL:
				evt.character = 0x7F;
				break;
			case SWT.ESC:
				evt.character = 0x1B;
				break;
			case SWT.TAB:
				evt.character = '\t';
				break;
			}

			if (event.getEventType() == javafx.scene.input.KeyEvent.KEY_RELEASED) {
				if (lastTypedDown != null) {
					sendEvent(SWT.KeyUp, lastTypedDown, true);
					lastTypedDown = null;
					lastLetterDown = null;
				}
			}

			if (evt.keyCode != 0) {
				int type;
				if (event.getEventType() == javafx.scene.input.KeyEvent.KEY_RELEASED) {
					type = SWT.KeyUp;

					if (event.isAltDown() || event.getCode() == KeyCode.ALT)
						evt.stateMask |= SWT.ALT;
					if (event.isShiftDown() || event.getCode() == KeyCode.SHIFT)
						evt.stateMask |= SWT.SHIFT;
					if (event.isControlDown()
							|| event.getCode() == KeyCode.CONTROL)
						evt.stateMask |= SWT.CONTROL;
					if (event.isMetaDown() || event.getCode() == KeyCode.META)
						evt.stateMask |= SWT.COMMAND;

				} else {
					type = SWT.KeyDown;

					if (event.isAltDown() && event.getCode() != KeyCode.ALT)
						evt.stateMask |= SWT.ALT;
					if (event.isShiftDown() && event.getCode() != KeyCode.SHIFT)
						evt.stateMask |= SWT.SHIFT;
					if (event.isControlDown()
							&& event.getCode() != KeyCode.CONTROL)
						evt.stateMask |= SWT.CONTROL;
					if (event.isMetaDown() && event.getCode() != KeyCode.META)
						evt.stateMask |= SWT.COMMAND;
				}

				sendEvent(type, evt, true);
				if (!evt.doit) {
					event.consume();
					return;
				}

				if (event.getEventType() == javafx.scene.input.KeyEvent.KEY_PRESSED) {
					Event tEvt = new Event();
					switch (event.getCode()) {
					case RIGHT:
						tEvt.detail = SWT.TRAVERSE_ARROW_NEXT;
						break;
					case LEFT:
						tEvt.detail = SWT.TRAVERSE_ARROW_PREVIOUS;
						break;
					case ESCAPE:
						tEvt.detail = SWT.TRAVERSE_ESCAPE;
						break;
					case PAGE_DOWN:
						tEvt.detail = SWT.TRAVERSE_PAGE_NEXT;
						break;
					case PAGE_UP:
						tEvt.detail = SWT.TRAVERSE_PAGE_PREVIOUS;
						break;
					case ENTER:
						tEvt.detail = SWT.TRAVERSE_RETURN;
						break;
					case TAB:
						if (event.isShiftDown()) {
							tEvt.detail = SWT.TRAVERSE_TAB_PREVIOUS;
						} else {
							tEvt.detail = SWT.TRAVERSE_TAB_NEXT;
						}
						break;
					default:
						if (event.isAltDown() && event.getCode() != KeyCode.ALT) {
							tEvt.detail = SWT.TRAVERSE_MNEMONIC;
						}

						break;
					}
					if (tEvt.detail != 0) {
						sendEvent(SWT.Traverse, tEvt, true);
						if (!tEvt.doit) {
							event.consume();
						}
					}
				}

			} else if (event.getCode().isLetterKey()) {
				evt.keyCode = Character.toLowerCase(event.getCode()
						.impl_getCode());
				lastLetterDown = evt;
			}
		}
	}

	void sendMouseEvent(int type, javafx.scene.input.MouseEvent event) {
		if (type == SWT.MouseEnter && (state & MOUSE_EXIT) != MOUSE_EXIT) {
			return;
		}
		if (type == SWT.MouseExit && (state & MOUSE_ENTER) != MOUSE_ENTER) {
			return;
		}

		Event evt = new Event();
		evt.type = type;
		switch (type) {
		case SWT.MouseDown:
		case SWT.MouseUp:
		case SWT.MouseDoubleClick:
		case SWT.MouseMove:
			switch (event.getButton()) {
			case PRIMARY:
				evt.button = 1;
				break;
			case SECONDARY:
				evt.button = 2;
				break;
			case MIDDLE:
				evt.button = 3;
				break;
			case NONE:
				evt.button = 4;
				break;
			default:
				break;
			}
			break;
		default:
			break;
		}

		if (evt.button != 0) {
			evt.count = event.getClickCount();
		}

		evt.x = (int) event.getX();
		evt.y = (int) event.getY();

		updateStateMask(evt, event);

		sendEvent(type, evt, true);

		if (type == SWT.MouseExit) {
			state &= ~MOUSE_ENTER;
			state |= MOUSE_EXIT;
			if (getParent() != null && event.getSceneX() > 0
					&& event.getSceneY() > 0) {
				Composite p = getParent();
				Point2D p2 = getParent().nativeControl
						.sceneToLocal(event.getSceneX(), event.getSceneY());
				while (p != null) {
					if (p.nativeControl.contains(p2)) {
						break;
					}
					p = p.getParent();
				}

				if (p != null) {
					Event e2 = new Event();
					e2.type = SWT.MouseEnter;
					e2.x = (int) p2.getX();
					e2.y = (int) p2.getY();
					e2.stateMask = evt.stateMask;
					p.state |= MOUSE_ENTER;
					p.state &= ~MOUSE_EXIT;
					p.sendEvent(SWT.MouseEnter, e2, true);
				}
				LAST_ENTER = p;
			} else {
				LAST_ENTER = null;
			}
		} else if (type == SWT.MouseEnter) {
			state |= MOUSE_ENTER;
			state &= ~MOUSE_EXIT;
			if (LAST_ENTER != null) {
				Point2D p2 = LAST_ENTER.nativeControl
						.sceneToLocal(event.getSceneX(), event.getSceneY());
				if (LAST_ENTER.nativeControl.contains(p2)) {
					Event e2 = new Event();
					e2.type = SWT.MouseExit;
					e2.x = (int) p2.getX();
					e2.y = (int) p2.getY();
					e2.stateMask = evt.stateMask;
					LAST_ENTER.state &= ~MOUSE_ENTER;
					LAST_ENTER.state |= MOUSE_EXIT;
					LAST_ENTER.sendEvent(SWT.MouseExit, e2, true);
				}
			}
			LAST_ENTER = this;
		} else if (type == SWT.MouseMove) {
			// FIXME We need to emulate hover
		}
	}
	
	/**
	 * Sets the receiver's background color to the color specified by the
	 * argument, or to the default system color for the control if the argument
	 * is null.
	 * <p>
	 * Note: This operation is a hint and may be overridden by the platform. For
	 * example, on Windows the background of a Button cannot be changed.
	 * </p>
	 * 
	 * @param color
	 *            the new color (or null)
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_ARGUMENT - if the argument has been
	 *                disposed</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setBackground(Color color) {
		this.background = color;
		internal_reapplyStyle();
//		if (color != null) {
////			internal_getNativeObject().setBackground(
////					new Background(new BackgroundFill(color
////							.internal_getNativeObject(), CornerRadii.EMPTY,
////							Insets.EMPTY)));
//		} else {
//			internal_getNativeObject().setBackground(null);
//		}
	}

	/**
	 * Sets the receiver's background image to the image specified by the
	 * argument, or to the default system color for the control if the argument
	 * is null. The background image is tiled to fill the available space.
	 * <p>
	 * Note: This operation is a hint and may be overridden by the platform. For
	 * example, on Windows the background of a Button cannot be changed.
	 * </p>
	 * 
	 * @param image
	 *            the new image (or null)
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_ARGUMENT - if the argument has been
	 *                disposed</li>
	 *                <li>ERROR_INVALID_ARGUMENT - if the argument is not a
	 *                bitmap</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @since 3.2
	 */
	public void setBackgroundImage(Image image) {
		backgroundImage = image;
		// TODO show it
	}

	/**
	 * Sets the receiver's size and location to the rectangular area specified
	 * by the argument. The <code>x</code> and <code>y</code> fields of the
	 * rectangle are relative to the receiver's parent (or its display if its
	 * parent is null).
	 * <p>
	 * Note: Attempting to set the width or height of the receiver to a negative
	 * number will cause that value to be set to zero instead.
	 * </p>
	 * 
	 * @param rect
	 *            the new bounds for the receiver
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setBounds(Rectangle rect) {
		setBounds(rect.x, rect.y, rect.width, rect.height);
	}

	/**
	 * Sets the receiver's size and location to the rectangular area specified
	 * by the arguments. The <code>x</code> and <code>y</code> arguments are
	 * relative to the receiver's parent (or its display if its parent is null),
	 * unless the receiver is a shell. In this case, the <code>x</code> and
	 * <code>y</code> arguments are relative to the display.
	 * <p>
	 * Note: Attempting to set the width or height of the receiver to a negative
	 * number will cause that value to be set to zero instead.
	 * </p>
	 * 
	 * @param x
	 *            the new x coordinate for the receiver
	 * @param y
	 *            the new y coordinate for the receiver
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
	public void setBounds(int x, int y, int width, int height) {
		checkWidget();
		nativeControl.resizeRelocate(x, y, width, height);
	}

	/**
	 * If the argument is <code>true</code>, causes the receiver to have all
	 * mouse events delivered to it until the method is called with
	 * <code>false</code> as the argument. Note that on some platforms, a mouse
	 * button must currently be down for capture to be assigned.
	 * 
	 * @param capture
	 *            <code>true</code> to capture the mouse, and <code>false</code>
	 *            to release it
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setCapture(boolean capture) {
		Util.logNotImplemented();
	}

	/**
	 * Sets the receiver's cursor to the cursor specified by the argument, or to
	 * the default cursor for that kind of control if the argument is null.
	 * <p>
	 * When the mouse pointer passes over a control its appearance is changed to
	 * match the control's cursor.
	 * </p>
	 * 
	 * @param cursor
	 *            the new cursor (or null)
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_ARGUMENT - if the argument has been
	 *                disposed</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setCursor(Cursor cursor) {
		this.cursor = cursor;
		nativeControl.setCursor(
				cursor == null ? null : cursor.internal_getNativeObject());
	}

	/**
	 * Sets the receiver's drag detect state. If the argument is
	 * <code>true</code>, the receiver will detect drag gestures, otherwise
	 * these gestures will be ignored.
	 * 
	 * @param dragDetect
	 *            the new drag detect state
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @since 3.3
	 */
	public void setDragDetect(boolean dragDetect) {
		Util.logNotImplemented();
	}

	/**
	 * Enables the receiver if the argument is <code>true</code>, and disables
	 * it otherwise. A disabled control is typically not selectable from the
	 * user interface and draws with an inactive or "grayed" look.
	 * 
	 * @param enabled
	 *            the new enabled state
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setEnabled(boolean enabled) {
		checkWidget();
		nativeControl.setDisable(!enabled);
	}

	/**
	 * Causes the receiver to have the <em>keyboard focus</em>, such that all
	 * keyboard events will be delivered to it. Focus reassignment will respect
	 * applicable platform constraints.
	 * 
	 * @return <code>true</code> if the control got focus, and
	 *         <code>false</code> if it was unable to.
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @see #forceFocus
	 */
	public boolean setFocus() {
		checkWidget();
		nativeControl.requestFocus();
		return nativeControl.isFocused();
	}

	/**
	 * Sets the font that the receiver will use to paint textual information to
	 * the font specified by the argument, or to the default font for that kind
	 * of control if the argument is null.
	 * 
	 * @param font
	 *            the new font (or null)
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_ARGUMENT - if the argument has been
	 *                disposed</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setFont(Font font) {
		checkWidget();
		this.font = font;
		internal_reapplyStyle();
	}

	/**
	 * Sets the receiver's foreground color to the color specified by the
	 * argument, or to the default system color for the control if the argument
	 * is null.
	 * <p>
	 * Note: This operation is a hint and may be overridden by the platform.
	 * </p>
	 * 
	 * @param color
	 *            the new color (or null)
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_ARGUMENT - if the argument has been
	 *                disposed</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setForeground(Color color) {
		checkWidget();
		this.foreground = color;
		internal_reapplyStyle();
	}

	/**
	 * Sets the layout data associated with the receiver to the argument.
	 * 
	 * @param layoutData
	 *            the new layout data for the receiver.
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setLayoutData(Object layoutData) {
		checkWidget();
		this.layoutData = layoutData;
	}

	/**
	 * Sets the receiver's location to the point specified by the arguments
	 * which are relative to the receiver's parent (or its display if its parent
	 * is null), unless the receiver is a shell. In this case, the point is
	 * relative to the display.
	 * 
	 * @param location
	 *            the new location for the receiver
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setLocation(Point location) {
		setLocation(location.x, location.y);
	}

	/**
	 * Sets the receiver's location to the point specified by the arguments
	 * which are relative to the receiver's parent (or its display if its parent
	 * is null), unless the receiver is a shell. In this case, the point is
	 * relative to the display.
	 * 
	 * @param x
	 *            the new x coordinate for the receiver
	 * @param y
	 *            the new y coordinate for the receiver
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setLocation(int x, int y) {
		checkWidget();
		nativeControl.relocate(x, y);
	}

	/**
	 * Sets the receiver's pop up menu to the argument. All controls may
	 * optionally have a pop up menu that is displayed when the user requests
	 * one for the control. The sequence of key strokes, button presses and/or
	 * button releases that are used to request a pop up menu is platform
	 * specific.
	 * <p>
	 * Note: Disposing of a control that has a pop up menu will dispose of the
	 * menu. To avoid this behavior, set the menu to null before the control is
	 * disposed.
	 * </p>
	 * 
	 * @param menu
	 *            the new pop up menu
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_MENU_NOT_POP_UP - the menu is not a pop up menu</li>
	 *                <li>ERROR_INVALID_PARENT - if the menu is not in the same
	 *                widget tree</li>
	 *                <li>ERROR_INVALID_ARGUMENT - if the menu has been disposed
	 *                </li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setMenu(Menu menu) {
		this.menu = menu;
		if (nativeControl instanceof javafx.scene.control.Control) {
			javafx.scene.control.Control c = (javafx.scene.control.Control) nativeControl;
			c.setContextMenu(menu == null ? null : menu.contextMenu);
		}
	}

	/**
	 * Sets the orientation of the receiver, which must be one of the constants
	 * <code>SWT.LEFT_TO_RIGHT</code> or <code>SWT.RIGHT_TO_LEFT</code>.
	 * <p>
	 * 
	 * @param orientation
	 *            new orientation style
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li> <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                called from the thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @since 3.7
	 */
	public void setOrientation(int orientation) {
		Util.logNotImplemented();
	}

	/**
	 * Changes the parent of the widget to be the one provided if the underlying
	 * operating system supports this feature. Returns <code>true</code> if the
	 * parent is successfully changed.
	 * 
	 * @param parent
	 *            the new parent for the control.
	 * @return <code>true</code> if the parent is changed and <code>false</code>
	 *         otherwise.
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_ARGUMENT - if the argument has been
	 *                disposed</li>
	 *                <li>ERROR_NULL_ARGUMENT - if the parent is
	 *                <code>null</code></li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public boolean setParent(Composite parent) {
		this.parent.removeChild(this);
		parent.addChild(this);
		this.parent = parent;
		return true;
	}

	/**
	 * If the argument is <code>false</code>, causes subsequent drawing
	 * operations in the receiver to be ignored. No drawing of any kind can
	 * occur in the receiver until the flag is set to true. Graphics operations
	 * that occurred while the flag was <code>false</code> are lost. When the
	 * flag is set to <code>true</code>, the entire widget is marked as needing
	 * to be redrawn. Nested calls to this method are stacked.
	 * <p>
	 * Note: This operation is a hint and may not be supported on some platforms
	 * or for some widgets.
	 * </p>
	 * 
	 * @param redraw
	 *            the new redraw state
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @see #redraw(int, int, int, int, boolean)
	 * @see #update()
	 */
	public void setRedraw(boolean redraw) {
		// Not needed in FX
	}

	/**
	 * Sets the shape of the control to the region specified by the argument.
	 * When the argument is null, the default shape of the control is restored.
	 * 
	 * @param region
	 *            the region that defines the shape of the control (or null)
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_ARGUMENT - if the region has been
	 *                disposed</li>
	 *                </ul>
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
	public void setRegion(Region region) {
		this.region = region;
		// TODO
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
		setSize(size.x, size.y);
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
		nativeControl.resize(width, height);
	}

	/**
	 * Sets the base text direction (a.k.a. "paragraph direction") of the
	 * receiver, which must be one of the constants
	 * <code>SWT.LEFT_TO_RIGHT</code> or <code>SWT.RIGHT_TO_LEFT</code>.
	 * <p>
	 * <code>setOrientation</code> would override this value with the text
	 * direction that is consistent with the new orientation.
	 * </p>
	 * <p>
	 * <b>Warning</b>: This API is currently only implemented on Windows. It
	 * doesn't set the base text direction on GTK and Cocoa.
	 * </p>
	 * 
	 * @param textDirection
	 *            the base text direction style
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @see SWT#FLIP_TEXT_DIRECTION
	 * 
	 * @since 3.102
	 */
	public void setTextDirection(int textDirection) {
		Util.logNotImplemented();
	}

	/**
	 * Sets the receiver's tool tip text to the argument, which may be null
	 * indicating that the default tool tip for the control will be shown. For a
	 * control that has a default tool tip, such as the Tree control on Windows,
	 * setting the tool tip text to an empty string replaces the default,
	 * causing no tool tip text to be shown.
	 * <p>
	 * The mnemonic indicator (character '&amp;') is not displayed in a tool
	 * tip. To display a single '&amp;' in the tool tip, the character '&amp;'
	 * can be escaped by doubling it in the string.
	 * </p>
	 * 
	 * @param string
	 *            the new tool tip text (or null)
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setToolTipText(String string) {
		this.tooltipText = string;
		if (nativeControl instanceof javafx.scene.control.Control) {
			javafx.scene.control.Control control = (javafx.scene.control.Control) nativeControl;
			if (string == null || string.length() == 0) {
				tooltip = null;
			} else {
				if (tooltip == null) {
					tooltip = new Tooltip();
				}
				tooltip.setText(string);
			}

			control.setTooltip(tooltip);
		}
	}

	/**
	 * Sets whether this control should send touch events (by default controls
	 * do not). Setting this to <code>false</code> causes the receiver to send
	 * gesture events instead. No exception is thrown if a touch-based input
	 * device is not detected (this can be determined with
	 * <code>Display#getTouchEnabled()</code>).
	 * 
	 * @param enabled
	 *            the new touch-enabled state
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 * 
	 * @see Display#getTouchEnabled
	 * 
	 * @since 3.7
	 */
	public void setTouchEnabled(boolean enabled) {
		// TODO
	}

	/**
	 * Marks the receiver as visible if the argument is <code>true</code>, and
	 * marks it invisible otherwise.
	 * <p>
	 * If one of the receiver's ancestors is not visible or some other condition
	 * makes the receiver not visible, marking it visible may not actually cause
	 * it to be displayed.
	 * </p>
	 * 
	 * @param visible
	 *            the new visibility state
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setVisible(boolean visible) {
		checkWidget();
		nativeControl.setVisible(visible);
	}

	/**
	 * Returns a point which is the result of converting the argument, which is
	 * specified in display relative coordinates, to coordinates relative to the
	 * receiver.
	 * <p>
	 * 
	 * @param x
	 *            the x coordinate to be translated
	 * @param y
	 *            the y coordinate to be translated
	 * @return the translated coordinates
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li> <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                called from the thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @since 2.1
	 */
	public Point toControl(int x, int y) {
		return getDisplay().map(null, this, x, y);
	}

	/**
	 * Returns a point which is the result of converting the argument, which is
	 * specified in display relative coordinates, to coordinates relative to the
	 * receiver.
	 * <p>
	 * 
	 * @param point
	 *            the point to be translated (must not be null)
	 * @return the translated coordinates
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the point is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li> <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public Point toControl(Point point) {
		return toControl(point.x, point.y);
	}

	/**
	 * Returns a point which is the result of converting the argument, which is
	 * specified in coordinates relative to the receiver, to display relative
	 * coordinates.
	 * <p>
	 * 
	 * @param x
	 *            the x coordinate to be translated
	 * @param y
	 *            the y coordinate to be translated
	 * @return the translated coordinates
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li> <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                called from the thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @since 2.1
	 */
	public Point toDisplay(int x, int y) {
		return getDisplay().map(this, null, x, y);
	}

	/**
	 * Returns a point which is the result of converting the argument, which is
	 * specified in coordinates relative to the receiver, to display relative
	 * coordinates.
	 * <p>
	 * 
	 * @param point
	 *            the point to be translated (must not be null)
	 * @return the translated coordinates
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the point is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li> <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public Point toDisplay(Point point) {
		return toDisplay(point.x, point.y);
	}

	private static int translateKey(KeyCode keyCode) {
		switch (keyCode) {
		case ALT:
			return SWT.ALT;
		case SHIFT:
			return SWT.SHIFT;
		case CONTROL:
			return SWT.CONTROL;
		case COMMAND:
			return SWT.COMMAND;
		case UP:
			return SWT.ARROW_UP;
		case DOWN:
			return SWT.ARROW_DOWN;
		case LEFT:
			return SWT.ARROW_LEFT;
		case RIGHT:
			return SWT.ARROW_RIGHT;
		case PAGE_UP:
			return SWT.PAGE_UP;
		case PAGE_DOWN:
			return SWT.PAGE_DOWN;
		case HOME:
			return SWT.HOME;
		case END:
			return SWT.END;
		case BACK_SPACE:
			return SWT.BS;
		case DELETE:
			return SWT.DEL;
		case ESCAPE:
			return SWT.ESC;
		case TAB:
			return SWT.TAB;
			// case ...:
			// return SWT.CR;
			// case ...:
			// return SWT.LF;
		case F1:
			return SWT.F1;
		case F2:
			return SWT.F2;
		case F3:
			return SWT.F3;
		case F4:
			return SWT.F4;
		case F5:
			return SWT.F5;
		case F6:
			return SWT.F6;
		case F7:
			return SWT.F7;
		case F8:
			return SWT.F8;
		case F9:
			return SWT.F9;
		case F10:
			return SWT.F10;
		case F11:
			return SWT.F11;
		case F12:
			return SWT.F12;
		case F13:
			return SWT.F13;
		case F14:
			return SWT.F14;
		case F15:
			return SWT.F15;
		case F16:
			return SWT.F16;
		case F17:
			return SWT.F17;
		case F18:
			return SWT.F18;
		case F19:
			return SWT.F19;
		case F20:
			return SWT.F20;
		case MULTIPLY:
			return SWT.KEYPAD_MULTIPLY;
		case ADD:
			return SWT.KEYPAD_ADD;
		case ENTER:
			return SWT.KEYPAD_CR;
		case SUBTRACT:
			return SWT.KEYPAD_SUBTRACT;
		case DECIMAL:
			return SWT.KEYPAD_DECIMAL;
		case DIVIDE:
			return SWT.KEYPAD_DIVIDE;
		case NUMPAD0:
			return SWT.KEYPAD_0;
		case NUMPAD1:
			return SWT.KEYPAD_1;
		case NUMPAD2:
			return SWT.KEYPAD_2;
		case NUMPAD3:
			return SWT.KEYPAD_3;
		case NUMPAD4:
			return SWT.KEYPAD_4;
		case NUMPAD5:
			return SWT.KEYPAD_5;
		case NUMPAD6:
			return SWT.KEYPAD_6;
		case NUMPAD7:
			return SWT.KEYPAD_7;
		case NUMPAD8:
			return SWT.KEYPAD_8;
		case NUMPAD9:
			return SWT.KEYPAD_9;
		case EQUALS:
			return SWT.KEYPAD_EQUAL;
		case CAPS:
			return SWT.CAPS_LOCK;
		case NUM_LOCK:
			return SWT.NUM_LOCK;
		case SCROLL_LOCK:
			return SWT.SCROLL_LOCK;
		case PAUSE:
			return SWT.PAUSE;
		case PRINTSCREEN:
			return SWT.PRINT_SCREEN;
		case HELP:
			return SWT.HELP;
		default:
			break;
		}
		return 0;
	}

	/**
	 * Based on the argument, perform one of the expected platform traversal
	 * action. The argument should be one of the constants:
	 * <code>SWT.TRAVERSE_ESCAPE</code>, <code>SWT.TRAVERSE_RETURN</code>,
	 * <code>SWT.TRAVERSE_TAB_NEXT</code>,
	 * <code>SWT.TRAVERSE_TAB_PREVIOUS</code>,
	 * <code>SWT.TRAVERSE_ARROW_NEXT</code>,
	 * <code>SWT.TRAVERSE_ARROW_PREVIOUS</code>,
	 * <code>SWT.TRAVERSE_PAGE_NEXT</code> and
	 * <code>SWT.TRAVERSE_PAGE_PREVIOUS</code>.
	 * 
	 * @param traversal
	 *            the type of traversal
	 * @return true if the traversal succeeded
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public boolean traverse(int traversal) {
		Util.logNotImplemented();
		return false;
	}

	/**
	 * Performs a platform traversal action corresponding to a
	 * <code>KeyDown</code> event.
	 * 
	 * <p>
	 * Valid traversal values are <code>SWT.TRAVERSE_NONE</code>,
	 * <code>SWT.TRAVERSE_MNEMONIC</code>, <code>SWT.TRAVERSE_ESCAPE</code>,
	 * <code>SWT.TRAVERSE_RETURN</code>, <code>SWT.TRAVERSE_TAB_NEXT</code>,
	 * <code>SWT.TRAVERSE_TAB_PREVIOUS</code>,
	 * <code>SWT.TRAVERSE_ARROW_NEXT</code>,
	 * <code>SWT.TRAVERSE_ARROW_PREVIOUS</code>,
	 * <code>SWT.TRAVERSE_PAGE_NEXT</code> and
	 * <code>SWT.TRAVERSE_PAGE_PREVIOUS</code>. If <code>traversal</code> is
	 * <code>SWT.TRAVERSE_NONE</code> then the Traverse event is created with
	 * standard values based on the KeyDown event. If <code>traversal</code> is
	 * one of the other traversal constants then the Traverse event is created
	 * with this detail, and its <code>doit</code> is taken from the KeyDown
	 * event.
	 * </p>
	 * 
	 * @param traversal
	 *            the type of traversal, or <code>SWT.TRAVERSE_NONE</code> to
	 *            compute this from <code>event</code>
	 * @param event
	 *            the KeyDown event
	 * 
	 * @return <code>true</code> if the traversal succeeded
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT if the event is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @since 3.6
	 */
	public boolean traverse(int traversal, Event event) {
		Util.logNotImplemented();
		return false;
	}

	/**
	 * Performs a platform traversal action corresponding to a
	 * <code>KeyDown</code> event.
	 * 
	 * <p>
	 * Valid traversal values are <code>SWT.TRAVERSE_NONE</code>,
	 * <code>SWT.TRAVERSE_MNEMONIC</code>, <code>SWT.TRAVERSE_ESCAPE</code>,
	 * <code>SWT.TRAVERSE_RETURN</code>, <code>SWT.TRAVERSE_TAB_NEXT</code>,
	 * <code>SWT.TRAVERSE_TAB_PREVIOUS</code>,
	 * <code>SWT.TRAVERSE_ARROW_NEXT</code>,
	 * <code>SWT.TRAVERSE_ARROW_PREVIOUS</code>,
	 * <code>SWT.TRAVERSE_PAGE_NEXT</code> and
	 * <code>SWT.TRAVERSE_PAGE_PREVIOUS</code>. If <code>traversal</code> is
	 * <code>SWT.TRAVERSE_NONE</code> then the Traverse event is created with
	 * standard values based on the KeyDown event. If <code>traversal</code> is
	 * one of the other traversal constants then the Traverse event is created
	 * with this detail, and its <code>doit</code> is taken from the KeyDown
	 * event.
	 * </p>
	 * 
	 * @param traversal
	 *            the type of traversal, or <code>SWT.TRAVERSE_NONE</code> to
	 *            compute this from <code>event</code>
	 * @param event
	 *            the KeyDown event
	 * 
	 * @return <code>true</code> if the traversal succeeded
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT if the event is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @since 3.6
	 */
	public boolean traverse(int traversal, KeyEvent event) {
		Util.logNotImplemented();
		return false;
	}

	/**
	 * Forces all outstanding paint requests for the widget to be processed
	 * before this method returns. If there are no outstanding paint request,
	 * this method does nothing.
	 * <p>
	 * Note: This method does not cause a redraw.
	 * </p>
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @see #redraw()
	 * @see #redraw(int, int, int, int, boolean)
	 * @see PaintListener
	 * @see SWT#Paint
	 */
	public void update() {
		// TODO do nothing for now
	}

	void updateLayout (boolean resize, boolean all) {
		/* Do nothing */
	}

	void updateStateMask(Event swtEvent, javafx.scene.input.MouseEvent event) {
		if (event.isAltDown()) {
			swtEvent.stateMask |= SWT.ALT;
		}
		if (event.isShiftDown()) {
			swtEvent.stateMask |= SWT.SHIFT;
		}
		if (event.isControlDown()) {
			swtEvent.stateMask |= SWT.CONTROL;
		}
		// TODO Is this correct???
		if (event.isMetaDown()) {
			swtEvent.stateMask |= SWT.COMMAND;
		}

		if (swtEvent.type == SWT.MouseDown || swtEvent.type == SWT.MouseMove) {
			switch (swtEvent.button) {
			case 1:
				swtEvent.stateMask |= SWT.BUTTON1;
				break;
			case 2:
				swtEvent.stateMask |= SWT.BUTTON2;
				break;
			case 3:
				swtEvent.stateMask |= SWT.BUTTON3;
				break;
			case 4:
				swtEvent.stateMask |= SWT.BUTTON4;
				break;
			case 5:
				swtEvent.stateMask |= SWT.BUTTON5;
				break;
			default:
				break;
			}
		}
	}

	/**
	 * Invokes platform specific functionality to allocate a new GC handle.
	 * <p>
	 * <b>IMPORTANT:</b> This method is <em>not</em> part of the public API for
	 * <code>Control</code>. It is marked public only so that it can be shared
	 * within the packages provided by SWT. It is not available on all
	 * platforms, and should never be called from application code.
	 * </p>
	 * 
	 * @param data
	 *            the platform specific GC data
	 * @return the platform specific GC handle
	 * 
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public long /* int */internal_new_GC(GCData data) {
		// TODO
		return 0;
	}

	/**
	 * Invokes platform specific functionality to dispose a GC handle.
	 * <p>
	 * <b>IMPORTANT:</b> This method is <em>not</em> part of the public API for
	 * <code>Control</code>. It is marked public only so that it can be shared
	 * within the packages provided by SWT. It is not available on all
	 * platforms, and should never be called from application code.
	 * </p>
	 * 
	 * @param hDC
	 *            the platform specific GC handle
	 * @param data
	 *            the platform specific GC data
	 * 
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public void internal_dispose_GC(long /* int */hDC, GCData data) {
		// TODO
	}
	
}

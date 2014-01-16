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

import java.util.ArrayList;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.internal.Util;

import com.sun.javafx.geom.PathIterator;

/**
 * Instances of this class represent the "windows" which the desktop or
 * "window manager" is managing. Instances that do not have a parent (that is,
 * they are built using the constructor, which takes a <code>Display</code> as
 * the argument) are described as <em>top level</em> shells. Instances that do
 * have a parent are described as <em>secondary</em> or <em>dialog</em> shells.
 * <p>
 * Instances are always displayed in one of the maximized, minimized or normal
 * states:
 * <ul>
 * <li>
 * When an instance is marked as <em>maximized</em>, the window manager will
 * typically resize it to fill the entire visible area of the display, and the
 * instance is usually put in a state where it can not be resized (even if it
 * has style <code>RESIZE</code>) until it is no longer maximized.</li>
 * <li>
 * When an instance is in the <em>normal</em> state (neither maximized or
 * minimized), its appearance is controlled by the style constants which were
 * specified when it was created and the restrictions of the window manager (see
 * below).</li>
 * <li>
 * When an instance has been marked as <em>minimized</em>, its contents (client
 * area) will usually not be visible, and depending on the window manager, it
 * may be "iconified" (that is, replaced on the desktop by a small simplified
 * representation of itself), relocated to a distinguished area of the screen,
 * or hidden. Combinations of these changes are also possible.</li>
 * </ul>
 * </p>
 * <p>
 * The <em>modality</em> of an instance may be specified using style bits. The
 * modality style bits are used to determine whether input is blocked for other
 * shells on the display. The <code>PRIMARY_MODAL</code> style allows an
 * instance to block input to its parent. The <code>APPLICATION_MODAL</code>
 * style allows an instance to block input to every other shell in the display.
 * The <code>SYSTEM_MODAL</code> style allows an instance to block input to all
 * shells, including shells belonging to different applications.
 * </p>
 * <p>
 * Note: The styles supported by this class are treated as <em>HINT</em>s, since
 * the window manager for the desktop on which the instance is visible has
 * ultimate control over the appearance and behavior of decorations and
 * modality. For example, some window managers only support resizable windows
 * and will always assume the RESIZE style, even if it is not set. In addition,
 * if a modality style is not supported, it is "upgraded" to a more restrictive
 * modality style that is supported. For example, if <code>PRIMARY_MODAL</code>
 * is not supported, it would be upgraded to <code>APPLICATION_MODAL</code>. A
 * modality style may also be "downgraded" to a less restrictive style. For
 * example, most operating systems no longer support <code>SYSTEM_MODAL</code>
 * because it can freeze up the desktop, so this is typically downgraded to
 * <code>APPLICATION_MODAL</code>.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>BORDER, CLOSE, MIN, MAX, NO_TRIM, RESIZE, TITLE, ON_TOP, TOOL, SHEET</dd>
 * <dd>APPLICATION_MODAL, MODELESS, PRIMARY_MODAL, SYSTEM_MODAL</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Activate, Close, Deactivate, Deiconify, Iconify</dd>
 * </dl>
 * Class <code>SWT</code> provides two "convenience constants" for the most
 * commonly required style combinations:
 * <dl>
 * <dt><code>SHELL_TRIM</code></dt>
 * <dd>
 * the result of combining the constants which are required to produce a typical
 * application top level shell: (that is,
 * <code>CLOSE | TITLE | MIN | MAX | RESIZE</code>)</dd>
 * <dt><code>DIALOG_TRIM</code></dt>
 * <dd>
 * the result of combining the constants which are required to produce a typical
 * application dialog shell: (that is, <code>TITLE | CLOSE | BORDER</code>)</dd>
 * </dl>
 * </p>
 * <p>
 * Note: Only one of the styles APPLICATION_MODAL, MODELESS, PRIMARY_MODAL and
 * SYSTEM_MODAL may be specified.
 * </p>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 * 
 * @see Decorations
 * @see SWT
 * @see <a href="http://www.eclipse.org/swt/snippets/#shell">Shell snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example:
 *      ControlExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further
 *      information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class Shell extends Decorations {

	private Stage stage;
	private static final double MIN_WIDTH = 500;
	private static final double MIN_HEIGHT = 200;
	private BorderPane nativeObject;
	private Shell parentShell;
	private org.eclipse.swt.graphics.Region region;
	
	/**
	 * Constructs a new instance of this class. This is equivalent to calling
	 * <code>Shell((Display) null)</code>.
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the parent</li>
	 *                <li>ERROR_INVALID_SUBCLASS - if this class is not an
	 *                allowed subclass</li>
	 *                </ul>
	 */
	public Shell() {
		this((Display) null, SWT.DIALOG_TRIM);
	}

	/**
	 * Constructs a new instance of this class given only the style value
	 * describing its behavior and appearance. This is equivalent to calling
	 * <code>Shell((Display) null, style)</code>.
	 * <p>
	 * The style value is either one of the style constants defined in class
	 * <code>SWT</code> which is applicable to instances of this class, or must
	 * be built by <em>bitwise OR</em>'ing together (that is, using the
	 * <code>int</code> "|" operator) two or more of those <code>SWT</code>
	 * style constants. The class description lists the style constants that are
	 * applicable to the class. Style bits are also inherited from superclasses.
	 * </p>
	 * 
	 * @param style
	 *            the style of control to construct
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the parent</li>
	 *                <li>ERROR_INVALID_SUBCLASS - if this class is not an
	 *                allowed subclass</li>
	 *                </ul>
	 * 
	 * @see SWT#BORDER
	 * @see SWT#CLOSE
	 * @see SWT#MIN
	 * @see SWT#MAX
	 * @see SWT#RESIZE
	 * @see SWT#TITLE
	 * @see SWT#TOOL
	 * @see SWT#NO_TRIM
	 * @see SWT#SHELL_TRIM
	 * @see SWT#DIALOG_TRIM
	 * @see SWT#ON_TOP
	 * @see SWT#MODELESS
	 * @see SWT#PRIMARY_MODAL
	 * @see SWT#APPLICATION_MODAL
	 * @see SWT#SYSTEM_MODAL
	 * @see SWT#SHEET
	 */
	public Shell(int style) {
		this((Display) null, style);
	}

	/**
	 * Constructs a new instance of this class given only the display to create
	 * it on. It is created with style <code>SWT.SHELL_TRIM</code>.
	 * <p>
	 * Note: Currently, null can be passed in for the display argument. This has
	 * the effect of creating the shell on the currently active display if there
	 * is one. If there is no current display, the shell is created on a
	 * "default" display. <b>Passing in null as the display argument is not
	 * considered to be good coding style, and may not be supported in a future
	 * release of SWT.</b>
	 * </p>
	 * 
	 * @param display
	 *            the display to create the shell on
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the parent</li>
	 *                <li>ERROR_INVALID_SUBCLASS - if this class is not an
	 *                allowed subclass</li>
	 *                </ul>
	 */
	public Shell(Display display) {
		this(display, SWT.SHELL_TRIM);
	}

	/**
	 * Constructs a new instance of this class given the display to create it on
	 * and a style value describing its behavior and appearance.
	 * <p>
	 * The style value is either one of the style constants defined in class
	 * <code>SWT</code> which is applicable to instances of this class, or must
	 * be built by <em>bitwise OR</em>'ing together (that is, using the
	 * <code>int</code> "|" operator) two or more of those <code>SWT</code>
	 * style constants. The class description lists the style constants that are
	 * applicable to the class. Style bits are also inherited from superclasses.
	 * </p>
	 * <p>
	 * Note: Currently, null can be passed in for the display argument. This has
	 * the effect of creating the shell on the currently active display if there
	 * is one. If there is no current display, the shell is created on a
	 * "default" display. <b>Passing in null as the display argument is not
	 * considered to be good coding style, and may not be supported in a future
	 * release of SWT.</b>
	 * </p>
	 * 
	 * @param display
	 *            the display to create the shell on
	 * @param style
	 *            the style of control to construct
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the parent</li>
	 *                <li>ERROR_INVALID_SUBCLASS - if this class is not an
	 *                allowed subclass</li>
	 *                </ul>
	 * 
	 * @see SWT#BORDER
	 * @see SWT#CLOSE
	 * @see SWT#MIN
	 * @see SWT#MAX
	 * @see SWT#RESIZE
	 * @see SWT#TITLE
	 * @see SWT#TOOL
	 * @see SWT#NO_TRIM
	 * @see SWT#SHELL_TRIM
	 * @see SWT#DIALOG_TRIM
	 * @see SWT#ON_TOP
	 * @see SWT#MODELESS
	 * @see SWT#PRIMARY_MODAL
	 * @see SWT#APPLICATION_MODAL
	 * @see SWT#SYSTEM_MODAL
	 * @see SWT#SHEET
	 */
	public Shell(Display display, int style) {
		this(display, null, style);
	}

	Shell (Display display, Shell parent, int style) {
		checkSubclass ();
		if (display == null) display = Display.getCurrent ();
		if (display == null) display = Display.getDefault ();
		if (!display.isValidThread ()) {
			error (SWT.ERROR_THREAD_INVALID_ACCESS);
		}
		if (parent != null && parent.isDisposed ()) {
			error (SWT.ERROR_INVALID_ARGUMENT);	
		}
		this.style = style;
		this.display = display;
		this.parent = parent;
		reskinWidget();
		createWidget();
	}

	/**
	 * Constructs a new instance of this class given only its parent. It is
	 * created with style <code>SWT.DIALOG_TRIM</code>.
	 * <p>
	 * Note: Currently, null can be passed in for the parent. This has the
	 * effect of creating the shell on the currently active display if there is
	 * one. If there is no current display, the shell is created on a "default"
	 * display. <b>Passing in null as the parent is not considered to be good
	 * coding style, and may not be supported in a future release of SWT.</b>
	 * </p>
	 * 
	 * @param parent
	 *            a shell which will be the parent of the new instance
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_ARGUMENT - if the parent is disposed</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the parent</li>
	 *                <li>ERROR_INVALID_SUBCLASS - if this class is not an
	 *                allowed subclass</li>
	 *                </ul>
	 */
	public Shell(Shell parent) {
		this(parent, SWT.SHELL_TRIM);
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
	 * <p>
	 * Note: Currently, null can be passed in for the parent. This has the
	 * effect of creating the shell on the currently active display if there is
	 * one. If there is no current display, the shell is created on a "default"
	 * display. <b>Passing in null as the parent is not considered to be good
	 * coding style, and may not be supported in a future release of SWT.</b>
	 * </p>
	 * 
	 * @param parent
	 *            a shell which will be the parent of the new instance
	 * @param style
	 *            the style of control to construct
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_ARGUMENT - if the parent is disposed</li>
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
	 * @see SWT#CLOSE
	 * @see SWT#MIN
	 * @see SWT#MAX
	 * @see SWT#RESIZE
	 * @see SWT#TITLE
	 * @see SWT#NO_TRIM
	 * @see SWT#SHELL_TRIM
	 * @see SWT#DIALOG_TRIM
	 * @see SWT#ON_TOP
	 * @see SWT#TOOL
	 * @see SWT#MODELESS
	 * @see SWT#PRIMARY_MODAL
	 * @see SWT#APPLICATION_MODAL
	 * @see SWT#SYSTEM_MODAL
	 * @see SWT#SHEET
	 */
	public Shell(Shell parent, int style) {
		this(parent != null ? parent.getDisplay() : Display.getDefault(), style);
		stage.initOwner(parent.stage);
		this.parentShell = parent;
	}

	public Shell(Stage stage) {
		this(Display.getCurrent(), SWT.NONE);
		this.stage = stage;
	}
	
	/**
	 * Adds the listener to the collection of listeners who will be notified
	 * when operations are performed on the receiver, by sending the listener
	 * one of the messages defined in the <code>ShellListener</code> interface.
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
	 * @see ShellListener
	 * @see #removeShellListener
	 */
	public void addShellListener(ShellListener listener) {
		Util.logNotImplemented();
	}

	/**
	 * Requests that the window manager close the receiver in the same way it
	 * would be closed when the user clicks on the "close box" or performs some
	 * other platform specific key or mouse combination that indicates the
	 * window should be removed.
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @see SWT#Close
	 * @see #dispose
	 */
	public void close() {
		stage.close();
	}

	@Override
	protected javafx.scene.layout.Region createWidget() {
		if( stage != null ) {
			return nativeObject = new BorderPane();
		}
		
		javafx.scene.layout.Region r = super.createWidget();
		nativeObject = new BorderPane();
		nativeObject.setCenter(r);
		
		stage = new Stage();
		final Scene s = new Scene(internal_getNativeObject());
		s.getStylesheets().add(getClass().getClassLoader().getResource("org/eclipse/swt/internal/swt.css").toExternalForm());
		stage.setScene(s);
		if( (getStyle() & SWT.TOOL) == SWT.TOOL ) {
			stage.initStyle(StageStyle.UNDECORATED);
		}
		
		if( (getStyle() & SWT.NO_FOCUS) == SWT.NO_FOCUS ) {
			System.err.println("NO FOCUS NOT IMPLEMENTED");
		}
		stage.setOnShowing(new EventHandler<WindowEvent>() {
			
			@Override
			public void handle(WindowEvent event) {
				if( stage.widthProperty().getValue().equals(Double.NaN) && stage.heightProperty().getValue().equals(Double.NaN) ) {
					stage.setWidth(MIN_WIDTH);
					stage.setHeight(MIN_HEIGHT);
				}
			}
		});
		stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, new EventHandler<WindowEvent>() {

			@Override
			public void handle(WindowEvent event) {
				if( isListening(SWT.Close) ) {
					Event evt = new Event();
					internal_sendEvent(SWT.Close, evt, true);
					if( ! evt.doit ) {
						event.consume();
					}
				}
			}
		});
		stage.focusedProperty().addListener(new InvalidationListener() {
			
			@Override
			public void invalidated(Observable observable) {
				if( s.getFocusOwner() != null ) {
					Object o = Widget.getWidget(s.getFocusOwner());
					if( o instanceof Control ) {
						getDisplay().setFocusControl((Control) o);	
					}
				} else {
					getDisplay().setFocusControl(null);
				}
			}
		});
		s.focusOwnerProperty().addListener(new InvalidationListener() {
			
			@Override
			public void invalidated(Observable observable) {
				if( stage.isFocused() ) {
					if( s.getFocusOwner() != null ) {
						Object o = Widget.getWidget(s.getFocusOwner());
						if( o instanceof Control ) {
							getDisplay().setFocusControl((Control) o);	
						}
					} else {
						getDisplay().setFocusControl(null);
					}
				}
			}
		});
		
		getDisplay().registerShell(this);
		
		return nativeObject;
	}
	
	@Override
	public void dispose() {
		getDisplay().unregisterShell(this);
		super.dispose();
		stage.close();
	}

	/**
	 * If the receiver is visible, moves it to the top of the drawing order for
	 * the display on which it was created (so that all other shells on that
	 * display, which are not the receiver's children will be drawn behind it)
	 * and forces the window manager to make the shell active.
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
	 * @see Control#moveAbove
	 * @see Control#setFocus
	 * @see Control#setVisible
	 * @see Display#getActiveShell
	 * @see Decorations#setDefaultButton(Button)
	 * @see Shell#open
	 * @see Shell#setActive
	 */
	public void forceActive() {
		Util.logNotImplemented();
	}

	/**
	 * Returns the receiver's alpha value. The alpha value is between 0
	 * (transparent) and 255 (opaque).
	 * 
	 * @return the alpha value
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
	public int getAlpha() {
		return 255 - (int)(stage.getOpacity() * 255);
	}

	@Override
	public Rectangle getBounds() {
		return new Rectangle((int)stage.getX(), (int)stage.getY(), (int)stage.getWidth(), (int)stage.getHeight());
	}

	/**
	 * Returns <code>true</code> if the receiver is currently in fullscreen
	 * state, and false otherwise.
	 * <p>
	 * 
	 * @return the fullscreen state
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li> <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                called from the thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @since 3.4
	 */
	public boolean getFullScreen() {
		// TODO
		return false;
	}

	/**
	 * Returns the receiver's input method editor mode. This will be the result
	 * of bitwise OR'ing together one or more of the following constants defined
	 * in class <code>SWT</code>: <code>NONE</code>, <code>ROMAN</code>,
	 * <code>DBCS</code>, <code>PHONETIC</code>, <code>NATIVE</code>,
	 * <code>ALPHA</code>.
	 * 
	 * @return the IME mode
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @see SWT
	 */
	public int getImeInputMode() {
		// TODO
		return 0;
	}

	@Override
	public Point getLocation() {
		return new Point((int)stage.getX(), (int)stage.getY());
	}
	
	public boolean getMaximized () {
		return stage.isMaximized();
	}
	
	public boolean getMinimized () {
		return stage.isIconified();
	}
	
	/**
	 * Returns a point describing the minimum receiver's size. The x coordinate
	 * of the result is the minimum width of the receiver. The y coordinate of
	 * the result is the minimum height of the receiver.
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
	 * 
	 * @since 3.1
	 */
	public Point getMinimumSize() {
		return new Point((int)stage.getMinWidth(), (int)stage.getMinHeight());
	}

	/**
	 * Gets the receiver's modified state.
	 * 
	 * @return <code>true</code> if the receiver is marked as modified, or
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
	 * @since 3.5
	 */
	public boolean getModified() {
		// TODO
		return false;
	}

	@Override
	public Region getRegion () {
		return region;
	}
	
	@Override
	public Point getSize() {
		return new Point((int)stage.getWidth(), (int)stage.getHeight());
	}
	
	/**
	 * Not part of official SWT API, but part of bridge API
	 * 
	 * @return JavaFX Stage object this Shell represents
	 */
	public Stage getStage() {
		return stage;
	}
	
	/**
	 * Returns a ToolBar object representing the tool bar that can be shown in
	 * the receiver's trim. This will return <code>null</code> if the platform
	 * does not support tool bars that are not part of the content area of the
	 * shell, or if the Shell's style does not support having a tool bar.
	 * <p>
	 * 
	 * @return a ToolBar object representing the Shell's tool bar, or
	 *         <ocde>null</code>.
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
	public ToolBar getToolBar() {
		// TODO
		return null;
	}

	@Override
	public Shell getShell() {
		return this;
	}

	/**
	 * Returns an array containing all shells which are descendants of the
	 * receiver.
	 * <p>
	 * 
	 * @return the dialog shells
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li> <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public Shell[] getShells() {
		java.util.List<Shell> shells = new ArrayList<>();
		for( Shell s : getDisplay().getShells() ) {
			if( s.parentShell == this ) {
				shells.add(s);
			}
		}
		return shells.toArray(new Shell[shells.size()]);
	}

	@Override
	public String getText() {
		return stage.getTitle();
	}
	
	@Override
	protected void initListeners() {
		super.initListeners();
		stage.setOnHidden(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				dispose();
			}
		});
	}

	@Override
	protected double internal_getHeight() {
		return stage.getHeight();
	}
	
	@Override
	public javafx.scene.layout.Region internal_getNativeObject() {
		return nativeObject;
	}
	
	@Override
	protected double internal_getWidth() {
		return stage.getWidth();
	}
	
	public Window internal_getWindow() {
		return stage;
	}
	
	@Override
	public boolean isDisposed() {
		return stage == null;
	}
	
	@Override
	public boolean isEnabled() {
		return getEnabled();
	}
	
	public boolean isVisible () {
		return getVisible();
	}
	
	/**
	 * Moves the receiver to the top of the drawing order for the display on
	 * which it was created (so that all other shells on that display, which are
	 * not the receiver's children will be drawn behind it), marks it visible,
	 * sets the focus and asks the window manager to make the shell active.
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @see Control#moveAbove
	 * @see Control#setFocus
	 * @see Control#setVisible
	 * @see Display#getActiveShell
	 * @see Decorations#setDefaultButton(Button)
	 * @see Shell#setActive
	 * @see Shell#forceActive
	 */
	public void open() {
		stage.show();
	}

	@Override
	public void pack() {
		stage.sizeToScene();
	}
	
	/**
	 * Removes the listener from the collection of listeners who will be
	 * notified when operations are performed on the receiver.
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
	 * @see ShellListener
	 * @see #addShellListener
	 */
	public void removeShellListener(ShellListener listener) {
		Util.logNotImplemented();
	}

	/**
	 * If the receiver is visible, moves it to the top of the drawing order for
	 * the display on which it was created (so that all other shells on that
	 * display, which are not the receiver's children will be drawn behind it)
	 * and asks the window manager to make the shell active
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
	 * @see Control#moveAbove
	 * @see Control#setFocus
	 * @see Control#setVisible
	 * @see Display#getActiveShell
	 * @see Decorations#setDefaultButton(Button)
	 * @see Shell#open
	 * @see Shell#setActive
	 */
	public void setActive() {
		stage.toFront();
		stage.setFocused(true);
	}

	/**
	 * Sets the receiver's alpha value which must be between 0 (transparent) and
	 * 255 (opaque).
	 * <p>
	 * This operation requires the operating system's advanced widgets subsystem
	 * which may not be available on some platforms.
	 * </p>
	 * 
	 * @param alpha
	 *            the alpha value
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
	public void setAlpha(int alpha) {
		stage.setOpacity(alpha/255.0);
	}

	@Override
	public void setBounds(int x, int y, int width, int height) {
		setFullScreen(false);
		stage.setX(x);
		stage.setY(y);
		stage.setWidth(width);
		stage.setHeight(height);
	}

	@Override
	public void setDefaultButton(Button defaultButton) {
		Button b = getDefaultButton();
		if( b != null ) {
			b.internal_setDefault(false);
		}
		
		super.setDefaultButton(defaultButton);
		
		if( defaultButton != null ) {
			defaultButton.internal_setDefault(true);
		}
	}
	
	/**
	 * Sets the full screen state of the receiver. If the argument is
	 * <code>true</code> causes the receiver to switch to the full screen state,
	 * and if the argument is <code>false</code> and the receiver was previously
	 * switched into full screen state, causes the receiver to switch back to
	 * either the maximized or normal states.
	 * <p>
	 * Note: The result of intermixing calls to <code>setFullScreen(true)</code>, <code>setMaximized(true)</code> and <code>setMinimized(true)</code>
	 * will vary by platform. Typically, the behavior will match the platform
	 * user's expectations, but not always. This should be avoided if possible.
	 * </p>
	 * 
	 * @param fullScreen
	 *            the new fullscreen state
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
	public void setFullScreen(boolean fullScreen) {
		stage.setFullScreen(fullScreen);
	}

	@Override
	public void setImage(Image image) {
		Image oldImage = getImage();
		super.setImage(image);
		
		if( oldImage != null ) {
			stage.getIcons().remove(oldImage.internal_getImage());
		}
		
		if( image != null ) {
			stage.getIcons().add(image.internal_getImage());
		}
	}
	
	@Override
	public void setImages(Image[] images) {
		super.setImages(images);
		javafx.scene.image.Image[] imgs = new javafx.scene.image.Image[images.length];
		
		for(int i = 0; i < imgs.length; i++) {
			imgs[i] = images[i].internal_getImage();
		}
		
		stage.getIcons().setAll(imgs);
	}

	/**
	 * Sets the input method editor mode to the argument which should be the
	 * result of bitwise OR'ing together one or more of the following constants
	 * defined in class <code>SWT</code>: <code>NONE</code>, <code>ROMAN</code>,
	 * <code>DBCS</code>, <code>PHONETIC</code>, <code>NATIVE</code>,
	 * <code>ALPHA</code>.
	 * 
	 * @param mode
	 *            the new IME mode
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @see SWT
	 */
	public void setImeInputMode(int mode) {
		// TODO
	}

	public void setMenuBar (Menu menu) {
		if( (menu.style & SWT.BAR) == SWT.BAR ) {
			nativeObject.setTop((Node)menu.internal_getNativeObject());
		}
		super.setMenuBar(menu);
	}

	@Override
	public void setLocation(int x, int y) {
		stage.setX(x);
		stage.setY(y);
	}
	
	public void setMaximized (boolean maximized) {
		stage.setMaximized(true);
		super.setMaximized(maximized);
	}
	
	public void setMinimized (boolean minimized) {
		stage.setIconified(true);
		super.setMinimized(minimized);
	}
	
	/**
	 * Sets the receiver's minimum size to the size specified by the arguments.
	 * If the new minimum size is larger than the current size of the receiver,
	 * the receiver is resized to the new minimum size.
	 * 
	 * @param width
	 *            the new minimum width for the receiver
	 * @param height
	 *            the new minimum height for the receiver
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @since 3.1
	 */
	public void setMinimumSize(int width, int height) {
		stage.setMinWidth(width);
		stage.setMinHeight(height);
	}

	/**
	 * Sets the receiver's minimum size to the size specified by the argument.
	 * If the new minimum size is larger than the current size of the receiver,
	 * the receiver is resized to the new minimum size.
	 * 
	 * @param size
	 *            the new minimum size for the receiver
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
	 * @since 3.1
	 */
	public void setMinimumSize(Point size) {
		setMinimumSize(size.x, size.y);
	}

	/**
	 * Sets the receiver's modified state as specified by the argument.
	 * 
	 * @param modified
	 *            the new modified state for the receiver
	 * 
	 *            </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @since 3.5
	 */
	public void setModified(boolean modified) {
		Util.logNotImplemented();
	}

	/**
	 * Sets the shape of the shell to the region specified by the argument. When
	 * the argument is null, the default shape of the shell is restored. The
	 * shell must be created with the style SWT.NO_TRIM in order to specify a
	 * region.
	 * 
	 * @param region
	 *            the region that defines the shape of the shell (or null)
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
	 * @since 3.0
	 * 
	 */
	@Override
	public void setRegion(Region region) {
		float coords[] = new float[6];
		
		float x = 0;
		float y = 0;
		
		PathIterator pathIterator = region.internal_getNativeObject().getPathIterator(null);
		
		Path p = new Path();
		
		p.getElements().add(new MoveTo(0, 0));
		
		while( ! pathIterator.isDone() ) {
			switch (pathIterator.currentSegment(coords)) {
			case PathIterator.SEG_CLOSE:
				p.getElements().add(new LineTo(x, y));
				break;
			case PathIterator.SEG_CUBICTO:
//				p.getElements().add( new BezierCurveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
				break;
			case PathIterator.SEG_LINETO:
				p.getElements().add(new LineTo(coords[0], coords[1]));
				break;
			case PathIterator.SEG_MOVETO:
				p.getElements().add(new MoveTo(coords[0], coords[1]));
				x = coords[0];
				y = coords[1];
				break;
			case PathIterator.SEG_QUADTO:
//				gc.quadraticCurveTo(coords[0], coords[1], coords[2], coords[3]);
				break;
			default:
				break;
			}
			pathIterator.next();
		}
		
		stage.getScene().getRoot().setClip(p);
		this.region = region;
	}
	

	@Override
	public void setSize(int width, int height) {
		stage.setWidth(width);
		stage.setHeight(height);
	}
	
	@Override
	public void setText(String string) {
		stage.setTitle(string);
	}

	public void setVisible (boolean visible) {
		stage.show();
	}
	
}

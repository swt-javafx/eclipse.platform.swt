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

import java.util.LinkedList;
import java.util.List;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;

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

	Stage stage;
	private Shell parentShell;
	private List<Shell> shells = new LinkedList<Shell>();
	
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
		this((Display) null);
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
		super(null, style);
		init();
	}

	private void init() {
		if (Display.primaryStage != null) {
			// First shell, use the primary stage
			stage = Display.primaryStage;
			Display.primaryStage = null;
		} else {
			stage = new Stage();
		}
		
		// TODO StageStyle
		display.addShell(this);
		
		stage.addEventHandler(WindowEvent.ANY, new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				if (event.getEventType().equals(WindowEvent.WINDOW_SHOWING)) {
					sendEvent(SWT.Activate);
				}
			}
		});
	}
	
	@Override
	void setNode(final Node node) {
		super.setNode(node);
		stage.setScene(new Scene((Parent)node, 640, 480));
		stage.setX(10);
		stage.setY(10);
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
		this(parent, SWT.DIALOG_TRIM);
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
		
		if (parent != null) {
			parentShell = parent;
			parent.addShell(this);
		}
	}

	/**
	 * Not part of official SWT API, but part of bridge API
	 * 
	 * @return JavaFX Stage object this Shell represents
	 */
	public Stage getStage() {
		return stage;
	}
	
	void addShell(Shell childShell) {
		shells.add(childShell);
	}
	
	void removeShell(Shell childShell) {
		shells.remove(childShell);
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
		// TODO
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
		// TODO
	}

	@Override
	public void dispose() {
		if (parentShell != null)
			parentShell.removeChild(this);
		stage = null;
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
		// TODO
		return 0;
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

		return stage.isFullScreen();
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
		// TODO
		return null;
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
		// TODO
		return new Shell[0];
	}

	@Override
	public String getText() {
		return stage.getTitle();
	}
	
	@Override
	public boolean isDisposed() {
		return stage == null;
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
		// TODO
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
		// TODO
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
		// TODO
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
		// TODO
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
		// TODO
	}

	@Override
	public void setSize(int width, int height) {
		stage.setWidth(width);
		stage.setHeight(height);
	}
	
	@Override
	public void setBounds(int x, int y, int width, int height) {
		stage.setX(x);
		stage.setY(y);
		setSize(width,height);
	}
	
	@Override
	public void setBounds(Rectangle rect) {
		setBounds(rect.x,rect.y,rect.width,rect.height);
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
		// TODO
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
		stage.setX(region.getBounds().x);
		stage.setY(region.getBounds().y);
		stage.setWidth(region.getBounds().width);
		stage.setHeight(region.getBounds().height);
	}

	@Override
	public void setText(String string) {
		stage.setTitle(string);
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
		// TODO
	}

}

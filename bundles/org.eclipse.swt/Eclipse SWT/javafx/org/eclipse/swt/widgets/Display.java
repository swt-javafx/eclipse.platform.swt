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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.DeviceData;
import org.eclipse.swt.graphics.GCData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.Util;

import com.sun.glass.ui.Robot;
import com.sun.javafx.tk.Toolkit;

/**
 * Instances of this class are responsible for managing the connection between
 * SWT and the underlying operating system. Their most important function is to
 * implement the SWT event loop in terms of the platform event model. They also
 * provide various methods for accessing information about the operating system,
 * and have overall control over the operating system resources which SWT
 * allocates.
 * <p>
 * Applications which are built with SWT will <em>almost always</em> require
 * only a single display. In particular, some platforms which SWT supports will
 * not allow more than one <em>active</em> display. In other words, some
 * platforms do not support creating a new display if one already exists that
 * has not been sent the <code>dispose()</code> message.
 * <p>
 * In SWT, the thread which creates a <code>Display</code> instance is
 * distinguished as the <em>user-interface thread</em> for that display.
 * </p>
 * The user-interface thread for a particular display has the following special
 * attributes:
 * <ul>
 * <li>
 * The event loop for that display must be run from the thread.</li>
 * <li>
 * Some SWT API methods (notably, most of the public methods in
 * <code>Widget</code> and its subclasses), may only be called from the thread.
 * (To support multi-threaded user-interface applications, class
 * <code>Display</code> provides inter-thread communication methods which allow
 * threads other than the user-interface thread to request that it perform
 * operations on their behalf.)</li>
 * <li>
 * The thread is not allowed to construct other <code>Display</code>s until that
 * display has been disposed. (Note that, this is in addition to the restriction
 * mentioned above concerning platform support for multiple displays. Thus, the
 * only way to have multiple simultaneously active displays, even on platforms
 * which support it, is to have multiple threads.)</li>
 * </ul>
 * Enforcing these attributes allows SWT to be implemented directly on the
 * underlying operating system's event model. This has numerous benefits
 * including smaller footprint, better use of resources, safer memory
 * management, clearer program logic, better performance, and fewer overall
 * operating system threads required. The down side however, is that care must
 * be taken (only) when constructing multi-threaded applications to use the
 * inter-thread communication mechanisms which this class provides when
 * required. </p>
 * <p>
 * All SWT API methods which may only be called from the user-interface thread
 * are distinguished in their documentation by indicating that they throw the "
 * <code>ERROR_THREAD_INVALID_ACCESS</code>" SWT exception.
 * </p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>(none)</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Close, Dispose, OpenDocument, Settings, Skin</dd>
 * </dl>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 * 
 * @see #syncExec
 * @see #asyncExec
 * @see #wake
 * @see #readAndDispatch
 * @see #sleep
 * @see Device#dispose
 * @see <a href="http://www.eclipse.org/swt/snippets/#display">Display
 *      snippets</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further
 *      information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class Display extends Device {

	static class DisplayTimerTask extends TimerTask {
		private List<DisplayTimerTask> tasks;
		private Runnable r;

		public DisplayTimerTask(List<DisplayTimerTask> tasks, Runnable r) {
			this.tasks = tasks;
			this.r = r;
			this.tasks.add(this);
		}
		
		@Override
		public void run() {
			this.tasks.remove(this);
			Platform.runLater(r);
			this.r = null;
		}
	}

	private static Display DEFAULT;
	private static String appName;
	private CountDownLatch startupLatch;
//	private volatile boolean inSleep;
//	private Thread wakeThread;
	private Timer timer;
	private int state;
	private List<DisplayTimerTask> currentTasks = new Vector<>();
	private Color[] widgetColors = new Color[SWT.COLOR_LINK_FOREGROUND + 1];
	
	private static final int KEYED_DATA = 1 << 1;
	private Control focusControl;
	private Timeline hoverTimer = new Timeline();
	
	Cursor [] cursors = new Cursor [SWT.CURSOR_HAND + 1];
	private Control hoverControl;
	private List<Shell> shells = new ArrayList<>();
	private List<Runnable> disposeList;
	private Image[] systemImages = new Image[5];
	private EventTable filterTable, eventTable;
	private Thread thread;
	private Object data;
	Tray tray;
	
	/*
	* TEMPORARY CODE.  Install the runnable that
	* gets the current display. This code will
	* be removed in the future.
	*/
	static {
		DeviceFinder = new Runnable () {
			public void run () {
				Device device = getCurrent ();
				if (device == null) {
					device = getDefault ();
				}
				setDevice (device);
			}
		};
	}

	/*
	* TEMPORARY CODE.
	*/
	static void setDevice (Device device) {
		CurrentDevice = device;
	}

	public static class SWTApplication extends Application {
		@Override
		public void start(Stage primaryStage) throws Exception {
			DEFAULT.thread = Thread.currentThread();
			if (appName != null)
				setAppName(appName);
			DEFAULT.startupLatch.countDown();
		}
	}
	
	/**
	 * Constructs a new instance of this class.
	 * <p>
	 * Note: The resulting display is marked as the <em>current</em> display. If
	 * this is the first display which has been constructed since the
	 * application started, it is also marked as the <em>default</em> display.
	 * </p>
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if called from a thread
	 *                that already created an existing display</li>
	 *                <li>ERROR_INVALID_SUBCLASS - if this class is not an
	 *                allowed subclass</li>
	 *                </ul>
	 * 
	 * @see #getCurrent
	 * @see #getDefault
	 * @see Widget#checkSubclass
	 * @see Shell
	 */
	public Display() {
		if (DEFAULT != null)
			throw new UnsupportedOperationException();
		DEFAULT = this;

		startupLatch = new CountDownLatch(1);
		new Thread("SWT JavaFX Launcher") {
			public void run() {
				Application.launch(SWTApplication.class, new String[0]);
				dispose();
			};
		}.start();
		try {
			startupLatch.await();
			startupLatch = null;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		hoverTimer.getKeyFrames().add(new KeyFrame(Duration.millis(560)));
		hoverTimer.setOnFinished(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if( hoverControl != null ) {
					Event evt = new Event();
					Point p = hoverControl.toControl(getCursorLocation());
					evt.x = p.x;
					evt.y = p.y;
					hoverControl.sendEvent(SWT.MouseHover, evt, true);
				}
			}
		});

		initColors();
	}

	/**
	 * Constructs a new instance of this class using the parameter.
	 * 
	 * @param data
	 *            the device data
	 */
	public Display(DeviceData data) {
		this();
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified
	 * when an event of the given type occurs anywhere in a widget. The event
	 * type is one of the event constants defined in class <code>SWT</code>.
	 * When the event does occur, the listener is notified by sending it the
	 * <code>handleEvent()</code> message.
	 * <p>
	 * Setting the type of an event to <code>SWT.None</code> from within the
	 * <code>handleEvent()</code> method can be used to change the event type
	 * and stop subsequent Java listeners from running. Because event filters
	 * run before other listeners, event filters can both block other listeners
	 * and set arbitrary fields within an event. For this reason, event filters
	 * are both powerful and dangerous. They should generally be avoided for
	 * performance, debugging and code maintenance reasons.
	 * </p>
	 * 
	 * @param eventType
	 *            the type of event to listen for
	 * @param listener
	 *            the listener which should be notified when the event occurs
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 * 
	 * @see Listener
	 * @see SWT
	 * @see #removeFilter
	 * @see #removeListener
	 * 
	 * @since 3.0
	 */
	public void addFilter(int eventType, Listener listener) {
		if (listener == null) throw new SWTException(SWT.ERROR_NULL_ARGUMENT);
		if (filterTable == null) filterTable = new EventTable ();
		filterTable.hook (eventType, listener);
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified
	 * when an event of the given type occurs. The event type is one of the
	 * event constants defined in class <code>SWT</code>. When the event does
	 * occur in the display, the listener is notified by sending it the
	 * <code>handleEvent()</code> message.
	 * 
	 * @param eventType
	 *            the type of event to listen for
	 * @param listener
	 *            the listener which should be notified when the event occurs
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 * 
	 * @see Listener
	 * @see SWT
	 * @see #removeListener
	 * 
	 * @since 2.0
	 */
	public void addListener(int eventType, Listener listener) {
		if (listener == null) throw new SWTException(SWT.ERROR_NULL_ARGUMENT);
		if (eventTable == null) eventTable = new EventTable();
		eventTable.hook(eventType, listener);
	}

	void addShell(Shell shell) {
		shells.add(shell);
	}
	
	/**
	 * Causes the <code>run()</code> method of the runnable to be invoked by the
	 * user-interface thread at the next reasonable opportunity. The caller of
	 * this method continues to run in parallel, and is not notified when the
	 * runnable has completed. Specifying <code>null</code> as the runnable
	 * simply wakes the user-interface thread when run.
	 * <p>
	 * Note that at the time the runnable is invoked, widgets that have the
	 * receiver as their display may have been disposed. Therefore, it is
	 * necessary to check for this case inside the runnable before accessing the
	 * widget.
	 * </p>
	 * 
	 * @param runnable
	 *            code to run on the user-interface thread or <code>null</code>
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 * 
	 * @see #syncExec
	 */
	public void asyncExec(final Runnable runnable) {
		Platform.runLater(runnable);
	}

	/**
	 * Causes the system hardware to emit a short sound (if it supports this
	 * capability).
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 */
	public void beep() {
		Util.logNotImplemented();
	}

	/**
	 * Requests that the connection between SWT and the underlying operating
	 * system be closed.
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 * 
	 * @see Device#dispose
	 * 
	 * @since 2.0
	 */
	public void close() {
		// TODO
	}

	static Monitor createMonitor(Screen screen) {
		Monitor monitor = new Monitor();

		Rectangle2D bounds = screen.getBounds();
		monitor.x = (int)bounds.getMinX();
		monitor.y = (int)bounds.getMinY();
		monitor.width = (int)bounds.getWidth();
		monitor.height = (int)bounds.getHeight();
		
		Rectangle2D vbounds = screen.getVisualBounds();
		monitor.clientX = (int)vbounds.getMinX();
		monitor.clientY = (int)vbounds.getMinY();
		monitor.clientWidth = (int)vbounds.getWidth();
		monitor.clientHeight = (int)vbounds.getHeight();
		
		return monitor;
	}
	
	@Override
	public void dispose() {
		if( disposeList != null ) {
			for( Runnable r : disposeList ) {
				r.run();
			}
		}
		super.dispose();
	}

	/**
	 * Causes the <code>run()</code> method of the runnable to be invoked by the
	 * user-interface thread just before the receiver is disposed. Specifying a
	 * <code>null</code> runnable is ignored.
	 * 
	 * @param runnable
	 *            code to run at dispose time.
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 */
	public void disposeExec(Runnable runnable) {
		if( disposeList == null ) {
			disposeList = new ArrayList<>();
		}
		disposeList.add(runnable);
	}

	public Object enterNestedEventLoop(Object key) {
		return Toolkit.getToolkit().enterNestedEventLoop(key);
	}
	
	public void exitNestedEventLoop(Object key, Object rval) {
		Toolkit.getToolkit().exitNestedEventLoop(key, rval);
	}
	
	boolean filterEvent (Event event) {
		if (filterTable != null) filterTable.sendEvent (event);
		return false;
	}

	boolean filters (int eventType) {
		if (filterTable == null) return false;
		return filterTable.hooks (eventType);
	}

	/**
	 * Returns the display which the given thread is the user-interface thread
	 * for, or null if the given thread is not a user-interface thread for any
	 * display. Specifying <code>null</code> as the thread will return
	 * <code>null</code> for the display.
	 * 
	 * @param thread
	 *            the user-interface thread
	 * @return the display for the given thread
	 */
	public static Display findDisplay(Thread thread) {
		if( DEFAULT == null ) {
			return null;
		}
		
		if( DEFAULT.thread == thread ) {
			return DEFAULT;
		}
		return null;
	}

	private Control findControl(Composite parent, int x, int y) {
		for( Control c : parent.getChildren() ) {
			if( c.getBounds().contains(c.toControl(x, y)) ) {
				Control rv = findControl(parent, x, y);
				if( rv != null ) {
					return rv;
				}
				return c;
			}
		}
		return null;
	}
	
	/**
	 * Given the operating system handle for a widget, returns the instance of
	 * the <code>Widget</code> subclass which represents it in the currently
	 * running application, if such exists, or null if no matching widget can be
	 * found.
	 * <p>
	 * <b>IMPORTANT:</b> This method should not be called from application code.
	 * The arguments are platform-specific.
	 * </p>
	 * 
	 * @param handle
	 *            the handle for the widget
	 * @return the SWT widget that the handle represents
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 * 
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public Widget findWidget(long /* int */handle) {
		// TODO
		return null;
	}

	/**
	 * Given the operating system handle for a widget, and widget-specific id,
	 * returns the instance of the <code>Widget</code> subclass which represents
	 * the handle/id pair in the currently running application, if such exists,
	 * or null if no matching widget can be found.
	 * <p>
	 * <b>IMPORTANT:</b> This method should not be called from application code.
	 * The arguments are platform-specific.
	 * </p>
	 * 
	 * @param handle
	 *            the handle for the widget
	 * @param id
	 *            the id for the subwidget (usually an item)
	 * @return the SWT widget that the handle/id pair represents
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 * 
	 * @noreference This method is not intended to be referenced by clients.
	 * 
	 * @since 3.1
	 */
	public Widget findWidget(long /* int */handle, long /* int */id) {
		// TODO
		return null;
	}

	/**
	 * Given a widget and a widget-specific id, returns the instance of the
	 * <code>Widget</code> subclass which represents the widget/id pair in the
	 * currently running application, if such exists, or null if no matching
	 * widget can be found.
	 * 
	 * @param widget
	 *            the widget
	 * @param id
	 *            the id for the subwidget (usually an item)
	 * @return the SWT subwidget (usually an item) that the widget/id pair
	 *         represents
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 * 
	 * @noreference This method is not intended to be referenced by clients.
	 * 
	 * @since 3.3
	 */
	public Widget findWidget(Widget widget, long /* int */id) {
		// TODO
		return null;
	}

	private static KeyCode fromSWT(Event evt) {
		if( evt.character != 0 ) {
			KeyCode keyCode = KeyCode.getKeyCode(Character.toUpperCase(evt.character)+"");
			if( keyCode == null ) {
				switch (evt.character) {
				case ' ':
					keyCode = KeyCode.SPACE;
					break;
				case '.':
					keyCode = KeyCode.PERIOD;
					break;
				default:
					break;
				}
			}
			
			if( keyCode == null ) {
				System.err.println("Unable to convert: " + evt.character);
			}
			
			return keyCode == null ? KeyCode.UNDEFINED : keyCode;
		} else if( evt.keyCode != 0 ) {
			switch (evt.keyCode) {
			case SWT.SHIFT:
				return KeyCode.SHIFT;
			case SWT.CTRL:
				return KeyCode.CONTROL;
			case SWT.COMMAND:
				return KeyCode.COMMAND;
			case SWT.ALT:
				return KeyCode.ALT;
			default:
				break;
			}
		}
		
		return KeyCode.UNDEFINED;
	}

	/**
	 * Returns the currently active <code>Shell</code>, or null if no shell
	 * belonging to the currently running application is active.
	 * 
	 * @return the active shell or null
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 */
	public Shell getActiveShell() {
		for( Shell s : shells ) {
			if( s.stage.isFocused() ) {
				return s;
			}
		}
		return null;
	}

	/**
	 * Returns the application name.
	 * 
	 * @return the application name
	 * 
	 * @see #setAppName(String)
	 * 
	 * @since 3.6
	 */
	public static String getAppName() {
		// TODO
		return null;
	}

	/**
	 * Returns the application version.
	 * 
	 * @return the application version
	 * 
	 * @see #setAppVersion(String)
	 * 
	 * @since 3.6
	 */
	public static String getAppVersion() {
		// TODO
		return null;
	}

	public Rectangle getBounds () {
		double minX = 0;
		double minY = 0;
		double width = 0;
		double height = 0;
		for( Screen s : Screen.getScreens() ) {
			minX = Math.min(s.getBounds().getMinX(), minX);
			minY = Math.min(s.getBounds().getMinY(), minY);
			width += s.getBounds().getWidth();
			height += s.getBounds().getHeight();
		}
		return new Rectangle((int)minX, (int)minY, (int)width, (int)height);
	}
	
	public Rectangle getClientArea() {
		double minX = 0;
		double minY = 0;
		double width = 0;
		double height = 0;
		for( Screen s : Screen.getScreens() ) {
			minX = Math.min(s.getVisualBounds().getMinX(), minX);
			minY = Math.min(s.getVisualBounds().getMinY(), minY);
			width += s.getVisualBounds().getWidth();
			height += s.getVisualBounds().getHeight();
		}
		return new Rectangle((int)minX, (int)minY, (int)width, (int)height);
	}
	
	/**
	 * Returns the display which the currently running thread is the
	 * user-interface thread for, or null if the currently running thread is not
	 * a user-interface thread for any display.
	 * 
	 * @return the current display
	 */
	public static Display getCurrent() {
		return Platform.isFxApplicationThread() ? getDefault() : null;
	}

	/**
	 * Returns the control which the on-screen pointer is currently over top of,
	 * or null if it is not currently over one of the controls built by the
	 * currently running application.
	 * 
	 * @return the control under the cursor or <code>null</code>
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 */
	public Control getCursorControl() {
		for (Shell s : shells) {
			if( s.stage.isFocused() ) {
				Point p = getCursorLocation();
				if( s.getBounds().contains(p.x, p.y) ) {
					return findControl(s, p.x, p.y);
				}
				return null;
			}
		}
		return null;
	}

	/**
	 * Returns the location of the on-screen pointer relative to the top left
	 * corner of the screen.
	 * 
	 * @return the cursor location
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 */
	public Point getCursorLocation() {
		Robot r = null;
		try {
			r = com.sun.glass.ui.Application.GetApplication().createRobot();
			return new Point(r.getMouseX(), r.getMouseY());
		} finally {
			if( r != null) {
				r.destroy();
			}
		}
	}

	/**
	 * Returns an array containing the recommended cursor sizes.
	 * 
	 * @return the array of cursor sizes
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 * 
	 * @since 3.0
	 */
	public Point[] getCursorSizes() {
		// TODO
		return null;
	}

	/**
	 * Returns the application defined, display specific data associated with
	 * the receiver, or null if it has not been set. The
	 * <em>display specific data</em> is a single, unnamed field that is stored
	 * with every display.
	 * <p>
	 * Applications may put arbitrary objects in this field. If the object
	 * stored in the display specific data needs to be notified when the display
	 * is disposed of, it is the application's responsibility to provide a
	 * <code>disposeExec()</code> handler which does so.
	 * </p>
	 * 
	 * @return the display specific data
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 * 
	 * @see #setData(Object)
	 * @see #disposeExec(Runnable)
	 */
	public Object getData() {
		return (state & KEYED_DATA) != 0 ? ((Object []) data) [0] : data;
	}

	/**
	 * Returns the application defined property of the receiver with the
	 * specified name, or null if it has not been set.
	 * <p>
	 * Applications may have associated arbitrary objects with the receiver in
	 * this fashion. If the objects stored in the properties need to be notified
	 * when the display is disposed of, it is the application's responsibility
	 * to provide a <code>disposeExec()</code> handler which does so.
	 * </p>
	 * 
	 * @param key
	 *            the name of the property
	 * @return the value of the property or null if it has not been set
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the key is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 * 
	 * @see #setData(String, Object)
	 * @see #disposeExec(Runnable)
	 */
	public Object getData(String key) {
		if (key == null) SWT.error (SWT.ERROR_NULL_ARGUMENT);
		if ((state & KEYED_DATA) != 0) {
			Object [] table = (Object []) data;
			for (int i=1; i<table.length; i+=2) {
				if (key.equals (table [i])) return table [i+1];
			}
		}
		return null;
	}

	/**
	 * Returns the default display. One is created (making the thread that
	 * invokes this method its user-interface thread) if it did not already
	 * exist.
	 * 
	 * @return the default display
	 */
	public static Display getDefault() {
		if( DEFAULT == null ) {
			DEFAULT = new Display();
		}
		return DEFAULT;
	}

	@Override
	public int getDepth() {
		Util.logNotImplemented();
		return 32;
	}
	
	/**
	 * Returns the button dismissal alignment, one of <code>LEFT</code> or
	 * <code>RIGHT</code>. The button dismissal alignment is the ordering that
	 * should be used when positioning the default dismissal button for a
	 * dialog. For example, in a dialog that contains an OK and CANCEL button,
	 * on platforms where the button dismissal alignment is <code>LEFT</code>,
	 * the button ordering should be OK/CANCEL. When button dismissal alignment
	 * is <code>RIGHT</code>, the button ordering should be CANCEL/OK.
	 * 
	 * @return the button dismissal order
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 * 
	 * @since 2.1
	 */
	public int getDismissalAlignment() {
		return SWT.LEFT; //TODO Should we check of OS-X and return RIGHT??
	}

	/**
	 * Returns the longest duration, in milliseconds, between two mouse button
	 * clicks that will be considered a <em>double click</em> by the underlying
	 * operating system.
	 * 
	 * @return the double click time
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 */
	public int getDoubleClickTime() {
		return 500; //TODO Can we read this from the OS???
	}

	@Override
	public Point getDPI() {
		return new Point((int)Screen.getPrimary().getDpi(),(int)Screen.getPrimary().getDpi());
	}
	
	/**
	 * Returns the control which currently has keyboard focus, or null if
	 * keyboard events are not currently going to any of the controls built by
	 * the currently running application.
	 * 
	 * @return the focus control or <code>null</code>
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 */
	public Control getFocusControl() {
		return focusControl;
	}

	/**
	 * Returns true when the high contrast mode is enabled. Otherwise, false is
	 * returned.
	 * <p>
	 * Note: This operation is a hint and is not supported on platforms that do
	 * not have this concept.
	 * </p>
	 * 
	 * @return the high contrast mode
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 * 
	 * @since 3.0
	 */
	public boolean getHighContrast() {
		Util.logNotImplemented();
		return false;
	}

	/**
	 * Returns the maximum allowed depth of icons on this display, in bits per
	 * pixel. On some platforms, this may be different than the actual depth of
	 * the display.
	 * 
	 * @return the maximum icon depth
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 * 
	 * @see Device#getDepth
	 */
	public int getIconDepth() {
		// TODO
		return 0;
	}

	/**
	 * Returns an array containing the recommended icon sizes.
	 * 
	 * @return the array of icon sizes
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 * 
	 * @see Decorations#setImages(Image[])
	 * 
	 * @since 3.0
	 */
	public Point[] getIconSizes() {
		// TODO
		return null;
	}

	public int getLastEventTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * Returns the single instance of the application menu bar, or
	 * <code>null</code> if there is no application menu bar for the platform.
	 * 
	 * @return the application menu bar, or <code>null</code>
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 * 
	 * @since 3.7
	 */
	public Menu getMenuBar() {
		// TODO
		return null;
	}

	/**
	 * Returns an array of monitors attached to the device.
	 * 
	 * @return the array of monitors
	 * 
	 * @since 3.0
	 */
	public Monitor[] getMonitors() {
		ObservableList<Screen> screens = Screen.getScreens();
		Monitor[] rv = new Monitor[screens.size()];
		for( int i = 0; i < rv.length; i++ ) {
			rv[i] = createMonitor(screens.get(i));
		}
		return rv;
	}

	/**
	 * Returns the primary monitor for that device.
	 * 
	 * @return the primary monitor
	 * 
	 * @since 3.0
	 */
	public Monitor getPrimaryMonitor() {
		return createMonitor(Screen.getPrimary());
	}

	/**
	 * Returns a (possibly empty) array containing all shells which have not
	 * been disposed and have the receiver as their display.
	 * 
	 * @return the receiver's shells
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 */
	public Shell[] getShells() {
		return shells.toArray(new Shell[shells.size()]);
	}

	/**
	 * Gets the synchronizer used by the display.
	 * 
	 * @return the receiver's synchronizer
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 * 
	 * @since 3.4
	 */
	public Synchronizer getSynchronizer() {
		// TODO
		return null;
	}

	/**
	 * Returns the thread that has invoked <code>syncExec</code> or null if no
	 * such runnable is currently being invoked by the user-interface thread.
	 * <p>
	 * Note: If a runnable invoked by asyncExec is currently running, this
	 * method will return null.
	 * </p>
	 * 
	 * @return the receiver's sync-interface thread
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 */
	public Thread getSyncThread() {
		// TODO
		return null;
	}

	@Override
	public Color getSystemColor(int id) {
		Color color = getWidgetColor (id);
		if (color != null) {
			return color;
		}
		return super.getSystemColor(id);
	}
	
	/**
	 * Returns the matching standard platform cursor for the given constant,
	 * which should be one of the cursor constants specified in class
	 * <code>SWT</code>. This cursor should not be free'd because it was
	 * allocated by the system, not the application. A value of
	 * <code>null</code> will be returned if the supplied constant is not an SWT
	 * cursor constant.
	 * 
	 * @param id
	 *            the SWT cursor constant
	 * @return the corresponding cursor or <code>null</code>
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 * 
	 * @see SWT#CURSOR_ARROW
	 * @see SWT#CURSOR_WAIT
	 * @see SWT#CURSOR_CROSS
	 * @see SWT#CURSOR_APPSTARTING
	 * @see SWT#CURSOR_HELP
	 * @see SWT#CURSOR_SIZEALL
	 * @see SWT#CURSOR_SIZENESW
	 * @see SWT#CURSOR_SIZENS
	 * @see SWT#CURSOR_SIZENWSE
	 * @see SWT#CURSOR_SIZEWE
	 * @see SWT#CURSOR_SIZEN
	 * @see SWT#CURSOR_SIZES
	 * @see SWT#CURSOR_SIZEE
	 * @see SWT#CURSOR_SIZEW
	 * @see SWT#CURSOR_SIZENE
	 * @see SWT#CURSOR_SIZESE
	 * @see SWT#CURSOR_SIZESW
	 * @see SWT#CURSOR_SIZENW
	 * @see SWT#CURSOR_UPARROW
	 * @see SWT#CURSOR_IBEAM
	 * @see SWT#CURSOR_NO
	 * @see SWT#CURSOR_HAND
	 * 
	 * @since 3.0
	 */
	public Cursor getSystemCursor(int id) {
		if( id < cursors.length ) {
			if( cursors[id] == null ) {
				cursors[id] = new Cursor(this, id);
			}
			return cursors[id];
		}
		return null;
	}

	/**
	 * Returns the matching standard platform image for the given constant,
	 * which should be one of the icon constants specified in class
	 * <code>SWT</code>. This image should not be free'd because it was
	 * allocated by the system, not the application. A value of
	 * <code>null</code> will be returned either if the supplied constant is not
	 * an SWT icon constant or if the platform does not define an image that
	 * corresponds to the constant.
	 * 
	 * @param id
	 *            the SWT icon constant
	 * @return the corresponding image or <code>null</code>
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 * 
	 * @see SWT#ICON_ERROR
	 * @see SWT#ICON_INFORMATION
	 * @see SWT#ICON_QUESTION
	 * @see SWT#ICON_WARNING
	 * @see SWT#ICON_WORKING
	 * 
	 * @since 3.0
	 */
	public Image getSystemImage (int id) {
		switch (id) {
		case SWT.ICON_ERROR:
			if( systemImages[0] == null ) {
				try(InputStream in = getClass().getResourceAsStream("dialog-error.png")) {
					systemImages[0] = new Image(this, in);	
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
			return systemImages[0];
		case SWT.ICON_INFORMATION:
			if( systemImages[1] == null ) {
				try(InputStream in = getClass().getResourceAsStream("dialog-information.png")) {
					systemImages[1] = new Image(this, in);	
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
			return systemImages[1];
		case SWT.ICON_QUESTION:
			if( systemImages[2] == null ) {
				try(InputStream in = getClass().getResourceAsStream("help-contents.png")) {
					systemImages[2] = new Image(this, in);	
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
			return systemImages[2];
		case SWT.ICON_WORKING:
			if( systemImages[3] == null ) {
				try(InputStream in = getClass().getResourceAsStream("user-away-extended.png")) {
					systemImages[3] = new Image(this, in);	
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
			return systemImages[3];
		case SWT.ICON_WARNING:
			if( systemImages[4] == null ) {
				try(InputStream in = getClass().getResourceAsStream("dialog-warning.png")) {
					systemImages[4] = new Image(this, in);	
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
			return systemImages[4];
		default:
			break;
		}
		return null;
	}

	/**
	 * Returns the single instance of the system-provided menu for the
	 * application, or <code>null</code> on platforms where no menu is provided
	 * for the application.
	 * 
	 * @return the system menu, or <code>null</code>
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 * 
	 * @since 3.7
	 */
	public Menu getSystemMenu() {
		// TODO
		return null;
	}

	/**
	 * Returns the single instance of the system taskBar or null when there is
	 * no system taskBar available for the platform.
	 * 
	 * @return the system taskBar or <code>null</code>
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 * 
	 * @since 3.6
	 */
	public TaskBar getSystemTaskBar() {
		// TODO
		return null;
	}

	/**
	 * Returns the single instance of the system tray or null when there is no
	 * system tray available for the platform.
	 * 
	 * @return the system tray or <code>null</code>
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 * 
	 * @since 3.0
	 */
	public Tray getSystemTray() {
		Util.logNotImplemented();
		return null;
	}

	/**
	 * Returns the user-interface thread for the receiver.
	 * 
	 * @return the receiver's user-interface thread
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 */
	public Thread getThread() {
		return thread;
	}

	/**
	 * Returns a boolean indicating whether a touch-aware input device is
	 * attached to the system and is ready for use.
	 * 
	 * @return <code>true</code> if a touch-aware input device is detected, or
	 *         <code>false</code> otherwise
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 * 
	 * @since 3.7
	 */
	public boolean getTouchEnabled() {
		// TODO
		return false;
	}

	Color getWidgetColor (int id) {
		if (0 <= id && id < widgetColors.length && widgetColors [id] != null) {
			return widgetColors[id];
		}
		return null;
	}
	
	private void initColors() {
		widgetColors[SWT.COLOR_INFO_FOREGROUND] = new Color(this, 0, 0, 0);
		widgetColors[SWT.COLOR_INFO_BACKGROUND] = new Color(this, 250, 251, 197);
		widgetColors[SWT.COLOR_TITLE_FOREGROUND] = new Color(this, 0, 0, 0);
		widgetColors[SWT.COLOR_TITLE_BACKGROUND] = new Color(this, 56, 117, 215);
		widgetColors[SWT.COLOR_TITLE_BACKGROUND_GRADIENT] = new Color(this, 180, 213, 255);
		widgetColors[SWT.COLOR_TITLE_INACTIVE_FOREGROUND] = new Color(this, 127, 127, 127);
		widgetColors[SWT.COLOR_TITLE_INACTIVE_BACKGROUND] = new Color(this, 212, 212, 212);
		widgetColors[SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT] = new Color(this, 212, 212, 212);
		widgetColors[SWT.COLOR_WIDGET_DARK_SHADOW] = new Color(this, 0, 0, 0);
		widgetColors[SWT.COLOR_WIDGET_NORMAL_SHADOW] = new Color(this, 159, 159, 159);
		widgetColors[SWT.COLOR_WIDGET_LIGHT_SHADOW] = new Color(this, 232, 232, 232);
		widgetColors[SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW] = new Color(this, 254, 255, 254);
		widgetColors[SWT.COLOR_WIDGET_BACKGROUND] = new Color(this, 232, 232, 232);
		widgetColors[SWT.COLOR_WIDGET_FOREGROUND] = new Color(this, 0, 0, 0);
		widgetColors[SWT.COLOR_WIDGET_BORDER] = new Color(this, 0, 0, 0);
		widgetColors[SWT.COLOR_LIST_FOREGROUND] = new Color(this, 0, 0, 0);
		widgetColors[SWT.COLOR_LIST_BACKGROUND] = new Color(this, 255, 255, 255);
		widgetColors[SWT.COLOR_LIST_SELECTION_TEXT] = new Color(this, 0, 0, 0);
		widgetColors[SWT.COLOR_LIST_SELECTION] = new Color(this, 180, 213, 255);
		widgetColors[SWT.COLOR_LINK_FOREGROUND] = new Color(this, 0, 0, 255);
	}

	/**
	 * Invokes platform specific functionality to dispose a GC handle.
	 * <p>
	 * <b>IMPORTANT:</b> This method is <em>not</em> part of the public API for
	 * <code>Display</code>. It is marked public only so that it can be shared
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
	@Override
	public void internal_dispose_GC(long /* int */hDC, GCData data) {
		// TODO
	}

	/**
	 * Invokes platform specific functionality to allocate a new GC handle.
	 * <p>
	 * <b>IMPORTANT:</b> This method is <em>not</em> part of the public API for
	 * <code>Display</code>. It is marked public only so that it can be shared
	 * within the packages provided by SWT. It is not available on all
	 * platforms, and should never be called from application code.
	 * </p>
	 * 
	 * @param data
	 *            the platform specific GC data
	 * @return the platform specific GC handle
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 * @exception SWTError
	 *                <ul>
	 *                <li>ERROR_NO_HANDLES if a handle could not be obtained for
	 *                gc creation</li>
	 *                </ul>
	 * 
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Override
	public long /* int */internal_new_GC(GCData data) {
		// TODO
		return 0;
	}

	static <T> boolean isValidClass(Class<T> cls) {
		return true;
	}
	
	boolean isValidThread() {
		return Platform.isFxApplicationThread();
	}

	/**
	 * Maps a point from one coordinate system to another. When the control is
	 * null, coordinates are mapped to the display.
	 * <p>
	 * NOTE: On right-to-left platforms where the coordinate systems are
	 * mirrored, special care needs to be taken when mapping coordinates from
	 * one control to another to ensure the result is correctly mirrored.
	 * 
	 * Mapping a point that is the origin of a rectangle and then adding the
	 * width and height is not equivalent to mapping the rectangle. When one
	 * control is mirrored and the other is not, adding the width and height to
	 * a point that was mapped causes the rectangle to extend in the wrong
	 * direction. Mapping the entire rectangle instead of just one point causes
	 * both the origin and the corner of the rectangle to be mapped.
	 * </p>
	 * 
	 * @param from
	 *            the source <code>Control</code> or <code>null</code>
	 * @param to
	 *            the destination <code>Control</code> or <code>null</code>
	 * @param point
	 *            to be mapped
	 * @return point with mapped coordinates
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the point is null</li>
	 *                <li>ERROR_INVALID_ARGUMENT - if the Control from or the
	 *                Control to have been disposed</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 * 
	 * @since 2.1.2
	 */
	public Point map(Control from, Control to, Point point) {
		return map(from, to, point.x, point.y);
	}

	/**
	 * Maps a point from one coordinate system to another. When the control is
	 * null, coordinates are mapped to the display.
	 * <p>
	 * NOTE: On right-to-left platforms where the coordinate systems are
	 * mirrored, special care needs to be taken when mapping coordinates from
	 * one control to another to ensure the result is correctly mirrored.
	 * 
	 * Mapping a point that is the origin of a rectangle and then adding the
	 * width and height is not equivalent to mapping the rectangle. When one
	 * control is mirrored and the other is not, adding the width and height to
	 * a point that was mapped causes the rectangle to extend in the wrong
	 * direction. Mapping the entire rectangle instead of just one point causes
	 * both the origin and the corner of the rectangle to be mapped.
	 * </p>
	 * 
	 * @param from
	 *            the source <code>Control</code> or <code>null</code>
	 * @param to
	 *            the destination <code>Control</code> or <code>null</code>
	 * @param x
	 *            coordinates to be mapped
	 * @param y
	 *            coordinates to be mapped
	 * @return point with mapped coordinates
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_ARGUMENT - if the Control from or the
	 *                Control to have been disposed</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 * 
	 * @since 2.1.2
	 */
	public Point map(Control from, Control to, int x, int y) {
		if( from == to ) {
			return new Point(x, y);
		}
		
		if( from == null ) {
			Point2D localToScreen = to.nativeControl.screenToLocal(x, y);
			return new Point((int)localToScreen.getX(), (int)localToScreen.getY());
		}
		
		// TabFolder elements not yet attached to the control through
		// the TabItem
		Node node = from.nativeControl;
		if( node.getScene() == null ) {
			Control c = from.getParent();
			do {
				if( c instanceof TabFolder ) {
					break;
				}
			} while( (c = c.getParent()) != null );
			
			if( c instanceof TabFolder ) {
				System.err.println("Warning: TabFolder child calculation before attached to TabFolder");
				if( c.nativeControl instanceof TabPane ) {
					TabPane p = (TabPane) c.nativeControl;
//					TabPaneSkin s = (TabPaneSkin) p.getSkin();
//					s.getChildren();
//					FIXME This is not 100% correct
					node = p;
				}
			}
		}

		Point2D localToScreen = node.localToScreen(x, y);
		if( to == null ) {
			return new Point((int)localToScreen.getX(), (int)localToScreen.getY());
		} else {
			Point2D sceneToLocal = to.nativeControl.screenToLocal(localToScreen);
			return new Point((int)sceneToLocal.getX(), (int)sceneToLocal.getY());
		}
	}

	/**
	 * Maps a point from one coordinate system to another. When the control is
	 * null, coordinates are mapped to the display.
	 * <p>
	 * NOTE: On right-to-left platforms where the coordinate systems are
	 * mirrored, special care needs to be taken when mapping coordinates from
	 * one control to another to ensure the result is correctly mirrored.
	 * 
	 * Mapping a point that is the origin of a rectangle and then adding the
	 * width and height is not equivalent to mapping the rectangle. When one
	 * control is mirrored and the other is not, adding the width and height to
	 * a point that was mapped causes the rectangle to extend in the wrong
	 * direction. Mapping the entire rectangle instead of just one point causes
	 * both the origin and the corner of the rectangle to be mapped.
	 * </p>
	 * 
	 * @param from
	 *            the source <code>Control</code> or <code>null</code>
	 * @param to
	 *            the destination <code>Control</code> or <code>null</code>
	 * @param rectangle
	 *            to be mapped
	 * @return rectangle with mapped coordinates
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the rectangle is null</li>
	 *                <li>ERROR_INVALID_ARGUMENT - if the Control from or the
	 *                Control to have been disposed</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 * 
	 * @since 2.1.2
	 */
	public Rectangle map(Control from, Control to, Rectangle rectangle) {
		return map(from, to, rectangle.x, rectangle.y, rectangle.width, rectangle.height); 
	}

	/**
	 * Maps a point from one coordinate system to another. When the control is
	 * null, coordinates are mapped to the display.
	 * <p>
	 * NOTE: On right-to-left platforms where the coordinate systems are
	 * mirrored, special care needs to be taken when mapping coordinates from
	 * one control to another to ensure the result is correctly mirrored.
	 * 
	 * Mapping a point that is the origin of a rectangle and then adding the
	 * width and height is not equivalent to mapping the rectangle. When one
	 * control is mirrored and the other is not, adding the width and height to
	 * a point that was mapped causes the rectangle to extend in the wrong
	 * direction. Mapping the entire rectangle instead of just one point causes
	 * both the origin and the corner of the rectangle to be mapped.
	 * </p>
	 * 
	 * @param from
	 *            the source <code>Control</code> or <code>null</code>
	 * @param to
	 *            the destination <code>Control</code> or <code>null</code>
	 * @param x
	 *            coordinates to be mapped
	 * @param y
	 *            coordinates to be mapped
	 * @param width
	 *            coordinates to be mapped
	 * @param height
	 *            coordinates to be mapped
	 * @return rectangle with mapped coordinates
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_ARGUMENT - if the Control from or the
	 *                Control to have been disposed</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 * 
	 * @since 2.1.2
	 */
	public Rectangle map(Control from, Control to, int x, int y, int width,
			int height) {
		Point p = map(from, to, x, y);
		return new Rectangle(p.x, p.y, width, height);
	}

	/**
	 * Generate a low level system event.
	 * 
	 * <code>post</code> is used to generate low level keyboard and mouse
	 * events. The intent is to enable automated UI testing by simulating the
	 * input from the user. Most SWT applications should never need to call this
	 * method.
	 * <p>
	 * Note that this operation can fail when the operating system fails to
	 * generate the event for any reason. For example, this can happen when
	 * there is no such key or mouse button or when the system event queue is
	 * full.
	 * </p>
	 * <p>
	 * <b>Event Types:</b>
	 * <p>
	 * KeyDown, KeyUp
	 * <p>
	 * The following fields in the <code>Event</code> apply:
	 * <ul>
	 * <li>(in) type KeyDown or KeyUp</li>
	 * <p>
	 * Either one of:
	 * <li>(in) character a character that corresponds to a keyboard key</li>
	 * <li>(in) keyCode the key code of the key that was typed, as defined by
	 * the key code constants in class <code>SWT</code></li>
	 * </ul>
	 * <p>
	 * MouseDown, MouseUp
	 * </p>
	 * <p>
	 * The following fields in the <code>Event</code> apply:
	 * <ul>
	 * <li>(in) type MouseDown or MouseUp
	 * <li>(in) button the button that is pressed or released
	 * </ul>
	 * <p>
	 * MouseMove
	 * </p>
	 * <p>
	 * The following fields in the <code>Event</code> apply:
	 * <ul>
	 * <li>(in) type MouseMove
	 * <li>(in) x the x coordinate to move the mouse pointer to in screen
	 * coordinates
	 * <li>(in) y the y coordinate to move the mouse pointer to in screen
	 * coordinates
	 * </ul>
	 * <p>
	 * MouseWheel
	 * </p>
	 * <p>
	 * The following fields in the <code>Event</code> apply:
	 * <ul>
	 * <li>(in) type MouseWheel
	 * <li>(in) detail either SWT.SCROLL_LINE or SWT.SCROLL_PAGE
	 * <li>(in) count the number of lines or pages to scroll
	 * </ul>
	 * </dl>
	 * 
	 * @param event
	 *            the event to be generated
	 * 
	 * @return true if the event was generated or false otherwise
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the event is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 * 
	 * @since 3.0
	 * 
	 */
	public boolean post(Event event) {
		Robot robot = com.sun.glass.ui.Application.GetApplication().createRobot();
		switch (event.type) {
		case SWT.MouseMove:
			robot.mouseMove(event.x, event.y);
			break;
		case SWT.MouseDown:
			robot.mousePress(event.button); 
			break;
		case SWT.MouseUp:
			robot.mouseRelease(event.button);
			break;
		case SWT.KeyDown:
			robot.keyPress(fromSWT(event).impl_getCode());
			break;
		case SWT.KeyUp:
			robot.keyRelease(fromSWT(event).impl_getCode());
			break;
		default:
			break;
		}
		
		robot.destroy();

		return false;
	}

	void postEvent (Event event) {
		// TODO
	}

	/**
	 * Reads an event from the operating system's event queue, dispatches it
	 * appropriately, and returns <code>true</code> if there is potentially more
	 * work to do, or <code>false</code> if the caller can sleep until another
	 * event is placed on the event queue.
	 * <p>
	 * In addition to checking the system event queue, this method also checks
	 * if any inter-thread messages (created by <code>syncExec()</code> or
	 * <code>asyncExec()</code>) are waiting to be processed, and if so handles
	 * them before returning.
	 * </p>
	 * 
	 * @return <code>false</code> if the caller can sleep upon return from this
	 *         method
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_FAILED_EXEC - if an exception occurred while
	 *                running an inter-thread message</li>
	 *                </ul>
	 * 
	 * @see #sleep
	 * @see #wake
	 */
	public boolean readAndDispatch() {
		return false;
	}

	void registerShell(Shell shell) {
		shells.add(shell);
	}
	
	/**
	 * Removes the listener from the collection of listeners who will be
	 * notified when an event of the given type occurs anywhere in a widget. The
	 * event type is one of the event constants defined in class
	 * <code>SWT</code>.
	 * 
	 * @param eventType
	 *            the type of event to listen for
	 * @param listener
	 *            the listener which should no longer be notified when the event
	 *            occurs
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @see Listener
	 * @see SWT
	 * @see #addFilter
	 * @see #addListener
	 * 
	 * @since 3.0
	 */
	public void removeFilter(int eventType, Listener listener) {
		if (filterTable != null) 
			filterTable.unhook(eventType, listener);
	}

	/**
	 * Removes the listener from the collection of listeners who will be
	 * notified when an event of the given type occurs. The event type is one of
	 * the event constants defined in class <code>SWT</code>.
	 * 
	 * @param eventType
	 *            the type of event to listen for
	 * @param listener
	 *            the listener which should no longer be notified
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 * 
	 * @see Listener
	 * @see SWT
	 * @see #addListener
	 * 
	 * @since 2.0
	 */
	public void removeListener(int eventType, Listener listener) {
		Util.logNotImplemented();
	}

	void removeShell(Shell shell) {
		shells.remove(shell);
	}

	void runSkin() {
		// TODO Auto-generated method stub
	}
	
	/*
	 * Not SWT API, needed to inject events from the workbench
	 */
	public void sendEvent (int eventType, Event event) {
		if (eventTable == null && filterTable == null) {
			return;
		}
		if (event == null) event = new Event ();
		event.display = this;
		event.type = eventType;
		if (event.time == 0) event.time = (int)(System.currentTimeMillis() / 1000);
		if (!filterEvent (event)) {
			if (eventTable != null) sendEvent(eventTable, event);
		}
	}

	void sendEvent(EventTable eventTable, Event event) {
		sendPreEvent(event);
		try {
			eventTable.sendEvent (event);
		} finally {
			sendPostEvent(event);
		}
	}

	void sendPreEvent(Event event) {
		if (event == null || (event.type != SWT.PreEvent && event.type != SWT.PostEvent)) {
			if (this.eventTable != null && this.eventTable.hooks(SWT.PreEvent)) {
				sendEvent(SWT.PreEvent, null);
			}
		}
	}

	void sendPostEvent(Event event) {
		if (event == null || (event.type != SWT.PreEvent && event.type != SWT.PostEvent)) {
			if (this.eventTable != null && this.eventTable.hooks(SWT.PostEvent)) {
				sendEvent(SWT.PostEvent, null);
			}
		}
	}

	/**
	 * Sets the application name to the argument.
	 * <p>
	 * The application name can be used in several ways, depending on the
	 * platform and tools being used. On Motif, for example, this can be used to
	 * set the name used for resource lookup. Accessibility tools may also ask
	 * for the application name.
	 * </p>
	 * <p>
	 * Specifying <code>null</code> for the name clears it.
	 * </p>
	 * 
	 * @param name
	 *            the new app name or <code>null</code>
	 */
	public static void setAppName(String name) {
		if (DEFAULT != null)
			com.sun.glass.ui.Application.GetApplication().setName(name);
		else
			appName = name;
	}

	/**
	 * Sets the application version to the argument.
	 * 
	 * @param version
	 *            the new app version
	 * 
	 * @since 3.6
	 */
	public static void setAppVersion(String version) {
		// TODO
	}

	/**
	 * Sets the location of the on-screen pointer relative to the top left
	 * corner of the screen. <b>Note: It is typically considered bad practice
	 * for a program to move the on-screen pointer location.</b>
	 * 
	 * @param x
	 *            the new x coordinate for the cursor
	 * @param y
	 *            the new y coordinate for the cursor
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 * 
	 * @since 2.1
	 */
	public void setCursorLocation(int x, int y) {
		Robot r = null;
		try {
			r = com.sun.glass.ui.Application.GetApplication().createRobot();
			r.mouseMove(x, y);
		} finally {
			if( r != null) {
				r.destroy();
			}
		}
	}

	/**
	 * Sets the location of the on-screen pointer relative to the top left
	 * corner of the screen. <b>Note: It is typically considered bad practice
	 * for a program to move the on-screen pointer location.</b>
	 * 
	 * @param point
	 *            new position
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                <li>ERROR_NULL_ARGUMENT - if the point is null
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 * 
	 * @since 2.0
	 */
	public void setCursorLocation(Point point) {
	}

	/**
	 * Sets the application defined, display specific data associated with the
	 * receiver, to the argument. The <em>display specific data</em> is a
	 * single, unnamed field that is stored with every display.
	 * <p>
	 * Applications may put arbitrary objects in this field. If the object
	 * stored in the display specific data needs to be notified when the display
	 * is disposed of, it is the application's responsibility provide a
	 * <code>disposeExec()</code> handler which does so.
	 * </p>
	 * 
	 * @param data
	 *            the new display specific data
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 * 
	 * @see #getData()
	 * @see #disposeExec(Runnable)
	 */
	public void setData(Object data) {
		if ((state & KEYED_DATA) != 0) {
			((Object []) this.data) [0] = data;
		} else {
			this.data = data;
		}
	}

	/**
	 * Sets the application defined property of the receiver with the specified
	 * name to the given argument.
	 * <p>
	 * Applications may have associated arbitrary objects with the receiver in
	 * this fashion. If the objects stored in the properties need to be notified
	 * when the display is disposed of, it is the application's responsibility
	 * provide a <code>disposeExec()</code> handler which does so.
	 * </p>
	 * 
	 * @param key
	 *            the name of the property
	 * @param value
	 *            the new value for the property
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the key is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 * 
	 * @see #getData(String)
	 * @see #disposeExec(Runnable)
	 */
	public void setData(String key, Object value) {
		if (key == null) SWT.error (SWT.ERROR_NULL_ARGUMENT);
		int index = 1;
		Object [] table = null;
		if ((state & KEYED_DATA) != 0) {
			table = (Object []) data;
			while (index < table.length) {
				if (key.equals (table [index])) break;
				index += 2;
			}
		}
		if (value != null) {
			if ((state & KEYED_DATA) != 0) {
				if (index == table.length) {
					Object [] newTable = new Object [table.length + 2];
					System.arraycopy (table, 0, newTable, 0, table.length);
					data = table = newTable;
				}
			} else {
				table = new Object [3];
				table [0] = data;
				data = table;
				state |= KEYED_DATA;
			}
			table [index] = key;
			table [index + 1] = value;
		} else {
			if ((state & KEYED_DATA) != 0) {
				if (index != table.length) {
					int length = table.length - 2;
					if (length == 1) {
						data = table [0];
						state &= ~KEYED_DATA;
					} else {
						Object [] newTable = new Object [length];
						System.arraycopy (table, 0, newTable, 0, index);
						System.arraycopy (table, index + 2, newTable, index, length - index);
						data = newTable;
					}
				}
			}
		}
	}

	void setFocusControl(Control focusControl) {
		if( this.focusControl != focusControl ) {
			Control oldFocusControl = this.focusControl;
			this.focusControl = focusControl;
			
			if( oldFocusControl != null ) {
				oldFocusControl.sendEvent(SWT.FocusOut, new Event(), true);
			}
			if( this.focusControl != null ) {
				this.focusControl.sendEvent(SWT.FocusIn, new Event(), true);
			}
		}
	}
	
	void setHoverControl(Control hoverControl) {
		this.hoverControl = hoverControl;
		hoverTimer.stop();
		hoverTimer.playFromStart();
	}

	/**
	 * Sets the synchronizer used by the display to be the argument, which can
	 * not be null.
	 * 
	 * @param synchronizer
	 *            the new synchronizer for the display (must not be null)
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the synchronizer is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_FAILED_EXEC - if an exception occurred while
	 *                running an inter-thread message</li>
	 *                </ul>
	 */
	public void setSynchronizer(Synchronizer synchronizer) {
		Util.logNotImplemented();
	}

	public void setWarnings(boolean b) {
		Util.logNotImplemented();
	}

	/**
	 * Causes the user-interface thread to <em>sleep</em> (that is, to be put in
	 * a state where it does not consume CPU cycles) until an event is received
	 * or it is otherwise awakened.
	 * 
	 * @return <code>true</code> if an event requiring dispatching was placed on
	 *         the queue.
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 * 
	 * @see #wake
	 */
	public boolean sleep() {
		// Not supported on JavaFX
		throw new UnsupportedOperationException();
	}

	/**
	 * Causes the <code>run()</code> method of the runnable to be invoked by the
	 * user-interface thread at the next reasonable opportunity. The thread
	 * which calls this method is suspended until the runnable completes.
	 * Specifying <code>null</code> as the runnable simply wakes the
	 * user-interface thread.
	 * <p>
	 * Note that at the time the runnable is invoked, widgets that have the
	 * receiver as their display may have been disposed. Therefore, it is
	 * necessary to check for this case inside the runnable before accessing the
	 * widget.
	 * </p>
	 * 
	 * @param runnable
	 *            code to run on the user-interface thread or <code>null</code>
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_FAILED_EXEC - if an exception occurred when
	 *                executing the runnable</li>
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 * 
	 * @see #asyncExec
	 */
	public void syncExec(final Runnable runnable) {
		if( isValidThread() ) {
			runnable.run();
		} else {
			final CountDownLatch c = new CountDownLatch(1);
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					try {
						runnable.run();	
					} finally {
						c.countDown();	
					}
				}
			});
			try {
				c.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private Timer timer() {
		if( timer == null ) {
			timer = new Timer(true);
		}
		return timer;
	}

	/**
	 * Causes the <code>run()</code> method of the runnable to be invoked by the
	 * user-interface thread after the specified number of milliseconds have
	 * elapsed. If milliseconds is less than zero, the runnable is not executed.
	 * <p>
	 * Note that at the time the runnable is invoked, widgets that have the
	 * receiver as their display may have been disposed. Therefore, it is
	 * necessary to check for this case inside the runnable before accessing the
	 * widget.
	 * </p>
	 * 
	 * @param milliseconds
	 *            the delay before running the runnable
	 * @param runnable
	 *            code to run on the user-interface thread
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the runnable is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 * 
	 * @see #asyncExec
	 */
	public void timerExec(int milliseconds, Runnable runnable) {
		if( milliseconds < 0 ) {
			DisplayTimerTask[] tasks = currentTasks.toArray(new DisplayTimerTask[0]);
			for( DisplayTimerTask t : tasks ) {
				if( t.r == runnable ) {
					t.cancel();
					currentTasks.remove(t);
				}
			}
		} else {
			timer().schedule(new DisplayTimerTask(currentTasks,runnable), milliseconds);	
		}
	}

	/**
	 * Forces all outstanding paint requests for the display to be processed
	 * before this method returns.
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 * 
	 * @see Control#update()
	 */
	public void update() {
		Util.logNotImplemented();
	}

	void unregisterShell(Shell shell) {
		shells.remove(shell);
	}
	
	/**
	 * If the receiver's user-interface thread was <code>sleep</code>ing, causes
	 * it to be awakened and start running again. Note that this method may be
	 * called from any thread.
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_DEVICE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 * 
	 * @see #sleep
	 */
	public void wake() {
		Util.logNotImplemented();
		new Exception().printStackTrace();
	}

	void wakeThread() {
		Util.logNotImplemented();
	}

}

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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.graphics.Point;

/**
 * Instances of this class are user interface objects that contain menu items.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>BAR, DROP_DOWN, POP_UP, NO_RADIO_GROUP</dd>
 * <dd>LEFT_TO_RIGHT, RIGHT_TO_LEFT</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Help, Hide, Show</dd>
 * </dl>
 * <p>
 * Note: Only one of BAR, DROP_DOWN and POP_UP may be specified. Only one of
 * LEFT_TO_RIGHT or RIGHT_TO_LEFT may be specified.
 * </p>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 * 
 * @see <a href="http://www.eclipse.org/swt/snippets/#menu">Menu snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example:
 *      ControlExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further
 *      information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class Menu extends Widget {

	MenuBar bar;
	ContextMenu contextMenu;
	javafx.scene.control.Menu menu;
	ToggleGroup toggleGroup;
	
	private static EventHandler<javafx.event.Event> contextMenuShowingHandler;
	private static EventHandler<javafx.event.Event> menuShowingHandler;
	private static ChangeListener<Toggle> toggleChangeListener;
	
	private Decorations parent;
	private MenuItem cascade;
	private java.util.List<MenuItem> items = new ArrayList<>();
	private MenuItem defaultItem;
	
	/**
	 * Constructs a new instance of this class given its parent, and sets the
	 * style for the instance so that the instance will be a popup menu on the
	 * given parent's shell.
	 * <p>
	 * After constructing a menu, it can be set into its parent using
	 * <code>parent.setMenu(menu)</code>. In this case, the parent may be any
	 * control in the same widget tree as the parent.
	 * </p>
	 * 
	 * @param parent
	 *            a control which will be the parent of the new instance (cannot
	 *            be null)
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
	 * @see SWT#POP_UP
	 * @see Widget#checkSubclass
	 * @see Widget#getStyle
	 */
	public Menu(Control parent) {
		// TODO menuShell or do we not need this?
		this(parent.getShell(), SWT.POP_UP);
	}

	/**
	 * Constructs a new instance of this class given its parent (which must be a
	 * <code>Decorations</code>) and a style value describing its behavior and
	 * appearance.
	 * <p>
	 * The style value is either one of the style constants defined in class
	 * <code>SWT</code> which is applicable to instances of this class, or must
	 * be built by <em>bitwise OR</em>'ing together (that is, using the
	 * <code>int</code> "|" operator) two or more of those <code>SWT</code>
	 * style constants. The class description lists the style constants that are
	 * applicable to the class. Style bits are also inherited from superclasses.
	 * </p>
	 * <p>
	 * After constructing a menu or menuBar, it can be set into its parent using
	 * <code>parent.setMenu(menu)</code> or
	 * <code>parent.setMenuBar(menuBar)</code>.
	 * </p>
	 * 
	 * @param parent
	 *            a decorations control which will be the parent of the new
	 *            instance (cannot be null)
	 * @param style
	 *            the style of menu to construct
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
	 * @see SWT#BAR
	 * @see SWT#DROP_DOWN
	 * @see SWT#POP_UP
	 * @see SWT#NO_RADIO_GROUP
	 * @see SWT#LEFT_TO_RIGHT
	 * @see SWT#RIGHT_TO_LEFT
	 * @see Widget#checkSubclass
	 * @see Widget#getStyle
	 */
	public Menu(Decorations parent, int style) {
		checkSubclass ();
		checkParent (parent);
		this.style = style;
		if (parent != null) {
			display = parent.display;
		} else {
			display = Display.getCurrent ();
			if (display == null) display = Display.getDefault ();
			if (!display.isValidThread ()) {
				error (SWT.ERROR_THREAD_INVALID_ACCESS);
			}
		}
		this.parent = parent;
		reskinWidget();
		createWidget();
	}

	/**
	 * Constructs a new instance of this class given its parent (which must be a
	 * <code>Menu</code>) and sets the style for the instance so that the
	 * instance will be a drop-down menu on the given parent's parent.
	 * <p>
	 * After constructing a drop-down menu, it can be set into its parentMenu
	 * using <code>parentMenu.setMenu(menu)</code>.
	 * </p>
	 * 
	 * @param parentMenu
	 *            a menu which will be the parent of the new instance (cannot be
	 *            null)
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
	public Menu(Menu parentMenu) {
		this(parentMenu.getParent(), SWT.DROP_DOWN);
	}

	/**
	 * Constructs a new instance of this class given its parent (which must be a
	 * <code>MenuItem</code>) and sets the style for the instance so that the
	 * instance will be a drop-down menu on the given parent's parent menu.
	 * <p>
	 * After constructing a drop-down menu, it can be set into its parentItem
	 * using <code>parentItem.setMenu(menu)</code>.
	 * </p>
	 * 
	 * @param parentItem
	 *            a menu item which will be the parent of the new instance
	 *            (cannot be null)
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
	public Menu(MenuItem parentItem) {
		this(parentItem.getParent().getParent(), 0);
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

	void addItem(MenuItem item) {
		items.add(item);
		if (item.getNativeObject() != null) {
			if ((item.getStyle() & SWT.RADIO) == SWT.RADIO) {
				if (toggleGroup == null) {
					toggleGroup = new ToggleGroup();
					toggleGroup.selectedToggleProperty().addListener(getSelectedChangeListener());
				}
				toggleGroup.getToggles().add((Toggle)item.getNativeObject());
			}
			if (menu != null) {
				menu.getItems().add(item.getNativeObject());
			} else if( contextMenu != null ) {
				contextMenu.getItems().add(item.getNativeObject());
			}
		}
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified
	 * when menus are hidden or shown, by sending it one of the messages defined
	 * in the <code>MenuListener</code> interface.
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
	 * @see MenuListener
	 * @see #removeMenuListener
	 */
	public void addMenuListener(MenuListener listener) {
		checkWidget ();
		if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
		TypedListener typedListener = new TypedListener (listener);
		addListener (SWT.Hide,typedListener);
		addListener (SWT.Show,typedListener);
	}

	void checkParent (Widget parent) {
		// TODO allow null parent for now
		if (parent != null) {
			if (parent.isDisposed()) error (SWT.ERROR_INVALID_ARGUMENT);
			parent.checkWidget();
		}
	}

	@Override
	void createNativeObject() {
		if( (style & SWT.BAR) == SWT.BAR ) {
			bar = new MenuBar();
		} else if( (style & SWT.POP_UP) == SWT.POP_UP ) {
			contextMenu = new ContextMenu();
			contextMenu.addEventHandler(javafx.scene.control.Menu.ON_SHOWING, getContextMenuShowingHandler());
			contextMenu.addEventHandler(javafx.scene.control.Menu.ON_SHOWING, getContextMenuShowingHandler());
		} else if( (style & SWT.DROP_DOWN) == SWT.DROP_DOWN ) {
			menu = new javafx.scene.control.Menu();
			menu.addEventHandler(javafx.scene.control.Menu.ON_SHOWING, getMenuShowingHandler());
			menu.addEventHandler(javafx.scene.control.Menu.ON_HIDDEN, getMenuShowingHandler());
		}
	}
	
	private static EventHandler<javafx.event.Event> getContextMenuShowingHandler() {
		if (contextMenuShowingHandler == null) {
			contextMenuShowingHandler = new EventHandler<javafx.event.Event>() {
				@Override
				public void handle(javafx.event.Event event) {
					Control c = Display.getDefault().getControl(event.getSource());
					c.sendEvent(event.getEventType() == javafx.scene.control.Menu.ON_SHOWING ? SWT.Show : SWT.Hide, new org.eclipse.swt.widgets.Event(), true);
				}
			};
		}
		return contextMenuShowingHandler;
	}

	private static EventHandler<javafx.event.Event> getMenuShowingHandler() {
		if (menuShowingHandler == null) {
			menuShowingHandler = new EventHandler<javafx.event.Event>() {
				@Override
				public void handle(javafx.event.Event event) {
					Control c = Display.getDefault().getControl(event.getSource());
					c.sendEvent(event.getEventType() == javafx.scene.control.Menu.ON_SHOWING ? SWT.Show : SWT.Hide, new org.eclipse.swt.widgets.Event(), true);
				}
			};
		}
		return menuShowingHandler;
	}

	/**
	 * Returns the default menu item or null if none has been previously set.
	 * 
	 * @return the default menu item.
	 * 
	 *         </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public MenuItem getDefaultItem() {
		checkWidget();
		return defaultItem;
	}

	/**
	 * Returns <code>true</code> if the receiver is enabled, and
	 * <code>false</code> otherwise. A disabled menu is typically not selectable
	 * from the user interface and draws with an inactive or "grayed" look.
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
		if (bar != null) {
			return ! bar.isDisable();
		} else if (menu != null) {
			return ! menu.isDisable();
		} else if (contextMenu != null) {
			return false;
		}
		return false;
	}

	/**
	 * Returns the item at the given, zero-relative index in the receiver.
	 * Throws an exception if the index is out of range.
	 * 
	 * @param index
	 *            the index of the item to return
	 * @return the item at the given index
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_RANGE - if the index is not between 0
	 *                and the number of elements in the list minus 1 (inclusive)
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
	public MenuItem getItem(int index) {
		checkWidget();
		return items.get(index);
	}

	/**
	 * Returns the number of items contained in the receiver.
	 * 
	 * @return the number of items
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public int getItemCount() {
		checkWidget();
		return items.size();
	}

	/**
	 * Returns a (possibly empty) array of <code>MenuItem</code>s which are the
	 * items in the receiver.
	 * <p>
	 * Note: This is not the actual structure used by the receiver to maintain
	 * its list of items, so modifying the array will not affect the receiver.
	 * </p>
	 * 
	 * @return the items in the receiver
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public MenuItem[] getItems() {
		checkWidget();
		return items.toArray(new MenuItem[items.size()]);
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
		// TODO
		return 0;
	}

	/**
	 * Returns the receiver's parent, which must be a <code>Decorations</code>.
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
	public Decorations getParent() {
		checkWidget();
		return parent;
	}

	/**
	 * Returns the receiver's parent item, which must be a <code>MenuItem</code>
	 * or null when the receiver is a root.
	 * 
	 * @return the receiver's parent item
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public MenuItem getParentItem() {
		checkWidget();
		return cascade;
	}

	/**
	 * Returns the receiver's parent item, which must be a <code>Menu</code> or
	 * null when the receiver is a root.
	 * 
	 * @return the receiver's parent item
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public Menu getParentMenu() {
		checkWidget();
		if (cascade != null) return cascade.parent;
		return null;
	}

	private static ChangeListener<Toggle> getSelectedChangeListener() {
		if (toggleChangeListener == null) {
			toggleChangeListener = new ChangeListener<Toggle>() {
				@Override
				public void changed(ObservableValue<? extends Toggle> observable,
						Toggle oldValue, Toggle newValue) {
					if (oldValue != null) {
						org.eclipse.swt.widgets.Event evt = new org.eclipse.swt.widgets.Event();
						Display.getDefault().getControl(oldValue).sendEvent(SWT.Selection, evt, true);
					}
					if (newValue != null) {
						org.eclipse.swt.widgets.Event evt = new org.eclipse.swt.widgets.Event();
						Display.getDefault().getControl(newValue).sendEvent(SWT.Selection, evt, true);
					}
				}
			};
		}
		return toggleChangeListener;
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
		if (cascade.parent != null) {
			return cascade.parent.getShell();
		} else if (parent != null ) {
			return parent.getShell();
		}
		return null;
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
		if (menu != null) {
			return menu.isVisible();
		} else if (contextMenu != null) {
			return contextMenu.isShowing();
		} else {
			return bar.isVisible();
		}
	}

	/**
	 * Searches the receiver's list starting at the first item (index 0) until
	 * an item is found that is equal to the argument, and returns the index of
	 * that item. If no item is found, returns -1.
	 * 
	 * @param item
	 *            the search item
	 * @return the index of the item
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the item is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public int indexOf(MenuItem item) {
		checkWidget();
		return items.indexOf(item);
	}

	/**
	 * Returns <code>true</code> if the receiver is enabled and all of the
	 * receiver's ancestors are enabled, and <code>false</code> otherwise. A
	 * disabled menu is typically not selectable from the user interface and
	 * draws with an inactive or "grayed" look.
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
		return menu != null ? ! menu.isDisable() : true;
	}

	/**
	 * Returns <code>true</code> if the receiver is visible and all of the
	 * receiver's ancestors are visible and <code>false</code> otherwise.
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
		checkWidget();
		if (cascade.parent != null) {
			return getVisible() && cascade.parent.isVisible();
		} else if (parent != null) {
			return getVisible() && parent.isVisible();
		}
		return getVisible();
	}

	/**
	 * Removes the listener from the collection of listeners who will be
	 * notified when the menu events are generated for the control.
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
	 * @see MenuListener
	 * @see #addMenuListener
	 */
	public void removeMenuListener(MenuListener listener) {
		checkWidget ();
		if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
		if (eventTable == null) return;
		eventTable.unhook (SWT.Hide, listener);
		eventTable.unhook (SWT.Show, listener);
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
		checkWidget ();
		if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
		if (eventTable == null) return;
		eventTable.unhook (SWT.Help, listener);
	}

	void removeItem(MenuItem item) {
		items.remove(item);
		if (item.getNativeObject() != null ) {
			if ((item.getStyle() & SWT.RADIO) != 0) {
				if (toggleGroup != null) {
					toggleGroup.getToggles().remove(item.getNativeObject());
				}
			}
			if (menu != null) {
				menu.getItems().remove(item.getNativeObject());	
			} else {
				contextMenu.getItems().remove(item.getNativeObject());
			}
		}
	}
	/**
	 * Sets the default menu item to the argument or removes the default
	 * emphasis when the argument is <code>null</code>.
	 * 
	 * @param item
	 *            the default menu item or null
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_ARGUMENT - if the menu item has been
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
	public void setDefaultItem(MenuItem item) {
		defaultItem = item;
		// TODO something with it
	}

	/**
	 * Enables the receiver if the argument is <code>true</code>, and disables
	 * it otherwise. A disabled menu is typically not selectable from the user
	 * interface and draws with an inactive or "grayed" look.
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
		if (menu != null) {
			menu.setDisable(!enabled);
		}
	}

	/**
	 * Sets the location of the receiver, which must be a popup, to the point
	 * specified by the arguments which are relative to the display.
	 * <p>
	 * Note that this is different from most widgets where the location of the
	 * widget is relative to the parent.
	 * </p>
	 * <p>
	 * Note that the platform window manager ultimately has control over the
	 * location of popup menus.
	 * </p>
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
		if (contextMenu != null) {
			contextMenu.setX(x);
			contextMenu.setY(y);
		}
	}

	/**
	 * Sets the location of the receiver, which must be a popup, to the point
	 * specified by the argument which is relative to the display.
	 * <p>
	 * Note that this is different from most widgets where the location of the
	 * widget is relative to the parent.
	 * </p>
	 * <p>
	 * Note that the platform window manager ultimately has control over the
	 * location of popup menus.
	 * </p>
	 * 
	 * @param location
	 *            the new location for the receiver
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
	 * @since 2.1
	 */
	public void setLocation(Point location) {
		setLocation(location.x, location.y);
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
		if (contextMenu != null) {
			// TODO
//			contextMenu.show(((Shell)decoration).internal_getWindow());
		}
	}

}

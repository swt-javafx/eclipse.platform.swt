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

import java.util.ArrayList;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCharacterCombination;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ArmListener;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.internal.Util;

/**
 * Instances of this class represent a selectable user interface object that
 * issues notification when pressed and released.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>CHECK, CASCADE, PUSH, RADIO, SEPARATOR</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Arm, Help, Selection</dd>
 * </dl>
 * <p>
 * Note: Only one of the styles CHECK, CASCADE, PUSH, RADIO and SEPARATOR may be
 * specified.
 * </p>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 * 
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further
 *      information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class MenuItem extends Item {

	private Menu parent;
	private javafx.scene.control.MenuItem nativeItem;
	private Menu menu;
	private int accelerator;
	private static EventHandler<ActionEvent> SELECTION_HANDLER;
	
	/**
	 * Constructs a new instance of this class given its parent (which must be a
	 * <code>Menu</code>) and a style value describing its behavior and
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
	 *            a menu control which will be the parent of the new instance
	 *            (cannot be null)
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
	 * @see SWT#CHECK
	 * @see SWT#CASCADE
	 * @see SWT#PUSH
	 * @see SWT#RADIO
	 * @see SWT#SEPARATOR
	 * @see Widget#checkSubclass
	 * @see Widget#getStyle
	 */
	public MenuItem(Menu parent, int style) {
		super(parent, style);
		this.parent = parent;
		parent.internal_addItem(this);
		armInit();
	}

	/**
	 * Constructs a new instance of this class given its parent (which must be a
	 * <code>Menu</code>), a style value describing its behavior and appearance,
	 * and the index at which to place it in the items maintained by its parent.
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
	 *            a menu control which will be the parent of the new instance
	 *            (cannot be null)
	 * @param style
	 *            the style of control to construct
	 * @param index
	 *            the zero-relative index to store the receiver in its parent
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
	 * @see SWT#CHECK
	 * @see SWT#CASCADE
	 * @see SWT#PUSH
	 * @see SWT#RADIO
	 * @see SWT#SEPARATOR
	 * @see Widget#checkSubclass
	 * @see Widget#getStyle
	 */
	public MenuItem(Menu parent, int style, int index) {
		super(parent, style);
		this.parent = parent;
		parent.internal_addItem(this,index);
		armInit();
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified
	 * when the arm events are generated for the control, by sending it one of
	 * the messages defined in the <code>ArmListener</code> interface.
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
	 * @see ArmListener
	 * @see #removeArmListener
	 */
	public void addArmListener(ArmListener listener) {
		if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
		TypedListener typedListener = new TypedListener (listener);
		addListener (SWT.Arm, typedListener);
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified
	 * when the help events are generated for the control, by sending it one of
	 * the messages defined in the <code>HelpListener</code> interface.
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
	 * when the menu item is selected by the user, by sending it one of the
	 * messages defined in the <code>SelectionListener</code> interface.
	 * <p>
	 * When <code>widgetSelected</code> is called, the stateMask field of the
	 * event object is valid. <code>widgetDefaultSelected</code> is not called.
	 * </p>
	 * <p>
	 * When the <code>SWT.RADIO</code> style bit is set, the
	 * <code>widgetSelected</code> method is also called when the receiver loses
	 * selection because another item in the same radio group was selected by
	 * the user. During <code>widgetSelected</code> the application can use
	 * <code>getSelection()</code> to determine the current selected state of
	 * the receiver.
	 * </p>
	 * 
	 * @param listener
	 *            the listener which should be notified when the menu item is
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
		checkWidget ();
		if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
		TypedListener typedListener = new TypedListener(listener);
		addListener (SWT.Selection,typedListener);
		addListener (SWT.DefaultSelection,typedListener);
	}

	private void armInit() {
		EventHandler<javafx.event.Event> eventHandler = new EventHandler<javafx.event.Event>() {
			private boolean initDone;
			@Override
			public void handle(javafx.event.Event event) {
				if( initDone ) {
					return;
				}
				initDone = true;
				final Node node = internal_getNativeObject().impl_styleableGetNode();
				if( node != null ) {
					node.focusedProperty().addListener(new InvalidationListener() {
						@Override
						public void invalidated(Observable observable) {
							if( node.isFocused() ) {
								internal_sendEvent(SWT.Arm, new org.eclipse.swt.widgets.Event(), true);	
							}
						}
					});
				}
			}
		};
		
		if( this.parent.internal_getNativeObject() instanceof javafx.scene.control.Menu ) {
			((javafx.scene.control.Menu)this.parent.internal_getNativeObject()).addEventHandler(javafx.scene.control.Menu.ON_SHOWN,eventHandler);			
		} else if( this.parent.internal_getNativeObject() instanceof ContextMenu ) {
			((ContextMenu)this.parent.internal_getNativeObject()).addEventHandler(javafx.scene.control.Menu.ON_SHOWN,eventHandler);
		}
	}

	@Override
	protected Object createWidget() {
		// don't create an item when style is cascade
		if( (style & SWT.PUSH) == SWT.PUSH ) {
			nativeItem = new javafx.scene.control.MenuItem();
		} else if( (style & SWT.CHECK) == SWT.CHECK ) {
			nativeItem = new CheckMenuItem();
		} else if( (style & SWT.RADIO) == SWT.RADIO ) {
			nativeItem = new RadioMenuItem("");
		} else if( (style & SWT.SEPARATOR) == SWT.SEPARATOR ) {
			nativeItem = new SeparatorMenuItem();
		} else {
			nativeItem = new javafx.scene.control.Menu("Placeholder");
		}
		return nativeItem;
	}
	
	@Override
	public void dispose() {
		parent.internal_removeItem(this);
		super.dispose();
	}
	
	static KeyCode keyChar (int key) {
		//TODO - use the NS key constants
		switch (key) {
			case SWT.BS: return KeyCode.BACK_SPACE;
			case SWT.CR: return KeyCode.ENTER; //TODO is the correct??
			case SWT.DEL: return KeyCode.DELETE;
			case SWT.ESC: return KeyCode.ESCAPE;
			case SWT.LF: return KeyCode.ENTER; //TODO is the correct??
			case SWT.TAB: return KeyCode.TAB;
//			case ' ': return OS.kMenuBlankGlyph;
//			case ' ': return OS.kMenuSpaceGlyph;		
			case SWT.ALT: return KeyCode.ALT;
			case SWT.SHIFT: return KeyCode.SHIFT;
			case SWT.CONTROL: return KeyCode.CONTROL;
			case SWT.COMMAND: return KeyCode.COMMAND;
			case SWT.ARROW_UP: return KeyCode.UP;
			case SWT.ARROW_DOWN: return KeyCode.DOWN;
			case SWT.ARROW_LEFT: return KeyCode.LEFT;
			case SWT.ARROW_RIGHT: return KeyCode.RIGHT;
			case SWT.PAGE_UP: return KeyCode.PAGE_UP;
			case SWT.PAGE_DOWN: return KeyCode.PAGE_DOWN;
			case SWT.KEYPAD_CR: return KeyCode.ENTER;
			case SWT.HELP: return KeyCode.HELP;
			case SWT.HOME: return KeyCode.HOME;
			case SWT.END: return KeyCode.END;
//			case SWT.CAPS_LOCK: return ??;
			case SWT.F1: return KeyCode.F1;
			case SWT.F2: return KeyCode.F2;
			case SWT.F3: return KeyCode.F3;
			case SWT.F4: return KeyCode.F4;
			case SWT.F5: return KeyCode.F5;
			case SWT.F6: return KeyCode.F6;
			case SWT.F7: return KeyCode.F7;
			case SWT.F8: return KeyCode.F8;
			case SWT.F9: return KeyCode.F9;
			case SWT.F10: return KeyCode.F10;
			case SWT.F11: return KeyCode.F11;
			case SWT.F12: return KeyCode.F12;
			case SWT.F13: return KeyCode.F13;
			case SWT.F14: return KeyCode.F14;
			case SWT.F15: return KeyCode.F15;
			/*
			* The following lines are intentionally commented.
			*/
//			case SWT.INSERT: return ??;
		}
		return null;
	}
	
	/**
	 * Returns the widget accelerator. An accelerator is the bit-wise OR of zero
	 * or more modifier masks and a key. Examples:
	 * <code>SWT.CONTROL | SWT.SHIFT | 'T', SWT.ALT | SWT.F2</code>. The default
	 * value is zero, indicating that the menu item does not have an
	 * accelerator.
	 * 
	 * @return the accelerator or 0
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
	public int getAccelerator() {
		return accelerator;
	}

	/**
	 * Returns <code>true</code> if the receiver is enabled, and
	 * <code>false</code> otherwise. A disabled menu item is typically not
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
		return (state & DISABLED) == 0;
	}

	/**
	 * Gets the identifier associated with the receiver.
	 * 
	 * @return the receiver's identifier
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
	public int getID() {
		// TODO
		return 0;
	}

	/**
	 * Returns the receiver's cascade menu if it has one or null if it does not.
	 * Only <code>CASCADE</code> menu items can have a pull down menu. The
	 * sequence of key strokes, button presses and/or button releases that are
	 * used to request a pull down menu is platform specific.
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
		return menu;
	}

	/**
	 * Returns the receiver's parent, which must be a <code>Menu</code>.
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
	public Menu getParent() {
		return parent;
	}

	/**
	 * Returns <code>true</code> if the receiver is selected, and false
	 * otherwise.
	 * <p>
	 * When the receiver is of type <code>CHECK</code> or <code>RADIO</code>, it
	 * is selected when it is checked.
	 * 
	 * @return the selection state
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li> <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public boolean getSelection() {
		if( nativeItem instanceof CheckMenuItem ) {
			return ((CheckMenuItem) nativeItem).isSelected();
		} else if( nativeItem instanceof RadioMenuItem ) {
			return ((RadioMenuItem) nativeItem).isSelected();
		}
		return false;
	}

	private static EventHandler<ActionEvent> getSelectionHandler() {
		if( SELECTION_HANDLER == null ) {
			SELECTION_HANDLER = new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					// TODO Work around for JavaFX bug https://javafx-jira.kenai.com/browse/RT-35401 
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							org.eclipse.swt.widgets.Event evt = new org.eclipse.swt.widgets.Event();
							MenuItem item = Widget.getWidget(event.getSource());
							if( item != null ) {
								item.internal_sendEvent(SWT.Selection, evt, true);
							}
						}
					});
				}
			};
		}
		return SELECTION_HANDLER;
	}
	
	@Override
	protected void initListeners() {
		super.initListeners();
		if( nativeItem != null ) {
			// Radio events are generated by the toggle-group in the menu
			if( (style & SWT.RADIO) != SWT.RADIO ) {
				nativeItem.setOnAction(getSelectionHandler());	
			}
		}
	}
	
	@Override
	public javafx.scene.control.MenuItem internal_getNativeObject() {
		return nativeItem;
	}
	
	/**
	 * Returns <code>true</code> if the receiver is enabled and all of the
	 * receiver's ancestors are enabled, and <code>false</code> otherwise. A
	 * disabled menu item is typically not selectable from the user interface
	 * and draws with an inactive or "grayed" look.
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
		// TODO
		return false;
	}

	/**
	 * Removes the listener from the collection of listeners who will be
	 * notified when the arm events are generated for the control.
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
	 * @see ArmListener
	 * @see #addArmListener
	 */
	public void removeArmListener(ArmListener listener) {
		// TODO
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
		// TODO
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
		// TODO
	}

	/**
	 * Sets the widget accelerator. An accelerator is the bit-wise OR of zero or
	 * more modifier masks and a key. Examples:
	 * <code>SWT.MOD1 | SWT.MOD2 | 'T', SWT.MOD3 | SWT.F2</code>.
	 * <code>SWT.CONTROL | SWT.SHIFT | 'T', SWT.ALT | SWT.F2</code>. The default
	 * value is zero, indicating that the menu item does not have an
	 * accelerator.
	 * 
	 * @param accelerator
	 *            an integer that is the bit-wise OR of masks and a key
	 * 
	 *            </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setAccelerator(int accelerator) {
		checkWidget();
		this.accelerator = accelerator;
		if( nativeItem != null ) {
			if( accelerator != 0 ) {
				int key = accelerator & SWT.KEY_MASK;
				KeyCode k = keyChar(key);
				
				java.util.List<KeyCombination.Modifier> l = new ArrayList<>();
				if( (accelerator & SWT.SHIFT) != 0 ) {
					l.add(KeyCombination.SHIFT_DOWN);
				}
				
				if( (accelerator & SWT.CONTROL) != 0 ) {
					l.add(KeyCombination.CONTROL_DOWN);
				}
				
				if( (accelerator & SWT.ALT) != 0 ) {
					l.add(KeyCombination.ALT_DOWN);
				}
				
				if( (accelerator & SWT.COMMAND) != 0 ) {
					l.add(KeyCombination.META_DOWN);
				}
								
				if( k == null ) {
					for( KeyCode c : KeyCode.values() ) {
						if( c.impl_getCode() == key ) {
							k = c;
							break;
						}
					}
				}
				
				KeyCombination kc;
				if( k != null ) {
					kc = new KeyCodeCombination(k,l.toArray(new KeyCombination.Modifier[0]));
				} else {
					kc = new KeyCharacterCombination(String.valueOf((char)key),l.toArray(new KeyCombination.Modifier[0]));
				}
				
				nativeItem.setAccelerator(kc);
			} else {
				nativeItem.setAccelerator(null);
			}
		}
	}

	/**
	 * Enables the receiver if the argument is <code>true</code>, and disables
	 * it otherwise. A disabled menu item is typically not selectable from the
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
		state |= DISABLED;
		if( nativeItem != null ) {
			nativeItem.setDisable(!enabled);
		}
	}

	/**
	 * Sets the identifier associated with the receiver to the argument.
	 * 
	 * @param id
	 *            the new identifier. This must be a non-negative value.
	 *            System-defined identifiers are negative values.
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                <li>ERROR_INVALID_ARGUMENT - if called with an
	 *                negative-valued argument.</li>
	 *                </ul>
	 * 
	 * @since 3.7
	 */
	public void setID(int id) {
		// TODO
	}

	/**
	 * Sets the image the receiver will display to the argument.
	 * <p>
	 * Note: This operation is a hint and is not supported on platforms that do
	 * not have this concept (for example, Windows NT). Furthermore, some
	 * platforms (such as GTK), cannot display both a check box and an image at
	 * the same time. Instead, they hide the image and display the check box.
	 * </p>
	 * 
	 * @param image
	 *            the image to display
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	@Override
	public void setImage(Image image) {
		super.setImage(image);
		if( nativeItem != null ) {
			nativeItem.setGraphic(image == null ? null : new ImageView(image.internal_getImage()));	
		}		
	}

	/**
	 * Sets the receiver's pull down menu to the argument. Only
	 * <code>CASCADE</code> menu items can have a pull down menu. The sequence
	 * of key strokes, button presses and/or button releases that are used to
	 * request a pull down menu is platform specific.
	 * <p>
	 * Note: Disposing of a menu item that has a pull down menu will dispose of
	 * the menu. To avoid this behavior, set the menu to null before the menu
	 * item is disposed.
	 * </p>
	 * 
	 * @param menu
	 *            the new pull down menu
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_MENU_NOT_DROP_DOWN - if the menu is not a drop
	 *                down menu</li>
	 *                <li>ERROR_MENUITEM_NOT_CASCADE - if the menu item is not a
	 *                <code>CASCADE</code></li>
	 *                <li>ERROR_INVALID_ARGUMENT - if the menu has been disposed
	 *                </li>
	 *                <li>ERROR_INVALID_PARENT - if the menu is not in the same
	 *                widget tree</li>
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
		this.menu.setParentItem(this);
		parent.internal_menuAttached(this,menu);
		nativeItem = (javafx.scene.control.Menu) menu.internal_getNativeObject();
		
		setText(getText());
		setImage(getImage());
		setEnabled(getEnabled());
		setAccelerator(getAccelerator());
	}

	/**
	 * Sets the selection state of the receiver.
	 * <p>
	 * When the receiver is of type <code>CHECK</code> or <code>RADIO</code>, it
	 * is selected when it is checked.
	 * 
	 * @param selected
	 *            the new selection state
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li> <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public void setSelection(boolean selected) {
		if( nativeItem != null)  {
			if(nativeItem instanceof CheckMenuItem) {
				((CheckMenuItem)nativeItem).setSelected(selected);
			} else if( nativeItem instanceof RadioMenuItem ) {
				((RadioMenuItem)nativeItem).setSelected(selected);
			}
		}
	}

	/**
	 * Sets the receiver's text. The string may include the mnemonic character
	 * and accelerator text.
	 * <p>
	 * Mnemonics are indicated by an '&amp;' that causes the next character to
	 * be the mnemonic. When the user presses a key sequence that matches the
	 * mnemonic, a selection event occurs. On most platforms, the mnemonic
	 * appears underlined but may be emphasised in a platform specific manner.
	 * The mnemonic indicator character '&amp;' can be escaped by doubling it in
	 * the string, causing a single '&amp;' to be displayed.
	 * </p>
	 * <p>
	 * Accelerator text is indicated by the '\t' character. On platforms that
	 * support accelerator text, the text that follows the '\t' character is
	 * displayed to the user, typically indicating the key stroke that will
	 * cause the item to become selected. On most platforms, the accelerator
	 * text appears right aligned in the menu. Setting the accelerator text does
	 * not install the accelerator key sequence. The accelerator key sequence is
	 * installed using #setAccelerator.
	 * </p>
	 * 
	 * @param string
	 *            the new text
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the text is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @see #setAccelerator
	 */
	@Override
	public void setText(String string) {
		super.setText(string);
		if( nativeItem != null ) {
			nativeItem.setText(Util.fixAccelerator(Util.fixMnemonic(string)));	
		}
	}

}

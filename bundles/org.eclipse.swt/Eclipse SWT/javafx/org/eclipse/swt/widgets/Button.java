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

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;

/**
 * Instances of this class represent a selectable user interface object that
 * issues notification when pressed and released.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>ARROW, CHECK, PUSH, RADIO, TOGGLE, FLAT, WRAP</dd>
 * <dd>UP, DOWN, LEFT, RIGHT, CENTER</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection</dd>
 * </dl>
 * <p>
 * Note: Only one of the styles ARROW, CHECK, PUSH, RADIO, and TOGGLE may be
 * specified.
 * </p>
 * <p>
 * Note: Only one of the styles LEFT, RIGHT, and CENTER may be specified.
 * </p>
 * <p>
 * Note: Only one of the styles UP, DOWN, LEFT, and RIGHT may be specified when
 * the ARROW style is specified.
 * </p>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 * 
 * @see <a href="http://www.eclipse.org/swt/snippets/#button">Button
 *      snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example:
 *      ControlExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further
 *      information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class Button extends Control {

	ToggleGroup toggleGroup;
	List<SelectionListener> selectionListeners;
	
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
	 * @see SWT#ARROW
	 * @see SWT#CHECK
	 * @see SWT#PUSH
	 * @see SWT#RADIO
	 * @see SWT#TOGGLE
	 * @see SWT#FLAT
	 * @see SWT#UP
	 * @see SWT#DOWN
	 * @see SWT#LEFT
	 * @see SWT#RIGHT
	 * @see SWT#CENTER
	 * @see Widget#checkSubclass
	 * @see Widget#getStyle
	 */
	public Button(Composite parent, int style) {
		super(parent, style);

		ButtonBase button;
		
		if ((style & SWT.RADIO) != 0) {
			RadioButton radioButton = new RadioButton();
			Control[] siblings = parent.getChildren();
			if (siblings != null && siblings.length > 0) {
				Control last = siblings[siblings.length - 1];
				if (last instanceof Button)
					toggleGroup = ((Button)last).toggleGroup;
			}
			if (toggleGroup == null) {
				toggleGroup = new ToggleGroup();
				radioButton.setSelected(true);
			}
			radioButton.setToggleGroup(toggleGroup);
			button = radioButton;
		} else if ((style & SWT.CHECK) != 0) { 
			button = new CheckBox();
		} else if ((style & SWT.TOGGLE)!= 0){
			button = new ToggleButton();
		}else{
			button = new javafx.scene.control.Button();
		}

		if ((style & SWT.WRAP)!=0){
			button.setWrapText(true);
		}
		
		button.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent actionEvent) {
				Event event = new Event();
				event.widget = event.item = Button.this;
				SelectionEvent se = new SelectionEvent(event);
				
				if (selectionListeners != null)
					for (SelectionListener listener : selectionListeners)
						listener.widgetSelected(se);
			}
		});

		setNode(button);
	}

	ButtonBase getButton() {
		return (ButtonBase)node;
	}
	
	/**
	 * Adds the listener to the collection of listeners who will be notified
	 * when the control is selected by the user, by sending it one of the
	 * messages defined in the <code>SelectionListener</code> interface.
	 * <p>
	 * <code>widgetSelected</code> is called when the control is selected by the
	 * user. <code>widgetDefaultSelected</code> is not called.
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
	 * @see SelectionListener
	 * @see #removeSelectionListener
	 * @see SelectionEvent
	 */
	public void addSelectionListener(SelectionListener listener) {
		if (selectionListeners == null)
			selectionListeners = new LinkedList<>();
		selectionListeners.add(listener);
	}

	/**
	 * Returns a value which describes the position of the text or image in the
	 * receiver. The value will be one of <code>LEFT</code>, <code>RIGHT</code>
	 * or <code>CENTER</code> unless the receiver is an <code>ARROW</code>
	 * button, in which case, the alignment will indicate the direction of the
	 * arrow (one of <code>LEFT</code>, <code>RIGHT</code>, <code>UP</code> or
	 * <code>DOWN</code>).
	 * 
	 * @return the alignment
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public int getAlignment() {
		if ((style & SWT.ARROW) == 0)
			switch (getButton().getAlignment()) {
			case CENTER_LEFT:
				return SWT.LEFT;
			case CENTER_RIGHT:
				return SWT.RIGHT;
			case CENTER:
				return SWT.CENTER;
			default:
				return 0;
			}
		else
			// TODO Arrow direction
			return 0;
	}

	/**
	 * Returns <code>true</code> if the receiver is grayed, and false otherwise.
	 * When the widget does not have the <code>CHECK</code> style, return false.
	 * 
	 * @return the grayed state of the checkbox
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
	public boolean getGrayed() {
		// TODO
		return false;
	}

	/**
	 * Returns the receiver's image if it has one, or null if it does not.
	 * 
	 * @return the receiver's image
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public Image getImage() {
		// TODO
		return null;
	}

	/**
	 * Returns <code>true</code> if the receiver is selected, and false
	 * otherwise.
	 * <p>
	 * When the receiver is of type <code>CHECK</code> or <code>RADIO</code>, it
	 * is selected when it is checked. When it is of type <code>TOGGLE</code>,
	 * it is selected when it is pushed in. If the receiver is of any other
	 * type, this method returns false.
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
		if (node instanceof ToggleButton)
			return ((ToggleButton)node).isSelected();
		else if (node instanceof CheckBox)
			return ((CheckBox)node).isSelected();
		else
			return false;
	}

	/**
	 * Returns the receiver's text, which will be an empty string if it has
	 * never been set or if the receiver is an <code>ARROW</code> button.
	 * 
	 * @return the receiver's text
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public String getText() {
		return getButton().getText();
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
		if (selectionListeners != null) {
			selectionListeners.remove(listener);
			if (selectionListeners.isEmpty())
				selectionListeners = null;
		}
	}

	/**
	 * Controls how text, images and arrows will be displayed in the receiver.
	 * The argument should be one of <code>LEFT</code>, <code>RIGHT</code> or
	 * <code>CENTER</code> unless the receiver is an <code>ARROW</code> button,
	 * in which case, the argument indicates the direction of the arrow (one of
	 * <code>LEFT</code>, <code>RIGHT</code>, <code>UP</code> or
	 * <code>DOWN</code>).
	 * 
	 * @param alignment
	 *            the new alignment
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setAlignment(int alignment) {
		if ((style & SWT.ARROW) == 0) {
			if ((alignment & SWT.LEFT) != 0)
				getButton().setAlignment(Pos.CENTER_LEFT);
			else if ((alignment & SWT.RIGHT) != 0)
				getButton().setAlignment(Pos.CENTER_RIGHT);
			else if ((alignment & SWT.CENTER) != 0)
				getButton().setAlignment(Pos.CENTER);
		} else {
			// TODO Arrow direction
		}
	}

	/**
	 * Sets the grayed state of the receiver. This state change only applies if
	 * the control was created with the SWT.CHECK style.
	 * 
	 * @param grayed
	 *            the new grayed state
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
	public void setGrayed(boolean grayed) {
		// TODO
	}

	/**
	 * Sets the receiver's image to the argument, which may be <code>null</code>
	 * indicating that no image should be displayed.
	 * <p>
	 * Note that a Button can display an image and text simultaneously on
	 * Windows (starting with XP), GTK+ and OSX. On other platforms, a Button
	 * that has an image and text set into it will display the image or text
	 * that was set most recently.
	 * </p>
	 * 
	 * @param image
	 *            the image to display on the receiver (may be <code>null</code>
	 *            )
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_ARGUMENT - if the image has been
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
	public void setImage(Image image) {
		// TODO
	}

	/**
	 * Sets the selection state of the receiver, if it is of type
	 * <code>CHECK</code>, <code>RADIO</code>, or <code>TOGGLE</code>.
	 * 
	 * <p>
	 * When the receiver is of type <code>CHECK</code> or <code>RADIO</code>, it
	 * is selected when it is checked. When it is of type <code>TOGGLE</code>,
	 * it is selected when it is pushed in.
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
		if (node instanceof ToggleButton)
			((ToggleButton)node).setSelected(selected);
		else if (node instanceof CheckBox)
			((CheckBox)node).setSelected(selected);
	}

	/**
	 * Sets the receiver's text.
	 * <p>
	 * This method sets the button label. The label may include the mnemonic
	 * character but must not contain line delimiters.
	 * </p>
	 * <p>
	 * Mnemonics are indicated by an '&amp;' that causes the next character to
	 * be the mnemonic. When the user presses a key sequence that matches the
	 * mnemonic, a selection event occurs. On most platforms, the mnemonic
	 * appears underlined but may be emphasized in a platform specific manner.
	 * The mnemonic indicator character '&amp;' can be escaped by doubling it in
	 * the string, causing a single '&amp;' to be displayed.
	 * </p>
	 * <p>
	 * Note that a Button can display an image and text simultaneously on
	 * Windows (starting with XP), GTK+ and OSX. On other platforms, a Button
	 * that has an image and text set into it will display the image or text
	 * that was set most recently.
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
	 */
	public void setText(String string) {
		getButton().setText(string);
	}

}

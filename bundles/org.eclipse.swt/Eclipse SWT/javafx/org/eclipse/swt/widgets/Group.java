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

import javafx.scene.control.TitledPane;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Instances of this class provide an etched border with an optional title.
 * <p>
 * Shadow styles are hints and may not be honoured by the platform. To create a
 * group with the default shadow style for the platform, do not specify a shadow
 * style.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>SHADOW_ETCHED_IN, SHADOW_ETCHED_OUT, SHADOW_IN, SHADOW_OUT, SHADOW_NONE</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * <p>
 * Note: Only one of the above styles may be specified.
 * </p>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 * 
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example:
 *      ControlExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further
 *      information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class Group extends Composite {

	private TitledPane pane;
	private FXLayoutPane layoutPane;
	
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
	 * @see SWT#SHADOW_ETCHED_IN
	 * @see SWT#SHADOW_ETCHED_OUT
	 * @see SWT#SHADOW_IN
	 * @see SWT#SHADOW_OUT
	 * @see SWT#SHADOW_NONE
	 * @see Widget#checkSubclass
	 * @see Widget#getStyle
	 */
	public Group(Composite parent, int style) {
		super(parent, style);
	}
	
	@Override
	protected TitledPane createWidget() {
		pane = new TitledPane();
		pane.setCollapsible(false);
		layoutPane = new FXLayoutPane(this);
		pane.setContent(layoutPane);
		return pane;
	}
	
	@Override
	public Rectangle computeTrim(int x, int y, int width, int height) {
		int w = (int) Math.ceil(pane.prefWidth(javafx.scene.control.Control.USE_COMPUTED_SIZE));
		int h = (int) Math.ceil(pane.prefHeight(javafx.scene.control.Control.USE_COMPUTED_SIZE));
		return super.computeTrim(x, y, w, h);
	}
	
	@Override
	public Rectangle getClientArea() {
		return new Rectangle(0, 0, (int)layoutPane.getWidth(), (int)layoutPane.getHeight());
	}
	
	/**
	 * Returns the receiver's text, which is the string that the is used as the
	 * <em>title</em>. If the text has not previously been set, returns an empty
	 * string.
	 * 
	 * @return the text
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
		checkWidget();
		return notNullString(pane.getText());
	}

	@Override
	protected void internal_attachControl(Control c) {
		layoutPane.getChildren().add(c.internal_getNativeObject());
	}
	
	@Override
	protected void internal_detachControl(Control c) {
		layoutPane.getChildren().remove(c.internal_getNativeObject());
	}
	
	@Override
	protected double internal_getHeight() {
		return pane.getHeight();
	}
	
	@Override
	protected double internal_getPrefHeight() {
		return pane.prefHeight(javafx.scene.control.Control.USE_COMPUTED_SIZE);
	}
	
	@Override
	protected double internal_getPrefWidth() {
		return pane.prefWidth(javafx.scene.control.Control.USE_COMPUTED_SIZE);
	}
	
	@Override
	public TitledPane internal_getNativeObject() {
		return pane;
	}

	@Override
	protected double internal_getWidth() {
		return pane.getWidth();
	}
	
	@Override
	protected void internal_setLayout(Layout layout) {
		layoutPane.setLayout(layout);
	}
	
	@Override
	protected void internal_doLayout() {
		pane.layout();
	}
	
	/**
	 * Sets the receiver's text, which is the string that will be displayed as
	 * the receiver's <em>title</em>, to the argument, which may not be null.
	 * The string may include the mnemonic character. </p> Mnemonics are
	 * indicated by an '&amp;' that causes the next character to be the
	 * mnemonic. When the user presses a key sequence that matches the mnemonic,
	 * focus is assigned to the first child of the group. On most platforms, the
	 * mnemonic appears underlined but may be emphasised in a platform specific
	 * manner. The mnemonic indicator character '&amp;' can be escaped by
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
		checkWidget ();
		if (string == null) error (SWT.ERROR_NULL_ARGUMENT);
		pane.setText(string);
	}

}

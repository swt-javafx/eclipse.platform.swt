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

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;

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

	VBox vbox;
	Label label;
	
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
	void createNode() {
		vbox = new VBox();
		vbox.setStyle("-fx-border-width: 1px; -fx-border-color: #222222; -fx-padding: 5;");
		vbox.setFillWidth(true);
		
//		setNode(vbox);
	}
	
	@Override
	void createPane() {
		convertLayout();
		vbox.getChildren().add(paneLayout.getPane());
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
		return label.getText();
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
		label = new Label();
		label.setStyle("-fx-padding: 2;");
		label.setText(string);

		vbox.getChildren().add(0, label);
	}

}

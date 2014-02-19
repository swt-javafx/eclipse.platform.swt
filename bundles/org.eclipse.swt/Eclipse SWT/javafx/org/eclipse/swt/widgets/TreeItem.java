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
import java.util.Arrays;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.util.Callback;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.Util;
import org.eclipse.swt.widgets.Tree.SWTTreeRow;

/**
 * Instances of this class represent a selectable user interface object that
 * represents a hierarchy of tree items in a tree widget.
 * 
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>(none)</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 * 
 * @see <a href="http://www.eclipse.org/swt/snippets/#tree">Tree, TreeItem,
 *      TreeColumn snippets</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further
 *      information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class TreeItem extends Item {

	javafx.scene.control.TreeItem<TreeItem> nativeObject;
	
	private Tree tree;
	private TreeItem parentItem;
	private java.util.List<Registration> registrations;
	
	private String[] texts;
	private Image[] images;
	private Color[] backgrounds;
	private Font[] fonts;
	private Color[] foregrounds;
	private boolean checked;
	private boolean grayed;
	
	class Registration {
		private int index;
		private Callback<AttributeType, Void> callback;
		
		public Registration(int index, Callback<AttributeType, Void> callback) {
			this.index = index;
			this.callback = callback;
		}
		
		public void dispose() {
			registrations.remove(this);
		}
	}
	
	enum AttributeType {
		TEXT,
		IMAGE,
		CHECK
	}

	/**
	 * Constructs a new instance of this class given its parent (which must be a
	 * <code>Tree</code> or a <code>TreeItem</code>) and a style value
	 * describing its behavior and appearance. The item is added to the end of
	 * the items maintained by its parent.
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
	 *            a tree control which will be the parent of the new instance
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
	 * @see SWT
	 * @see Widget#checkSubclass
	 * @see Widget#getStyle
	 */
	public TreeItem(Tree parent, int style) {
		super(parent, style);
		this.tree = parent;
		this.registrations = new ArrayList<>();
		createWidget();
		tree.internal_itemAdded(this);
	}

	/**
	 * Constructs a new instance of this class given its parent (which must be a
	 * <code>Tree</code> or a <code>TreeItem</code>), a style value describing
	 * its behavior and appearance, and the index at which to place it in the
	 * items maintained by its parent.
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
	 *            a tree control which will be the parent of the new instance
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
	 * @see SWT
	 * @see Widget#checkSubclass
	 * @see Widget#getStyle
	 */
	public TreeItem(Tree parent, int style, int index) {
		super(parent, style);
		this.tree = parent;
		this.registrations = new ArrayList<>();
		createWidget();
		tree.internal_itemAdded(this, index);
	}

	/**
	 * Constructs a new instance of this class given its parent (which must be a
	 * <code>Tree</code> or a <code>TreeItem</code>) and a style value
	 * describing its behavior and appearance. The item is added to the end of
	 * the items maintained by its parent.
	 * <p>
	 * The style value is either one of the style constants defined in class
	 * <code>SWT</code> which is applicable to instances of this class, or must
	 * be built by <em>bitwise OR</em>'ing together (that is, using the
	 * <code>int</code> "|" operator) two or more of those <code>SWT</code>
	 * style constants. The class description lists the style constants that are
	 * applicable to the class. Style bits are also inherited from superclasses.
	 * </p>
	 * 
	 * @param parentItem
	 *            a tree control which will be the parent of the new instance
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
	 * @see SWT
	 * @see Widget#checkSubclass
	 * @see Widget#getStyle
	 */
	public TreeItem(TreeItem parentItem, int style) {
		super(parentItem, style);
		this.parentItem = parentItem;
		this.registrations = new ArrayList<>();
		createWidget();
		parentItem.nativeObject.getChildren().add(nativeObject);
	}

	/**
	 * Constructs a new instance of this class given its parent (which must be a
	 * <code>Tree</code> or a <code>TreeItem</code>), a style value describing
	 * its behavior and appearance, and the index at which to place it in the
	 * items maintained by its parent.
	 * <p>
	 * The style value is either one of the style constants defined in class
	 * <code>SWT</code> which is applicable to instances of this class, or must
	 * be built by <em>bitwise OR</em>'ing together (that is, using the
	 * <code>int</code> "|" operator) two or more of those <code>SWT</code>
	 * style constants. The class description lists the style constants that are
	 * applicable to the class. Style bits are also inherited from superclasses.
	 * </p>
	 * 
	 * @param parentItem
	 *            a tree control which will be the parent of the new instance
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
	 * @see SWT
	 * @see Widget#checkSubclass
	 * @see Widget#getStyle
	 */
	public TreeItem(TreeItem parentItem, int style, int index) {
		super(parentItem, style);
		this.parentItem = parentItem;
		this.registrations = new ArrayList<>();
		createWidget();
		parentItem.nativeObject.getChildren().add(index, nativeObject);
	}

	private <T> void arrayUpdate(AttributeType type, T[] originalAr, T[] newAr) {
		newAr = Arrays.copyOf(newAr,newAr.length);
		
		T[] i1;
		T[] i2;
		if( originalAr.length > newAr.length ) {
			i1 = originalAr;
			i2 = newAr;
		} else {
			i1 = newAr;
			i2 = originalAr;
		}
		
		switch (type) {
		case IMAGE:
			images = (Image[]) newAr;
			break;
		case TEXT:
			texts = (String[]) newAr;
			break;
		default:
			throw new IllegalArgumentException("Unsupported type '"+type+"'");
		}
		
		for( int i = 0; i < i1.length; i++ ) {
			if( i < i2.length ) {
				if( i1[i] != i2[i] ) {
					fireModification(i, AttributeType.IMAGE);
				}
			} else {
				fireModification(i, AttributeType.IMAGE);
			}
		}
	}
	
	/**
	 * Clears the item at the given zero-relative index in the receiver. The
	 * text, icon and other attributes of the item are set to the default value.
	 * If the tree was created with the <code>SWT.VIRTUAL</code> style, these
	 * attributes are requested again as needed.
	 * 
	 * @param index
	 *            the index of the item to clear
	 * @param all
	 *            <code>true</code> if all child items of the indexed item
	 *            should be cleared recursively, and <code>false</code>
	 *            otherwise
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
	 * 
	 * @see SWT#VIRTUAL
	 * @see SWT#SetData
	 * 
	 * @since 3.2
	 */
	public void clear(int index, boolean all) {
		Util.logNotImplemented();
	}

	/**
	 * Clears all the items in the receiver. The text, icon and other attributes
	 * of the items are set to their default values. If the tree was created
	 * with the <code>SWT.VIRTUAL</code> style, these attributes are requested
	 * again as needed.
	 * 
	 * @param all
	 *            <code>true</code> if all child items should be cleared
	 *            recursively, and <code>false</code> otherwise
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @see SWT#VIRTUAL
	 * @see SWT#SetData
	 * 
	 * @since 3.2
	 */
	public void clearAll(boolean all) {
		Util.logNotImplemented();
	}

	@Override
	void createHandle() {
		nativeObject = new javafx.scene.control.TreeItem<TreeItem>(this);
		nativeObject.expandedProperty().addListener(new InvalidationListener() {
			@Override
			public void invalidated(Observable observable) {
				Event evt = new Event();
				evt.item = TreeItem.this;
				getTree().sendEvent(nativeObject.isExpanded() ? SWT.Expand : SWT.Collapse, evt, true);
			}
		});
	}
	
	@Override
	public void dispose() {
		if( tree != null ) {
			tree.internal_itemRemoved(this);
			tree = null;
		} else {
			parentItem.internal_itemRemoved(this);
			nativeObject.setValue(null);
			parentItem = null;
		}
		
		javafx.scene.control.TreeItem<TreeItem>[] children = nativeObject.getChildren().toArray(new javafx.scene.control.TreeItem[0]);
		// clear the list this makes the remove faster
		nativeObject.getChildren().clear();
		
		for( javafx.scene.control.TreeItem<TreeItem> i : children ) {
			i.getValue().dispose();
		}
		
		super.dispose();
	}
	
	private void fireModification(int index, AttributeType type) {
		for( Registration r : registrations.toArray(new Registration[0]) ) {
			if( r.index == index ) {
				r.callback.call(type);
			}
		}
	}
	
	/**
	 * Returns the receiver's background color.
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
	 * 
	 * @since 2.0
	 * 
	 */
	public Color getBackground() {
		checkWidget();
		return getBackground(0); 
	}

	/**
	 * Returns the background color at the given column index in the receiver.
	 * 
	 * @param index
	 *            the column index
	 * @return the background color
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
	public Color getBackground(int index) {
		checkWidget();
		if( backgrounds != null && index <  backgrounds.length ) {
			return backgrounds[index];
		}
		return null;
	}

	/**
	 * Returns a rectangle describing the receiver's size and location relative
	 * to its parent at a column in the tree.
	 * 
	 * @param index
	 *            the index that specifies the column
	 * @return the receiver's bounding column rectangle
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
	public Rectangle getBounds(int index) {
		SWTTreeRow row = getTree().internal_getTreeRow(this);
		if( row != null ) {
			return row.swt_getBounds(index);
		}
		return new Rectangle(0, 0, 0, 0);
	}

	/**
	 * Returns a rectangle describing the size and location of the receiver's
	 * text relative to its parent.
	 * 
	 * @return the bounding rectangle of the receiver's text
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
		return getBounds(0);
	}

	/**
	 * Returns <code>true</code> if the receiver is checked, and false
	 * otherwise. When the parent does not have the
	 * <code>CHECK style, return false.
	 * <p>
	 * 
	 * @return the checked state
	 * 
	 * @exception SWTException
	 *                <ul> <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li> <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public boolean getChecked() {
		return checked;
	}

	/**
	 * Returns <code>true</code> if the receiver is expanded, and false
	 * otherwise.
	 * <p>
	 * 
	 * @return the expanded state
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li> <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public boolean getExpanded() {
		return grayed;
	}

	/**
	 * Returns the font that the receiver will use to paint textual information
	 * for this item.
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
	 * 
	 * @since 3.0
	 */
	public Font getFont() {
		return getFont(0);
	}

	/**
	 * Returns the font that the receiver will use to paint textual information
	 * for the specified cell in this item.
	 * 
	 * @param index
	 *            the column index
	 * @return the receiver's font
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
	public Font getFont(int index) {
		checkWidget();
		if( fonts != null && index < fonts.length ) {
			return fonts[index];
		}
		// TODO is this the right answer
		return display.getSystemFont();
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
	 * 
	 * @since 2.0
	 * 
	 */
	public Color getForeground() {
		return getForeground(0);
	}

	/**
	 * 
	 * Returns the foreground color at the given column index in the receiver.
	 * 
	 * @param index
	 *            the column index
	 * @return the foreground color
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
	public Color getForeground(int index) {
		checkWidget();
		if( foregrounds != null && index < foregrounds.length ) {
			return foregrounds[index];
		}
		return null;
	}

	/**
	 * Returns <code>true</code> if the receiver is grayed, and false otherwise.
	 * When the parent does not have the <code>CHECK style, return false.
	 * <p>
	 * 
	 * @return the grayed state of the checkbox
	 * 
	 * @exception SWTException
	 *                <ul> <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li> <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public boolean getGrayed() {
		return grayed;
	}

	/**
	 * Returns the image stored at the given column index in the receiver, or
	 * null if the image has not been set or if the column does not exist.
	 * 
	 * @param index
	 *            the column index
	 * @return the image stored at the given column index in the receiver
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
	public Image getImage(int index) {
		if( images != null && index < images.length ) {
			return images[index];
		}
		return null;
	}

	@Override
	public Image getImage() {
		return getImage(0);
	}
	
	/**
	 * Returns a rectangle describing the size and location relative to its
	 * parent of an image at a column in the tree.
	 * 
	 * @param index
	 *            the index that specifies the column
	 * @return the receiver's bounding image rectangle
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
	public Rectangle getImageBounds(int index) {
		Util.logNotImplemented();
		return new Rectangle(0, 0, 0, 0);
	}

	/**
	 * Returns the number of items contained in the receiver that are direct
	 * item children of the receiver.
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
		return nativeObject.getChildren().size();
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
	 * 
	 * @since 3.1
	 */
	public TreeItem getItem(int index) {
		return nativeObject.getChildren().get(index).getValue();
	}

	/**
	 * Returns a (possibly empty) array of <code>TreeItem</code>s which are the
	 * direct item children of the receiver.
	 * <p>
	 * Note: This is not the actual structure used by the receiver to maintain
	 * its list of items, so modifying the array will not affect the receiver.
	 * </p>
	 * 
	 * @return the receiver's items
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public TreeItem[] getItems() {
		return Tree.extractItemArray(nativeObject.getChildren());
	}

	/**
	 * Returns the receiver's parent, which must be a <code>Tree</code>.
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
	public Tree getParent() {
		return getTree();
	}

	/**
	 * Returns the receiver's parent item, which must be a <code>TreeItem</code>
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
	public TreeItem getParentItem() {
		return parentItem;
	}

	@Override
	public String getText() {
		return getText(0);
	}
	
	/**
	 * Returns the text stored at the given column index in the receiver, or
	 * empty string if the text has not been set.
	 * 
	 * @param index
	 *            the column index
	 * @return the text stored at the given column index in the receiver
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
	public String getText(int index) {
		if( texts != null && index < texts.length ) {
			return Util.notNull(texts[index]);
		}
		
		return "";
	}

	/**
	 * Returns a rectangle describing the size and location relative to its
	 * parent of the text at a column in the tree.
	 * 
	 * @param index
	 *            the index that specifies the column
	 * @return the receiver's bounding text rectangle
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
	public Rectangle getTextBounds(int index) {
		Util.logNotImplemented();
		return new Rectangle(0, 0, 0, 0);
	}

	private Tree getTree() {
		if( tree == null ) {
			return parentItem.getTree();
		}
		return tree;
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
	 *                <li>ERROR_INVALID_ARGUMENT - if the item has been disposed
	 *                </li>
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
	public int indexOf(TreeItem item) {
		ObservableList<javafx.scene.control.TreeItem<TreeItem>> children = nativeObject.getChildren();
		for( int i = 0; i < children.size(); i++ ) {
			if( children.get(i).getValue() == item ) {
				return i;
			}
		}
		
		return -1;
	}

	private void internal_itemRemoved(TreeItem item) {
		nativeObject.getChildren().remove(item.nativeObject);
	}
	
	/**
	 * Removes all of the items from the receiver.
	 * <p>
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li> <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                called from the thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @since 3.1
	 */
	public void removeAll() {
		// TODO
	}

	/**
	 * Sets the receiver's background color to the color specified by the
	 * argument, or to the default system color for the item if the argument is
	 * null.
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
	 * 
	 * @since 2.0
	 * 
	 */
	public void setBackground(Color color) {
		setBackground(0, color);
	}

	/**
	 * Sets the background color at the given column index in the receiver to
	 * the color specified by the argument, or to the default system color for
	 * the item if the argument is null.
	 * 
	 * @param index
	 *            the column index
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
	 * 
	 * @since 3.1
	 * 
	 */
	public void setBackground(int index, Color color) {
		checkWidget();
		if( backgrounds == null ) {
			backgrounds = new Color[index+1];
		}
		Util.setIndexValue(index, backgrounds, color);
	}

	/**
	 * Sets the checked state of the receiver.
	 * <p>
	 * 
	 * @param checked
	 *            the new checked state
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li> <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public void setChecked(boolean checked) {
		this.checked = checked;
		fireModification(0, AttributeType.CHECK);
	}

	/**
	 * Sets the expanded state of the receiver.
	 * <p>
	 * 
	 * @param expanded
	 *            the new expanded state
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li> <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public void setExpanded(boolean expanded) {
		getTree().internal_runNoEvent(new Runnable() {
			
			@Override
			public void run() {
				nativeObject.setExpanded(expanded);
			}
		});
	}

	/**
	 * Sets the font that the receiver will use to paint textual information for
	 * this item to the font specified by the argument, or to the default font
	 * for that kind of control if the argument is null.
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
	 * 
	 * @since 3.0
	 */
	public void setFont(Font font) {
		setFont(0, font);
	}

	/**
	 * Sets the font that the receiver will use to paint textual information for
	 * the specified cell in this item to the font specified by the argument, or
	 * to the default font for that kind of control if the argument is null.
	 * 
	 * @param index
	 *            the column index
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
	 * 
	 * @since 3.1
	 */
	public void setFont(int index, Font font) {
		checkWidget();
		if( fonts == null ) {
			fonts = new Font[index+1];
		}
		Util.setIndexValue(index, fonts, font);
	}

	/**
	 * Sets the receiver's foreground color to the color specified by the
	 * argument, or to the default system color for the item if the argument is
	 * null.
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
	 * 
	 * @since 2.0
	 * 
	 */
	public void setForeground(Color color) {
		setForeground(0, color);
	}

	/**
	 * Sets the foreground color at the given column index in the receiver to
	 * the color specified by the argument, or to the default system color for
	 * the item if the argument is null.
	 * 
	 * @param index
	 *            the column index
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
	 * 
	 * @since 3.1
	 * 
	 */
	public void setForeground(int index, Color color) {
		checkWidget();
		if( foregrounds == null ) {
			foregrounds = new Color[index+1];
		}
		Util.setIndexValue(0, foregrounds, color);
	}

	/**
	 * Sets the grayed state of the checkbox for this item. This state change
	 * only applies if the Tree was created with the SWT.CHECK style.
	 * 
	 * @param grayed
	 *            the new grayed state of the checkbox
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setGrayed(boolean grayed) {
		this.grayed = grayed;
		fireModification(0, AttributeType.CHECK);
	}

	@Override
	public void setImage(Image image) {
		setImage(0,image);
	}
	
	/**
	 * Sets the receiver's image at a column.
	 * 
	 * @param index
	 *            the column index
	 * @param image
	 *            the new image
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
	 * 
	 * @since 3.1
	 */
	public void setImage(int index, Image image) {
		if( images == null ) {
			images = new Image[index];
		}
		images = Util.setIndexValue(index, images, image);
		fireModification(index, AttributeType.IMAGE);
	}

	/**
	 * Sets the image for multiple columns in the tree.
	 * 
	 * @param images
	 *            the array of new images
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the array of images is null</li>
	 *                <li>ERROR_INVALID_ARGUMENT - if one of the images has been
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
	 * @since 3.1
	 */
	public void setImage(Image[] images) {
		if( images.length == 0 ) {
			Image[] oldImages = this.images;
			this.images = null;
			
			if( oldImages != null ) {
				for( int i = 0; i < oldImages.length; i++ ) {
					fireModification(i, AttributeType.IMAGE);
				}	
			}
		} else {
			arrayUpdate(AttributeType.IMAGE, this.images == null ? new Image[0] : this.images, images);
		}
	}

	/**
	 * Sets the number of child items contained in the receiver.
	 * 
	 * @param count
	 *            the number of items
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
	public void setItemCount(int count) {
		Util.logNotImplemented();
	}

	@Override
	public void setText(String string) {
		setText(0,string);
	}
	
	/**
	 * Sets the receiver's text at a column
	 * 
	 * @param index
	 *            the column index
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
	 * @since 3.1
	 */
	public void setText(int index, String string) {
		if( texts == null ) {
			texts = new String[index];
		}
		texts = Util.setIndexValue(index, texts, string);
	}

	/**
	 * Sets the text for multiple columns in the tree.
	 * 
	 * @param strings
	 *            the array of new strings
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
	 * @since 3.1
	 */
	public void setText(String[] strings) {
		if( texts.length == 0 ) {
			this.texts = null;
		} else {
			arrayUpdate(AttributeType.TEXT, this.texts == null ? new String[0] : this.texts, strings);
		}
	}

}

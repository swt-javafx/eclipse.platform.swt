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

import javafx.geometry.Insets;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Line;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Device.NoOpDrawableGC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.Util;

/**
 * Instances of this class provide an area for dynamically positioning the items
 * they contain.
 * <p>
 * The item children that may be added to instances of this class must be of
 * type <code>CoolItem</code>.
 * </p>
 * <p>
 * Note that although this class is a subclass of <code>Composite</code>, it
 * does not make sense to add <code>Control</code> children to it, or set a
 * layout on it.
 * </p>
 * <p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>FLAT, HORIZONTAL, VERTICAL</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * </p>
 * <p>
 * Note: Only one of the styles HORIZONTAL and VERTICAL may be specified.
 * </p>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 * 
 * @see <a href="http://www.eclipse.org/swt/snippets/#coolbar">CoolBar
 *      snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example:
 *      ControlExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further
 *      information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CoolBar extends Composite {
	private Pane pane;
	
	static final int ROW_SPACING = 2;
	
	private java.util.List<CoolItem> items = new ArrayList<CoolItem>();
	
	private static class CoolBarItem extends HBox {
		private Control control;
		private CoolItem item;
		
		public CoolBarItem(Control control) {
			setManaged(false);
			setVisible(false);
			this.control = control;
			
			//FIXME We need to find a better L&F for the grabber
			StackPane r = new StackPane();
			r.setPadding(new Insets(0, 3, 0, 3));
			Line l = new Line(3,3,3,0);
			l.setStrokeWidth(2);
			l.getStrokeDashArray().addAll(3.0, 3.0);
			l.setStroke(javafx.scene.paint.Color.LIGHTGRAY);
			r.getChildren().add(l);
			r.setCursor(javafx.scene.Cursor.HAND);
			Region region = (Region) control.internal_getNativeObject();
			getChildren().addAll(r, region);
			l.endYProperty().bind(region.heightProperty().subtract(6));
		}
		
		public void setCoolItem(CoolItem item) {
			setManaged(true);
			setVisible(true);
		}
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
	 * @see SWT
	 * @see SWT#FLAT
	 * @see SWT#HORIZONTAL
	 * @see SWT#VERTICAL
	 * @see Widget#checkSubclass
	 * @see Widget#getStyle
	 */
	public CoolBar(Composite parent, int style) {
		super(parent, checkStyle(style));
	}

	static int checkStyle(int style) {
		style |= SWT.NO_FOCUS;
		return (style | SWT.NO_REDRAW_RESIZE) & ~(SWT.V_SCROLL | SWT.H_SCROLL);
	}

	@Override
	protected void checkSubclass() {
		if (!isValidSubclass())
			error(SWT.ERROR_INVALID_SUBCLASS);
	}

	@Override
	protected Pane createWidget() {
		pane = new FlowPane(0,2);
		return pane;
	}

	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		checkWidget();
		// TODO
		return new Point(0, 0);
	}

	/**
	 * Returns the item that is currently displayed at the given, zero-relative
	 * index. Throws an exception if the index is out of range.
	 * 
	 * @param index
	 *            the visual index of the item to return
	 * @return the item at the given visual index
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
	public CoolItem getItem(int index) {
		checkWidget();
		if (index < 0)
			error(SWT.ERROR_INVALID_RANGE);
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
	 * Returns an array of <code>CoolItem</code>s in the order in which they are
	 * currently being displayed.
	 * <p>
	 * Note: This is not the actual structure used by the receiver to maintain
	 * its list of items, so modifying the array will not affect the receiver.
	 * </p>
	 * 
	 * @return the receiver's items in their current visual order
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public CoolItem[] getItems() {
		checkWidget();
		return items.toArray(new CoolItem[items.size()]);
	}

	void fixEvent(Event event) {
		if ((style & SWT.VERTICAL) != 0) {
			int tmp = event.x;
			event.x = event.y;
			event.y = tmp;
		}
	}

	Rectangle fixRectangle(int x, int y, int width, int height) {
		if ((style & SWT.VERTICAL) != 0) {
			return new Rectangle(y, x, height, width);
		}
		return new Rectangle(x, y, width, height);
	}

	Point fixPoint(int x, int y) {
		if ((style & SWT.VERTICAL) != 0) {
			return new Point(y, x);
		}
		return new Point(x, y);
	}

	/**
	 * Searches the receiver's items in the order they are currently being
	 * displayed, starting at the first item (index 0), until an item is found
	 * that is equal to the argument, and returns the index of that item. If no
	 * item is found, returns -1.
	 * 
	 * @param item
	 *            the search item
	 * @return the visual order index of the search item, or -1 if the item is
	 *         not found
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the item is null</li>
	 *                <li>ERROR_INVALID_ARGUMENT - if the item is disposed</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public int indexOf(CoolItem item) {
		checkWidget();
		if (item == null)
			error(SWT.ERROR_NULL_ARGUMENT);
		if (item.isDisposed())
			error(SWT.ERROR_INVALID_ARGUMENT);
		return items.indexOf(item);
	}

	@Override
	protected void internal_attachControl(Control c) {
		pane.getChildren().add(new CoolBarItem(c));
//		c.internal_getNativeObject().setManaged(false);
//		c.internal_getNativeObject().setVisible(false);
	}
	
	@Override
	protected void internal_attachControl(int idx, Control c) {
		pane.getChildren().add(idx, new CoolBarItem(c));
//		c.internal_getNativeObject().setManaged(false);
//		c.internal_getNativeObject().setVisible(false);
	}
	
	@Override
	protected void internal_detachControl(Control c) {
		pane.getChildren().remove(c.internal_getNativeObject());
	}
	
	@Override
	public Pane internal_getNativeObject() {
		return pane;
	}

	void internal_registerItem(CoolItem item) {
		items.add(item);
//		pane.getChildren().add(item.getChevronNode());
	}

	void internal_registerItem(CoolItem item, int index) {
		items.add(index,item);
//		pane.getChildren().add(item.getChevronNode());
	}
	
	void internal_controlUpdated(CoolItem item) {
		int itemIdx = items.indexOf(item);
		int managedIndex = 0;
		CoolBarItem cItem = null;
		for( int i = 0; i < pane.getChildren().size(); i++ ) {
			CoolBarItem cbi = (CoolBarItem) pane.getChildren().get(i);
			if( cbi.control == item.getControl() ) {
				cItem = cbi;
				break;
			}
			if( cbi.isManaged() ) {
				managedIndex++;
			}
		}
		
		if( cItem != null ) {
			cItem.setCoolItem(item);
			if( itemIdx != managedIndex ) {
				System.err.println("DOES NOT MATCH");
			}
			
		}
	}
	
	void internal_unregisterItem(CoolItem item) {
		items.remove(item);
		if( item.getControl() != null ) {
//			item.getControl().internal_getNativeObject().setManaged(false);
//			item.getControl().internal_getNativeObject().setVisible(false);
		}
//		pane.getChildren().remove(item.getChevronNode());
	}
	
	@Override
	public void internal_dispose_GC(DrawableGC gc) {
	}
	
	@Override
	public DrawableGC internal_new_GC() {
		return new NoOpDrawableGC(this,getFont());
	}
	
	void internalRedraw(int x, int y, int width, int height) {
		if ((style & SWT.VERTICAL) != 0) {
			redraw(y, x, height, width, false);
		} else {
			redraw(x, y, width, height, false);
		}
	}

	/**
	 * Returns an array of zero-relative ints that map the creation order of the
	 * receiver's items to the order in which they are currently being
	 * displayed.
	 * <p>
	 * Specifically, the indices of the returned array represent the current
	 * visual order of the items, and the contents of the array represent the
	 * creation order of the items.
	 * </p>
	 * <p>
	 * Note: This is not the actual structure used by the receiver to maintain
	 * its list of items, so modifying the array will not affect the receiver.
	 * </p>
	 * 
	 * @return the current visual order of the receiver's items
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public int[] getItemOrder() {
		checkWidget();
		// TODO
		return new int[0];
	}

	/**
	 * Returns an array of points whose x and y coordinates describe the widths
	 * and heights (respectively) of the items in the receiver in the order in
	 * which they are currently being displayed.
	 * 
	 * @return the receiver's item sizes in their current visual order
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public Point[] getItemSizes() {
		checkWidget();
		CoolItem[] items = getItems();
		Point[] sizes = new Point[items.length];
		for (int i = 0; i < items.length; i++) {
			sizes[i] = items[i].getSize();
		}
		return sizes;
	}

	/**
	 * Returns whether or not the receiver is 'locked'. When a coolbar is
	 * locked, its items cannot be repositioned.
	 * 
	 * @return true if the coolbar is locked, false otherwise
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
	 */
	public boolean getLocked() {
		checkWidget();
		return false;
	}

	int getWidth() {
		if ((style & SWT.VERTICAL) != 0)
			return getSize().y;
		return getSize().x;
	}

	/**
	 * Returns an array of ints that describe the zero-relative indices of any
	 * item(s) in the receiver that will begin on a new row. The 0th visible
	 * item always begins the first row, therefore it does not count as a wrap
	 * index.
	 * 
	 * @return an array containing the receiver's wrap indices, or an empty
	 *         array if all items are in one row
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public int[] getWrapIndices() {
		Util.logNotImplemented();
		return new int[0];
	}

	/**
	 * Sets whether or not the receiver is 'locked'. When a coolbar is locked,
	 * its items cannot be repositioned.
	 * 
	 * @param locked
	 *            lock the coolbar if true, otherwise unlock the coolbar
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
	 */
	public void setLocked(boolean locked) {
		checkWidget();
		Util.logNotImplemented();
	}

	/**
	 * Sets the indices of all item(s) in the receiver that will begin on a new
	 * row. The indices are given in the order in which they are currently being
	 * displayed. The 0th item always begins the first row, therefore it does
	 * not count as a wrap index. If indices is null or empty, the items will be
	 * placed on one line.
	 * 
	 * @param indices
	 *            an array of wrap indices, or null
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setWrapIndices(int[] indices) {
		checkWidget();
		Util.logNotImplemented();
	}

	/**
	 * Sets the receiver's item order, wrap indices, and item sizes all at once.
	 * This method is typically used to restore the displayed state of the
	 * receiver to a previously stored state.
	 * <p>
	 * The item order is the order in which the items in the receiver should be
	 * displayed, given in terms of the zero-relative ordering of when the items
	 * were added.
	 * </p>
	 * <p>
	 * The wrap indices are the indices of all item(s) in the receiver that will
	 * begin on a new row. The indices are given in the order specified by the
	 * item order. The 0th item always begins the first row, therefore it does
	 * not count as a wrap index. If wrap indices is null or empty, the items
	 * will be placed on one line.
	 * </p>
	 * <p>
	 * The sizes are specified in an array of points whose x and y coordinates
	 * describe the new widths and heights (respectively) of the receiver's
	 * items in the order specified by the item order.
	 * </p>
	 * 
	 * @param itemOrder
	 *            an array of indices that describe the new order to display the
	 *            items in
	 * @param wrapIndices
	 *            an array of wrap indices, or null
	 * @param sizes
	 *            an array containing the new sizes for each of the receiver's
	 *            items in visual order
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if item order or sizes is null</li>
	 *                <li>ERROR_INVALID_ARGUMENT - if item order or sizes is not
	 *                the same length as the number of items</li>
	 *                </ul>
	 */
	public void setItemLayout(int[] itemOrder, int[] wrapIndices, Point[] sizes) {
		checkWidget();
		// TODO
	}

	@Override
	public void setOrientation(int orientation) {
		super.setOrientation(orientation);
		// TODO
	}

}

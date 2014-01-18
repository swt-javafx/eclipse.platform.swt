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
import java.util.WeakHashMap;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.control.CheckBox;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeTableView.TreeTableViewSelectionModel;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Device.NoOpDrawableGC;
import org.eclipse.swt.graphics.Drawable;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.GCData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.CanvasGC;
import org.eclipse.swt.internal.Util;

/**
 * Instances of this class provide a selectable user interface object that
 * displays a hierarchy of items and issues notification when an item in the
 * hierarchy is selected.
 * <p>
 * The item children that may be added to instances of this class must be of
 * type <code>TreeItem</code>.
 * </p>
 * <p>
 * Style <code>VIRTUAL</code> is used to create a <code>Tree</code> whose
 * <code>TreeItem</code>s are to be populated by the client on an on-demand
 * basis instead of up-front. This can provide significant performance
 * improvements for trees that are very large or for which <code>TreeItem</code>
 * population is expensive (for example, retrieving values from an external
 * source).
 * </p>
 * <p>
 * Here is an example of using a <code>Tree</code> with style
 * <code>VIRTUAL</code>: <code><pre>
 *  final Tree tree = new Tree(parent, SWT.VIRTUAL | SWT.BORDER);
 *  tree.setItemCount(20);
 *  tree.addListener(SWT.SetData, new Listener() {
 *      public void handleEvent(Event event) {
 *          TreeItem item = (TreeItem)event.item;
 *          TreeItem parentItem = item.getParentItem();
 *          String text = null;
 *          if (parentItem == null) {
 *              text = "node " + tree.indexOf(item);
 *          } else {
 *              text = parentItem.getText() + " - " + parentItem.indexOf(item);
 *          }
 *          item.setText(text);
 *          System.out.println(text);
 *          item.setItemCount(10);
 *      }
 *  });
 * </pre></code>
 * </p>
 * <p>
 * Note that although this class is a subclass of <code>Composite</code>, it
 * does not normally make sense to add <code>Control</code> children to it, or
 * set a layout on it, unless implementing something like a cell editor.
 * </p>
 * <p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>SINGLE, MULTI, CHECK, FULL_SELECTION, VIRTUAL, NO_SCROLL</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection, DefaultSelection, Collapse, Expand, SetData, MeasureItem,
 * EraseItem, PaintItem</dd>
 * </dl>
 * </p>
 * <p>
 * Note: Only one of the styles SINGLE and MULTI may be specified.
 * </p>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 * 
 * @see <a href="http://www.eclipse.org/swt/snippets/#tree">Tree, TreeItem,
 *      TreeColumn snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example:
 *      ControlExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further
 *      information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class Tree extends Composite {

	private AnchorPane container;
	
	private TreeView<TreeItem> treeView;
	private TreeTableView<TreeItem> treeTableView;
	private java.util.List<TreeColumn> columns;
	
	private WeakHashMap<SWTTreeRow, Boolean> currentCells = new WeakHashMap<>();
	
	private javafx.scene.control.TreeItem<TreeItem> rootItem;
	
	private boolean measureItem;
	private boolean paintItem;
	private boolean eraseItem;
	
	public interface SWTTreeRow {

		void swt_hideEditor(int column);

		void swt_showEditor(Control editor, int column);

		Rectangle swt_getBounds();

		TreeItem swt_getTreeItem();

		Rectangle swt_getBounds(int i);
		
		int swt_getItemHeight();
	}
	
	@Override
	protected void internal_attachControl(Control c) {
		Util.logNotImplemented();
	}
	
	@Override
	protected void internal_attachControl(int idx, Control c) {
		Util.logNotImplemented();
	}
	
	@Override
	protected void internal_detachControl(Control c) {
		Util.logNotImplemented();
	}
	
	@Override
	public void internal_dispose_GC(DrawableGC gc) {
		
	}
	
	@Override
	public DrawableGC internal_new_GC() {
		return new NoOpDrawableGC(this,getFont());
	}
	
	class TreeCellImpl extends TreeCell<TreeItem> implements SWTTreeRow, Drawable {
		private ImageView imageView;
		private TreeItem currentItem;
		private CheckBox checkbox;
		private HBox graphicItemsContainer;
		private Control editor;
		private javafx.scene.canvas.Canvas ownerDrawCanvas;
		private StackPane ownerDrawContainer;
		
		@Override
		protected void updateItem(TreeItem item, boolean empty) {
			this.currentItem = item;
			if( item != null && ! empty ) {
				if( measureItem ) {
					initCanvas();
					sendMeasureEvent();	
				}
				
				updateText();
				updateImage();
				currentCells.put(this, Boolean.TRUE);
			} else {
				setText(null);
				setGraphic(null);
				currentCells.remove(this);
			}
			super.updateItem(item, empty);
		}

		private void updateText() {
			if( editor != null || paintItem ) {
				setText(null);
			} else {
				setText(currentItem.getText());	
			}
		}
		
		private void updateImage() {
			Image img = currentItem.getImage();
			if( (Tree.this.getStyle() & SWT.CHECK) == SWT.CHECK ) {
				if( checkbox == null ) {
					checkbox = new CheckBox();
					checkbox.setOnAction(new EventHandler<ActionEvent>() {

						@Override
						public void handle(ActionEvent event) {
							currentItem.setChecked(checkbox.isSelected());
							Event evt = new Event();
							evt.item = currentItem;
							evt.detail = SWT.CHECK;
							internal_sendEvent(SWT.Selection, evt, true);
						}
					});
				}
				
				checkbox.setSelected(currentItem.getChecked());
				checkbox.setIndeterminate(currentItem.getGrayed());
			}
			
			//TODO Keep or hide image???
			if( img != null ) {
				if( imageView == null ) {
					imageView = new ImageView(img.internal_getImage());
				} else {
					imageView.setImage(img.internal_getImage());
				}
								
				if( editor != null ) {
					HBox h = new HBox();
					h.getChildren().setAll(checkbox, imageView, editor.internal_getNativeObject());
					setGraphic(h);
				} else {
					if( checkbox != null ) {
						if( graphicItemsContainer == null ) {
							graphicItemsContainer = new HBox();
						}
						graphicItemsContainer.getChildren().setAll(checkbox,imageView);
						setGraphic(graphicItemsContainer);
					} else {
						if( paintItem ) {
							if( graphicItemsContainer == null ) {
								graphicItemsContainer = new HBox();
							}
							if( ownerDrawCanvas == null ) {
								initCanvas();
							}
							graphicItemsContainer.getChildren().setAll(imageView,ownerDrawContainer);
						} else {
							setGraphic(imageView);	
						}	
					}
					
				}
			} else {
				if( editor != null ) {
					if( checkbox != null ) {
						if( graphicItemsContainer == null ) {
							graphicItemsContainer = new HBox();
						}
						graphicItemsContainer.getChildren().setAll(checkbox, editor.internal_getNativeObject());
					} else {
						setGraphic(editor.internal_getNativeObject());	
					}
				} else {
					if( checkbox != null ) {
						setGraphic(checkbox);
					} else {
						if( paintItem ) {
							if( ownerDrawCanvas == null ) {
								initCanvas();
							}
							setGraphic(ownerDrawContainer);
						} else {
							setGraphic(null);	
						}
					}
				}
			}
		}
		
		private void initCanvas() {
			ownerDrawCanvas = new javafx.scene.canvas.Canvas();
			ownerDrawContainer = new StackPane();
			InvalidationListener l = o -> { ownerDrawCanvas.setHeight(ownerDrawContainer.getHeight()); sendPaintEvent(); };
			ownerDrawContainer.heightProperty().addListener(l);
			
			l = o -> { ownerDrawCanvas.setWidth(ownerDrawContainer.getWidth()); sendPaintEvent(); };
			ownerDrawContainer.widthProperty().addListener(l);
			ownerDrawContainer.getChildren().add(ownerDrawCanvas);
		}

		@Override
		public DrawableGC internal_new_GC() {
			Font f = currentItem.getFont();
			if( f == null ) {
				f = currentItem.getParent().getFont();
			}
			return new CanvasGC(ownerDrawCanvas, f, currentItem.getBackground(), currentItem.getForeground());
		}
		
		@Override
		public void internal_dispose_GC(DrawableGC gc) {
			gc.dispose();
		}
		
		private void sendPaintEvent() {
			Event event = new Event();
			event.item = currentItem;
			event.gc = new GC(this);
			ownerDrawCanvas.getGraphicsContext2D().clearRect(0,0,ownerDrawCanvas.getWidth(),ownerDrawCanvas.getHeight());
			internal_sendEvent(SWT.PaintItem, event, true);
			event.gc.dispose();
		}
		
		private void sendMeasureEvent() {
			Event event = new Event();
			event.item = currentItem;
			event.gc = new GC(this);
			internal_sendEvent(SWT.MeasureItem, event, true);
			ownerDrawCanvas.setWidth(event.width);
			ownerDrawCanvas.setHeight(event.height);
			event.gc.dispose();
		}
		
		private void sendEraseEvent() {
			// TODO?
		}
		
		@Override
		public void swt_hideEditor(int column) {
			this.editor = null;
			updateText();
			updateImage();
		}

		@Override
		public void swt_showEditor(Control editor, int column) {
			this.editor = editor;
			updateText();
			updateImage();
		}
		
		@Override
		public Rectangle getBounds() {
			return swt_getBounds();
		}
		
		@Override
		public Rectangle swt_getBounds() {
			Bounds bounds = getBoundsInParent();
			Point2D coords = internal_getNativeObject().sceneToLocal(localToScene(0, 0));
			
			return new Rectangle((int)coords.getX(), (int)coords.getY(), (int)bounds.getWidth(), (int)bounds.getHeight());
		}
		
		@Override
		public Rectangle swt_getBounds(int i) {
			return swt_getBounds();
		}

		@Override
		public TreeItem swt_getTreeItem() {
			return currentItem;
		}
		
		@Override
		public int swt_getItemHeight() {
			return (int) getHeight();
		}
		
		@Override
		public long internal_new_GC(GCData data) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void internal_dispose_GC(long handle, GCData data) {
			// TODO Auto-generated method stub
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
	 * @see SWT#SINGLE
	 * @see SWT#MULTI
	 * @see SWT#CHECK
	 * @see SWT#FULL_SELECTION
	 * @see SWT#VIRTUAL
	 * @see SWT#NO_SCROLL
	 * @see Widget#checkSubclass
	 * @see Widget#getStyle
	 */
	public Tree(Composite parent, int style) {
		super(parent, style);
	}

	@Override
	public void addListener(int eventType, Listener listener) {
		super.addListener(eventType, listener);
		switch (eventType) {
		case SWT.MeasureItem:
			measureItem = true;
			break;
		case SWT.PaintItem:
			paintItem = true;
			break;
		case SWT.EraseItem:
			eraseItem = true;
			break;
		default:
			break;
		}
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified
	 * when the user changes the receiver's selection, by sending it one of the
	 * messages defined in the <code>SelectionListener</code> interface.
	 * <p>
	 * When <code>widgetSelected</code> is called, the item field of the event
	 * object is valid. If the receiver has the <code>SWT.CHECK</code> style and
	 * the check selection changes, the event object detail field contains the
	 * value <code>SWT.CHECK</code>. <code>widgetDefaultSelected</code> is
	 * typically called when an item is double-clicked. The item field of the
	 * event object is valid for default selection, but the detail field is not
	 * used.
	 * </p>
	 * 
	 * @param listener
	 *            the listener which should be notified when the user changes
	 *            the receiver's selection
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
		TypedListener typedListener = new TypedListener (listener);
		addListener (SWT.Selection, typedListener);
		addListener (SWT.DefaultSelection, typedListener);
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified
	 * when an item in the receiver is expanded or collapsed by sending it one
	 * of the messages defined in the <code>TreeListener</code> interface.
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
	 * @see TreeListener
	 * @see #removeTreeListener
	 */
	public void addTreeListener(TreeListener listener) {
		TypedListener typedListener = new TypedListener (listener);
		addListener (SWT.Expand, typedListener);
		addListener (SWT.Collapse, typedListener);
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
	protected Region createWidget() {
		rootItem = new javafx.scene.control.TreeItem<TreeItem>();
		container = new AnchorPane();
		treeView = new TreeView<>(rootItem);
		registerConnection(treeView);
		treeView.setShowRoot(false);
		treeView.setCellFactory(new Callback<TreeView<TreeItem>, TreeCell<TreeItem>>() {
			
			@Override
			public TreeCell<TreeItem> call(TreeView<TreeItem> param) {
				return new TreeCellImpl();
			}
		});
		treeView.getSelectionModel().setSelectionMode((style & SWT.MULTI) == SWT.MULTI ? SelectionMode.MULTIPLE : SelectionMode.SINGLE);
		treeView.getSelectionModel().getSelectedItems().addListener(new InvalidationListener() {
			
			@Override
			public void invalidated(Observable observable) {
				if( treeView.getSelectionModel().getSelectedItems().isEmpty() ) {
					internal_sendEvent(SWT.Selection, new Event(), true);
				} else {
					javafx.scene.control.TreeItem<TreeItem> treeItem = treeView.getSelectionModel().getSelectedItems().get(treeView.getSelectionModel().getSelectedItems().size()-1);
					Event evt = new Event();
					evt.item = treeItem.getValue();
					evt.index =  treeView.getRow(treeItem);
					internal_sendEvent(SWT.Selection, evt, true);
				}
			}
		});
		AnchorPane.setTopAnchor(treeView, 0.0);
		AnchorPane.setBottomAnchor(treeView, 0.0);
		AnchorPane.setLeftAnchor(treeView, 0.0);
		AnchorPane.setRightAnchor(treeView, 0.0);
		container.getChildren().add(treeView);
		return container;
	}
	
	/**
	 * Deselects an item in the receiver. If the item was already deselected, it
	 * remains deselected.
	 * 
	 * @param item
	 *            the item to be deselected
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
	 * @since 3.4
	 */
	public void deselect(TreeItem item) {
		Util.logNotImplemented();
	}

	/**
	 * Deselects all selected items in the receiver.
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void deselectAll() {
		Util.logNotImplemented();
	}

	@Override
	public void dispose() {
		javafx.scene.control.TreeItem<TreeItem>[] children = rootItem.getChildren().toArray(new javafx.scene.control.TreeItem[0]);
		// clear the list this makes the remove faster
		rootItem.getChildren().clear();
		
		for( javafx.scene.control.TreeItem<TreeItem> i : children ) {
			i.getValue().dispose();
		}
		
		super.dispose();
	}
	
	static TreeItem[] extractItemArray(java.util.List<javafx.scene.control.TreeItem<TreeItem>> list) {
		TreeItem[] rv = new TreeItem[list.size()];
		int i = 0;
		for( javafx.scene.control.TreeItem<TreeItem> t : list ) {
			rv[i++] = t.getValue();
		}
		return rv;
	}
	
	/**
	 * Returns the column at the given, zero-relative index in the receiver.
	 * Throws an exception if the index is out of range. Columns are returned in
	 * the order that they were created. If no <code>TreeColumn</code>s were
	 * created by the programmer, this method will throw
	 * <code>ERROR_INVALID_RANGE</code> despite the fact that a single column of
	 * data may be visible in the tree. This occurs when the programmer uses the
	 * tree like a list, adding items but never creating a column.
	 * 
	 * @param index
	 *            the index of the column to return
	 * @return the column at the given index
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
	 * @see Tree#getColumnOrder()
	 * @see Tree#setColumnOrder(int[])
	 * @see TreeColumn#getMoveable()
	 * @see TreeColumn#setMoveable(boolean)
	 * @see SWT#Move
	 * 
	 * @since 3.1
	 */
	public TreeColumn getColumn(int index) {
		if( columns != null && index < columns.size() ) {
			return columns.get(index);
		}
		
		throw new IllegalArgumentException();
	}

	/**
	 * Returns the number of columns contained in the receiver. If no
	 * <code>TreeColumn</code>s were created by the programmer, this value is
	 * zero, despite the fact that visually, one column of items may be visible.
	 * This occurs when the programmer uses the tree like a list, adding items
	 * but never creating a column.
	 * 
	 * @return the number of columns
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
	public int getColumnCount() {
		return columns != null ? columns.size() : 0;
	}

	/**
	 * Returns an array of zero-relative integers that map the creation order of
	 * the receiver's items to the order in which they are currently being
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
	 * 
	 * @see Tree#setColumnOrder(int[])
	 * @see TreeColumn#getMoveable()
	 * @see TreeColumn#setMoveable(boolean)
	 * @see SWT#Move
	 * 
	 * @since 3.2
	 */
	public int[] getColumnOrder() {
		int[] rv = new int[columns.size()];
		
		int i = 0;
		for( javafx.scene.control.TreeTableColumn<TreeItem,?> c : treeTableView.getColumns() ) {
			rv[i++] = columns.indexOf(Widget.getWidget(c)); 
		}
		
		return rv;		
	}

	/**
	 * Returns an array of <code>TreeColumn</code>s which are the columns in the
	 * receiver. Columns are returned in the order that they were created. If no
	 * <code>TreeColumn</code>s were created by the programmer, the array is
	 * empty, despite the fact that visually, one column of items may be
	 * visible. This occurs when the programmer uses the tree like a list,
	 * adding items but never creating a column.
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
	 * 
	 * @see Tree#getColumnOrder()
	 * @see Tree#setColumnOrder(int[])
	 * @see TreeColumn#getMoveable()
	 * @see TreeColumn#setMoveable(boolean)
	 * @see SWT#Move
	 * 
	 * @since 3.1
	 */
	public TreeColumn[] getColumns() {
		return columns != null ? columns.toArray(new TreeColumn[0]) : new TreeColumn[0];
	}

	/**
	 * Returns the width in pixels of a grid line.
	 * 
	 * @return the width of a grid line in pixels
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
	public int getGridLineWidth() {
		Util.logNotImplemented();
		return 0;
	}

	/**
	 * Returns the height of the receiver's header
	 * 
	 * @return the height of the header or zero if the header is not visible
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
	public int getHeaderHeight() {
		Util.logNotImplemented();
		return 0;
	}

	/**
	 * Returns <code>true</code> if the receiver's header is visible, and
	 * <code>false</code> otherwise.
	 * <p>
	 * If one of the receiver's ancestors is not visible or some other condition
	 * makes the receiver not visible, this method may still indicate that it is
	 * considered visible even though it may not actually be showing.
	 * </p>
	 * 
	 * @return the receiver's header's visibility state
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
	public boolean getHeaderVisible() {
		Util.logNotImplemented();
		return true;
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
		if( treeView != null ) {
			return rootItem.getChildren().get(index).getValue();
		} else {
			//TODO Implement
			Util.logNotImplemented();
			return null;
		}
	}

	/**
	 * Returns the item at the given point in the receiver or null if no such
	 * item exists. The point is in the coordinate system of the receiver.
	 * <p>
	 * The item that is returned represents an item that could be selected by
	 * the user. For example, if selection only occurs in items in the first
	 * column, then null is returned if the point is outside of the item. Note
	 * that the SWT.FULL_SELECTION style hint, which specifies the selection
	 * policy, determines the extent of the selection.
	 * </p>
	 * 
	 * @param point
	 *            the point used to locate the item
	 * @return the item at the given point, or null if the point is not in a
	 *         selectable item
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
	 */
	public TreeItem getItem(Point point) {
		for( SWTTreeRow t : currentCells.keySet() ) {
			if( t.swt_getBounds().contains(point.x, point.y) ) {
				return t.swt_getTreeItem();
			}
		}
		//TODO We can only search visible cells!!!
		return null;
	}

	/**
	 * Returns the number of items contained in the receiver that are direct
	 * item children of the receiver. The number that is returned is the number
	 * of roots in the tree.
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
		return rootItem.getChildren().size();
	}

	/**
	 * Returns the height of the area which would be used to display
	 * <em>one</em> of the items in the tree.
	 * 
	 * @return the height of one item
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public int getItemHeight() {
		int itemHeight = 1;
		for( SWTTreeRow c : currentCells.keySet() ) {
			itemHeight = (int) Math.max(itemHeight, c.swt_getItemHeight());
		}
		return itemHeight;
	}

	/**
	 * Returns a (possibly empty) array of items contained in the receiver that
	 * are direct item children of the receiver. These are the roots of the
	 * tree.
	 * <p>
	 * Note: This is not the actual structure used by the receiver to maintain
	 * its list of items, so modifying the array will not affect the receiver.
	 * </p>
	 * 
	 * @return the items
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
		return extractItemArray(rootItem.getChildren());
	}

	/**
	 * Returns <code>true</code> if the receiver's lines are visible, and
	 * <code>false</code> otherwise. Note that some platforms draw grid lines
	 * while others may draw alternating row colors.
	 * <p>
	 * If one of the receiver's ancestors is not visible or some other condition
	 * makes the receiver not visible, this method may still indicate that it is
	 * considered visible even though it may not actually be showing.
	 * </p>
	 * 
	 * @return the visibility state of the lines
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
	public boolean getLinesVisible() {
		Util.logNotImplemented();
		return true;
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
		return null;
	}

	/**
	 * Returns an array of <code>TreeItem</code>s that are currently selected in
	 * the receiver. The order of the items is unspecified. An empty array
	 * indicates that no items are selected.
	 * <p>
	 * Note: This is not the actual structure used by the receiver to maintain
	 * its selection, so modifying the array will not affect the receiver.
	 * </p>
	 * 
	 * @return an array representing the selection
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public TreeItem[] getSelection() {
		ObservableList<javafx.scene.control.TreeItem<TreeItem>> selectedItems;
		if( treeView != null ) {
			selectedItems = treeView.getSelectionModel().getSelectedItems();
		} else {
			selectedItems = treeTableView.getSelectionModel().getSelectedItems();
		}
		
		TreeItem[] rv = new TreeItem[selectedItems.size()];
		int i = 0;
		for( javafx.scene.control.TreeItem<TreeItem> t : selectedItems ) {
			rv[i++] = t.getValue();
		}
		
		return rv;
	}

	/**
	 * Returns the number of selected items contained in the receiver.
	 * 
	 * @return the number of selected items
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public int getSelectionCount() {
		if( treeView != null ) {
			return treeView.getSelectionModel().getSelectedIndices().size();
		} else {
			return treeTableView.getSelectionModel().getSelectedIndices().size();
		}
	}

	/**
	 * Returns the column which shows the sort indicator for the receiver. The
	 * value may be null if no column shows the sort indicator.
	 * 
	 * @return the sort indicator
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @see #setSortColumn(TreeColumn)
	 * 
	 * @since 3.2
	 */
	public TreeColumn getSortColumn() {
		Util.logNotImplemented();
		return null;
	}

	/**
	 * Returns the direction of the sort indicator for the receiver. The value
	 * will be one of <code>UP</code>, <code>DOWN</code> or <code>NONE</code>.
	 * 
	 * @return the sort direction
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @see #setSortDirection(int)
	 * 
	 * @since 3.2
	 */
	public int getSortDirection() {
		Util.logNotImplemented();
		return SWT.NONE;
	}

	/**
	 * Returns the item which is currently at the top of the receiver. This item
	 * can change when items are expanded, collapsed, scrolled or new items are
	 * added or removed.
	 * 
	 * @return the item at the top of the receiver
	 * 
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
	public TreeItem getTopItem() {
		Util.logNotImplemented();
		return null;
	}

	/**
	 * Searches the receiver's list starting at the first column (index 0) until
	 * a column is found that is equal to the argument, and returns the index of
	 * that column. If no column is found, returns -1.
	 * 
	 * @param column
	 *            the search column
	 * @return the index of the column
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the column is null</li>
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
	public int indexOf(TreeColumn column) {
		return columns != null ? columns.indexOf(column) : -1;
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
		return rootItem.getChildren().indexOf(item);
	}

	boolean internal_isMeasureItem() {
		return measureItem;
	}
	
	boolean internal_isPaintItem() {
		return paintItem;
	}
	
	boolean internal_isEraseItem() {
		return eraseItem;
	}
	
	public SWTTreeRow internal_getTreeRow(TreeItem item) {
		for( SWTTreeRow c : currentCells.keySet() ) {
			System.err.println(c.swt_getTreeItem().getText());
			if( item == c.swt_getTreeItem() ) {
				return c;
			}
		}
		return null;
	}
	
	public Region internal_getNativeControl() {
		return treeView != null ? treeView : treeTableView;
	}
	
	protected Region internal_getEventTarget() {
		return treeView != null ? treeView : treeTableView;
	}
	
	TreeTableView<TreeItem> internal_getTreeTable() {
		return treeTableView;
	}
	
	public void internal_columnAdded(TreeColumn column) {
		Util.logNotImplemented();
		if( treeTableView == null ) {
			treeTableView = new TreeTableView<TreeItem>();
			container.getChildren().setAll(treeTableView);
		}
	}
	
	protected void internal_setLayout(Layout layout) {
		// Not needed for trees!!!
	}
	
	protected javafx.scene.canvas.Canvas internal_initCanvas() {
		Util.logNotImplemented();
		return null;
	}
	
	@Override
	public Region internal_getNativeObject() {
		return container;
	}
	
	void internal_itemAdded(TreeItem item) {
		rootItem.getChildren().add(item.internal_getNativeObject());
	}
	
	void internal_itemAdded(TreeItem item, int index) {
		rootItem.getChildren().add(index, item.internal_getNativeObject());
	}
	
	void internal_itemRemoved(TreeItem item) {
		rootItem.getChildren().remove(item.internal_getNativeObject());
	}
	
	/**
	 * Removes all of the items from the receiver.
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void removeAll() {
		javafx.scene.control.TreeItem<TreeItem>[] children = rootItem.getChildren().toArray(new javafx.scene.control.TreeItem[0]);
		rootItem.getChildren().clear();
		for( javafx.scene.control.TreeItem<TreeItem> t : children ) {
			t.getValue().dispose();
		}
	}

	/**
	 * Removes the listener from the collection of listeners who will be
	 * notified when the user changes the receiver's selection.
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
		removeListener(SWT.Selection, listener);
		removeListener(SWT.DefaultSelection, listener);
	}

	/**
	 * Removes the listener from the collection of listeners who will be
	 * notified when items in the receiver are expanded or collapsed.
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
	 * @see TreeListener
	 * @see #addTreeListener
	 */
	public void removeTreeListener(TreeListener listener) {
		removeListener(SWT.Expand, listener);
		removeListener(SWT.Collapse, listener);
	}

	/**
	 * Selects an item in the receiver. If the item was already selected, it
	 * remains selected.
	 * 
	 * @param item
	 *            the item to be selected
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
	 * @since 3.4
	 */
	public void select(final TreeItem item) {
		internal_runNoEvent(new Runnable() {
			@Override
			public void run() {
				if( treeView != null ) {
					treeView.getSelectionModel().select(item.internal_getNativeObject());
				} else {
					treeTableView.getSelectionModel().select(item.internal_getNativeObject());
				}
			}
		});
	}

	/**
	 * Selects all of the items in the receiver.
	 * <p>
	 * If the receiver is single-select, do nothing.
	 * </p>
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void selectAll() {
		internal_runNoEvent(new Runnable() {
			@Override
			public void run() {
				if( treeView != null ) {
					treeView.getSelectionModel().selectAll();	
				} else {
					treeTableView.getSelectionModel().selectAll();
				}
			}
		});
	}

	/**
	 * Display a mark indicating the point at which an item will be inserted.
	 * The drop insert item has a visual hint to show where a dragged item will
	 * be inserted when dropped on the tree.
	 * 
	 * @param item
	 *            the insert item. Null will clear the insertion mark.
	 * @param before
	 *            true places the insert mark above 'item'. false places the
	 *            insert mark below 'item'.
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
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
	 */
	public void setInsertMark(TreeItem item, boolean before) {
		Util.logNotImplemented();
	}

	/**
	 * Sets the order that the items in the receiver should be displayed in to
	 * the given argument which is described in terms of the zero-relative
	 * ordering of when the items were added.
	 * 
	 * @param order
	 *            the new order to display the items
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
	 *                <li>ERROR_NULL_ARGUMENT - if the item order is null</li>
	 *                <li>ERROR_INVALID_ARGUMENT - if the item order is not the
	 *                same length as the number of items</li>
	 *                </ul>
	 * 
	 * @see Tree#getColumnOrder()
	 * @see TreeColumn#getMoveable()
	 * @see TreeColumn#setMoveable(boolean)
	 * @see SWT#Move
	 * 
	 * @since 3.2
	 */
	public void setColumnOrder(int[] order) {
		Util.logNotImplemented();
	}

	/**
	 * Marks the receiver's header as visible if the argument is
	 * <code>true</code>, and marks it invisible otherwise.
	 * <p>
	 * If one of the receiver's ancestors is not visible or some other condition
	 * makes the receiver not visible, marking it visible may not actually cause
	 * it to be displayed.
	 * </p>
	 * 
	 * @param show
	 *            the new visibility state
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
	public void setHeaderVisible(boolean show) {
		Util.logNotImplemented();
	}

	/**
	 * Sets the number of root-level items contained in the receiver.
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

	/**
	 * Marks the receiver's lines as visible if the argument is
	 * <code>true</code>, and marks it invisible otherwise. Note that some
	 * platforms draw grid lines while others may draw alternating row colors.
	 * <p>
	 * If one of the receiver's ancestors is not visible or some other condition
	 * makes the receiver not visible, marking it visible may not actually cause
	 * it to be displayed.
	 * </p>
	 * 
	 * @param show
	 *            the new visibility state
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
	public void setLinesVisible(boolean show) {
		Util.logNotImplemented();
	}

	/**
	 * Sets the receiver's selection to the given item. The current selection is
	 * cleared before the new item is selected, and if necessary the receiver is
	 * scrolled to make the new selection visible.
	 * <p>
	 * If the item is not in the receiver, then it is ignored.
	 * </p>
	 * 
	 * @param item
	 *            the item to select
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
	 * @since 3.2
	 */
	public void setSelection(final TreeItem item) {
		internal_runNoEvent(new Runnable() {
			@Override
			public void run() {
				if( treeView != null ) {
					treeView.getSelectionModel().select(item.internal_getNativeObject());
					treeView.scrollTo(treeView.getRow(item.internal_getNativeObject()));
				} else {
					treeTableView.getSelectionModel().select(item.internal_getNativeObject());
					treeTableView.scrollTo(treeTableView.getRow(item.internal_getNativeObject()));
				}
			}
		});
	}

	/**
	 * Sets the receiver's selection to be the given array of items. The current
	 * selection is cleared before the new items are selected, and if necessary
	 * the receiver is scrolled to make the new selection visible.
	 * <p>
	 * Items that are not in the receiver are ignored. If the receiver is
	 * single-select and multiple items are specified, then all items are
	 * ignored.
	 * </p>
	 * 
	 * @param items
	 *            the array of items
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the array of items is null</li>
	 *                <li>ERROR_INVALID_ARGUMENT - if one of the items has been
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
	 * @see Tree#deselectAll()
	 */
	public void setSelection(final TreeItem[] items) {
		internal_runNoEvent(new Runnable() {
			@Override
			public void run() {
				java.util.List<javafx.scene.control.TreeItem<TreeItem>> l = new ArrayList<>(items.length);
				for( TreeItem i : items ) {
					l.add(i.internal_getNativeObject());
				}
				
				if( treeView != null ) {
					MultipleSelectionModel<javafx.scene.control.TreeItem<TreeItem>> selectionModel = treeView.getSelectionModel();
					selectionModel.clearSelection();
					
					for( javafx.scene.control.TreeItem<TreeItem> i : l ) {
						selectionModel.select(i);
					}
					if( ! l.isEmpty() ) {
						treeView.getFocusModel().focus(treeView.getRow(l.get(0)));
						treeView.scrollTo(treeView.getRow(l.get(0)));
					}
				} else {
					TreeTableViewSelectionModel<TreeItem> selectionModel = treeTableView.getSelectionModel();
					selectionModel.clearSelection();
					
					for( javafx.scene.control.TreeItem<TreeItem> i : l ) {
						selectionModel.select(i);
					}
					
					if( ! l.isEmpty() ) {
						treeTableView.scrollTo(treeTableView.getRow(l.get(0)));	
					}
				}
			}
		});
	}

	/**
	 * Sets the column used by the sort indicator for the receiver. A null value
	 * will clear the sort indicator. The current sort column is cleared before
	 * the new column is set.
	 * 
	 * @param column
	 *            the column used by the sort indicator or <code>null</code>
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_ARGUMENT - if the column is disposed</li>
	 *                </ul>
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
	public void setSortColumn(TreeColumn column) {
		Util.logNotImplemented();
	}

	/**
	 * Sets the direction of the sort indicator for the receiver. The value can
	 * be one of <code>UP</code>, <code>DOWN</code> or <code>NONE</code>.
	 * 
	 * @param direction
	 *            the direction of the sort indicator
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
	public void setSortDirection(int direction) {
		Util.logNotImplemented();
	}

	/**
	 * Sets the item which is currently at the top of the receiver. This item
	 * can change when items are expanded, collapsed, scrolled or new items are
	 * added or removed.
	 * 
	 * @param item
	 *            the item to be shown
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
	 * @see Tree#getTopItem()
	 * 
	 * @since 2.1
	 */
	public void setTopItem(TreeItem item) {
		Util.logNotImplemented();
	}

	/**
	 * Shows the column. If the column is already showing in the receiver, this
	 * method simply returns. Otherwise, the columns are scrolled until the
	 * column is visible.
	 * 
	 * @param column
	 *            the column to be shown
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
	public void showColumn(TreeColumn column) {
		treeTableView.scrollToColumn(column.internal_getNativeObject());
	}

	/**
	 * Shows the item. If the item is already showing in the receiver, this
	 * method simply returns. Otherwise, the items are scrolled and expanded
	 * until the item is visible.
	 * 
	 * @param item
	 *            the item to be shown
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
	 * @see Tree#showSelection()
	 */
	public void showItem(TreeItem item) {
		Util.logNotImplemented();
	}

	/**
	 * Shows the selection. If the selection is already showing in the receiver,
	 * this method simply returns. Otherwise, the items are scrolled until the
	 * selection is visible.
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @see Tree#showItem(TreeItem)
	 */
	public void showSelection() {
		Util.logNotImplemented();
	}

}

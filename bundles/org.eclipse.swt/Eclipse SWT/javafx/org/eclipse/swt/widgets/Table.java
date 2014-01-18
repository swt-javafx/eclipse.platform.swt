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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.WeakHashMap;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.FocusModel;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.Skin;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Drawable;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.GCData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.CanvasGC;
import org.eclipse.swt.internal.Util;
import org.eclipse.swt.widgets.TableColumn.TableColumnCell;
import org.eclipse.swt.widgets.TableItem.AttributeType;
import org.eclipse.swt.widgets.TableItem.Registration;

import com.sun.javafx.scene.control.skin.ListViewSkin;
import com.sun.javafx.scene.control.skin.TableViewSkin;
import com.sun.javafx.scene.control.skin.VirtualFlow;

/**
 * Instances of this class implement a selectable user interface object that
 * displays a list of images and strings and issues notification when selected.
 * <p>
 * The item children that may be added to instances of this class must be of
 * type <code>TableItem</code>.
 * </p>
 * <p>
 * Style <code>VIRTUAL</code> is used to create a <code>Table</code> whose
 * <code>TableItem</code>s are to be populated by the client on an on-demand
 * basis instead of up-front. This can provide significant performance
 * improvements for tables that are very large or for which
 * <code>TableItem</code> population is expensive (for example, retrieving
 * values from an external source).
 * </p>
 * <p>
 * Here is an example of using a <code>Table</code> with style
 * <code>VIRTUAL</code>: <code><pre>
 *  final Table table = new Table (parent, SWT.VIRTUAL | SWT.BORDER);
 *  table.setItemCount (1000000);
 *  table.addListener (SWT.SetData, new Listener () {
 *      public void handleEvent (Event event) {
 *          TableItem item = (TableItem) event.item;
 *          int index = table.indexOf (item);
 *          item.setText ("Item " + index);
 *          System.out.println (item.getText ());
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
 * <dd>SINGLE, MULTI, CHECK, FULL_SELECTION, HIDE_SELECTION, VIRTUAL, NO_SCROLL</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection, DefaultSelection, SetData, MeasureItem, EraseItem, PaintItem</dd>
 * </dl>
 * </p>
 * <p>
 * Note: Only one of the styles SINGLE, and MULTI may be specified.
 * </p>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 * 
 * @see <a href="http://www.eclipse.org/swt/snippets/#table">Table, TableItem,
 *      TableColumn snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example:
 *      ControlExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further
 *      information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class Table extends Composite {

	private AnchorPane container; 
	private TableView<TableItem> tableView; 
	private ListView<TableItem> listView;
	private ObservableList<TableItem> list;
	
	private java.util.List<TableColumn> columns = new ArrayList<TableColumn>();
	private WeakHashMap<SWTTableRow, Boolean> currentRows = new WeakHashMap<>();
	private InvalidationListener selectionListener;
	
	private ScrollBar hBar;
	private ScrollBar vBar;
	
	private boolean measureItem;
	private boolean paintItem;
	private boolean eraseItem;
	
	public interface SWTTableRow {
		public double getHeight();
		public TableItem getTableItem();
		public void hideEditor(int column);
		public void showEditor(Control editor, int column);
		public Rectangle getBounds();
		public Rectangle getBounds(int index);
	}
	
	class TableRowImpl extends TableRow<TableItem> implements SWTTableRow {
		private TableItem currentItem;
		
		@Override
		protected void updateItem(TableItem item, boolean empty) {
			if( (style & SWT.VIRTUAL) == SWT.VIRTUAL ) {
				if( item != null && (item.state & Widget.DATA_SET) != Widget.DATA_SET ) {
					item.state |= Widget.DATA_SET;
					Event evt = new Event();
					evt.item = item;
					internal_sendEvent(SWT.SetData, evt, true);
				}
			}
			super.updateItem(item, empty);
			currentItem = item;
			
			if( item != null && ! empty ) {
				currentRows.put(this, Boolean.TRUE);
			} else {
				currentRows.remove(this);
			}
		}

		@Override
		public TableItem getTableItem() {
			return currentItem;
		}

		@Override
		public void hideEditor(int column) {
			TableColumnCell cell = getColumn(column).getCell(currentItem);
			if( cell != null ) {
				cell.hideEditor();
			}
		}

		@Override
		public void showEditor(Control editor, int column) {
			TableColumnCell cell = getColumn(column).getCell(currentItem);
			if( cell != null ) {
				cell.showEditor(editor);
			}
		}
		
		@Override
		public Rectangle getBounds(int index) {
			TableColumnCell cell = getColumn(index).getCell(currentItem);
			return cell.getBounds();
		}
		
		@Override
		public Rectangle getBounds() {
			Bounds bounds = getBoundsInParent();
			Point2D coords = internal_getNativeObject().sceneToLocal(localToScene(0, 0));
			
			return new Rectangle((int)coords.getX(), (int)coords.getY(), (int)bounds.getWidth(), (int)bounds.getHeight());
		}
	}
	
	class TableListCell extends ListCell<TableItem> implements SWTTableRow, Callback<TableItem.AttributeType, Void>, Drawable {
		private ImageView imageView;
		private CheckBox checkbox;
		private HBox graphicItemsContainer;
		private TableItem currentItem;
		private Registration updateRegistration;
		private Control editor;
		private javafx.scene.canvas.Canvas ownerDrawCanvas;
		private StackPane ownerDrawContainer;
		
		@Override
		protected void layoutChildren() {
			super.layoutChildren();
		}
		
		@Override
		public TableItem getTableItem() {
			return currentItem;
		}
		
		@Override
		public Rectangle getBounds(int index) {
			Bounds bounds = getBoundsInParent();
			Point2D coords = internal_getNativeObject().sceneToLocal(localToScene(0, 0));
			
			return new Rectangle((int)coords.getX(), (int)coords.getY(), (int)bounds.getWidth(), (int)bounds.getHeight());
		}
		
		@Override
		public Rectangle getBounds() {
			Bounds bounds = getBoundsInParent();
			Point2D coords = internal_getNativeObject().sceneToLocal(localToScene(0, 0));
			
			return new Rectangle((int)coords.getX(), (int)coords.getY(), (int)bounds.getWidth(), (int)bounds.getHeight());
		}
		
		@Override
		protected void updateItem(TableItem item, boolean empty) {
			if( (style & SWT.VIRTUAL) == SWT.VIRTUAL ) {
				if( item != null && (item.state & Widget.DATA_SET) != Widget.DATA_SET ) {
					item.state |= Widget.DATA_SET;
					Event evt = new Event();
					evt.item = item;
					internal_sendEvent(SWT.SetData, evt, true);
				}
			}
			
			// remove the old registration
			if( updateRegistration != null ) {
				updateRegistration.dispose();
				updateRegistration = null;
			}
			
			if( item != null ) {
				updateRegistration = item.internal_registerModificationListener(0, this);
			}
			
			currentItem = item;
			
			if( item != null && ! empty ) {
				if( measureItem ) {
					initCanvas();
					sendMeasureEvent();	
				}
				updateText();
				updateImage();
				if( Table.this.listView != null ) {
					Table.this.currentRows.put(this, Boolean.TRUE);	
				}
			} else {
				setText(null);
				setGraphic(null);
				if( Table.this.listView != null ) {
					Table.this.currentRows.remove(this);	
				}
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
			
			if( (Table.this.getStyle() & SWT.CHECK) == SWT.CHECK ) {
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
		public Void call(AttributeType param) {
			switch (param) {
			case IMAGE:
			case CHECK:
				updateImage();
				break;
			case TEXT:
				updateText();
				break;
			default:
				break;
			}
			return null;
		}

		@Override
		public void hideEditor(int column) {
			this.editor = null;
			updateImage();
			updateText();
		}

		@Override
		public void showEditor(final Control editor, int column) {
			this.editor = editor;
			updateImage();
			updateText();
			if( editor != null ) {
				Platform.runLater(new Runnable() {
					
					@Override
					public void run() {
						editor.setFocus();
					}
				});
			}
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

	class FXSelectionListener implements EventHandler<MouseEvent> {
		private TableListCell cell;
		
		public FXSelectionListener(TableListCell cell) {
			this.cell = cell;
		}
		
		@Override
		public void handle(MouseEvent event) {
			if( event.getClickCount() == 2 && cell.currentItem != null ) {
				Event evt = new Event();
				evt.item = cell.currentItem;
				internal_sendEvent(SWT.DefaultSelection, evt, true);
			}
		}
	}
	
	static class CustomListViewSkin extends ListViewSkin<TableItem> {

		public CustomListViewSkin(ListView<TableItem> arg0) {
			super(arg0);
		}
		
		public VirtualFlow<ListCell<TableItem>> swt_getFlow() {
			return flow;
		}
	}
	
	static class CustomTableViewSkin extends TableViewSkin<TableItem> {

		public CustomTableViewSkin(TableView<TableItem> arg0) {
			super(arg0);
		}
		
		public VirtualFlow<TableRow<TableItem>> swt_getFlow() {
			return flow;
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
	 * @see SWT#HIDE_SELECTION
	 * @see SWT#VIRTUAL
	 * @see SWT#NO_SCROLL
	 * @see Widget#checkSubclass
	 * @see Widget#getStyle
	 */
	public Table(Composite parent, int style) {
		super(parent, style);
		// TODO
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
		checkWidget ();
		if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
		TypedListener typedListener = new TypedListener (listener);
		registerListener (SWT.Selection,typedListener);
		registerListener (SWT.DefaultSelection,typedListener);
	}

	/**
	 * Clears the item at the given zero-relative index in the receiver. The
	 * text, icon and other attributes of the item are set to the default value.
	 * If the table was created with the <code>SWT.VIRTUAL</code> style, these
	 * attributes are requested again as needed.
	 * 
	 * @param index
	 *            the index of the item to clear
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
	 * @since 3.0
	 */
	public void clear(int index) {
		if( (style & SWT.VIRTUAL) == SWT.VIRTUAL ) {
			list.get(index).state &= ~Widget.DATA_SET;
		}
	}

	/**
	 * Removes the items from the receiver which are between the given
	 * zero-relative start and end indices (inclusive). The text, icon and other
	 * attributes of the items are set to their default values. If the table was
	 * created with the <code>SWT.VIRTUAL</code> style, these attributes are
	 * requested again as needed.
	 * 
	 * @param start
	 *            the start index of the item to clear
	 * @param end
	 *            the end index of the item to clear
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_RANGE - if either the start or end are
	 *                not between 0 and the number of elements in the list minus
	 *                1 (inclusive)</li>
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
	 * @since 3.0
	 */
	public void clear(int start, int end) {
		if( (style & SWT.VIRTUAL) == SWT.VIRTUAL ) {
			for( ; start <= end; start++ ) {
				list.get(start).state &= ~Widget.DATA_SET;
			}
		}
	}

	/**
	 * Clears the items at the given zero-relative indices in the receiver. The
	 * text, icon and other attributes of the items are set to their default
	 * values. If the table was created with the <code>SWT.VIRTUAL</code> style,
	 * these attributes are requested again as needed.
	 * 
	 * @param indices
	 *            the array of indices of the items
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_RANGE - if the index is not between 0
	 *                and the number of elements in the list minus 1 (inclusive)
	 *                </li>
	 *                <li>ERROR_NULL_ARGUMENT - if the indices array is null</li>
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
	 * @since 3.0
	 */
	public void clear(int[] indices) {
		if( (style & SWT.VIRTUAL) == SWT.VIRTUAL ) {
			for( int i = 0; i < indices.length; i++ ) {
				list.get(i).state &= ~Widget.DATA_SET;
			}
		}
	}

	/**
	 * Clears all the items in the receiver. The text, icon and other attributes
	 * of the items are set to their default values. If the table was created
	 * with the <code>SWT.VIRTUAL</code> style, these attributes are requested
	 * again as needed.
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
	 * @since 3.0
	 */
	public void clearAll() {
		if( (style & SWT.VIRTUAL) == SWT.VIRTUAL ) {
			for( TableItem i : list ) {
				i.state &= ~Widget.DATA_SET;
			}	
		}		
	}

	@Override
	protected AnchorPane createWidget() {
		this.list = FXCollections.observableArrayList();
		this.container = new AnchorPane();
		
		//TODO We could be smarter and init with no control
		this.listView = new ListView<TableItem>(list) {
			@Override
			protected Skin<?> createDefaultSkin() {
				return new CustomListViewSkin(this);
			}
		};
		this.listView.setCellFactory(new Callback<ListView<TableItem>, ListCell<TableItem>>() {
			
			@Override
			public ListCell<TableItem> call(ListView<TableItem> param) {
				TableListCell c = new TableListCell();
				c.addEventFilter(MouseEvent.MOUSE_CLICKED,new FXSelectionListener(c));
				return c;
			}
		});
		this.listView.getSelectionModel().setSelectionMode((style & SWT.MULTI) == SWT.MULTI ? SelectionMode.MULTIPLE : SelectionMode.SINGLE);
		
		selectionListener = new InvalidationListener() {
			
			@Override
			public void invalidated(Observable observable) {
				MultipleSelectionModel<TableItem> model = listView != null ? listView.getSelectionModel() : tableView.getSelectionModel();
				
				if( model.isEmpty() ) {
					internal_sendEvent(SWT.Selection, new Event(), true);
				} else {
					Event evt = new Event();
					TableItem tableItem = model.getSelectedItems().get(model.getSelectedItems().size()-1);
					evt.item = tableItem;
					evt.index = list.indexOf(list);
					internal_sendEvent(SWT.Selection, evt, true);
				}
			}
		};
		this.listView.getSelectionModel().getSelectedItems().addListener(selectionListener);
		registerConnection(listView);
		AnchorPane.setTopAnchor(listView, 0.0);
		AnchorPane.setBottomAnchor(listView, 0.0);
		AnchorPane.setLeftAnchor(listView, 0.0);
		AnchorPane.setRightAnchor(listView, 0.0);
		this.container.getChildren().add(listView);
		return this.container;
	}
	
	/**
	 * Deselects the item at the given zero-relative index in the receiver. If
	 * the item at the index was already deselected, it remains deselected.
	 * Indices that are out of range are ignored.
	 * 
	 * @param index
	 *            the index of the item to deselect
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void deselect(int index) {
		checkWidget();
		getSelectionModel().clearSelection(index);
	}

	/**
	 * Deselects the items at the given zero-relative indices in the receiver.
	 * If the item at the given zero-relative index in the receiver is selected,
	 * it is deselected. If the item at the index was not selected, it remains
	 * deselected. The range of the indices is inclusive. Indices that are out
	 * of range are ignored.
	 * 
	 * @param start
	 *            the start index of the items to deselect
	 * @param end
	 *            the end index of the items to deselect
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void deselect(int start, int end) {
		checkWidget();
		SelectionModel<TableItem> model = getSelectionModel();
		for (; start <= end; start++) {
			model.clearSelection(start);
		}
	}

	/**
	 * Deselects the items at the given zero-relative indices in the receiver.
	 * If the item at the given zero-relative index in the receiver is selected,
	 * it is deselected. If the item at the index was not selected, it remains
	 * deselected. Indices that are out of range and duplicate indices are
	 * ignored.
	 * 
	 * @param indices
	 *            the array of indices for the items to deselect
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the set of indices is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void deselect(int[] indices) {
		checkWidget();
		SelectionModel<TableItem> model = getSelectionModel();
		for (int idx : indices) {
			model.clearSelection(idx);
		}
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
		checkWidget();
		getSelectionModel().clearSelection();
	}

	/**
	 * Returns the column at the given, zero-relative index in the receiver.
	 * Throws an exception if the index is out of range. Columns are returned in
	 * the order that they were created. If no <code>TableColumn</code>s were
	 * created by the programmer, this method will throw
	 * <code>ERROR_INVALID_RANGE</code> despite the fact that a single column of
	 * data may be visible in the table. This occurs when the programmer uses
	 * the table like a list, adding items but never creating a column.
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
	 * @see Table#getColumnOrder()
	 * @see Table#setColumnOrder(int[])
	 * @see TableColumn#getMoveable()
	 * @see TableColumn#setMoveable(boolean)
	 * @see SWT#Move
	 */
	public TableColumn getColumn(int index) {
		checkWidget();
		if (!(0 <= index && index < getColumnCount())) {
			error(SWT.ERROR_INVALID_RANGE);
		}
		return columns.get(index);
	}

	/**
	 * Returns the number of columns contained in the receiver. If no
	 * <code>TableColumn</code>s were created by the programmer, this value is
	 * zero, despite the fact that visually, one column of items may be visible.
	 * This occurs when the programmer uses the table like a list, adding items
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
	 */
	public int getColumnCount() {
		return columns.size();
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
	 * @see Table#setColumnOrder(int[])
	 * @see TableColumn#getMoveable()
	 * @see TableColumn#setMoveable(boolean)
	 * @see SWT#Move
	 * 
	 * @since 3.1
	 */
	public int[] getColumnOrder() {
		int[] rv = new int[columns.size()];
		
		int i = 0;
		for( javafx.scene.control.TableColumn<TableItem,?> c : tableView.getColumns() ) {
			rv[i++] = columns.indexOf(Widget.getWidget(c)); 
		}
		
		return rv;
	}

	/**
	 * Returns an array of <code>TableColumn</code>s which are the columns in
	 * the receiver. Columns are returned in the order that they were created.
	 * If no <code>TableColumn</code>s were created by the programmer, the array
	 * is empty, despite the fact that visually, one column of items may be
	 * visible. This occurs when the programmer uses the table like a list,
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
	 * @see Table#getColumnOrder()
	 * @see Table#setColumnOrder(int[])
	 * @see TableColumn#getMoveable()
	 * @see TableColumn#setMoveable(boolean)
	 * @see SWT#Move
	 */
	public TableColumn[] getColumns() {
		checkWidget();
		return columns.toArray(new TableColumn[0]);
	}

	private VirtualFlow<?> getFlow() {
		VirtualFlow<?> flow = null;
		if( listView != null ) {
			if( listView.getSkin() != null ) {
				flow = ((CustomListViewSkin)listView.getSkin()).swt_getFlow();	
			}
		} else {
			if( tableView.getSkin() != null ) {
				flow = ((CustomTableViewSkin)tableView.getSkin()).swt_getFlow();	
			}
		}
		
		return flow;
	}
	
	private FocusModel<TableItem> getFocusModel() {
		return listView != null ? listView.getFocusModel() : tableView.getFocusModel();
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
	 * @since 2.0
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
	 */
	public boolean getHeaderVisible() {
		Util.logNotImplemented();
		return false;
	}

	@Override
	public ScrollBar getHorizontalBar() {
		if( hBar != null ) {
			return hBar;
		}
		
		VirtualFlow<?> flow;
		forceSizeProcessing();
		if( listView != null ) {
			flow = ((CustomListViewSkin)listView.getSkin()).swt_getFlow();
		} else {
			flow = ((CustomTableViewSkin)tableView.getSkin()).swt_getFlow();
		}
		
		try {
			Method m = VirtualFlow.class.getDeclaredMethod("getHbar");
			m.setAccessible(true);
			javafx.scene.control.ScrollBar bar = (javafx.scene.control.ScrollBar) m.invoke(flow);
			hBar = new ScrollBar(this, bar, SWT.HORIZONTAL);
			return hBar;
		} catch (Throwable t) {
			throw new IllegalStateException(t);
		}
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
	public TableItem getItem(int index) {
		if (!(0 <= index && index < getItemCount())) {
			error(SWT.ERROR_INVALID_RANGE);
		}
		return list.get(index);
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
	public TableItem getItem(Point point) {
		for( SWTTableRow r : currentRows.keySet()) {
			if( columns.isEmpty() ) {
				if( r.getBounds(0).contains(point) ) {
					return r.getTableItem();
				}
			}
			
		}
		Util.logNotImplemented();
		return null;
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
		return list.size();
	}

	/**
	 * Returns the height of the area which would be used to display
	 * <em>one</em> of the items in the receiver.
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
		for( SWTTableRow c : currentRows.keySet() ) {
			itemHeight = (int) Math.max(itemHeight, c.getHeight());
		}
		return itemHeight;
	}

	/**
	 * Returns a (possibly empty) array of <code>TableItem</code>s which are the
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
	public TableItem[] getItems() {
		checkWidget();
		return list.toArray(new TableItem[list.size()]);
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
	 */
	public boolean getLinesVisible() {
		Util.logNotImplemented();
		return true;
	}

	/**
	 * Returns an array of <code>TableItem</code>s that are currently selected
	 * in the receiver. The order of the items is unspecified. An empty array
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
	public TableItem[] getSelection() {
		checkWidget();
		return getSelectionModel().getSelectedItems().toArray(new TableItem[0]);
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
		checkWidget();
		return getSelectionModel().getSelectedIndices().size();
	}

	/**
	 * Returns the zero-relative index of the item which is currently selected
	 * in the receiver, or -1 if no item is selected.
	 * 
	 * @return the index of the selected item
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public int getSelectionIndex() {
		checkWidget();
		return getSelectionModel().getSelectedIndex();
	}

	/**
	 * Returns the zero-relative indices of the items which are currently
	 * selected in the receiver. The order of the indices is unspecified. The
	 * array is empty if no items are selected.
	 * <p>
	 * Note: This is not the actual structure used by the receiver to maintain
	 * its selection, so modifying the array will not affect the receiver.
	 * </p>
	 * 
	 * @return the array of indices of the selected items
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public int[] getSelectionIndices() {
		checkWidget();
		ObservableList<Integer> l = getSelectionModel().getSelectedIndices();
		int[] rv = new int[l.size()];
		int j = 0;
		for( Integer i : l ) {
			rv[j++] = i.intValue();
		}
		return rv;
	}

	private MultipleSelectionModel<TableItem> getSelectionModel() {
		return listView != null ? listView.getSelectionModel() : tableView.getSelectionModel();
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
	 * @see #setSortColumn(TableColumn)
	 * 
	 * @since 3.2
	 */
	public TableColumn getSortColumn() {
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
		return 0;
	}

	/**
	 * Returns the zero-relative index of the item which is currently at the top
	 * of the receiver. This index can change when items are scrolled or new
	 * items are added or removed.
	 * 
	 * @return the index of the top item
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public int getTopIndex() {
		IndexedCell<TableItem> cell;
		if( tableView != null ) {
			CustomTableViewSkin skin = (CustomTableViewSkin) tableView.getSkin();
			cell = skin.swt_getFlow().getFirstVisibleCell();
		} else {
			CustomListViewSkin skin = (CustomListViewSkin) listView.getSkin();
			cell = skin.swt_getFlow().getFirstVisibleCell();
		}
		if( cell != null ) {
			return cell.getIndex();
		}
		return -1;
	}

	@Override
	public ScrollBar getVerticalBar() {
		if( vBar != null ) {
			return vBar;
		}
		
		VirtualFlow<?> flow;
		if( listView != null ) {
			flow = ((CustomListViewSkin)listView.getSkin()).swt_getFlow();
		} else {
			flow = ((CustomTableViewSkin)tableView.getSkin()).swt_getFlow();
		}
		
		try {
			Method m = VirtualFlow.class.getDeclaredMethod("getVbar");
			m.setAccessible(true);
			javafx.scene.control.ScrollBar bar = (javafx.scene.control.ScrollBar) m.invoke(flow);
			vBar = new ScrollBar(this, bar, SWT.VERTICAL);
			return vBar;
		} catch (Throwable t) {
			throw new IllegalStateException(t);
		}
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
	 */
	public int indexOf(TableColumn column) {
		checkWidget();
		return columns.indexOf(column);
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
	public int indexOf(TableItem item) {
		checkWidget();
		return list.indexOf(item);
	}

	private void initTableView() {
		if( tableView == null ) {
			currentRows.clear();
			uninitListeners();
			unregisterConnection(listView);
			ListView<TableItem> listView = this.listView;
			ContextMenu contextMenu = listView.getContextMenu();
			this.listView = null;
			
			listView.getSelectionModel().getSelectedItems().removeListener(selectionListener);
			listView.setItems(null);
			listView.setContextMenu(null);
			
			tableView = new TableView<TableItem>(list) {
				@Override
				protected Skin<?> createDefaultSkin() {
					return new CustomTableViewSkin(this);
				}
			};
			registerConnection(tableView);
			tableView.setRowFactory(new Callback<TableView<TableItem>, TableRow<TableItem>>() {
				
				@Override
				public TableRow<TableItem> call(TableView<TableItem> param) {
					return new TableRowImpl();
				}
			});
			tableView.getSelectionModel().getSelectedItems().addListener(selectionListener);
			tableView.setContextMenu(contextMenu);
			AnchorPane.setTopAnchor(tableView, 0.0);
			AnchorPane.setBottomAnchor(tableView, 0.0);
			AnchorPane.setLeftAnchor(tableView, 0.0);
			AnchorPane.setRightAnchor(tableView, 0.0);
			container.getChildren().setAll(tableView);
			initListeners();
		}
	}
	
	@Override
	protected Region internal_getEventTarget() {
		return internal_getNativeControl();
	}
	
	@Override
	public AnchorPane internal_getNativeObject() {
		return container;
	}
	
	@Override
	public Region internal_getNativeControl() {
		return tableView != null ? tableView : listView;
	}
	
	@Override
	public DrawableGC internal_new_GC() {
		return new Device.NoOpDrawableGC(this,getFont());
	}
	
	@Override
	protected void internal_attachControl(Control c) {
		if( c instanceof TableCursor ) { 
			c.internal_getNativeObject().setManaged(false);
			container.getChildren().add(c.internal_getNativeObject());
		}
	}
	
	@Override
	protected void internal_attachControl(int idx, Control c) {
	}
	
	@Override
	protected void internal_detachControl(Control c) {
	}
	
	@Override
	protected void internal_doLayout() {
		// no layouting needed
	}
	
	@Override
	protected double internal_getHeight() {
		return container.getHeight();
	}
	
	@Override
	protected double internal_getPrefHeight() {
		return container.prefHeight(javafx.scene.control.Control.USE_COMPUTED_SIZE);
	}
	
	@Override
	protected double internal_getPrefWidth() {
		return container.prefWidth(javafx.scene.control.Control.USE_COMPUTED_SIZE);
	}
	
	@Override
	protected double internal_getWidth() {
		return container.getHeight();
	}
	
	public void internal_itemAdded(TableItem item) {
		list.add(item);
	}

	public void internal_itemAdded(TableItem item, int index) {
		list.add(index,item);
	}

	public void internal_columnAdded(TableColumn column) {
		columns.add(column);
		initTableView();
		tableView.getColumns().add(column.internal_getNativeObject());
	}
	
	public void internal_columnAdded(TableColumn column, int index) {
		columns.add(index,column);
		initTableView();
		tableView.getColumns().add(index, column.internal_getNativeObject());
	}
	
	public SWTTableRow internal_getTableRow(TableItem item) {
		for( SWTTableRow c : currentRows.keySet() ) {
			if( item == c.getTableItem() ) {
				return c;
			}
		}
		return null;
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

	/**
	 * Returns <code>true</code> if the item is selected, and <code>false</code>
	 * otherwise. Indices out of range are ignored.
	 * 
	 * @param index
	 *            the index of the item
	 * @return the selection state of the item at the index
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public boolean isSelected(int index) {
		checkWidget();
		return getSelectionModel().isSelected(index);
	}

	private void remove(TableItem[] items) {
		for( int i = 0; i < items.length; i++ ) {
			items[i].dispose();
		}
	}

	/**
	 * Removes the item from the receiver at the given zero-relative index.
	 * 
	 * @param index
	 *            the index for the item
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
	public void remove(int index) {
		checkWidget();
		//TODO Dispose
		list.remove(index);
	}

	/**
	 * Removes the items from the receiver which are between the given
	 * zero-relative start and end indices (inclusive).
	 * 
	 * @param start
	 *            the start of the range
	 * @param end
	 *            the end of the range
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_RANGE - if either the start or end are
	 *                not between 0 and the number of elements in the list minus
	 *                1 (inclusive)</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void remove(int start, int end) {
		checkWidget();
		//TODO Dispose
		list.remove(start, end);
	}

	/**
	 * Removes the items from the receiver's list at the given zero-relative
	 * indices.
	 * 
	 * @param indices
	 *            the array of indices of the items
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_RANGE - if the index is not between 0
	 *                and the number of elements in the list minus 1 (inclusive)
	 *                </li>
	 *                <li>ERROR_NULL_ARGUMENT - if the indices array is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void remove(int[] indices) {
		checkWidget();
		//TODO Dispose
		Util.removeListIndices(list, indices);
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
		checkWidget();
		//TODO Dispose
		list.clear();
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
	 * @see #addSelectionListener(SelectionListener)
	 */
	public void removeSelectionListener(SelectionListener listener) {
		checkWidget ();
		removeListener(SWT.Selection, listener);
		removeListener(SWT.DefaultSelection,listener);	
	}

	/**
	 * Selects the item at the given zero-relative index in the receiver. If the
	 * item at the index was already selected, it remains selected. Indices that
	 * are out of range are ignored.
	 * 
	 * @param index
	 *            the index of the item to select
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void select(final int index) {
		checkWidget();
		internal_runNoEvent(new Runnable() {
			@Override
			public void run() {
				getSelectionModel().select(index);
			}
		});
	}

	/**
	 * Selects the items in the range specified by the given zero-relative
	 * indices in the receiver. The range of indices is inclusive. The current
	 * selection is not cleared before the new items are selected.
	 * <p>
	 * If an item in the given range is not selected, it is selected. If an item
	 * in the given range was already selected, it remains selected. Indices
	 * that are out of range are ignored and no items will be selected if start
	 * is greater than end. If the receiver is single-select and there is more
	 * than one item in the given range, then all indices are ignored.
	 * </p>
	 * 
	 * @param start
	 *            the start of the range
	 * @param end
	 *            the end of the range
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @see Table#setSelection(int,int)
	 */
	public void select(int start, int end) {
		checkWidget();
		internal_runNoEvent(new Runnable() {
			
			@Override
			public void run() {
				getSelectionModel().selectRange(start, end);
			}
		});
	}

	/**
	 * Selects the items at the given zero-relative indices in the receiver. The
	 * current selection is not cleared before the new items are selected.
	 * <p>
	 * If the item at a given index is not selected, it is selected. If the item
	 * at a given index was already selected, it remains selected. Indices that
	 * are out of range and duplicate indices are ignored. If the receiver is
	 * single-select and multiple indices are specified, then all indices are
	 * ignored.
	 * </p>
	 * 
	 * @param indices
	 *            the array of indices for the items to select
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the array of indices is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @see Table#setSelection(int[])
	 */
	public void select(int[] indices) {
		checkWidget();
		internal_runNoEvent(new Runnable() {
			@Override
			public void run() {
				if( indices.length == 0 ) {
					getSelectionModel().clearSelection();
				} else if( indices.length == 1 ) {
					getSelectionModel().selectIndices(indices[0]);
				} else {
					int idx = indices[0];
					int[] rest = new int[indices.length-1];
					System.arraycopy(indices, 1, rest, 0, indices.length-1);
					getSelectionModel().selectIndices(idx,rest);
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
		checkWidget();
		internal_runNoEvent(new Runnable() {
			@Override
			public void run() {
				getSelectionModel().clearSelection();
			}
		});
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
	 * @see Table#getColumnOrder()
	 * @see TableColumn#getMoveable()
	 * @see TableColumn#setMoveable(boolean)
	 * @see SWT#Move
	 * 
	 * @since 3.1
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
	 */
	public void setHeaderVisible(boolean show) {
		Util.logNotImplemented();
	}

	/**
	 * Sets the number of items contained in the receiver.
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
	 * @since 3.0
	 */
	public void setItemCount(int count) {
		if( (style & SWT.VIRTUAL) == SWT.VIRTUAL ) {
			if( count < list.size() ) {
				java.util.List<TableItem> sublist = list.subList(count, list.size()-1);
				TableItem[] items = new TableItem[sublist.size()];
				sublist.toArray(items);
				sublist.clear();
				remove(items);
			} else if( count > list.size() ) {
				int l = count - list.size();
				TableItem[] items = new TableItem[l];
				for( int i = 0; i < l; i++ ) {
					items[i] = new TableItem(this);
				}
				list.addAll(items);
			}
		}
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
	 */
	public void setLinesVisible(boolean show) {
		Util.logNotImplemented();
	}

	/**
	 * Selects the item at the given zero-relative index in the receiver. The
	 * current selection is first cleared, then the new item is selected, and if
	 * necessary the receiver is scrolled to make the new selection visible.
	 * 
	 * @param index
	 *            the index of the item to select
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @see Table#deselectAll()
	 * @see Table#select(int)
	 */
	public void setSelection(int index) {
		checkWidget();
		internal_runNoEvent(new Runnable() {
			
			@Override
			public void run() {
				getSelectionModel().clearAndSelect(index);
				getFocusModel().focus(index);
				showItem(list.get(index));
			}
		});
	}

	/**
	 * Selects the items in the range specified by the given zero-relative
	 * indices in the receiver. The range of indices is inclusive. The current
	 * selection is cleared before the new items are selected, and if necessary
	 * the receiver is scrolled to make the new selection visible.
	 * <p>
	 * Indices that are out of range are ignored and no items will be selected
	 * if start is greater than end. If the receiver is single-select and there
	 * is more than one item in the given range, then all indices are ignored.
	 * </p>
	 * 
	 * @param start
	 *            the start index of the items to select
	 * @param end
	 *            the end index of the items to select
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @see Table#deselectAll()
	 * @see Table#select(int,int)
	 */
	public void setSelection(int start, int end) {
		checkWidget();
		internal_runNoEvent(new Runnable() {
			@Override
			public void run() {
				getSelectionModel().clearSelection();
				select(start, end);
				getFocusModel().focus(end);
			}
		});
	}

	/**
	 * Selects the items at the given zero-relative indices in the receiver. The
	 * current selection is cleared before the new items are selected, and if
	 * necessary the receiver is scrolled to make the new selection visible.
	 * <p>
	 * Indices that are out of range and duplicate indices are ignored. If the
	 * receiver is single-select and multiple indices are specified, then all
	 * indices are ignored.
	 * </p>
	 * 
	 * @param indices
	 *            the indices of the items to select
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the array of indices is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @see Table#deselectAll()
	 * @see Table#select(int[])
	 */
	public void setSelection(int[] indices) {
		checkWidget();
		internal_runNoEvent(new Runnable() {
			
			@Override
			public void run() {
				getSelectionModel().clearSelection();
				if( indices.length > 0 ) {
					int[] sorted = new int[indices.length];
					System.arraycopy(indices, 0, sorted, 0, indices.length);
					Arrays.sort(sorted);
					getFocusModel().focus(sorted[sorted.length-1]);
				}
			}
		});
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
	public void setSelection(TableItem item) {
		checkWidget();
		internal_runNoEvent(new Runnable() {
			@Override
			public void run() {
				getSelectionModel().clearSelection();
				getSelectionModel().select(item);
				getFocusModel().focus(tableView.getSelectionModel().getSelectedIndex());
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
	 * @see Table#deselectAll()
	 * @see Table#select(int[])
	 * @see Table#setSelection(int[])
	 */
	public void setSelection(TableItem[] items) {
		checkWidget();
		internal_runNoEvent(new Runnable() {
			@Override
			public void run() {
				getSelectionModel().clearSelection();
				for( TableItem t : items ) {
					getSelectionModel().select(t);	
				}
				getFocusModel().focus(getSelectionModel().getSelectedIndex());
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
	public void setSortColumn(TableColumn column) {
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
	 * Sets the zero-relative index of the item which is currently at the top of
	 * the receiver. This index can change when items are scrolled or new items
	 * are added and removed.
	 * 
	 * @param index
	 *            the index of the top item
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setTopIndex(int index) {
		if( tableView != null ) {
			tableView.scrollTo(index);
		} else {
			listView.scrollTo(index);
		}
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
	 *                <li>ERROR_NULL_ARGUMENT - if the column is null</li>
	 *                <li>ERROR_INVALID_ARGUMENT - if the column has been
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
	public void showColumn(TableColumn column) {
		if( tableView != null ) {
			tableView.scrollToColumn(column.internal_getNativeObject());
		}
	}

	/**
	 * Shows the item. If the item is already showing in the receiver, this
	 * method simply returns. Otherwise, the items are scrolled until the item
	 * is visible.
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
	 * @see Table#showSelection()
	 */
	public void showItem(TableItem item) {
		// Only scroll if not yet visible
		VirtualFlow<?> flow = getFlow();
		if( flow != null ) {
			SWTTableRow first = (SWTTableRow) getFlow().getFirstVisibleCellWithinViewPort();
			SWTTableRow last = (SWTTableRow) getFlow().getLastVisibleCellWithinViewPort();
			
			int i = list.indexOf(first.getTableItem());
			int j;
			
			if( last != null && last.getTableItem() != null ) {
				j = list.indexOf(last.getTableItem());	
			} else {
				j = list.size()-1;
			}
			
			for( ; i <= j; i++ ) {
				if( list.get(i) == item ) {
					return;
				}
			}
		}
		
		if( tableView != null ) {
			tableView.scrollTo(item);
		} else {
			listView.scrollTo(item);
		}
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
	 * @see Table#showItem(TableItem)
	 */
	public void showSelection() {
		int idx = getSelectionIndex();
		if( idx >= 0 ) {
			showItem(list.get(idx));
		}
	}

}

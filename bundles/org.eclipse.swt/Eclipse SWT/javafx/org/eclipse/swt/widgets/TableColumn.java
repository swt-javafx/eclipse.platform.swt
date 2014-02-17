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

import java.util.WeakHashMap;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableRow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Drawable;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.GCData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.CanvasGC;
import org.eclipse.swt.internal.Util;
import org.eclipse.swt.widgets.TableItem.AttributeType;
import org.eclipse.swt.widgets.TableItem.Registration;

/**
 * Instances of this class represent a column in a table widget.
 * <p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>LEFT, RIGHT, CENTER</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Move, Resize, Selection</dd>
 * </dl>
 * </p>
 * <p>
 * Note: Only one of the styles LEFT, RIGHT and CENTER may be specified.
 * </p>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 * 
 * @see <a href="http://www.eclipse.org/swt/snippets/#table">Table, TableItem,
 *      TableColumn snippets</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further
 *      information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class TableColumn extends Item {

	javafx.scene.control.TableColumn<TableItem, CellItem> nativeColumn;
	private int index;
	private Table parent;
	private boolean moveable;
	
	private WeakHashMap<TableColumnCell, Boolean> cellMap = new WeakHashMap<>();
	
	class CellItem {
		private int index;
		private TableItem item;
		
		public CellItem(int index, TableItem item) {
			this.index = index;
			this.item = item;
		}
		
		String getText() {
			return item.getText(index);
		}
		
		Image getImage() {
			return item.getImage(index);
		}

		Color getForeground() {
			return item.getForeground(index);
		}

		Color getBackground() {
			return item.getBackground(index);
		}

		Font getFont() {
			return item.getFont(index);
		}
		
		
	}
	
	TableColumnCell getCell(TableItem item) {
		for( TableColumnCell c : cellMap.keySet() ) {
			if( c.currentItem.item == item ) {
				return c;
			}
		}
		parent.showItem(item);
		parent.showColumn(this);
		
		for( TableColumnCell c : cellMap.keySet() ) {
			if( c.currentItem.item == item ) {
				return c;
			}
		}
		
		return null; 
	}

	class TableColumnCell extends TableCell<TableItem, CellItem> implements Callback<TableItem.AttributeType, Void>, Drawable {
		private ImageView imageView;
		private CellItem currentItem;
		private Registration registration;
		private Control editor;
		private InvalidationListener selectionListner;
		private javafx.scene.canvas.Canvas ownerDrawCanvas;
		private StackPane ownerDrawContainer;
		private HBox graphicItemsContainer;
		
		public TableColumnCell() {
			selectionListner = new InvalidationListener() {
				
				@Override
				public void invalidated(Observable observable) {
					updateVisuals();
				}
			};
			parentProperty().addListener(new ChangeListener<Parent>() {

				@Override
				public void changed(
						ObservableValue<? extends Parent> observable,
						Parent oldValue, Parent newValue) {
					if( oldValue != null && oldValue instanceof TableRow<?> ) {
						((TableRow<?>)oldValue).selectedProperty().removeListener(selectionListner);
					}
					if( newValue != null && newValue instanceof TableRow<?> ) {
						((TableRow<?>)newValue).selectedProperty().addListener(selectionListner);
					}
					updateVisuals();
				}
			});
		}
		
		
		@Override
		protected void updateItem(CellItem item, boolean empty) {
			if( registration != null ) {
				registration.dispose();
				registration = null;
			}
			
			if( item != null ) {
				registration = item.item.internal_registerModificationListener(item.index, this);
			}
			
			currentItem = item;
			
			if( item != null && ! empty ) {
				updateText();
				updateImage();
				updateVisuals();
				cellMap.put(this, Boolean.TRUE);
			} else {
				setText(null);
				setGraphic(null);
				setStyle(null);
				cellMap.remove(this);
			}
			super.updateItem(item, empty);
		}
		
		private void updateVisuals() {
			if( currentItem == null || getParent() == null || !(getParent() instanceof TableRow<?>) ) {
				setStyle(null);
				return;
			}
			if(  ((TableRow<?>)getParent()).isSelected() ) {
				Font f = currentItem.getFont();
				if( f != null ) {
					setStyle("-fx-font: "+f.internal_getAsCSSString()+";");
				} else {
					setStyle(null);
				}
			} else {
				Color fg = currentItem.getForeground();
				Color bg = currentItem.getBackground();
				Font f = currentItem.getFont();
				
				StringBuilder b = new StringBuilder();
				if( fg != null ) {
					String rgb = "rgb("+fg.getRed()+","+fg.getGreen()+","+fg.getBlue()+")";
					b.append("-fx-text-inner-color:"+rgb+" ; -fx-text-background-color: "+rgb+";");
				}
				if( bg != null ) {
					b.append("-fx-background-color: rgb("+bg.getRed()+","+bg.getGreen()+","+bg.getBlue()+");");
				}			
				if( f != null ) {
					b.append("-fx-font: "+f.internal_getAsCSSString()+";");
				}
				
				setStyle(b.length() == 0 ? null : b.toString());				
			}
		}
		
		private void updateText() {
			if( editor != null || parent.internal_isPaintItem() ) {
				setText(null);
			} else {
				setText(currentItem.getText());	
			}
		}
		
		private void updateImage() {
			//TODO Keep or hide image???
			Image img = currentItem.getImage();
			if( img != null ) {
				if( imageView == null ) {
					imageView = new ImageView(img.internal_getImage());
				} else {
					imageView.setImage(img.internal_getImage());
				}
				
				if( editor != null ) {
					HBox h = new HBox();
					h.getChildren().setAll(imageView, editor.nativeControl);
					setGraphic(h);
				} else {
					if( parent.internal_isPaintItem() ) {
						if( graphicItemsContainer == null ) {
							graphicItemsContainer = new HBox();
						}
						
						if( ownerDrawCanvas == null ) {
							initCanvas();
						}
						sendMeasureEvent();
//						sendPaintEvent();
						graphicItemsContainer.getChildren().setAll(imageView,ownerDrawContainer);
						setGraphic(ownerDrawContainer);
					} else {
						setGraphic(imageView);	
					}
				}
			} else {
				if( editor != null ) {
					setGraphic(editor.nativeControl);
				} else {
					if( parent.internal_isPaintItem() ) {
						if( ownerDrawCanvas == null ) {
							initCanvas();
						}
						sendMeasureEvent();
//						sendPaintEvent();
						setGraphic(ownerDrawContainer);
					} else {
						setGraphic(null);	
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
			Font f = currentItem.item.getFont(index);
			if( f == null ) {
				f = currentItem.item.getParent().getFont();
			}
			return new CanvasGC(ownerDrawCanvas, f, currentItem.item.getBackground(index), currentItem.item.getForeground(index));
		}
		
		@Override
		public void internal_dispose_GC(DrawableGC gc) {
			gc.dispose();
		}
		
		private void sendPaintEvent() {
			Event event = new Event();
			event.item = currentItem.item;
			event.gc = new GC(this);
			ownerDrawCanvas.getGraphicsContext2D().clearRect(0,0,ownerDrawCanvas.getWidth(),ownerDrawCanvas.getHeight());
			parent.sendEvent(SWT.PaintItem, event, true);
			event.gc.dispose();
		}
		
		private void sendMeasureEvent() {
			Event event = new Event();
			event.item = currentItem.item;
			event.gc = new GC(this);
			parent.sendEvent(SWT.MeasureItem, event, true);
			ownerDrawCanvas.setWidth(event.width);
			ownerDrawCanvas.setHeight(event.height);
			event.gc.dispose();
		}
		
		private void sendEraseEvent() {
			// TODO?
		}
		
		public void hideEditor() {
			this.editor = null;
			updateImage();
			updateText();
		}
		
		public void showEditor(Control control) {
			this.editor = control;
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
		public Void call(AttributeType param) {
			switch (param) {
			case IMAGE:
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
		
		public Rectangle getBounds() {
			Bounds bounds = getBoundsInParent();
			Point2D coords = parent.nativeControl.sceneToLocal(localToScene(0, 0));
			Rectangle r = new Rectangle((int)coords.getX(), (int)coords.getY(), (int)bounds.getWidth(), (int)bounds.getHeight());
			return r;
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
	
	//FIXME We need to remove this and reuse the selection model so that
	// we track keybindings double click are still handled like this
	class FXSelectionListener implements EventHandler<MouseEvent> {
		private TableColumnCell cell;
		
		public FXSelectionListener(TableColumnCell cell) {
			this.cell = cell;
		}
		
		@Override
		public void handle(MouseEvent event) {
			if( event.getClickCount() == 2 && cell.currentItem != null ) {
				Event evt = new Event();
				evt.item = cell.currentItem.item;
				sendEvent(SWT.DefaultSelection, evt, true);
			} else {
				Event evt = new Event();
				evt.item = cell.currentItem.item;
				sendEvent(SWT.Selection, evt, true);
			}
		}
	}

	/**
	 * Constructs a new instance of this class given its parent (which must be a
	 * <code>Table</code>) and a style value describing its behavior and
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
	 * @see SWT#LEFT
	 * @see SWT#RIGHT
	 * @see SWT#CENTER
	 * @see Widget#checkSubclass
	 * @see Widget#getStyle
	 */
	public TableColumn(Table parent, int style) {
		super(parent, style);
		this.parent = parent;
		// before we call the add because we'll maybe get called immediately
		index = parent.getColumnCount();
		createWidget();
	}

	/**
	 * Constructs a new instance of this class given its parent (which must be a
	 * <code>Table</code>), a style value describing its behavior and
	 * appearance, and the index at which to place it in the items maintained by
	 * its parent.
	 * <p>
	 * The style value is either one of the style constants defined in class
	 * <code>SWT</code> which is applicable to instances of this class, or must
	 * be built by <em>bitwise OR</em>'ing together (that is, using the
	 * <code>int</code> "|" operator) two or more of those <code>SWT</code>
	 * style constants. The class description lists the style constants that are
	 * applicable to the class. Style bits are also inherited from superclasses.
	 * </p>
	 * <p>
	 * Note that due to a restriction on some platforms, the first column is
	 * always left aligned.
	 * </p>
	 * 
	 * @param parent
	 *            a composite control which will be the parent of the new
	 *            instance (cannot be null)
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
	 * @see SWT#LEFT
	 * @see SWT#RIGHT
	 * @see SWT#CENTER
	 * @see Widget#checkSubclass
	 * @see Widget#getStyle
	 */
	public TableColumn(Table parent, int style, int index) {
		super(parent, style);
		this.parent = parent;
		// before we call the add because we'll maybe get called immediately
		this.index = index;
		createWidget();
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified
	 * when the control is moved or resized, by sending it one of the messages
	 * defined in the <code>ControlListener</code> interface.
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
	 * @see ControlListener
	 * @see #removeControlListener
	 */
	public void addControlListener(ControlListener listener) {
		Util.logNotImplemented();
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified
	 * when the control is selected by the user, by sending it one of the
	 * messages defined in the <code>SelectionListener</code> interface.
	 * <p>
	 * <code>widgetSelected</code> is called when the column header is selected.
	 * <code>widgetDefaultSelected</code> is not called.
	 * </p>
	 * 
	 * @param listener
	 *            the listener which should be notified when the control is
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
		Util.logNotImplemented();
	}

	@Override
	void createHandle() {
		nativeColumn = new javafx.scene.control.TableColumn<TableItem, CellItem>();
		nativeColumn.setCellValueFactory(new Callback<CellDataFeatures<TableItem,CellItem>, ObservableValue<CellItem>>() {
			@Override
			public ObservableValue<CellItem> call(CellDataFeatures<TableItem, CellItem> param) {
				return new SimpleObjectProperty<TableColumn.CellItem>(new CellItem(index,param.getValue())); 
			}
		});
		nativeColumn.setCellFactory(new Callback<javafx.scene.control.TableColumn<TableItem, CellItem>, TableCell<TableItem,CellItem>>() {
			@Override
			public TableCell<TableItem, CellItem> call(
					javafx.scene.control.TableColumn<TableItem, CellItem> param) {
				TableColumnCell i = new TableColumnCell();
				i.addEventFilter(MouseEvent.MOUSE_CLICKED, new FXSelectionListener(i));
				return i;
			}
		});
	}
	
	/**
	 * Returns a value which describes the position of the text or image in the
	 * receiver. The value will be one of <code>LEFT</code>, <code>RIGHT</code>
	 * or <code>CENTER</code>.
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
		if( (getStyle() & SWT.LEFT) != 0 ) {
			return SWT.LEFT;
		} else if( (getStyle() & SWT.CENTER) != 0 ) {
			return SWT.CENTER;
		} else if( (getStyle() & SWT.RIGHT) != 0 ) {
			return SWT.RIGHT;
		}
		return SWT.LEFT;
	}

	/**
	 * Gets the moveable attribute. A column that is not moveable cannot be
	 * reordered by the user by dragging the header but may be reordered by the
	 * programmer.
	 * 
	 * @return the moveable attribute
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
	 * @see TableColumn#setMoveable(boolean)
	 * @see SWT#Move
	 * 
	 * @since 3.1
	 */
	public boolean getMoveable() {
		checkWidget();
		return moveable;
	}

	/**
	 * Returns the receiver's parent, which must be a <code>Table</code>.
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
	public Table getParent() {
		checkWidget();
		return parent;
	}

	/**
	 * Gets the resizable attribute. A column that is not resizable cannot be
	 * dragged by the user but may be resized by the programmer.
	 * 
	 * @return the resizable attribute
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public boolean getResizable() {
		checkWidget();
		return nativeColumn.isResizable();
	}

	@Override
	public String getText() {
		checkWidget();
		return notNullString(nativeColumn.getText());
	}

	/**
	 * Returns the receiver's tool tip text, or null if it has not been set.
	 * 
	 * @return the receiver's tool tip text
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
	public String getToolTipText() {
		Util.logNotImplemented();
		return null;
	}

	/**
	 * Gets the width of the receiver.
	 * 
	 * @return the width
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public int getWidth() {
		checkWidget();
		return (int) nativeColumn.getWidth();
	}

	/**
	 * Causes the receiver to be resized to its preferred size. For a composite,
	 * this involves computing the preferred size from its layout, if there is
	 * one.
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 */
	public void pack() {
		Util.logNotImplemented();
	}

	@Override
	void registerHandle() {
		super.registerHandle();
		parent.internal_columnAdded(this);
	}
	
	/**
	 * Removes the listener from the collection of listeners who will be
	 * notified when the control is moved or resized.
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
	 * @see ControlListener
	 * @see #addControlListener
	 */
	public void removeControlListener(ControlListener listener) {
		Util.logNotImplemented();
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
		Util.logNotImplemented();
	}

	/**
	 * Controls how text and images will be displayed in the receiver. The
	 * argument should be one of <code>LEFT</code>, <code>RIGHT</code> or
	 * <code>CENTER</code>.
	 * <p>
	 * Note that due to a restriction on some platforms, the first column is
	 * always left aligned.
	 * </p>
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
		style &= ~(SWT.LEFT|SWT.CENTER|SWT.RIGHT);
		style |= alignment;
	}

	@Override
	public void setImage(Image image) {
		super.setImage(image);
		if( image == null ) {
			nativeColumn.setGraphic(null);
		} else {
			nativeColumn.setGraphic(new ImageView(image.internal_getImage()));			
		}
	}
	
	/**
	 * Sets the moveable attribute. A column that is moveable can be reordered
	 * by the user by dragging the header. A column that is not moveable cannot
	 * be dragged by the user but may be reordered by the programmer.
	 * 
	 * @param moveable
	 *            the moveable attribute
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
	 * @see Table#getColumnOrder()
	 * @see TableColumn#getMoveable()
	 * @see SWT#Move
	 * 
	 * @since 3.1
	 */
	public void setMoveable(boolean moveable) {
		this.moveable = moveable;
		Util.logNotImplemented();
	}

	/**
	 * Sets the resizable attribute. A column that is resizable can be resized
	 * by the user dragging the edge of the header. A column that is not
	 * resizable cannot be dragged by the user but may be resized by the
	 * programmer.
	 * 
	 * @param resizable
	 *            the resize attribute
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setResizable(boolean resizable) {
		nativeColumn.setResizable(resizable);
	}

	@Override
	public void setText(String string) {
		checkWidget();
		nativeColumn.setText(string);
	}
	
	/**
	 * Sets the receiver's tool tip text to the argument, which may be null
	 * indicating that the default tool tip for the control will be shown. For a
	 * control that has a default tool tip, such as the Tree control on Windows,
	 * setting the tool tip text to an empty string replaces the default,
	 * causing no tool tip text to be shown.
	 * <p>
	 * The mnemonic indicator (character '&amp;') is not displayed in a tool
	 * tip. To display a single '&amp;' in the tool tip, the character '&amp;'
	 * can be escaped by doubling it in the string.
	 * </p>
	 * 
	 * @param string
	 *            the new tool tip text (or null)
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
	public void setToolTipText(String string) {
		Util.logNotImplemented();
	}

	/**
	 * Sets the width of the receiver.
	 * 
	 * @param width
	 *            the new width
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setWidth(int width) {
		nativeColumn.setPrefWidth(width);
	}

}

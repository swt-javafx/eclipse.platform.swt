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

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.CanvasGC;
import org.eclipse.swt.internal.Util;

/**
 * Instances of this class are controls which are capable of containing other
 * controls.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>NO_BACKGROUND, NO_FOCUS, NO_MERGE_PAINTS, NO_REDRAW_RESIZE,
 * NO_RADIO_GROUP, EMBEDDED, DOUBLE_BUFFERED</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * <p>
 * Note: The <code>NO_BACKGROUND</code>, <code>NO_FOCUS</code>,
 * <code>NO_MERGE_PAINTS</code>, and <code>NO_REDRAW_RESIZE</code> styles are
 * intended for use with <code>Canvas</code>. They can be used with
 * <code>Composite</code> if you are drawing your own, but their behavior is
 * undefined if they are used with subclasses of <code>Composite</code> other
 * than <code>Canvas</code>.
 * </p>
 * <p>
 * Note: The <code>CENTER</code> style, although undefined for composites, has
 * the same value as <code>EMBEDDED</code> which is used to embed widgets from
 * other widget toolkits into SWT. On some operating systems (GTK, Motif), this
 * may cause the children of this composite to be obscured.
 * </p>
 * <p>
 * This class may be subclassed by custom control implementors who are building
 * controls that are constructed from aggregates of other controls.
 * </p>
 * 
 * @see Canvas
 * @see <a href="http://www.eclipse.org/swt/snippets/#composite">Composite
 *      snippets</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further
 *      information</a>
 */
public class Composite extends Scrollable {

	private java.util.List<Control> children = new ArrayList<Control>();
	
	private FXLayoutPane controlContainer;
	
	private ToggleGroup group;
	
	private Layout layout;

	private AnchorPane scrollable;
	
	private javafx.scene.canvas.Canvas canvas;
	
	private static final double SCROLLBAR_WIDTH = 20.0;
	
	private ScrollBar vScroll;
	private ScrollBar hScroll;
	private Node corner;
	
	private Menu menu;
	
	private String tooltipText;
	private Tooltip tooltip;
	
	private static EventHandler<MouseEvent> FOCUS_HANDLER;
	private static EventHandler<ContextMenuEvent> CONTEXT_MENU_HANDLER;
	
	Composite() {
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
	 *            a widget which will be the parent of the new instance (cannot
	 *            be null)
	 * @param style
	 *            the style of widget to construct
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the parent</li>
	 *                </ul>
	 * 
	 * @see SWT#NO_BACKGROUND
	 * @see SWT#NO_FOCUS
	 * @see SWT#NO_MERGE_PAINTS
	 * @see SWT#NO_REDRAW_RESIZE
	 * @see SWT#NO_RADIO_GROUP
	 * @see SWT#EMBEDDED
	 * @see SWT#DOUBLE_BUFFERED
	 * @see Widget#getStyle
	 */
	public Composite(Composite parent, int style) {
		super(parent, style);
	}

	protected void applyBorderStyle() {
		if( (style & SWT.BORDER) == SWT.BORDER && internal_getNativeObject() != null ) {
			internal_getNativeObject().setStyle("-fx-boder-style: solid; -fx-border-width: 1px; -fx-border-color: gray;");	
		}
	}

	@Override
	public void addPaintListener(PaintListener listener) {
		super.addPaintListener(listener);
//TODO		internal_initCanvas();
	}
	
	@Override
	public void addListener(int eventType, Listener listener) {
		super.addListener(eventType, listener);
		if( eventType == SWT.Paint ) {
//TODO			internal_initCanvas();
		} else if( eventType == SWT.KeyDown || eventType == SWT.KeyUp ) {
			internal_enableFocusTraversable();
		}
	}
	
	/**
	 * Clears any data that has been cached by a Layout for all widgets that are
	 * in the parent hierarchy of the changed control up to and including the
	 * receiver. If an ancestor does not have a layout, it is skipped.
	 * 
	 * @param changed
	 *            an array of controls that changed state and require a
	 *            recalculation of size
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_ARGUMENT - if the changed array is null
	 *                any of its controls are null or have been disposed</li>
	 *                <li>ERROR_INVALID_PARENT - if any control in changed is
	 *                not in the widget tree of the receiver</li>
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
	public void changed(Control[] changed) {
		// TODO
	}

	public Point computeSize(int wHint, int hHint, boolean flushCache) {
		forceSizeProcessing();
		int width;
		int height;
		
		if( layout != null ) {
			Point size = layout.computeSize(this, wHint, hHint, flushCache);
			Rectangle trim = computeTrim (0, 0, size.x, size.y);
			width = trim.width;
			height = trim.height;
		} else {
			width = (int) Math.ceil(internal_getPrefWidth());
			height = (int) Math.ceil(internal_getPrefHeight());
		}
		
		if (wHint != SWT.DEFAULT) width = wHint;
		if (hHint != SWT.DEFAULT) height = hHint;
				
		return new Point(width, height);
	}
	
	@Override
	protected Region createWidget() {
		scrollable = new AnchorPane() {
			@Override
			protected void layoutChildren() {
				super.layoutChildren();
				double endCorrection = 0;
				if( corner != null && vScroll.getVisible() && hScroll.getVisible() ) {
					endCorrection = SCROLLBAR_WIDTH;
				}
				
				if( vScroll != null ) {
					vScroll.internal_getNativeObject().resizeRelocate(getWidth()-SCROLLBAR_WIDTH, 0, SCROLLBAR_WIDTH, getHeight()-endCorrection);	
				}
				
				if( hScroll != null ) {
					hScroll.internal_getNativeObject().resizeRelocate(0, getHeight()-SCROLLBAR_WIDTH, getWidth()-endCorrection, SCROLLBAR_WIDTH);
				}
				
				if( corner != null ) {
					if( hScroll.getVisible() && vScroll.getVisible() ) {
						corner.setVisible(true);	
					} else {
						corner.setVisible(false);
					}
					corner.resizeRelocate(getWidth()-SCROLLBAR_WIDTH, getHeight()-SCROLLBAR_WIDTH, SCROLLBAR_WIDTH, SCROLLBAR_WIDTH);
				}
				
			}
		};
		
		javafx.scene.shape.Rectangle r = new javafx.scene.shape.Rectangle();
		r.widthProperty().bind(scrollable.widthProperty());
		r.heightProperty().bind(scrollable.heightProperty());
		scrollable.setClip(r);

		scrollable.getStyleClass().add(getStyleClassname());
		controlContainer = internal_createLayoutPane();
		registerConnection(controlContainer);
		
		AnchorPane.setLeftAnchor(controlContainer, 0.0);
		AnchorPane.setTopAnchor(controlContainer, 0.0);
		
		scrollable.getChildren().add(controlContainer);
		
		if( (style & SWT.V_SCROLL) == SWT.V_SCROLL ) {
			AnchorPane.setRightAnchor(controlContainer, SCROLLBAR_WIDTH);
			vScroll = new ScrollBar(this,SWT.VERTICAL);
			Node n = vScroll.internal_getNativeObject();
			n.setManaged(false);
			scrollable.getChildren().add(n);
		} else {
			AnchorPane.setRightAnchor(controlContainer, 0.0);
		}
		
		if( (style & SWT.H_SCROLL) == SWT.H_SCROLL ) {
			AnchorPane.setBottomAnchor(controlContainer, SCROLLBAR_WIDTH);
			hScroll = new ScrollBar(this,SWT.HORIZONTAL);
			Node n = hScroll.internal_getNativeObject();
			n.setManaged(false);
			scrollable.getChildren().add(n);
		} else {
			AnchorPane.setBottomAnchor(controlContainer, 0.0);
		}
		
		if( hScroll != null && vScroll != null ) {
			corner = new StackPane();
	        corner.getStyleClass().setAll("corner");
	        scrollable.getChildren().add(corner);
		}

		applyBorderStyle();
		
		return scrollable;
	}
	
	/**
	 * Fills the interior of the rectangle specified by the arguments, with the
	 * receiver's background.
	 * 
	 * <p>
	 * The <code>offsetX</code> and <code>offsetY</code> are used to map from
	 * the <code>gc</code> origin to the origin of the parent image background.
	 * This is useful to ensure proper alignment of the image background.
	 * </p>
	 * 
	 * @param gc
	 *            the gc where the rectangle is to be filled
	 * @param x
	 *            the x coordinate of the rectangle to be filled
	 * @param y
	 *            the y coordinate of the rectangle to be filled
	 * @param width
	 *            the width of the rectangle to be filled
	 * @param height
	 *            the height of the rectangle to be filled
	 * @param offsetX
	 *            the image background x offset
	 * @param offsetY
	 *            the image background y offset
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the gc is null</li>
	 *                <li>ERROR_INVALID_ARGUMENT - if the gc has been disposed</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @since 3.6
	 */
	public void drawBackground(GC gc, int x, int y, int width, int height,
			int offsetX, int offsetY) {
		Util.logNotImplemented(); 
	}

	Composite findDeferredControl () {
		return this;
		// TODO return layoutCount > 0 ? this : parent.findDeferredControl ();
	}

	/**
	 * Returns the receiver's background drawing mode. This will be one of the
	 * following constants defined in class <code>SWT</code>:
	 * <code>INHERIT_NONE</code>, <code>INHERIT_DEFAULT</code>,
	 * <code>INHERIT_FORCE</code>.
	 * 
	 * @return the background mode
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @see SWT
	 * 
	 * @since 3.2
	 */
	public int getBackgroundMode() {
		Util.logNotImplemented();
		return 0;
	}

	/**
	 * Returns a (possibly empty) array containing the receiver's children.
	 * Children are returned in the order that they are drawn. The topmost
	 * control appears at the beginning of the array. Subsequent controls draw
	 * beneath this control and appear later in the array.
	 * <p>
	 * Note: This is not the actual structure used by the receiver to maintain
	 * its list of children, so modifying the array will not affect the
	 * receiver.
	 * </p>
	 * 
	 * @return an array of children
	 * 
	 * @see Control#moveAbove
	 * @see Control#moveBelow
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public Control[] getChildren() {
		return children.toArray(new Control[children.size()]);
	}

	@Override
	public Rectangle getClientArea() {
		forceSizeProcessing();
		return new Rectangle(0, 0, (int)internal_getClientAreaWidth(), (int)internal_getClientAreaHeight());
	}
	
	private static EventHandler<ContextMenuEvent> getContextMenuHandler() {
		if( CONTEXT_MENU_HANDLER == null ) {
			CONTEXT_MENU_HANDLER = new EventHandler<ContextMenuEvent>() {
				
				@Override
				public void handle(ContextMenuEvent event) {
					Node n = (Node) event.getTarget();
					Control c = (Control) Widget.getWidget(n);
					
					if( c != null ) {
						ContextMenu ctm = (ContextMenu) c.getMenu().internal_getNativeObject();
						ctm.show(n, event.getScreenX(), event.getScreenY());	
					}
					
					//TODO Do we need more code e.g. to hide?
				}
			};
		}
		return CONTEXT_MENU_HANDLER;
	}

	private static EventHandler<MouseEvent> getFocusHandler() {
		if( FOCUS_HANDLER == null ) {
			FOCUS_HANDLER = new EventHandler<MouseEvent>() {
				
				@Override
				public void handle(MouseEvent event) {
					Control c = Widget.getWidget(event.getSource());
					if( c != null ) {
						c.setFocus();	
					}
				}
			};
		}
		return FOCUS_HANDLER;
	}
	
	@Override
	public ScrollBar getHorizontalBar() {
		return hScroll;
	}

	/**
	 * Returns layout which is associated with the receiver, or null if one has
	 * not been set.
	 * 
	 * @return the receiver's layout or null
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public Layout getLayout() {
		return layout;
	}

	/**
	 * Returns <code>true</code> if the receiver has deferred the performing of
	 * layout, and <code>false</code> otherwise.
	 * 
	 * @return the receiver's deferred layout state
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @see #setLayoutDeferred(boolean)
	 * @see #isLayoutDeferred()
	 * 
	 * @since 3.1
	 */
	public boolean getLayoutDeferred() {
		// TODO
		return false;
	}

	@Override
	public Menu getMenu() {
		return menu;
	}
	
	protected String getStyleClassname() {
		return "swt-composite";
	}

	/**
	 * Gets the (possibly empty) tabbing order for the control.
	 * 
	 * @return tabList the ordered list of controls representing the tab order
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @see #setTabList
	 */
	public Control[] getTabList() {
		Util.logNotImplemented();
		return getChildren();
	}

	@Override
	public String getToolTipText() {
		if( controlContainer != null ) {
			return tooltipText;
		}
		return super.getToolTipText();
	}
	
	@Override
	public ScrollBar getVerticalBar() {
		return vScroll;
	}

	protected void internal_attachControl(Control c) {
		controlContainer.getChildren().add(c.internal_getNativeObject());
	}
	
	protected void internal_attachControl(int idx, Control c) {
		controlContainer.getChildren().add(idx, c.internal_getNativeObject());
	}
	
	protected void internal_detachControl(Control c) {
		controlContainer.getChildren().remove(c.internal_getNativeObject());
	}
	
	protected void internal_doLayout() {
		controlContainer.layout();
	}
	
	public void internal_controlAdded(Control c) {
		if( c instanceof Button && (c.getStyle() & SWT.RADIO) == SWT.RADIO && (getStyle() & SWT.NO_RADIO_GROUP) != SWT.NO_RADIO_GROUP ) {
			if( group == null ) {
				group = new ToggleGroup();
			}
			group.getToggles().add((Toggle) c.internal_getNativeObject());
		}
		children.add(c);
		internal_attachControl(c);
	}

	public void internal_controlAdded(int index, Control c) {
		if( c instanceof Button && (c.getStyle() & SWT.RADIO) == SWT.RADIO && (getStyle() & SWT.NO_RADIO_GROUP) != SWT.NO_RADIO_GROUP ) {
			if( group == null ) {
				group = new ToggleGroup();
			}
			group.getToggles().add(index,(Toggle) c.internal_getNativeObject());
		}
		children.add(index, c);
		internal_attachControl(index,c);
	}

	public void internal_controlRemoved(Control c) {
		if( c instanceof Button && (c.getStyle() & SWT.RADIO) == SWT.RADIO && (getStyle() & SWT.NO_RADIO_GROUP) != SWT.NO_RADIO_GROUP ) {
			if( group != null ) {
				group.getToggles().remove(c.internal_getNativeObject());
			}
		}
		children.remove(c);
		internal_detachControl(c);
	}

	public void internal_controlMoveAbove(Control control, Control reference) {
		internal_controlRemoved(control);
		
		if( reference == null ) {
			internal_controlAdded(control);
		} else {
			for( int i = 0; i < children.size(); i++ ) {
				if( children.get(i) == reference ) {
					int idx = i+1;
					if( idx < children.size() ) {
						internal_controlAdded(idx, control);
					} else {
						internal_controlAdded(control);
					}
					return;
				}
			}	
		}
	}
	
	public void internal_controlMoveBelow(Control control, Control reference) {
		internal_controlRemoved(control);
		
		if( reference == null ) {
			internal_controlAdded(control);
		} else {
			for( int i = 0; i < children.size(); i++ ) {
				if( children.get(i) == reference ) {
					int idx = i;
					if( idx < children.size() ) {
						internal_controlAdded(idx, control);
					} else {
						internal_controlAdded(control);
					}
					return;
				}
			}
		}
	}
	
	protected FXLayoutPane internal_createLayoutPane() {
		return new FXLayoutPane(this);
	}
	
	void internal_enableFocusTraversable() {
		if( scrollable != null && ! scrollable.isFocusTraversable() ) {
			scrollable.setFocusTraversable(true);
			scrollable.addEventHandler(MouseEvent.MOUSE_RELEASED, getFocusHandler());
		}		
	}
	
	protected double internal_getClientAreaWidth() {
		if( controlContainer != null ) {
			return controlContainer.getWidth();
		}
		return internal_getWidth();
	}

	protected double internal_getClientAreaHeight() {
		if( controlContainer != null ) {
			return controlContainer.getHeight();
		}
		return internal_getHeight();
	}

	@Override
	public Region internal_getNativeObject() {
		return scrollable;
	}
	
	@Override
	public Region internal_getNativeControl() {
		return controlContainer == null ? super.internal_getNativeControl() : controlContainer;
	}
	
	protected double internal_getPrefWidth() {
		return ((Region)internal_getNativeObject()).prefWidth(-1);
	}

	protected double internal_getPrefHeight() {
		return ((Region)internal_getNativeObject()).prefHeight(-1);
	}

	protected javafx.scene.canvas.Canvas internal_initCanvas() {
		if( canvas == null ) {
			canvas = new javafx.scene.canvas.Canvas(scrollable.getWidth(),scrollable.getHeight()) {
				@Override
				public double minWidth(double height) {
					return 0;
				}
				
				@Override
				public double minHeight(double width) {
					return 0;
				}
				
				@Override
				public double prefHeight(double width) {
					return 0;
				}
				
				@Override
				public double prefWidth(double height) {
					return 0;
				}
			
			};
			
			//TODO Do we need to remove the scrollbars????
			scrollable.getChildren().add(0, canvas);
			InvalidationListener l = new InvalidationListener() {
				
				@Override
				public void invalidated(Observable observable) {
					double w = scrollable.getWidth();
					double h = scrollable.getHeight();
					
					if( vScroll != null ) {
						w -= vScroll.internal_getNativeObject().getWidth();
					}
					
					if( hScroll != null ) {
						h -= hScroll.internal_getNativeObject().getHeight();
					}
					
					canvas.setWidth(w);
					canvas.setHeight(h);
					redraw();
				}
			};
			scrollable.widthProperty().addListener(l);
			scrollable.heightProperty().addListener(l);
			redraw();
		}
		return canvas;
	}
	
	@Override
	public DrawableGC internal_new_GC() {
		if( canvas == null ) {
			return super.internal_new_GC();
		} else {
			return new CanvasGC(canvas,
					getFont(),
					getBackground(),
					getForeground()
					);	
		}
	}
	
	protected void internal_setLayout(Layout layout) {
		controlContainer.setLayout(layout);
	}

	/**
	 * Returns <code>true</code> if the receiver or any ancestor up to and
	 * including the receiver's nearest ancestor shell has deferred the
	 * performing of layouts. Otherwise, <code>false</code> is returned.
	 * 
	 * @return the receiver's deferred layout state
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @see #setLayoutDeferred(boolean)
	 * @see #getLayoutDeferred()
	 * 
	 * @since 3.1
	 */
	public boolean isLayoutDeferred() {
		Util.logNotImplemented();
		return false;
	}

	/**
	 * If the receiver has a layout, asks the layout to <em>lay out</em> (that
	 * is, set the size and location of) the receiver's children. If the
	 * receiver does not have a layout, do nothing.
	 * <p>
	 * This is equivalent to calling <code>layout(true)</code>.
	 * </p>
	 * <p>
	 * Note: Layout is different from painting. If a child is moved or resized
	 * such that an area in the parent is exposed, then the parent will paint.
	 * If no child is affected, the parent will not paint.
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
	public void layout() {
		layout(true);
	}

	/**
	 * If the receiver has a layout, asks the layout to <em>lay out</em> (that
	 * is, set the size and location of) the receiver's children. If the
	 * argument is <code>true</code> the layout must not rely on any information
	 * it has cached about the immediate children. If it is <code>false</code>
	 * the layout may (potentially) optimize the work it is doing by assuming
	 * that none of the receiver's children has changed state since the last
	 * layout. If the receiver does not have a layout, do nothing.
	 * <p>
	 * If a child is resized as a result of a call to layout, the resize event
	 * will invoke the layout of the child. The layout will cascade down through
	 * all child widgets in the receiver's widget tree until a child is
	 * encountered that does not resize. Note that a layout due to a resize will
	 * not flush any cached information (same as <code>layout(false)</code>).
	 * </p>
	 * <p>
	 * Note: Layout is different from painting. If a child is moved or resized
	 * such that an area in the parent is exposed, then the parent will paint.
	 * If no child is affected, the parent will not paint.
	 * </p>
	 * 
	 * @param changed
	 *            <code>true</code> if the layout must flush its caches, and
	 *            <code>false</code> otherwise
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void layout(boolean changed) {
		if (layout == null) return;
		layout (changed, false);
	}

	/**
	 * If the receiver has a layout, asks the layout to <em>lay out</em> (that
	 * is, set the size and location of) the receiver's children. If the changed
	 * argument is <code>true</code> the layout must not rely on any information
	 * it has cached about its children. If it is <code>false</code> the layout
	 * may (potentially) optimize the work it is doing by assuming that none of
	 * the receiver's children has changed state since the last layout. If the
	 * all argument is <code>true</code> the layout will cascade down through
	 * all child widgets in the receiver's widget tree, regardless of whether
	 * the child has changed size. The changed argument is applied to all
	 * layouts. If the all argument is <code>false</code>, the layout will
	 * <em>not</em> cascade down through all child widgets in the receiver's
	 * widget tree. However, if a child is resized as a result of a call to
	 * layout, the resize event will invoke the layout of the child. Note that a
	 * layout due to a resize will not flush any cached information (same as
	 * <code>layout(false)</code>). </p>
	 * <p>
	 * Note: Layout is different from painting. If a child is moved or resized
	 * such that an area in the parent is exposed, then the parent will paint.
	 * If no child is affected, the parent will not paint.
	 * </p>
	 * 
	 * @param changed
	 *            <code>true</code> if the layout must flush its caches, and
	 *            <code>false</code> otherwise
	 * @param all
	 *            <code>true</code> if all children in the receiver's widget
	 *            tree should be laid out, and <code>false</code> otherwise
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
	public void layout(boolean changed, boolean all) {
		checkWidget ();
		if (layout == null && !all) return;
		markLayout (changed, all);
		updateLayout (all);
	}

	/**
	 * Forces a lay out (that is, sets the size and location) of all widgets
	 * that are in the parent hierarchy of the changed control up to and
	 * including the receiver. The layouts in the hierarchy must not rely on any
	 * information cached about the changed control or any of its ancestors. The
	 * layout may (potentially) optimize the work it is doing by assuming that
	 * none of the peers of the changed control have changed state since the
	 * last layout. If an ancestor does not have a layout, skip it.
	 * <p>
	 * Note: Layout is different from painting. If a child is moved or resized
	 * such that an area in the parent is exposed, then the parent will paint.
	 * If no child is affected, the parent will not paint.
	 * </p>
	 * 
	 * @param changed
	 *            a control that has had a state change which requires a
	 *            recalculation of its size
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_ARGUMENT - if the changed array is null
	 *                any of its controls are null or have been disposed</li>
	 *                <li>ERROR_INVALID_PARENT - if any control in changed is
	 *                not in the widget tree of the receiver</li>
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
	public void layout(Control[] changed) {
		if (changed == null) error (SWT.ERROR_INVALID_ARGUMENT);
		layout (changed, SWT.NONE);
	}

	/**
	 * Forces a lay out (that is, sets the size and location) of all widgets
	 * that are in the parent hierarchy of the changed control up to and
	 * including the receiver.
	 * <p>
	 * The parameter <code>flags</code> may be a combination of:
	 * <dl>
	 * <dt><b>SWT.ALL</b></dt>
	 * <dd>all children in the receiver's widget tree should be laid out</dd>
	 * <dt><b>SWT.CHANGED</b></dt>
	 * <dd>the layout must flush its caches</dd>
	 * <dt><b>SWT.DEFER</b></dt>
	 * <dd>layout will be deferred</dd>
	 * </dl>
	 * </p>
	 * <p>
	 * When the <code>changed</code> array is specified, the flags
	 * <code>SWT.ALL</code> and <code>SWT.CHANGED</code> have no effect. In this
	 * case, the layouts in the hierarchy must not rely on any information
	 * cached about the changed control or any of its ancestors. The layout may
	 * (potentially) optimize the work it is doing by assuming that none of the
	 * peers of the changed control have changed state since the last layout. If
	 * an ancestor does not have a layout, skip it.
	 * </p>
	 * <p>
	 * When the <code>changed</code> array is not specified, the flag
	 * <code>SWT.ALL</code> indicates that the whole widget tree should be laid
	 * out. And the flag <code>SWT.CHANGED</code> indicates that the layouts
	 * should flush any cached information for all controls that are laid out.
	 * </p>
	 * <p>
	 * The <code>SWT.DEFER</code> flag always causes the layout to be deferred
	 * by calling <code>Composite.setLayoutDeferred(true)</code> and scheduling
	 * a call to <code>Composite.setLayoutDeferred(false)</code>, which will
	 * happen when appropriate (usually before the next event is handled). When
	 * this flag is set, the application should not call
	 * <code>Composite.setLayoutDeferred(boolean)</code>.
	 * </p>
	 * <p>
	 * Note: Layout is different from painting. If a child is moved or resized
	 * such that an area in the parent is exposed, then the parent will paint.
	 * If no child is affected, the parent will not paint.
	 * </p>
	 * 
	 * @param changed
	 *            a control that has had a state change which requires a
	 *            recalculation of its size
	 * @param flags
	 *            the flags specifying how the layout should happen
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_ARGUMENT - if any of the controls in
	 *                changed is null or has been disposed</li>
	 *                <li>ERROR_INVALID_PARENT - if any control in changed is
	 *                not in the widget tree of the receiver</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @since 3.6
	 */
	public void layout(Control[] changed, int flags) {
		checkWidget ();
		if (changed != null) {
			for (int i=0; i<changed.length; i++) {
				Control control = changed [i];
				if (control == null) error (SWT.ERROR_INVALID_ARGUMENT);
				if (control.isDisposed ()) error (SWT.ERROR_INVALID_ARGUMENT);
				boolean ancestor = false;
				Composite composite = control.getParent();
				while (composite != null) {
					ancestor = composite == this;
					if (ancestor) break;
					composite = composite.getParent();
				}
				if (!ancestor) error (SWT.ERROR_INVALID_PARENT);
			}
			int updateCount = 0;
			Composite [] update = new Composite [16];
			for (int i=0; i<changed.length; i++) {
				Control child = changed [i];
				Composite composite = child.getParent();
				while (child != this) {
					if (composite.layout != null) {
						composite.state |= LAYOUT_NEEDED;
						if (!composite.layout.flushCache (child)) {
							composite.state |= LAYOUT_CHANGED;
						}
					}
					if (updateCount == update.length) {
						Composite [] newUpdate = new Composite [update.length + 16];
						System.arraycopy (update, 0, newUpdate, 0, update.length);
						update = newUpdate;
					}
					child = update [updateCount++] = composite;
					composite = child.getParent();
				}
			}
			if ((flags & SWT.DEFER) != 0) {
				setLayoutDeferred (true);
//				display.addLayoutDeferred (this);
			}
			for (int i=updateCount-1; i>=0; i--) {
				update [i].updateLayout (false);
			}
		} else {
			if (layout == null && (flags & SWT.ALL) == 0) return;
			markLayout ((flags & SWT.CHANGED) != 0, (flags & SWT.ALL) != 0);
			if ((flags & SWT.DEFER) != 0) {
				setLayoutDeferred (true);
//				display.addLayoutDeferred (this);
			}
			updateLayout ((flags & SWT.ALL) != 0);
		}
	}

	@Override
	void markLayout (boolean changed, boolean all) {
		if (layout != null) {
			state |= LAYOUT_NEEDED;
			if (changed) state |= LAYOUT_CHANGED;
		}
		if (all) {
			for (Control child : children)
				child.markLayout(changed, all);
		}
	}

	@Override
	public void redraw() {
		redraw(0, 0, internal_getWidth(), internal_getPrefHeight(), true);
	}
	
	@Override
	public void redraw(int x, int y, int width, int height, boolean all) {
		redraw(x*1.0, y, width, height, all);
	}
	
	void redraw(double x, double y, double width, double height, boolean all) {
		if( canvas != null ) {
			if( all ) {
				canvas.getGraphicsContext2D().clearRect(0,0,canvas.getWidth(),canvas.getHeight());
				x = 0;
				y = 0;
				width = canvas.getWidth();
				height = canvas.getHeight();
			} else {
				canvas.getGraphicsContext2D().clearRect(x,y,width,height);
			}
			
			Event event = new Event ();
			GC gc = new GC(this);
			event.gc = gc;
			event.x = (int)x;
			event.y = (int)y;
			event.width = (int)width;
			event.height = (int)height;
			internal_sendEvent (SWT.Paint, event,true);
			event.gc = null;
			gc.dispose ();
		}
	}
	
	/**
	 * Sets the background drawing mode to the argument which should be one of
	 * the following constants defined in class <code>SWT</code>:
	 * <code>INHERIT_NONE</code>, <code>INHERIT_DEFAULT</code>,
	 * <code>INHERIT_FORCE</code>.
	 * 
	 * @param mode
	 *            the new background mode
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @see SWT
	 * 
	 * @since 3.2
	 */
	public void setBackgroundMode(int mode) {
		Util.logNotImplemented();
	}

	/**
	 * Sets the layout which is associated with the receiver to be the argument
	 * which may be null.
	 * 
	 * @param layout
	 *            the receiver's new layout or null
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setLayout(Layout layout) {
		this.layout = layout;
		internal_setLayout(layout);
	}

	/**
	 * If the argument is <code>true</code>, causes subsequent layout operations
	 * in the receiver or any of its children to be ignored. No layout of any
	 * kind can occur in the receiver or any of its children until the flag is
	 * set to false. Layout operations that occurred while the flag was
	 * <code>true</code> are remembered and when the flag is set to
	 * <code>false</code>, the layout operations are performed in an optimized
	 * manner. Nested calls to this method are stacked.
	 * 
	 * @param defer
	 *            the new defer state
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @see #layout(boolean)
	 * @see #layout(Control[])
	 * 
	 * @since 3.1
	 */
	public void setLayoutDeferred(boolean defer) {
		Util.logNotImplemented();
	}

	@Override
	public void setMenu(Menu menu) {
		if( controlContainer != null ) {
			controlContainer.addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED, getContextMenuHandler());
			this.menu = menu;
		} else {
			super.setMenu(menu);	
		}
	}
	
	/**
	 * Sets the tabbing order for the specified controls to match the order that
	 * they occur in the argument list.
	 * 
	 * @param tabList
	 *            the ordered list of controls representing the tab order or
	 *            null
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_ARGUMENT - if a widget in the tabList is
	 *                null or has been disposed</li>
	 *                <li>ERROR_INVALID_PARENT - if widget in the tabList is not
	 *                in the same widget tree</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setTabList(Control[] tabList) {
		Util.logNotImplemented();
	}

	@Override
	public void setToolTipText(String string) {
		if( controlContainer != null ) {
			if( string == null || string.length() == 0 ) {
				if( tooltip != null ) {
					Tooltip.uninstall(controlContainer, tooltip);
				}
				tooltip = null;
			} else {
				if( tooltip == null ) {
					tooltip = new Tooltip();
				}
				tooltip.setText(string);
				Tooltip.install(controlContainer, tooltip);
			}
		} else {
			super.setToolTipText(string);	
		}
		
	}
	
	void updateLayout (boolean all) {
		updateLayout (true, all);
	}

	@Override
	void updateLayout(boolean resize, boolean all) {
		Composite parent = findDeferredControl ();
		if (parent != null) {
			parent.state |= LAYOUT_CHILD;
			return;
		}
		if ((state & LAYOUT_NEEDED) != 0) {
			boolean changed = (state & LAYOUT_CHANGED) != 0;
			state &= ~(LAYOUT_NEEDED | LAYOUT_CHANGED);
//			display.runSkin();
//			if (resize) setResizeChildren (false);
			layout.layout (this, changed);
//			if (resize) setResizeChildren (true);
		}
		if (all) {
			state &= ~LAYOUT_CHILD;
			for (Control child : children)
				child.updateLayout(resize, all);
		}
	}
	
}

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

import javafx.scene.Node;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

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

	private AnchorPane scrollable;
	private Pane controlContainer;
	private ScrollBar vScroll;
	private ScrollBar hScroll;
	private Node corner;
	private ToggleGroup toggleGroup;

	private static final double SCROLLBAR_WIDTH = 20.0;

	Layout layout;
	java.util.List<Control> children = new ArrayList<>();
	int backgroundMode, layoutCount;
	
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

	void controlMoveAbove(Control control1, Control control2) {
		// TODO
	}
	
	void controlMoveBelow(Control control1, Control control2) {
		// TODO
	}
	
	void addControl(Control control) {
		if (control instanceof Button && (control.getStyle() & SWT.RADIO) != 0
				&& (getStyle() & SWT.NO_RADIO_GROUP) == 0) {
			if (toggleGroup == null) {
				toggleGroup = new ToggleGroup();
			}
			toggleGroup.getToggles().add((Toggle)control.getNativeObject());
		}
		children.add(control);
		controlContainer.getChildren().add(control.getNativeObject());
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

	protected void applyBorderStyle() {
		if( (style & SWT.BORDER) == SWT.BORDER && getNativeObject() != null ) {
			getNativeObject().setStyle("-fx-boder-style: solid; -fx-border-width: 1px; -fx-border-color: gray;");	
		}
	}
	
	@Override
	void createNativeObject() {
		scrollable = new AnchorPane() {
			@Override
			protected void layoutChildren() {
				super.layoutChildren();
				double endCorrection = 0;
				if( corner != null && vScroll.getVisible() && hScroll.getVisible() ) {
					endCorrection = SCROLLBAR_WIDTH;
				}
				
				if( vScroll != null ) {
					vScroll.getNativeObject().resizeRelocate(getWidth()-SCROLLBAR_WIDTH, 0, SCROLLBAR_WIDTH, getHeight()-endCorrection);	
				}
				
				if( hScroll != null ) {
					hScroll.getNativeObject().resizeRelocate(0, getHeight()-SCROLLBAR_WIDTH, getWidth()-endCorrection, SCROLLBAR_WIDTH);
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
		
		scrollable.getStyleClass().add(getStyleClassname());
		controlContainer = new Pane() {
			@Override 
			protected double computeMinWidth(double height) {
				if (layout != null) {
					return layout.computeSize(Composite.this, 0, 0, true).x;
				}
				return super.computeMinWidth(height);
			}
			
			@Override
			protected double computeMinHeight(double width) {
				if (layout != null) {
					return layout.computeSize(Composite.this, 0, 0, true).y;
				}
				return super.computeMinHeight(width);
			}
			
			@Override
			protected double computeMaxHeight(double width) {
				if (layout != null) {
					return layout.computeSize(Composite.this, Integer.MAX_VALUE, Integer.MAX_VALUE, true).y;
				}
				return super.computeMaxHeight(width);
			}
			
			@Override
			protected double computeMaxWidth(double height) {
				if (layout != null) {
					return layout.computeSize(Composite.this, Integer.MAX_VALUE, Integer.MAX_VALUE, true).x;
				}
				return super.computeMaxWidth(height);
			}
			
			@Override
			protected double computePrefHeight(double width) {
				if (layout != null) {
					return layout.computeSize(Composite.this, SWT.DEFAULT, SWT.DEFAULT, true).y;
				}
				return super.computePrefHeight(width);
			}
			
			@Override
			protected double computePrefWidth(double height) {
				if (layout != null) {
					return layout.computeSize(Composite.this, SWT.DEFAULT, SWT.DEFAULT, true).x;
				}
				return super.computePrefWidth(height);
			}
			
			@Override
			protected void layoutChildren() {
				if (layout != null) {
					layout.layout(Composite.this, true);
				} else {
					//Do not call super else nodes resized absolutely are
					//are resized to their minimal value			
				}
			}			
		};
		
		AnchorPane.setLeftAnchor(controlContainer, 0.0);
		AnchorPane.setTopAnchor(controlContainer, 0.0);
		
		scrollable.getChildren().add(controlContainer);
		
		if( (style & SWT.V_SCROLL) == SWT.V_SCROLL ) {
			AnchorPane.setRightAnchor(controlContainer, SCROLLBAR_WIDTH);
			vScroll = new ScrollBar(this,SWT.VERTICAL);
			Node n = vScroll.getNativeObject();
			n.setManaged(false);
			scrollable.getChildren().add(n);
		} else {
			AnchorPane.setRightAnchor(controlContainer, 0.0);
		}
		
		if( (style & SWT.H_SCROLL) == SWT.H_SCROLL ) {
			AnchorPane.setBottomAnchor(controlContainer, SCROLLBAR_WIDTH);
			hScroll = new ScrollBar(this,SWT.HORIZONTAL);
			Node n = hScroll.getNativeObject();
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
		// TODO
	}

	Composite findDeferredControl () {
		return layoutCount > 0 ? this : parent.findDeferredControl ();
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
		// TODO
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
		return new Rectangle(0, 0, (int)controlContainer.getWidth(), (int)controlContainer.getHeight());
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
	Region getNativeControl() {
		return controlContainer;
	}
	
	@Override
	Region getNativeObject() {
		return scrollable;
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
		// TODO
		return getChildren();
	}

	@Override
	public ScrollBar getVerticalBar() {
		return vScroll;
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
		// TODO
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
				Composite composite = control.parent;
				while (composite != null) {
					ancestor = composite == this;
					if (ancestor) break;
					composite = composite.parent;
				}
				if (!ancestor) error (SWT.ERROR_INVALID_PARENT);
			}
			int updateCount = 0;
			Composite [] update = new Composite [16];
			for (int i=0; i<changed.length; i++) {
				Control child = changed [i];
				Composite composite = child.parent;
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
					composite = child.parent;
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

	void removeControl(Control control) {
		if (control instanceof Button && (control.getStyle() & SWT.RADIO) != 0
				&& (getStyle() & SWT.NO_RADIO_GROUP) == 0 ) {
			if (toggleGroup != null) {
				toggleGroup.getToggles().remove(control.getNativeObject());
			}
		}
		children.remove(control);
		controlContainer.getChildren().remove(control.getNativeObject());
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
		// TODO
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
		// TODO
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
		// TODO
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

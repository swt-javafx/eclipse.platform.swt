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

import java.lang.reflect.Field;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.geometry.Orientation;
import javafx.scene.control.Skin;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Device.NoOpDrawableGC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.internal.Util;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.SliderBehavior;
import com.sun.javafx.scene.control.skin.BehaviorSkinBase;

/**
 * Instances of this class are selectable user interface objects that represent
 * a range of positive, numeric values.
 * <p>
 * At any given moment, a given slider will have a single 'selection' that is
 * considered to be its value, which is constrained to be within the range of
 * values the slider represents (that is, between its <em>minimum</em> and
 * <em>maximum</em> values).
 * </p>
 * <p>
 * Typically, sliders will be made up of five areas:
 * <ol>
 * <li>an arrow button for decrementing the value</li>
 * <li>a page decrement area for decrementing the value by a larger amount</li>
 * <li>a <em>thumb</em> for modifying the value by mouse dragging</li>
 * <li>a page increment area for incrementing the value by a larger amount</li>
 * <li>an arrow button for incrementing the value</li>
 * </ol>
 * Based on their style, sliders are either <code>HORIZONTAL</code> (which have
 * a left facing button for decrementing the value and a right facing button for
 * incrementing it) or <code>VERTICAL</code> (which have an upward facing button
 * for decrementing the value and a downward facing buttons for incrementing
 * it).
 * </p>
 * <p>
 * On some platforms, the size of the slider's thumb can be varied relative to
 * the magnitude of the range of values it represents (that is, relative to the
 * difference between its maximum and minimum values). Typically, this is used
 * to indicate some proportional value such as the ratio of the visible area of
 * a document to the total amount of space that it would take to display it. SWT
 * supports setting the thumb size even if the underlying platform does not, but
 * in this case the appearance of the slider will not change.
 * </p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>HORIZONTAL, VERTICAL</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection</dd>
 * </dl>
 * <p>
 * Note: Only one of the styles HORIZONTAL and VERTICAL may be specified.
 * </p>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 * 
 * @see ScrollBar
 * @see <a href="http://www.eclipse.org/swt/snippets/#slider">Slider
 *      snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example:
 *      ControlExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further
 *      information</a>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class Slider extends Control {

	private javafx.scene.control.Slider slider;
	
	boolean noEvent;
	
	int eventType = SWT.DRAG;

	static class SWTBehavior extends SliderBehavior {
		private Slider swtSlider;
		public SWTBehavior(Slider swtSlider, javafx.scene.control.Slider slider) {
			super(slider);
			this.swtSlider = swtSlider;
		}
		
		protected void callAction(String name) {
			if( "Home".equals(name) ) {
				swtSlider.eventType = SWT.HOME;
			} else if( "End".equals(name) ) {
				swtSlider.eventType = SWT.END;
			} else if( "IncrementValue".equals(name) ) {
				swtSlider.eventType = SWT.ARROW_DOWN;
			} else if( "DecrementValue".equals(name) ) {
				swtSlider.eventType = SWT.ARROW_UP;
			}
			super.callAction(name);
			swtSlider.eventType = SWT.DRAG;
			
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
	 * @see SWT#HORIZONTAL
	 * @see SWT#VERTICAL
	 * @see Widget#checkSubclass
	 * @see Widget#getStyle
	 */
	public Slider(Composite parent, int style) {
		super(parent, style);
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified
	 * when the user changes the receiver's value, by sending it one of the
	 * messages defined in the <code>SelectionListener</code> interface.
	 * <p>
	 * When <code>widgetSelected</code> is called, the event object detail field
	 * contains one of the following values: <code>SWT.NONE</code> - for the end
	 * of a drag. <code>SWT.DRAG</code>. <code>SWT.HOME</code>.
	 * <code>SWT.END</code>. <code>SWT.ARROW_DOWN</code>.
	 * <code>SWT.ARROW_UP</code>. <code>SWT.PAGE_DOWN</code>.
	 * <code>SWT.PAGE_UP</code>. <code>widgetDefaultSelected</code> is not
	 * called.
	 * </p>
	 * 
	 * @param listener
	 *            the listener which should be notified when the user changes
	 *            the receiver's value
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

	@Override
	public Point computeSize(int wHint, int hHint, boolean flushCache) {
		forceSizeProcessing();
		int width = (int) internal_getNativeObject().prefWidth(javafx.scene.control.Control.USE_COMPUTED_SIZE);
		int height = (int) internal_getNativeObject().prefHeight(javafx.scene.control.Control.USE_COMPUTED_SIZE);

		if (wHint != SWT.DEFAULT)
			width = wHint;
		if (hHint != SWT.DEFAULT)
			height = hHint;

		Point p = new Point(width, height);
		return p;
	}

	@Override
	protected javafx.scene.control.Slider createWidget() {
		slider = new javafx.scene.control.Slider(0, 100, 0) {
			@Override
			protected Skin<?> createDefaultSkin() {
				Skin<?> s = super.createDefaultSkin();
				replaceBehavior(Slider.this,this,s);
				return s;
			}
		};
		slider.setMajorTickUnit(1);
		
		if( (style & SWT.VERTICAL) == SWT.VERTICAL ) {
			slider.setOrientation(Orientation.VERTICAL);
		}
		
		if( (style & SWT.BORDER) == SWT.BORDER ) {
			Util.logNotImplemented();
		}
		return slider;
	}
	
	/**
	 * Returns the amount that the receiver's value will be modified by when the
	 * up/down (or right/left) arrows are pressed.
	 * 
	 * @return the increment
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public int getIncrement() {
		checkWidget();
		return (int) slider.getMajorTickUnit();
	}

	/**
	 * Returns the maximum value which the receiver will allow.
	 * 
	 * @return the maximum
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public int getMaximum() {
		checkWidget();
		return (int) slider.getMax();
	}

	/**
	 * Returns the minimum value which the receiver will allow.
	 * 
	 * @return the minimum
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public int getMinimum() {
		checkWidget();
		return (int) slider.getMin();
	}

	/**
	 * Returns the amount that the receiver's value will be modified by when the
	 * page increment/decrement areas are selected.
	 * 
	 * @return the page increment
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public int getPageIncrement() {
		Util.logNotImplemented();
		return 0;
	}

	/**
	 * Returns the 'selection', which is the receiver's value.
	 * 
	 * @return the selection
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public int getSelection() {
		checkWidget();
		return (int) slider.getValue();
	}

	/**
	 * Returns the receiver's thumb value.
	 * 
	 * @return the thumb value
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public int getThumb() {
		checkWidget();
		return (int) slider.getBlockIncrement();
	}

	@Override
	protected void initListeners() {
		slider.valueProperty().addListener(new InvalidationListener() {
			
			@Override
			public void invalidated(Observable arg0) {
				if( noEvent ) {
					return;
				}
				
				Event event = new Event();
				event.detail = eventType;
				internal_sendEvent(SWT.Selection, event, true);
			}
		});
	}

	@Override
	public javafx.scene.control.Slider internal_getNativeObject() {
		return slider;
	}

	@Override
	public void internal_dispose_GC(DrawableGC gc) {
		Util.logNotImplemented();
	}
	
	@Override
	public DrawableGC internal_new_GC() {
		return new NoOpDrawableGC(this,getFont());
	}
	
	/**
	 * Removes the listener from the collection of listeners who will be
	 * notified when the user changes the receiver's value.
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
		checkWidget ();
		if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
		unregisterListener(SWT.Selection, listener);
		unregisterListener(SWT.DefaultSelection,listener);
	}

	private static boolean replaceBehavior(Slider swtSlider, javafx.scene.control.Slider slider, Skin<?> skin) {
		try {
			Field f = BehaviorSkinBase.class.getDeclaredField("behavior");
			f.setAccessible(true);
			BehaviorBase<?> b = (BehaviorBase<?>) f.get(skin);
			b.dispose();
			f.set(skin, new SWTBehavior(swtSlider,slider));
			return true;
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Sets the amount that the receiver's value will be modified by when the
	 * up/down (or right/left) arrows are pressed to the argument, which must be
	 * at least one.
	 * 
	 * @param value
	 *            the new increment (must be greater than zero)
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setIncrement(int value) {
		checkWidget();
		slider.setMajorTickUnit(value);
	}

	/**
	 * Sets the maximum. If this value is negative or less than or equal to the
	 * minimum, the value is ignored. If necessary, first the thumb and then the
	 * selection are adjusted to fit within the new range.
	 * 
	 * @param value
	 *            the new maximum, which must be greater than the current
	 *            minimum
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setMaximum(int value) {
		checkWidget();
		slider.setMax(value);
	}

	/**
	 * Sets the minimum value. If this value is negative or greater than or
	 * equal to the maximum, the value is ignored. If necessary, first the thumb
	 * and then the selection are adjusted to fit within the new range.
	 * 
	 * @param value
	 *            the new minimum
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setMinimum(int value) {
		checkWidget();
		slider.setMin(value);
	}

	/**
	 * Sets the amount that the receiver's value will be modified by when the
	 * page increment/decrement areas are selected to the argument, which must
	 * be at least one.
	 * 
	 * @param value
	 *            the page increment (must be greater than zero)
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setPageIncrement(int value) {
		Util.logNotImplemented();
	}

	/**
	 * Sets the 'selection', which is the receiver's value, to the argument
	 * which must be greater than or equal to zero.
	 * 
	 * @param value
	 *            the new selection (must be zero or greater)
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setSelection(int value) {
		checkWidget();
		try {
			noEvent = true;
			slider.setValue(value);	
		} finally {
			noEvent = false;
		}
	}

	/**
	 * Sets the thumb value. The thumb value should be used to represent the
	 * size of the visual portion of the current range. This value is usually
	 * the same as the page increment value.
	 * <p>
	 * This new value will be ignored if it is less than one, and will be
	 * clamped if it exceeds the receiver's current range.
	 * </p>
	 * 
	 * @param value
	 *            the new thumb value, which must be at least one and not larger
	 *            than the size of the current range
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setThumb(int value) {
		checkWidget();
		slider.setBlockIncrement(value);
	}

	/**
	 * Sets the receiver's selection, minimum value, maximum value, thumb,
	 * increment and page increment all at once.
	 * <p>
	 * Note: This is similar to setting the values individually using the
	 * appropriate methods, but may be implemented in a more efficient fashion
	 * on some platforms.
	 * </p>
	 * 
	 * @param selection
	 *            the new selection value
	 * @param minimum
	 *            the new minimum value
	 * @param maximum
	 *            the new maximum value
	 * @param thumb
	 *            the new thumb value
	 * @param increment
	 *            the new increment value
	 * @param pageIncrement
	 *            the new pageIncrement value
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setValues(int selection, int minimum, int maximum, int thumb,
			int increment, int pageIncrement) {
		setMinimum(minimum);
		setMinimum(maximum);

		setSelection(selection);

		setThumb(thumb);
		setIncrement(increment);
		setPageIncrement(pageIncrement);
	}

}

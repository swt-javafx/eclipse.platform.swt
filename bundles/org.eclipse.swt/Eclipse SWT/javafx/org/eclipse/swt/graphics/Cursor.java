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
package org.eclipse.swt.graphics;

import javafx.scene.ImageCursor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;

/**
 * Instances of this class manage operating system resources that specify the
 * appearance of the on-screen pointer. To create a cursor you specify the
 * device and either a simple cursor style describing one of the standard
 * operating system provided cursors or the image and mask data for the desired
 * appearance.
 * <p>
 * Application code must explicitly invoke the <code>Cursor.dispose()</code>
 * method to release the operating system resources managed by each instance
 * when those instances are no longer required.
 * </p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>
 * CURSOR_ARROW, CURSOR_WAIT, CURSOR_CROSS, CURSOR_APPSTARTING, CURSOR_HELP,
 * CURSOR_SIZEALL, CURSOR_SIZENESW, CURSOR_SIZENS, CURSOR_SIZENWSE,
 * CURSOR_SIZEWE, CURSOR_SIZEN, CURSOR_SIZES, CURSOR_SIZEE, CURSOR_SIZEW,
 * CURSOR_SIZENE, CURSOR_SIZESE, CURSOR_SIZESW, CURSOR_SIZENW, CURSOR_UPARROW,
 * CURSOR_IBEAM, CURSOR_NO, CURSOR_HAND</dd>
 * </dl>
 * <p>
 * Note: Only one of the above styles may be specified.
 * </p>
 * 
 * @see <a href="http://www.eclipse.org/swt/snippets/#cursor">Cursor
 *      snippets</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further
 *      information</a>
 */
public final class Cursor extends Resource {

	private javafx.scene.Cursor nativeObject;
	
	/**
	 * Constructs a new cursor given a device and a style constant describing
	 * the desired cursor appearance.
	 * <p>
	 * You must dispose the cursor when it is no longer required.
	 * </p>
	 * NOTE: It is recommended to use
	 * {@link org.eclipse.swt.widgets.Display#getSystemCursor(int)} instead of
	 * using this constructor. This way you can avoid the overhead of disposing
	 * the Cursor resource.
	 * 
	 * @param device
	 *            the device on which to allocate the cursor
	 * @param style
	 *            the style of cursor to allocate
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if device is null and there is
	 *                no current device</li>
	 *                <li>ERROR_INVALID_ARGUMENT - when an unknown style is
	 *                specified</li>
	 *                </ul>
	 * @exception SWTError
	 *                <ul>
	 *                <li>ERROR_NO_HANDLES - if a handle could not be obtained
	 *                for cursor creation</li>
	 *                </ul>
	 * 
	 * @see SWT#CURSOR_ARROW
	 * @see SWT#CURSOR_WAIT
	 * @see SWT#CURSOR_CROSS
	 * @see SWT#CURSOR_APPSTARTING
	 * @see SWT#CURSOR_HELP
	 * @see SWT#CURSOR_SIZEALL
	 * @see SWT#CURSOR_SIZENESW
	 * @see SWT#CURSOR_SIZENS
	 * @see SWT#CURSOR_SIZENWSE
	 * @see SWT#CURSOR_SIZEWE
	 * @see SWT#CURSOR_SIZEN
	 * @see SWT#CURSOR_SIZES
	 * @see SWT#CURSOR_SIZEE
	 * @see SWT#CURSOR_SIZEW
	 * @see SWT#CURSOR_SIZENE
	 * @see SWT#CURSOR_SIZESE
	 * @see SWT#CURSOR_SIZESW
	 * @see SWT#CURSOR_SIZENW
	 * @see SWT#CURSOR_UPARROW
	 * @see SWT#CURSOR_IBEAM
	 * @see SWT#CURSOR_NO
	 * @see SWT#CURSOR_HAND
	 */
	public Cursor(Device device, int style) {
		super(device);
		switch (style) {
		case SWT.CURSOR_HAND: 			nativeObject = javafx.scene.Cursor.HAND; break;
		case SWT.CURSOR_ARROW: 			nativeObject = javafx.scene.Cursor.DEFAULT; break; //TODO is this correct
		case SWT.CURSOR_WAIT: 			nativeObject = javafx.scene.Cursor.WAIT; break;
		case SWT.CURSOR_CROSS:	 		nativeObject = javafx.scene.Cursor.CROSSHAIR; break;
		case SWT.CURSOR_APPSTARTING: 	nativeObject = javafx.scene.Cursor.DEFAULT; break;
		case SWT.CURSOR_HELP: 			nativeObject = javafx.scene.Cursor.CROSSHAIR; break;
		case SWT.CURSOR_SIZEALL: 		nativeObject = javafx.scene.Cursor.CROSSHAIR; break;
		case SWT.CURSOR_SIZENESW: 		nativeObject = javafx.scene.Cursor.CROSSHAIR; break;
		case SWT.CURSOR_SIZEWE:			nativeObject = javafx.scene.Cursor.H_RESIZE; break;
		case SWT.CURSOR_SIZENWSE: 		nativeObject = javafx.scene.Cursor.CROSSHAIR; break;
		case SWT.CURSOR_SIZENS: 		nativeObject = javafx.scene.Cursor.V_RESIZE; break;
		case SWT.CURSOR_SIZEN: 			nativeObject = javafx.scene.Cursor.N_RESIZE; break;
		case SWT.CURSOR_SIZES: 			nativeObject = javafx.scene.Cursor.S_RESIZE; break;
		case SWT.CURSOR_SIZEE: 			nativeObject = javafx.scene.Cursor.E_RESIZE; break;
		case SWT.CURSOR_SIZEW: 			nativeObject = javafx.scene.Cursor.W_RESIZE; break;
		case SWT.CURSOR_SIZENE: 		nativeObject = javafx.scene.Cursor.NE_RESIZE; break;
		case SWT.CURSOR_SIZESE: 		nativeObject = javafx.scene.Cursor.SE_RESIZE; break;
		case SWT.CURSOR_SIZESW: 		nativeObject = javafx.scene.Cursor.SW_RESIZE; break;
		case SWT.CURSOR_SIZENW: 		nativeObject = javafx.scene.Cursor.NW_RESIZE; break;
		case SWT.CURSOR_UPARROW: 		nativeObject = javafx.scene.Cursor.CROSSHAIR; break;
		case SWT.CURSOR_IBEAM: 			nativeObject = javafx.scene.Cursor.CROSSHAIR; break;
		case SWT.CURSOR_NO: 			nativeObject = javafx.scene.Cursor.NONE; break;
		default:
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		}
	}

	/**
	 * Constructs a new cursor given a device, image and mask data describing
	 * the desired cursor appearance, and the x and y coordinates of the
	 * <em>hotspot</em> (that is, the point within the area covered by the
	 * cursor which is considered to be where the on-screen pointer is
	 * "pointing").
	 * <p>
	 * The mask data is allowed to be null, but in this case the source must be
	 * an ImageData representing an icon that specifies both color data and mask
	 * data.
	 * <p>
	 * You must dispose the cursor when it is no longer required.
	 * </p>
	 * 
	 * @param device
	 *            the device on which to allocate the cursor
	 * @param source
	 *            the color data for the cursor
	 * @param mask
	 *            the mask data for the cursor (or null)
	 * @param hotspotX
	 *            the x coordinate of the cursor's hotspot
	 * @param hotspotY
	 *            the y coordinate of the cursor's hotspot
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if device is null and there is
	 *                no current device</li>
	 *                <li>ERROR_NULL_ARGUMENT - if the source is null</li>
	 *                <li>ERROR_NULL_ARGUMENT - if the mask is null and the
	 *                source does not have a mask</li>
	 *                <li>ERROR_INVALID_ARGUMENT - if the source and the mask
	 *                are not the same size, or if the hotspot is outside the
	 *                bounds of the image</li>
	 *                </ul>
	 * @exception SWTError
	 *                <ul>
	 *                <li>ERROR_NO_HANDLES - if a handle could not be obtained
	 *                for cursor creation</li>
	 *                </ul>
	 */
	public Cursor(Device device, ImageData source, ImageData mask,
			int hotspotX, int hotspotY) {
		super(device);
		Image img = new Image(device, source, mask);
		nativeObject = new ImageCursor(img.internal_getImage(), hotspotX, hotspotY);
		img.dispose();
	}

	/**
	 * Constructs a new cursor given a device, image data describing the desired
	 * cursor appearance, and the x and y coordinates of the <em>hotspot</em>
	 * (that is, the point within the area covered by the cursor which is
	 * considered to be where the on-screen pointer is "pointing").
	 * <p>
	 * You must dispose the cursor when it is no longer required.
	 * </p>
	 * 
	 * @param device
	 *            the device on which to allocate the cursor
	 * @param source
	 *            the image data for the cursor
	 * @param hotspotX
	 *            the x coordinate of the cursor's hotspot
	 * @param hotspotY
	 *            the y coordinate of the cursor's hotspot
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if device is null and there is
	 *                no current device</li>
	 *                <li>ERROR_NULL_ARGUMENT - if the image is null</li>
	 *                <li>ERROR_INVALID_ARGUMENT - if the hotspot is outside the
	 *                bounds of the image</li>
	 *                </ul>
	 * @exception SWTError
	 *                <ul>
	 *                <li>ERROR_NO_HANDLES - if a handle could not be obtained
	 *                for cursor creation</li>
	 *                </ul>
	 * 
	 * @since 3.0
	 */
	public Cursor(Device device, ImageData source, int hotspotX, int hotspotY) {
		super(device);
		Image img = new Image(device, source);
		nativeObject = new ImageCursor(img.internal_getImage(), hotspotX, hotspotY);
		img.dispose();
	}

	@Override
	public void dispose() {
		nativeObject = null;
	}

	public javafx.scene.Cursor internal_getNativeObject() {
		return nativeObject;
	}

	/**
	 * Returns <code>true</code> if the cursor has been disposed,
	 * and <code>false</code> otherwise.
	 * <p>
	 * This method gets the dispose state for the cursor.
	 * When a cursor has been disposed, it is an error to
	 * invoke any other method (except {@link #dispose()}) using the cursor.
	 *
	 * @return <code>true</code> when the cursor is disposed and <code>false</code> otherwise
	 */
	@Override
	public boolean isDisposed() {
		return nativeObject != null;
	}


}

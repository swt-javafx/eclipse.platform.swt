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
package org.eclipse.swt.graphics;

import java.io.InputStream;

import javafx.scene.image.WritableImage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.SWTException;

/**
 * Instances of this class are graphics which have been prepared for display on
 * a specific device. That is, they are ready to paint using methods such as
 * <code>GC.drawImage()</code> and display on widgets with, for example,
 * <code>Button.setImage()</code>.
 * <p>
 * If loaded from a file format that supports it, an <code>Image</code> may have
 * transparency, meaning that certain pixels are specified as being transparent
 * when drawn. Examples of file formats that support transparency are GIF and
 * PNG.
 * </p>
 * <p>
 * There are two primary ways to use <code>Images</code>. The first is to load a
 * graphic file from disk and create an <code>Image</code> from it. This is done
 * using an <code>Image</code> constructor, for example:
 * 
 * <pre>
 * Image i = new Image(device, &quot;C:\\graphic.bmp&quot;);
 * </pre>
 * 
 * A graphic file may contain a color table specifying which colors the image
 * was intended to possess. In the above example, these colors will be mapped to
 * the closest available color in SWT. It is possible to get more control over
 * the mapping of colors as the image is being created, using code of the form:
 * 
 * <pre>
 * ImageData data = new ImageData(&quot;C:\\graphic.bmp&quot;);
 * RGB[] rgbs = data.getRGBs();
 * // At this point, rgbs contains specifications of all
 * // the colors contained within this image. You may
 * // allocate as many of these colors as you wish by
 * // using the Color constructor Color(RGB), then
 * // create the image:
 * Image i = new Image(device, data);
 * </pre>
 * <p>
 * Applications which require even greater control over the image loading
 * process should use the support provided in class <code>ImageLoader</code>.
 * </p>
 * <p>
 * Application code must explicitly invoke the <code>Image.dispose()</code>
 * method to release the operating system resources managed by each instance
 * when those instances are no longer required.
 * </p>
 * 
 * @see Color
 * @see ImageData
 * @see ImageLoader
 * @see <a href="http://www.eclipse.org/swt/snippets/#image">Image snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Examples:
 *      GraphicsExample, ImageAnalyzer</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further
 *      information</a>
 */
public final class Image extends Resource implements Drawable {
	
	private WritableImage image;
	
	/**
	 * Constructs an empty instance of this class with the specified width and
	 * height. The result may be drawn upon by creating a GC and using any of
	 * its drawing operations, as shown in the following example:
	 * 
	 * <pre>
	 * Image i = new Image(device, width, height);
	 * GC gc = new GC(i);
	 * gc.drawRectangle(0, 0, 50, 50);
	 * gc.dispose();
	 * </pre>
	 * <p>
	 * Note: Some platforms may have a limitation on the size of image that can
	 * be created (size depends on width, height, and depth). For example,
	 * Windows 95, 98, and ME do not allow images larger than 16M.
	 * </p>
	 * 
	 * @param device
	 *            the device on which to create the image
	 * @param width
	 *            the width of the new image
	 * @param height
	 *            the height of the new image
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if device is null and there is
	 *                no current device</li>
	 *                <li>ERROR_INVALID_ARGUMENT - if either the width or height
	 *                is negative or zero</li>
	 *                </ul>
	 * @exception SWTError
	 *                <ul>
	 *                <li>ERROR_NO_HANDLES if a handle could not be obtained for
	 *                image creation</li>
	 *                </ul>
	 */
	public Image(Device device, int width, int height) {
		super(device);
		if (width == 0 || height == 0)
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		image = new WritableImage(width, height);
	}

	/**
	 * Constructs an empty instance of this class with the width and height of
	 * the specified rectangle. The result may be drawn upon by creating a GC
	 * and using any of its drawing operations, as shown in the following
	 * example:
	 * 
	 * <pre>
	 * Image i = new Image(device, boundsRectangle);
	 * GC gc = new GC(i);
	 * gc.drawRectangle(0, 0, 50, 50);
	 * gc.dispose();
	 * </pre>
	 * <p>
	 * Note: Some platforms may have a limitation on the size of image that can
	 * be created (size depends on width, height, and depth). For example,
	 * Windows 95, 98, and ME do not allow images larger than 16M.
	 * </p>
	 * 
	 * @param device
	 *            the device on which to create the image
	 * @param bounds
	 *            a rectangle specifying the image's width and height (must not
	 *            be null)
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if device is null and there is
	 *                no current device</li>
	 *                <li>ERROR_NULL_ARGUMENT - if the bounds rectangle is null</li>
	 *                <li>ERROR_INVALID_ARGUMENT - if either the rectangle's
	 *                width or height is negative</li>
	 *                </ul>
	 * @exception SWTError
	 *                <ul>
	 *                <li>ERROR_NO_HANDLES if a handle could not be obtained for
	 *                image creation</li>
	 *                </ul>
	 */
	public Image(Device device, Rectangle bounds) {
		this(device, bounds.width, bounds.height);
	}

	/**
	 * Constructs an instance of this class by loading its representation from
	 * the specified input stream. Throws an error if an error occurs while
	 * loading the image, or if the result is an image of an unsupported type.
	 * Application code is still responsible for closing the input stream.
	 * <p>
	 * This constructor is provided for convenience when loading a single image
	 * only. If the stream contains multiple images, only the first one will be
	 * loaded. To load multiple images, use <code>ImageLoader.load()</code>.
	 * </p>
	 * <p>
	 * This constructor may be used to load a resource as follows:
	 * </p>
	 * 
	 * <pre>
	 * static Image loadImage(Display display, Class clazz, String string) {
	 * 	InputStream stream = clazz.getResourceAsStream(string);
	 * 	if (stream == null)
	 * 		return null;
	 * 	Image image = null;
	 * 	try {
	 * 		image = new Image(display, stream);
	 * 	} catch (SWTException ex) {
	 * 	} finally {
	 * 		try {
	 * 			stream.close();
	 * 		} catch (IOException ex) {
	 * 		}
	 * 	}
	 * 	return image;
	 * }
	 * </pre>
	 * 
	 * @param device
	 *            the device on which to create the image
	 * @param stream
	 *            the input stream to load the image from
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if device is null and there is
	 *                no current device</li>
	 *                <li>ERROR_NULL_ARGUMENT - if the stream is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_IO - if an IO error occurs while reading from
	 *                the stream</li>
	 *                <li>ERROR_INVALID_IMAGE - if the image stream contains
	 *                invalid data</li>
	 *                <li>ERROR_UNSUPPORTED_DEPTH - if the image stream
	 *                describes an image with an unsupported depth</li>
	 *                <li>ERROR_UNSUPPORTED_FORMAT - if the image stream
	 *                contains an unrecognized format</li>
	 *                </ul>
	 * @exception SWTError
	 *                <ul>
	 *                <li>ERROR_NO_HANDLES if a handle could not be obtained for
	 *                image creation</li>
	 *                </ul>
	 */
	public Image(Device device, InputStream stream) {
		super(device);
		javafx.scene.image.Image image = new javafx.scene.image.Image(stream);
		this.image = new WritableImage(image.getPixelReader(),
				(int) image.getWidth(), (int) image.getHeight());
	}

	/**
	 * Constructs an instance of this class by loading its representation from
	 * the file with the specified name. Throws an error if an error occurs
	 * while loading the image, or if the result is an image of an unsupported
	 * type.
	 * <p>
	 * This constructor is provided for convenience when loading a single image
	 * only. If the specified file contains multiple images, only the first one
	 * will be used.
	 * 
	 * @param device
	 *            the device on which to create the image
	 * @param filename
	 *            the name of the file to load the image from
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if device is null and there is
	 *                no current device</li> <li>ERROR_NULL_ARGUMENT - if the
	 *                file name is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_IO - if an IO error occurs while reading from
	 *                the file</li> <li>ERROR_INVALID_IMAGE - if the image file
	 *                contains invalid data </li> <li>ERROR_UNSUPPORTED_DEPTH -
	 *                if the image file describes an image with an unsupported
	 *                depth</li> <li>ERROR_UNSUPPORTED_FORMAT - if the image
	 *                file contains an unrecognized format</li>
	 *                </ul>
	 * @exception SWTError
	 *                <ul>
	 *                <li>ERROR_NO_HANDLES if a handle could not be obtained for
	 *                image creation</li>
	 *                </ul>
	 */
	public Image(Device device, String filename) {
		super(device);
		javafx.scene.image.Image image = new javafx.scene.image.Image("file://" + filename);
		this.image = new WritableImage(image.getPixelReader(),
				(int) image.getWidth(), (int) image.getHeight());
	}

	/**
	 * Constructs a new instance of this class based on the provided image, with
	 * an appearance that varies depending on the value of the flag. The
	 * possible flag values are:
	 * <dl>
	 * <dt><b>{@link SWT#IMAGE_COPY}</b></dt>
	 * <dd>the result is an identical copy of srcImage</dd>
	 * <dt><b>{@link SWT#IMAGE_DISABLE}</b></dt>
	 * <dd>the result is a copy of srcImage which has a <em>disabled</em> look</dd>
	 * <dt><b>{@link SWT#IMAGE_GRAY}</b></dt>
	 * <dd>the result is a copy of srcImage which has a <em>gray scale</em> look
	 * </dd>
	 * </dl>
	 * 
	 * @param device
	 *            the device on which to create the image
	 * @param srcImage
	 *            the image to use as the source
	 * @param flag
	 *            the style, either <code>IMAGE_COPY</code>,
	 *            <code>IMAGE_DISABLE</code> or <code>IMAGE_GRAY</code>
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if device is null and there is
	 *                no current device</li>
	 *                <li>ERROR_NULL_ARGUMENT - if srcImage is null</li>
	 *                <li>ERROR_INVALID_ARGUMENT - if the flag is not one of
	 *                <code>IMAGE_COPY</code>, <code>IMAGE_DISABLE</code> or
	 *                <code>IMAGE_GRAY</code></li>
	 *                <li>ERROR_INVALID_ARGUMENT - if the image has been
	 *                disposed</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_INVALID_IMAGE - if the image is not a bitmap or
	 *                an icon, or is otherwise in an invalid state</li>
	 *                <li>ERROR_UNSUPPORTED_DEPTH - if the depth of the image is
	 *                not supported</li>
	 *                </ul>
	 * @exception SWTError
	 *                <ul>
	 *                <li>ERROR_NO_HANDLES if a handle could not be obtained for
	 *                image creation</li>
	 *                </ul>
	 */
	public Image(Device device, Image srcImage, int flag) {
		super(device);
		switch (flag) {
		case SWT.IMAGE_COPY:
			image = new WritableImage(srcImage.image.getPixelReader(),
					(int) srcImage.image.getWidth(),
					(int) srcImage.image.getHeight());
			break;
		default:
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Constructs an instance of this class from the given
	 * <code>ImageData</code>.
	 * 
	 * @param device
	 *            the device on which to create the image
	 * @param data
	 *            the image data to create the image from (must not be null)
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if device is null and there is
	 *                no current device</li>
	 *                <li>ERROR_NULL_ARGUMENT - if the image data is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_UNSUPPORTED_DEPTH - if the depth of the
	 *                ImageData is not supported</li>
	 *                </ul>
	 * @exception SWTError
	 *                <ul>
	 *                <li>ERROR_NO_HANDLES if a handle could not be obtained for
	 *                image creation</li>
	 *                </ul>
	 */
	public Image(Device device, ImageData data) {
		super(device);
		image = fromData(data);
	}

	/**
	 * Constructs an instance of this class, whose type is <code>SWT.ICON</code>
	 * , from the two given <code>ImageData</code> objects. The two images must
	 * be the same size. Pixel transparency in either image will be ignored.
	 * <p>
	 * The mask image should contain white wherever the icon is to be visible,
	 * and black wherever the icon is to be transparent. In addition, the source
	 * image should contain black wherever the icon is to be transparent.
	 * </p>
	 * 
	 * @param device
	 *            the device on which to create the icon
	 * @param source
	 *            the color data for the icon
	 * @param mask
	 *            the mask data for the icon
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if device is null and there is
	 *                no current device</li>
	 *                <li>ERROR_NULL_ARGUMENT - if either the source or mask is
	 *                null</li>
	 *                <li>ERROR_INVALID_ARGUMENT - if source and mask are
	 *                different sizes</li>
	 *                </ul>
	 * @exception SWTError
	 *                <ul>
	 *                <li>ERROR_NO_HANDLES if a handle could not be obtained for
	 *                image creation</li>
	 *                </ul>
	 */
	public Image(Device device, ImageData source, ImageData mask) {
		super(device);
		image = fromData(source, ImageData.convertMask(mask));
	}

	@Override
	public void dispose() {
		super.dispose();
		image = null;
	}
	
	private WritableImage fromData(ImageData data) {
		WritableImage img = new WritableImage(data.width, data.height);
		ImageData transparencyMask = data.getTransparencyMask();
		for (int x = data.width - 1; x >= 0; x--) {
			for (int y = data.height - 1; y >= 0; y--) {
				RGB rgb = data.palette.getRGB(data.getPixel(x, y));
				int pixel = rgb.red << 16 | rgb.green << 8 | rgb.blue;
				rgb = transparencyMask.palette.getRGB(transparencyMask
						.getPixel(x, y));
				int mask = rgb.red << 16 | rgb.green << 8 | rgb.blue;
				if (mask != 0) {
					int alpha = data.getAlpha(x, y);
					if (alpha > 0) {
						pixel = pixel & 0x00FFFFFF | alpha << 24;
						img.getPixelWriter().setArgb(x, y, pixel);
					}
				}
			}
		}
		return img;
	}

	private WritableImage fromData(ImageData source, ImageData mask) {
		ImageData imageData;
		int blackIndex = 0;
		if (source.palette.isDirect) {
			imageData = new ImageData(source.width, source.height,
					source.depth, source.palette);
		} else {
			RGB black = new RGB(0, 0, 0);
			RGB[] rgbs = source.getRGBs();
			if (source.transparentPixel != -1) {
				/*
				 * The source had transparency, so we can use the transparent
				 * pixel for black.
				 */
				RGB[] newRGBs = new RGB[rgbs.length];
				System.arraycopy(rgbs, 0, newRGBs, 0, rgbs.length);
				if (source.transparentPixel >= newRGBs.length) {
					/* Grow the palette with black */
					rgbs = new RGB[source.transparentPixel + 1];
					System.arraycopy(newRGBs, 0, rgbs, 0, newRGBs.length);
					for (int i = newRGBs.length; i <= source.transparentPixel; i++) {
						rgbs[i] = new RGB(0, 0, 0);
					}
				} else {
					newRGBs[source.transparentPixel] = black;
					rgbs = newRGBs;
				}
				blackIndex = source.transparentPixel;
				imageData = new ImageData(source.width, source.height,
						source.depth, new PaletteData(rgbs));
			} else {
				while (blackIndex < rgbs.length) {
					if (rgbs[blackIndex].equals(black))
						break;
					blackIndex++;
				}
				if (blackIndex == rgbs.length) {
					/*
					 * We didn't find black in the palette, and there is no
					 * transparent pixel we can use.
					 */
					if ((1 << source.depth) > rgbs.length) {
						/* We can grow the palette and add black */
						RGB[] newRGBs = new RGB[rgbs.length + 1];
						System.arraycopy(rgbs, 0, newRGBs, 0, rgbs.length);
						newRGBs[rgbs.length] = black;
						rgbs = newRGBs;
					} else {
						/* No room to grow the palette */
						blackIndex = -1;
					}
				}
				imageData = new ImageData(source.width, source.height,
						source.depth, new PaletteData(rgbs));
			}
		}
		if (blackIndex == -1) {
			/* There was no black in the palette, so just copy the data over */
			System.arraycopy(source.data, 0, imageData.data, 0,
					imageData.data.length);
		} else {
			/* Modify the source image to contain black wherever the mask is 0 */
			int[] imagePixels = new int[imageData.width];
			int[] maskPixels = new int[mask.width];
			for (int y = 0; y < imageData.height; y++) {
				source.getPixels(0, y, imageData.width, imagePixels, 0);
				mask.getPixels(0, y, mask.width, maskPixels, 0);
				for (int i = 0; i < imagePixels.length; i++) {
					if (maskPixels[i] == 0)
						imagePixels[i] = blackIndex;
				}
				imageData.setPixels(0, y, source.width, imagePixels, 0);
			}
		}
		imageData.maskPad = mask.scanlinePad;
		imageData.maskData = mask.data;
		return fromData(imageData);
	}

	/**
	 * Returns the color to which to map the transparent pixel, or null if the
	 * receiver has no transparent pixel.
	 * <p>
	 * There are certain uses of Images that do not support transparency (for
	 * example, setting an image into a button or label). In these cases, it may
	 * be desired to simulate transparency by using the background color of the
	 * widget to paint the transparent pixels of the image. Use this method to
	 * check which color will be used in these cases in place of transparency.
	 * This value may be set with setBackground().
	 * <p>
	 * 
	 * @return the background color of the image, or null if there is no
	 *         transparency in the image
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 */
	public Color getBackground() {
		// TODO N/A?
		return null;
	}

	/**
	 * Returns the bounds of the receiver. The rectangle will always have x and
	 * y values of 0, and the width and height of the image.
	 * 
	 * @return a rectangle specifying the image's bounds
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_INVALID_IMAGE - if the image is not a bitmap or
	 *                an icon</li>
	 *                </ul>
	 */
	public Rectangle getBounds() {
		return new Rectangle(0, 0, (int)image.getWidth(), (int)image.getHeight());
	}

	/**
	 * Returns an <code>ImageData</code> based on the receiver Modifications
	 * made to this <code>ImageData</code> will not affect the Image.
	 * 
	 * @return an <code>ImageData</code> containing the image's data and
	 *         attributes
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_INVALID_IMAGE - if the image is not a bitmap or
	 *                an icon</li>
	 *                </ul>
	 * 
	 * @see ImageData
	 */
	public ImageData getImageData() {
		// TODO
		return new ImageData(
				(int)image.getWidth(), 
				(int)image.getHeight(),
				4, new PaletteData(0xff0000, 0xff00, 0xff));
	}

	public javafx.scene.image.Image getNativeImage() {
		return image;
	}
	
	/**
	 * Returns <code>true</code> if the image has been disposed, and
	 * <code>false</code> otherwise.
	 * <p>
	 * This method gets the dispose state for the image. When an image has been
	 * disposed, it is an error to invoke any other method (except
	 * {@link #dispose()}) using the image.
	 * 
	 * @return <code>true</code> when the image is disposed and
	 *         <code>false</code> otherwise
	 */
	@Override
	public boolean isDisposed() {
		return image == null;
	}

	/**
	 * Sets the color to which to map the transparent pixel.
	 * <p>
	 * There are certain uses of <code>Images</code> that do not support
	 * transparency (for example, setting an image into a button or label). In
	 * these cases, it may be desired to simulate transparency by using the
	 * background color of the widget to paint the transparent pixels of the
	 * image. This method specifies the color that will be used in these cases.
	 * For example:
	 * 
	 * <pre>
	 * Button b = new Button();
	 * image.setBackground(b.getBackground());
	 * b.setImage(image);
	 * </pre>
	 * 
	 * </p>
	 * <p>
	 * The image may be modified by this operation (in effect, the transparent
	 * regions may be filled with the supplied color). Hence this operation is
	 * not reversible and it is not legal to call this function twice or with a
	 * null argument.
	 * </p>
	 * <p>
	 * This method has no effect if the receiver does not have a transparent
	 * pixel value.
	 * </p>
	 * 
	 * @param color
	 *            the color to use when a transparent pixel is specified
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the color is null</li>
	 *                <li>ERROR_INVALID_ARGUMENT - if the color has been
	 *                disposed</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 */
	public void setBackground(Color color) {
		// TODO
	}
	
	/**	 
	 * Invokes platform specific functionality to allocate a new GC handle.
	 * <p>
	 * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
	 * API for <code>Drawable</code>. It is marked public only so that it
	 * can be shared within the packages provided by SWT. It is not
	 * available on all platforms, and should never be called from
	 * application code.
	 * </p>
	 *
	 * @param data the platform specific GC data 
	 * @return the platform specific GC handle
	 * 
	 * @noreference This method is not intended to be referenced by clients.
	 */
	 
	public long /*int*/ internal_new_GC (GCData data) {
		// TODO
		return 0;
	}

	/**	 
	 * Invokes platform specific functionality to dispose a GC handle.
	 * <p>
	 * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
	 * API for <code>Drawable</code>. It is marked public only so that it
	 * can be shared within the packages provided by SWT. It is not
	 * available on all platforms, and should never be called from
	 * application code.
	 * </p>
	 *
	 * @param handle the platform specific GC handle
	 * @param data the platform specific GC data 
	 * 
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public void internal_dispose_GC (long /*int*/ handle, GCData data) {
		// TODO
	}
}

/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.graphics;


/**
 * Instances of this class provide measurement information about fonts including
 * ascent, descent, height, leading space between rows, and average character
 * width. <code>FontMetrics</code> are obtained from <code>GC</code>s using the
 * <code>getFontMetrics()</code> method.
 * 
 * @see GC#getFontMetrics
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further
 *      information</a>
 */
public final class FontMetrics {

	private final int ascent;
	private final int descent;
	private final int averageCharWidth;
	private final int leading;
	private final int height;
	
	FontMetrics(int ascent, int descent, int averageCharWidth, int leading, int height) {
		this.ascent = ascent;
		this.descent = descent;
		this.averageCharWidth = averageCharWidth;
		this.leading = leading;
		this.height = height;
	}
	
	public static FontMetrics internal_new(int ascent, int descent, int averageCharWidth, int leading, int height) {
		return new FontMetrics(ascent, descent, averageCharWidth, leading, height);
	}

	/**
	 * Returns the ascent of the font described by the receiver. A font's
	 * <em>ascent</em> is the distance from the baseline to the top of actual
	 * characters, not including any of the leading area, measured in pixels.
	 * 
	 * @return the ascent of the font
	 */
	public int getAscent() {
		return ascent;
	}

	/**
	 * Returns the average character width, measured in pixels, of the font
	 * described by the receiver.
	 * 
	 * @return the average character width of the font
	 */
	public int getAverageCharWidth() {
		return averageCharWidth;
	}

	/**
	 * Returns the descent of the font described by the receiver. A font's
	 * <em>descent</em> is the distance from the baseline to the bottom of
	 * actual characters, not including any of the leading area, measured in
	 * pixels.
	 * 
	 * @return the descent of the font
	 */
	public int getDescent() {
		return descent;
	}

	/**
	 * Returns the height of the font described by the receiver, measured in
	 * pixels. A font's <em>height</em> is the sum of its ascent, descent and
	 * leading area.
	 * 
	 * @return the height of the font
	 * 
	 * @see #getAscent
	 * @see #getDescent
	 * @see #getLeading
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Returns the leading area of the font described by the receiver. A font's
	 * <em>leading area</em> is the space above its ascent which may include
	 * accents or other marks.
	 * 
	 * @return the leading space of the font
	 */
	public int getLeading() {
		return leading;
	}

}

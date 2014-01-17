package org.eclipse.swt.internal;

import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Drawable;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Control;

import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.Shape;
import com.sun.javafx.scene.text.TextLayout;
import com.sun.javafx.scene.text.TextLayoutFactory;
import com.sun.javafx.tk.Toolkit;

public class NoOpDrawableGC implements DrawableGC {

	private Font font;
	private Drawable d;
	
	public NoOpDrawableGC(Drawable d, Font font) {
		this.d = d;
		this.font = font;
	}
	
	@Override
	public void setBackground(Color color) {
		Util.logNotImplemented();
	}
	
	@Override
	public void fillRectangle(int x, int y, int width, int height) {
		Util.logNotImplemented();
	}
	
	@Override
	public void drawRectangle(int x, int y, int width, int height) {
		Util.logNotImplemented();
	}

	@Override
	public void fillOval(int x, int y, int width, int height) {
		Util.logNotImplemented();
	}
	
	@Override
	public void setAlpha(int alpha) {
		Util.logNotImplemented();
	}
	
	@Override
	public void setTransform(Transform transform) {
		Util.logNotImplemented();
	}
	
	@Override
	public void setForeground(Color color) {
		Util.logNotImplemented();
	}
	
	@Override
	public void fillPath(Path path) {
		Util.logNotImplemented();
	}
	
	@Override
	public void drawPath(Path path) {
		Util.logNotImplemented();
	}
	
	@Override
	public void drawImage(Image image, int srcX, int srcY, int srcWidth,
			int srcHeight, int destX, int destY, int destWidth,
			int destHeight) {
		Util.logNotImplemented();
	}
	
	@Override
	public void setLineWidth(int lineWidth) {
		Util.logNotImplemented();
	}
	
	@Override
	public void drawLine(int x1, int y1, int x2, int y2) {
		Util.logNotImplemented();
	}
	
	@Override
	public void drawImage(Image image, int x, int y) {
		Util.logNotImplemented();
	}
	
	@Override
	public void drawText (String string, int x, int y, int flags) {
		Util.logNotImplemented();
	}
	
	@Override
	public void drawPolyline(int[] pointArray) {
		Util.logNotImplemented();
	}
	
	@Override
	public Point textExtent(String string, int flags) {
		TextLayoutFactory factory = Toolkit.getToolkit().getTextLayoutFactory();
		TextLayout layout = factory.createLayout();
		layout.setContent(string, getFont().internal_getNativeObject().impl_getNativeFont());
		BaseBounds b = layout.getBounds();
				
		return new Point((int)b.getWidth(), (int)b.getHeight());
	}
	
	@Override
	public void setFont(org.eclipse.swt.graphics.Font font) {
		this.font = font;
	}
	
	@Override
	public Font getFont() {
		return font;
	}
	
	@Override
	public void fillGradientRectangle(int x, int y, int width, int height,
			boolean vertical) {
		Util.logNotImplemented();
	}
	
	@Override
	public void setClipping(Region region) {
		Util.logNotImplemented();
	}
	
	@Override
	public void fillPolygon(int[] pointArray) {
		Util.logNotImplemented();
	}
	
	@Override
	public void drawPolygon(int[] pointArray) {
		Util.logNotImplemented();
	}
	
	@Override
	public void drawRoundRectangle(int x, int y, int width, int height,
			int arcWidth, int arcHeight) {
		Util.logNotImplemented();
	}
	
	@Override
	public void fillRoundRectangle(int x, int y, int width, int height,
			int arcWidth, int arcHeight) {
		Util.logNotImplemented();
	}
	
	@Override
	public void drawPoint(int x, int y) {
		Util.logNotImplemented();
	}
	
	@Override
	public void drawFocus(int x, int y, int width, int height) {
		Util.logNotImplemented();
	}
	
	@Override
	public Color getBackground() {
		Util.logNotImplemented();
		return null;
	}
	
	@Override
	public Color getForeground() {
		Util.logNotImplemented();
		return null;
	}
	
	@Override
	public void fillArc(int x, int y, int width, int height,
			int startAngle, int arcAngle) {
		Util.logNotImplemented();
	}
	
	@Override
	public void copyArea(Image image, int x, int y) {
		if( d instanceof Control ) {
			javafx.scene.layout.Region nativeObject = ((Control) d).internal_getNativeControl();
			SnapshotParameters p = new SnapshotParameters();
			p.setViewport(new Rectangle2D(x, y, image.getBounds().width, image.getBounds().height));
			nativeObject.snapshot(p, (WritableImage) image.internal_getImage());
		}
	}
	
	@Override
	public void drawArc(int x, int y, int width, int height,
			int startAngle, int arcAngle) {
		Util.logNotImplemented();
	}
	
	@Override
	public Point stringExtent(String string) {
		TextLayoutFactory factory = Toolkit.getToolkit().getTextLayoutFactory();
		TextLayout layout = factory.createLayout();
		layout.setContent(string, getFont().internal_getNativeObject().impl_getNativeFont());
		BaseBounds b = layout.getBounds();
				
		return new Point((int)b.getWidth(), (int)b.getHeight());
	}
			
	@Override
	public void setLineCap(int cap) {
		Util.logNotImplemented();
	}
	
	@Override
	public void setLineJoin(int join) {
		Util.logNotImplemented();
	}

	@Override
	public void drawShape(int x, int y, Shape shape) {
		Util.logNotImplemented();
	}
	
	@Override
	public void setLineStyle(int lineStyle) {
		Util.logNotImplemented();
	}
	
	@Override
	public void setBackgroundPattern(Pattern pattern) {
		Util.logNotImplemented();
	}
	
	@Override
	public void setForegroundPattern(Pattern pattern) {
		Util.logNotImplemented();
	}
	
	@Override
	public void dispose() {
		Util.logNotImplemented();
	}

}

package org.eclipse.swt.internal;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.graphics.Transform;

import com.sun.javafx.geom.Shape;

public interface DrawableGC {
	public void setBackground(Color color);
	public void fillRectangle(int x, int y, int width, int height);
	public void drawRectangle(int x, int y, int width, int height);
	public void fillOval(int x, int y, int width, int height);
	public void setAlpha(int alpha);
	public void setTransform(Transform transform);
	public void setForeground(Color color);
	public void fillPath(Path path);
	public void drawPath(Path path);
	public void drawImage(Image image, int srcX, int srcY, int srcWidth,
			int srcHeight, int destX, int destY, int destWidth,
			int destHeight);
	public void setLineWidth(int lineWidth);
	public void drawLine(int x1, int y1, int x2, int y2);
	public void drawImage(Image image, int x, int y);
	public void drawText (String string, int x, int y, int flags);
	public void drawPolyline(int[] pointArray);
	public Point textExtent(String string, int flags);
	public void setFont(Font font);
	public Font getFont();
	public void fillGradientRectangle(int x, int y, int width, int height,
			boolean vertical);
	public void setClipping(Region region);
	public void fillPolygon(int[] pointArray);
	public void drawPolygon(int[] pointArray);
	public void drawRoundRectangle(int x, int y, int width, int height,
			int arcWidth, int arcHeight);
	public void fillRoundRectangle(int x, int y, int width, int height,
			int arcWidth, int arcHeight);
	public void drawPoint(int x, int y);
	public void drawFocus(int x, int y, int width, int height);
	public Color getBackground();
	public Color getForeground();
	public void copyArea(Image image, int x, int y);
	public void fillArc(int x, int y, int width, int height,
			int startAngle, int arcAngle);
	public Point stringExtent(String string);
	public void drawArc(int x, int y, int width, int height,
			int startAngle, int arcAngle);
//	public void setClipping(Rectangle rect);
	public void setLineCap(int cap);
	public void setLineJoin(int join);
	public void drawShape(int x, int y, Shape shape);
	public void setLineStyle(int lineStyle);
//	public void setClipping(int x, int y, int width, int height);
	public void dispose();
	public void setBackgroundPattern(Pattern pattern);
	public void setForegroundPattern(Pattern pattern);
}

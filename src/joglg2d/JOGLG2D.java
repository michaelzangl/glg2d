/**************************************************************************
   Copyright 2010 Brandon Borkholder

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 ***************************************************************************/

package joglg2d;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;
import java.util.WeakHashMap;

import javax.media.opengl.GL;
import javax.media.opengl.Threading;

import com.sun.opengl.util.j2d.TextRenderer;

public class JOGLG2D extends Graphics2D implements Cloneable {
  private static final Map<Font, TextRenderer> TEXT_RENDER_CACHE = new WeakHashMap<Font, TextRenderer>();

  private final GL gl;

  private final int height;

  private final int width;

  protected boolean isDisposed;

  protected Color color;

  protected Color background;

  protected Font font;

  protected JOGLShapeDrawer shapeDrawer;

  protected JOGLImageDrawer imageDrawer;

  protected Stroke stroke;

  protected Rectangle clip;

  public JOGLG2D(GL gl, int width, int height) {
    this.gl = gl;
    this.height = height;
    this.width = width;
    setStroke(new BasicStroke());
    setColor(Color.BLACK);
    setBackground(Color.BLACK);
    setFont(new Font(null, Font.PLAIN, 10));
    shapeDrawer = new JOGLShapeDrawer(gl);
    imageDrawer = new JOGLImageDrawer(gl);
  }

  protected void prePaint(Component component) {
    setBackground(component.getBackground());
    setColor(component.getForeground());
    setFont(component.getFont());
    setStroke(new BasicStroke());
    setClip(null);

    gl.glDisable(GL.GL_DEPTH_TEST);
    gl.glShadeModel(GL.GL_FLAT);
    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

    gl.glMatrixMode(GL.GL_MODELVIEW);
    gl.glPushMatrix();
    gl.glLoadIdentity();
    gl.glTranslatef(0, height, 0);
    gl.glScalef(1, -1, 1);

    gl.glMatrixMode(GL.GL_TEXTURE);
    gl.glPushMatrix();
    gl.glLoadIdentity();
  }

  protected void postPaint() {
    gl.glMatrixMode(GL.GL_MODELVIEW);
    gl.glPopMatrix();
    gl.glMatrixMode(GL.GL_PROJECTION);
    gl.glPopMatrix();
    gl.glMatrixMode(GL.GL_TEXTURE);
    gl.glPopMatrix();
    gl.glFlush();
  }

  @Override
  public void draw(Shape s) {
    shapeDrawer.draw(s, stroke);
  }

  @Override
  public void drawString(String str, int x, int y) {
    TextRenderer renderer = TEXT_RENDER_CACHE.get(font);
    if (renderer == null) {
      renderer = new TextRenderer(font);
      TEXT_RENDER_CACHE.put(font, renderer);
    }

    renderer.setColor(color);

    gl.glMatrixMode(GL.GL_MODELVIEW);
    gl.glPushMatrix();
    gl.glScalef(1, -1, 1);
    gl.glTranslatef(0, -height, 0);

    renderer.begin3DRendering();
    renderer.draw3D(str, x, height - y, 0, 1);
    renderer.end3DRendering();

    gl.glPopMatrix();
  }

  @Override
  public void drawString(String str, float x, float y) {
    drawString(str, (int) x, (int) y);
  }

  @Override
  public void drawString(AttributedCharacterIterator iterator, int x, int y) {
    assert false : "Operation not supported";
  }

  @Override
  public void drawString(AttributedCharacterIterator iterator, float x, float y) {
    assert false : "Operation not supported";
  }

  @Override
  public void drawGlyphVector(GlyphVector g, float x, float y) {
    shapeDrawer.fill(g.getOutline(x, y));
  }

  @Override
  public void fill(Shape s) {
    shapeDrawer.fill(s);
  }

  @Override
  public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public GraphicsConfiguration getDeviceConfiguration() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Composite getComposite() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setComposite(Composite comp) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setPaint(Paint paint) {
    if (paint instanceof Color) {
      setColor((Color) paint);
    } else {
      // TODO
      throw new UnsupportedOperationException();
    }
  }

  @Override
  public void setRenderingHint(Key hintKey, Object hintValue) {
    // TODO Auto-generated method stub

  }

  @Override
  public Object getRenderingHint(Key hintKey) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setRenderingHints(Map<?, ?> hints) {
    // TODO Auto-generated method stub

  }

  @Override
  public void addRenderingHints(Map<?, ?> hints) {
    // TODO Auto-generated method stub

  }

  @Override
  public RenderingHints getRenderingHints() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void translate(int x, int y) {
    gl.glMatrixMode(GL.GL_MODELVIEW);
    gl.glTranslatef(x, y, 0);
  }

  @Override
  public void translate(double tx, double ty) {
    gl.glMatrixMode(GL.GL_MODELVIEW);
    gl.glTranslated(tx, ty, 0);
  }

  @Override
  public void rotate(double theta) {
    gl.glMatrixMode(GL.GL_MODELVIEW);
    gl.glRotated(theta / Math.PI * 180, 0, 0, 1);
  }

  @Override
  public void rotate(double theta, double x, double y) {
    gl.glMatrixMode(GL.GL_MODELVIEW);
    gl.glTranslated(x, y, 0);
    gl.glRotated(theta / Math.PI * 180, 0, 0, 1);
    gl.glTranslated(-x, -y, 0);
  }

  @Override
  public void scale(double sx, double sy) {
    gl.glMatrixMode(GL.GL_MODELVIEW);
    gl.glScaled(sx, sy, 1);
  }

  @Override
  public void shear(double shx, double shy) {
    gl.glMatrixMode(GL.GL_MODELVIEW);
    double[] shear = new double[] {
        1, shy, 0, 0,
        shx, 1, 0, 0,
          0, 0, 1, 0,
          0, 0, 0, 1 };
    gl.glMultMatrixd(shear, 0);
  }

  @Override
  public void transform(AffineTransform Tx) {
    gl.glMatrixMode(GL.GL_MODELVIEW);
    multMatrix(gl, Tx);
  }

  @Override
  public void setTransform(AffineTransform transform) {
    gl.glMatrixMode(GL.GL_MODELVIEW);
    gl.glLoadIdentity();
    gl.glTranslatef(0, height, 0);
    gl.glScalef(1, -1, 1);
    multMatrix(gl, transform);
  }

  public static void multMatrix(GL gl, AffineTransform transform) {
    double[] matrix = new double[16];
    matrix[0] = transform.getScaleX();
    matrix[1] = transform.getShearY();
    matrix[4] = transform.getShearX();
    matrix[5] = transform.getScaleY();
    matrix[10] = 1;
    matrix[12] = transform.getTranslateX();
    matrix[13] = transform.getTranslateY();
    matrix[15] = 1;

    gl.glMultMatrixd(matrix, 0);
  }

  @Override
  public AffineTransform getTransform() {
    double[] m = new double[16];
    gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, m, 0);

    /*
     * Since the MODELVIEW matrix includes the transform from Java2D to OpenGL
     * coords, we remove that transform inline here.
     */
    return new AffineTransform(m[0], -m[1], m[4], -m[5], m[12], height - m[13]);
  }

  @Override
  public Paint getPaint() {
    return color;
  }

  @Override
  public Color getColor() {
    return color;
  }

  @Override
  public void setColor(Color c) {
    color = c;
    int rgb = color.getRGB();
    gl.glColor4ub((byte) (rgb >> 16 & 0xFF), (byte) (rgb >> 8 & 0xFF), (byte) (rgb & 0xFF), (byte) (rgb >> 24 & 0xFF));
  }

  @Override
  public void setBackground(Color color) {
    background = color;
    int rgb = background.getRGB();
    gl.glClearColor((rgb >> 16 & 0xFF) / 255F, (rgb >> 8 & 0xFF) / 255F, (rgb & 0xFF) / 255F, (rgb >> 24 & 0xFF) / 255F);
  }

  @Override
  public Color getBackground() {
    return background;
  }

  @Override
  public Stroke getStroke() {
    return stroke;
  }

  @Override
  public void setStroke(Stroke s) {
    stroke = s;
  }

  @Override
  public Graphics create() {
    gl.glMatrixMode(GL.GL_MODELVIEW);
    gl.glPushMatrix();
    gl.glPushAttrib(GL.GL_CURRENT_BIT | GL.GL_TRANSFORM_BIT | GL.GL_SCISSOR_BIT | GL.GL_HINT_BIT);
    return clone();
  }

  @Override
  public void setPaintMode() {
    // TODO Auto-generated method stub

  }

  @Override
  public void setXORMode(Color c1) {
    // TODO Auto-generated method stub

  }

  @Override
  public Font getFont() {
    return font;
  }

  @Override
  public void setFont(Font font) {
    this.font = font;
  }

  @Override
  public FontMetrics getFontMetrics(Font f) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public FontRenderContext getFontRenderContext() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Rectangle getClipBounds() {
    return clip;
  }

  @Override
  public void clip(Shape s) {
    setClip(s.getBounds(), true);
  }

  @Override
  public void clipRect(int x, int y, int width, int height) {
    setClip(new Rectangle(x, y, width, height), true);
  }

  @Override
  public void setClip(int x, int y, int width, int height) {
    setClip(new Rectangle(x, y, width, height), false);
  }

  @Override
  public Shape getClip() {
    return (Shape)clip.clone();
  }

  @Override
  public void setClip(Shape clipShape) {
    if (clipShape instanceof Rectangle2D) {
      setClip((Rectangle2D) clipShape, false);
    } else if (clipShape == null) {
      setClip(null, false);
    } else {
      throw new IllegalArgumentException("Illegal shape for clip bounds, only java.awt.geom.Rectangle2D objects are supported");
    }
  }

  protected void setClip(Rectangle2D clipShape, boolean intersect) {
    if (clipShape == null) {
      clip = null;
      scissor(false);
    } else if (intersect && clip != null) {
      Rectangle rect = getTransform().createTransformedShape(clipShape).getBounds();
      clip = rect.intersection(clip);
      scissor(true);
    } else {
      clip = getTransform().createTransformedShape(clipShape).getBounds();
      scissor(true);
    }
  }

  protected void scissor(boolean enable) {
    if (enable) {
      gl.glScissor(clip.x, height - clip.y - clip.height, clip.width, clip.height);
      gl.glEnable(GL.GL_SCISSOR_TEST);
    } else {
      clip = null;
      gl.glDisable(GL.GL_SCISSOR_TEST);
    }
  }

  @Override
  public void copyArea(int x, int y, int width, int height, int dx, int dy) {
    // TODO Auto-generated method stub
  }

  @Override
  public void drawLine(int x1, int y1, int x2, int y2) {
    shapeDrawer.drawLine(x1, y1, x2, y2, stroke);
  }

  @Override
  public void fillRect(int x, int y, int width, int height) {
    shapeDrawer.drawRect(x, y, width, height, true, stroke);
  }

  @Override
  public void clearRect(int x, int y, int width, int height) {
    Color origColor = color;
    setColor(background);
    fillRect(x, y, width, height);
    setColor(origColor);
  }

  @Override
  public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
    shapeDrawer.drawRoundRect(x, y, width, height, arcWidth, arcHeight, false, stroke);
  }

  @Override
  public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
    shapeDrawer.drawRoundRect(x, y, width, height, arcWidth, arcHeight, true, stroke);
  }

  @Override
  public void drawOval(int x, int y, int width, int height) {
    shapeDrawer.drawOval(x, y, width, height, false, stroke);
  }

  @Override
  public void fillOval(int x, int y, int width, int height) {
    shapeDrawer.drawOval(x, y, width, height, true, stroke);
  }

  @Override
  public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
    shapeDrawer.drawArc(x, y, width, height, startAngle, arcAngle, false, stroke);
  }

  @Override
  public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
    shapeDrawer.drawArc(x, y, width, height, startAngle, arcAngle, true, stroke);
  }

  @Override
  public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
    shapeDrawer.drawPolyline(xPoints, yPoints, nPoints, stroke);
  }

  @Override
  public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
    shapeDrawer.drawPolygon(xPoints, yPoints, nPoints, false, stroke);
  }

  @Override
  public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
    shapeDrawer.drawPolygon(xPoints, yPoints, nPoints, true, stroke);
  }

  @Override
  public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
    return imageDrawer.drawImage(img, xform, obs);
  }

  @Override
  public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
    // TODO Auto-generated method stub
  }

  @Override
  public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
    // TODO Auto-generated method stub
  }

  @Override
  public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
    // TODO Auto-generated method stub
  }

  @Override
  public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor, ImageObserver observer) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bgcolor,
      ImageObserver observer) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void dispose() {
    /*
     * This is also called on the finalizer thread, which should not make OpenGL
     * calls. We also want to make sure that this only executes once.
     */
    if (!isDisposed && Threading.isOpenGLThread()) {
      isDisposed = true;
      gl.glPopAttrib();
      gl.glMatrixMode(GL.GL_MODELVIEW);
      gl.glPopMatrix();
    }
  }

  @Override
  protected JOGLG2D clone() {
    try {
      return (JOGLG2D) super.clone();
    } catch (CloneNotSupportedException exception) {
      throw new RuntimeException(exception);
    }
  }
}

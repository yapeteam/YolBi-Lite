package cn.yapeteam.yolbi.managers;

import cn.yapeteam.loader.Natives;
import cn.yapeteam.loader.logger.Logger;
import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.render.EventRender2D;
import cn.yapeteam.yolbi.event.impl.render.EventSkijaRender;
import cn.yapeteam.yolbi.utils.IMinecraft;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Font;
import io.github.humbleui.skija.Image;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.skija.*;
import io.github.humbleui.types.RRect;
import io.github.humbleui.types.Rect;
import net.minecraft.client.gui.ScaledResolution;
import org.apache.commons.imaging.Imaging;
import org.lwjgl.opengl.Display;

import javax.swing.*;
import java.awt.Color;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class RenderManager implements IMinecraft {
    public static Surface surface;
    public static Canvas canvas;
    private static JPanel panel;
    private static JFrame frame;
    private static BufferedImage frameBuffer;
    private static final Paint paint = new Paint();

    public void init() {
        createWindow();
        updatePosition();
        ScaledResolution sr = new ScaledResolution(mc);
        int width = sr.getScaledWidth() * sr.getScaleFactor();
        int height = sr.getScaledHeight() * sr.getScaleFactor();
        surface = Surface.makeRaster(ImageInfo.makeN32Premul(width, height));
        canvas = surface.getCanvas();
        canvas.clear(new Color4f(0, 0, 0, 0).toColor());
        frame.setSize(width, height);
        frame.setVisible(true);
        Natives.SetWindowsTransparent(true, frame.getTitle());
    }

    public void shutdown() {
        frame.dispose();
    }

    private void createWindow() {
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setUndecorated(true);
        frame.setTitle("Yolbi Renderer");

        panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                YolBi.instance.getEventManager().post(new EventSkijaRender());
                try {
                    frameBuffer = Imaging.getBufferedImage(Objects.requireNonNull(EncoderPNG.encode(surface.makeImageSnapshot())).getBytes());
                    canvas.clear(new Color(0, 0, 0, 0).getRGB());
                } catch (IOException e) {
                    Logger.exception(e);
                }
                g.drawImage(frameBuffer, 0, 0, null);
            }
        };
        frame.setBackground(new java.awt.Color(0, 0, 0, 0));
        frame.setAlwaysOnTop(true);
        frame.add(panel);
    }

    private int lastX = -1, lastY = -1;
    private int lastWidth = -1, lastHeight = -1;

    private static float convertFrom(float num) {
        return num * scaledResolution.getScaleFactor();
    }

    private static float convertTo(float num) {
        return num / scaledResolution.getScaleFactor();
    }

    private static ScaledResolution scaledResolution;

    @Listener
    private void onRender2D(EventRender2D event) {
        scaledResolution = new ScaledResolution(mc);
        int width = scaledResolution.getScaledWidth() * scaledResolution.getScaleFactor();
        int height = scaledResolution.getScaledHeight() * scaledResolution.getScaleFactor();
        if (lastWidth != width || lastHeight != height) {
            System.out.println("resize: " + width + ", " + height);
            lastWidth = width;
            lastHeight = height;
            frame.setSize(width, height);
            surface.close();
            surface = Surface.makeRaster(ImageInfo.makeN32Premul(width, height));
            canvas = surface.getCanvas();
        }
        int x = Display.getX();
        int y = Display.getY();
        if (lastX != x || lastY != y) {
            lastX = x;
            lastY = y;
            System.out.println("pos: " + x + ", " + y);
            updatePosition();
        }
        if (surface == null || canvas == null) return;
        panel.repaint();
    }

    private void updatePosition() {
        int titleBarHeight = 30;
        frame.setLocation(Display.getX() + 6, Display.getY() + titleBarHeight);
    }

    public static void drawRoundedRect(float x, float y, float width, float height, float radius, int color) {
        RRect rrect = RRect.makeXYWH(convertFrom(x), convertFrom(y), convertFrom(width), convertFrom(height), radius);
        canvas.save();
        canvas.clipRRect(rrect, true); // Clip to the rounded rectangle
        canvas.drawRRect(rrect, paint.setColor(color));
        canvas.restore();
    }

    public static void drawRect(float x, float y, float width, float height, int color) {
        Rect rect = Rect.makeXYWH(convertFrom(x), convertFrom(y), convertFrom(width), convertFrom(height));
        canvas.drawRect(rect, paint.setColor(color));
    }

    public static void drawText(String text, Font font, float x, float y, int color) {
        canvas.drawString(text, convertFrom(x), convertFrom(y), font, paint.setColor(color));
    }

    public static void drawImage(Image image, float x, float y, float width, float height) {
        canvas.drawImageRect(image, Rect.makeXYWH(convertFrom(x), convertFrom(y), convertFrom(width), convertFrom(height)));
    }

    public static float[] getFontRect(Font font, String text) {
        Rect rect = font.measureText(text);
        return new float[]{convertTo(rect.getWidth()), convertTo(rect.getHeight())};
    }

    public static float getFontWidth(Font font, String text) {
        return getFontRect(font, text)[0];
    }

    public static float getFontHeight(Font font, String text) {
        return getFontRect(font, text)[1];
    }

    public static float getFontHeight(Font font) {
        return convertTo(font.getMetrics().getHeight());
    }
}

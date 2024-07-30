package cn.yapeteam.yolbi.managers;

import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.render.EventRender2D;
import io.github.humbleui.skija.*;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Color;
import io.github.humbleui.skija.Font;
import io.github.humbleui.skija.Image;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.types.RRect;
import io.github.humbleui.types.Rect;

import javax.imageio.ImageIO;
import javax.swing.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class RenderManager {

    public static Surface surface = Surface.makeRaster(ImageInfo.makeN32Premul(800, 600));
    public static Canvas canvas = surface.getCanvas();
    private static JPanel panel;
    private static BufferedImage image;
    private static JFrame frame;

    public void init(){
        createwindow();
        makewindowtransparent();
    }

    public void shutdown(){
        frame.dispose();
    }

    private void createwindow(){
        JFrame frame = new JFrame();
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setUndecorated(true);
        frame.setTitle("Yolbi Renderer");

        panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (image != null)
                    g.drawImage(image, 0, 0, null);
            }
        };
        panel.setBackground(new java.awt.Color(0, 0, 0, 0));
        frame.add(panel);
        frame.setVisible(true);
    }

    private void makewindowtransparent(){

    }

    @Listener
    public void onRender(EventRender2D eventRender2D){
        try {
            byte[] pngBytes = renderToBitmap();
            if (pngBytes != null) {
                image = ImageIO.read(new ByteArrayInputStream(pngBytes));
            }
            panel.repaint();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static byte[] renderToBitmap() {
        // Clear the canvas before drawing
        canvas.clear(new java.awt.Color(0, 0, 0, 0).getRGB());
        Data data = EncoderPNG.encode(surface.makeImageSnapshot());
        return data != null ? data.getBytes() : null;
    }

    public static void drawRoundedRect(double x, double y, double width, double height, double radius, int color) {
        RRect rrect = RRect.makeXYWH((float) x, (float) y, (float) width, (float) height, (float) radius);
        Paint paint = new Paint().setColor(color); // Set color to the provided color
        canvas.save();
        canvas.clipRRect(rrect, true); // Clip to the rounded rectangle
        canvas.drawRRect(rrect, paint);
        canvas.restore();
    }

    public static void drawRect(double x, double y, double width, double height, int color) {
        Paint paint = new Paint().setColor(color); // Set color to the provided color
        Rect rect = Rect.makeXYWH((int) x, (int) y, (int) width, (int) height);
        canvas.drawRect(rect, paint);
    }

    public static void drawText(String text, Font font, double x, double y, int color) {
        Paint paint = new Paint().setColor(color);
        canvas.drawString(text, (float) x, (float) y, font, paint);
    }

    public static void drawImage(Image image, double x, double y, double width, double height) {
        // Draw the image on the canvas
        canvas.drawImageRect(image, Rect.makeXYWH((float) x, (float) y, (float) width, (float) height));
    }
}

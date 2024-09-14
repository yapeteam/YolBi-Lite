package cn.yapeteam.yolbi.utils.math;

import cn.yapeteam.yolbi.utils.math.vector.Vector2f;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class BezureRotationVisualizer extends JPanel {

    private List<Vector2f> bezierPoints;

    public BezureRotationVisualizer(Vector2f initPos, Vector2f finPos, int deviation, int speed) {
        this.bezierPoints = BezierUtils.mouseBez(initPos, finPos, deviation, speed);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(Color.RED);
        for (int i = 0; i < bezierPoints.size() - 1; i++) {
            Vector2f p1 = bezierPoints.get(i);
            Vector2f p2 = bezierPoints.get(i + 1);
            g2d.drawLine((int) p1.getX(), (int) p1.getY(), (int) p2.getX(), (int) p2.getY());
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Bezier Rotation Visualizer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        Vector2f initPos = new Vector2f(300, 300);
        Vector2f finPos = new Vector2f(600, 400);
        int deviation = 5;
        int speed = 10;

        BezureRotationVisualizer visualizer = new BezureRotationVisualizer(initPos, finPos, deviation, speed);
        frame.add(visualizer);
        frame.setVisible(true);
    }
}
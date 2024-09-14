package cn.yapeteam.yolbi.utils.math;

import cn.yapeteam.yolbi.utils.math.vector.Vector2f;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class BezureRotationVisualizer extends JPanel {

    private List<Vector2f> bezierPoints;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(Color.RED);
        if (bezierPoints == null) return;
        for (int i = 0; i < bezierPoints.size() - 1; i++) {
            Vector2f p1 = bezierPoints.get(i);
            Vector2f p2 = bezierPoints.get(i + 1);
            g2d.drawLine((int) p1.getX(), (int) p1.getY(), (int) p2.getX(), (int) p2.getY());
        }
    }

    public List<Vector2f> paint(Vector2f initPos, Vector2f finPos, int deviation, int speed) {
        this.bezierPoints = BezierUtils.mouseBez(initPos, finPos, deviation, speed);
        repaint();
        return bezierPoints;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Bezier Rotation Visualizer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 1000);
        frame.setLayout(new BorderLayout());
        BezureRotationVisualizer visualizer = new BezureRotationVisualizer();
        frame.add(visualizer, BorderLayout.CENTER);
        Box box = Box.createVerticalBox();
        JSlider x1 = new JSlider(0, 1000, 200);
        JSlider x2 = new JSlider(0, 1000, 200);
        JSlider y1 = new JSlider(0, 1000, 200);
        JSlider y2 = new JSlider(0, 1000, 200);
        JSlider deviation = new JSlider(0, 100, 5);
        JSlider speed = new JSlider(0, 100, 10);
        JButton button = new JButton();
        JLabel count = new JLabel();
        button.setText("Generate");
        button.addActionListener((e -> count.setText("points: " +
                visualizer.paint(new Vector2f(x1.getValue(), y1.getValue()), new Vector2f(x2.getValue(), y2.getValue()), deviation.getValue(), speed.getValue()).size())));

        JLabel x11 = new JLabel("x1: " + x1.getValue());
        box.add(x11);
        x1.addChangeListener(e -> x11.setText("x1: " + x1.getValue()));
        box.add(x1);
        JLabel x21 = new JLabel("x2: " + x2.getValue());
        box.add(x21);
        x2.addChangeListener(e -> x21.setText("x2: " + x2.getValue()));
        box.add(x2);
        JLabel y11 = new JLabel("y1: " + y1.getValue());
        box.add(y11);
        y1.addChangeListener(e -> y11.setText("y1: " + y1.getValue()));
        box.add(y1);
        JLabel y21 = new JLabel("y2: " + y2.getValue());
        box.add(y21);
        y2.addChangeListener(e -> y21.setText("y2: " + y2.getValue()));
        box.add(y2);
        JLabel deviation1 = new JLabel("deviation: " + deviation.getValue());
        box.add(deviation1);
        deviation.addChangeListener(e -> deviation1.setText("deviation: " + deviation.getValue()));
        box.add(deviation);
        JLabel speed1 = new JLabel("speed: " + speed.getValue());
        box.add(speed1);
        speed.addChangeListener(e -> speed1.setText("speed: " + speed.getValue()));
        box.add(speed);
        box.add(button);
        frame.add(count, BorderLayout.NORTH);
        frame.add(box, BorderLayout.EAST);
        frame.setVisible(true);
    }
}
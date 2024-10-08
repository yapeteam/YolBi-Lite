package cn.yapeteam.yolbi.utils.math;


import cn.yapeteam.yolbi.utils.math.vector.Vector2f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class BezierUtils {

    // Pascal's Triangle Row
    public static List<Double> pascalRow(int n) {
        List<Double> result = new ArrayList<>();
        result.add(1.0);
        double x = 1.0;
        int numerator = n;

        for (int denominator = 1; denominator <= n / 2; denominator++) {
            x *= numerator;
            x /= denominator;
            result.add(x);
            numerator--;
        }

        List<Double> reversed = new ArrayList<>(result);
        Collections.reverse(reversed);

        if (n % 2 == 0) {
            result.addAll(reversed.subList(1, reversed.size()));
        } else {
            result.addAll(reversed);
        }

        return result;
    }

    // Bezier Curve Generator
    public static List<Vector2f> makeBezier(List<Vector2f> controlPoints, List<Double> ts) {
        int n = controlPoints.size();
        List<Double> combinations = pascalRow(n - 1);
        List<Vector2f> result = new ArrayList<>();

        for (double t : ts) {
            double[] tpowers = new double[n];
            double[] upowers = new double[n];

            for (int i = 0; i < n; i++) {
                tpowers[i] = Math.pow(t, i);
                upowers[i] = Math.pow(1 - t, n - 1 - i);
            }

            float x = 0;
            float y = 0;

            for (int i = 0; i < n; i++) {
                double coef = combinations.get(i) * tpowers[i] * upowers[i];
                x += coef * controlPoints.get(i).x;
                y += coef * controlPoints.get(i).y;
            }

            System.out.println(x + " " + y);
            result.add(new Vector2f(x, y));
        }

        return result;
    }

    // Generate Bezier curve points between two positions
    public static List<Vector2f> mouseBez(Vector2f initPos, Vector2f finPos, int deviation, int speed) {
        Random rand = new Random();
        Vector2f control1 = new Vector2f(
                initPos.x + (rand.nextBoolean() ? 1 : -1) * Math.abs(finPos.x - initPos.x) * 0.01f * (rand.nextInt(deviation / 2) + deviation),
                initPos.y + (rand.nextBoolean() ? 1 : -1) * Math.abs(finPos.y - initPos.y) * 0.01f * (rand.nextInt(deviation / 2) + deviation)
        );
        Vector2f control2 = new Vector2f(
                initPos.x + (rand.nextBoolean() ? 1 : -1) * Math.abs(finPos.x - initPos.x) * 0.01f * (rand.nextInt(deviation / 2) + deviation),
                initPos.y + (rand.nextBoolean() ? 1 : -1) * Math.abs(finPos.y - initPos.y) * 0.01f * (rand.nextInt(deviation / 2) + deviation)
        );

        List<Vector2f> controlPoints = new ArrayList<>();
        controlPoints.add(initPos);
        controlPoints.add(control1);
        controlPoints.add(control2);
        controlPoints.add(finPos);

        List<Double> ts = new ArrayList<>();
        for (int t = 0; t <= speed * 100; t++) {
            ts.add(t / (speed * 100.0));
        }

        return makeBezier(controlPoints, ts);
    }

}

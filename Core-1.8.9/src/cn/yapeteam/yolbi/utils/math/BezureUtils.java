package cn.yapeteam.yolbi.utils.math;

import cn.yapeteam.yolbi.utils.math.vector.Vector2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BezureUtils {


    private static List<Vector2f> bezierPoints = new ArrayList<>();
    private static int bezierIndex = 0;


    private static List<Vector2f> mouseBez(Vector2f initPos, Vector2f finPos, int deviation, int speed) {
        Random rand = new Random();
        Vector2f control1 = new Vector2f(
                (float) (initPos.getX() + (rand.nextBoolean() ? 1 : -1) * Math.abs(finPos.getX() - initPos.getX()) * 0.01 * (rand.nextInt(deviation / 2) + deviation)),
                (float) (initPos.getY() + (rand.nextBoolean() ? 1 : -1) * Math.abs(finPos.getY() - initPos.getY()) * 0.01 * (rand.nextInt(deviation / 2) + deviation))
        );
        Vector2f control2 = new Vector2f(
                (float) (initPos.getX() + (rand.nextBoolean() ? 1 : -1) * Math.abs(finPos.getX() - initPos.getX()) * 0.01 * (rand.nextInt(deviation / 2) + deviation)),
                (float) (initPos.getY() + (rand.nextBoolean() ? 1 : -1) * Math.abs(finPos.getY() - initPos.getY()) * 0.01 * (rand.nextInt(deviation / 2) + deviation))
        );
        List<Vector2f> xys = new ArrayList<>();
        xys.add(initPos);
        xys.add(control1);
        xys.add(control2);
        xys.add(finPos);
        return makeBezier(xys, speed);
    }

    private static List<Integer> pascalRow(int n) {
        List<Integer> result = new ArrayList<>();
        result.add(1);
        int x = 1, numerator = n;
        for (int denominator = 1; denominator <= n / 2; denominator++) {
            x *= numerator;
            x /= denominator;
            result.add(x);
            numerator--;
        }
        for (int i = n / 2 - 1; i >= 0; i--) {
            result.add(result.get(i));
        }
        return result;
    }

    private static List<Vector2f> makeBezier(List<Vector2f> xys, int speed) {
        int n = xys.size();
        List<Integer> combinations = pascalRow(n - 1);
        List<Vector2f> result = new ArrayList<>();
        for (int t = 0; t <= speed * 100; t++) {
            double ts = t / (speed * 100.0);
            double[] tpowers = new double[n];
            double[] upowers = new double[n];
            for (int i = 0; i < n; i++) {
                tpowers[i] = Math.pow(ts, i);
                upowers[i] = Math.pow(1 - ts, n - 1 - i);
            }
            double x = 0, y = 0;
            for (int i = 0; i < n; i++) {
                double coef = combinations.get(i) * tpowers[i] * upowers[i];
                x += coef * xys.get(i).getX();
                y += coef * xys.get(i).getY();
            }
            result.add(new Vector2f((float) x, (float) y));
        }
        return result;
    }
}

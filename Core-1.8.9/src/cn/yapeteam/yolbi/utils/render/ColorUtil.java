package cn.yapeteam.yolbi.utils.render;

import cn.yapeteam.yolbi.utils.math.MathUtils;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class ColorUtil {


    public static Color blendColors(float[] fractions, Color[] colors, float progress) {
        if (fractions.length == colors.length) {
            int[] indices = getFractionIndices(fractions, progress);
            float[] range = new float[]{fractions[indices[0]], fractions[indices[1]]};
            Color[] colorRange = new Color[]{colors[indices[0]], colors[indices[1]]};
            float max = range[1] - range[0];
            float value = progress - range[0];
            float weight = value / max;
            return blend(colorRange[0], colorRange[1], 1.0f - weight);
        }
        throw new IllegalArgumentException("Fractions and colours must have equal number of elements");
    }

    public static int[] getFractionIndices(float[] fractions, float progress) {
        int startPoint;
        int[] range = new int[2];
        for (startPoint = 0; startPoint < fractions.length && fractions[startPoint] <= progress; ++startPoint) {
        }
        if (startPoint >= fractions.length) {
            startPoint = fractions.length - 1;
        }
        range[0] = startPoint - 1;
        range[1] = startPoint;
        return range;
    }

    /**
     * Method which colors using a hex code
     *
     * @param hex used hex code
     */
    public static void glColor(final int hex) {
        final float a = (hex >> 24 & 0xFF) / 255.0F;
        final float r = (hex >> 16 & 0xFF) / 255.0F;
        final float g = (hex >> 8 & 0xFF) / 255.0F;
        final float b = (hex & 0xFF) / 255.0F;
        GL11.glColor4f(r, g, b, a);
    }

    /**
     * Method which colors using a color
     *
     * @param color used color
     */
    public static void glColor(final Color color) {
        GL11.glColor4f(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
    }

    public static Color withAlpha(final Color color, final int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) MathUtils.clamp(alpha, 0, 255));
    }

    public static Color mixColors(final Color color1, final Color color2, final double percent) {
        final double inverse_percent = 1.0 - percent;
        final int redPart = (int) (color1.getRed() * percent + color2.getRed() * inverse_percent);
        final int greenPart = (int) (color1.getGreen() * percent + color2.getGreen() * inverse_percent);
        final int bluePart = (int) (color1.getBlue() * percent + color2.getBlue() * inverse_percent);
        return new Color(redPart, greenPart, bluePart);
    }

    public static Color colorFromInt(int color) {
        Color c = new Color(color);
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), 255);
    }

    public static Color blend(Color color1, Color color2, double ratio) {
        float r = (float) ratio;
        float ir = 1.0f - r;
        float[] rgb1 = new float[3];
        float[] rgb2 = new float[3];
        color1.getColorComponents(rgb1);
        color2.getColorComponents(rgb2);
        return new Color(rgb1[0] * r + rgb2[0] * ir, rgb1[1] * r + rgb2[1] * ir, rgb1[2] * r + rgb2[2] * ir);
    }

    public static Color blend(Color color1, Color color2) {
        return ColorUtil.blend(color1, color2, 0.5);
    }

    public static Color blend(int color1, int color2) {
        return ColorUtil.blend(ColorUtil.colorFromInt(color1), ColorUtil.colorFromInt(color2), 0.5);
    }

    public static int reAlpha(int color, float alpha) {
        return reAlpha(new Color(color), alpha).getRGB();
    }

    public static Color reAlpha(Color color, float alpha) {
        float r = 0.003921569f * (float) color.getRed();
        float g = 0.003921569f * (float) color.getGreen();
        float b = 0.003921569f * (float) color.getBlue();
        return new Color(r, g, b, alpha);
    }

    public static final int buttonHoveredColor = new Color(255, 255, 255).getRGB();

    public static int getColor(Color color1, Color color2, long ms, int offset) {
        double scale = (((System.currentTimeMillis() + offset) % ms) / (double) ms) * 2;
        double finalScale = scale > 1 ? 2 - scale : scale;

        return getGradient(color1, color2, finalScale).getRGB();
    }

    public static int getColor(float hueoffset, float saturation, float brightness) {
        float speed = 4500f;
        float hue = System.currentTimeMillis() % speed / speed;
        return Color.HSBtoRGB(hue - hueoffset / 54, saturation, brightness);
    }


    public static int getColor(Color color1, Color color2, Color color3, long ms, int offset) {
        double scale = (((System.currentTimeMillis() + offset) % ms) / (double) ms) * 3;

        if (scale > 2) {
            return getGradient(color3, color1, scale - 2).getRGB();
        } else if (scale > 1) {
            return getGradient(color2, color3, scale - 1).getRGB();
        } else {
            return getGradient(color1, color2, scale).getRGB();
        }
    }

    public static Color intToColor(int color) {
        Color c1 = new Color(color);
        return new Color(c1.getRed(), c1.getGreen(), c1.getBlue(), color >> 24 & 255);
    }

    public static Color rainbow(int speed, int index, float saturation, float brightness, float opacity) {
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        float hue = angle / 360f;
        Color color = new Color(Color.HSBtoRGB(hue, saturation, brightness));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, Math.min(255, (int) (opacity * 255))));
    }

    public static Color getGradient(Color color1, Color color2, double scale) {
        scale = Math.max(0, Math.min(1, scale));

        return new Color((int) (color1.getRed() + (color2.getRed() - color1.getRed()) * scale),
                (int) (color1.getGreen() + (color2.getGreen() - color1.getGreen()) * scale),
                (int) (color1.getBlue() + (color2.getBlue() - color1.getBlue()) * scale));
    }

    public static int getRainbow(long ms, int offset, float saturation, float brightness) {
        float scale = ((System.currentTimeMillis() + offset) % ms) / (float) ms;

        return Color.HSBtoRGB(scale, saturation, brightness);
    }

    /**
     * @param startColor Color
     * @param endColor   Color
     * @param size       int
     * @return Color[]
     * @author 来自GPT的神秘力量
     */
    public static Color[] generateGradientColors(Color startColor, Color endColor, int size) {
        Color[] gradientColors = new Color[size];

        // 提取起始颜色和结束颜色的RGB值
        int startRed = startColor.getRed();
        int startGreen = startColor.getGreen();
        int startBlue = startColor.getBlue();
        int endRed = endColor.getRed();
        int endGreen = endColor.getGreen();
        int endBlue = endColor.getBlue();

        // 计算颜色差值
        int redDiff = endRed - startRed;
        int greenDiff = endGreen - startGreen;
        int blueDiff = endBlue - startBlue;

        // 计算每一步的颜色增量
        double redStep = (double) redDiff / (size - 1);
        double greenStep = (double) greenDiff / (size - 1);
        double blueStep = (double) blueDiff / (size - 1);

        // 生成渐变色数组
        for (int i = 0; i < size; i++) {
            int red = (int) (startRed + redStep * i);
            int green = (int) (startGreen + greenStep * i);
            int blue = (int) (startBlue + blueStep * i);

            gradientColors[i] = new Color(red, green, blue);
        }

        return gradientColors;
    }

    /**
     * @param startColor int
     * @param endColor   int
     * @param size       int
     * @return int[]
     * @author 来自GPT的神秘力量
     */
    public static int[] generateGradientColors(int startColor, int endColor, int size) {
        int[] gradientColors = new int[size];

        Color startColorC = new Color(startColor);
        Color endColorC = new Color(endColor);

        // 提取起始颜色和结束颜色的RGB值
        int startRed = startColorC.getRed();
        int startGreen = startColorC.getGreen();
        int startBlue = startColorC.getBlue();
        int endRed = endColorC.getRed();
        int endGreen = endColorC.getGreen();
        int endBlue = endColorC.getBlue();

        // 计算颜色差值
        int redDiff = endRed - startRed;
        int greenDiff = endGreen - startGreen;
        int blueDiff = endBlue - startBlue;

        // 计算每一步的颜色增量
        double redStep = (double) redDiff / (size - 1);
        double greenStep = (double) greenDiff / (size - 1);
        double blueStep = (double) blueDiff / (size - 1);

        // 生成渐变色数组
        for (int i = 0; i < size; i++) {
            int red = (int) (startRed + redStep * i);
            int green = (int) (startGreen + greenStep * i);
            int blue = (int) (startBlue + blueStep * i);

            gradientColors[i] = new Color(red, green, blue).getRGB();
        }

        return gradientColors;
    }

    public static int getOppositeColor(int color) {
        int R = bitChangeColor(color, 0);
        int G = bitChangeColor(color, 8);
        int B = bitChangeColor(color, 16);
        int A = bitChangeColor(color, 24);
        R = 255 - R;
        G = 255 - G;
        B = 255 - B;
        return R + (G << 8) + (B << 16) + (A << 24);
    }

    public static Color getOppositeColor(Color color) {
        return new Color(getOppositeColor(color.getRGB()));
    }


    private static int bitChangeColor(int color, int bitChange) {
        return (color >> bitChange) & 255;
    }

}
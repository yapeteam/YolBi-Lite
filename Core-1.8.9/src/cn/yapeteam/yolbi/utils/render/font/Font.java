package cn.yapeteam.yolbi.utils.render.font;

public abstract class Font {
    public abstract int draw(String text, double x, double y, int color, boolean dropShadow);

    public abstract int draw(final String text, final double x, final double y, final int color);

    public abstract int drawWithShadow(final String text, final double x, final double y, final int color);

    public abstract int width(String text);

    public abstract int drawCentered(final String text, final double x, final double y, final int color);

    public abstract int drawRight(final String text, final double x, final double y, final int color);

    public abstract float height();
}

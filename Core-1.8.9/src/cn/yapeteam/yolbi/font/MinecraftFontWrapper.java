package cn.yapeteam.yolbi.font;

import cn.yapeteam.yolbi.utils.font.Font;
import lombok.AllArgsConstructor;
import net.minecraft.client.gui.FontRenderer;

@AllArgsConstructor
public class MinecraftFontWrapper extends Font {
    private final FontRenderer fontRenderer;

    @Override
    public int draw(String text, double x, double y, int color, boolean dropShadow) {
        return fontRenderer.drawString(text, (float) x, (float) y, color, dropShadow);
    }

    @Override
    public int draw(String text, double x, double y, int color) {
        return draw(text, (float) x, (float) y, color, false);
    }

    @Override
    public int drawWithShadow(String text, double x, double y, int color) {
        return draw(text, (float) x, (float) y, color, true);
    }

    @Override
    public int width(String text) {
        return fontRenderer.getStringWidth(text);
    }

    @Override
    public int drawCentered(String text, double x, double y, int color) {
        return draw(text, (float) (x - width(text) / 2f), (float) y, color, false);
    }

    @Override
    public int drawRight(String text, double x, double y, int color) {
        return draw(text, x - width(text), y, color);
    }

    @Override
    public float height() {
        return fontRenderer.FONT_HEIGHT;
    }
}

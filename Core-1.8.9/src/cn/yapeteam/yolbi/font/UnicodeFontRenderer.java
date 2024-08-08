package cn.yapeteam.yolbi.font;

import cn.yapeteam.yolbi.font.slick.SlickException;
import cn.yapeteam.yolbi.font.slick.UnicodeFont;
import cn.yapeteam.yolbi.font.slick.font.HieroSettings;
import cn.yapeteam.yolbi.font.slick.font.effects.ColorEffect;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class UnicodeFontRenderer implements AbstractFontRenderer {
    public final UnicodeFont font;
    public int FONT_HEIGHT;
    public String gs = "";

    @Getter
    private final int scaleFactor;

    public UnicodeFontRenderer(Font awtFont) {
        HieroSettings hieroSettings = new HieroSettings();
        hieroSettings.setGlyphPageWidth(2048);
        hieroSettings.setGlyphPageHeight(2048);

        scaleFactor = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();

        hieroSettings.setFontSize((int) ((float) awtFont.getSize() / 2.0F * (float) getScaleFactor()));
        this.font = new UnicodeFont(awtFont, hieroSettings);
        font.getEffects().add(new ColorEffect(Color.WHITE));

        this.FONT_HEIGHT = this.font.getHeight("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ123456789") / getScaleFactor();
    }

    public int getHeight() {
        return this.FONT_HEIGHT;
    }

    public void load(String string) {
        if (!gs.contains(string)) {
            font.addGlyphs(string);
            gs += string;
            try {
                font.loadGlyphs();
            } catch (SlickException ignored) {
            }
        }
    }

    @Override
    public float drawString(String string, float x, float y, int color) {
        if (string == null) {
            return 0.0F;
        } else {
            load(string);
            GL11.glPushMatrix();
            GL11.glScaled(
                    1.0F / getScaleFactor(),
                    1.0F / getScaleFactor(),
                    1.0F / getScaleFactor());
            boolean blend = GL11.glIsEnabled(3042);
            boolean lighting = GL11.glIsEnabled(2896);
            boolean texture = GL11.glIsEnabled(3553);
            if (!blend) {
                GL11.glEnable(3042);
            }
            if (lighting) {
                GL11.glDisable(2896);
            }

            if (texture) {
                GL11.glDisable(3553);
            }
            // ClassLoader
            x *= (float) getScaleFactor();
            y *= (float) getScaleFactor();

            this.font.drawString((int) x, (int) y, string, new cn.yapeteam.yolbi.font.slick.Color(color));
            if (texture) {
                GL11.glEnable(3553);
            }

            if (lighting) {
                GL11.glEnable(2896);
            }

            if (!blend) {
                GL11.glDisable(3042);
            }

            GlStateManager.color(0.0F, 0.0F, 0.0F);
            GL11.glPopMatrix();
            // GlStateManager.bindTexture(0);
            return x;
        }
    }

    @Override
    public float getStringHeight() {
        return getHeight();
    }

    @Override
    public Font getFont() {
        return font.getFont();
    }

    @Override
    public float drawStringWithShadow(String text, float x, float y, int color) {
        this.drawString(text, x + 0.5F, y + 0.5F, -16777216);
        return this.drawString(text, x, y, color);
    }

    @Override
    public void drawStringWithShadow(String text, float x, float y, Color color) {
        this.drawString(text, x + 0.5F, y + 0.5F, -16777216);
        this.drawString(text, x, y, color);
    }

    @Override
    public float drawString(String text, float x, float y, int color, boolean shadow) {
        if (shadow)
            this.drawString(text, x + 0.5F, y + 0.5F, -16777216);
        return this.drawString(text, x, y, color);
    }

    @Override
    public float drawString(String text, float x, float y, Color color) {
        return 0;
    }

    public float getCharWidth(char c) {
        return this.getStringWidth(Character.toString(c));
    }

    @Override
    public float getStringWidth(String string) {
        return ((float) this.font.getWidth(string) / getScaleFactor()) + 2;
    }

    @Override
    public float getStringHeight(String string) {
        return (float) this.font.getHeight(string) / getScaleFactor();
    }

    public void drawCenteredString(String text, float x, float y, int color) {
        this.drawString(text, x - (this.getStringWidth(text) / 2), y, color);
    }
}

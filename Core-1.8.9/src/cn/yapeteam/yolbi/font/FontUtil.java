package cn.yapeteam.yolbi.font;

import cn.yapeteam.loader.logger.Logger;
import net.minecraft.client.Minecraft;

import java.awt.*;
import java.io.InputStream;

public class FontUtil {
    public static final int[] colorCode = new int[32];

    static {
        for (int i = 0; i < 32; ++i) {
            int j = (i >> 3 & 1) * 85;
            int k = (i >> 2 & 1) * 170 + j;
            int l = (i >> 1 & 1) * 170 + j;
            int i1 = (i & 1) * 170 + j;
            if (i == 6) {
                k += 85;
            }
            if (Minecraft.getMinecraft().gameSettings.anaglyph) {
                int j1 = (k * 30 + l * 59 + i1 * 11) / 100;
                int k1 = (k * 30 + l * 70) / 100;
                int l1 = (k * 30 + i1 * 70) / 100;
                k = j1;
                l = k1;
                i1 = l1;
            }
            if (i >= 16) {
                k /= 4;
                l /= 4;
                i1 /= 4;
            }
            colorCode[i] = (k & 255) << 16 | (l & 255) << 8 | i1 & 255;
        }
    }

    public static Font getFontFromTTF(InputStream is, float fontSize, int fontType) {
        Font output = null;
        try {
            output = Font.createFont(fontType, is);
            output = output.deriveFont(fontSize);
        } catch (Exception e) {
            Logger.exception(e);
        }
        return output;
    }
}

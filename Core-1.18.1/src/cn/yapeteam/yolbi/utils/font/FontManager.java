package cn.yapeteam.yolbi.utils.font;

import cn.yapeteam.loader.ResourceManager;
import cn.yapeteam.yolbi.font.renderer.FontRenderer;

import java.awt.*;
import java.io.InputStream;

public class FontManager {
    public static FontRenderer inkFree;
    public static FontRenderer tenacity;
    public static FontRenderer tenacity20;
    public static FontRenderer simkai;

    public static void init() {
        tenacity=createFontRenderer(ResourceManager.resources.getStream("fonts/fluxicon.ttf") ,25);
    }

    public static Font getFont(int size, InputStream is) {
        Font font;
        try {
            font = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(Font.PLAIN, (float) size);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Error loading font");
            font = new Font("default", Font.PLAIN, size);
        }
        return font;
    }
    public static FontRenderer createFontRenderer(InputStream font,int size) {
        return new FontRenderer(getFont(size,font),size/2f);
    }


}

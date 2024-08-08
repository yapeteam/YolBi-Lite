package cn.yapeteam.yolbi.font;

import cn.yapeteam.loader.ResourceManager;
import cn.yapeteam.yolbi.font.cfont.CFontRenderer;
import lombok.Getter;

import java.awt.*;

@Getter
public class FontManager {
    public FontManager() {
        JelloRegular18 = new UnicodeFontRenderer(getFont("JelloRegular.ttf", 18));
        JelloLight18 = new UnicodeFontRenderer(getFont("JelloLight.ttf", 18));
        JelloMedium18 = new UnicodeFontRenderer(getFont("JelloMedium.ttf", 18));
        PingFang10 = new UnicodeFontRenderer(getFont("PingFang_Normal.ttf", 10));
        PingFang12 = new UnicodeFontRenderer(getFont("PingFang_Normal.ttf", 12));
        PingFang13 = new UnicodeFontRenderer(getFont("PingFang_Normal.ttf", 13));
        PingFang14 = new UnicodeFontRenderer(getFont("PingFang_Normal.ttf", 14));
        PingFang18 = new UnicodeFontRenderer(getFont("PingFang_Normal.ttf", 18));
        CPingFang18 = new CFontRenderer(getFont("PingFang_Normal.ttf", 18), true, true);
        PingFang16 = new UnicodeFontRenderer(getFont("PingFang_Normal.ttf", 16));
        PingFangBold18 = new UnicodeFontRenderer(getFont("PingFang_Bold.ttf", 18));
        FLUXICON14 = new UnicodeFontRenderer(getFont("fluxicon.ttf", 18));
        RobotoLight = new UnicodeFontRenderer(getFont("RobotoLight.ttf", 16));
        default18 = new UnicodeFontRenderer(new Font(null, Font.PLAIN, 18));
    }

    private static Font getFont(String name, int size, int type) {
        return FontUtil.getFontFromTTF(ResourceManager.resources.getStream("fonts/" + name), size, type);
    }

    private static Font getFont(String name, int size) {
        return FontUtil.getFontFromTTF(ResourceManager.resources.getStream("fonts/" + name), size, Font.PLAIN);
    }

    private final AbstractFontRenderer JelloRegular18;
    private final AbstractFontRenderer JelloLight18;
    private final AbstractFontRenderer JelloMedium18;
    private final AbstractFontRenderer PingFang10;
    private final AbstractFontRenderer PingFang12;
    private final AbstractFontRenderer PingFang13;
    private final AbstractFontRenderer PingFang14;
    private final AbstractFontRenderer PingFang16;
    private final AbstractFontRenderer PingFang18;
    private final AbstractFontRenderer CPingFang18;
    private final AbstractFontRenderer PingFangBold18;
    private final AbstractFontRenderer FLUXICON14;
    private final AbstractFontRenderer default18;
    private final AbstractFontRenderer RobotoLight;
}

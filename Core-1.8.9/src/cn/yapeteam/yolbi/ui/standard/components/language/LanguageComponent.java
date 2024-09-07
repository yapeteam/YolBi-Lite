package cn.yapeteam.yolbi.ui.standard.components.language;


import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.managers.RenderManager;
import cn.yapeteam.yolbi.ui.standard.screen.Colors;
import cn.yapeteam.yolbi.utils.render.font.impl.general.Fonts;
import cn.yapeteam.yolbi.utils.render.font.impl.general.Weight;
import cn.yapeteam.yolbi.utils.interfaces.Accessor;
import cn.yapeteam.yolbi.utils.profiling.localization.Locale;
import cn.yapeteam.yolbi.utils.render.GuiUtil;
import cn.yapeteam.yolbi.utils.math.vector.Vector2d;
import cn.yapeteam.yolbi.utils.math.vector.Vector2f;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.util.ResourceLocation;

/**
 * @author Hazsi
 * @since 10/31/22
 */
@Getter
@RequiredArgsConstructor
public class LanguageComponent implements Accessor {
    private final Locale locale;
    private final String localName, englishName;

    private double lastY;

    public void draw(double y) {
        final Vector2f position = getClickGUI().getPosition();
        final Vector2f scale = getClickGUI().getScale();
        final double sidebar = getClickGUI().getSidebar().sidebarWidth;

        RenderManager.roundedRectangle(position.getX() + sidebar + 8, position.getY() + y, 285,
                38, 6, Colors.OVERLAY.get());

        // Draw locale english name
        Fonts.MAIN.get(20, Weight.REGULAR).draw(this.englishName, position.getX() + sidebar + 18, position.getY() + y + 9,
                YolBi.instance.getLocale().equals(this.locale) ? getTheme().getAccentColor(new Vector2d(0, position.y / 5)).getRGB() :
                        Colors.TEXT.getRGB());

        // Draw locale native name
        Fonts.MAIN.get(17, Weight.REGULAR).draw(this.localName, position.getX() + sidebar + 18,
                position.getY() + y + 24, Colors.TEXT.getRGBWithAlpha(100));

        // Draw flag
        RenderManager.image(new ResourceLocation("rise/icons/language/" + locale.getFile() + ".png"),
                position.getX() + sidebar + Fonts.MAIN.get(20, Weight.REGULAR).width(this.englishName) + 25, position.getY() + y + 5, 15, 15);

        this.lastY = y;
    }

    public void click(double mouseX, double mouseY) {
        final Vector2f position = getClickGUI().getPosition();
        final double sidebar = getClickGUI().getSidebar().sidebarWidth;

        if (GuiUtil.mouseOver(position.getX() + sidebar + 8, position.getY() + lastY,
                285, 38, mouseX, mouseY)) {
            YolBi.instance.setLocale(this.locale);
        }
    }
}
package cn.yapeteam.yolbi.ui.standard.components.category;


import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.managers.RenderManager;
import cn.yapeteam.yolbi.module.api.Category;
import cn.yapeteam.yolbi.ui.standard.RiseClickGUI;
import cn.yapeteam.yolbi.ui.standard.screen.Screen;
import cn.yapeteam.yolbi.utils.render.animation.Animation;
import cn.yapeteam.yolbi.utils.render.font.impl.general.Fonts;
import cn.yapeteam.yolbi.utils.render.font.impl.general.Weight;
import cn.yapeteam.yolbi.utils.interfaces.Accessor;
import cn.yapeteam.yolbi.utils.profiling.localization.Localization;
import cn.yapeteam.yolbi.utils.render.ColorUtil;
import cn.yapeteam.yolbi.utils.render.GuiUtil;
import cn.yapeteam.yolbi.utils.math.vector.Vector2d;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

import static cn.yapeteam.yolbi.utils.render.animation.Easing.LINEAR;

public final class CategoryComponent implements Accessor {

    private final Animation animation = new Animation(LINEAR, 500);
    public final Category category;
    private long lastTime = 0;
    private double selectorOpacity;

    private float x, y;
    private boolean down;

    public CategoryComponent(final Category category) {
        this.category = category;
    }

    public void render(final double offset, final double sidebarWidth, final double opacity, final Screen selectedScreen) {
        final RiseClickGUI clickGUI = YolBi.instance.getClickGUI();

        if (System.currentTimeMillis() - lastTime > 300) lastTime = System.currentTimeMillis();
        final long time = System.currentTimeMillis();

        /* Gets position depending on sidebar animation */
        x = (float) (clickGUI.position.x - (69 - sidebarWidth) - 21);
        y = (float) (clickGUI.position.y + offset) + 16;

        /* Animations */
        animation.setDuration(200);
        animation.run(selectedScreen.equals(category.getClickGUIScreen()) ? 255 : 0);

        final double spacer = 4;
        final double width = Fonts.MAIN.get(16, Weight.REGULAR).width(Localization.get(category.getName())) + spacer * 2 + category.getFontRenderer().width(category.getIcon());

        double scale = 0.5;
        GlStateManager.pushMatrix();
//        GlStateManager.translate(x, y, 0);
//        GlStateManager.scale(scale, scale, 1);

        /* Draws selection */
        //RenderManager.roundedRectangle(x + 1.5, y - 6.5, width + 9, 17, 6,
        //     ColorUtil.withAlpha(getTheme().getAccentColor(new Vector2d(0, y / 5D)), (int) (Math.min(animation.getValue(), opacity))).darker());

        RenderManager.roundedRectangle(x, y - 5.5, width + 8, 15, 5,
                ColorUtil.withAlpha(getTheme().getAccentColor(new Vector2d(0, y / 5D)), (int) (Math.min(animation.getValue(), opacity))).darker());

        int color = new Color(255, 255, 255, Math.min(selectedScreen.equals(category.getClickGUIScreen()) ? 255 : 200, (int) opacity)).hashCode();

        category.getFontRenderer().draw(category.getIcon(), (float) (x + animation.getValue() / 80f + 3), y, color);

        Fonts.MAIN.get(16, Weight.REGULAR).draw(Localization.get(category.getName()), (float) (x + animation.getValue() / 80f + 3 + spacer) +
                Fonts.ICONS_1.get(17).width(category.getIcon()), y, color);

        GlStateManager.popMatrix();

        lastTime = time;
    }

    public void click(final float mouseX, final float mouseY, final int button) {
        final boolean left = button == 0;
        if (GuiUtil.mouseOver(x - 11, y - 5, 70, 22, mouseX, mouseY) && left) {
            this.getClickGUI().switchScreen(this.category);
            down = true;
        }
    }

    public void bloom(final double opacity) {
        final double spacer = 4;
        final double width = Fonts.MAIN.get(16, Weight.REGULAR).width(Localization.get(category.getName())) + spacer * 2 + category.getFontRenderer().width(category.getIcon());

        RenderManager.roundedRectangle(x, y - 5, width + 8, 14, 5,
                ColorUtil.withAlpha(this.getTheme().getAccentColor(new Vector2d(0, y / 5D)), (int) (Math.min(animation.getValue(), opacity))).darker());
    }

    public void release() {
        down = false;
    }
}

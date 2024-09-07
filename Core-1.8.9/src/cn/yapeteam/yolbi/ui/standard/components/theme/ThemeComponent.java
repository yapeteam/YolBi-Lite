package cn.yapeteam.yolbi.ui.standard.components.theme;


import cn.yapeteam.yolbi.utils.render.font.impl.general.Fonts;
import cn.yapeteam.yolbi.utils.render.font.impl.general.Weight;
import cn.yapeteam.yolbi.managers.RenderManager;
import cn.yapeteam.yolbi.ui.standard.RiseClickGUI;
import cn.yapeteam.yolbi.ui.theme.Themes;
import cn.yapeteam.yolbi.utils.render.animation.Animation;
import cn.yapeteam.yolbi.utils.render.animation.Easing;
import cn.yapeteam.yolbi.utils.interfaces.Accessor;
import cn.yapeteam.yolbi.utils.render.ColorUtil;
import cn.yapeteam.yolbi.utils.math.vector.Vector3d;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static cn.yapeteam.yolbi.utils.render.layer.Layers.BLOOM;

/**
 * @author Hazsi
 * @since 10/15/2022
 */
@Getter
@RequiredArgsConstructor
public class ThemeComponent implements Accessor {
    private final Themes activeTheme;
    private Vector3d lastDraw = new Vector3d(0, 0, 0);

    private final Animation xAnimation = new Animation(Easing.EASE_OUT_QUINT, 500);
    private final Animation yAnimation = new Animation(Easing.EASE_OUT_QUINT, 500);
    private final Animation opacityAnimation = new Animation(Easing.EASE_OUT_QUINT, 500);
    private final Animation selectorAnimation = new Animation(Easing.EASE_OUT_QUINT, 500);

    public void draw(double yOffset, double width) {
        final int alpha = (int) opacityAnimation.getValue();

        final boolean active = this.activeTheme.equals(this.getTheme());
        final Color color = active ? new Color(15, 19, 26, (int) opacityAnimation.getValue()) :
                new Color(18, 21, 30, alpha);

        final double x = this.xAnimation.getValue();
        final double y = this.yAnimation.getValue() + yOffset;

        // This needs to be done in a runnable so that's its run AFTER the NORMAL_BLOOM_RUNNABLES runnable

        // Draw background
        RenderManager.roundedRectangle(x, y, width, 50, 10, color);

        if (this.activeTheme.isTriColor()) {
            RenderManager.drawRoundedGradientRectTest(x, y, width, 30, 9,
                    ColorUtil.withAlpha(activeTheme.getFirstColor(), alpha),
                    ColorUtil.withAlpha(activeTheme.getSecondColor(), alpha),
                    ColorUtil.withAlpha(activeTheme.getThirdColor(), alpha), false,
                    true, true, false, false);
        } else {
            RenderManager.drawRoundedGradientRectTest(x, y, width, 30, 9,
                    ColorUtil.withAlpha(activeTheme.getFirstColor(), alpha),
                    ColorUtil.withAlpha(activeTheme.getSecondColor(), alpha), false,
                    true, true, false, false);
        }

        RenderManager.rectangle(x, y + 30, width, 10, color);

        Fonts.MAIN.get(16, Weight.REGULAR).drawCentered(activeTheme.getThemeName(),
                x + width / 2D, y + 37, active ? ColorUtil.withAlpha(this.getTheme().getFirstColor(), alpha).getRGB() :
                        new Color(255, 255, 255, alpha).getRGB());

        // Render selector
        selectorAnimation.run(this.activeTheme.equals(getTheme()) ? 255 : 0);
        int selectorAlpha = (int) Math.min(selectorAnimation.getValue(), alpha);

        RiseClickGUI clickGUI = getClickGUI();
        if (selectorAlpha > 0 && clickGUI.animationTime > 0.8) {
            getLayer(BLOOM, 3).add(() -> {
                GlStateManager.pushMatrix();
                GL11.glEnable(GL11.GL_SCISSOR_TEST);
                int padding = 1;
                RenderManager.scissor(
                        clickGUI.position.x * clickGUI.animationTime + clickGUI.getTranslate().x + padding,
                        clickGUI.position.y * clickGUI.animationTime + clickGUI.getTranslate().y + padding,
                        clickGUI.scale.x * clickGUI.animationTime - padding * 2,
                        clickGUI.scale.y * clickGUI.animationTime - padding * 2
                );
                if (this.activeTheme.isTriColor()) {
                    RenderManager.drawRoundedGradientRectTest(x, y, width, 30, 10,
                            ColorUtil.withAlpha(activeTheme.getFirstColor(), alpha),
                            ColorUtil.withAlpha(activeTheme.getSecondColor(), alpha),
                            ColorUtil.withAlpha(activeTheme.getThirdColor(), alpha), false,
                            true, true, false, false);
                } else {
                    RenderManager.drawRoundedGradientRectTest(x + 1, y, width - 2, 30, 10,
                            ColorUtil.withAlpha(activeTheme.getFirstColor(), selectorAlpha),
                            ColorUtil.withAlpha(activeTheme.getSecondColor(), selectorAlpha), false,
                            true, true, false, false);
                }
                Fonts.MAIN.get(16, Weight.REGULAR).drawCentered(activeTheme.getThemeName(),
                        x + width / 2D, y + 37, ColorUtil.withAlpha(activeTheme.getFirstColor(), selectorAlpha).getRGB());
                GL11.glDisable(GL11.GL_SCISSOR_TEST);
                GlStateManager.popMatrix();
            });
        }

        this.lastDraw = new Vector3d(x, y, width);
    }
}

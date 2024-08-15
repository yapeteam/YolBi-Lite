package cn.yapeteam.yolbi.ui;

import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.font.AbstractFontRenderer;
import cn.yapeteam.yolbi.module.ModuleCategory;
import cn.yapeteam.yolbi.utils.IMinecraft;
import cn.yapeteam.yolbi.utils.render.GuiUtil;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

public class CatagoryComponent implements IMinecraft {

    private float x, y;

    public ModuleCategory category;

    public void CategoryComponent(final ModuleCategory category) {
        this.category = category;
    }

    AbstractFontRenderer productSans16 = YolBi.instance.getFontManager().getProductSansRegular16();

    public void render(final double offset, final double sidebarWidth, final double opacity) {
        /* Gets position depending on sidebar animation */
        x = (float) (YolbiClickGui.position.x - (69 - sidebarWidth) - 21);
        y = (float) (YolbiClickGui.position.y + offset) + 16;

        final double spacer = 4;
        final double width = productSans16.getStringWidth(YolbiClickGui.currentCategory.name());

        double scale = 0.5;
        GlStateManager.pushMatrix();
//        GlStateManager.translate(x, y, 0);
//        GlStateManager.scale(scale, scale, 1);

        /* Draws selection */
        YolBi.instance.getRenderManager().roundedRectangle(x + 1.5, y - 5.5, width + 9, 15, 4,
                new Color(79, 199, 200, (int) opacity).darker().getRGB()
        );

//        int color = new Color(255, 255, 255, Math.min(selectedScreen.(category.getClickGUIScreen()) ? 255 : 200, (int) opacity)).hashCode();

//        category.getFontRenderer().drawString(category.getIcon(), (float) (x + animation.getValue() / 80f + 3), y, color);
//
//        YolbiClickGui.nunitoSmall.drawString(Localization.get(category.getName()), (float) (x + animation.getValue() / 80f + 3 + spacer) +
//                FontManager.getIconsOne(17).width(category.getIcon()), y, color);

        GlStateManager.popMatrix();
    }

    public void click(final float mouseX, final float mouseY, final int button) {
        final boolean left = button == 0;
        if (GuiUtil.mouseOver(x - 11, y - 5, 70, 22, mouseX, mouseY) && left) {

        }
    }



}

// Java
package cn.yapeteam.yolbi.ui;

import cn.yapeteam.loader.logger.Logger;
import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.font.AbstractFontRenderer;
import cn.yapeteam.yolbi.module.ModuleCategory;
import cn.yapeteam.yolbi.utils.IMinecraft;
import cn.yapeteam.yolbi.utils.render.GuiUtil;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

public final class CategoryComponent implements IMinecraft {

    private float x, y;

    public ModuleCategory category;

    public CategoryComponent(final ModuleCategory category) {
        this.category = category;
    }

    AbstractFontRenderer productSans16 = YolBi.instance.getFontManager().getProductSansRegular16();

    public void render(final double offset, final double sidebarWidth, final double opacity, final float mouseX, final float mouseY) {
        /* Gets position depending on sidebar animation */
        x = (float) (YolbiClickGui.position.x - (69 - sidebarWidth) - 21);
        y = (float) (YolbiClickGui.position.y + offset) + 16;

        final double width = productSans16.getStringWidth(category.name());

        GlStateManager.pushMatrix();

        /* Check if the category is hovered */
        boolean isHovered = GuiUtil.mouseOver(x - 11, y - 5, 70, 22, mouseX, mouseY);
        int rectOpacity = (int) opacity;
        if (isHovered) {
            rectOpacity = 200; // Slightly glow when hovered
        }
        if (YolbiClickGui.currentCategory == category) {
            rectOpacity = 255; // Full opacity for selected category
        }

        /* Draws selection */
        YolBi.instance.getRenderManager().roundedRectangle(x + 1.5, y - 5.5, width + 9, 15, 4,
                new Color(79, 199, 200, rectOpacity).darker().getRGB()
        );

        productSans16.drawString(category.name(), x + 7, y, Color.WHITE);

        GlStateManager.popMatrix();
    }

    public void click(final float mouseX, final float mouseY, final int button) {
        final boolean left = button == 0;
        Logger.info("CategoryComponent click: " + category.name() + "is left: " + left);
        if (GuiUtil.mouseOver(x - 11, y - 5, 70, 22, mouseX, mouseY) && left) {
            YolbiClickGui.currentCategory = category; // Set the clicked category as the current category
        }
    }
}
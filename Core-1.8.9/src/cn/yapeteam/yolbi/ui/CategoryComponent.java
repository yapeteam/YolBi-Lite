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

    AbstractFontRenderer icon17 = YolBi.instance.getFontManager().getIcons17();

    public void render(final double offset, final double sidebarWidth, final double opacity, final float mouseX, final float mouseY) {
        /* Gets position depending on sidebar animation */
        x = (float) (YolbiClickGui.position.x - (69 - sidebarWidth) - 21);
        y = (float) (YolbiClickGui.position.y + offset) + 30;

        final double width = productSans16.getStringWidth(category.name()) + icon17.getStringWidth(category.getIcon());

        GlStateManager.pushMatrix();

        /* Check if the category is hovered */
        boolean isHovered = GuiUtil.mouseOver(x - 11, y - 5, 70, 22, mouseX, mouseY);
        Color rectColor = new Color(0, 0, 0, 0);
        if (isHovered) {
            rectColor = new Color(66, 68, 73, 255); // Hover color
        }
        if (YolbiClickGui.currentCategory == category) {
            rectColor = new Color(46, 66, 109, 255); // Selected color
        }


        /* Draws selection */
        YolBi.instance.getRenderManager().roundedRectangle(x + 1.5 + (YolbiClickGui.currentCategory == category ? 3 : 0), y - 5.5, width + 11, 15, 4, rectColor.getRGB());

        // draws icon
        icon17.drawString(category.getIcon(), x + 4 + (YolbiClickGui.currentCategory == category ? 3 : 0), y, new Color(255, 255, 255));

        productSans16.drawString(category.name(), x + 7 + icon17.getStringWidth(category.getIcon()) + (YolbiClickGui.currentCategory == category ? 3 : 0), y, new Color(245, 245, 247));

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
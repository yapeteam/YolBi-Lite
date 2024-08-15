// Java
package cn.yapeteam.yolbi.ui;

import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.font.AbstractFontRenderer;
import cn.yapeteam.yolbi.module.ModuleCategory;
import cn.yapeteam.yolbi.utils.render.ColorUtil;
import cn.yapeteam.yolbi.utils.render.GuiUtil;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SideBarComponent {

    /* Information */
    public double sidebarWidth = 100;
    private double opacity;

    AbstractFontRenderer productSans32 = YolBi.instance.getFontManager().getProductSansRegular32();
    AbstractFontRenderer productSans16 = YolBi.instance.getFontManager().getProductSansRegular16();

    private List<CategoryComponent> categories;

    public SideBarComponent() {
        categories = Arrays.stream(ModuleCategory.values())
                .map(CategoryComponent::new)
                .collect(Collectors.toList());
    }

    public void renderSidebar(final float mouseX, final float mouseY) {

        if (GuiUtil.mouseOver(YolbiClickGui.position.x, YolbiClickGui.position.y, opacity > 0 ? 70 : 10, YolbiClickGui.scale.y, mouseX, mouseY)) {
            opacity = 255;
        } else {
            opacity = 0;
        }

        // rendering the yolbi logo and version

        final float posX = YolbiClickGui.position.getX() + 9;
        final float posY = YolbiClickGui.position.getY() + ((19.5F + 30) / 2.0F - productSans32.getStringHeight() / 2.0F);

        productSans32.drawString(YolBi.name, posX + 5, posY + 2, Color.WHITE.getRGB());
        productSans16.drawString(YolBi.version, posX + 5 + productSans32.getStringWidth(YolBi.name), posY, new Color(255, 255, 255, 200).getRGB());

        // rendering the categories

        /* Renders all categories */
        double offsetTop = 10;

        for (final CategoryComponent category : categories) {
            category.render((offsetTop += 19.5), sidebarWidth, (int) opacity, mouseX, mouseY);
        }

        productSans32.drawString(YolBi.name, posX + 5, posY + 2, ColorUtil.reAlpha(Color.WHITE, (int) opacity / 255F).hashCode());
        productSans16.drawString(YolBi.version, posX + 5 + productSans32.getStringWidth(YolBi.name), posY, cn.yapeteam.yolbi.utils.render.ColorUtil.reAlpha(new Color(185, 250, 255), (int) Math.min(opacity, 200) / 255F).getRGB());
    }

    public void clickSidebar(final float mouseX, final float mouseY, final int button) {
        for (final CategoryComponent category : categories) {
            category.click(mouseX, mouseY, button);
        }
    }
}
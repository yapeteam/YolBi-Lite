package cn.yapeteam.yolbi.ui.standard.screen.impl;


import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.font.Fonts;
import cn.yapeteam.yolbi.font.Weight;
import cn.yapeteam.yolbi.ui.standard.components.theme.ThemeComponent;
import cn.yapeteam.yolbi.ui.standard.components.theme.ThemeKeyColorComponent;
import cn.yapeteam.yolbi.ui.standard.screen.Screen;
import cn.yapeteam.yolbi.ui.theme.Themes;
import cn.yapeteam.yolbi.utils.interfaces.Accessor;
import cn.yapeteam.yolbi.utils.render.GuiUtil;
import cn.yapeteam.yolbi.utils.render.ScrollUtil;
import cn.yapeteam.yolbi.utils.vector.Vector2d;
import cn.yapeteam.yolbi.utils.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;

/**
 * @author Hazsi
 * @since 10/15/2022
 */
public class ThemeScreen implements Screen, Accessor {
    private final ArrayList<ThemeComponent> allThemes = new ArrayList<>();
    private ArrayList<ThemeComponent> visibleThemes = new ArrayList<>();

    private final ArrayList<ThemeKeyColorComponent> colors = new ArrayList<>();
    private final ScrollUtil scrollUtil = new ScrollUtil();

    private ThemeKeyColorComponent selectedColor = null;

    public ThemeScreen() {
        for (Themes themes : Themes.values()) {
            this.allThemes.add(new ThemeComponent(themes));
        }
        for (Themes.KeyColors colors : Themes.KeyColors.values()) {
            this.colors.add(new ThemeKeyColorComponent(colors));
        }
    }

    @Override
    public void onRender(int mouseX, int mouseY, float partialTicks) {
        scrollUtil.onRender();

//        scrollUtil.setMax( -(60 + 57 * (Math.floor(visibleThemes.size() / 3D) - 3)));
        final double rows = Math.ceil(visibleThemes.size() / 3D);
        scrollUtil.setMax(-57 * Math.max(0, (rows - 3)));

        double padding = 7;
        double scrollX = getClickGUI().getPosition().getX() + getClickGUI().getScale().getX() - 4;
        double scrollY = getClickGUI().getPosition().getY() + padding;

        scrollUtil.renderScrollBar(new Vector2d(scrollX, scrollY), getClickGUI().scale.y - padding * 2);

        final Vector2f position = getClickGUI().getPosition();
        final Vector2f scale = getClickGUI().getScale();
        final double sidebar = getClickGUI().getSidebar().sidebarWidth;
        final double positionY = position.getY() + 44 + scrollUtil.getScroll();

        final double themeWidth = (scale.getX() - sidebar - 29) / 3D;
        final double colorWidth = (scale.getX() - sidebar - 43) / 5D;

//        FontManager.getIconsThree(28).drawString("4",
//                position.getX() + sidebar + 14, position.getY() + 16 + scrollUtil.getScroll(), -1);
        Fonts.MAIN.get(16, Weight.REGULAR).drawRight("You can click on a color to filter by it. Click again to reset.",
                position.getX() + scale.getX() - 20, position.getY() + 20 + scrollUtil.getScroll(), new Color(255, 255, 255, 128).getRGB());

        // Draw key colors
        for (int i = 0; i < this.colors.size(); i++) {
            ThemeKeyColorComponent color = this.colors.get(i);

            color.draw(position.getX() + sidebar + 7 + ((7 + colorWidth) * (i % 5)),
                    positionY + Math.floor(i / 5D) * 24, colorWidth, this.selectedColor != null && this.selectedColor.equals(color));

            color.getDimAnimation().run(this.selectedColor == null || this.selectedColor.equals(color) ? 1 : 0);
            color.getBloomAnimation().run(Objects.equals(this.selectedColor, color) ? 1 : 0);
        }

        // Draw themes
        for (int i = 0; i < this.visibleThemes.size(); i++) {
            ThemeComponent theme = this.visibleThemes.get(i);

            theme.getXAnimation().run(position.getX() + sidebar + 7 + ((7 + themeWidth) * (i % 3)));
            theme.getYAnimation().run(position.getY() + 44 + Math.floor(i / 3D) * 57 + 60);
        }

        for (ThemeComponent theme : this.allThemes) {
            if (theme.getOpacityAnimation().getValue() > 0) {
                theme.draw(this.scrollUtil.getScroll(), themeWidth);
            }

            theme.getOpacityAnimation().run(this.visibleThemes.contains(theme) ? 255 : 0);
        }
    }

    @Override
    public void onClick(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton > 0) return;

        for (ThemeComponent theme : this.visibleThemes) {
            if (GuiUtil.mouseOver(theme.getLastDraw().getX(), theme.getLastDraw().getY(), theme.getLastDraw().getZ(), 50, mouseX, mouseY)) {
                YolBi.instance.getThemeManager().setTheme(theme.getActiveTheme());
            }
        }

        for (ThemeKeyColorComponent color : this.colors) {
            if (GuiUtil.mouseOver(color.getLastDraw().getX(), color.getLastDraw().getY(), color.getLastDraw().getZ(), 17, mouseX, mouseY)) {
                if (this.selectedColor == color) {
                    this.selectedColor = null;
                } else {
                    this.selectedColor = color;
                }

                this.sortThemes();
            }
        }
    }
    private void sortThemes() {
        this.visibleThemes.clear();

        if (this.selectedColor == null) {
            this.visibleThemes = new ArrayList<>(this.allThemes);
            this.visibleThemes.forEach(themeComponent -> themeComponent.getOpacityAnimation().run(255));
            return;
        }

        for (ThemeComponent theme : this.allThemes) {
            if (theme.getActiveTheme().getKeyColors().contains(this.selectedColor.getColor())) {
                this.visibleThemes.add(theme);
                theme.getOpacityAnimation().run(255);
            }
//            else {
//                theme.getOpacityAnimation().run(0);
//            }
        }
    }

    @Override
    public void onInit() {
        this.allThemes.forEach(theme -> theme.getOpacityAnimation().setValue(255));
        this.visibleThemes = new ArrayList<>(this.allThemes);
        this.selectedColor = null;
        this.scrollUtil.reset();
        this.resetAnimations();
    }

    public void resetAnimations() {
        final Vector2f position = getClickGUI().getPosition();
        final Vector2f scale = getClickGUI().getScale();
        final double sidebar = getClickGUI().getSidebar().sidebarWidth;
        final double themeWidth = (scale.getX() - sidebar - 29) / 3D;

        for (int i = 0; i < this.visibleThemes.size(); i++) {
            ThemeComponent theme = this.visibleThemes.get(i);

            theme.getXAnimation().setValue(position.getX() + sidebar + 7 + ((7 + themeWidth) * (i % 3)));
            theme.getYAnimation().setValue(position.getY() + 44 + Math.floor(i / 3D) * 57 + 60);
        }
    }
}
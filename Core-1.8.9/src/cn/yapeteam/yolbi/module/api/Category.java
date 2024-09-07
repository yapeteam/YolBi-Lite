package cn.yapeteam.yolbi.module.api;

import cn.yapeteam.yolbi.utils.render.font.impl.general.Fonts;
import cn.yapeteam.yolbi.ui.standard.screen.Screen;
import cn.yapeteam.yolbi.ui.standard.screen.impl.*;
import cn.yapeteam.yolbi.utils.render.font.Font;
import lombok.Getter;

/**
 * @author Patrick
 * @since 10/19/2021
 */
@Getter
public enum Category {
    SEARCH("category.search", Fonts.ICONS_2.get(17), "U", 0x1, new SearchScreen()),
    COMBAT("category.combat", Fonts.ICONS_1.get(17), "a", 0x2, new CategoryScreen()),
    MOVEMENT("category.movement", Fonts.ICONS_1.get(17), "b", 0x3, new CategoryScreen()),
    PLAYER("category.player", Fonts.ICONS_1.get(17), "c", 0x4, new CategoryScreen()),
    RENDER("category.render", Fonts.ICONS_1.get(17), "g", 0x5, new CategoryScreen()),
    EXPLOIT("category.exploit", Fonts.ICONS_1.get(17), "a", 0x6, new CategoryScreen()),
    GHOST("category.ghost", Fonts.ICONS_1.get(17), "f", 0x7, new CategoryScreen()),
    THEME("category.themes", Fonts.ICONS_2.get(17), "U", 0xA, new ThemeScreen()),
    LANGUAGE("category.language", Fonts.ICONS_2.get(17), "U", 0xA, new LanguageScreen());

    // name of category (in case we don't use enum names)
    @Getter
    private final String name;

    // icon character
    private final String icon;

    // optional color for every specific category (module list or click gui)
    @Getter
    private final int color;

    private final Font fontRenderer;

    public final Screen clickGUIScreen;

    Category(final String name, final Font fontRenderer, final String icon, final int color, final Screen clickGUIScreen) {
        this.name = name;
        this.icon = icon;
        this.color = color;
        this.clickGUIScreen = clickGUIScreen;
        this.fontRenderer = fontRenderer;
    }
}
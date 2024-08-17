package cn.yapeteam.yolbi.module;

import cn.yapeteam.yolbi.ui.standard.screen.Screen;
import cn.yapeteam.yolbi.ui.standard.screen.impl.CategoryScreen;
import cn.yapeteam.yolbi.ui.standard.screen.impl.SearchScreen;
import lombok.Getter;

public enum Category {
    SEARCH ("Search", "U", new SearchScreen()),
    COMBAT ("Combat", "a", new CategoryScreen()),
    MOVEMENT ("Movement", "b", new CategoryScreen()),
    PLAYER ("Player", "c", new CategoryScreen()),
    RENDER ("Render", "g", new CategoryScreen()),
    MISC ("Misc", "e", new CategoryScreen()),
    GHOST("Ghost", "f", new CategoryScreen());

    @Getter
    public final Screen clickGUIScreen;

    @Getter
    private final String name;

    @Getter
    private final String icon;

    Category(String name, String icon, Screen clickGUIScreen) {
        this.clickGUIScreen = clickGUIScreen;
        this.name = name;
        this.icon = icon;
    }
}

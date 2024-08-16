package cn.yapeteam.yolbi.module;

import lombok.Getter;

public enum ModuleCategory {
    COMBAT ("Combat", "a"),
    MOVEMENT ("Movement", "b"),
    PLAYER ("Player", "c"),
    RENDER ("Render", "g"),
    MISC ("Misc", "e"),
    GHOST("Ghost", "f");


    @Getter
    private final String name;

    @Getter
    private final String icon;

    ModuleCategory(String name, String icon) {
        this.name = name;
        this.icon = icon;
    }
}

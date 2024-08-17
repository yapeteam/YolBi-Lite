package cn.yapeteam.yolbi.module.impl.render;

import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.api.Category;
import cn.yapeteam.yolbi.module.api.ModuleInfo;
import cn.yapeteam.yolbi.value.impl.ModeValue;
import cn.yapeteam.yolbi.value.impl.SubMode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ModuleInfo(name = "Interface", description = "ClickGuiSettings", category = Category.RENDER, autoEnabled = true)
public final class Interface extends Module {
    private final ModeValue modulesToShow = new ModeValue("Modules to Show", this) {{
        add(new SubMode("All"));
        add(new SubMode("Exclude render"));
        add(new SubMode("Only bound"));
        setDefault("Exclude render");
    }};
}

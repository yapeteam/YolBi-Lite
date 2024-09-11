package cn.yapeteam.yolbi.module.impl.render;

import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.api.Category;
import cn.yapeteam.yolbi.module.api.ModuleInfo;
import cn.yapeteam.yolbi.module.api.value.impl.BooleanValue;
import cn.yapeteam.yolbi.module.api.value.impl.ModeValue;
import cn.yapeteam.yolbi.module.api.value.impl.SubMode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ModuleInfo(aliases = {"module.render.interface.name"}, description = "ClickGuiSettings", category = Category.RENDER, autoEnabled = true,isblatant = true)
public final class Interface extends Module {
    public final ModeValue modulesToShow = new ModeValue("Modules to Show", this) {{
        add(new SubMode("All"));
        add(new SubMode("Exclude render"));
        add(new SubMode("Only bound"));
        setDefault("Exclude render");
    }};

    public final BooleanValue showCertain = new BooleanValue("Show certain", this, false);

    public final ModeValue certainModules = new ModeValue("Certain modules", this, showCertain::getValue) {{
        add(new SubMode("Ghost"));
        add(new SubMode("Blatant"));
        setDefault("Ghost");
    }};
}

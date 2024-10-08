package cn.yapeteam.yolbi.module.impl.movement;


import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.api.Category;
import cn.yapeteam.yolbi.module.api.ModuleInfo;
import cn.yapeteam.yolbi.module.api.value.impl.BooleanValue;

@ModuleInfo(aliases = {"module.movement.legitscaffold.name"}, description = "module.movement.legitscaffold.description", category = Category.MOVEMENT)
public class LegitScaffold extends Module {
    public final BooleanValue onlybackwards = new BooleanValue("Only Backwards", this, false);
}

package cn.yapeteam.yolbi.module.impl.ghost;


import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.api.Category;
import cn.yapeteam.yolbi.module.api.ModuleInfo;
import cn.yapeteam.yolbi.module.api.value.impl.ModeValue;
import cn.yapeteam.yolbi.module.api.value.impl.NumberValue;
import cn.yapeteam.yolbi.module.impl.combat.wtap.LegitWTap;
import cn.yapeteam.yolbi.module.impl.combat.wtap.SilentWTap;

/**
 * @author Alan
 * @since 29/01/2021
 */

@ModuleInfo(aliases = {"module.ghost.wtap.name", "Extra Knock Back", "Super Knock Back", "Knock Back", "Sprint Reset"}, description = "module.ghost.wtap.description", category = Category.GHOST)
public class WTap extends Module {
    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new LegitWTap("Legit", this))
            .add(new SilentWTap("Silent", this))
            .setDefault("Legit");

    public final NumberValue chance = new NumberValue("WTap Chance", this, 100, 0, 100, 1);
}
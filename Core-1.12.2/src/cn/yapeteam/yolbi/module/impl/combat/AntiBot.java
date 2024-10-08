package cn.yapeteam.yolbi.module.impl.combat;


import cn.yapeteam.yolbi.managers.BotManager;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.api.Category;
import cn.yapeteam.yolbi.module.api.ModuleInfo;
import cn.yapeteam.yolbi.module.api.value.impl.BooleanValue;
import cn.yapeteam.yolbi.module.impl.combat.antibot.*;

@ModuleInfo(aliases = {"module.combat.antibot.name"}, description = "module.combat.antibot.description", category = Category.COMBAT, isblatant = true)
public final class AntiBot extends Module {

    private final BooleanValue funcraftAntiBot = new BooleanValue("Funcraft Check", this, false,
            new FuncraftAntiBot("", this));

    private final BooleanValue duplicate = new BooleanValue("Duplicate Name Check", this, false,
            new DuplicateNameCheck("", this));

    private final BooleanValue ping = new BooleanValue("No Ping Check", this, false,
            new PingCheck("", this));

    private final BooleanValue negativeIDCheck = new BooleanValue("Negative Unique ID Check", this, false,
            new NegativeIDCheck("", this));

    private final BooleanValue duplicateIDCheck = new BooleanValue("Duplicate Unique ID Check", this, false,
            new DuplicateIDCheck("", this));

    private final BooleanValue middleClick = new BooleanValue("Middle Click Bot", this, false,
            new MiddleClickBot("", this));

    @Override
    public void onDisable() {
        BotManager.bots.clear();
    }
}

package cn.yapeteam.yolbi.module.impl.combat;

import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.api.Category;
import cn.yapeteam.yolbi.module.api.ModuleInfo;
import cn.yapeteam.yolbi.module.api.value.impl.BooleanValue;
import lombok.Getter;

@Getter
@ModuleInfo(aliases = {"module.combat.combatsettings.name"}, description = "module.combat.combatsettings.description", category = Category.COMBAT)
public class CombatSettings extends Module {
    private final BooleanValue players = new BooleanValue("Players", this,  true);
    private final BooleanValue notTeamMates = new BooleanValue("No Team Mates", this, true, players::getValue);
    private final BooleanValue animals = new BooleanValue("Animals", this,  true);
    private final BooleanValue mobs = new BooleanValue("Mobs", this,  true);
    private final BooleanValue villagers = new BooleanValue("Villagers", this,  true);
    // for weapons
    private final BooleanValue rod = new BooleanValue("Rod as Weapon", this,  true);
    private final BooleanValue axe = new BooleanValue("Axe as Weapon", this,  false);
    private final BooleanValue stick = new BooleanValue("Stick as Weapon", this,  false);
}

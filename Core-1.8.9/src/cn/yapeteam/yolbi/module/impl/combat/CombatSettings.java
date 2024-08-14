package cn.yapeteam.yolbi.module.impl.combat;

import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.setting.impl.ButtonSetting;

public class CombatSettings extends Module {

    private final ButtonSetting Players = new ButtonSetting("Players", true);
    private final ButtonSetting Mobs = new ButtonSetting("Mobs", true);
    private final ButtonSetting Animals = new ButtonSetting("Animals", true);
    private final ButtonSetting Invisible = new ButtonSetting("Invisible", true);
    private final ButtonSetting Friend = new ButtonSetting("Friend", true);
    private final ButtonSetting Team = new ButtonSetting("Team", true);

    public CombatSettings() {
        super("CombatSettings", category.combat);
        registerSetting(Players,Mobs,Animals,Invisible,Friend,Team);
    }
}

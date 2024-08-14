package cn.yapeteam.yolbi.managers;

import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.setting.impl.ButtonSetting;
import cn.yapeteam.yolbi.utils.IMinecraft;
import cn.yapeteam.yolbi.utils.player.PlayerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TargetManager implements IMinecraft {
    private static Module combatSettingsModule = null;
    private static Module antiBotModule = null;

    public static List<Entity> getTargets(double range) {
        if (combatSettingsModule == null)
            combatSettingsModule = YolBi.instance.getModuleManager().getModule("CombatSettings");
        if (antiBotModule == null)
            antiBotModule = YolBi.instance.getModuleManager().getModule("Antibot");
        if (mc.theWorld == null) return new ArrayList<>();

        return mc.theWorld.loadedEntityList.stream()
                .filter(entity -> entity instanceof EntityLivingBase)
                .filter(
                        entity -> (((ButtonSetting) combatSettingsModule.getSettings().get(0)).isToggled() && entity instanceof EntityPlayer &&
                                !((ButtonSetting) combatSettingsModule.getSettings().get(5)).isToggled() && PlayerUtil.sameTeam((EntityPlayer) entity)) ||
                                ((ButtonSetting) combatSettingsModule.getSettings().get(2)).isToggled() && entity instanceof EntityAnimal ||
                                ((ButtonSetting) combatSettingsModule.getSettings().get(1)).isToggled() && entity instanceof EntityMob ||
                                ((ButtonSetting) combatSettingsModule.getSettings().get(3)).isToggled() && entity instanceof EntityVillager
                )
                // not ourselves
                .filter(entity -> entity != mc.thePlayer)
                // must be in distance
                .filter(entity -> mc.thePlayer.getDistanceToEntity(entity) <= range)
                // not bots
                .filter(entity -> !(antiBotModule.isEnabled() && BotManager.bots.contains(entity)))
                // sort by distance
                .sorted(Comparator.comparingDouble(entity -> mc.thePlayer.getDistanceToEntity(entity)))
                .collect(Collectors.toList());
    }
}

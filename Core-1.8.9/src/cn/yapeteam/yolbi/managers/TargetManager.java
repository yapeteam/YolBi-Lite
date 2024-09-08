package cn.yapeteam.yolbi.managers;

import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.module.impl.combat.AntiBot;
import cn.yapeteam.yolbi.module.impl.combat.CombatSettings;
import cn.yapeteam.yolbi.utils.interfaces.Accessor;
import cn.yapeteam.yolbi.utils.player.PlayerUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TargetManager implements Accessor {
    private static CombatSettings combatSettingsModule = null;
    private static AntiBot antiBotModule = null;

    public static List<EntityLivingBase> getTargets(double range) {
        if (combatSettingsModule == null)
            combatSettingsModule = YolBi.instance.getModuleManager().get(CombatSettings.class);
        if (antiBotModule == null)
            antiBotModule = YolBi.instance.getModuleManager().get(AntiBot.class);
        if (mc.theWorld == null) return new ArrayList<>();
        return mc.theWorld.playerEntities.stream()
                .filter(entity -> entity instanceof EntityLivingBase)
                .filter(
                        entity -> (combatSettingsModule.getPlayers().getValue() && entity instanceof EntityPlayer &&
                                !(combatSettingsModule.getNotTeamMates().getValue() && PlayerUtil.sameTeam((EntityPlayer) entity)))
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

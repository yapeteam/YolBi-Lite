package cn.yapeteam.yolbi.module.impl.combat.antibot;


import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.player.EventMotion;
import cn.yapeteam.yolbi.managers.BotManager;
import cn.yapeteam.yolbi.module.api.value.Mode;
import cn.yapeteam.yolbi.module.impl.combat.AntiBot;
import net.minecraft.client.network.NetworkPlayerInfo;

import java.util.Objects;

public final class PingCheck extends Mode<AntiBot> {

    public PingCheck(String name, AntiBot parent) {
        super(name, parent);
    }

    @Listener
    public void onUpdate(EventMotion event){
        mc.world.playerEntities.forEach(player -> {
            final NetworkPlayerInfo info = Objects.requireNonNull(mc.getConnection()).getPlayerInfo(player.getUniqueID());

            if (info.getResponseTime() <= 0) {
                BotManager.addBot(player);
            }
        });
    };
}
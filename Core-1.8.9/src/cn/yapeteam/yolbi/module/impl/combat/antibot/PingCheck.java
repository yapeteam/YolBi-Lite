package cn.yapeteam.yolbi.module.impl.combat.antibot;


import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.player.EventMotion;
import cn.yapeteam.yolbi.managers.BotManager;
import cn.yapeteam.yolbi.module.api.value.Mode;
import cn.yapeteam.yolbi.module.impl.combat.AntiBot;
import net.minecraft.client.network.NetworkPlayerInfo;

public final class PingCheck extends Mode<AntiBot> {

    public PingCheck(String name, AntiBot parent) {
        super(name, parent);
    }

    @Listener
    public void onUpdate(EventMotion event){
        mc.theWorld.playerEntities.forEach(player -> {
            final NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfo(player.getUniqueID());

            if (info != null && info.getResponseTime() <= 0) {
                BotManager.addBot(player);
            }
        });
    };
}
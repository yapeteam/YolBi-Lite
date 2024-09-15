package cn.yapeteam.yolbi.module.impl.combat.antibot;


import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.player.EventMotion;
import cn.yapeteam.yolbi.managers.BotManager;
import cn.yapeteam.yolbi.module.api.value.Mode;
import cn.yapeteam.yolbi.module.impl.combat.AntiBot;

public final class NegativeIDCheck extends Mode<AntiBot> {

    public NegativeIDCheck(String name, AntiBot parent) {
        super(name, parent);
    }

    @Listener
    public void onUpdate(EventMotion event){
        mc.theWorld.playerEntities.forEach(player -> {
            if (player.getEntityId() < 0) {
                BotManager.addBot(player);
            }
        });
    };
}
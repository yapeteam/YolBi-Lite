package cn.yapeteam.yolbi.module.impl.combat.antibot;


import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.player.EventMotion;
import cn.yapeteam.yolbi.managers.BotManager;
import cn.yapeteam.yolbi.module.api.value.Mode;
import cn.yapeteam.yolbi.module.impl.combat.AntiBot;

public final class DuplicateIDCheck extends Mode<AntiBot> {

    public DuplicateIDCheck(String name, AntiBot parent) {
        super(name, parent);
    }

    @Listener
    public void onUpdate(EventMotion event){
        mc.world.playerEntities.forEach(player -> {
            if (mc.world.playerEntities.stream().anyMatch(player2 -> player2.getEntityId() == player.getEntityId() && player2 != player)) {
                BotManager.removeBot(player);
            }
        });
    };
}
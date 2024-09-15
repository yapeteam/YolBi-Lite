package cn.yapeteam.yolbi.module.impl.combat.antibot;


import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.player.EventUpdate;
import cn.yapeteam.yolbi.managers.BotManager;
import cn.yapeteam.yolbi.module.api.value.Mode;
import cn.yapeteam.yolbi.module.impl.combat.AntiBot;

public final class TicksVisibleCheck extends Mode<AntiBot> {

    public TicksVisibleCheck(String name, AntiBot parent) {
        super(name, parent);
    }

    @Listener
    public void onUpdate(EventUpdate event){
        mc.theWorld.playerEntities.forEach(player -> {
            if (player.ticksVisible < 160) {
                BotManager.addBot(player);
            } else if (player.ticksExisted == 160) {
                BotManager.removeBot(player);
            }
        });
    };
}
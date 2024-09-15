package cn.yapeteam.yolbi.module.impl.combat.antibot;


import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.player.EventMotion;
import cn.yapeteam.yolbi.managers.BotManager;
import cn.yapeteam.yolbi.module.api.value.Mode;
import cn.yapeteam.yolbi.module.impl.combat.AntiBot;

/**
 * @author Wykt
 * @since 2/04/2023
 */

public final class FuncraftAntiBot extends Mode<AntiBot> {
    public FuncraftAntiBot(String name, AntiBot parent) {
        super(name, parent);
    }

    @Listener
    public void onUpdate(EventMotion event){
        mc.theWorld.playerEntities.forEach(player -> {
            if (player.getDisplayName().getUnformattedText().contains("§")) {
                BotManager.removeBot(player);
                return;
            }

            BotManager.addBot(player);;
        });
    };
}
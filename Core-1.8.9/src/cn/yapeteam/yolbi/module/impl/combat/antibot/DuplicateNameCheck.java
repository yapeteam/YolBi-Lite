package cn.yapeteam.yolbi.module.impl.combat.antibot;


import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.player.EventMotion;
import cn.yapeteam.yolbi.managers.BotManager;
import cn.yapeteam.yolbi.module.api.value.Mode;
import cn.yapeteam.yolbi.module.impl.combat.AntiBot;

public final class DuplicateNameCheck extends Mode<AntiBot> {

    public DuplicateNameCheck(String name, AntiBot parent) {
        super(name, parent);
    }

    @Listener
    public void onUpdate(EventMotion event){
        mc.theWorld.playerEntities.forEach(player -> {
            String name = player.getDisplayName().getUnformattedText();

            if (mc.theWorld.playerEntities.stream().anyMatch(player2 -> name.equals(player2.getDisplayName().getUnformattedText()))) {
                BotManager.addBot(player);
            }
        });
    };
}
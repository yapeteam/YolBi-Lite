package cn.yapeteam.yolbi.module.impl.combat.antibot;


import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.player.EventMotion;
import cn.yapeteam.yolbi.managers.BotManager;
import cn.yapeteam.yolbi.module.api.value.Mode;
import cn.yapeteam.yolbi.module.impl.combat.AntiBot;
import net.minecraft.entity.Entity;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public final class MiddleClickBot extends Mode<AntiBot> {

    private boolean down;

    public MiddleClickBot(String name, AntiBot parent) {
        super(name, parent);
    }

    @Listener
    public void onUpdate(EventMotion event){
        if (Mouse.isButtonDown(2) || (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && mc.gameSettings.keyBindAttack.isKeyDown())) {
            if (down) return;
            down = true;

            if (mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                Entity entity = mc.objectMouseOver.entityHit;

                if (BotManager.bots.contains(entity)) {
                    BotManager.removeBot(entity);
                } else {
                    BotManager.addBot(entity);
                }
            }
        } else down = false;
    };
}
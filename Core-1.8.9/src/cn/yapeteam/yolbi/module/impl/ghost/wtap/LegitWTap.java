package cn.yapeteam.yolbi.module.impl.ghost.wtap;


import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.player.EventAttack;
import cn.yapeteam.yolbi.event.impl.player.EventMotion;
import cn.yapeteam.yolbi.managers.ReflectionManager;
import cn.yapeteam.yolbi.module.api.value.Mode;
import cn.yapeteam.yolbi.module.impl.ghost.WTap;

public class LegitWTap extends Mode<WTap> {

    private boolean unsprint, wTap;

    public LegitWTap(String name, WTap parent) {
        super(name, parent);
    }

    @Listener
    public void onAttack(EventAttack event){
        wTap = Math.random() * 100 < getParent().chance.getValue().doubleValue() && event.getTargetEntity().hurtTime >= 6;

        if (!wTap || unsprint) return;

        if (mc.thePlayer.isSprinting() || mc.gameSettings.keyBindSprint.isKeyDown()) {
            ReflectionManager.SetPressed(mc.gameSettings.keyBindSprint, true);
            unsprint = true;
        }
    };

    @Listener
    public void onMotion(EventMotion eventMotion){
        if (!wTap) return;

        if (unsprint && Math.random() * 100 < getParent().chance.getValue().doubleValue()) {
            ReflectionManager.SetPressed(mc.gameSettings.keyBindSprint, false);
            unsprint = false;
        }
    };
}
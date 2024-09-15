package cn.yapeteam.yolbi.module.impl.combat.velocity;


import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.player.EventMotion;
import cn.yapeteam.yolbi.module.api.value.Mode;
import cn.yapeteam.yolbi.module.impl.combat.Velocity;

public final class AACVelocity extends Mode<Velocity> {

    private boolean jump;

    public AACVelocity(String name, Velocity parent) {
        super(name, parent);
    }

    @Listener
    public void EventPreMotion(EventMotion event){
        if (getParent().onSwing.getValue() && !mc.thePlayer.isSwingInProgress) return;

        if (mc.thePlayer.hurtTime > 0 && !BadPacketsComponent.bad(false, true, false, false, false)) {
            mc.thePlayer.motionX *= 0.6D;
            mc.thePlayer.motionZ *= 0.6D;
        }

        jump = false;
    };

    @EventLink
    public final Listener<MoveInputEvent> onMove = event -> {
        if (getParent().onSwing.getValue() && !mc.thePlayer.isSwingInProgress) return;

        if (jump) {
            event.setJump(true);
        }
    };
}

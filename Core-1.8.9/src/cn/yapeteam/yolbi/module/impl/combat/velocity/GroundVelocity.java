package cn.yapeteam.yolbi.module.impl.combat.velocity;


import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.player.EventMotion;
import cn.yapeteam.yolbi.module.api.value.Mode;
import cn.yapeteam.yolbi.module.api.value.impl.NumberValue;
import cn.yapeteam.yolbi.module.impl.combat.Velocity;

public final class GroundVelocity extends Mode<Velocity> {

    private final NumberValue delay = new NumberValue("Delay", this, 1, 0, 20, 1);

    private int ticks;

    public GroundVelocity(String name, Velocity parent) {
        super(name, parent);
    }

    @Listener
    public void EventPreMotion(EventMotion event){
        if (getParent().onSwing.getValue() && !mc.thePlayer.isSwingInProgress) return;

        if (mc.thePlayer.ticksSinceVelocity == delay.getValue().intValue()) {
            mc.thePlayer.onGround = true;
        }
    };

    @EventLink
    public final Listener<MoveInputEvent> onMove = event -> {
        if (mc.thePlayer.ticksSinceVelocity == delay.getValue().intValue() + 1) {
            event.setJump(false);
        }
    };
}

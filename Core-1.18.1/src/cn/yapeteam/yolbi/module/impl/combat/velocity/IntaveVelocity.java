package cn.yapeteam.yolbi.module.impl.combat.velocity;


import cn.yapeteam.yolbi.module.api.value.Mode;
import cn.yapeteam.yolbi.module.impl.combat.Velocity;

public final class IntaveVelocity extends Mode<Velocity> {

    private boolean attacked, slowDown;

    public IntaveVelocity(String name, Velocity parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        if (getParent().onSwing.getValue() && !mc.thePlayer.isSwingInProgress) return;

        if (attacked && !slowDown && mc.thePlayer.isSprinting()) {
            mc.thePlayer.motionX *= 0.6D;
            mc.thePlayer.motionZ *= 0.6D;
            mc.thePlayer.setSprinting(false);
        }

        attacked = false;
        slowDown = false;
    };

    @EventLink
    public final Listener<HitSlowDownEvent> onHitSlowDown = event -> {
        slowDown = true;
    };

    @EventLink
    public final Listener<AttackEvent> onAttack = event -> {
        attacked = true;
    };
}

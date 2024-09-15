package cn.yapeteam.yolbi.module.impl.combat.velocity;


import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.player.EventMotion;
import cn.yapeteam.yolbi.module.api.value.Mode;
import cn.yapeteam.yolbi.module.api.value.impl.NumberValue;
import cn.yapeteam.yolbi.module.impl.combat.Velocity;
import cn.yapeteam.yolbi.utils.player.MoveUtil;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S12PacketEntityVelocity;

public final class LegitVelocity extends Mode<Velocity> {
    public final NumberValue chance = new NumberValue("Chance", this, 100, 0, 100, 1);
    public final BooleanValue legitTiming = new BooleanValue("Legit Timing", this, false);
    private boolean jump;

    public LegitVelocity(String name, Velocity parent) {
        super(name, parent);
    }

    @Listener
    public void EventPreMotion(EventMotion event){
        jump = false;
    };

    @EventLink
    public final Listener<MoveInputEvent> onMove = event -> {
        if (getParent().onSwing.getValue() && !mc.thePlayer.isSwingInProgress) return;

        if (jump && MoveUtil.isMoving() && Math.random() * 100 < chance.getValue().doubleValue()) {
            event.setJump(true);
        }
    };

    @EventLink
    public final Listener<PacketReceiveEvent> onPacketReceiveEvent = event -> {
        if (getParent().onSwing.getValue() && !mc.thePlayer.isSwingInProgress || event.isCancelled()) return;

        if (!mc.thePlayer.onGround) {
            return;
        }

        final Packet<?> p = event.getPacket();

        if (p instanceof S12PacketEntityVelocity) {
            final S12PacketEntityVelocity wrapper = (S12PacketEntityVelocity) p;

            if (wrapper.getEntityID() == mc.thePlayer.getEntityId() && wrapper.motionY > 0 && (!legitTiming.getValue() || mc.thePlayer.ticksSinceVelocity <= 14 || mc.thePlayer.onGroundTicks <= 1)) {
                jump = true;
            }
        }
    };
}

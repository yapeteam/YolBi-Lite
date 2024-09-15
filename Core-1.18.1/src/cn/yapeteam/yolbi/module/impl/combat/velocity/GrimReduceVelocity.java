package cn.yapeteam.yolbi.module.impl.combat.velocity;


import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.player.EventUpdate;
import cn.yapeteam.yolbi.module.api.value.Mode;
import cn.yapeteam.yolbi.module.impl.combat.Velocity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.C0APacketAnimation;

import java.util.List;

public final class GrimReduceVelocity extends Mode<Velocity> {

    public GrimReduceVelocity(String name, Velocity parent) {
        super(name, parent);
    }

    @Listener
    public void EventPreMotion(EventUpdate){
        if (getParent().onSwing.getValue() && !mc.thePlayer.isSwingInProgress ||
                mc.thePlayer.ticksExisted <= 20) return;

        List<EntityLivingBase> targets = TargetComponent.getTargets(7);

        if (targets.isEmpty()) return;

        if (mc.thePlayer.ticksSinceVelocity <= 14 && !BadPacketsComponent.bad()) {
            PacketUtil.send(new C0APacketAnimation());
            mc.playerController.attackEntity(mc.thePlayer, targets.get(0));
        }
    };
}

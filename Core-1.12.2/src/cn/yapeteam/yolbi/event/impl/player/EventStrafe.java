package cn.yapeteam.yolbi.event.impl.player;

import cn.yapeteam.yolbi.event.type.CancellableEvent;
import cn.yapeteam.yolbi.utils.IMinecraft;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public final class EventStrafe extends CancellableEvent implements IMinecraft {
    private float forward;
    private float strafe;
    private float friction;
    private float yaw;

    public void setSpeed(final double speed, final double motionMultiplier) {
        setFriction((float) (getForward() != 0 && getStrafe() != 0 ? speed * 0.98F : speed));
        mc.player.motionX *= motionMultiplier;
        mc.player.motionZ *= motionMultiplier;
    }

    public void slow(double slowIn) {
        this.forward *= (float) slowIn;
        this.strafe *= (float) slowIn;
    }
}

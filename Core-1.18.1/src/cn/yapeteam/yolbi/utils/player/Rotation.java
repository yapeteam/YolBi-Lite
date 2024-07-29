package cn.yapeteam.yolbi.utils.player;

import cn.yapeteam.yolbi.utils.IMinecraft;
import cn.yapeteam.yolbi.utils.vector.Vector2f;
import lombok.Getter;

public class Rotation implements IMinecraft {
    @Getter
    public float yaw, pitch;
    public boolean tag = false;

    public static float wrapAngleTo180(float value) {
        value = value % 360.0F;

        if (value >= 180.0F) {
            value -= 360.0F;
        }

        if (value < -180.0F) {
            value += 360.0F;
        }

        return value;
    }

    public Rotation(float yawIn, float pitchIn) {
        this.yaw = yawIn > 180 || yawIn < 180 ? wrapAngleTo180(yawIn) : yawIn;
        this.pitch = pitchIn > 90 || pitchIn < -90 ? wrapAngleTo180(pitchIn) : pitchIn;
    }

    @Override
    public String toString() {
        return "pitch:" + pitch + " yaw:" + yaw;
    }

    public Rotation(Vector2f v) {
        this(v.y, v.x);
    }


    public Vector2f toVec2f() {
        return new Vector2f(pitch, yaw);
    }


    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) || (obj instanceof Rotation && ((Rotation) obj).toVec2f().equals(this.toVec2f())) || (obj instanceof Vector2f && obj.equals(this.toVec2f()));
    }

    public static Rotation player() {
        assert mc.player != null;
        return new Rotation(mc.player.getYHeadRot(), mc.player.getXRot());
    }
}

package cn.yapeteam.yolbi.module.impl.movement;

import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.player.EventUpdate;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.Category;
import cn.yapeteam.yolbi.module.impl.misc.Disabler;
import cn.yapeteam.yolbi.utils.Utils;
import cn.yapeteam.yolbi.utils.player.MoveUtil;
import org.jetbrains.annotations.NotNull;

public class Speed extends Module {
    private int offGroundTicks = 0;

    public Speed() {
        super("Speed", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        offGroundTicks = 0;
    }

    @Listener
    public void onUpdate(@NotNull EventUpdate event) {
        if (!event.isPre()) return;

        if (mc.thePlayer.onGround)
            offGroundTicks = 0;
        else
            offGroundTicks++;

        if (!MoveUtil.isMoving() || !Disabler.isDisabled()) return;
        switch (offGroundTicks) {
            case 0:
                if (!Utils.jumpDown()) {
                    mc.thePlayer.jump();
                    MoveUtil.strafe(0.485);
                }
                break;
            case 5:
                mc.thePlayer.motionY = MoveUtil.predictedMotion(mc.thePlayer.motionY, 2);
                break;
        }
    }
}

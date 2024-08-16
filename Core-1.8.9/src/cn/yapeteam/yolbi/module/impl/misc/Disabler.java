package cn.yapeteam.yolbi.module.impl.misc;

import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.network.EventPacketReceive;
import cn.yapeteam.yolbi.event.impl.player.EventMotion;
import cn.yapeteam.yolbi.event.impl.player.EventUpdate;
import cn.yapeteam.yolbi.event.impl.render.EventRender2D;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.ModuleCategory;
import cn.yapeteam.yolbi.notification.Notification;
import cn.yapeteam.yolbi.notification.NotificationType;
import cn.yapeteam.yolbi.utils.animation.Easing;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Disabler extends Module {
    private Integer op;
    private boolean oq;
    private boolean fk;
    private int offGroundTicks = 0;
    private @Nullable WorldClient lastWorld = null;
    private static boolean isFinished = false;

    public Disabler() {
        super("Disabler", ModuleCategory.MISC);
    }

    public static boolean isDisabled() {
        return isFinished;
    }

    @Listener
    public void onPreMotion(EventMotion event) {
        if (this.fk && mc.thePlayer.onGround) {
            mc.thePlayer.jump();
            this.fk = false;
            this.oq = true;
        } else if (offGroundTicks >= 9 && this.oq) {
            if (offGroundTicks % 2 == 0) {
                event.setX(event.getX() + 0.095);
            }

            mc.thePlayer.motionX = mc.thePlayer.motionY = mc.thePlayer.motionZ = 0.0;
        }
    }

    @Listener
    public void onReceivePacket(@NotNull EventPacketReceive event) {
        if (event.getPacket() instanceof S08PacketPlayerPosLook) {
            this.op = this.op + 1;
            if (this.op == 20) {
                this.oq = false;
                this.op = 0;
                YolBi.instance.getNotificationManager().post(new Notification(
                        "Automatically disabled Watchdog Jump check",
                        Easing.EASE_OUT_BACK, Easing.EASE_IN_OUT_CUBIC,
                        1000, NotificationType.INIT
                ));
                isFinished = true;
            }
        }
    }

    @Listener
    public void onRenderTick(EventRender2D event) {
        if (lastWorld != mc.theWorld) {
            onWorldChange();
            lastWorld = mc.theWorld;
        }
    }

    public void onWorldChange() {
        isFinished = false;
        YolBi.instance.getNotificationManager().post(new Notification(
                "Disabling Jump check for Watchdog",
                Easing.EASE_OUT_BACK, Easing.EASE_IN_OUT_CUBIC,
                4000, NotificationType.SUCCESS
        ));
        this.fk = true;
        this.oq = false;
        this.op = 0;
    }

    @Listener
    public void onUpdate(EventUpdate event) {
        if (mc.thePlayer.onGround)
            offGroundTicks = 0;
        else
            offGroundTicks++;
    }

    @Override
    public void onDisable() {
        isFinished = false;
    }

    @Override
    public void onEnable() {
        onWorldChange();
    }
}

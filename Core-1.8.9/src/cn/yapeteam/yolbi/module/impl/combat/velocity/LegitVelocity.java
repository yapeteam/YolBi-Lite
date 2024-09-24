package cn.yapeteam.yolbi.module.impl.combat.velocity;


import cn.yapeteam.loader.Natives;
import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.network.EventPacket;
import cn.yapeteam.yolbi.module.api.value.Mode;
import cn.yapeteam.yolbi.module.api.value.impl.NumberValue;
import cn.yapeteam.yolbi.module.impl.combat.Velocity;
import cn.yapeteam.yolbi.utils.math.MathUtils;
import cn.yapeteam.yolbi.utils.player.misc.VirtualKeyBoard;
import net.minecraft.network.play.server.S12PacketEntityVelocity;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class LegitVelocity extends Mode<Velocity> {
    //probability for having a perfect jump
    private final NumberValue probability = new NumberValue("Probability", this,  100.0, 0.0, 100.0, 1.0);

    //delay for jumping after the perfect jump
    private final NumberValue maxJumpDelay = new NumberValue("Max Jump Delay", this,  0.0, 0.0, 1000.0, 10.0);

    private final NumberValue minJumpDelay = new NumberValue("Min Jump Delay", this,  0.0, 0.0, 1000.0, 10.0);

    // how long you hold the space bar
    private final NumberValue maxJumpHold = new NumberValue("Max Jump Hold", this,  400.0, 0.0, 1000.0, 10.0);

    private final NumberValue minJumpHold = new NumberValue("Min Jump Hold", this,  400.0, 0.0, 1000.0, 10.0);


    public LegitVelocity(String name, Velocity parent) {
        super(name, parent);
    }

    public void jumpreset() {
        Natives.SetKeyBoard(VirtualKeyBoard.VK_SPACE, true);

        // Create a ScheduledExecutorService
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

        // Generate a random delay in milliseconds
        int delay = (int) MathUtils.getRandom(minJumpHold.getValue().doubleValue(), maxJumpHold.getValue().doubleValue()); // replace 1000 with the maximum delay you want

        // Schedule a task to set the jump keybind to false after the delay
        executorService.schedule(() -> Natives.SetKeyBoard(VirtualKeyBoard.VK_SPACE, false), delay, TimeUnit.MILLISECONDS);

        // Shut down the executor service
        executorService.shutdown();
    }

    @Listener
    public void onPacket(EventPacket event) {
        if (mc.currentScreen != null) return;
        if (event.getPacket() instanceof S12PacketEntityVelocity && ((S12PacketEntityVelocity) event.getPacket()).getEntityID() == mc.thePlayer.getEntityId()) {
            if ((new Random((long) (Math.random() * 114514)).nextInt(101) <= probability.getValue().doubleValue())) {
                jumpreset();
            } else {
                // this means to delay the jump
                ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
                int delay = (int) MathUtils.getRandom(minJumpDelay.getValue().doubleValue(), maxJumpDelay.getValue().doubleValue());
                executorService.schedule(this::jumpreset, delay, TimeUnit.MILLISECONDS);
                executorService.shutdown();
            }
        }
    }
}

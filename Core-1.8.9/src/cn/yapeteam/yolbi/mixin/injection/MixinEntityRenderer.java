package cn.yapeteam.yolbi.mixin.injection;

import cn.yapeteam.ymixin.annotations.*;
import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.event.impl.player.EventMouseOver;
import cn.yapeteam.yolbi.event.impl.render.EventRender2D;
import cn.yapeteam.yolbi.event.impl.render.EventRender3D;
import cn.yapeteam.yolbi.event.impl.render.EventRenderGUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {
    @Shadow
    private Minecraft mc;
    @Shadow
    private Entity pointedEntity;

    @Shadow
    private double d0;

    @Inject(
            method = "renderWorldPass", desc = "(IFJ)V",
            target = @Target(
                    value = "INVOKESTATIC",
                    target = "net/minecraft/client/renderer/GlStateManager.disableFog()V",
                    shift = Target.Shift.AFTER
            )
    )
    private void render(@Local(source = "partialTicks", index = 2) float partialTicks) {
        YolBi.instance.getEventManager().post(new EventRender3D(partialTicks));
    }

    @Inject(
            method = "getMouseOver", desc = "(F)V",
            target = @Target(
                value = "INVOKE",
                target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;getBlockReachDistance()D",
                shift = Target.Shift.AFTER
            )
    )
    private void getMouseOver(@Local(source = "partialTicks", index = 1) float partialTicks) {
        EventMouseOver event = new EventMouseOver(this.mc.playerController.getBlockReachDistance());
        YolBi.instance.getEventManager().post(event);

        // Modify the reach distance based on the event's result
        double newReach = event.getReach();
        if (newReach > this.mc.playerController.getBlockReachDistance()) {
            d0 = newReach;
        }
    }


    @Inject(method = "updateCameraAndRender", desc = "(FJ)V",
            target = @Target(
                    value = "INVOKESTATIC",
                    target = "net/minecraft/client/renderer/GlStateManager.alphaFunc(IF)V",
                    shift = Target.Shift.BEFORE
            ))
    private void onRender2D(
            @Local(source = "sr", index = 5) ScaledResolution sr,
            @Local(source = "partialTicks", index = 1) float partialTicks
    ) {
        YolBi.instance.getEventManager().post(new EventRender2D(partialTicks, sr));
    }

    @Inject(
            method = "updateCameraAndRender", desc = "(FJ)V",
            target = @Target(
                    value = "INVOKEVIRTUAL",
                    target = "net/minecraft/client/gui/GuiScreen.drawScreen(IIF)V",
                    shift = Target.Shift.BEFORE
            )
    )
    private void onRenderGUI() {
        YolBi.instance.getEventManager().post(new EventRenderGUI());
    }
}

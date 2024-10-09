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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {
    @Shadow
    private Minecraft mc;
    @Shadow
    private Entity pointedEntity;

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
            method = "getMouseOver",
            desc = "(F)V",
            target = @Target(
                    value = "PUTFIELD",
                    target = "net/minecraft/client/renderer/EntityRenderer.pointedEntity Lnet/minecraft/entity/Entity;",
                    shift = Target.Shift.BEFORE,
                    ordinal = 4
            )
    )
    private void modifyReach(@Local(source = "vec33", index = 12) Vec3d vec33) {
        EventMouseOver event = new EventMouseOver(3.0f);
        YolBi.instance.getEventManager().post(event);
        if (!event.isCancelled()) {
            // pointedEntity = RayCastUtil.rayCast(new Vector2f(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch), event.getReach()).entityHit;
            if (this.pointedEntity != null) {
                this.mc.objectMouseOver = new RayTraceResult(this.pointedEntity, vec33);
                if (this.pointedEntity instanceof EntityLivingBase || this.pointedEntity instanceof EntityItemFrame) {
                    this.mc.pointedEntity = this.pointedEntity;
                }
            }

            this.mc.mcProfiler.endSection();
            return;
        }
    }

    @Inject(
            method = "getMouseOver",
            desc = "(F)V",
            target = @Target(
                    value = "ISTORE",
                    shift = Target.Shift.AFTER
            )
    )
    private void getMouseOver(@Local(source = "partialTicks", index = 1) float partialTicks) {
        EventMouseOver event = new EventMouseOver(3.0f);
        YolBi.instance.getEventManager().post(event);
    }

    @Inject(
            method = "updateCameraAndRender",
            desc = "(FJ)V",
            target = @Target(
                    value = "INVOKEVIRTUAL",
                    target = "net/minecraft/client/gui/GuiIngame.renderGameOverlay(F)V",
                    shift = Target.Shift.AFTER
            )
    )
    private void onRender2D(@Local(source = "partialTicks", index = 1) float partialTicks, @Local(source = "sr", index = 5) ScaledResolution sr) {
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

package cn.yapeteam.yolbi.mixin.injection;

import cn.yapeteam.ymixin.annotations.*;
import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.event.impl.player.EventMouseOver;
import cn.yapeteam.yolbi.event.impl.render.EventRender2D;
import cn.yapeteam.yolbi.event.impl.render.EventRender3D;
import cn.yapeteam.yolbi.event.impl.render.EventRenderGUI;
import cn.yapeteam.yolbi.utils.render.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {
    @Shadow
    private Minecraft mc;
    @Shadow
    private Entity pointedEntity;

    @Shadow
    private Vec3 vec3;

    @Shadow
    private boolean flag;

    @Shadow
    private Vec3 vec33;

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
                    value = "INVOKEVIRTUAL",
                    target = "net/minecraft/util/Vec3.distanceTo(Lnet/minecraft/util/Vec3;)D",
                    shift = Target.Shift.BEFORE,
                    ordinal = 2
            )
    )
    private void modifyreach(@Local(source = "partialTicks", index = 1) float partialTicks) {
        EventMouseOver event = new EventMouseOver(3.0f);
        YolBi.instance.getEventManager().post(event);
        if (this.pointedEntity != null && flag && vec3.distanceTo(vec33) < event.getReach()) {
//            so we remove check using original minecraft logic
            flag = false;
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
        GlStateManager.pushMatrix();
        YolBi.instance.getEventManager().post(new EventRender2D(partialTicks, sr));
        GlStateManager.enableBlend();
        // Cross-hair
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(Gui.icons);
        GlStateManager.enableBlend();
        if (GuiUtil.showCrosshair()) {
            GlStateManager.tryBlendFuncSeparate(775, 769, 1, 0);
            GlStateManager.enableAlpha();
            mc.ingameGUI.drawTexturedModalRect(sr.getScaledWidth() / 2f - 7,
                    sr.getScaledHeight() / 2f - 7, 0, 0, 16, 16);
        }
        GlStateManager.popMatrix();
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

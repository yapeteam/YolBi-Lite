package cn.yapeteam.yolbi.mixin.injection;

import cn.yapeteam.ymixin.annotations.*;
import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.event.impl.player.EventMouseOver;
import cn.yapeteam.yolbi.event.impl.render.EventRender2D;
import cn.yapeteam.yolbi.event.impl.render.EventRender3D;
import cn.yapeteam.yolbi.event.impl.render.EventRenderGUI;
import cn.yapeteam.yolbi.managers.RenderManager;
import cn.yapeteam.yolbi.utils.misc.ObjectStore;
import cn.yapeteam.yolbi.utils.render.shader.ShaderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;

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
                    value = "ISTORE",
                    shift = Target.Shift.AFTER
            )
    )
    private void getMouseOver(@Local(source = "partialTicks", index = 1) float partialTicks) {
        EventMouseOver event = new EventMouseOver(3.0f);
        YolBi.instance.getEventManager().post(event);
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
        GlStateManager.popMatrix();
        mc.getFramebuffer().bindFramebuffer(false);
        Framebuffer framebuffer = (Framebuffer) ObjectStore.objects.get("framebuffer");
        if (framebuffer == null) {
            framebuffer = new Framebuffer(1, 1, false);
            ObjectStore.objects.put("framebuffer", framebuffer);
        }
        framebuffer = RenderManager.createFrameBuffer(framebuffer);
        framebuffer.bindFramebufferTexture();
        ShaderUtil.drawQuads(sr);
        GlStateManager.bindTexture(0);
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


    //@Modify(method = "getMouseOver", desc = "(F)V", replacepath = "cn/yapeteam/yolbi/event/impl/player/EventMouseOver", replacementfunc = "getReach", funcdesc = "()F")
    //private void modifygetMouseOver(@Local(source = "partialTicks", index = 1) float partialTicks) {
    //}
}

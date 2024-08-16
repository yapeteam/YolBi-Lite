package cn.yapeteam.yolbi.utils.shader.impl;

import RazerOfficial.Razer.gg.Razer;
import RazerOfficial.Razer.gg.event.impl.render.AlphaEvent;
import RazerOfficial.Razer.gg.util.shader.base.RiseShader;
import RazerOfficial.Razer.gg.util.shader.base.RiseShaderProgram;
import RazerOfficial.Razer.gg.util.shader.base.ShaderRenderType;
import RazerOfficial.Razer.gg.util.shader.base.ShaderUniforms;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class AlphaShader extends RiseShader {

    private final RiseShaderProgram alphaProgram = new RiseShaderProgram("alpha.frag", "vertex.vsh");
    private Framebuffer inputFramebuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);

    private float alpha;

    @Override
    public void run(final ShaderRenderType type, final float partialTicks, List<Runnable> runnable) {
        // Prevent rendering
        if (!Display.isVisible()) {
            return;
        }

        if (type == ShaderRenderType.OVERLAY) {
            this.update();
            this.setActive(mc.currentScreen != null || !mc.timeScreen.finished(500));

            if (this.isActive()) {
                this.inputFramebuffer.bindFramebuffer(true);
                Razer.INSTANCE.getEventBus().handle(new AlphaEvent());

                mc.getFramebuffer().bindFramebuffer(true);
                final int programId = this.alphaProgram.getProgramId();
                this.alphaProgram.start();

                ShaderUniforms.uniform1i(programId, "u_diffuse_sampler", 0);
                ShaderUniforms.uniform1f(programId, "u_alpha", alpha);

                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
                this.inputFramebuffer.bindFramebufferTexture();
                RiseShaderProgram.drawQuad();
                GlStateManager.disableBlend();

                RiseShaderProgram.stop();
            }
        }
    }

    @Override
    public void update() {
        if (mc.displayWidth != inputFramebuffer.framebufferWidth || mc.displayHeight != inputFramebuffer.framebufferHeight) {
            inputFramebuffer.deleteFramebuffer();
            inputFramebuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
        } else {
            inputFramebuffer.framebufferClear();
        }
        inputFramebuffer.setFramebufferColor(0.0F, 0.0F, 0.0F, 0.F);
    }

    public void setAlpha(final float alpha) {
        this.alpha = alpha;
    }
}

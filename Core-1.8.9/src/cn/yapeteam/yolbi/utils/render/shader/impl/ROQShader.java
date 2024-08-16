package cn.yapeteam.yolbi.utils.render.shader.impl;

import cn.yapeteam.yolbi.utils.render.shader.base.RiseShaderProgram;
import cn.yapeteam.yolbi.utils.render.shader.base.ShaderUniforms;
import net.minecraft.client.renderer.GlStateManager;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import java.awt.*;

// Rounded Outline Quad
public class ROQShader {

    private final RiseShaderProgram program = new RiseShaderProgram("roq.glsl", "vertex.vsh");

    public void draw(float x, float y, float width, float height, float radius, float borderSize, @NotNull Color color) {
        int programId = this.program.getProgramId();
        this.program.start();
        ShaderUniforms.uniform2f(programId, "u_size", width, height);
        ShaderUniforms.uniform1f(programId, "u_radius", radius);
        ShaderUniforms.uniform1f(programId, "u_border_size", borderSize);
        ShaderUniforms.uniform4f(programId, "u_color", color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RiseShaderProgram.drawQuad(x, y, width, height);
        GlStateManager.disableBlend();
        RiseShaderProgram.stop();
    }

    public void draw(double x, double y, double width, double height, double radius, double borderSize, Color color) {
        draw((float) x, (float) y, (float) width, (float) height, (float) radius, (float) borderSize, color);
    }
}

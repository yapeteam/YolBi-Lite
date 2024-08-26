package cn.yapeteam.yolbi.ui.gui;

import cn.yapeteam.yolbi.utils.interfaces.Accessor;
import cn.yapeteam.yolbi.utils.render.shader.ShaderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;

public class GuiIngameCache implements Accessor {
    private static final Minecraft MC = Minecraft.getMinecraft();
    private static Framebuffer framebuffer;

    // TODO: SET DIRTY TO TRUE IN END OF RUN TICK
    public static boolean dirty;

    private static ScaledResolution scaledResolution = new ScaledResolution(mc);

    public static void renderGameOverlay(float partialTicks) {
        if (scaledResolution.getScaledWidth() != mc.displayWidth || scaledResolution.getScaledHeight() != mc.displayHeight) {
            scaledResolution = new ScaledResolution(mc);
        }
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        ShaderUtil.drawQuads(scaledResolution);
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private static final Tessellator TESSELLATOR = Tessellator.getInstance();
    private static final WorldRenderer WORLD_RENDERER = TESSELLATOR.getWorldRenderer();

    public static void renderCrosshair(int x, int y) {
        MC.getTextureManager().bindTexture(Gui.icons);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(775, 769, 1, 0);
        GlStateManager.enableAlpha();
        WORLD_RENDERER.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        WORLD_RENDERER.pos(x, y + 16, 100).tex(0, 16 * 0.00390625F).endVertex();
        WORLD_RENDERER.pos(x + 16, y + 16, 100).tex(16 * 0.00390625F, 16 * 0.00390625F).endVertex();
        WORLD_RENDERER.pos(x + 16, y, 100).tex(16 * 0.00390625F, 0).endVertex();
        WORLD_RENDERER.pos(x, y, 100).tex(0, 0).endVertex();
        TESSELLATOR.draw();
    }

    public static void drawTexturedRect(float x, float y, float width, float height, float uMin, float uMax, float vMin, float vMax, int filter) {
        GlStateManager.enableTexture2D();

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, filter);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, filter);

        WORLD_RENDERER.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        WORLD_RENDERER.pos(x, y + height, 0.0D).tex(uMin, vMax).endVertex();
        WORLD_RENDERER.pos(x + width, y + height, 0.0D).tex(uMax, vMax).endVertex();
        WORLD_RENDERER.pos(x + width, y, 0.0D).tex(uMax, vMin).endVertex();
        WORLD_RENDERER.pos(x, y, 0.0D).tex(uMin, vMin).endVertex();
        TESSELLATOR.draw();

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
    }

    public static Framebuffer refreshFramebuffer(Framebuffer framebuffer, int width, int height) {
        if (framebuffer == null) {
            framebuffer = new Framebuffer(width, height, true);
            framebuffer.setFramebufferFilter(GL11.GL_NEAREST);
            framebuffer.framebufferColor[0] = 0;
            framebuffer.framebufferColor[1] = 0;
            framebuffer.framebufferColor[2] = 0;
        } else if (framebuffer.framebufferWidth != width || framebuffer.framebufferHeight != height) {
            framebuffer.createBindFramebuffer(width, height);
            framebuffer.setFramebufferFilter(GL11.GL_NEAREST);
        }

        GuiIngameCache.framebuffer = framebuffer;
        return framebuffer;
    }
}

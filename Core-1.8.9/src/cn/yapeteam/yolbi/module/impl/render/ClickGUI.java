package cn.yapeteam.yolbi.module.impl.render;

import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.game.EventKey;
import cn.yapeteam.yolbi.event.impl.render.EventRender2D;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.api.Category;
import cn.yapeteam.yolbi.module.api.ModuleInfo;
import cn.yapeteam.yolbi.utils.StopWatch;
import cn.yapeteam.yolbi.utils.render.layer.Layers;
import org.lwjgl.input.Keyboard;

@ModuleInfo(aliases = "module.render.clickgui.name", description = "module.render.clickgui.description", category = Category.RENDER, keyBind = Keyboard.KEY_RCONTROL)
public final class ClickGUI extends Module {
    private final StopWatch stopWatch = new StopWatch();

    @Override
    public void onEnable() {
        YolBi.instance.getEventManager().register(YolBi.instance.getClickGUI());
        mc.displayGuiScreen(YolBi.instance.getClickGUI());
        stopWatch.reset();
    }

    @Override
    public void onDisable() {
        mc.setIngameFocus();
        Keyboard.enableRepeatEvents(false);
        YolBi.instance.getEventManager().unregister(YolBi.instance.getClickGUI());
    }

    @Listener
    public void onRender2D(EventRender2D eventRender2D){
        getLayer(Layers.REGULAR, 2).add(() -> YolBi.instance.getClickGUI().render());
        getLayer(Layers.BLOOM, 3).add(() -> YolBi.instance.getClickGUI().bloom());
    }

    @Listener
    public void onKey(EventKey event){
        if (!stopWatch.finished(50)) return;

        if (event.getKey() == this.getKey()) {
            this.mc.displayGuiScreen(null);

            if (this.mc.currentScreen == null) {
                this.mc.setIngameFocus();
                this.toggle();
            }
        }
    }

//    @Listener
//    public void onRender2D(EventRender2D eventRender2D){
//        reportGLStates();
//        reportminecraftstate();
//        getLayer(Layers.BLOOM, 2).add(() -> render());
//    }
//
//    public void render() {
//        RenderManager.roundedRectangle(10, 10, 100, 100, 5, new Color(123,11,231));
//    }
//
//    public void reportminecraftstate(){
//        ScaledResolution scaledResolution = new ScaledResolution(mc);
//        Logger.info("Minecraft State Report:");
//        Logger.info("Minecraft Display Width: " + mc.displayWidth);
//        Logger.info("Minecraft Display Height: " + mc.displayHeight);
//        Logger.info("scaledWidth: " + scaledResolution.getScaledWidth());
//        Logger.info("scaledHeight: " + scaledResolution.getScaledHeight());
//    }
//
//    public void reportGLStates() {
//        // Enable/Disable States
//        Logger.info("OpenGL State Report:");
//
//        // Blending State
//        boolean blendingEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
//        Logger.info("Blending Enabled: " + blendingEnabled);
//
//        if (blendingEnabled) {
//            int blendSrc = GL11.glGetInteger(GL11.GL_BLEND_SRC);
//            int blendDst = GL11.glGetInteger(GL11.GL_BLEND_DST);
//            Logger.info("Blend Source Factor: " + blendSrc);
//            Logger.info("Blend Dest Factor: " + blendDst);
//        }
//
//        // Depth Testing State
//        boolean depthTestEnabled = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
//        Logger.info("Depth Test Enabled: " + depthTestEnabled);
//
//        // Culling State
//        boolean cullFaceEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);
//        Logger.info("Cull Face Enabled: " + cullFaceEnabled);
//
//        if (cullFaceEnabled) {
//            int cullFaceMode = GL11.glGetInteger(GL11.GL_CULL_FACE_MODE);
//            Logger.info("Cull Face Mode: " + cullFaceMode);
//        }
//
//        // Depth Mask
//        boolean depthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
//        Logger.info("Depth Write Mask: " + depthMask);
//
//        // Clear Depth
//        double clearDepth = GL11.glGetDouble(GL11.GL_DEPTH_CLEAR_VALUE);
//        Logger.info("Clear Depth Value: " + clearDepth);
//
//        // Current Active Texture
//        int activeTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
//        Logger.info("Active Texture Unit: " + activeTexture);
//
//        // Texture Binding
//        int boundTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
//        Logger.info("Bound 2D Texture: " + boundTexture);
//
//        // Shader Program Info
//        int currentProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
//        Logger.info("Current Shader Program: " + currentProgram);
//
//        // Framebuffer Bound
//        int framebuffer = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
//        Logger.info("Framebuffer Bound: " + framebuffer);
//    }


}

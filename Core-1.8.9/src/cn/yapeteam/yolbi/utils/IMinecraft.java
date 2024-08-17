package cn.yapeteam.yolbi.utils;

import cn.yapeteam.yolbi.utils.profiling.Profiler;
import cn.yapeteam.yolbi.utils.render.shader.RiseShaders;
import cn.yapeteam.yolbi.utils.render.shader.base.ShaderRenderType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public interface IMinecraft {
    Minecraft mc = Minecraft.getMinecraft();

    List<Runnable> UI_BLOOM_RUNNABLES = new ArrayList<>();
    List<Runnable> UI_POST_BLOOM_RUNNABLES = new ArrayList<>();
    List<Runnable> UI_RENDER_RUNNABLES = new ArrayList<>();
    List<Runnable> UI_BLUR_RUNNABLES = new ArrayList<>();

    List<Runnable> NORMAL_PRE_RENDER_RUNNABLES = new ArrayList<>();
    List<Runnable> NORMAL_BLUR_RUNNABLES = new ArrayList<>();
    List<Runnable> NORMAL_POST_BLOOM_RUNNABLES = new ArrayList<>();
    List<Runnable> NORMAL_OUTLINE_RUNNABLES = new ArrayList<>();
    List<Runnable> NORMAL_RENDER_RUNNABLES = new ArrayList<>();
    List<Runnable> NORMAL_POST_RENDER_RUNNABLES = new ArrayList<>();

    List<Runnable> LIMITED_PRE_RENDER_RUNNABLES = new ArrayList<>();
    List<Runnable> LIMITED_POST_RENDER_RUNNABLES = new ArrayList<>();

    Executor threadPool = Executors.newFixedThreadPool(2);

    Profiler bloomProfiler = new Profiler();
    Profiler render2dProfiler = new Profiler();
    Profiler renderLimited2dProfiler = new Profiler();
    Profiler outlineProfiler = new Profiler();
    Profiler blurProfiler = new Profiler();
    Profiler dragProfiler = new Profiler();

    static void render2DRunnables(float partialTicks, boolean shaders) {

        GuiIngame guiIngame = mc.ingameGUI;
        render2dProfiler.start();
        NORMAL_PRE_RENDER_RUNNABLES.forEach(Runnable::run);
        render2dProfiler.stop();

        if (shaders) {
            outlineProfiler.start();
            RiseShaders.OUTLINE_SHADER.run(ShaderRenderType.OVERLAY, partialTicks, IMinecraft.NORMAL_OUTLINE_RUNNABLES);
            outlineProfiler.stop();

            blurProfiler.start();
            RiseShaders.GAUSSIAN_BLUR_SHADER.run(ShaderRenderType.OVERLAY, partialTicks, IMinecraft.NORMAL_BLUR_RUNNABLES);
            blurProfiler.stop();
        }

        render2dProfiler.start();
        NORMAL_RENDER_RUNNABLES.forEach(Runnable::run);
        render2dProfiler.stop();

        renderLimited2dProfiler.start();
        guiIngame.renderGameOverlay(partialTicks);
        renderLimited2dProfiler.start();

        render2dProfiler.start();
        NORMAL_POST_RENDER_RUNNABLES.forEach(Runnable::run);
        render2dProfiler.stop();

        if (shaders) {
            bloomProfiler.start();
            RiseShaders.POST_BLOOM_SHADER.run(ShaderRenderType.OVERLAY, partialTicks, IMinecraft.NORMAL_POST_BLOOM_RUNNABLES);
            bloomProfiler.stop();
        }

        dragProfiler.start();
        dragProfiler.stop();

        UI_RENDER_RUNNABLES.forEach(Runnable::run);

        if (mc.currentScreen != null) {

            RiseShaders.UI_BLOOM_SHADER.run(ShaderRenderType.OVERLAY, partialTicks, IMinecraft.UI_BLOOM_RUNNABLES);

            RiseShaders.UI_POST_BLOOM_SHADER.run(ShaderRenderType.OVERLAY, partialTicks, IMinecraft.UI_POST_BLOOM_RUNNABLES);
        }

        dragProfiler.reset();
        bloomProfiler.reset();
        render2dProfiler.reset();
        outlineProfiler.reset();
        blurProfiler.reset();
        renderLimited2dProfiler.reset();
    }

    static void render3DRunnables(float partialTicks) {
        RiseShaders.OUTLINE_SHADER.run(ShaderRenderType.CAMERA, partialTicks, IMinecraft.NORMAL_OUTLINE_RUNNABLES);
        RiseShaders.POST_BLOOM_SHADER.run(ShaderRenderType.CAMERA, partialTicks, IMinecraft.NORMAL_POST_BLOOM_RUNNABLES);
        RiseShaders.UI_BLOOM_SHADER.run(ShaderRenderType.CAMERA, partialTicks, IMinecraft.UI_BLOOM_RUNNABLES);
        RiseShaders.GAUSSIAN_BLUR_SHADER.run(ShaderRenderType.CAMERA, partialTicks, IMinecraft.NORMAL_BLUR_RUNNABLES);
    }

    static void clearRunnables() {
        NORMAL_BLUR_RUNNABLES.clear();
        NORMAL_POST_BLOOM_RUNNABLES.clear();
        NORMAL_OUTLINE_RUNNABLES.clear();
        NORMAL_RENDER_RUNNABLES.clear();
        UI_BLOOM_RUNNABLES.clear();
        UI_RENDER_RUNNABLES.clear();
        NORMAL_PRE_RENDER_RUNNABLES.clear();
        NORMAL_POST_RENDER_RUNNABLES.clear();
        UI_POST_BLOOM_RUNNABLES.clear();

        LIMITED_PRE_RENDER_RUNNABLES.clear();
        LIMITED_POST_RENDER_RUNNABLES.clear();
    }

}

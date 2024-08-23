package cn.yapeteam.yolbi.utils.render.shader.base;

import cn.yapeteam.loader.logger.Logger;
import cn.yapeteam.yolbi.utils.interfaces.Accessor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.shader.Framebuffer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
@Setter
public abstract class RiseShader implements Accessor {
    private boolean active;

    public abstract void run(ShaderRenderType type, float partialTicks, List<Runnable> runnable);

    public abstract void update();

    protected boolean shouldResize(@NotNull Framebuffer instance, int width, int height) {
        Logger.info("[DEBUG] Checking if framebuffer should resize. Current size: "
                + instance.framebufferWidth + "x" + instance.framebufferHeight
                + ", Target size: " + width + "x" + height);
        return width != instance.framebufferWidth || height != instance.framebufferHeight;
    }
}

package cn.yapeteam.yolbi.utils.render.shader.base;

import cn.yapeteam.yolbi.utils.interfaces.IMinecraft;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.shader.Framebuffer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
@Setter
public abstract class RiseShader implements IMinecraft {
    private boolean active;

    public abstract void run(ShaderRenderType type, float partialTicks, List<Runnable> runnable);

    public abstract void update();

    protected boolean shouldResize(@NotNull Framebuffer instance, int width, int height) {
        return width != instance.framebufferWidth || height != instance.framebufferHeight;
    }
}

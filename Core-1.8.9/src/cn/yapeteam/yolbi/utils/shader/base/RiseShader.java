package cn.yapeteam.yolbi.utils.shader.base;

import RazerOfficial.Razer.gg.util.interfaces.InstanceAccess;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public abstract class RiseShader implements InstanceAccess {
    private boolean active;

    public abstract void run(ShaderRenderType type, float partialTicks, List<Runnable> runnable);

    public abstract void update();
}

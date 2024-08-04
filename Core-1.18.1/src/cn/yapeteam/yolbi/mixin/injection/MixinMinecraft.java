package cn.yapeteam.yolbi.mixin.injection;

import cn.yapeteam.ymixin.annotations.Mixin;
import cn.yapeteam.ymixin.annotations.Overwrite;
import cn.yapeteam.yolbi.managers.ReflectionManager;
import net.minecraft.client.Minecraft;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Overwrite(method = "getInstance", desc = "()Lnet/minecraft/world/entity/Entity;")
    public static Minecraft getInstance() {
        return ReflectionManager.Minecraft$getInstance();
    }
}

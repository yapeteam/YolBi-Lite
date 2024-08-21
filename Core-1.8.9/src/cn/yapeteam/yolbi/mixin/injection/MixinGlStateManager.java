package cn.yapeteam.yolbi.mixin.injection;

import cn.yapeteam.ymixin.annotations.Inject;
import cn.yapeteam.ymixin.annotations.Mixin;
import cn.yapeteam.ymixin.annotations.Target;
import net.minecraft.client.renderer.GlStateManager;

@Mixin(GlStateManager.class)
public class MixinGlStateManager {
    public static boolean caching = false;
    private static boolean blendCacheState = false;

    @Inject(method = "disableBlend", desc = "()V", target = @Target(
            value = "INVOKEVIRTUAL",
            target = "net/minecraft/client/renderer/GlStateManager$BooleanState.setDisabled ()V",
            shift = Target.Shift.BEFORE
    ))
    public void onDisableBlend(){
        if(caching){
            blendCacheState = false;
        }
    }


    @Inject(method = "enableBlend", desc = "()V", target = @Target(
            value = "INVOKEVIRTUAL",
            target = "net/minecraft/client/renderer/GlStateManager$BooleanState.setEnabled ()V",
            shift = Target.Shift.BEFORE
    ))
    public void onEnableBlend(){
        if (caching) {
            blendCacheState = true;
        }

    }
}

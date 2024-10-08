package cn.yapeteam.yolbi.mixin.injection;

import cn.yapeteam.ymixin.annotations.*;
import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.event.impl.player.EventAttack;
import cn.yapeteam.yolbi.event.type.CancellableEvent;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.GameType;

/**
 * @author yuxiangll
 * @since 2024/1/7 20:58
 * IntelliJ IDEA
 */
@Mixin(PlayerControllerMP.class)
public class MixinPlayerControllerMP {

    @Shadow
    private GameType currentGameType = GameType.SURVIVAL;

    @Inject(
            method = "attackEntity",
            desc = "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/entity/Entity;)V",
            target = @Target("HEAD")
    )
    public void attackEntity(@Local(source = "targetEntity", index = 2) Entity targetEntity) {
        if (targetEntity != null && ((CancellableEvent) YolBi.instance.getEventManager().post(new EventAttack((EntityLivingBase) targetEntity))).isCancelled()) {
            //noinspection UnnecessaryReturnStatement
            return;
        }
    }
}

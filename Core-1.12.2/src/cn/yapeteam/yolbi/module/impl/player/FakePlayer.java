package cn.yapeteam.yolbi.module.impl.player;

import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.game.EventLoadWorld;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.api.Category;
import cn.yapeteam.yolbi.module.api.ModuleInfo;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import org.lwjgl.input.Keyboard;


@ModuleInfo(aliases = {"module.player.fakeplayer.name"}, description = "module.player.fakeplayer.description", category = Category.PLAYER, keyBind = Keyboard.KEY_V, isblatant = true, isghost = false)
public class FakePlayer extends Module {

    private EntityOtherPlayerMP blinkEntity;

    public void deSpawnEntity() {
        if (blinkEntity != null) {
            mc.world.removeEntityFromWorld(blinkEntity.getEntityId());
            blinkEntity = null;
        }
    }

    public void spawnEntity() {
        if (blinkEntity == null) {
            blinkEntity = new EntityOtherPlayerMP(mc.world, mc.player.getGameProfile());
            blinkEntity.setPositionAndRotation(mc.player.posX, mc.player.posY, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch);
            blinkEntity.rotationYawHead = mc.player.rotationYawHead;
            blinkEntity.setSprinting(mc.player.isSprinting());
            blinkEntity.setInvisible(mc.player.isInvisible());
            blinkEntity.setSneaking(mc.player.isSneaking());
            blinkEntity.inventory = mc.player.inventory;
            blinkEntity.setHealth(mc.player.getHealth());

            mc.world.addEntityToWorld(blinkEntity.getEntityId(), blinkEntity);
        }
    }

    @Override
    public void onEnable() {
        spawnEntity();
    }

    @Override
    public void onDisable() {
        deSpawnEntity();
    }

    @Listener
    public void onworldChange(EventLoadWorld event) {
        this.onDisable();
    }
}

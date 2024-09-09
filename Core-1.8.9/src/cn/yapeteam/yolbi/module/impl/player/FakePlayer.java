package cn.yapeteam.yolbi.module.impl.player;

import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.game.EventLoadWorld;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.api.Category;
import cn.yapeteam.yolbi.module.api.ModuleInfo;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import org.lwjgl.input.Keyboard;


@ModuleInfo(aliases = {"module.player.fakeplayer.name"}, description = "module.player.fakeplayer.description", category = Category.PLAYER, keyBind = Keyboard.KEY_V)
public class FakePlayer extends Module {

    private EntityOtherPlayerMP blinkEntity;

    public void deSpawnEntity() {
        if (blinkEntity != null) {
            mc.theWorld.removeEntityFromWorld(blinkEntity.getEntityId());
            blinkEntity = null;
        }
    }

    public void spawnEntity() {
        if (blinkEntity == null) {
            blinkEntity = new EntityOtherPlayerMP(mc.theWorld, mc.thePlayer.getGameProfile());
            blinkEntity.setPositionAndRotation(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
            blinkEntity.rotationYawHead = mc.thePlayer.rotationYawHead;
            blinkEntity.setSprinting(mc.thePlayer.isSprinting());
            blinkEntity.setInvisible(mc.thePlayer.isInvisible());
            blinkEntity.setSneaking(mc.thePlayer.isSneaking());
            blinkEntity.inventory = mc.thePlayer.inventory;
            blinkEntity.setHealth(mc.thePlayer.getHealth());

            mc.theWorld.addEntityToWorld(blinkEntity.getEntityId(), blinkEntity);
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

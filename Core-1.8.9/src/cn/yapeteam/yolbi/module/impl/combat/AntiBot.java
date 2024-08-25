package cn.yapeteam.yolbi.module.impl.combat;

import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.player.EventUpdate;
import cn.yapeteam.yolbi.managers.BotManager;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.api.Category;
import cn.yapeteam.yolbi.module.api.ModuleInfo;
import cn.yapeteam.yolbi.module.api.value.impl.ModeValue;
import cn.yapeteam.yolbi.module.api.value.impl.SubMode;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

@ModuleInfo(aliases = {"module.combat.antibot.name"}, description = "module.combat.antibot.description", category = Category.COMBAT)
public class AntiBot extends Module {

    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new SubMode("Hypixel"))
            .setDefault("Hypixel");

    @Listener
    private void onUpdate(EventUpdate event) {
        if (mode.getValue().toString().equalsIgnoreCase("Hypixel")) {
            for (int i = 0; i < mc.theWorld.getLoadedEntityList().size(); i++) {
                Entity entity = mc.theWorld.getLoadedEntityList().get(i);

                if (!(entity instanceof EntityPlayer)) continue;

                if (entity.getName().contains("\u00A7") || (entity.hasCustomName() && entity.getCustomNameTag().contains(entity.getName())) || (entity.getName().equals(mc.thePlayer.getName()) && entity != mc.thePlayer)) {
                    BotManager.addBot(entity);
                }
            }
        }
    }

    public static boolean isBot(EntityLivingBase entity) {
        // must have a player info
        final NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfo(entity.getUniqueID());
        return info == null;
    }

    @Override
    public void onDisable() {
        BotManager.bots.clear();
    }
}

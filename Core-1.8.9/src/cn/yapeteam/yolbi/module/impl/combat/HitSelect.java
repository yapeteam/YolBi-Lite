package cn.yapeteam.yolbi.module.impl.combat;

import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.player.EventAttack;
import cn.yapeteam.yolbi.event.impl.player.EventMotion;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.ModuleCategory;
import cn.yapeteam.yolbi.module.values.impl.ModeValue;
import cn.yapeteam.yolbi.module.values.impl.NumberValue;
import cn.yapeteam.yolbi.utils.player.MoveUtil;
import net.minecraft.entity.Entity;

public class HitSelect extends Module {

    public static final ModeValue<String> preference = new ModeValue<>("Move Speed", "Closest", "Move speed", "KB reduction", "Critical hits");
    public static final ModeValue<String> mode = new ModeValue<>("Mode", "Pause", "Pause", "Active");
    public static final NumberValue<Integer> delay = new NumberValue<>("Delay", 420, 300, 500, 1);
    public static final NumberValue<Integer> chance = new NumberValue<>("Chance", 80, 0, 100, 1);

    private static long attackTime = -1;
    private static boolean currentShouldAttack = false;

    public HitSelect() {
        super("HitSelect", ModuleCategory.COMBAT);
        addValues(mode, preference, delay, chance);
    }

    @Listener
    public void onAttack(EventAttack event) {
        if (mode.getValue().equals("Active") && !currentShouldAttack) {
            event.setCancelled(true);
            return;
        }

        attackTime = System.currentTimeMillis();
    }

    @Listener
    public void onPreUpdate(EventMotion event) {
        if (Math.random() * 100 > chance.getValue()) {
            currentShouldAttack = true;
        } else {
            switch (preference.getValue()) {
                case "Move speed":
                    currentShouldAttack = mc.thePlayer.hurtTime > 0 && !mc.thePlayer.onGround && MoveUtil.isMoving();
                    break;
                case "KB reduction":
                    currentShouldAttack = !mc.thePlayer.onGround && mc.thePlayer.motionY < 0;
                    break;
            }

            if (!currentShouldAttack)
                currentShouldAttack = System.currentTimeMillis() - attackTime >= delay.getValue();
        }
    }

    public static boolean canAttack(Entity target) {
        return canSwing();
    }

    public static boolean canSwing() {
        if (mode.getValue().equals("Active")) return true;
        return currentShouldAttack;
    }
}

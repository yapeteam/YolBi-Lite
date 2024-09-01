package cn.yapeteam.yolbi.module.impl.combat.wtap;


import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.player.EventAttack;
import cn.yapeteam.yolbi.event.impl.player.EventMotion;
import cn.yapeteam.yolbi.module.api.value.Mode;
import cn.yapeteam.yolbi.module.impl.ghost.WTap;
import net.minecraft.entity.EntityLivingBase;

public final class SilentWTap extends Mode<WTap> {
    private EntityLivingBase target;

    public SilentWTap(String name, WTap parent) {
        super(name, parent);
    }

    @Listener
    public void onMotion(EventMotion eventMotion){
        if (target != null && target.hurtTime == 9) {
            mc.thePlayer.setSprinting(false);
        }
    };

    @Listener
    public void onAttack(EventAttack event){
        target = (EntityLivingBase) event.getTargetEntity();
    };
}

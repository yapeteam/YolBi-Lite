package cn.yapeteam.yolbi.module.impl.combat;

import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.game.EventTick;
import cn.yapeteam.yolbi.event.impl.network.EventPacketReceive;
import cn.yapeteam.yolbi.event.impl.player.EventAttack;
import cn.yapeteam.yolbi.event.impl.render.EventRender3D;
import cn.yapeteam.yolbi.managers.TargetManager;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.ModuleCategory;
import cn.yapeteam.yolbi.module.values.impl.BooleanValue;
import cn.yapeteam.yolbi.module.values.impl.NumberValue;
import lombok.val;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.server.S18PacketEntityTeleport;
import net.minecraft.util.Vec3;

import java.util.List;

public class Backtrack extends Module {
    public static EntityLivingBase target;
    public Backtrack() {
        super("Backtrack-fix", ModuleCategory.COMBAT);
        addValues(amount,range,esp);
    }
    private NumberValue<Double> amount = new NumberValue<Double>("Amount", 1.0, 1.0, 3.0, 0.1);
    private final NumberValue<Double> range = new NumberValue<Double>("Range", 2.0, 2.0, 8.0, 0.1);
    private final NumberValue<Double> interval = new NumberValue<Double>("IntervalTick", 1.0, 0.0, 10.0, 1.0);
    private final BooleanValue esp = new BooleanValue("Esp", false);
    private Vec3 realTargetPosition = new Vec3(0.0, 0.0, 0.0);
    public static double realX;
    public static double realY;
    public static double realZ;
    int tick = 0;


    public void onAttack(EventAttack e) {

        val target =
                (val) TargetManager.getTargets(3.6);
        ;
    }

    @Listener
    public void onTick(EventTick e) {
        target =
                (EntityLivingBase) TargetManager.getTargets(3.6);
        if ((double)this.tick <= this.interval.getValue()) {
            ++this.tick;
        }
        if (target != null && (double)mc.thePlayer.getDistanceToEntity(target) <= this.range.getValue()) {
            Vec3 vec3 = new Vec3(target.posX, target.posY, target.posZ);
            if (vec3.distanceTo(this.realTargetPosition) < this.amount.getValue() && (double)this.tick > this.interval.getValue()) {
                target.posX = target.lastTickPosX;
                target.posY = target.lastTickPosY;
                target.posZ = target.lastTickPosZ;
                this.tick = 0;
            }
        }
    }
    @Listener
    public void onPacketReceive(EventPacketReceive e) {
        S18PacketEntityTeleport s18;
        if (e.getPacket() instanceof S18PacketEntityTeleport && (s18 = (S18PacketEntityTeleport)e.getPacket()).getEntityId() == target.getEntityId()) {
            this.realTargetPosition = new Vec3((double)s18.getX() / 32.0, (double)s18.getY() / 32.0, (double)s18.getZ() / 32.0);
            realX = (double)s18.getX() / 32.0;
            realY = (double)s18.getY() / 32.0;
            realZ = (double)s18.getZ() / 32.0;
        }
    }
    @Listener
    public void onRender3D(EventRender3D event) {
       // if (this.esp.getValue().booleanValue() && KillAura.target != null) {
          //  RenderUtil.renderBoundingBox(target, HUD.color(2), 60.0f);
           // RenderUtil.resetColor();
       // 厉害主播 这个没有 ~}
    }
    @Override
    public void onDisable() {
        target = null;
        this.tick = 0;
    }



}
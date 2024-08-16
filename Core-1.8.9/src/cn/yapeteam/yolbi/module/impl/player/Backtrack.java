package cn.yapeteam.yolbi.module.impl.player;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.game.EventTick;
import cn.yapeteam.yolbi.event.impl.network.EventPacketReceive;
import cn.yapeteam.yolbi.event.impl.player.EventAttack;
import cn.yapeteam.yolbi.event.impl.player.EventUpdate;
import cn.yapeteam.yolbi.event.impl.render.EventRender3D;
import cn.yapeteam.yolbi.managers.PacketManager;
import cn.yapeteam.yolbi.managers.ReflectionManager;
import cn.yapeteam.yolbi.managers.RenderManager;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.ModuleCategory;
import cn.yapeteam.yolbi.module.values.impl.compat.SliderSetting;
import cn.yapeteam.yolbi.utils.Utils;
import cn.yapeteam.yolbi.utils.animation.Animation;
import cn.yapeteam.yolbi.utils.animation.Easing;
import cn.yapeteam.yolbi.utils.backtrack.TimedPacket;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.*;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

public class Backtrack extends Module {
    public static final int color = new Color(72, 125, 227).getRGB();

    private final SliderSetting minLatency = new SliderSetting("Min latency", 50, 10, 1000, 10);
    private final SliderSetting maxLatency = new SliderSetting("Max latency", 100, 10, 1000, 10);
    private final SliderSetting minDistance = new SliderSetting("Min distance", 0.0, 0.0, 3.0, 0.1);
    private final SliderSetting maxDistance = new SliderSetting("Max distance", 6.0, 0.0, 10.0, 0.1);
    private final SliderSetting stopOnTargetHurtTime = new SliderSetting("Stop on target HurtTime", -1, -1, 10, 1);
    private final SliderSetting stopOnSelfHurtTime = new SliderSetting("Stop on self HurtTime", -1, -1, 10, 1);

    private final Queue<TimedPacket> packetQueue = new ConcurrentLinkedQueue<>();
    private final List<Packet<?>> skipPackets = new ArrayList<>();
    private @Nullable Animation animationX;
    private @Nullable Animation animationY;
    private @Nullable Animation animationZ;
    private Vec3 vec3;
    private EntityPlayer target;

    private int currentLatency = 0;

    public Backtrack() {
        super("Backtrack", ModuleCategory.PLAYER);
        this.addValues(minLatency);
        this.addValues(maxLatency);
        this.addValues(minDistance);
        this.addValues(maxDistance);
        this.addValues(stopOnTargetHurtTime);
        this.addValues(stopOnSelfHurtTime);
    }

    @Override
    public String getDescription() {  // ? 6
        return "Allows you to hit past opponents.";
    }

    @Override
    public void onEnable() {
        packetQueue.clear();
        skipPackets.clear();
        vec3 = null;
        target = null;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.thePlayer == null)
            return;

        releaseAll();
    }

    @Listener
    public void onPreUpdate(@NotNull EventUpdate e) {
        if (!e.isPre()) return;

        Utils.correctValue(minLatency, maxLatency);
        Utils.correctValue(minDistance, maxDistance);

        try {
            final double distance = vec3.distanceTo(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ));
            if (distance > maxDistance.getInput()
                    || distance < minDistance.getInput()
            ) {
                currentLatency = 0;
            }

        } catch (NullPointerException ignored) {
        }
    }

    @Listener
    public void onPreTick(EventTick e) {
        while (!packetQueue.isEmpty()) {
            try {
                if (packetQueue.element().getCold().getCum(currentLatency)) {
                    Packet<INetHandlerPlayClient> packet = (Packet<INetHandlerPlayClient>) packetQueue.remove().getPacket();
                    skipPackets.add(packet);
                    PacketManager.receivePacket(packet);
//                    ReflectionManager.callMethod(NetworkManager.class, mc.getNetHandler().getNetworkManager(), "channelRead0", packet);
                } else {
                    break;
                }
            } catch (NullPointerException exception) {
                Utils.sendMessage(exception.getMessage());
            }
        }

        if (packetQueue.isEmpty() && target != null) {
            vec3 = target.getPositionVector();
        }
    }

    @Listener
    public void onRender(EventRender3D e) {
        if (target == null || vec3 == null || target.isDead)
            return;

        final Vec3 pos = currentLatency > 0 ? vec3 : target.getPositionVector();

        if (animationX == null || animationY == null || animationZ == null) {
            animationX = new Animation(Easing.EASE_OUT_CIRC, 50);
            animationY = new Animation(Easing.EASE_OUT_CIRC, 50);
            animationZ = new Animation(Easing.EASE_OUT_CIRC, 50);

            animationX.setValue(pos.xCoord);
            animationY.setValue(pos.yCoord);
            animationZ.setValue(pos.zCoord);
        }

        animationX.animate(pos.xCoord);
        animationY.animate(pos.yCoord);
        animationZ.animate(pos.zCoord);
        drawBox(new net.minecraft.util.Vec3(animationX.getValue(), animationY.getValue(), animationZ.getValue()));
    }

    public static void drawBox(@NotNull Vec3 pos) {
        GlStateManager.pushMatrix();
        double x = pos.xCoord - mc.getRenderManager().viewerPosX;
        double y = pos.yCoord - mc.getRenderManager().viewerPosY;
        double z = pos.zCoord - mc.getRenderManager().viewerPosZ;
        AxisAlignedBB bbox = mc.thePlayer.getEntityBoundingBox().expand(0.1D, 0.1, 0.1);
        AxisAlignedBB axis = new AxisAlignedBB(bbox.minX - mc.thePlayer.posX + x, bbox.minY - mc.thePlayer.posY + y, bbox.minZ - mc.thePlayer.posZ + z, bbox.maxX - mc.thePlayer.posX + x, bbox.maxY - mc.thePlayer.posY + y, bbox.maxZ - mc.thePlayer.posZ + z);
        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glLineWidth(2.0F);
        GL11.glColor4f(r, g, b, a);
        RenderManager.drawFilledBox(axis);
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GlStateManager.popMatrix();
    }

    @Listener
    public void onAttack(@NotNull EventAttack e) {
        final Vec3 targetPos = e.getTargetEntity().getPositionVector();
        if (e.getTargetEntity() instanceof EntityPlayer) {
            if (target == null || e.getTargetEntity() != target) {
                vec3 = targetPos;
                if (animationX != null && animationY != null && animationZ != null) {
                    long duration = target == null ? 0 : Math.min(500, Math.max(100, (long) new Vec3(e.getTargetEntity().posX, e.getTargetEntity().posY, e.getTargetEntity().posZ).distanceTo(new Vec3(target.posX, target.posY, target.posZ)) * 50));
                    animationX.setDuration(duration);
                    animationY.setDuration(duration);
                    animationZ.setDuration(duration);
                }
            } else if (animationX != null && animationY != null && animationZ != null) {
                animationX.setDuration(100);
                animationY.setDuration(100);
                animationZ.setDuration(100);
            }
            target = (EntityPlayer) e.getTargetEntity();

            try {
                final double distance = targetPos.distanceTo(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ));
                if (distance > maxDistance.getInput() || distance < minDistance.getInput())
                    return;

            } catch (NullPointerException ignored) {
            }

            currentLatency = (int) (Math.random() * (maxLatency.getInput() - minLatency.getInput()) + minLatency.getInput());
        }
    }

    @Listener
    public void onReceivePacket(@NotNull EventPacketReceive e) {
        if (!Utils.nullCheck()) return;
        Packet<?> p = e.getPacket();
        if (skipPackets.contains(p)) {
            skipPackets.remove(p);
            return;
        }

        if (target != null && stopOnTargetHurtTime.getInput() != -1 && target.hurtTime == stopOnTargetHurtTime.getInput()) {
            releaseAll();
            return;
        }
        if (stopOnSelfHurtTime.getInput() != -1 && mc.thePlayer.hurtTime == stopOnSelfHurtTime.getInput()) {
            releaseAll();
            return;
        }

        try {
            if (mc.thePlayer == null || mc.thePlayer.ticksExisted < 20) {
                packetQueue.clear();
                return;
            }

            if (target == null) {
                releaseAll();
                return;
            }

            if (e.isCancelled())
                return;

            if (p instanceof S19PacketEntityStatus
                    || p instanceof S02PacketChat
                    || p instanceof S0BPacketAnimation
                    || p instanceof S06PacketUpdateHealth
            )
                return;

            if (p instanceof S08PacketPlayerPosLook || p instanceof S40PacketDisconnect) {
                releaseAll();
                target = null;
                vec3 = null;
                return;

            } else if (p instanceof S13PacketDestroyEntities) {
                S13PacketDestroyEntities wrapper = (S13PacketDestroyEntities) p;
                for (int id : wrapper.getEntityIDs()) {
                    if (id == target.getEntityId()) {
                        target = null;
                        vec3 = null;
                        releaseAll();
                        return;
                    }
                }
            } else if (p instanceof S14PacketEntity) {
                S14PacketEntity wrapper = (S14PacketEntity) p;
                if ((int) ReflectionManager.getValue(S14PacketEntity.class, wrapper, "entityId") == target.getEntityId()) {
                    vec3 = vec3.add(new Vec3(wrapper.func_149062_c() / 32.0D, wrapper.func_149061_d() / 32.0D,
                            wrapper.func_149064_e() / 32.0D));
                }
            } else if (p instanceof S18PacketEntityTeleport) {
                S18PacketEntityTeleport wrapper = (S18PacketEntityTeleport) p;
                if (wrapper.getEntityId() == target.getEntityId()) {
                    vec3 = new Vec3(wrapper.getX() / 32.0D, wrapper.getY() / 32.0D, wrapper.getZ() / 32.0D);
                }
            }

            packetQueue.add(new TimedPacket(p));
            e.setCancelled(true);
        } catch (NullPointerException ignored) {

        }
    }

    private void releaseAll() {
        if (!packetQueue.isEmpty()) {
            for (TimedPacket timedPacket : packetQueue) {
                Packet<INetHandlerPlayClient> packet = (Packet<INetHandlerPlayClient>) timedPacket.getPacket();
                skipPackets.add(packet);
                PacketManager.receivePacket(packet);
            }
            packetQueue.clear();
        }
    }

}
package cn.yapeteam.yolbi.utils.player;

import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.module.impl.combat.CombatSettings;
import cn.yapeteam.yolbi.utils.interfaces.Accessor;
import com.google.common.base.Predicates;
import lombok.experimental.UtilityClass;
import lombok.val;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemSword;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TextComponentString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

@UtilityClass
public class PlayerUtil implements Accessor {

    Frustum frustrum = new Frustum();

    public static boolean isInViewFrustrum(Entity entity) {
        return isInViewFrustrum(entity.getEntityBoundingBox()) || entity.ignoreFrustumCheck;
    }

    private static boolean isInViewFrustrum(AxisAlignedBB bb) {
        Entity current = mc.getRenderViewEntity();
        frustrum.setPosition(current.posX, current.posY, current.posZ);
        return frustrum.isBoundingBoxInFrustum(bb);
    }

    private final HashMap<Integer, Integer> GOOD_POTIONS = new HashMap<Integer, Integer>() {{
        put(6, 1); // Instant Health
        put(10, 2); // Regeneration
        put(11, 3); // Resistance
        put(21, 4); // Health Boost
        put(22, 5); // Absorption
        put(23, 6); // Saturation
        put(5, 7); // Strength
        put(1, 8); // Speed
        put(12, 9); // Fire Resistance
        put(14, 10); // Invisibility
        put(3, 11); // Haste
        put(13, 12); // Water Breathing
    }};

    private int getPing(Entity entity) {
        val uniqueID = mc.getConnection().getPlayerInfo(entity.getUniqueID());
        return uniqueID != null ? uniqueID.getResponseTime() : 0;
    }

    public static void sendMessage(String msg) {
        if (mc.player != null) {
            mc.player.sendChatMessage(String.valueOf(new TextComponentString("\247b[Yolbi]\247r " + msg)));
        }
    }

    public static double fovFromEntity(Entity en) {
        return ((((double) (mc.player.rotationYaw - fovToEntity(en)) % 360.0D) + 540.0D) % 360.0D) - 180.0D;
    }

    public static float fovToEntity(Entity ent) {
        double xCoord = ent.posX - mc.player.posX;
        double zCoord = ent.posZ - mc.player.posZ;
        double yaw = Math.atan2(xCoord, zCoord) * 57.2957795D;
        return (float) (yaw * -1.0D);
    }

    public static boolean isMoving() {
        return mc.player.moveForward != 0 || mc.player.moveStrafing != 0;
    }

    public double direction() {
        float rotationYaw = mc.player.rotationYaw;

        if (mc.player.moveForward < 0) {
            rotationYaw += 180;
        }

        float forward = 1;

        if (mc.player.moveForward < 0) {
            forward = -0.5F;
        } else if (mc.player.moveForward > 0) {
            forward = 0.5F;
        }

        if (mc.player.moveStrafing > 0) {
            rotationYaw -= 70 * forward;
        }

        if (mc.player.moveStrafing < 0) {
            rotationYaw += 70 * forward;
        }

        return Math.toRadians(rotationYaw);
    }

    public static double PitchFromEntity(Entity en, float f) {
        return (double) (mc.player.rotationPitch - pitchToEntity(en, f));
    }

    public static float pitchToEntity(Entity ent, float f) {
        double xCoord = mc.player.getDistance(ent.posX, ent.posY, ent.posZ);
        double yCoord = mc.player.posY - (ent.posY + f);
        double pitch = (((Math.atan2(xCoord, yCoord) * 180.0D) / 3.141592653589793D));
        return (float) (90 - pitch);
    }

    public Block block(final double xCoord, final double yCoord, final double zCoord) {
        return mc.world.getBlockState(new BlockPos(xCoord, yCoord, zCoord)).getBlock();
    }

    public Block block(final BlockPos blockPos) {
        return mc.world.getBlockState(blockPos).getBlock();
    }

    public double distance(final BlockPos pos1, final BlockPos pos2) {
        final double xCoord = pos1.getX() - pos2.getX();
        final double yCoord = pos1.getY() - pos2.getY();
        final double zCoord = pos1.getZ() - pos2.getZ();
        return xCoord * xCoord + yCoord * yCoord + zCoord * zCoord;
    }

    public Block blockRelativeToPlayer(final double offsetX, final double offsetY, final double offsetZ) {
        return mc.world.getBlockState(new BlockPos(mc.player).add(offsetX, offsetY, offsetZ)).getBlock();
    }

    public boolean sameTeam(final EntityLivingBase player) {
        if (player.getTeam() != null && mc.player.getTeam() != null) {
            final char c1 = player.getDisplayName().getFormattedText().charAt(1);
            final char c2 = mc.player.getDisplayName().getFormattedText().charAt(1);
            return c1 == c2;
        }
        return false;
    }

    public EnumFacingOffset getEnumFacing(final Vec3d position) {
        for (int x2 = -1; x2 <= 1; x2 += 2) {
            if (!(PlayerUtil.block(position.xCoord + x2, position.yCoord, position.zCoord) instanceof BlockAir)) {
                if (x2 > 0) {
                    return new EnumFacingOffset(EnumFacing.WEST, new Vec3d(x2, 0, 0));
                } else {
                    return new EnumFacingOffset(EnumFacing.EAST, new Vec3d(x2, 0, 0));
                }
            }
        }

        for (int y2 = -1; y2 <= 1; y2 += 2) {
            if (!(PlayerUtil.block(position.xCoord, position.yCoord + y2, position.zCoord) instanceof BlockAir)) {
                if (y2 < 0) {
                    return new EnumFacingOffset(EnumFacing.UP, new Vec3d(0, y2, 0));
                }
            }
        }

        for (int z2 = -1; z2 <= 1; z2 += 2) {
            if (!(PlayerUtil.block(position.xCoord, position.yCoord, position.zCoord + z2) instanceof BlockAir)) {
                if (z2 < 0) {
                    return new EnumFacingOffset(EnumFacing.SOUTH, new Vec3d(0, 0, z2));
                } else {
                    return new EnumFacingOffset(EnumFacing.NORTH, new Vec3d(0, 0, z2));
                }
            }
        }

        return null;
    }

    public static double calculateHorizontalAngleDifference(Entity en) {
        return ((double) (mc.player.rotationYaw - getYaw(en)) % 360.0D + 540.0D) % 360.0D - 180.0D;
    }

    public static float getYaw(Entity ent) {
        double xCoord = ent.posX - mc.player.posX;
        double zCoord = ent.posZ - mc.player.posZ;
        double yaw = Math.atan2(xCoord, zCoord) * 57.29577951308232;
        return (float) (yaw * -1.0D);
    }

    public static boolean holdingWeapon() {
        CombatSettings settings = YolBi.instance.getModuleManager().get(CombatSettings.class);
        if (mc.player.getHeldItem(EnumHand.MAIN_HAND) == null) {
            return false;
        }
        Item getItem = mc.player.getHeldItem(EnumHand.MAIN_HAND).getItem();
        return getItem instanceof ItemSword || (settings.getAxe().getValue() && getItem instanceof ItemAxe) || (settings.getRod().getValue() && getItem instanceof ItemFishingRod) || (settings.getStick().getValue() && getItem == Items.STICK);
    }

    public static boolean overAir() {
        return mc.world.isAirBlock(new BlockPos(mc.player.posX, mc.player.posY - 1.0, mc.player.posZ));
    }

    public static boolean onEdge() {
        return onEdge(mc.player);
    }

    public static boolean onEdge(Entity entity) {
        return mc.world.getCollisionBoxes(entity, entity.getEntityBoundingBox().offset(entity.motionX / 3.0D, -1.0D, entity.motionZ / 3.0D)).isEmpty();
    }

    public static Entity getMouseOver(final float partialTicks, final double Reach) {
        Entity pointedEntity = null;
        final Entity entity = mc.getRenderViewEntity();

        if (entity != null && mc.world != null) {
            mc.mcProfiler.startSection("pick");
            mc.objectMouseOver = entity.rayTrace(Reach, partialTicks);
            double distance = Reach;
            final Vec3d Vec3d = entity.getPositionEyes(partialTicks);

            if (mc.objectMouseOver != null) {
                distance = mc.objectMouseOver.hitVec.distanceTo(Vec3d);
            }

            final Vec3d Vec3d1 = entity.getLook(partialTicks);
            final Vec3d Vec3d2 = Vec3d.addVector(Vec3d1.xCoord * Reach, Vec3d1.yCoord * Reach, Vec3d1.zCoord * Reach);
            Vec3d Vec3d3 = null;
            final float f = 1.0F;
            final List<Entity> list = mc.world.getEntitiesInAABBexcluding(entity, entity.getEntityBoundingBox().expand(Vec3d1.xCoord * Reach, Vec3d1.yCoord * Reach, Vec3d1.zCoord * Reach), Predicates.and(EntitySelectors.NOT_SPECTATING, Entity::canBeCollidedWith));
            double d2 = distance;

            for (final Entity entity1 : list) {
                final float f1 = entity1.getCollisionBorderSize();
                final AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expand(f1, f1, f1);
                final RayTraceResult movingobjectposition = axisalignedbb.calculateIntercept(Vec3d, Vec3d2);

                if (axisalignedbb.isVecInside(Vec3d)) {
                    pointedEntity = entity1;
                } else if (movingobjectposition != null) {
                    final double d3 = Vec3d.distanceTo(movingobjectposition.hitVec);

                    if (d3 < d2 || d2 == 0.0D) {
                        boolean flag1 = false;

                        if (!flag1 && entity1 == entity.getRidingEntity()) {
                            if (d2 == 0.0D) {
                                pointedEntity = entity1;
                            }
                        } else {
                            pointedEntity = entity1;
                            Vec3d3 = movingobjectposition.hitVec;
                            d2 = d3;
                        }
                    }
                }
            }

            mc.mcProfiler.endSection();
            return pointedEntity;
        }
        return null;
    }

    public boolean isBlockUnder(final double height) {
        return isBlockUnder(height, true);
    }

    public boolean isBlockUnder(final double height, final boolean boundingBox) {
        if (boundingBox) {
            for (int offset = 0; offset < height; offset += 2) {
                final AxisAlignedBB bb = mc.player.getEntityBoundingBox().offset(0, -offset, 0);

                if (!mc.world.getCollisionBoxes(mc.player, bb).isEmpty()) {
                    return true;
                }
            }
        } else {
            for (int offset = 0; offset < height; offset++) {
                IBlockState blockState = mc.world.getBlockState(mc.player.getPosition().add(0, -offset, 0));
                if (blockState.isFullBlock()) {
                    return true;
                }
            }
        }
        return false;
    }


    public boolean isBlockUnder() {
        return isBlockUnder(mc.player.height);
    }

    public boolean goodPotion(final int id) {
        return GOOD_POTIONS.containsKey(id);
    }

    public int potionRanking(final int id) {
        return GOOD_POTIONS.getOrDefault(id, -1);
    }

    public boolean inLiquid() {
        final AxisAlignedBB bb = mc.player.getEntityBoundingBox();
        for (int xCoord = MathHelper.floor(bb.minX); xCoord < MathHelper.floor(bb.maxX) + 1; ++xCoord) {
            for (int zCoord = MathHelper.floor(bb.minZ); zCoord < MathHelper.floor(bb.maxZ) + 1; ++zCoord) {
                final BlockPos pos = new BlockPos(xCoord, (int) bb.minY, zCoord);
                final Block block = mc.world.getBlockState(pos).getBlock();
                if (!(block instanceof BlockAir)) {
                    return block.getMaterial(block.getDefaultState()) == Material.WATER || block.getMaterial(block.getDefaultState()) == Material.LAVA;
                }
            }
        }
        return false;
    }

    public boolean blockNear(final int range) {
        for (int xCoord = -range; xCoord <= range; ++xCoord) {
            for (int yCoord = -range; yCoord <= range; ++yCoord) {
                for (int zCoord = -range; zCoord <= range; ++zCoord) {
                    final BlockPos pos = new BlockPos(mc.player.posX + xCoord, mc.player.posY + yCoord, mc.player.posZ + zCoord);
                    final Block block = mc.world.getBlockState(pos).getBlock();
                    if (!(block instanceof BlockAir)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean insideBlock() {
        final EntityPlayerSP player = PlayerUtil.mc.player;
        final WorldClient world = PlayerUtil.mc.world;
        final AxisAlignedBB bb = player.getEntityBoundingBox();
        for (int xCoord = MathHelper.floor(bb.minX); xCoord < MathHelper.floor(bb.maxX) + 1; ++xCoord) {
            for (int yCoord = MathHelper.floor(bb.minY); yCoord < MathHelper.floor(bb.maxY) + 1; ++yCoord) {
                for (int zCoord = MathHelper.floor(bb.minZ); zCoord < MathHelper.floor(bb.maxZ) + 1; ++zCoord) {
                    final Block block = world.getBlockState(new BlockPos(xCoord, yCoord, zCoord)).getBlock();
                    if (!(block instanceof BlockAir)) {
                        return block.getMaterial(block.getDefaultState()).blocksMovement();
                    }
                }
            }
        }
        return false;
    }

    public void sendClick(final int button, final boolean state) {
        final int keyBind = button == 0 ? mc.gameSettings.keyBindAttack.getKeyCode() : mc.gameSettings.keyBindUseItem.getKeyCode();

        KeyBinding.setKeyBindState(keyBind, state);

        if (state) {
            KeyBinding.onTick(keyBind);
        }
    }

    public static boolean onLiquid() {
        boolean onLiquid = false;
        final AxisAlignedBB playerBB = PlayerUtil.mc.player.getEntityBoundingBox();
        final WorldClient world = PlayerUtil.mc.world;
        final int yCoord = (int) playerBB.offset(0.0, -0.01, 0.0).minY;
        for (int xCoord = MathHelper.floor(playerBB.minX); xCoord < MathHelper.floor(playerBB.maxX) + 1; ++xCoord) {
            for (int zCoord = MathHelper.floor(playerBB.minZ); zCoord < MathHelper.floor(playerBB.maxZ) + 1; ++zCoord) {
                final Block block = world.getBlockState(new BlockPos(xCoord, yCoord, zCoord)).getBlock();
                if (block.getMaterial(block.getDefaultState()) == Material.WATER || block.getMaterial(block.getDefaultState()) == Material.LAVA) {
                    onLiquid = true;
                }
            }
        }
        return onLiquid;
    }

    public static @Nullable Vec3d getPlacePossibility(double offsetX, double offsetY, double offsetZ) {
        final List<Vec3d> possibilities = new ArrayList<>();
        final int range = (int) (5 + (Math.abs(offsetX) + Math.abs(offsetZ)));

        for (int xCoord = -range; xCoord <= range; ++xCoord) {
            for (int yCoord = -range; yCoord <= range; ++yCoord) {
                for (int zCoord = -range; zCoord <= range; ++zCoord) {
                    final BlockPos pos = new BlockPos(mc.player.posX + xCoord, mc.player.posY + yCoord, mc.player.posZ + zCoord);
                    final Block block = mc.world.getBlockState(pos).getBlock();
                    if (block instanceof BlockAir) {
                        possibilities.add(new Vec3d(pos.getX(), pos.getY(), pos.getZ()));
                    }
                }
            }
        }

        possibilities.removeIf(Vec3d -> mc.player.getDistance(Vec3d.xCoord, Vec3d.yCoord, Vec3d.zCoord) > 5 || !(PlayerUtil.block(Vec3d.xCoord, Vec3d.yCoord, Vec3d.zCoord) instanceof BlockAir));

        if (possibilities.isEmpty()) return null;

        possibilities.sort(Comparator.comparingDouble(Vec3d -> mc.player.getDistance(Vec3d.xCoord, Vec3d.yCoord, Vec3d.zCoord)));

        return possibilities.get(0);
    }

    public static Vec3d getPlacePossibility() {
        return getPlacePossibility(0, 0, 0);
    }

    public static boolean replaceable(BlockPos blockPos) {
        if (mc.player == null || mc.world == null) {
            return false;
        }
        return block(blockPos).isReplaceable(mc.world, blockPos);
    }

    public static boolean isFluid(@NotNull Block block) {
        return block.getMaterial(block.getDefaultState()) == Material.WATER || block.getMaterial(block.getDefaultState()) == Material.LAVA;
    }
}
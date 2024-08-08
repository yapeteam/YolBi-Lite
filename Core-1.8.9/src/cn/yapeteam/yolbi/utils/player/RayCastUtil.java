package cn.yapeteam.yolbi.utils.player;


import cn.yapeteam.loader.logger.Logger;
import cn.yapeteam.ymixin.utils.Mapper;
import cn.yapeteam.yolbi.managers.ReflectionManager;
import cn.yapeteam.yolbi.utils.IMinecraft;
import cn.yapeteam.yolbi.utils.vector.Vector2f;
import com.google.common.base.Predicates;
import kotlin.Triple;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.entity.Entity;
import net.minecraft.util.*;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.*;

/**
 * @author Patrick
 * @since 11/17/2021
 */
public final class RayCastUtil implements IMinecraft {

    private static Frustum FRUSTUM;

    public static MovingObjectPosition rayCast(final Vector2f rotation, final double range) {
        return rayCast(rotation, range, 0);
    }

    public static MovingObjectPosition rayCast(final Vector2f rotation, final double range, final float expand) {
        return rayCast(rotation, range, expand, mc.thePlayer);
    }


    public static MovingObjectPosition rayTraceCustom(Entity entity, double blockReachDistance, float yaw, float pitch) {
        Vec3 vec3 = entity.getPositionEyes(1.0F);
        Vec3 vec31 = Objects.requireNonNull(ReflectionManager.Entity$getVectorForRotation(entity, yaw, pitch));
        Vec3 vec32 = vec3.addVector(vec31.xCoord * blockReachDistance, vec31.yCoord * blockReachDistance, vec31.zCoord * blockReachDistance);
        return mc.theWorld.rayTraceBlocks(vec3, vec32, false, false, true);
    }

    public static MovingObjectPosition rayCast(final Vector2f rotation, final double range, final float expand, Entity entity) {
        final float partialTicks = Objects.requireNonNull(ReflectionManager.Minecraft$getTimer(mc)).renderPartialTicks;
        MovingObjectPosition objectMouseOver;

        if (entity != null && mc.theWorld != null) {
            objectMouseOver = rayTraceCustom(entity, range, rotation.x, rotation.y);
            double d1 = range;
            final Vec3 vec3 = entity.getPositionEyes(partialTicks);

            if (objectMouseOver != null) {
                d1 = objectMouseOver.hitVec.distanceTo(vec3);
            }

            final Vec3 vec31 = Objects.requireNonNull(ReflectionManager.Entity$getVectorForRotation(mc.thePlayer, rotation.y, rotation.x));
            final Vec3 vec32 = vec3.addVector(vec31.xCoord * range, vec31.yCoord * range, vec31.zCoord * range);
            Entity pointedEntity = null;
            Vec3 vec33 = null;
            final float f = 1.0F;
            final List<Entity> list = mc.theWorld.getEntitiesInAABBexcluding(entity, entity.getEntityBoundingBox().addCoord(vec31.xCoord * range, vec31.yCoord * range, vec31.zCoord * range).expand(f, f, f), Predicates.and(EntitySelectors.NOT_SPECTATING, Entity::canBeCollidedWith));
            double d2 = d1;

            for (final Entity entity1 : list) {
                final float f1 = entity1.getCollisionBorderSize() + expand;
                final AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expand(f1, f1, f1);
                final MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(vec3, vec32);

                if (axisalignedbb.isVecInside(vec3)) {
                    if (d2 >= 0.0D) {
                        pointedEntity = entity1;
                        vec33 = movingobjectposition == null ? vec3 : movingobjectposition.hitVec;
                        d2 = 0.0D;
                    }
                } else if (movingobjectposition != null) {
                    final double d3 = vec3.distanceTo(movingobjectposition.hitVec);

                    if (d3 < d2 || d2 == 0.0D) {
                        pointedEntity = entity1;
                        vec33 = movingobjectposition.hitVec;
                        d2 = d3;
                    }
                }
            }

            if (pointedEntity != null && (d2 < d1 || objectMouseOver == null)) {
                objectMouseOver = new MovingObjectPosition(pointedEntity, vec33);
            }

            return objectMouseOver;
        }

        return null;
    }

    public static boolean overBlock(final Vector2f rotation, final EnumFacing enumFacing, final BlockPos pos, final boolean strict) {
        final MovingObjectPosition movingObjectPosition = rayTraceCustom(mc.thePlayer, 4.5f, rotation.x, rotation.y);

        if (movingObjectPosition == null) return false;

        final Vec3 hitVec = movingObjectPosition.hitVec;
        if (hitVec == null) return false;

        return movingObjectPosition.getBlockPos().equals(pos) && (!strict || movingObjectPosition.sideHit == enumFacing);
    }

    public static boolean overBlock(final EnumFacing enumFacing, final BlockPos pos, final boolean strict) {
        final MovingObjectPosition movingObjectPosition = mc.objectMouseOver;

        if (movingObjectPosition == null) return false;

        final Vec3 hitVec = movingObjectPosition.hitVec;
        if (hitVec == null) return false;

        return movingObjectPosition.getBlockPos().equals(pos) && (!strict || movingObjectPosition.sideHit == enumFacing);
    }

    public static Boolean overBlock(final Vector2f rotation, final BlockPos pos) {
        return overBlock(rotation, EnumFacing.UP, pos, false);
    }

    public static Boolean overBlock(final Vector2f rotation, final BlockPos pos, final EnumFacing enumFacing) {
        return overBlock(rotation, enumFacing, pos, true);
    }

    public static boolean isInViewFrustrum(final Entity entity) {
        return (isInViewFrustrum(entity.getEntityBoundingBox()) || entity.ignoreFrustumCheck);
    }

    private static boolean isInViewFrustrum(final AxisAlignedBB bb) {
        if (FRUSTUM == null) FRUSTUM = new Frustum();//由于客户端初始化在非主线程进行，所以clinit中无法调用GL方法
        final Entity current = mc.getRenderViewEntity();
        FRUSTUM.setPosition(current.posX, current.posY, current.posZ);
        return FRUSTUM.isBoundingBoxInFrustum(bb);
    }

    private static Field EnumFacing$VALUES;

    static {
        try {
            EnumFacing$VALUES = EnumFacing.class.getDeclaredField(Mapper.map("net/minecraft/util/EnumFacing", "VALUES", null, Mapper.Type.Field));
            EnumFacing$VALUES.setAccessible(true);
        } catch (NoSuchFieldException e) {
            Logger.exception(e);
        }
    }

    private static EnumFacing[] EnumFacing$VALUES() {
        try {
            return (EnumFacing[]) EnumFacing$VALUES.get(null);
        } catch (IllegalAccessException e) {
            return new EnumFacing[0];
        }
    }

    private static final Set<EnumFacing> FACINGS = new HashSet<>(Arrays.asList(EnumFacing$VALUES()));

    public static @NotNull Optional<Triple<BlockPos, EnumFacing, Vec3>> getPlaceSide(@NotNull BlockPos blockPos) {
        return getPlaceSide(blockPos, FACINGS);
    }

    public static @NotNull Optional<Triple<BlockPos, EnumFacing, Vec3>> getPlaceSide(@NotNull BlockPos blockPos, Set<EnumFacing> limitFacing) {
        final List<BlockPos> possible = Arrays.asList(
                blockPos.down(), blockPos.east(), blockPos.west(),
                blockPos.north(), blockPos.south(), blockPos.up()
        );

        for (BlockPos pos : possible) {
            if (!PlayerUtil.replaceable(pos)) {
                EnumFacing facing;
                Vec3 hitPos;
                if (pos.getY() < blockPos.getY()) {
                    facing = EnumFacing.UP;
                    hitPos = new Vec3(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
                } else if (pos.getX() > blockPos.getX()) {
                    facing = EnumFacing.WEST;
                    hitPos = new Vec3(pos.getX(), pos.getY() + 0.5, pos.getZ() + 0.5);
                } else if (pos.getX() < blockPos.getX()) {
                    facing = EnumFacing.EAST;
                    hitPos = new Vec3(pos.getX() + 1, pos.getY() + 0.5, pos.getZ() + 0.5);
                } else if (pos.getZ() < blockPos.getZ()) {
                    facing = EnumFacing.SOUTH;
                    hitPos = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 1);
                } else if (pos.getZ() > blockPos.getZ()) {
                    facing = EnumFacing.NORTH;
                    hitPos = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ());
                } else {
                    facing = EnumFacing.DOWN;
                    hitPos = new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                }

                if (!limitFacing.contains(facing)) continue;

                return Optional.of(new Triple<>(pos, facing, hitPos));
            }
        }
        return Optional.empty();
    }
}
package cn.yapeteam.yolbi.utils.player;

import cn.yapeteam.yolbi.event.impl.player.EventMoveInput;
import cn.yapeteam.yolbi.utils.interfaces.Accessor;
import lombok.experimental.UtilityClass;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.MobEffects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;


/**
 * This is a motion util which can be used to do various things related to the players motion
 *
 * @author Dort, Auth, Patrick, Alan
 * @since 21/10/2021
 */
@UtilityClass
public class MoveUtil implements Accessor {

    public static final double WALK_SPEED = 0.221;
    public static final double BUNNY_SLOPE = 0.66;
    public static final double MOD_SPRINTING = 1.3F;
    public static final double MOD_SNEAK = 0.3F;
    public static final double MOD_ICE = 2.5F;
    public static final double MOD_WEB = 0.105 / WALK_SPEED;
    public static final double JUMP_HEIGHT = 0.42F;
    public static final double BUNNY_FRICTION = 159.9F;
    public static final double Y_ON_GROUND_MIN = 0.00001;
    public static final double Y_ON_GROUND_MAX = 0.0626;

    public static final double AIR_FRICTION = 0.9800000190734863D;
    public static final double WATER_FRICTION = 0.800000011920929D;
    public static final double LAVA_FRICTION = 0.5D;
    public static final double MOD_SWIM = 0.115F / WALK_SPEED;
    public static final double[] MOD_DEPTH_STRIDER = {
            1.0F,
            0.1645F / MOD_SWIM / WALK_SPEED,
            0.1995F / MOD_SWIM / WALK_SPEED,
            1.0F / MOD_SWIM,
    };

    public static final double UNLOADED_CHUNK_MOTION = -0.09800000190735147;
    public static final double HEAD_HITTER_MOTION = -0.0784000015258789;

    /**
     * Checks if the player is moving
     *
     * @return player moving
     */
    public boolean isMoving() {
        return mc.player.moveForward != 0 || mc.player.moveStrafing != 0;
    }

    /**
     * Checks if the player has enough movement input for sprinting
     *
     * @return movement input enough for sprinting
     */
    public boolean enoughMovementForSprinting() {
        return Math.abs(mc.player.moveForward) >= 0.8F || Math.abs(mc.player.moveStrafing) >= 0.8F;
    }

    /**
     * Checks if the player is allowed to sprint
     *
     * @param legit should the player follow vanilla sprinting rules?
     * @return player able to sprint
     */
    public boolean canSprint(final boolean legit) {
        return (legit ? mc.player.moveForward >= 0.8F
                && !mc.player.isCollidedHorizontally
                && (mc.player.getFoodStats().getFoodLevel() > 6 || mc.player.capabilities.allowFlying)
                && !mc.player.isPotionActive(MobEffects.BLINDNESS)
                && !mc.player.isHandActive()
                && !mc.player.isSneaking()
                : enoughMovementForSprinting());
    }


    /**
     * Returns the distance the player moved in the last tick
     *
     * @return last tick distance
     */
    public double movementDelta() {
        return Math.hypot(mc.player.posX - mc.player.prevPosX, mc.player.posZ - mc.player.prevPosZ);
    }

    public double speedPotionAmp(final double amp) {
        return mc.player.isPotionActive(MobEffects.SPEED) ? ((mc.player.getActivePotionEffect(MobEffects.SPEED).getAmplifier() + 1) * amp) : 0;
    }

    /**
     * Calculates the default player jump motion
     *
     * @return player jump motion
     */
    public double jumpMotion() {
        return jumpBoostMotion(JUMP_HEIGHT);
    }

    /**
     * Modifies a selected motion with jump boost
     *
     * @param motionY input motion
     * @return modified motion
     */
    public double jumpBoostMotion(final double motionY) {
        if (mc.player.isPotionActive(MobEffects.JUMP_BOOST)) {
            return motionY + (mc.player.getActivePotionEffect(MobEffects.JUMP_BOOST).getAmplifier() + 1) * 0.1F;
        }

        return motionY;
    }



    /**
     * Gets the players' depth strider modifier
     *
     * @return depth strider modifier
     */
    public int depthStriderLevel() {
        return EnchantmentHelper.getDepthStriderModifier(mc.player);
    }

    public static float fallDistanceForDamage() {
        float fallDistanceReq = 3;

        if (mc.player.isPotionActive(MobEffects.JUMP_BOOST)) {
            int amplifier = mc.player.getActivePotionEffect(MobEffects.JUMP_BOOST).getAmplifier();
            fallDistanceReq += (float) (amplifier + 1);
        }

        return fallDistanceReq;
    }


    /**
     * Rounds the players' position to a valid ground position
     *
     * @return valid ground position
     */
    public double roundToGround(final double posY) {
        return Math.round(posY / 0.015625) * 0.015625;
    }

    /**
     * Gets the players predicted jump motion 1 tick ahead
     *
     * @return predicted jump motion
     */
    public double predictedMotion(final double motion) {
        return (motion - 0.08) * 0.98F;
    }

    /**
     * Gets the players predicted jump motion the specified amount of ticks ahead
     *
     * @return predicted jump motion
     */
    public double predictedMotion(final double motion, final int ticks) {
        if (ticks == 0) return motion;
        double predicted = motion;

        for (int i = 0; i < ticks; i++) {
            predicted = (predicted - 0.08) * 0.98F;
        }

        return predicted;
    }


    /**
     * Sets the players' jump motion to the specified value with random to bypass value patches
     */
    public void jumpRandom(final double motion) {
        mc.player.motionY = motion + (Math.random() / 500);
    }


    /**
     * Stops the player from moving
     */
    public void stop() {
        mc.player.motionX = 0;
        mc.player.motionZ = 0;
    }

    /**
     * Gets the players' movement yaw
     */

    /**
     * Gets the players' movement yaw
     */
    public double direction(float rotationYaw, final double moveForward, final double moveStrafing) {
        if (moveForward < 0F) rotationYaw += 180F;

        float forward = 1F;

        if (moveForward < 0F) forward = -0.5F;
        else if (moveForward > 0F) forward = 0.5F;

        if (moveStrafing > 0F) rotationYaw -= 90F * forward;
        if (moveStrafing < 0F) rotationYaw += 90F * forward;

        return Math.toRadians(rotationYaw);
    }

    /**
     * Used to get the players speed
     */
    public double speed() {
        return Math.hypot(mc.player.motionX, mc.player.motionZ);
    }

    /**
     * Fixes the players movement
     */
    public void fixMovement(final EventMoveInput event, final float yaw) {
        final float forward = event.getForward();
        final float strafe = event.getStrafe();

        final double angle = MathHelper.wrapDegrees(Math.toDegrees(MoveUtil.direction(mc.player.rotationYaw, forward, strafe)));

        if (forward == 0 && strafe == 0) {
            return;
        }

        float closestForward = 0, closestStrafe = 0, closestDifference = Float.MAX_VALUE;

        for (float predictedForward = -1F; predictedForward <= 1F; predictedForward += 1F) {
            for (float predictedStrafe = -1F; predictedStrafe <= 1F; predictedStrafe += 1F) {
                if (predictedStrafe == 0 && predictedForward == 0) continue;

                final double predictedAngle = MathHelper.wrapDegrees(Math.toDegrees(MoveUtil.direction(yaw, predictedForward, predictedStrafe)));
                final double difference = Math.abs(angle - predictedAngle);

                if (difference < closestDifference) {
                    closestDifference = (float) difference;
                    closestForward = predictedForward;
                    closestStrafe = predictedStrafe;
                }
            }
        }

        event.setForward(closestForward);
        event.setStrafe(closestStrafe);
    }

    public double getMCFriction() {
        float f = 0.91F;

        if (mc.player.onGround) {
            f = mc.world.getBlockState(new BlockPos(MathHelper.floor(mc.player.posX), MathHelper.floor(mc.player.getEntityBoundingBox().minY) - 1, MathHelper.floor(mc.player.posZ))).getBlock().slipperiness * 0.91F;
        }

        return f;
    }

    public static double direction() {
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

    public static void strafe(final double speed) {
        final double yaw = direction();
        mc.player.motionX = -MathHelper.sin((float) yaw) * speed;
        mc.player.motionZ = MathHelper.cos((float) yaw) * speed;
    }
}

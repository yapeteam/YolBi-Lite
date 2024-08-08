package cn.yapeteam.yolbi.module.impl.world;

import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.player.EventUpdate;
import cn.yapeteam.yolbi.managers.RotationManager;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.ModuleCategory;
import cn.yapeteam.yolbi.utils.player.EnumFacingOffset;
import cn.yapeteam.yolbi.utils.player.PlayerUtil;
import cn.yapeteam.yolbi.utils.player.RayCastUtil;
import cn.yapeteam.yolbi.utils.player.RotationsUtil;
import cn.yapeteam.yolbi.utils.vector.Vector2f;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;

public class Scaffold extends Module {
    private MovingObjectPosition rayCasted = null;
    private boolean forceStrict = false;
    private float placeYaw;
    private float placePitch = 85;

    public Scaffold() {
        super("Scaffold", ModuleCategory.WORLD);
    }

    @Override
    protected void onEnable() {
        forceStrict = false;
        placeYaw = mc.thePlayer.rotationYaw + 180;
    }

    @Listener
    private void onPreUpdate(@NotNull EventUpdate event) {
        if (!event.isPre()) return;

        searchPlace();

        if (forceStrict) {
            RotationManager.setRotations(new Vector2f(placeYaw, placePitch), 60);
        } else {
            RotationManager.setRotations(new Vector2f(mc.thePlayer.rotationYaw + 180, 85), 60);
        }

        RotationManager.smooth();

        if (rayCasted != null) {
            mc.playerController.onPlayerRightClick(
                    mc.thePlayer, mc.theWorld,
                    mc.thePlayer.getHeldItem(),
                    rayCasted.getBlockPos(), rayCasted.sideHit, rayCasted.hitVec
            );
            mc.thePlayer.swingItem();
        }
    }

    private void searchPlace() {
        rayCasted = null;
        ItemStack heldItem = mc.thePlayer.getHeldItem();
        float searchYaw = 35;
        float[] searchPitch = new float[]{72, 12};
        Vec3 targetVec3 = PlayerUtil.getPlacePossibility();
        BlockPos targetPos = new BlockPos(targetVec3);
        float[] targetRotation = RotationsUtil.getRotationsToPosition(targetPos.getX() + 0.45, targetPos.getY() + 0.45, targetPos.getZ() + 0.45);
        EnumFacingOffset enumFacing = getEnumFacing(targetVec3);
        if (enumFacing == null) {
            return;
        }
        targetPos = targetPos.add(enumFacing.getOffset().xCoord, enumFacing.getOffset().yCoord, enumFacing.getOffset().zCoord);

        for (int i = 0; i < 2; i++) {
            if (i == 1 && overPlaceable(-1)) {
                searchYaw = 180;
                searchPitch = new float[]{65, 25};
            }
            for (float checkYaw : generateSearchSequence(searchYaw)) {
                float playerYaw = isDiagonal() ? mc.thePlayer.rotationYaw + 180 : targetRotation[0];
                float fixedYaw = (float) (playerYaw - checkYaw + getRandom());
                double deltaYaw = Math.abs(playerYaw - fixedYaw);
                if (i == 1 && (inBetween(75, 95, (float) deltaYaw)) || deltaYaw > 500) {
                    continue;
                }
                for (float checkPitch : generateSearchSequence(searchPitch[1])) {
                    float fixedPitch = clampTo90((float) (targetRotation[1] + checkPitch + getRandom()));
                    MovingObjectPosition raycast = RayCastUtil.rayTraceCustom(mc.thePlayer, mc.playerController.getBlockReachDistance(), fixedYaw, fixedPitch);
                    if (raycast != null) {
                        if (raycast.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                            if (raycast.getBlockPos().equals(targetPos) && raycast.sideHit == enumFacing.getEnumFacing()) {
                                if (rayCasted == null || !raycast.getBlockPos().equals(rayCasted.getBlockPos())) {
                                    if (heldItem != null && heldItem.getItem() instanceof ItemBlock && ((ItemBlock) heldItem.getItem()).canPlaceBlockOnSide(mc.theWorld, raycast.getBlockPos(), raycast.sideHit, mc.thePlayer, heldItem)) {
                                        if (rayCasted == null) {
                                            forceStrict = (forceStrict(checkYaw)) && i == 1;
                                            rayCasted = raycast;
                                            placeYaw = fixedYaw;
                                            placePitch = fixedPitch;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (rayCasted != null) {
                break;
            }
        }
    }

    public float[] generateSearchSequence(float value) {
        int length = (int) value * 2;
        float[] sequence = new float[length + 1];

        int index = 0;
        sequence[index++] = 0;

        for (int i = 1; i <= value; i++) {
            sequence[index++] = i;
            sequence[index++] = -i;
        }

        return sequence;
    }

    private boolean isDiagonal() {
        float yaw = ((mc.thePlayer.rotationYaw % 360) + 360) % 360 > 180 ? ((mc.thePlayer.rotationYaw % 360) + 360) % 360 - 360 : ((mc.thePlayer.rotationYaw % 360) + 360) % 360;
        return (yaw >= -170 && yaw <= 170) && !(yaw >= -10 && yaw <= 10) && !(yaw >= 80 && yaw <= 100) && !(yaw >= -100 && yaw <= -80) || Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode()) || Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode());
    }

    private double getRandom() {
        return (Math.random() * 90) / 100.0;
    }

    public static boolean overPlaceable(double yOffset) {
        BlockPos playerPos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + yOffset, mc.thePlayer.posZ);
        return PlayerUtil.replaceable(playerPos) || PlayerUtil.isFluid(PlayerUtil.block(playerPos));
    }

    private boolean inBetween(float min, float max, float value) {
        return value >= min && value <= max;
    }

    public static float clampTo90(final float n) {
        return MathHelper.clamp_float(n, -90.0f, 90.0f);
    }

    private @Nullable EnumFacingOffset getEnumFacing(final Vec3 position) {
        for (int x2 = -1; x2 <= 1; x2 += 2) {
            if (!PlayerUtil.block(position.xCoord + x2, position.yCoord, position.zCoord).getMaterial().isReplaceable()) {
                if (x2 > 0) {
                    return new EnumFacingOffset(EnumFacing.WEST, new Vec3(x2, 0, 0));
                } else {
                    return new EnumFacingOffset(EnumFacing.EAST, new Vec3(x2, 0, 0));
                }
            }
        }

        for (int y2 = -1; y2 <= 1; y2 += 2) {
            if (!PlayerUtil.block(position.xCoord, position.yCoord + y2, position.zCoord).getMaterial().isReplaceable()) {
                if (y2 < 0) {
                    return new EnumFacingOffset(EnumFacing.UP, new Vec3(0, y2, 0));
                }
            }
        }

        for (int z2 = -1; z2 <= 1; z2 += 2) {
            if (!PlayerUtil.block(position.xCoord, position.yCoord, position.zCoord + z2).getMaterial().isReplaceable()) {
                if (z2 < 0) {
                    return new EnumFacingOffset(EnumFacing.SOUTH, new Vec3(0, 0, z2));
                } else {
                    return new EnumFacingOffset(EnumFacing.NORTH, new Vec3(0, 0, z2));
                }
            }
        }

        return null;
    }

    private boolean forceStrict(float value) {
        return (inBetween(-170, -105, value) || inBetween(-80, 80, value) || inBetween(98, 170, value)) && !inBetween(-10, 10, value);
    }
}

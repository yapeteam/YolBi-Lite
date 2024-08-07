package cn.yapeteam.yolbi.module.impl.movement;

import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.network.EventPacketReceive;
import cn.yapeteam.yolbi.event.impl.player.EventMotion;
import cn.yapeteam.yolbi.event.impl.player.EventUpdate;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.ModuleCategory;
import cn.yapeteam.yolbi.module.impl.player.SlotHandler;
import cn.yapeteam.yolbi.module.values.impl.BooleanValue;
import cn.yapeteam.yolbi.module.values.impl.ModeValue;
import cn.yapeteam.yolbi.module.values.impl.NumberValue;

import cn.yapeteam.yolbi.utils.math.MathUtils;
import cn.yapeteam.yolbi.utils.misc.TimerUtil;
import cn.yapeteam.yolbi.utils.player.PlayerUtil;
import net.minecraft.block.Block;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.*;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class Scaffold extends Module {
    private final ModeValue<String> clickMode = new ModeValue<>("Click mode", "Basic", "Basic", "Normal");

    private final NumberValue<Double> aimSpeed = new NumberValue<>("Aim speed", 20.0, 5.0, 20.0, 0.1);
    private final NumberValue<Double> motion = new NumberValue<>("Motion", 1.0, 0.5, 1.2, 0.01);
    private final ModeValue rotation = new ModeValue("Rotation", new String[]{"None", "Backwards", "Strict", "Precise", "Telly", "Constant", "Snap"}, 1);
    private final BooleanValue moveFix = new BooleanValue("MoveFix", false);
    private final NumberValue<Double> tellyStartTick = new NumberValue<>("Telly start", 3.0, 0.0, 11.0, 1.0);
    private final NumberValue<Double> tellyStopTick = new NumberValue<>("Telly stop", 8.0, 0.0, 11.0, 1.0);
    private final NumberValue<Double> strafe = new NumberValue<>("Strafe", 0.0, -45.0, 45.0, 5.0);
    private final ModeValue fastScaffold = new ModeValue("Fast scaffold", new String[]{"Disabled", "Sprint", "Edge", "Jump A", "Jump B", "Jump C", "Float", "Side", "Legit", "GrimAC", "Sneak", "Star"}, 0);
    private final BooleanValue cancelSprint = new BooleanValue("Cancel sprint", false);
    private final BooleanValue rayCast = new BooleanValue("Ray cast", false);
    private final BooleanValue recycleRotation = new BooleanValue("Recycle rotation", false);
    private final BooleanValue sneak = new BooleanValue("Sneak", false);
    private final NumberValue<Double> sneakEveryBlocks = new NumberValue<>("Sneak every blocks", 0.0, 1.0, 10.0, 1.0);
    private final NumberValue<Double> sneakTime = new NumberValue<>("Sneak time", 50.0, 0.0, 500.0, 10.0);
    private final BooleanValue jump = new BooleanValue("Jump", false);
    private final NumberValue<Double> jumpEveryBlocks = new NumberValue<>("Jump every blocks", 0.0, 1.0, 10.0, 1.0);
    private final BooleanValue rotateWithMovement = new BooleanValue("Rotate with movement", true);
    private final BooleanValue staticYaw = new BooleanValue("Static yaw", false);
    private final BooleanValue reserveYaw = new BooleanValue("Reserve yaw", false);
    private final BooleanValue staticPitch = new BooleanValue("Static pitch", false);
    private final BooleanValue staticPitchOnJump = new BooleanValue("Static pitch on jump", false);
    private final NumberValue<Double> straightPitch = new NumberValue<>("Straight pitch", 75.7, 45.0, 90.0, 0.1);
    private final NumberValue<Double> diagonalPitch = new NumberValue<>("Diagonal pitch", 75.6, 45.0, 90.0, 0.1);
    private final ModeValue precision = new ModeValue("Precision", new String[]{"Very low", "Low", "Moderate", "High", "Very high"}, 4);
    private final BooleanValue autoSwap = new BooleanValue("AutoSwap", true);
    private final BooleanValue useBiggestStack = new BooleanValue("Use biggest stack", true);
    private final BooleanValue fastOnRMB = new BooleanValue("Fast on RMB", false);
    private final BooleanValue highlightBlocks = new BooleanValue("Highlight blocks", true);
    private final BooleanValue multiPlace = new BooleanValue("Multi-place", false);
    public final BooleanValue safeWalk = new BooleanValue("Safewalk", true);
    private final BooleanValue showBlockCount = new BooleanValue("Show block count", true);
    private final BooleanValue delayOnJump = new BooleanValue("Delay on jump", true);
    private final BooleanValue silentSwing = new BooleanValue("Silent swing", false);
    public final BooleanValue tower = new BooleanValue("Tower", false);
    public final BooleanValue fast = new BooleanValue("Fast", false);
    public final BooleanValue sameY = new BooleanValue("SameY", false);
    public final BooleanValue autoJump = new BooleanValue("Auto jump", false);
    private final BooleanValue expand = new BooleanValue("Expand", false);
    private final NumberValue<Double> expandDistance = new NumberValue<>("Expand distance", 4.5, 0.0, 10.0, 0.1);
    private final BooleanValue polar = new BooleanValue("Polar", false);

    public MovingObjectPosition placeBlock;
    private int lastSlot;
    private static final String[] rotationModes = new String[]{"None", "Backwards", "Strict", "Precise", "Telly", "Constant", "Snap"};
    private static final String[] fastScaffoldModes = new String[]{"Disabled", "Sprint", "Edge", "Jump A", "Jump B", "Jump C", "Float", "Side", "Legit", "GrimAC", "Sneak", "Star"};
    private static final String[] precisionModes = new String[]{"Very low", "Low", "Moderate", "High", "Very high"};
    public float placeYaw;
    public float placePitch = 85;
    public int at;
    public int index;
    public boolean rmbDown;
    private double startPos = -1;
    private final Map<BlockPos, TimerUtil> highlight = new HashMap<>();
    private boolean forceStrict;
    private boolean down;
    private boolean delay;
    private boolean place;
    private int add = 0;
    private int sameY$bridged = 1;
    private int sneak$bridged = 0;
    private int jump$bridged = 0;
    private boolean placedUp;
    private int offGroundTicks = 0;
    private boolean telly$noBlockPlace = false;
    public boolean tower$noBlockPlace = false;
    private Float lastYaw = null, lastPitch = null;
    private boolean polar$waitingForExpand = false;
    TimerUtil timer = new TimerUtil();

    protected Scaffold() {
        super("Scaffold", ModuleCategory.MOVEMENT);
        addValues(clickMode, aimSpeed, motion, rotation, moveFix, tellyStartTick, tellyStopTick, strafe, fastScaffold, cancelSprint, rayCast, recycleRotation, sneak, sneakEveryBlocks, sneakTime, jump, jumpEveryBlocks, rotateWithMovement, staticYaw, reserveYaw, staticPitch, staticPitchOnJump, straightPitch, diagonalPitch, precision, autoSwap, useBiggestStack, fastOnRMB, highlightBlocks, multiPlace, safeWalk, showBlockCount, delayOnJump, silentSwing, tower, fast, sameY, autoJump, expand, expandDistance, polar);
    }

    @Override
    public void onDisable() {

        placeBlock = null;
        if (lastSlot != -1) {
            SlotHandler.setCurrentSlot(lastSlot);
            lastSlot = -1;
        }
        delay = false;
        highlight.clear();
        at = index = 0;
        add = 0;
        startPos = -1;
        forceStrict = false;
        down = false;
        place = false;
        placedUp = false;
        sameY$bridged = 1;
        offGroundTicks = 0;
        telly$noBlockPlace = false;
        tower$noBlockPlace = false;
        lastYaw = lastPitch = null;
        polar$waitingForExpand = false;
        timer.reset();
    }

    @Override
    public void onEnable() {
        lastSlot = -1;
        startPos = mc.thePlayer.posY;
        sneak$bridged = 0;
        jump$bridged = 0;
    }

    @Listener
    public void onPreUpdate(EventUpdate eventUpdate){

    }

    @Listener
    public void onPacketReceive(EventPacketReceive eventPacketReceive){

    }

    @Listener
    public void onPreMotion(EventMotion eventMotion){

    }

    public Vec3 getPlacePossibility(double offsetY, double original) { // rise
        List<Vec3> possibilities = new ArrayList<>();
        int range = 5;
        for (int x = -range; x <= range; ++x) {
            for (int y = -range; y <= range; ++y) {
                for (int z = -range; z <= range; ++z) {
                    final Block block = PlayerUtil.blockRelativeToPlayer(x, y, z);
                    if (!block.getMaterial().isReplaceable()) {
                        for (int x2 = -1; x2 <= 1; x2 += 2) {
                            possibilities.add(new Vec3(mc.thePlayer.posX + x + x2, mc.thePlayer.posY + y, mc.thePlayer.posZ + z));
                        }
                        for (int y2 = -1; y2 <= 1; y2 += 2) {
                            possibilities.add(new Vec3(mc.thePlayer.posX + x, mc.thePlayer.posY + y + y2, mc.thePlayer.posZ + z));
                        }
                        for (int z2 = -1; z2 <= 1; z2 += 2) {
                            possibilities.add(new Vec3(mc.thePlayer.posX + x, mc.thePlayer.posY + y, mc.thePlayer.posZ + z + z2));
                        }
                    }
                }
            }
        }

        possibilities.removeIf(vec3 -> mc.thePlayer.getDistance(vec3.xCoord, vec3.yCoord, vec3.zCoord) > 5);

        if (possibilities.isEmpty()) {
            return null;
        }
        possibilities.sort(Comparator.comparingDouble(vec3 -> {
            final double d0 = (mc.thePlayer.posX) - vec3.xCoord;
            final double d1 = ((keepYPosition() ? original : mc.thePlayer.posY) - 1 + offsetY) - vec3.yCoord;
            final double d2 = (mc.thePlayer.posZ) - vec3.zCoord;
            return MathHelper.sqrt_double(d0 * d0 + d1 * d1 + d2 * d2);
        }));

        return possibilities.get(0);
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

    public boolean sprint() {
        if (this.isEnabled()
                && fastScaffold.getValue() > 0
                && this.placeBlock != null
                && (!this.fastOnRMB.getValue() || Mouse.isButtonDown(1))) {
            switch ((int) this.fastScaffold.getValue()) {
                case 1:
                case 7:
                case 9:
                case 10:
                    return true;
                case 2:
                    return Utils.onEdge();
                case 3:
                case 4:
                case 5:
                case 6:
                case 11:
                    return keepYPosition();
                case 8:
                    return Math.abs(MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw) - MathHelper.wrapAngleTo180_float(RotationHandler.getRotationYaw())) <= 45;
            }
        }
        return false;
    }

    private boolean forceStrict(float value) {
        return (inBetween(-170, -105, value) || inBetween(-80, 80, value) || inBetween(98, 170, value)) && !inBetween(-10, 10, value);
    }

    private boolean keepYPosition() {
        boolean sameYSca = fastScaffold.getValue() == 4 || fastScaffold.getValue() == 3 || fastScaffold.getValue() == 5 || fastScaffold.getValue() == 6 || fastScaffold.getValue() == 11;
        return this.isEnabled() && Utils.keysDown() && (sameYSca || (sameY.getValue() && !Utils.jumpDown())) && (!Utils.jumpDown() || fastScaffold.getValue() == 6) && (!fastOnRMB.getValue() || Mouse.isButtonDown(1));
    }

    public boolean safewalk() {
        return this.isEnabled() && safeWalk.getValue() && (!keepYPosition() || fastScaffold.getValue() == 3);
    }

    public boolean stopRotation() {
        return this.isEnabled() && (rotation.getValue() <= 1 || (rotation.getValue() == 2 && placeBlock != null));
    }

    private boolean inBetween(float min, float max, float value) {
        return value >= min && value <= max;
    }

    private double getRandom() {
        return MathUtils.getRandom(-90, 90) / 100.0;
    }

    public float getYaw() {
        float yaw = 180.0f;
        double moveForward = mc.thePlayer.movementInput.moveForward;
        double moveStrafe = mc.thePlayer.movementInput.moveStrafe;
        if (rotateWithMovement.getValue()) {
            if (moveForward > 0.0) {
                if (moveStrafe > 0.0) {
                    yaw = 135.0f;
                } else if (moveStrafe < 0.0) {
                    yaw = -135.0f;
                }
            } else if (moveForward < 0.0) {
                if (moveStrafe > 0.0) {
                    yaw = 45.0f;
                } else if (moveStrafe < 0.0) {
                    yaw = -45.0f;
                } else {
                    yaw = 0.0f;
                }
            } else {
                if (moveStrafe > 0.0) {
                    yaw = 90.0f;
                }
                else if (moveStrafe < 0.0) {
                    yaw = -90.0f;
                }
            }
        }

        return mc.thePlayer.rotationYaw + yaw;
    }

    private @Nullable EnumFacingOffset getEnumFacing(final Vec3 position) {
        for (int x2 = -1; x2 <= 1; x2 += 2) {
            if (!BlockUtils.getBlock(position.xCoord + x2, position.yCoord, position.zCoord).getMaterial().isReplaceable()) {
                if (x2 > 0) {
                    return new EnumFacingOffset(EnumFacing.WEST, new Vec3(x2, 0, 0));
                } else {
                    return new EnumFacingOffset(EnumFacing.EAST, new Vec3(x2, 0, 0));
                }
            }
        }

        for (int y2 = -1; y2 <= 1; y2 += 2) {
            if (!BlockUtils.getBlock(position.xCoord, position.yCoord + y2, position.zCoord).getMaterial().isReplaceable()) {
                if (y2 < 0) {
                    return new EnumFacingOffset(EnumFacing.UP, new Vec3(0, y2, 0));
                }
            }
        }

        for (int z2 = -1; z2 <= 1; z2 += 2) {
            if (!BlockUtils.getBlock(position.xCoord, position.yCoord, position.zCoord + z2).getMaterial().isReplaceable()) {
                if (z2 < 0) {
                    return new EnumFacingOffset(EnumFacing.SOUTH, new Vec3(0, 0, z2));
                } else {
                    return new EnumFacingOffset(EnumFacing.NORTH, new Vec3(0, 0, z2));
                }
            }
        }

        return null;
    }

    public void place(MovingObjectPosition block, boolean extra) {
        if (rotation.getValue() == 4 && telly$noBlockPlace) return;
        if (tower$noBlockPlace) {
            tower$noBlockPlace = false;
            return;
        }

        if (sneak.getValue()) {
            if (sneak$bridged >= sneakEveryBlocks.getValue()) {
                sneak$bridged = 0;
                ((KeyBindingAccessor) mc.gameSettings.keyBindSneak).setPressed(true);
                Raven.getExecutor().schedule(() -> ((KeyBindingAccessor) mc.gameSettings.keyBindSneak).setPressed(Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode())), (long) sneakTime.getValue(), TimeUnit.MILLISECONDS);
            }
        }

        if (jump.getValue()) {
            if (jump$bridged >= jumpEveryBlocks.getValue()) {
                jump$bridged = 0;
                if (mc.thePlayer.onGround)
                    mc.thePlayer.jump();
            }
        }

        ItemStack heldItem = SlotHandler.getHeldItem();
        if (heldItem == null || !(heldItem.getItem() instanceof ItemBlock)) {
            return;
        }

        if (rayCast.getValue()) {
            MovingObjectPosition hitResult = RotationUtils.rayCast(4.5, placeYaw, placePitch);
            if (hitResult != null && hitResult.getBlockPos().equals(block.getBlockPos())) {
                block.sideHit = hitResult.sideHit;
                block.hitVec = hitResult.hitVec;
            } else {
                return;
            }
        }

        ScaffoldPlaceEvent event = new ScaffoldPlaceEvent(block, extra);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) return;

        block = event.getHitResult();
        extra = event.isExtra();

        if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, heldItem, block.getBlockPos(), block.sideHit, block.hitVec)) {
            sneak$bridged++;
            jump$bridged++;
            if (silentSwing.getValue()) {
                mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
            }
            else {
                mc.thePlayer.swingItem();
                mc.getItemRenderer().resetEquippedProgress();
            }
            if (!extra) {
                highlight.put(block.getBlockPos().offset(block.sideHit), null);
            }
        }
    }

    public static int getSlot() {
        int slot = -1;
        int highestStack = -1;
        for (int i = 0; i < 9; ++i) {
            final ItemStack itemStack = mc.thePlayer.inventory.mainInventory[i];
            if (itemStack != null && itemStack.getItem() instanceof ItemBlock && ContainerUtils.canBePlaced((ItemBlock) itemStack.getItem()) && itemStack.stackSize > 0) {
                if (mc.thePlayer.inventory.mainInventory[i].stackSize > highestStack) {
                    highestStack = mc.thePlayer.inventory.mainInventory[i].stackSize;
                    slot = i;
                }
            }
        }
        return slot;
    }

    public int totalBlocks() {
        int totalBlocks = 0;
        for (int i = 0; i < 9; ++i) {
            final ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
            if (stack != null && stack.getItem() instanceof ItemBlock && ContainerUtils.canBePlaced((ItemBlock) stack.getItem()) && stack.stackSize > 0) {
                totalBlocks += stack.stackSize;
            }
        }
        return totalBlocks;
    }

    static class EnumFacingOffset {
        EnumFacing enumFacing;
        Vec3 offset;

        EnumFacingOffset(EnumFacing enumFacing, Vec3 offset) {
            this.enumFacing = enumFacing;
            this.offset = offset;
        }

        EnumFacing getEnumFacing() {
            return enumFacing;
        }

        Vec3 getOffset() {
            return offset;
        }
    }


    private void starScaffold() {
        ItemStack heldItem = mc.thePlayer.getHeldItem();
        if (getSlot() != -1 || heldItem != null && heldItem.getItem() instanceof ItemBlock) {

            if (this.keepYPosition() && !this.down) {
                this.startPos = Math.floor(mc.thePlayer.posY);
                this.down = true;
            } else if (!this.keepYPosition()) {
                this.down = false;
                this.placedUp = false;
            }

            if (mc.thePlayer.onGround && MoveUtil.isMoving()) {
                mc.thePlayer.jump();
                this.add = 0;
            }

            double original = this.startPos;
            if (this.groundDistance() > 0.0 && mc.thePlayer.posY >= Math.floor(mc.thePlayer.posY) && mc.thePlayer.fallDistance > 0.0F) {
                ++original;
            }

            Vec3 targetVec3 = this.getPlacePossibility(0.0, original);
            if (targetVec3 != null) {
                BlockPos targetPos = new BlockPos(targetVec3.xCoord, targetVec3.yCoord, targetVec3.zCoord);
                if (heldItem != null && heldItem.getItem() instanceof ItemBlock) {
                    MovingObjectPosition rayCasted = null;
                    float searchYaw = 25.0F;
                    EnumFacingOffset enumFacing = this.getEnumFacing(targetVec3);
                    if (enumFacing != null) {
                        targetPos = targetPos.add(enumFacing.getOffset().xCoord, enumFacing.getOffset().yCoord, enumFacing.getOffset().zCoord);
                        float[] targetRotation = new float[]{PlayerRotation.getYaw(targetPos), PlayerRotation.getPitch(targetPos)};
                        float[] searchPitch = new float[]{78.0F, 59.0F};

                        for(int i = 0; i < 2; ++i) {
                            if (i == 1 && Utils.overPlaceable(-1.0)) {
                                searchYaw = 180.0F;
                                searchPitch = new float[]{65.0F, 25.0F};
                            } else if (i == 1) {
                                break;
                            }

                            float[] var13 = this.generateSearchSequence(searchYaw);

                            for (float checkYaw : var13) {
                                float playerYaw = this.isDiagonal() ? getYaw() : targetRotation[0];
                                float fixedYaw = (float) ((double) (playerYaw - checkYaw) + this.getRandom());
                                double deltaYaw = Math.abs(playerYaw - fixedYaw);
                                if ((i != 1 || !this.inBetween(75.0F, 95.0F, (float) deltaYaw)) && !(deltaYaw > 500.0)) {
                                    float[] var21 = this.generateSearchSequence(searchPitch[1]);

                                    for (float checkPitch : var21) {
                                        float fixedPitch = MathUtils.clampTo90((float) ((double) (targetRotation[1] + checkPitch) + this.getRandom()));
                                        MovingObjectPosition raycast = RotationUtils.rayTraceCustom(mc.playerController.getBlockReachDistance(), fixedYaw, fixedPitch);
                                        if (raycast != null && raycast.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && raycast.getBlockPos().equals(targetPos) && raycast.sideHit == enumFacing.getEnumFacing() && (rayCasted == null || !BlockUtils.isSamePos(raycast.getBlockPos(), rayCasted.getBlockPos())) && ((ItemBlock) heldItem.getItem()).canPlaceBlockOnSide(mc.theWorld, raycast.getBlockPos(), raycast.sideHit, mc.thePlayer, heldItem) && rayCasted == null) {
                                            this.forceStrict = this.forceStrict(checkYaw) && i == 1;

                                            rayCasted = raycast;
                                            this.placeYaw = fixedYaw;
                                            this.placePitch = fixedPitch;
                                            break;
                                        }
                                    }
                                }
                            }

                            if (rayCasted != null) {
                                break;
                            }
                        }

                        if (rayCasted != null) {
                            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
                            this.placeBlock = rayCasted;
                            this.place(this.placeBlock, false);
                            this.place = false;
                            if (this.placeBlock.sideHit == EnumFacing.UP && this.keepYPosition()) {
                                this.placedUp = true;
                            }
                        }
                    }
                }
            }
        }
    }
}

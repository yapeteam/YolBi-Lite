package cn.yapeteam.yolbi.utils;

import cn.yapeteam.yolbi.managers.ReflectionManager;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.utils.player.PlayerUtil;
import cn.yapeteam.yolbi.utils.player.RotationsUtil;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockSign;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.item.*;
import net.minecraft.network.play.client.C03PacketPlayer.C05PacketPlayerLook;
import net.minecraft.potion.Potion;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.*;
import net.minecraft.util.Timer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.List;
import java.util.Queue;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class Utils {
    private static final Random rand = new Random();
    public static final Minecraft mc = Minecraft.getMinecraft();
    public static final ChatComponentText PREFIX = new ChatComponentText("&7[&dR&7]&r ");
    public static HashSet<String> friends = new HashSet<>();
    public static HashSet<String> enemies = new HashSet<>();
    public static final Logger log = LogManager.getLogger();
    @Getter
    public static ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);

    public static boolean addEnemy(String name) {
        if (enemies.add(name.toLowerCase())) {
            Utils.sendMessage("&7Added &cenemy&7: &b" + name);
            return true;
        }
        return false;
    }

    public static boolean overVoid(double posX, double posY, double posZ) {
        for (int i = (int) posY; i > -1; i--) {
            if (!(mc.theWorld.getBlockState(new BlockPos(posX, i, posZ)).getBlock() instanceof BlockAir)) {
                return false;
            }
        }
        return true;
    }

    public static boolean overVoid() {
        return overVoid(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
    }

    public static List<NetworkPlayerInfo> getTablist() {
        final ArrayList<NetworkPlayerInfo> list = new ArrayList<>(mc.getNetHandler().getPlayerInfoMap());
        removeDuplicates(list);
        list.remove(mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID()));
        return list;
    }

    public static double getFallDistance(Entity entity) {
        double fallDist = -1;
        Vec3 pos = new Vec3(entity.posX, entity.posY, entity.posZ);
        int y = (int) Math.floor(pos.yCoord);
        if (pos.yCoord % 1 == 0) y--;
        for (int i = y; i > -1; i--) {
            Block block = PlayerUtil.block(new BlockPos((int) Math.floor(pos.xCoord), i, (int) Math.floor(pos.zCoord)));
            if (!(block instanceof BlockAir) && !(block instanceof BlockSign)) {
                fallDist = y - i;
                break;
            }
        }
        return fallDist;
    }

    public static void removeDuplicates(final ArrayList list) {
        final HashSet set = new HashSet(list);
        list.clear();
        list.addAll(set);
    }

    public static boolean removeFriend(String name) {
        if (friends.remove(name.toLowerCase())) {
            Utils.sendMessage("&7Removed &afriend&7: &b" + name);
            return true;
        }
        return false;
    }

    public static boolean addFriend(String name) {
        if (friends.add(name.toLowerCase())) {
            Utils.sendMessage("&7Added &afriend&7: &b" + name);
            return true;
        }
        return false;
    }

    public static boolean isWholeNumber(double num) {
        return num == Math.floor(num);
    }

    public static int randomizeInt(double min, double max) {
        return (int) randomizeDouble(min, max);
    }

    public static double randomizeDouble(double min, double max) {
        return Math.random() * (max - min) + min;
    }

    public static boolean inFov(float fov, @NotNull BlockPos blockPos) {
        return inFov(fov, blockPos.getX(), blockPos.getZ());
    }

    public static boolean inFov(float fov, @NotNull Entity entity) {
        return inFov(fov, entity.posX, entity.posZ);
    }

    public static boolean inFov(float fov, @NotNull Entity self, @NotNull Entity target) {
        return inFov(self.rotationYaw, fov, target.posX, target.posZ);
    }

    public static boolean inFov(float fov, final double n2, final double n3) {
        fov *= 0.5F;
        final double fovToPoint = getFov(n2, n3);
        if (fovToPoint > 0.0) {
            return fovToPoint < fov;
        } else return fovToPoint > -fov;
    }

    public static boolean inFov(float yaw, float fov, final double n2, final double n3) {
        fov *= 0.5F;
        final double fovToPoint = getFov(yaw, n2, n3);
        if (fovToPoint > 0.0) {
            return fovToPoint < fov;
        } else return fovToPoint > -fov;
    }

    public static @Range(from = -180, to = 180) double getFov(final double posX, final double posZ) {
        return getFov(mc.thePlayer.rotationYaw, posX, posZ);
    }

    public static @Range(from = -180, to = 180) double getFov(final float yaw, final double posX, final double posZ) {
        return MathHelper.wrapAngleTo180_double((yaw - RotationsUtil.getRotationsToPosition(posX, 0, posZ)[0]) % 360.0f);
    }

    private static final Queue<String> delayedMessage = new ConcurrentLinkedQueue<>();

    public static void sendMessage(String txt) {
        if (nullCheck()) {
            String m = formatColor("&7[&dR&7]&r " + replace(txt));
            mc.thePlayer.addChatMessage(new ChatComponentText(m));
        }
    }

    public static @Nullable String replace(String string) {
        if (string == null)
            return null;
//        if (ModuleManager.nameHider != null && ModuleManager.nameHider.isEnabled()) {
//            string = NameHider.getFakeName(string);
//        }
//        if (ModuleManager.antiShuffle != null && ModuleManager.antiShuffle.isEnabled()) {
//            string = AntiShuffle.removeObfuscation(string);
//        }
//        if (ModuleManager.language != null && ModuleManager.language.isEnabled()) {
//            List<Map<String, String>> replaceMap = I18nManager.REPLACE_MAP;
//            int index = (int) ModuleManager.language.mode.getInput();
//            if (replaceMap.size() > index)
//                string = replaceMap.get(index).getOrDefault(string, string);
//        }

        return string;
    }

    public static void sendMessageAnyWay(String txt) {
        if (nullCheck()) {
            sendMessage(txt);
        } else {
            delayedMessage.add(txt);
        }
    }

    static {
        getExecutor().scheduleWithFixedDelay(() -> {
            if (Utils.nullCheck() && !delayedMessage.isEmpty()) {
                for (String s : delayedMessage) {
                    sendMessage(s);
                }
                delayedMessage.clear();
            }
        }, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    public static void sendDebugMessage(String message) {
        if (nullCheck()) {
            mc.thePlayer.addChatMessage(new ChatComponentText("§7[§dR§7]§r " + message));
        }
    }

    public static void attackEntity(Entity e, boolean clientSwing) {
//        boolean attack = HitSelect.canAttack();
//        if (clientSwing) {
//            if (attack || HitSelect.canSwing())
                mc.thePlayer.swingItem();
//        } else {
//            if (attack || HitSelect.canSwing()) mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
//        }
//        if (attack)
            mc.playerController.attackEntity(mc.thePlayer, e);
    }

    public static void attackEntityNoSwing(Entity e) {
//        if (HitSelect.canAttack())
            mc.playerController.attackEntity(mc.thePlayer, e);
    }

    public static void sendRawMessage(String txt) {
        if (nullCheck()) {
            mc.thePlayer.addChatMessage(new ChatComponentText(formatColor(txt)));
        }
    }

    public static float getCompleteHealth(EntityLivingBase entity) {
        if (entity == null) return 0;
        return entity.getHealth() + entity.getAbsorptionAmount();
    }

    public static String getHealthStr(EntityLivingBase entity) {
        float completeHealth = getCompleteHealth(entity);
        return getColorForHealth(entity.getHealth() / entity.getMaxHealth(), completeHealth);
    }

    public static int getTool(Block block) {
        float n = 1.0f;
        int n2 = -1;
        for (int i = 0; i < InventoryPlayer.getHotbarSize(); ++i) {
            final ItemStack getStackInSlot = mc.thePlayer.inventory.getStackInSlot(i);
            if (getStackInSlot != null) {
                final float a = getEfficiency(getStackInSlot, block);
                if (a > n) {
                    n = a;
                    n2 = i;
                }
            }
        }
        return n2;
    }

    public static int getWeapon() {
        int weaponIndex = -1;
        double highestPriority = 0.0;

        for (int i = 0; i < InventoryPlayer.getHotbarSize(); ++i) {
            ItemStack item = mc.thePlayer.inventory.getStackInSlot(i);
            if (item == null) continue;

            double priority = 0.0;
            double typePriority = 0.0;

//            if (Settings.weaponSword.isToggled() && item.getItem() instanceof ItemSword) {
                priority = getDamage(item);
                typePriority = 0.4;
//            } else if (Settings.weaponAxe.isToggled() && item.getItem() instanceof ItemAxe) {
//                priority = getDamage(item);
//                typePriority = 0.3;
//            } else if (Settings.weaponStick.isToggled() && item.getItem() == Items.stick) {
//                priority = getDamage(item);
//                typePriority = 0.2;
//            } else if (Settings.weaponRod.isToggled() && item.getItem() == Items.fishing_rod) {
//                priority = getDamage(item);
//                typePriority = 0.1;
//            }
            // The typePriority is added to ensure that if two weapons have the same damage,
            // the weapon with the higher typePriority (based on the predefined weapon order:
            // sword > axe > stick > rod) is selected. This helps in making a more informed
            // choice when weapons have identical damage values.
            priority += typePriority;

            if (priority > highestPriority) {
                highestPriority = priority;
                weaponIndex = i;
            }
        }

        return weaponIndex;
    }

    public static float getEfficiency(final ItemStack itemStack, final Block block) {
        float getStrVsBlock = itemStack.getStrVsBlock(block);
        if (getStrVsBlock > 1.0f) {
            final int getEnchantmentLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, itemStack);
            if (getEnchantmentLevel > 0) {
                getStrVsBlock += getEnchantmentLevel * getEnchantmentLevel + 1;
            }
        }
        return getStrVsBlock;
    }

    public static boolean isEnemy(EntityPlayer entityPlayer) {
        return !enemies.isEmpty() && enemies.contains(entityPlayer.getName().toLowerCase());
    }

    public static boolean isEnemy(String name) {
        return !enemies.isEmpty() && enemies.contains(name.toLowerCase());
    }

    public static String getColorForHealth(double n, double n2) {
        double health = rnd(n2, 1);
        return ((n < 0.3) ? "§c" : ((n < 0.5) ? "§6" : ((n < 0.7) ? "§e" : "§a"))) + (isWholeNumber(health) ? (int) health + "" : health);
    }

    public static int getColorForHealth(double health) {
        return ((health < 0.3) ? -43691 : ((health < 0.5) ? -22016 : ((health < 0.7) ? -171 : -11141291)));
    }

    @Contract(pure = true)
    public static @NotNull String formatColor(@NotNull String txt) {
        return txt.replaceAll("&", "§");
    }

    public static String generateRandomString(final int n) {
        final char[] array = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
        final StringBuilder sb = new StringBuilder();
        IntStream.range(0, n).forEach(p2 -> sb.append(array[rand.nextInt(array.length)]));
        return sb.toString();
    }

    public static boolean isFriended(EntityPlayer entityPlayer) {
        return !friends.isEmpty() && friends.contains(entityPlayer.getName().toLowerCase());
    }

    public static boolean isFriended(String name) {
        return !friends.isEmpty() && friends.contains(name.toLowerCase());
    }
//
//    public static double getRandomValue(SliderSetting a, SliderSetting b, Random r) {
//        return a.getInput() == b.getInput() ? a.getInput() : a.getInput() + r.nextDouble() * (b.getInput() - a.getInput());
//    }

    public static boolean nullCheck() {
        return mc.thePlayer != null && mc.theWorld != null;
    }

    public static boolean isHypixel() {
        return !mc.isSingleplayer() && mc.getCurrentServerData() != null
                && mc.getCurrentServerData().serverIP.contains("hypixel.net");
    }

    public static boolean isCraftiGames() {
        return !mc.isSingleplayer() && mc.getCurrentServerData() != null
                && (mc.getCurrentServerData().serverIP.contains("pika-network.net") || mc.getCurrentServerData().serverIP.contains("pikasys.net") || mc.getCurrentServerData().serverIP.contains("pika.host") || mc.getCurrentServerData().serverIP.contains("jartexsys.net") || mc.getCurrentServerData().serverIP.contains("jartexnetwork.com"));
    }

    static Timer timer = null;

    static {
        try {
            timer = (Timer) Minecraft.class.getDeclaredField("timer").get(Minecraft.getMinecraft());
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static net.minecraft.util.Timer getTimer() {
        return ReflectionManager.Minecraft$getTimer(mc);
    }

    public static String getHitsToKill(final EntityPlayer entityPlayer, final ItemStack itemStack) {
        final int n = (int) Math.ceil(ap(entityPlayer, itemStack));
        return "§" + ((n <= 1) ? "c" : ((n <= 3) ? "6" : ((n <= 5) ? "e" : "a"))) + n;
    }

    public static double ap(final EntityPlayer entityPlayer, final ItemStack itemStack) {
        double n = 1.0;
        if (itemStack != null && (itemStack.getItem() instanceof ItemSword || itemStack.getItem() instanceof ItemAxe)) {
            n += getDamage(itemStack);
        }
        double n2 = 0.0;
        double n3 = 0.0;
        for (int i = 0; i < 4; ++i) {
            final ItemStack armorItemInSlot = entityPlayer.inventory.armorItemInSlot(i);
            if (armorItemInSlot != null) {
                if (armorItemInSlot.getItem() instanceof ItemArmor) {
                    n2 += ((ItemArmor) armorItemInSlot.getItem()).damageReduceAmount * 0.04;
                    final int getEnchantmentLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, armorItemInSlot);
                    if (getEnchantmentLevel != 0) {
                        n3 += Math.floor(0.75 * (6 + getEnchantmentLevel * getEnchantmentLevel) / 3.0);
                    }
                }
            }
        }
        return rnd((double) getCompleteHealth(entityPlayer) / (n * (1.0 - (n2 + 0.04 * Math.min(Math.ceil(Math.min(n3, 25.0) * 0.75), 20.0) * (1.0 - n2)))), 1);
    }

    public static float n() {
        return ae(mc.thePlayer.rotationYaw, mc.thePlayer.movementInput.moveForward, mc.thePlayer.movementInput.moveStrafe);
    }

    public static String extractFileName(String name) {
        int firstIndex = name.indexOf("_");
        int lastIndex = name.lastIndexOf("_");

        if (firstIndex != -1 && lastIndex != -1 && lastIndex > firstIndex) {
            return name.substring(firstIndex + 1, lastIndex);
        } else {
            return name;
        }
    }

    public static int merge(int n, int n2) {
        return (n & 0xFFFFFF) | n2 << 24;
    }

    public static int clamp(int n) {
        if (n > 255) {
            return 255;
        }
        return Math.max(n, 4);
    }

    public static boolean isTeamMate(Entity entity) {
        try {
            Entity teamMate = entity;
            if (mc.thePlayer.isOnSameTeam((EntityLivingBase) entity) || mc.thePlayer.getDisplayName().getUnformattedText().startsWith(teamMate.getDisplayName().getUnformattedText().substring(0, 2))) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    public static void setSpeed(double n) {
        if (n == 0.0) {
            mc.thePlayer.motionZ = 0.0;
            mc.thePlayer.motionX = 0.0;
            return;
        }
        float n3 = n();
        mc.thePlayer.motionX = -Math.sin(n3) * n;
        mc.thePlayer.motionZ = Math.cos(n3) * n;
    }

    public static void resetTimer() {
        try {
            getTimer().timerSpeed = 1.0F;
        } catch (NullPointerException var1) {
        }
    }

    public static boolean inInventory() {
        if (!Utils.nullCheck()) {
            return false;
        }
        return (mc.currentScreen != null) && (mc.thePlayer.inventoryContainer != null) && (mc.thePlayer.inventoryContainer instanceof ContainerPlayer) && (mc.currentScreen instanceof GuiInventory);
    }

    public static int getBedwarsStatus() {
        if (!Utils.nullCheck()) {
            return -1;
        }
        final Scoreboard scoreboard = mc.theWorld.getScoreboard();
        if (scoreboard == null) {
            return -1;
        }
        final ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
        if (objective == null) {
            return -1;
        }
        String displayName = stripString(objective.getDisplayName());
        if (!displayName.contains("BED WARS") && !displayName.contains("BedWars")) {
            return -1;
        }
        for (String line : getSidebarLines()) {
            line = stripString(line).trim();
            String[] parts = line.split("  ");
            if (parts.length > 1 && parts[1].equalsIgnoreCase("L")) {
                return 0;
            }
            if (line.equalsIgnoreCase("Waiting...") || line.startsWith("Starting")) {
                return 1;
            }
            if (line.startsWith("R Red:") || line.startsWith("B Blue:") || line.startsWith("Red") || line.startsWith("Blue")) {
                return 2;
            }
        }
        return -1;
    }

    public static String stripString(final String s) {
        final char[] nonValidatedString = StringUtils.stripControlCodes(s).toCharArray();
        final StringBuilder validated = new StringBuilder();
        for (final char c : nonValidatedString) {
            if (c < '' && c > '') {
                validated.append(c);
            }
        }
        return validated.toString();
    }

    public static List<String> getSidebarLines() {
        final List<String> lines = new ArrayList<>();
        if (mc.theWorld == null) {
            return lines;
        }
        final Scoreboard scoreboard = mc.theWorld.getScoreboard();
        if (scoreboard == null) {
            return lines;
        }
        final ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
        if (objective == null) {
            return lines;
        }
        Collection<Score> scores = scoreboard.getSortedScores(objective);
        final List<Score> list = new ArrayList<>();
        for (final Score input : scores) {
            if (input != null && input.getPlayerName() != null && !input.getPlayerName().startsWith("#")) {
                list.add(input);
            }
        }
        if (list.size() > 15) {
            scores = new ArrayList<>(Lists.newArrayList(Iterables.skip(list, list.size() - 15)));
        } else {
            scores = list;
        }
        int index = 0;
        for (final Score score : scores) {
            ++index;
            final ScorePlayerTeam team = scoreboard.getPlayersTeam(score.getPlayerName());
            lines.add(ScorePlayerTeam.formatPlayerName(team, score.getPlayerName()));
            if (index == scores.size()) {
                lines.add(objective.getDisplayName());
            }
        }
        return lines;
    }

    public static Random getRandom() {
        return rand;
    }

    public static boolean isMoving() {
        if (!Utils.nullCheck()) return false;
        return mc.thePlayer.moveForward != 0.0F || mc.thePlayer.moveStrafing != 0.0F;
    }

    /**
     * it's UNFAIR if pc == true!
     * TODO we need a rotation system.
     */
    public static void aim(Entity en, float ps, boolean pc) {
        if (en != null) {
            float[] t = getRotation(en);
            if (t != null) {
                float y = t[0];
                float p = t[1] + 4.0F + ps;
                if (pc) {
                    mc.getNetHandler().addToSendQueue(new C05PacketPlayerLook(y, p, mc.thePlayer.onGround));
                } else {
                    mc.thePlayer.rotationYaw = y;
                    mc.thePlayer.rotationPitch = p;
                }
            }

        }
    }

    public static float[] getRotation(Entity q) {
        if (q == null) {
            return null;
        } else {
            double diffX = q.posX - mc.thePlayer.posX;
            double diffY;
            if (q instanceof EntityLivingBase) {
                EntityLivingBase en = (EntityLivingBase) q;
                diffY = en.posY + (double) en.getEyeHeight() * 0.9D - (mc.thePlayer.posY + (double) mc.thePlayer.getEyeHeight());
            } else {
                diffY = (q.getEntityBoundingBox().minY + q.getEntityBoundingBox().maxY) / 2.0D - (mc.thePlayer.posY + (double) mc.thePlayer.getEyeHeight());
            }

            double diffZ = q.posZ - mc.thePlayer.posZ;
            double dist = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);
            float yaw = (float) (Math.atan2(diffZ, diffX) * 180.0D / 3.141592653589793D) - 90.0F;
            float pitch = (float) (-(Math.atan2(diffY, dist) * 180.0D / 3.141592653589793D));
            return new float[]{mc.thePlayer.rotationYaw + MathHelper.wrapAngleTo180_float(yaw - mc.thePlayer.rotationYaw), mc.thePlayer.rotationPitch + MathHelper.wrapAngleTo180_float(pitch - mc.thePlayer.rotationPitch)};
        }
    }

    public static double n(Entity en) {
        return ((double) (mc.thePlayer.rotationYaw - getYaw(en)) % 360.0D + 540.0D) % 360.0D - 180.0D;
    }

    public static float getYaw(Entity ent) {
        double x = ent.posX - mc.thePlayer.posX;
        double z = ent.posZ - mc.thePlayer.posZ;
        double yaw = Math.atan2(x, z) * 57.29577951308232;
        return (float) (yaw * -1.0D);
    }

    public static void ss(double s, boolean m) {
        if (!m || isMoving()) {
            mc.thePlayer.motionX = -Math.sin(gd()) * s;
            mc.thePlayer.motionZ = Math.cos(gd()) * s;
        }
    }

    public static boolean keysDown() {
        return Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode()) || Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode()) || Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode()) || Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode());
    }

    public static boolean jumpDown() {
        return Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode());
    }

    public static float gd() {
        float yw = mc.thePlayer.rotationYaw;
        if (mc.thePlayer.moveForward < 0.0F) {
            yw += 180.0F;
        }

        float f;
        if (mc.thePlayer.moveForward < 0.0F) {
            f = -0.5F;
        } else if (mc.thePlayer.moveForward > 0.0F) {
            f = 0.5F;
        } else {
            f = 1.0F;
        }

        if (mc.thePlayer.moveStrafing > 0.0F) {
            yw -= 90.0F * f;
        }

        if (mc.thePlayer.moveStrafing < 0.0F) {
            yw += 90.0F * f;
        }

        yw *= 0.017453292F;
        return yw;
    }

    public static float ae(float n, float n2, float n3) {
        float n4 = 1.0f;
        if (n2 < 0.0f) {
            n += 180.0f;
            n4 = -0.5f;
        } else if (n2 > 0.0f) {
            n4 = 0.5f;
        }
        if (n3 > 0.0f) {
            n -= 90.0f * n4;
        } else if (n3 < 0.0f) {
            n += 90.0f * n4;
        }
        return n * 0.017453292f;
    }

    public static double getHorizontalSpeed() {
        return getHorizontalSpeed(mc.thePlayer);
    }

    public static double getHorizontalSpeed(Entity entity) {
        return Math.sqrt(entity.motionX * entity.motionX + entity.motionZ * entity.motionZ);
    }

    public static boolean onEdge() {
        return onEdge(mc.thePlayer);
    }

    public static boolean onEdge(Entity entity) {
        return mc.theWorld.getCollidingBoundingBoxes(entity, entity.getEntityBoundingBox().offset(entity.motionX / 3.0D, -1.0D, entity.motionZ / 3.0D)).isEmpty();
    }

    public static double gbps(Entity en, int d) {
        double x = en.posX - en.prevPosX;
        double z = en.posZ - en.prevPosZ;
        double sp = Math.sqrt(x * x + z * z) * 20.0D;
        return rnd(sp, d);
    }

    public static String removeFormatCodes(String str) {
        return str.replace("§k", "").replace("§l", "").replace("§m", "").replace("§n", "").replace("§o", "").replace("§r", "");
    }

    public static boolean tryingToCombo() {
        return Mouse.isButtonDown(0) && Mouse.isButtonDown(1);
    }

    public static long getDifference(long n, long n2) {
        return Math.abs(n2 - n);
    }

    public static void sendModuleMessage(Module module, String s) {
        sendRawMessage("&3" + module.getName() + "&7: &r" + s);
    }

    public static EntityLivingBase raytrace(final int n) {
        Entity entity = null;
        MovingObjectPosition rayTrace = mc.thePlayer.rayTrace((double) n, 1.0f);
        final Vec3 getPositionEyes = mc.thePlayer.getPositionEyes(1.0f);
        final float rotationYaw = mc.thePlayer.rotationYaw;
        final float rotationPitch = mc.thePlayer.rotationPitch;
        final float cos = MathHelper.cos(-rotationYaw * 0.017453292f - 3.1415927f);
        final float sin = MathHelper.sin(-rotationYaw * 0.017453292f - 3.1415927f);
        final float n2 = -MathHelper.cos(-rotationPitch * 0.017453292f);
        final Vec3 vec3 = new Vec3((double) (sin * n2), (double) MathHelper.sin(-rotationPitch * 0.017453292f), (double) (cos * n2));
        final Vec3 addVector = getPositionEyes.addVector(vec3.xCoord * (double) n, vec3.yCoord * (double) n, vec3.zCoord * (double) n);
        Vec3 vec4 = null;
        final List<?> getEntitiesWithinAABBExcludingEntity = mc.theWorld.getEntitiesWithinAABBExcludingEntity(mc.getRenderViewEntity(), mc.getRenderViewEntity().getEntityBoundingBox().addCoord(vec3.xCoord * (double) n, vec3.yCoord * (double) n, vec3.zCoord * (double) n).expand(1.0, 1.0, 1.0));
        double n3 = n;
        for (Object o : getEntitiesWithinAABBExcludingEntity) {
            final Entity entity2 = (Entity) o;
            if (entity2.canBeCollidedWith()) {
                final float getCollisionBorderSize = entity2.getCollisionBorderSize();
                final AxisAlignedBB expand = entity2.getEntityBoundingBox().expand((double) getCollisionBorderSize, (double) getCollisionBorderSize, (double) getCollisionBorderSize);
                final MovingObjectPosition calculateIntercept = expand.calculateIntercept(getPositionEyes, addVector);
                if (expand.isVecInside(getPositionEyes)) {
                    if (0.0 < n3 || n3 == 0.0) {
                        entity = entity2;
                        vec4 = ((calculateIntercept == null) ? getPositionEyes : calculateIntercept.hitVec);
                        n3 = 0.0;
                    }
                } else if (calculateIntercept != null) {
                    final double distanceTo = getPositionEyes.distanceTo(calculateIntercept.hitVec);
                    if (distanceTo < n3 || n3 == 0.0) {
                        if (entity2 == mc.getRenderViewEntity().ridingEntity) {
                            if (n3 == 0.0) {
                                entity = entity2;
                                vec4 = calculateIntercept.hitVec;
                            }
                        } else {
                            entity = entity2;
                            vec4 = calculateIntercept.hitVec;
                            n3 = distanceTo;
                        }
                    }
                }
            }
        }
        if (entity != null && (n3 < n || rayTrace == null)) {
            rayTrace = new MovingObjectPosition(entity, vec4);
        }
        if (rayTrace != null && rayTrace.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && rayTrace.entityHit instanceof EntityLivingBase) {
            return (EntityLivingBase) rayTrace.entityHit;
        }
        return null;
    }

    public static int getChroma(long speed, long... delay) {
        long time = System.currentTimeMillis() + (delay.length > 0 ? delay[0] : 0L);
        return Color.getHSBColor((float) (time % (15000L / speed)) / (15000.0F / (float) speed), 1.0F, 1.0F).getRGB();
    }

    public static double rnd(double n, int d) {
        if (d == 0) {
            return (double) Math.round(n);
        } else {
            double p = Math.pow(10.0D, (double) d);
            return (double) Math.round(n * p) / p;
        }
    }
    public static double PitchFromEntity(EntityPlayer en, float f) {
        return (double) (mc.thePlayer.rotationPitch - pitchToEntity(en, f));
    }
    public static double fovFromEntity(EntityPlayer en) {
        return ((((double) (mc.thePlayer.rotationYaw - fovToEntity(en)) % 360.0D) + 540.0D) % 360.0D) - 180.0D;
    }

    public static float fovFromEntityf(EntityPlayer en) {
        return (float) (((((float) (mc.thePlayer.rotationYaw - fovToEntity(en)) % 360.0D) + 540.0D) % 360.0D) - 180.0D);
    }

    public static float fovToEntity(EntityPlayer ent) {
        double x = ent.posX - mc.thePlayer.posX;
        double z = ent.posZ - mc.thePlayer.posZ;
        double yaw = Math.atan2(x, z) * 57.2957795D;
        return (float) (yaw * -1.0D);
    }
    public static float pitchToEntity(EntityPlayer ent, float f) {
        double x = mc.thePlayer.getDistanceToEntity(ent);
        double y = mc.thePlayer.posY - (ent.posY + f);
        double pitch = (((Math.atan2(x, y) * 180.0D) / 3.141592653589793D));
        return (float) (90 - pitch);
    }
    public static String stripColor(final String s) {
        if (s.isEmpty()) {
            return s;
        }
        final char[] array = StringUtils.stripControlCodes(s).toCharArray();
        final StringBuilder sb = new StringBuilder();
        for (final char c : array) {
            if (c < '\u007f' && c > '\u0014') {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static List<String> gsl() {
        List<String> lines = new ArrayList();
        if (mc.theWorld == null) {
            return lines;
        } else {
            Scoreboard scoreboard = mc.theWorld.getScoreboard();
            if (scoreboard == null) {
                return lines;
            } else {
                ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
                if (objective == null) {
                    return lines;
                } else {
                    Collection<Score> scores = scoreboard.getSortedScores(objective);
                    List<Score> list = new ArrayList();
                    Iterator var5 = scores.iterator();

                    Score score;
                    while (var5.hasNext()) {
                        score = (Score) var5.next();
                        if (score != null && score.getPlayerName() != null && !score.getPlayerName().startsWith("#")) {
                            list.add(score);
                        }
                    }

                    if (list.size() > 15) {
                        scores = Lists.newArrayList(Iterables.skip(list, scores.size() - 15));
                    } else {
                        scores = list;
                    }

                    var5 = scores.iterator();

                    while (var5.hasNext()) {
                        score = (Score) var5.next();
                        ScorePlayerTeam team = scoreboard.getPlayersTeam(score.getPlayerName());
                        lines.add(ScorePlayerTeam.formatPlayerName(team, score.getPlayerName()));
                    }

                    return lines;
                }
            }
        }
    }

    public static void playerSwing() {
        EntityPlayerSP p = mc.thePlayer;
        int armSwingEnd = p.isPotionActive(Potion.digSpeed) ? 6 - (1 + p.getActivePotionEffect(Potion.digSpeed).getAmplifier()) : (p.isPotionActive(Potion.digSlowdown) ? 6 + (1 + p.getActivePotionEffect(Potion.digSlowdown).getAmplifier()) * 2 : 6);
        if (!p.isSwingInProgress || p.swingProgressInt >= armSwingEnd / 2 || p.swingProgressInt < 0) {
            p.swingProgressInt = -1;
            p.isSwingInProgress = true;
        }

    }

    public static String uf(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public static boolean overAir() {
        if (!Utils.nullCheck()) return false;
        return mc.theWorld.isAirBlock(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ));
    }

    public static boolean overPlaceable(double yOffset) {
        BlockPos playerPos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + yOffset, mc.thePlayer.posZ);
        return PlayerUtil.replaceable(playerPos) || PlayerUtil.isFluid(PlayerUtil.block(playerPos));
    }

    public static boolean holdingWeapon() {
        return holdingSword();
    }

    public static boolean holdingSword() {
        ItemStack item = mc.thePlayer.getHeldItem();
        if (item == null) {
            return false;
        }
        Item getItem = item.getItem();
        return getItem instanceof ItemSword;
    }

    public static double getDamage(final ItemStack itemStack) {
        double getAmount = 0;
        for (final Map.Entry<String, AttributeModifier> entry : itemStack.getAttributeModifiers().entries()) {
            if (entry.getKey().equals("generic.attackDamage")) {
                getAmount = entry.getValue().getAmount();
                break;
            }
        }
        return getAmount + EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, itemStack) * 1.25;
    }

    public static @NotNull String getUnformatedString(@NotNull String s) {
        StringBuilder stringbuilder = new StringBuilder();

        boolean doIgnore = false;
        forEach:
        for (char c : s.toCharArray()) {
            if (doIgnore) {
                doIgnore = false;
                for (EnumChatFormatting format : EnumChatFormatting.values())
                    if (((char) ReflectionManager.getValue(EnumChatFormatting.class, format, "formattingCode")) == c)
                        continue forEach;
            }
            if (c == '§') {
                doIgnore = true;
                continue;
            }

            stringbuilder.append(c);
        }

        return stringbuilder.toString();
    }

    public static Vec3 getEyePos(@NotNull Entity entity, @NotNull Vec3 position) {
        return position.add(new Vec3(0, entity.getEyeHeight(), 0));
    }

    public static Vec3 getEyePos(Entity entity) {
        return getEyePos(entity, new Vec3(entity.posX, entity.posY, entity.posZ));
    }

    public static Vec3 getEyePos() {
        return getEyePos(mc.thePlayer);
    }

    public static boolean isTargetNearby() {
        return mc.theWorld.playerEntities.stream()
                .filter(target -> target != mc.thePlayer)
                .anyMatch(target -> new Vec3(target.posX, target.posY, target.posZ).distanceTo(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)) < 6);
    }

    public static boolean isTargetNearby(double dist) {
        return mc.theWorld.playerEntities.stream()
                .filter(target -> target != mc.thePlayer)
                .anyMatch(target -> new Vec3(target.posX, target.posY, target.posZ).distanceTo(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)) < dist);
    }

    /**
     * Checks if the player is in a liquid
     *
     * @return in liquid
     */
    public static boolean inLiquid() {
        return mc.thePlayer.isInWater() || mc.thePlayer.isInLava();
    }

    public static boolean isLobby() {
        if (Utils.isHypixel()) {
            List<String> sidebarLines = Utils.getSidebarLines();
            if (!sidebarLines.isEmpty()) {
                try {
                    String[] parts = Utils.stripColor(sidebarLines.get(1)).split(" {2}");
                    return parts.length > 1 && parts[1].charAt(0) == 'L';
                } catch (IndexOutOfBoundsException ignored) {
                }
            }
        }
        return false;
    }

    public static String getKeyName(int keyCode) {
        if (keyCode >= 1000)
            return "M" + (keyCode - 1000);
        try {
            return Keyboard.getKeyName(keyCode);
        } catch (Exception e) {
            return "Unknown";
        }
    }

    public static int getKeyCode(@NotNull String keyName) {
        try {
            if (keyName.startsWith("M") && keyName.length() > 1) {
                final String number = keyName.substring(1);
                if (number.chars().allMatch(Character::isDigit)) {
                    return Integer.parseInt(number) + 1000;
                }
            }

            return Keyboard.getKeyIndex(keyName);
        } catch (Exception e) {
            return Keyboard.KEY_NONE;
        }
    }

    public static boolean isInRange(double value, double target, double range) {
        return value - range <= target && value + range >= target;

    }

    /**
     * Sends a click to Minecraft legitimately
     */
    public static void sendClick(final int button, final boolean state) {
        final int keyBind = button == 0 ? mc.gameSettings.keyBindAttack.getKeyCode() : mc.gameSettings.keyBindUseItem.getKeyCode();

        KeyBinding.setKeyBindState(keyBind, state);

        if (state) {
            KeyBinding.onTick(keyBind);
        }
    }

    public static int limit(int value, int min, int max) {
        return Math.max(Math.min(value, max), min);
    }

    public static long limit(long value, long min, long max) {
        return Math.max(Math.min(value, max), min);
    }

    public static float limit(float value, float min, float max) {
        return Math.max(Math.min(value, max), min);
    }

    public static double limit(double value, double min, double max) {
        return Math.max(Math.min(value, max), min);
    }
}

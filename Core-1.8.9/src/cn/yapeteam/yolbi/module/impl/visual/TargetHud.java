//package cn.yapeteam.yolbi.module.impl.visual;
//
//import cn.yapeteam.loader.api.module.ModuleCategory;
//import cn.yapeteam.loader.api.module.ModuleInfo;
//import cn.yapeteam.yolbi.event.Listener;
//import cn.yapeteam.yolbi.event.impl.render.EventRender2D;
//import cn.yapeteam.yolbi.event.impl.render.EntityDamageEvent;
//import cn.yapeteam.yolbi.module.Module;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.FontRenderer;
//import net.minecraft.client.gui.Gui;
//import net.minecraft.client.network.NetworkPlayerInfo;
//import net.minecraft.client.renderer.GlStateManager;
//import net.minecraft.entity.EntityLivingBase;
//import net.minecraft.entity.player.EntityPlayer;
//import org.lwjgl.opengl.GL11;
//
//import java.awt.*;
//
//@ModuleInfo(name = "TargetHUD", category = ModuleCategory.VISUAL)
//public class TargetHud extends Module {
//    private final Minecraft mc = Minecraft.getMinecraft();
//    private boolean showOnPlayerHit = true; // 击中玩家时显示
//    private boolean showOnMobHit = true; // 击中生物时显示
//    private EntityLivingBase currentTarget = null;
//
//    @Listener
//    public void onRender2D(EventRender2D event) {
//        if (currentTarget == null) {
//            return;
//        }
//
//        int x = 200; // HUD 的 X 位置
//        int y = 200; // HUD 的 Y 位置
//        int width = 120; // HUD 的宽度
//        int height = 40; // HUD 的高度
//
//        FontRenderer fr = mc.fontRendererObj;
//
//        // 绘制背景
//        drawRect(x, y, x + width, y + height, new Color(0, 0, 0, 120).getRGB());
//
//        // 绘制目标玩家头像
//        NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfo(currentTarget.getUniqueID());
//        if (info != null) {
//            mc.getTextureManager().bindTexture(info.getLocationSkin());
//            drawPlayerHead(x + 2, y + 2, 36, 36);
//        }
//
//        // 绘制目标玩家名字
//        String name = currentTarget.getName();
//        fr.drawStringWithShadow(name, x + 40, y + 2, -1);
//
//        // 绘制目标玩家生命值
//        float health = currentTarget.getHealth();
//        float maxHealth = currentTarget.getMaxHealth();
//        int healthColor = Color.HSBtoRGB(Math.max(0.0F, Math.min(health / maxHealth, 1.0F)) / 3.0F, 1.0F, 1.0F);
//        String healthStr = String.format("%.1f", health / 2.0F);
//        fr.drawStringWithShadow("❤ " + healthStr, x + 40, y + 14, healthColor);
//
//        // 绘制与目标玩家的距离
//        double distance = mc.thePlayer.getDistanceToEntity(currentTarget);
//        String distanceStr = String.format("%.1f", distance);
//        fr.drawStringWithShadow("距离: " + distanceStr, x + 40, y + 26, -1);
//
//        // 绘制目标玩家护甲值
//        int armor = currentTarget.getTotalArmorValue();
//        fr.drawStringWithShadow("护甲: " + armor, x + 80, y + 26, -1);
//    }
//
//    @Listener
//    public void onEntityDamage(EntityDamageEvent event) {
//        if (event.getAttacker() == mc.thePlayer) {
//            if (event.getTarget() instanceof EntityPlayer && showOnPlayerHit) {
//                currentTarget = (EntityLivingBase) event.getTarget();
//            } else if (event.getTarget() instanceof EntityLivingBase && showOnMobHit) {
//                currentTarget = (EntityLivingBase) event.getTarget();
//            }
//        }
//    }
//
//    private void drawPlayerHead(int x, int y, int width, int height) {
//        GL11.glPushMatrix();
//        GlStateManager.enableBlend();
//        GL11.glTranslatef(x, y, 0);
//        GL11.glScalef(width / 32.0F, height / 32.0F, 1);
//        Gui.drawModalRectWithCustomSizedTexture(0, 0, 8, 8, 8, 8, 64, 64);
//        GL11.glPopMatrix();
//    }
//
//    private void drawRect(int left, int top, int right, int bottom, int color) {
//        Gui.drawRect(left, top, right, bottom, color);
//    }
//}


package cn.yapeteam.yolbi.module.impl.visual;

import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.render.EventRender2D;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.ModuleCategory;
import cn.yapeteam.yolbi.module.impl.combat.KillAura;
import cn.yapeteam.yolbi.module.values.impl.ColorValue;
import cn.yapeteam.yolbi.utils.render.RenderUtil;
import cn.yapeteam.yolbi.utils.render.StencilUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.text.DecimalFormat;

// code by wzhy233
public class TargetHud extends Module {
    private final Minecraft mc = Minecraft.getMinecraft();
    public Color color(int tick) {
        return new Color(RenderUtil.colorSwitch(new Color(mainColor.getColor()), new Color(mainColor2.getColor()), 2000.0F, -(tick * 200) / 40, 75L, 2.0D));
    }
    private ColorValue mainColor=new ColorValue("MainColor",0xffffff);
    private ColorValue mainColor2=new ColorValue("MainColor2",0xffffff);

    public TargetHud() {
        super("TargetHUD", ModuleCategory.VISUAL);
    }

    @Listener
    public void onRender2D(EventRender2D event) {
        /*EntityLivingBase target = getTarget();
        if (!(target instanceof EntityPlayer))
            return;



        FontRenderer fr = mc.fontRendererObj;//66mc原版fontrenderer

        // 绘制背景
        drawRect(x, y, x + width, y + height, new Color(0, 0, 0, 120).getRGB());

        // 绘制目标玩家头像
        NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfo(target.getUniqueID());
        if (info != null) {
            mc.getTextureManager().bindTexture(info.getLocationSkin());
            drawPlayerHead(x + 2, y + 2, 36, 36);
        }

        // 绘制目标玩家名字
        String name = target.getName();
        fr.drawStringWithShadow(name, x + 40, y + 2, -1);

        // 绘制目标玩家生命值
        float health = target.getHealth();
        float maxHealth = target.getMaxHealth();
        int healthColor = Color.HSBtoRGB(Math.max(0.0F, Math.min(health / maxHealth, 1.0F)) / 3.0F, 1.0F, 1.0F);
        String healthStr = String.format("%.1f", health / 2.0F);
        fr.drawStringWithShadow("❤ " + healthStr, x + 40, y + 14, healthColor);

        // 绘制与目标玩家的距离
        double distance = mc.thePlayer.getDistanceToEntity(target);
        String distanceStr = String.format("%.1f", distance);
        fr.drawStringWithShadow("Distant: " + distanceStr, x + 40, y + 26, -1);

        // 绘制目标玩家护甲值
        int armor = target.getTotalArmorValue();
        fr.drawStringWithShadow("Armor: " + armor, x + 120, y + 26, -1);*/
        int x = 200; // HUD 的 X 位置
        int y = 200; // HUD 的 Y 位置
        int width = 120; // HUD 的宽度
        int height = 40; // HUD 的高度
        float x2 = x;
        float y2 = y;
        KillAura ka = YolBi.instance.getModuleManager().getModule(KillAura.class);

        RenderUtil.scaleStart((float)((double)x2 + width / 2.0), (float)((double)y2 + height / 2.0), 1f);//动画我先不一致
        this.render(x2, y2, 1.0f, ka.getTarget());
        RenderUtil.scaleEnd();
    }
    public void render(float x2, float y2, float alpha, EntityLivingBase target) {
        Color firstColor = color(1);
        Color secondColor = color(6);

        DecimalFormat DF1 = new DecimalFormat("0");

        RenderUtil.drawRoundOutline(x2, y2, (float)this.getWidth(), (float)this.getHeight(), 5.0f, 0.1f, new Color(255, 255, 255, 10), RenderUtil.reAlpha(HUD.color(1), 200));
        float hurt_time1 = Math.max(7, target.hurtTime);
        target.animatedHealthBar = AnimationUtil.animate(target.animatedHealthBar, target.getHealth(), 0.2f);
        FontManager.arial18.drawString(target.getName(), x2 + 37.0f, y2 + 9.0f, HUD.color(2).getRGB());
        FontManager.arial18.drawString(DF1.format(target.animatedHealthBar), (float)((double)x2 + this.getWidth() - 19.0), y2 + 9.0f, HUD.color(8).getRGB());
        RenderUtil.drawRound(x2 + 38.0f, y2 + 21.0f, (float)(this.getWidth() - 46.0), 4.0f, 2.0f, new Color(32, 32, 32, 100));
        RenderUtil.drawGradientRoundLR(x2 + 38.0f, y2 + 21.0f, (float)((double)(target.animatedHealthBar / target.getMaxHealth()) * (this.getWidth() - 46.0)), 4.0f, 2.0f, firstColor, secondColor);
        NetworkPlayerInfo playerInfo1 = mc.getNetHandler().getPlayerInfo(TargetHud.mc.thePlayer.getUniqueID());
        if (target instanceof EntityPlayer) {
            playerInfo1 = mc.getNetHandler().getPlayerInfo(target.getUniqueID());
        }
        if (playerInfo1 != null && target instanceof AbstractClientPlayer) {
            float renderHurtTime = (float)target.hurtTime - (target.hurtTime != 0 ? Minecraft.getMinecraft().timer.renderPartialTicks : 0.0f);
            float hurtPercent1 = renderHurtTime / 10.0f;
            GL11.glColor4f((float)1.0f, (float)(1.0f - hurtPercent1), (float)(1.0f - hurtPercent1), (float)1.0f);
            GL11.glPushMatrix();
            this.drawBigHeadRound2(x2 - 2.0f + hurt_time1, y2 - 2.0f + hurt_time1, 42.0f - hurt_time1 * 2.0f, 42.0f - hurt_time1 * 2.0f, (AbstractClientPlayer)target);
            GL11.glPopMatrix();
        }
        if (hurt_time1 != 7.0f) return;
        hurt_time1 = 0.0f;
    }
    protected void drawBigHeadRound2(float x2, float y2, float width, float height, AbstractClientPlayer player) {
        StencilUtil.initStencilToWrite();
        RenderUtil.renderRoundedRect(x2 - 1.0f, y2 - 1.0f, width, height, 6.0f, -1);
        StencilUtil.readStencilBuffer(1);
        RenderUtil.color(-1);
        this.drawBigHead(x2 - 1.0f, y2 - 1.0f, width, height, player);//徐锦良的奇妙命名6：6666666666画大头？
        StencilUtil.uninitStencilBuffer();
        GlStateManager.disableBlend();
    }
    protected void drawBigHead(float x2, float y2, float width, float height, AbstractClientPlayer player) {
        double offset = -(player.hurtTime * 23);
        RenderUtil.glColor(new Color(255, (int)(255.0 + offset), (int)(255.0 + offset)).getRGB());
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        mc.getTextureManager().bindTexture(player.getLocationSkin());
        Gui.drawScaledCustomSizeModalRect(x2, y2, 8.0f, 8.0f, 8, 8, width, height, 64.0f, 64.0f);
        GlStateManager.disableBlend();
        GlStateManager.resetColor();
    }

    private EntityLivingBase getTarget() {
        if (mc.pointedEntity instanceof EntityLivingBase) {
            return (EntityLivingBase) mc.pointedEntity;
        }
        return null;
    }

    private void drawPlayerHead(int x, int y, int width, int height) {
        GL11.glPushMatrix();
        GlStateManager.enableBlend();
        GL11.glTranslatef(x, y, 0);
        GL11.glScalef(width / 32.0F, height / 32.0F, 1);
        Gui.drawModalRectWithCustomSizedTexture(0, 0, 8, 8, 8, 8, 64, 64);
        GL11.glPopMatrix();
    }

    private void drawRect(int left, int top, int right, int bottom, int color) {
        Gui.drawRect(left, top, right, bottom, color);
    }
}

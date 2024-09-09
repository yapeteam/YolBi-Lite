package cn.yapeteam.yolbi.module.impl.render;


import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.game.EventLoadWorld;
import cn.yapeteam.yolbi.event.impl.player.EventUpdate;
import cn.yapeteam.yolbi.event.impl.render.EventRender2D;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.api.Category;
import cn.yapeteam.yolbi.module.api.ModuleInfo;
import cn.yapeteam.yolbi.module.api.value.impl.BooleanValue;
import cn.yapeteam.yolbi.module.api.value.impl.NumberValue;
import cn.yapeteam.yolbi.utils.Utils;
import cn.yapeteam.yolbi.utils.player.BlockUtils;
import cn.yapeteam.yolbi.utils.render.shader.ShaderUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glPopMatrix;

@ModuleInfo(aliases = {"module.render.bedesp.name"}, description = "module.render.bedesp.description", category = Category.RENDER)
public class BedESP extends Module {
    NumberValue range = new NumberValue("Range", this, 10, 2, 30, 1);
    BooleanValue firstBed = new BooleanValue("Only render first bed", this, false);
    NumberValue rate = new NumberValue("Rate", this, 0.4, 0.1, 3, 0.1);

    public static final ShaderUtils roundedShader = new ShaderUtils("shader/rrect.frag");
    private BlockPos[] bed = null;
    private final List<BlockPos[]> beds = new ArrayList<>();
    private long lastCheck = 0;

    @Listener
    public void onUpdate(EventUpdate eventUpdate){
        if (System.currentTimeMillis() - lastCheck < rate.getValue().doubleValue() * 1000) {
            return;
        }
        lastCheck = System.currentTimeMillis();
        int i;
        priorityLoop:
        for (int n = i = (int) range.getValue(); i >= -n; --i) {
            for (int j = -n; j <= n; ++j) {
                for (int k = -n; k <= n; ++k) {
                    final BlockPos blockPos = new BlockPos(mc.thePlayer.posX + j, mc.thePlayer.posY + i, mc.thePlayer.posZ + k);
                    final IBlockState getBlockState = mc.theWorld.getBlockState(blockPos);
                    if (getBlockState.getBlock() == Blocks.bed && getBlockState.getValue((IProperty) BlockBed.PART) == BlockBed.EnumPartType.FOOT) {
                        if (firstBed.getValue()) {
                            if (this.bed != null && BlockUtils.isSamePos(blockPos, this.bed[0])) {
                                return;
                            }
                            this.bed = new BlockPos[]{blockPos, blockPos.offset((EnumFacing) getBlockState.getValue((IProperty) BlockBed.FACING))};
                            return;
                        } else {
                            for (int l = 0; l < this.beds.size(); ++l) {
                                if (BlockUtils.isSamePos(blockPos, ((BlockPos[]) this.beds.get(l))[0])) {
                                    continue priorityLoop;
                                }
                            }
                            this.beds.add(new BlockPos[]{blockPos, blockPos.offset((EnumFacing) getBlockState.getValue((IProperty) BlockBed.FACING))});
                        }
                    }
                }
            }
        }
    }

    @Listener
    public void onEntityJoin(EventLoadWorld eventLoadWorld){
        this.beds.clear();
        this.bed = null;
    }

    @Listener
    public void onRender2D(EventRender2D eventRender2D){
        if (Utils.nullCheck()) {
            if (firstBed.getValue() && this.bed != null) {
                if (!(mc.theWorld.getBlockState(bed[0]).getBlock() instanceof BlockBed)) {
                    this.bed = null;
                    return;
                }
                renderBedDefense(this.bed);
                return;
            }
            if (this.beds.isEmpty()) {
                return;
            }
            Iterator<BlockPos[]> iterator = this.beds.iterator();
            while (iterator.hasNext()) {
                BlockPos[] blockPos = iterator.next();
                if (!(mc.theWorld.getBlockState(blockPos[0]).getBlock() instanceof BlockBed)) {
                    iterator.remove();
                    continue;
                }
                renderBedDefense(blockPos);
            }
        }
    }

    @Override
    public void onDisable() {
        this.bed = null;
        this.beds.clear();
    }

    private void renderBedDefense(BlockPos[] bedPos) {
        List<Block> blocks = getBedDefense(bedPos);
        BlockPos blockPos = bedPos[0];
        if (blocks.isEmpty()) {
            return;
        }

        float rotateX = mc.gameSettings.thirdPersonView == 2 ? -1.0F : 1.0F;
        glPushMatrix();
        glDisable(GL_DEPTH_TEST);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glTranslatef((float) (blockPos.getX() - mc.getRenderManager().viewerPosX + 0.5), (float) (blockPos.getY() - mc.getRenderManager().viewerPosY + 3), (float) (blockPos.getZ() - mc.getRenderManager().viewerPosZ + 0.5));
        glNormal3f(0.0F, 1.0F, 0.0F);
        glRotatef(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        glRotatef(mc.getRenderManager().playerViewX, rotateX, 0.0F, 0.0F);
        glScaled(-0.01666666753590107D * Math.sqrt(mc.thePlayer.getDistance(blockPos.getX(), blockPos.getY(), blockPos.getZ())), -0.01666666753590107D * Math.sqrt(mc.thePlayer.getDistance(blockPos.getX(), blockPos.getY(), blockPos.getZ())), 0.01666666753590107D * Math.sqrt(mc.thePlayer.getDistance(blockPos.getX(), blockPos.getY(), blockPos.getZ())));
        drawRound(Math.max(17.5, blocks.size() * 17.5) / -2, -0.5, Math.max(17.5, blocks.size() * 17.5) - 2.5, 26.5, 3, new Color(0, 0, 0, 90));
        String dist = Math.round(mc.thePlayer.getDistance(blockPos.getX(), blockPos.getY(), blockPos.getZ())) + "m";
        mc.fontRendererObj.drawString(dist, -mc.fontRendererObj.getStringWidth(dist) / 2, 0, new Color(255, 255, 255, 255).getRGB());
        double offset = (blocks.size() * -17.5) / 2;
        for (Block block :blocks) {
            mc.getTextureManager().bindTexture(new ResourceLocation("keystrokesmod:images/" + block.getLocalizedName() + ".png"));
            Gui.drawModalRectWithCustomSizedTexture((int) offset, 10, 0, 0, 15, 15, 15, 15);
            offset += 17.5;
        }
        GlStateManager.disableBlend();
        glEnable(GL_DEPTH_TEST);
        glPopMatrix();
    }

    public static void drawRound(double x, double y, double width, double height, double radius, @NotNull Color color) {
        GlStateManager.color(1, 1, 1, 1);
        roundedShader.init();

        setupRoundedRectUniforms(x, y, width, height, radius);
        roundedShader.setUniformf("color", color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);

        ShaderUtils.drawQuads(x - 1, y - 1, width + 2, height + 2);
        roundedShader.unload();
    }

    private static void setupRoundedRectUniforms(double x, double y, double width, double height, double radius) {
        ScaledResolution sr = new ScaledResolution(mc);
        roundedShader.setUniformf("location", x * sr.getScaleFactor(), (mc.displayHeight - (height * sr.getScaleFactor())) - (y * sr.getScaleFactor()));
        roundedShader.setUniformf("rectSize", width * sr.getScaleFactor(), height * sr.getScaleFactor());
        roundedShader.setUniformf("radius", radius * sr.getScaleFactor());
    }

    private List<Block> getBedDefense(BlockPos bedPos[]) {

        List<Block> defenseblocks = new ArrayList<>();

        double x = bedPos[0].getX() - mc.getRenderManager().viewerPosX;
        double y = bedPos[0].getY() - mc.getRenderManager().viewerPosY;
        double z = bedPos[0].getZ() - mc.getRenderManager().viewerPosZ;

        // we iterate through the top bottom and sides of the bed until we reach air
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                for (int k = 0; k < 2; k++) {
                    BlockPos blockPos = new BlockPos(bedPos[0].getX() + i, bedPos[0].getY() + j, bedPos[0].getZ() + k);
                    IBlockState blockState = mc.theWorld.getBlockState(blockPos);
                    Block block = blockState.getBlock();
                    if (block != Blocks.air) {
                        defenseblocks.add(block);
                    }
                }
            }
        }

        return defenseblocks;
    }
}


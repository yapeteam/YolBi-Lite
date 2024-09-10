package cn.yapeteam.yolbi.module.impl.render;

import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.game.EventLoadWorld;
import cn.yapeteam.yolbi.event.impl.player.EventUpdate;
import cn.yapeteam.yolbi.event.impl.render.EventRender2D;
import cn.yapeteam.yolbi.managers.RenderManager;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.api.Category;
import cn.yapeteam.yolbi.module.api.ModuleInfo;
import cn.yapeteam.yolbi.module.api.value.impl.BooleanValue;
import cn.yapeteam.yolbi.module.api.value.impl.NumberValue;
import cn.yapeteam.yolbi.utils.math.vector.Vector4d;
import cn.yapeteam.yolbi.utils.render.ProjectionUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@ModuleInfo(aliases = {"module.render.bedesp.name"}, description = "module.render.bedesp.description", category = Category.RENDER)
public class BedESP extends Module {
    NumberValue range = new NumberValue("Range", this, 10, 2, 30, 1);
    BooleanValue firstBed = new BooleanValue("Only render first bed", this, false);
    NumberValue rate = new NumberValue("Rate", this, 0.4, 0.1, 3, 0.1);
    NumberValue layers = new NumberValue("Layers", this, 3, 1, 10, 1);

    private BlockPos[] bed = null;
    private final List<BlockPos> beds = new ArrayList<>();
    private final List<List<Block>> bedBlocks = new ArrayList<>();
    private long lastCheck = 0;

    @Listener
    public void onUpdate(EventUpdate eventUpdate) {
        if (System.currentTimeMillis() - lastCheck < rate.getValue().doubleValue() * 1000) {
            return;
        }
        lastCheck = System.currentTimeMillis();

        beds.clear();
        bedBlocks.clear();

        int n = range.getValue().intValue();
        for (int i = -n; i <= n; i++) {
            for (int j = -n; j <= n; j++) {
                for (int k = -n; k <= n; k++) {
                    BlockPos blockPos = new BlockPos(mc.thePlayer.posX + j, mc.thePlayer.posY + i, mc.thePlayer.posZ + k);
                    Block block = mc.theWorld.getBlockState(blockPos).getBlock();

                    if (block == Blocks.bed) {
                        if (firstBed.getValue() && bed != null) {
                            return;
                        }

                        beds.add(blockPos);
                        bedBlocks.add(new ArrayList<>());
                        findBed(blockPos, beds.size() - 1);
                    }
                }
            }
        }
    }

    @Listener
    public void onEntityJoin(EventLoadWorld eventLoadWorld) {
        beds.clear();
        bedBlocks.clear();
        bed = null;
    }

    @Listener
    public void onRender2D(EventRender2D eventRender2D) {
        // Add debugging log for rendering
        System.out.println("Rendering bed defenses...");

        for (int i = 0; i < beds.size(); i++) {
            BlockPos blockPos = beds.get(i);
            if (mc.theWorld.getBlockState(blockPos).getBlock() != Blocks.bed) {
                continue;
            }

            renderBedDefense(blockPos, i);
        }
    }

    @Override
    public void onDisable() {
        beds.clear();
        bedBlocks.clear();
        bed = null;
    }

    private void renderBedDefense(BlockPos blockPos, int index) {

        // Get the projected position of the block
        Vector4d projectedPos = ProjectionUtil.get(new BlockPos(blockPos.getX(), blockPos.getY()+4, blockPos.getZ()));

        // Ensure the projection was successful
        if (projectedPos == null) {
            return; // Skip rendering if the block is not in view
        }

        // Extract the screen position from the projected coordinates
        double screenX = projectedPos.x;
        double screenY = projectedPos.y;

        // Push the current OpenGL matrix state
        GlStateManager.pushMatrix();

        // Disable depth testing for 2D rendering and enable blending
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Render the rounded rectangle background
        List<Block> blocks = bedBlocks.get(index);

        // Calculate the horizontal offset for icons
        RenderManager.roundedRectangle(screenX, screenY, Math.max(17.5, blocks.size() * 17.5) - 2.5, 17,4,new Color(0, 0, 0));

        double offset = (blocks.size() * -17.5) / 2;
        // Render each block's texture icon
        for (Block block : blocks) {
            String texturePath = "textures/blocks/" + block.getLocalizedName() + ".png";
            mc.getTextureManager().bindTexture(new ResourceLocation(texturePath));
            Gui.drawModalRectWithCustomSizedTexture((int) (offset + screenX), (int) (screenY + 10), 0, 0, 15, 15, 15, 15);
            offset += 17.5;
        }

        // Restore the previous OpenGL matrix state
        GlStateManager.disableBlend();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GlStateManager.popMatrix();
    }

    private boolean findBed(BlockPos bedPos, int index) {
        Block bedBlock = mc.theWorld.getBlockState(bedPos).getBlock();
        if (beds.contains(bedPos) || !bedBlock.equals(Blocks.bed)) {
            return false;
        }

        // Check if the bed block is the head of the bed
        if (mc.theWorld.getBlockState(bedPos).getValue(BlockBed.PART) != BlockBed.EnumPartType.HEAD) {
            return false;
        }

        bedBlocks.get(index).add(Blocks.bed);
        beds.set(index, bedPos);

        int[][] directions = {
                {0, 1, 0}, // Above
                {1, 0, 0}, // Right
                {-1, 0, 0}, // Left
                {0, 0, 1}, // Front
                {0, 0, -1}  // Back
        };

        int layersCount = layers.getValue().intValue();

        for (int[] dir : directions) {
            for (int layer = 1; layer <= layersCount; layer++) {
                BlockPos currentPos = bedPos.add(dir[0] * layer, dir[1] * layer, dir[2] * layer);
                Block currentBlock = mc.theWorld.getBlockState(currentPos).getBlock();

                if (currentBlock.equals(Blocks.air)) {
                    break;
                }

                if (isValidBedBlock(currentBlock) && !bedBlocks.get(index).contains(currentBlock)) {
                    bedBlocks.get(index).add(currentBlock);
                }
            }
        }

        return true;
    }

    private boolean isValidBedBlock(Block block) {
        return block.equals(Blocks.wool) || block.equals(Blocks.stained_hardened_clay) ||
                block.equals(Blocks.stained_glass) || block.equals(Blocks.planks) ||
                block.equals(Blocks.log) || block.equals(Blocks.log2) ||
                block.equals(Blocks.end_stone) || block.equals(Blocks.obsidian) ||
                block.equals(Blocks.water);
    }
}

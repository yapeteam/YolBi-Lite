package cn.yapeteam.yolbi.module.impl.render;

import cn.yapeteam.loader.logger.Logger;
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
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
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
                    BlockPos blockPos = new BlockPos(mc.player.posX + j, mc.player.posY + i, mc.player.posZ + k);
                    Block block = mc.world.getBlockState(blockPos).getBlock();

                    if (block == Blocks.BED) {
                        if (firstBed.getValue() && bed != null || mc.world.getBlockState(blockPos).getValue(BlockBed.PART) != BlockBed.EnumPartType.HEAD) {
                            return;
                        }

                        beds.add(blockPos);
                        bedBlocks.add(new ArrayList<>());
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
        for (int i = 0; i < beds.size(); i++) {
            BlockPos blockPos = beds.get(i);
            if (mc.world.getBlockState(blockPos).getBlock() != Blocks.BED) {
                continue;
            }

            renderBedDefense(blockPos);
        }
    }

    @Override
    public void onDisable() {
        beds.clear();
        bedBlocks.clear();
        bed = null;
    }

    private void renderBedDefense(BlockPos blockPos) {

        // Get the projected position of the block
        Vector4d projectedPos = ProjectionUtil.get(new BlockPos(blockPos.getX(), blockPos.getY() + 4, blockPos.getZ()));

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

        // Get the blocks around the bed defense
        List<Block> blocks = getBedBlocks(blockPos);

        Logger.info("Rendering bed at: " + blockPos + " with " + blocks.size() + " blocks");

        // Calculate the width of the rounded rectangle based on the number of blocks
        double rectangleWidth = Math.max(17.5, blocks.size() * 16 + 8); // Adjusted to accommodate larger icons
        RenderManager.roundedRectangle(screenX, screenY, rectangleWidth, 28, 4, new Color(0, 0, 0)); // Adjusted height for larger icons

        // Offset for placing block icons
        double offset = 4;

        // Render each block's texture icon with increased size
        for (Block block : blocks) {
            renderItemStack(new ItemStack(block), screenX + offset, screenY + 4, 16, 16); // Set custom width and height
            offset += 16.0; // Adjust spacing to accommodate larger icons
        }

        // Restore the previous OpenGL matrix state
        GlStateManager.disableBlend();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GlStateManager.popMatrix();
    }

    private void renderItemStack(ItemStack stack, double x, double y, int width, int height) {
        GlStateManager.pushMatrix();

        // Translate to the desired position
        GlStateManager.translate(x, y, 0);

        // Scale to match the custom width and height (icons bigger)
        double scale = width / 16.0;  // Scale based on the default 16x16 size of the item texture
        GlStateManager.scale(scale, scale, scale);

        GlStateManager.enableRescaleNormal();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        RenderHelper.enableGUIStandardItemLighting();

        // Render the item
        mc.getRenderItem().renderItemAndEffectIntoGUI(stack, 0, 0);
        mc.getRenderItem().renderItemOverlays(mc.fontRendererObj, stack, 0, 0);

        RenderHelper.disableStandardItemLighting();

        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }



    private List<Block> getBedBlocks(BlockPos bedPos) {
        List<Block> blocks = new ArrayList<>();

        // Get the facing direction of the bed (North, South, East, West)
        EnumFacing bedFacing = mc.world.getBlockState(bedPos).getValue(BlockBed.FACING);

        // Define directions around the bed based on the direction it is facing
        int[][] directions = {
                {0, 1, 0},    // Above
                {1, 0, 0},    // Right
                {-1, 0, 0},   // Left
                {0, 0, 1},    // Front
                {0, 0, -1},   // Back
                {1, 0, 1},    // Right Front
                {1, 0, -1},   // Right Back
                {-1, 0, 1},   // Left Front
                {-1, 0, -1},  // Left Back
                {1, 1, 0},    // Up-Right
                {-1, 1, 0},   // Up-Left
                {0, 1, 1},    // Up-Front
                {0, 1, -1},   // Up-Back
                {1, 1, 1},    // Up-Right Front
                {1, 1, -1},   // Up-Right Back
                {-1, 1, 1},   // Up-Left Front
                {-1, 1, -1}   // Up-Left Back
        };

        // Adjust positions based on the bed's facing direction (for the foot block)
        BlockPos footPos = adjustFootPosition(bedPos, bedFacing);

        // Loop through all directions once for the head and foot of the bed
        for (BlockPos currentPos : new BlockPos[]{bedPos, footPos}) {
            for (int[] dir : directions) {
                BlockPos posToCheck = currentPos.add(dir[0], dir[1], dir[2]);
                Block currentBlock = mc.world.getBlockState(posToCheck).getBlock();

                if (!currentBlock.equals(Blocks.AIR)) {
                    // Check if the block is a valid bed defense block and hasn't already been added
                    if (isValidBedBlock(currentBlock) && !blocks.contains(currentBlock)) {
                        blocks.add(currentBlock);
                    }

                    // Check if there's a LADDER attached and add it if not already in the list
                    if (checkIfLADDER(posToCheck) && !blocks.contains(Blocks.LADDER)) {
                        blocks.add(Blocks.LADDER);
                    }
                }
            }
        }

        return blocks;
    }

    private BlockPos adjustFootPosition(BlockPos bedPos, EnumFacing facing) {
        // Adjust the foot position based on the direction the bed is facing
        switch (facing) {
            case NORTH:
                return bedPos.south(); // Foot is one block south
            case SOUTH:
                return bedPos.north(); // Foot is one block north
            case WEST:
                return bedPos.east();  // Foot is one block east
            case EAST:
                return bedPos.west();  // Foot is one block west
            default:
                return bedPos; // Default to bedPos if something goes wrong
        }
    }




    private boolean checkIfLADDER(BlockPos blockPos) {
        // Define directions to check for LADDERs: right, left, front, back
        int[][] LADDERDirections = {
                {1, 0, 0},   // Right
                {-1, 0, 0},  // Left
                {0, 0, 1},   // Front
                {0, 0, -1}   // Back
        };

        for (int[] dir : LADDERDirections) {
            BlockPos neighborPos = blockPos.add(dir[0], dir[1], dir[2]);
            Block neighborBlock = mc.world.getBlockState(neighborPos).getBlock();

            // Check if the block is a LADDER
            if (neighborBlock.equals(Blocks.LADDER)) {
                return true;
            }
        }

        return false;
    }



    private boolean isValidBedBlock(Block block) {
        return block.equals(Blocks.WOOL) || block.equals(Blocks.STAINED_HARDENED_CLAY) ||
                block.equals(Blocks.STAINED_GLASS) || block.equals(Blocks.PLANKS) ||
                block.equals(Blocks.LOG) || block.equals(Blocks.LOG2) ||
                block.equals(Blocks.END_STONE) || block.equals(Blocks.OBSIDIAN) ||
                block.equals(Blocks.WATER) || block.equals(Blocks.LADDER);
    }
}

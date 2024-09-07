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
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@ModuleInfo(aliases = {"module.render.bedesp.name"}, description = "module.render.bedesp.description", category = Category.RENDER)
public class BedESP extends Module {
    NumberValue range = new NumberValue("Range", this, 10, 2, 30, 1);
    BooleanValue firstBed = new BooleanValue("Only render first bed", this, false);
    NumberValue rate = new NumberValue("Rate", this, 0.4, 0.1, 3, 0.1);

    private BlockPos[] bed = null;
    private List<BlockPos[]> beds = new ArrayList<>();
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

    private void renderBedDefense(BlockPos bedPos[]) {
        List<Block> defenseblocks = getBedDefense(bedPos);
        if (defenseblocks.isEmpty()) {
            return;
        }




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


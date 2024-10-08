package cn.yapeteam.yolbi.utils.player;


import net.minecraft.util.math.BlockPos;

public class BlockUtils {
    public static boolean isSamePos(BlockPos pos1, BlockPos pos2) {
        return pos1.getX() == pos2.getX() && pos1.getY() == pos2.getY() && pos1.getZ() == pos2.getZ();
    }
}

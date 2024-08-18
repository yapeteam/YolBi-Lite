package cn.yapeteam.yolbi.utils.render;

import cn.yapeteam.yolbi.utils.interfaces.IMinecraft;
import cn.yapeteam.yolbi.utils.vector.Vector2d;
import cn.yapeteam.yolbi.utils.vector.Vector2f;
import lombok.experimental.UtilityClass;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;

@UtilityClass
public class GuiUtil implements IMinecraft {
    public boolean mouseOver(final double posX, final double posY, final double width, final double height, final double mouseX, final double mouseY) {
        return mouseX > posX && mouseX < posX + width && mouseY > posY && mouseY < posY + height;
    }

    public boolean mouseOver(final Vector2d position, final Vector2d scale, final double mouseX, final double mouseY) {
        return mouseX > position.x && mouseX < position.x + scale.x && mouseY > position.y && mouseY < position.y + scale.y;
    }

    public boolean mouseOver(final Vector2f position, final Vector2f scale, final double mouseX, final double mouseY) {
        return mouseX > position.x && mouseX < position.x + scale.x && mouseY > position.y && mouseY < position.y + scale.y;
    }

    public boolean mouseOver(final Vector2f position, final Vector2f scale, final Vector2d mouse) {
        return mouse.x > position.x && mouse.x < position.x + scale.x && mouse.y > position.y && mouse.y < position.y + scale.y;
    }

    public boolean showCrosshair() {
        if (mc.gameSettings.showDebugInfo && !mc.thePlayer.hasReducedDebug() && !mc.gameSettings.reducedDebugInfo) {
            return false;
        } else if (mc.playerController.isSpectator()) {
            if (mc.pointedEntity != null) {
                return true;
            } else {
                if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                    BlockPos blockpos = mc.objectMouseOver.getBlockPos();
                    if (mc.theWorld.getTileEntity(blockpos) instanceof IInventory) {
                        return true;
                    }
                }

                return false;
            }
        } else {
            return true;
        }
    }
}

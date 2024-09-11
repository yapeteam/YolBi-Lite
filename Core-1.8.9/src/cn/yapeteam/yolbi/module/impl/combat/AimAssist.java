package cn.yapeteam.yolbi.module.impl.combat;

import cn.yapeteam.loader.Natives;
import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.game.EventTick;
import cn.yapeteam.yolbi.event.impl.render.EventRender2D;
import cn.yapeteam.yolbi.managers.RotationManager;
import cn.yapeteam.yolbi.managers.TargetManager;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.api.Category;
import cn.yapeteam.yolbi.module.api.ModuleInfo;
import cn.yapeteam.yolbi.utils.player.misc.VirtualKeyBoard;
import cn.yapeteam.yolbi.utils.player.PlayerUtil;
import cn.yapeteam.yolbi.utils.player.RayCastUtil;
import cn.yapeteam.yolbi.utils.math.vector.Vector2f;
import cn.yapeteam.yolbi.module.api.value.impl.BooleanValue;
import cn.yapeteam.yolbi.module.api.value.impl.ModeValue;
import cn.yapeteam.yolbi.module.api.value.impl.NumberValue;
import cn.yapeteam.yolbi.module.api.value.impl.SubMode;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


@ModuleInfo(aliases = {"module.combat.aimassist.name"}, description = "module.combat.aimassist.description", category = Category.COMBAT)
public class AimAssist extends Module {
    private final NumberValue Range = new NumberValue("Aim Range",this, 5, 3, 10, 1);

    public final ModeValue TargetPriority = new ModeValue("Target Priority", this)
            .add(new SubMode("Distance"))
            .add(new SubMode("Health"))
            .add(new SubMode( "Clip"))
            .add(new SubMode("Angle"))
            .setDefault("Clip");
    private final BooleanValue View = new BooleanValue("In View",this, true);
    private final BooleanValue WeaponOnly = new BooleanValue("Weapon Only",this, true);
    private final BooleanValue ClickAim = new BooleanValue("Click Aim",this, true);
    private final NumberValue rotSpeed = new NumberValue("Rotation Speed",this, 50f, 1f, 100f, .5f);

    private Entity target = null;

    RotationManager rotationManager = YolBi.instance.getRotationManager();

    @Listener
    private void onTick(EventTick e) {
        if (mc.thePlayer == null)
            return;
        if (mc.currentScreen != null) return;
        if (target != null && (target.isDead | target.getDistanceSqToEntity(mc.thePlayer) > Range.getValue().doubleValue()))
            target = null;
        if (TargetPriority.getValue().getName().equals("Clip"))
            target = PlayerUtil.getMouseOver(1, Range.getValue().doubleValue());
        else {
            target = getTargets();
        }
    }

    @Listener
    public void onUpdate(EventRender2D event) {
        if (mc.currentScreen != null || !mc.inGameHasFocus)
            return;
        if (WeaponOnly.getValue() && !PlayerUtil.holdingWeapon())
            return;
        if (target == null) return;
        if (ClickAim.getValue() && !Natives.IsKeyDown(VirtualKeyBoard.VK_LBUTTON))
            return;
        Vector2f movementcalc = YolBi.instance.getRotationManager().calcSmooth(new Vector2f(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch), new Vector2f((float) rotationManager.getRotationsNeeded(target)[0], (float) rotationManager.getRotationsNeeded(target)[1]), rotSpeed.getValue().doubleValue() * 0.1);
        double deltayaw = movementcalc.getX() - mc.thePlayer.rotationYaw; // we need to wrap this to -180 to 180 and multiply base on the speed
        double deltapitch = MathHelper.wrapAngleTo180_float(movementcalc.getY() - mc.thePlayer.rotationPitch);
        mc.thePlayer.rotationYaw += (float) (deltayaw * rotSpeed.getValue().doubleValue() * 0.1);
        mc.thePlayer.rotationPitch += (float) (deltapitch * rotSpeed.getValue().doubleValue() * 0.1);
    }

    public Entity getTargets() {
        // define targets first to eliminate any null pointer exceptions
        List<EntityLivingBase> targets = TargetManager.getTargets(Range.getValue().doubleValue());
        if (View.getValue())
            targets = targets.stream()
                    .filter(RayCastUtil::isInViewFrustrum)
                    .collect(Collectors.toList());
        if (TargetPriority.getValue().getName().equals("Distance"))
            targets.sort(Comparator.comparingDouble(o -> mc.thePlayer.getDistanceToEntity(o)));
        else if (TargetPriority.getValue().getName().equals("Health"))
            targets.sort(Comparator.comparingDouble(o -> ((EntityLivingBase) o).getHealth()));
        else if (TargetPriority.getValue().getName().equals("Angle"))
            targets.sort(Comparator.comparingDouble(entity -> YolBi.instance.getRotationManager().getRotationsNeeded(entity)[0]));
        return targets.isEmpty() ? null : targets.get(0);
    }
}

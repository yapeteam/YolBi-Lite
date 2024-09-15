package cn.yapeteam.yolbi.module.impl.combat;


import cn.yapeteam.loader.Natives;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.api.Category;
import cn.yapeteam.yolbi.module.api.ModuleInfo;
import cn.yapeteam.yolbi.module.api.value.impl.*;
import cn.yapeteam.yolbi.utils.math.MathUtils;
import cn.yapeteam.yolbi.utils.player.misc.VirtualKeyBoard;
import lombok.Getter;
import net.minecraft.item.ItemFood;
import net.minecraft.util.MovingObjectPosition;

import java.util.Random;

@ModuleInfo(aliases = {"module.combat.autoclicker.name"}, description = "module.combat.autoclicker.description", category = Category.COMBAT)
public class AutoClicker extends Module {
    private final BoundsNumberValue cps = new BoundsNumberValue("Cps", this, 17,20, 0, 50, 1);
    private final BooleanValue leftClick = new BooleanValue("leftClick",this, true),
            rightClick = new BooleanValue("rightClick",this, false);

    private final BooleanValue noeat = new BooleanValue("No Click When Eating",this, true);

    private final BooleanValue nomine = new BooleanValue("No Click When Mining",this, true);
    private final ListValue mode = new ModeValue("Mode", this)
            .add(new SubMode("Left"))
            .add(new SubMode("Right"))
            .setDefault("Left");

    private final NumberValue pressPercentage = new NumberValue("Click Percentage", this, 100, 0, 100, 1);
    
    private double delay = 1;

    @Override
    public void onEnable() {
        delay = generate(cps.getDefaultValue().doubleValue(), cps.getDefaultSecondValue().doubleValue());
        clickThread = new Thread(() -> {
            while (isEnabled()) {
                delay = generate(cps.getDefaultValue().doubleValue(), cps.getDefaultSecondValue().doubleValue());;
                try {
                    sendClick();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        clickThread.start();
    }

    @Getter
    private Thread clickThread = null;

    public AutoClicker() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Natives.SendLeft(false);
            Natives.SendRight(false);
        }));
    }

    private static final Random random = new Random();

    public static double generate(double cps, double range) {
        double mean = 1000.0 / MathUtils.getRandom(cps - range, cps + range);
        return mean + random.nextGaussian();
    }

    private final Runnable leftClickRunnable = () -> {
        try {
            float pressPercentageValue = pressPercentage.getValue().floatValue() / 100f;
            Natives.SendLeft(true);
            Thread.sleep((long) (1000 / delay * pressPercentageValue));
            Natives.SendLeft(false);
            Thread.sleep((long) (1000 / delay * (1 - pressPercentageValue)));
        } catch (InterruptedException ignored) {
        }
    };

    private final Runnable rightClickRunnable = () -> {
        try {
            float pressPercentageValue = pressPercentage.getValue().floatValue() / 100f;
            Natives.SendRight(true);
            Thread.sleep((long) (1000 / delay * pressPercentageValue));
            Natives.SendRight(false);
            Thread.sleep((long) (1000 / delay * (1 - pressPercentageValue)));
        } catch (InterruptedException ignored) {
        }
    };

    public void sendClick() throws InterruptedException {
        if (!isEnabled() || mc.currentScreen != null || mc.thePlayer == null) return;

        boolean left = leftClick.getValue() && Natives.IsKeyDown(VirtualKeyBoard.VK_LBUTTON);
        if (nomine.getValue() && mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
            left = false;
        boolean right = rightClick.getValue() && Natives.IsKeyDown(VirtualKeyBoard.VK_RBUTTON);
        if ((mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemFood) && noeat.getValue())
            right = false;

        if (left && right) {
            if (mode.getValue().equals("Left")) {
                leftClickRunnable.run();
                Thread.sleep((long) (1000 / delay)); // Delay between consecutive clicks
                rightClickRunnable.run();
            } else {
                rightClickRunnable.run();
                Thread.sleep((long) (1000 / delay)); // Delay between consecutive clicks
                leftClickRunnable.run();
            }
        } else {
            if (mode.getValue().equals("Left") && left) {
                leftClickRunnable.run();
                Thread.sleep((long) (1000 / delay)); // Ensure delay after each click
            } else if (right) {
                rightClickRunnable.run();
                return;
            }
            if (mode.getValue().equals("Right") && right) {
                rightClickRunnable.run();
                Thread.sleep((long) (1000 / delay)); // Ensure delay after each click
            } else if (left) {
                leftClickRunnable.run();
            }
        }
    }
}

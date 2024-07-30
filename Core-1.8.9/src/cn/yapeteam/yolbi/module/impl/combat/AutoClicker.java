package cn.yapeteam.yolbi.module.impl.combat;

import cn.yapeteam.loader.Natives;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.ModuleCategory;
import cn.yapeteam.yolbi.module.values.impl.BooleanValue;
import cn.yapeteam.yolbi.module.values.impl.ModeValue;
import cn.yapeteam.yolbi.module.values.impl.NumberValue;
import cn.yapeteam.yolbi.utils.misc.VirtualKeyBoard;
import cn.yapeteam.yolbi.utils.player.PlayerUtil;
import lombok.Getter;
import net.minecraft.item.ItemFood;
import net.minecraft.util.MovingObjectPosition;

import java.util.Random;

public class AutoClicker extends Module {
    private final NumberValue<Integer> cps = new NumberValue<>("cps", 17, 1, 100, 1);
    private final NumberValue<Double> range = new NumberValue<>("cps range", 1.5, 0.1d, 2.5d, 0.1);
    private final BooleanValue leftClick = new BooleanValue("leftClick", true),
            rightClick = new BooleanValue("rightClick", false);

    private final BooleanValue noeat = new BooleanValue("No Click When Eating", true);
    private final BooleanValue nomine = new BooleanValue("No Click When Mining", true);
    private final ModeValue<String> clickprio = new ModeValue<>("Click Priority", "Left", "Left", "Right");
    private double delay = 1;

    @Override
    public void onEnable() {
        delay = generateGaussian(cps.getValue(), range.getValue());
        clickThread = new Thread(() -> {
            while (isEnabled()) {
                PlayerUtil.sendMessage("delay: " + delay);
                delay = generateGaussian(cps.getValue(), range.getValue());
                sendClick();
                try {
                    clickThread.sleep((long) delay * 1000);
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
        super("AutoClicker", ModuleCategory.COMBAT);
        addValues(cps, range, leftClick, rightClick, noeat, nomine, clickprio);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Natives.SendLeft(false);
            Natives.SendRight(false);
        }));
    }

    private static final Random random = new Random();

    public static double generateGaussian(double cps, double range) {
        double mean = 1.0 / cps;
        double stdDev = range / 2.0;
        double delay = mean + Math.abs(random.nextGaussian() * stdDev);
        return Math.max(0.05, delay); // Ensure a minimum delay
    }

    private final Runnable leftClickRunnable = () -> {
        Natives.SendLeft(true);
        PlayerUtil.sendMessage("Left click sent");
        Natives.SendLeft(false);
    };

    private final Runnable rightClickRunnable = () -> {
        Natives.SendRight(true);
        PlayerUtil.sendMessage("Right click sent");
        Natives.SendRight(false);
    };

    public void sendClick() {
        if (!isEnabled() || mc.currentScreen != null || mc.thePlayer == null) return;

        boolean left = leftClick.getValue() && Natives.IsKeyDown(VirtualKeyBoard.VK_LBUTTON);
        if (nomine.getValue() && mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
            left = false;
        boolean right = rightClick.getValue() && Natives.IsKeyDown(VirtualKeyBoard.VK_RBUTTON);
        if ((mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemFood) && noeat.getValue())
            right = false;
        if (clickprio.getValue().equals("Left") && left) {
            leftClickRunnable.run();
        } else if (right) {
            rightClickRunnable.run();
            return;
        }
        if (clickprio.getValue().equals("Right") && right) {
            rightClickRunnable.run();
        } else if (left) {
            leftClickRunnable.run();
        }
    }

    @Override
    public String getSuffix() {
        return (cps.getValue() - range.getValue()) + " ~ " + (cps.getValue() + range.getValue());
    }
}

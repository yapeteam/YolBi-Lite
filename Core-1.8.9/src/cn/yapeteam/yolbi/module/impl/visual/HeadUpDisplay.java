package cn.yapeteam.yolbi.module.impl.visual;

import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.render.EventRender2D;
import cn.yapeteam.yolbi.font.AbstractFontRenderer;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.ModuleCategory;
import cn.yapeteam.yolbi.module.values.impl.BooleanValue;
import cn.yapeteam.yolbi.module.values.impl.ModeValue;
import cn.yapeteam.yolbi.utils.animation.Easing;
import cn.yapeteam.yolbi.utils.animation.EasingAnimation;
import cn.yapeteam.yolbi.utils.render.GradientBlur;
import cn.yapeteam.yolbi.utils.render.RenderUtil;
import lombok.val;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HeadUpDisplay extends Module {
    private ClientTheme theme = null;
    private final BooleanValue waterMark = new BooleanValue("Water Mark", true);
    private final BooleanValue moduleList = new BooleanValue("Module List", true);
    private final ModeValue<String> font = new ModeValue<>("Font", "PingFang", "Jello", "PingFang", "default");

    private final Map<Module, ModuleNode> moduleNodes = new HashMap<>();

    public HeadUpDisplay() {
        super("HUD", ModuleCategory.VISUAL);
        addValues(waterMark, moduleList, font);
    }

    private class ModuleNode {
        private final Module module;

        private ModuleNode(Module module) {
            this.module = module;
        }

        private final GradientBlur blur = new GradientBlur(GradientBlur.Type.TB);
        private final EasingAnimation animationY = new EasingAnimation(Easing.EASE_OUT_EXPO, 1000, 0);
        private final EasingAnimation animationX = new EasingAnimation(Easing.EASE_IN_OUT_QUAD, 1000, 0);
        private float x, y, width, height;
        private int color;

        public float update(ClientTheme theme, float y, int index, ScaledResolution sr) {
            double[] rect = getRect(getText(module), index, sr);
            x = (float) animationX.getValue(module.isEnabled() ? rect[0] : sr.getScaledWidth());
            this.y = (float) animationY.getValue(y);
            width = (float) rect[2];
            height = (float) rect[3];
            blur.updatePixels(x, this.y, width, height);
            color = theme.getColor((int) (this.y * 5));
            return module.isEnabled() || !animationX.isFinished() ? this.y + height : this.y;
        }

        public void render(float partialTicks) {
            AbstractFontRenderer font = getFontRenderer();
            RenderUtil.drawBloomShadow(x, y, width, height, 12, 15, color, false);
            blur.render(x, y, width, height, partialTicks, 1);
            RenderUtil.drawRect(x, y, x + width, y + height, new Color(0, 0, 0, 66).getRGB());
            String text = getText(module);
            font.drawString(text, x + 2.5, y + (height - font.getHeight()) / 2f + 0.5f, new Color(0, 0, 0).getRGB());
            font.drawString(text, x + 2, y + (height - font.getHeight()) / 2f, color, false);
        }
    }

    @Listener
    private void onRender(EventRender2D e) {
        if (theme == null)
            theme = YolBi.instance.getModuleManager().getModule(ClientTheme.class);
        val font = getFontRenderer();
        if (waterMark.getValue())
            font.drawString(YolBi.name + " " + YolBi.version, 2, 2, -1);
        if (moduleList.getValue()) {
            List<Module> activeModules = YolBi.instance.getModuleManager().getModules().stream()
                    .sorted(Comparator.comparingInt(m -> (int) -font.getStringWidth(getText(m))))
                    .collect(Collectors.toList());
            float y = 0;
            for (int i = 0; i < activeModules.size(); i++) {
                Module module = activeModules.get(i);
                ModuleNode node = moduleNodes.get(module);
                if (node == null) {
                    node = new ModuleNode(module);
                    moduleNodes.put(module, node);
                }
                y = node.update(theme, y, i, e.getScaledresolution());
            }
            for (Module module : activeModules) {
                ModuleNode node = moduleNodes.get(module);
                node.render(e.getPartialTicks());
            }
        }
    }

    private double[] getRect(String text, int index, ScaledResolution sr) {
        val font = getFontRenderer();
        double width = font.getStringWidth(text) + 4;
        float height = 12;
        double x = sr.getScaledWidth() - width;
        double y = index * height;
        return new double[]{x, y, width, height};
    }

    private String getText(Module module) {
        return module.getName() + (module.getSuffix() != null ? " " + module.getSuffix() : "");
    }

    private AbstractFontRenderer getFontRenderer() {
        switch (font.getValue()) {
            case "Jello":
                return YolBi.instance.getFontManager().getJelloRegular18();
            case "PingFang":
                return YolBi.instance.getFontManager().getPingFang18();
            case "default":
                return YolBi.instance.getFontManager().getDefault18();
        }
        return YolBi.instance.getFontManager().getDefault18();
    }
}

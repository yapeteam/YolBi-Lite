package cn.yapeteam.yolbi.module.impl.visual;

import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.render.EventSkijaRender;
import cn.yapeteam.yolbi.managers.RenderManager;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.ModuleCategory;
import cn.yapeteam.yolbi.module.values.impl.BooleanValue;
import cn.yapeteam.yolbi.module.values.impl.ModeValue;
import io.github.humbleui.skija.Font;
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

    static class ModuleNode {
        public void render(io.github.humbleui.skija.Font font, String text, ClientTheme theme, float x, float y, float width, float height, int index) {
            RenderManager.drawRect(x, y, width, height, new Color(0, 0, 0, 66).getRGB());
            RenderManager.drawText(text, font, x + 2.5f, y + (height - RenderManager.getFontHeight(font)) / 2f + 0.5f, new Color(0, 0, 0).getRGB());
            RenderManager.drawText(text, font, x + 2, y + (height - RenderManager.getFontHeight(font)) / 2f, theme.getColor(index * 200));
        }
    }

    @Listener
    private void onRender(EventSkijaRender e) {
        if (theme == null)
            theme = YolBi.instance.getModuleManager().getModule(ClientTheme.class);
        val font = getFont();
        if (waterMark.getValue())
            RenderManager.drawText(YolBi.name + " " + YolBi.version, font, 2, 2, -1);
        if (moduleList.getValue()) {
            List<Module> activeModules = YolBi.instance.getModuleManager().getModules().stream()
                    .filter(Module::isEnabled)
                    .sorted(Comparator.comparingInt(m -> (int) -RenderManager.getFontWidth(font, m.getName() + (m.getSuffix() != null ? " " + m.getSuffix() : ""))))
                    .collect(Collectors.toList());
            for (int i = 0; i < activeModules.size(); i++) {
                Module module = activeModules.get(i);
                String text = module.getName() + (module.getSuffix() != null ? " " + module.getSuffix() : "");
                double width = RenderManager.getFontWidth(font, text) + 4;
                float height = 12;
                double x = new ScaledResolution(mc).getScaledWidth() - width;
                double y = i * height;
                ModuleNode node = moduleNodes.computeIfAbsent(module, k -> new ModuleNode());
                node.render(font, text, theme, (float) x, (float) y, (float) width, height, i);
            }
        }
    }

    private Font getFont() {
        switch (font.getValue()) {
            case "Jello":
                return YolBi.instance.getFontManager().getJelloRegular18();
            case "PingFang":
                return YolBi.instance.getFontManager().getPingFang18();
        }
        return YolBi.instance.getFontManager().getPingFang18();
    }
}

package cn.yapeteam.yolbi.managers;


import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.render.EventRender2D;
import cn.yapeteam.yolbi.utils.render.layer.Layer;
import cn.yapeteam.yolbi.utils.render.layer.Layers;
import cn.yapeteam.yolbi.utils.render.shader.base.RiseShader;
import cn.yapeteam.yolbi.utils.render.shader.base.ShaderRenderType;
import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.renderer.GlStateManager;

import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class LayerManager {
    private final LinkedHashMap<Integer, LinkedHashMap<Layers, Layer>> layers = new LinkedHashMap<>();
    private static final int maxLayers = 3;

    @SneakyThrows
    public LayerManager() {
        for (int i = 0; i <= maxLayers; i++) {
            layers.put(i, new LinkedHashMap<>());

            for (Layers layer : Layers.values()) {
                this.layers.get(i).put(layer, new Layer(layer.getType().getShader() == null ? null :
                        (RiseShader) layer.getType().getShader().newInstance()));
            }
        }

        YolBi.instance.getEventManager().register(this);
    }

    public Layer get(Layers layer) {
        return get(layer, 0);
    }

    public Layer get(Layers layer, int group) {
        return this.layers.get(group).get(layer);
    }
    @Listener
    public void onRender2D(EventRender2D eventRender2D){
        GuiIngame guiIngame = Minecraft.getMinecraft().ingameGUI;
        guiIngame.renderGameOverlay(0);
        render(ShaderRenderType.OVERLAY);
    }

    private void render(ShaderRenderType type) {
        if (YolBi.DEVELOPMENT) {
            AtomicInteger active = new AtomicInteger();

            layers.forEach(((group, layers) -> layers.values().forEach(layer -> {
                if (layer.getShader() != null && !layer.getRunnables().isEmpty()) {
                    active.getAndIncrement();
                }
            })));

            if (active.get() > 2 && Minecraft.getMinecraft().currentScreen == null) {
                System.out.println("To many shader layers rendering " + active.get());
            }
        }

        layers.forEach(((group, layers) -> layers.values().forEach(layer -> layer.run(type))));
        layers.forEach((groups, map) -> map.forEach((layer, items) -> items.clear()));

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
//        GlStateManager.disableLighting();
        GlStateManager.enableAlpha();
    }
}
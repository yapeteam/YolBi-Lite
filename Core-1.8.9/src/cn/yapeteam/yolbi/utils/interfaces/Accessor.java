package cn.yapeteam.yolbi.utils.interfaces;

import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.utils.layer.Layer;
import cn.yapeteam.yolbi.managers.LayerManager;
import cn.yapeteam.yolbi.utils.layer.Layers;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.ui.standard.RiseClickGUI;
import cn.yapeteam.yolbi.ui.theme.Themes;
import net.minecraft.client.Minecraft;


public interface Accessor {
    Minecraft mc = Minecraft.getMinecraft();

    default YolBi getInstance() {
        return YolBi.instance;
    }

    default LayerManager getLayerManager() {
        return getInstance().getLayerManager();
    }

    default RiseClickGUI getClickGUI() {
        return getInstance().getClickGUI();
    }

    default Layer getLayer(Layers layer) {
        return getLayerManager().get(layer);
    }

    default Layer getLayer(Layers layer, int group) {
        return getLayerManager().get(layer, group);
    }


    default Themes getTheme() {
        return getInstance().getThemeManager().getTheme();
    }

    default <T extends Module> T getModule(final Class<T> clazz) {
        return getInstance().getModuleManager().get(clazz);
    }


    default Minecraft getClient() {
        return Minecraft.getMinecraft();
    }
}


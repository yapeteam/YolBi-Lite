package cn.yapeteam.yolbi.managers;


import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.impl.render.ClickGUI;
import cn.yapeteam.yolbi.module.impl.render.Interface;
import cn.yapeteam.yolbi.module.impl.render.Test;
import cn.yapeteam.yolbi.ui.standard.components.ModuleComponent;
import cn.yapeteam.yolbi.utils.AdaptiveMap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author Patrick
 * @since 10/19/2021
 */
@SuppressWarnings("unchecked")
public final class ModuleManager {
    private final List<ModuleComponent> allModuleComponents = new ArrayList<>();

    private AdaptiveMap<Class<? extends Module>, Module> moduleMap = new AdaptiveMap<>();

    /**
     * Called on client start
     */
    public void init() {
        moduleMap = new AdaptiveMap<>();

        this.put(Interface.class, new Interface());
        this.put(ClickGUI.class, new ClickGUI());
        this.put(Test.class, new Test());

        // Automatic initializations
        this.getAll().stream().filter(module -> module.getModuleInfo().autoEnabled()).forEach(module -> module.setEnabled(true));

        // Has to be a listener to handle the key presses
        YolBi.instance.getEventManager().register(this);
    }

    public ArrayList<Module> getAll() {
        return this.moduleMap.values();
    }

    public <T extends Module> T get(final Class<T> clazz) {
        return (T) this.moduleMap.get(clazz);
    }

    public <T extends Module> T get(final String name) {
        return this.getAll().stream()
                .filter(module -> module.getName().replace(" ", "").equalsIgnoreCase(name.replace(" ", "")))
                .map(module -> (T) module)
                .findAny()
                .orElse(null);
    }

    public void put(Class<? extends Module> clazz, Module module) {
        this.moduleMap.put(clazz, module);
    }

    public void remove(Module key) {
        this.moduleMap.removeValue(key);
        this.updateArraylistCache();
    }

    public boolean add(final Module module) {
        this.moduleMap.put(module);
        this.updateArraylistCache();

        return true;
    }

    private void updateArraylistCache() {
        allModuleComponents.clear();
        getAll().stream()
                .sorted(Comparator.comparingDouble(module -> -module.getName().length()))
                .forEach(module -> allModuleComponents.add(new ModuleComponent(module)));
    }
}

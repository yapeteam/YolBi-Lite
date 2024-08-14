package cn.yapeteam.yolbi.module.setting.impl;

import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.setting.Setting;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a sub-mode within a module.
 * This class extends the Module class and provides additional functionality specific to sub-modes.
 *
 * @param <T> the type of the parent module
 */
public abstract class SubMode<T extends Module> extends Module {
    protected final String name;
    protected final T parent;

    public SubMode(String name, @NotNull T parent) {
        super(parent.getName() + "$" + name, parent.moduleCategory());
        this.name = name;
        this.parent = parent;
    }

    public void register() {
        YolBi.instance.getModuleManager().addModule(this);
    }

    @Override
    public String getInfo() {
        return getPrettyName();
    }

    @Override
    public String getPrettyName() {
        return getRawPrettyName();
    }

    @Override
    public String getRawPrettyName() {
        return name;
    }

    @Override
    public void registerSetting(Setting setting) {
        super.registerSetting(setting);
    }
}

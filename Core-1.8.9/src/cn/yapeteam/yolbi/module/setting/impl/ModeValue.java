package cn.yapeteam.yolbi.module.setting.impl;

import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.setting.Setting;
import cn.yapeteam.yolbi.module.setting.interfaces.InputSetting;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class ModeValue extends Setting implements InputSetting {
    @Getter
    private final String settingName;
    @Getter
    private final Module parent;
    private final List<SubMode<?>> subModes = new ArrayList<>();
    private int selected = 0;
    public ModeValue(String settingName, Module parent) {
        this(settingName, parent, () -> true);
    }

    public ModeValue(String settingName, Module parent, String toolTip) {
        this(settingName, parent, () -> true, toolTip);
    }

    public ModeValue(String settingName, Module parent, Supplier<Boolean> visibleCheck) {
        this(settingName, parent, visibleCheck, null);
    }

    public ModeValue(String settingName, Module parent, Supplier<Boolean> visibleCheck, String toolTip) {
        super(settingName, visibleCheck, toolTip);
        this.settingName = settingName;
        this.parent = parent;
    }

    public ModeValue add(final SubMode<?> subMode) {
        if (subMode == null)
            return this;
        subModes.add(subMode);
        subMode.register();
        // register settings from SubModes
        for (Setting setting : subMode.getSettings()) {
            final Supplier<Boolean> fromVisibleCheck = setting.visibleCheck;
            setting.visibleCheck = () -> subModes.get((int) this.getInput()) == subMode && fromVisibleCheck.get();
            setting.viewOnly = true;
            parent.registerSetting(setting);
        }
        return this;
    }
    public List<SubMode<?>> getSubModeValues() {
        return subModes;
    }
    public ModeValue setDefaultValue(String name) {
        Optional<SubMode<?>> subMode = subModes.stream().filter(mode -> Objects.equals(mode.getName(), name)).findFirst();
        if (!subMode.isPresent()) return this;

        setValueRaw(subModes.indexOf(subMode.get()));
        return this;
    }
    @Override
    public void loadProfile(@NotNull JsonObject profile) {
        if (profile.has(getName()) && profile.get(getName()).isJsonPrimitive()) {
            JsonPrimitive jsonPrimitive = profile.getAsJsonPrimitive(getName());
            if (jsonPrimitive.isNumber()) {
                int newValue = jsonPrimitive.getAsInt();
                setValueRaw(newValue);
            }
        }
    }

    @Override
    public double getInput() {
        return this.selected;
    }

    @Override
    public void setValue(double value) {
        if (value > getMax() || value < getMin()) {
            this.selected = (int) getMin();
        } else {
            this.selected = (int) value;
        }
        if (this.parent.isEnabled() || !parent.canBeEnabled) {
            this.subModes.get(selected).enable();
        }
    }
    public void setValueRaw(int n) {
        disable();
        this.setValue(n);
    }
    public double getMax() {
        return subModes.size() - 1;
    }
    public double getMin() {
        return 0;
    }
    public void nextValue() {
        if (getInput() >= getMax()) {
            setValueRaw((int) getMin());
        } else {
            setValueRaw((int) (getInput() + 1));
        }
    }


    public void prevValue() {
        if (getInput() <= getMin()) {
            setValueRaw((int) getMax());
        } else {
            setValueRaw((int) (getInput() - 1));

        }
    }

    public void enable() {
        setValueRaw((int) getInput());
    }

    public void disable() {
        this.subModes.get(selected).disable();
    }

    public SubMode<?> getSelected() {
        return getSubModeValues().get((int) getInput());
    }
}
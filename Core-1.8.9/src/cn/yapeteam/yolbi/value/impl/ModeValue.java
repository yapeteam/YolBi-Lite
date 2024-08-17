package cn.yapeteam.yolbi.value.impl;


import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.value.Mode;
import cn.yapeteam.yolbi.value.Value;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * @author Patrick
 * @since 10/19/2021
 */
@Getter
public class ModeValue extends ListValue<Mode<?>> {

    private final List<Mode<?>> modes = new ArrayList<>();

    public ModeValue(final String name, final Module parent) {
        super(name, parent);
    }

    public ModeValue(final String name, final Mode<?> parent) {
        super(name, parent);
    }

    public ModeValue(final String name, final Module parent, final BooleanSupplier hideIf) {
        super(name, parent, hideIf);
    }

    public ModeValue(final String name, final Mode<?> parent, final BooleanSupplier hideIf) {
        super(name, parent, hideIf);
    }

    public void update(final Mode<?> value) {

        if (this.getParent() != null && (!(this.getParent() instanceof Module) || ((Module) this.getParent()).isEnabled())) {
            getValue().unregister();
            setValue(value);
            getValue().register();
        } else {
            setValue(value);
        }
    }

    public ModeValue add(final Mode<?>... modes) {
        if (modes == null) {
            return this;
        }

        this.modes.addAll(Arrays.asList(modes));
        return this;
    }

    public ModeValue setDefault(final String name) {
        setValue(modes.stream()
                .filter(mode -> mode.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(modes.get(0))
        );

        setDefaultValue(getValue());

        modes.forEach(mode -> mode.getValues().forEach(value -> value.setInternalHideIf(() -> mode != this.getValue())));

        return this;
    }

    public void setValue(final String name) {
        setValue(modes.stream()
                .filter(mode -> mode.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(modes.get(0))
        );
    }

    public JsonArray getAllSubValuesAsJson() {
        JsonArray allSubValuesJson = new JsonArray();
        for (Mode<?> mode : getModes()) {
            allSubValuesJson.add(new JsonPrimitive(mode.getName()));
        }
        return allSubValuesJson;
    }

    @Override
    public List<Value<?>> getSubValues() {
        ArrayList<Value<?>> allValues = new ArrayList<>();

        for (Mode<?> mode : getModes()) {
            allValues.addAll(mode.getValues());
        }

        return allValues;
    }
}
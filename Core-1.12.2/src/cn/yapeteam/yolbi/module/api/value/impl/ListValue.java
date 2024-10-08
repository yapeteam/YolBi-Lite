package cn.yapeteam.yolbi.module.api.value.impl;


import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.api.value.Mode;
import cn.yapeteam.yolbi.module.api.value.Value;
import cn.yapeteam.yolbi.ui.standard.components.value.impl.ListValueComponent;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * @author Strikeless
 * @since 01.07.2022
 */
@Getter
public class ListValue<T> extends Value<T> {

    private final List<T> modes = new ArrayList<>();

    public ListValue(final String name, final Module parent) {
        super(name, parent, null);
    }

    public ListValue(final String name, final Mode<?> parent) {
        super(name, parent, null);
    }

    public ListValue(final String name, final Module parent, final BooleanSupplier hideIf) {
        super(name, parent, null, hideIf);
    }

    public ListValue(final String name, final Mode<?> parent, final BooleanSupplier hideIf) {
        super(name, parent, null, hideIf);
    }

    public ListValue<T> add(final T... modes) {
        if (modes == null) {
            return this;
        }

        this.modes.addAll(Arrays.asList(modes));
        return this;
    }

    public ListValue<T> setDefault(final int index) {
        setValue(modes.get(index));
        return this;
    }

    public ListValue<T> setDefault(final T mode) {
        setValue(mode);
        return this;
    }

    @Override
    public List<Value<?>> getSubValues() {
        return null;
    }

    @Override
    public ListValueComponent createUIComponent() {
        return new ListValueComponent(this);
    }

    public JsonArray getSubValuesAsJson() {
        JsonArray subValues = new JsonArray();

        for (T mode : modes) {
            // Assuming mode.toString() returns the name of the mode
            subValues.add(new JsonPrimitive(mode.toString()));
        }

        return subValues;
    }
}
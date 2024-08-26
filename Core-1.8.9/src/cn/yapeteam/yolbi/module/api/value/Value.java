package cn.yapeteam.yolbi.module.api.value;


import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.ui.standard.components.value.ValueComponent;
import cn.yapeteam.yolbi.utils.interfaces.Toggleable;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * @author Patrick
 * @since 10/19/2021
 */
@Getter
@Setter
public abstract class Value<T> {

    private final String name;

    public BooleanSupplier hideIf, internalHideIf;

    private T value;
    private boolean visible;
    private Toggleable parent;

    private Consumer<T> valueChangeConsumer;
    @Setter
    private T defaultValue;

    public Value(final String name, final Module parent, final T defaultValue) {
        this.name = name;
        this.hideIf = null;
        this.parent = parent;
        this.defaultValue = defaultValue;
        this.setValue(defaultValue);
        parent.getValues().add(this);
    }

    public Value(final String name, final Mode<?> parent, final T defaultValue) {
        this.name = name;
        this.hideIf = null;
        this.defaultValue = defaultValue;
        this.parent = parent;
        this.setValue(defaultValue);
        parent.getValues().add(this);
    }

    public Value(final String name, final Module parent, final T defaultValue, final BooleanSupplier hideIf) {
        this.name = name;
        this.hideIf = hideIf;
        this.parent = parent;
        this.defaultValue = defaultValue;
        this.setValue(defaultValue);
        parent.getValues().add(this);
    }

    public Value(final String name, final Mode<?> parent, final T defaultValue, final BooleanSupplier hideIf) {
        this.name = name;
        this.hideIf = hideIf;
        this.defaultValue = defaultValue;
        this.setValue(defaultValue);
        parent.getValues().add(this);
    }

    public void setValueAsObject(final Object value) {
        if (this.valueChangeConsumer != null) this.valueChangeConsumer.accept((T) value);
        this.value = (T) value;
    }

    public void setValue(final T value) {
        if (this.valueChangeConsumer != null) this.valueChangeConsumer.accept(value);
        this.value = value;
    }

    public abstract List<Value<?>> getSubValues();

    public ValueComponent createUIComponent() {
        return null;
    }
    @Override
    public String toString() {
        return getName();
    }
}
package cn.yapeteam.yolbi.module.api.value.impl;


import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.api.value.Mode;
import cn.yapeteam.yolbi.module.api.value.Value;
import cn.yapeteam.yolbi.ui.standard.components.value.impl.ColorValueComponent;

import java.awt.*;
import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * @author Alan
 * @since 10/19/2022
 */
public class ColorValue extends Value<Color> {

    public ColorValue(final String name, final Module parent, final Color defaultValue) {
        super(name, parent, defaultValue);
    }

    public ColorValue(final String name, final Mode<?> parent, final Color defaultValue) {
        super(name, parent, defaultValue);
    }

    public ColorValue(final String name, final Module parent, final Color defaultValue, final BooleanSupplier hideIf) {
        super(name, parent, defaultValue, hideIf);
    }

    public ColorValue(final String name, final Mode<?> parent, final Color defaultValue, final BooleanSupplier hideIf) {
        super(name, parent, defaultValue, hideIf);
    }

    @Override
    public List<Value<?>> getSubValues() {
        return null;
    }

    @Override
    public ColorValueComponent createUIComponent() {
        return new ColorValueComponent(this);
    }
}
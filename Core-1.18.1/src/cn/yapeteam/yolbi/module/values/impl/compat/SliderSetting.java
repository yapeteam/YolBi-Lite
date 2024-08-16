package cn.yapeteam.yolbi.module.values.impl.compat;

import cn.yapeteam.yolbi.module.values.impl.NumberValue;

public class SliderSetting extends NumberValue<Double> {
    public SliderSetting(String name, double value, double min, double max, double inc) {
        super(name, value, min, max, inc);
    }

    public double getInput() {
        return this.getValue();
    }
}

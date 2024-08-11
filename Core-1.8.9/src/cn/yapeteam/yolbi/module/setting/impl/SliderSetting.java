package cn.yapeteam.yolbi.module.setting.impl;

import cn.yapeteam.yolbi.module.setting.Setting;
import cn.yapeteam.yolbi.module.setting.interfaces.InputSetting;
import cn.yapeteam.yolbi.utils.i18n.I18nModule;
import cn.yapeteam.yolbi.utils.i18n.settings.I18nSetting;
import cn.yapeteam.yolbi.utils.i18n.settings.I18nSliderSetting;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.function.Supplier;

public class SliderSetting extends Setting implements InputSetting {
    private final String settingName;
    @Getter
    private String[] options = null;
    private double defaultValue;
    @Getter
    private double max;
    @Getter
    private final double min;
    @Getter
    private final double intervals;
    public boolean isString;
    private String settingInfo = "";

    public SliderSetting(String settingName, double defaultValue, double min, double max, double intervals) {
        this(settingName, defaultValue, min, max, intervals, "");
    }

    public SliderSetting(String settingName, double defaultValue, double min, double max, double intervals, String settingInfo) {
        this(settingName, defaultValue, min, max, intervals, settingInfo, () -> true);
    }

    public SliderSetting(String settingName, double defaultValue, double min, double max, double intervals,
                         Supplier<Boolean> visibleCheck) {
        this(settingName, defaultValue, min, max, intervals, "", visibleCheck);
    }

    public SliderSetting(String settingName, double defaultValue, double min, double max, double intervals, String settingInfo,
                         Supplier<Boolean> visibleCheck) {
        super(settingName, visibleCheck, null);
        this.settingName = settingName;
        this.defaultValue = defaultValue;
        this.min = min;
        this.max = max;
        this.intervals = intervals;
        this.isString = false;
        this.settingInfo = settingInfo;
    }

    @Deprecated
    public SliderSetting(String settingName, String[] options, double defaultValue) {
        this(settingName, options, defaultValue, () -> true);
    }

    @Deprecated
    public SliderSetting(String settingName, String @NotNull [] options, double defaultValue, Supplier<Boolean> visibleCheck) {
        super(settingName, visibleCheck, null);
        this.settingName = settingName;
        this.options = options;
        this.defaultValue = defaultValue;
        this.min = 0;
        this.max = options.length - 1;
        this.intervals = 1;
        this.isString = true;
    }

    public String getPrettyInfo() {
        if (parent != null) {
            I18nModule i18nObject = parent.getI18nObject();
            if (i18nObject != null) {
                Map<Setting, I18nSetting> settings = i18nObject.getSettings();
                if (settings.containsKey(this)) {
                    return ((I18nSliderSetting) settings.get(this)).getSettingInfo();
                }
            }
        }
        return getInfo();
    }

    public String getInfo() {
        return this.settingInfo;
    }

    public void setOptions(String @NotNull [] options) {
        this.options = options;
        this.max = options.length - 1;
    }

    @Override
    public String getName() {
        return this.settingName;
    }

    @Override
    public double getInput() {
        return roundToInterval(this.defaultValue, 2);
    }

    @Override
    public void setValue(double n) {
        n = correctValue(n, this.min, this.max);
        n = (double) Math.round(n * (1.0D / this.intervals)) / (1.0D / this.intervals);
        this.defaultValue = n;
    }

    public void setValueRaw(double n) {
        this.defaultValue = n;
    }

    public static double correctValue(double v, double i, double a) {
        v = Math.max(i, v);
        v = Math.min(a, v);
        return v;
    }

    public static double roundToInterval(double v, int p) {
        if (p < 0) {
            return 0.0D;
        } else {
            BigDecimal bd = new BigDecimal(v);
            bd = bd.setScale(p, RoundingMode.HALF_UP);
            return bd.doubleValue();
        }
    }

    @Override
    public void loadProfile(JsonObject data) {
        if (data.has(getName()) && data.get(getName()).isJsonPrimitive()) {
            JsonPrimitive jsonPrimitive = data.getAsJsonPrimitive(getName());
            if (jsonPrimitive.isNumber()) {
                double newValue = jsonPrimitive.getAsDouble();
                setValue(newValue);
            }
        }
    }
}

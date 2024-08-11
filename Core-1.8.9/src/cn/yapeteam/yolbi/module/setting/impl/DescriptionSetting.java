package cn.yapeteam.yolbi.module.setting.impl;

import cn.yapeteam.yolbi.module.setting.Setting;
import cn.yapeteam.yolbi.utils.i18n.I18nModule;
import cn.yapeteam.yolbi.utils.i18n.settings.I18nDescriptionSetting;
import cn.yapeteam.yolbi.utils.i18n.settings.I18nSetting;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Supplier;

@Setter
@Getter
public class DescriptionSetting extends Setting {
    private String desc;

    public DescriptionSetting(String t) {
        this(t, () -> true);
    }

    public DescriptionSetting(String t, @NotNull Supplier<Boolean> visibleCheck) {
        this(t, visibleCheck, null);
    }

    public DescriptionSetting(String t, @NotNull Supplier<Boolean> visibleCheck, String toolTip) {
        super(t, visibleCheck, toolTip);
        this.desc = t;
    }

    public String getPrettyDesc() {
        if (parent != null) {
            I18nModule i18nObject = parent.getI18nObject();
            if (i18nObject != null) {
                Map<Setting, I18nSetting> settings = i18nObject.getSettings();
                if (settings.containsKey(this)) {
                    return ((I18nDescriptionSetting) settings.get(this)).getDesc();
                }
            }
        }
        return getDesc();
    }

    @Override
    public void loadProfile(JsonObject data) {
    }
}

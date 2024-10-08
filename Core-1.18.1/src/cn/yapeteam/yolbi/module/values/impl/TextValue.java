package cn.yapeteam.yolbi.module.values.impl;

import cn.yapeteam.ymixin.annotations.DontMap;
import cn.yapeteam.yolbi.module.values.Value;
import cn.yapeteam.yolbi.module.values.Visibility;

@DontMap
public class TextValue extends Value<String> {
    public TextValue(String name, String value) {
        super(name);
        this.name = name;
        this.value = value;
    }

    public TextValue(String name, Visibility visibility, String value) {
        super(name);
        this.name = name;
        setVisibility(visibility);
        this.value = value;
    }

    public TextValue(String name, String desc, String value) {
        super(name);
        this.name = name;
        this.desc = desc;
        this.value = value;
    }

    public TextValue(String name, String desc, Visibility visibility, String value) {
        super(name);
        this.name = name;
        this.desc = desc;
        setVisibility(visibility);
        this.value = value;
    }
}

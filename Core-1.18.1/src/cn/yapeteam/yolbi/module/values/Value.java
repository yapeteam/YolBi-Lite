package cn.yapeteam.yolbi.module.values;

import cn.yapeteam.ymixin.annotations.DontMap;
import lombok.Getter;
import lombok.Setter;

@DontMap
@Getter
public class Value<T> {
    protected T value;
    public String name;
    protected String desc;
    @Setter
    private ChangedCallback<T> callback = null;

    @Setter
    private Visibility visibility = () -> true;

    public Value(String name) {
        this.name = name;
    }

    public Value(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    public void setValue(T value) {
        if (callback != null)
            this.value = callback.run(this.value, value);
        else this.value = value;
    }
}

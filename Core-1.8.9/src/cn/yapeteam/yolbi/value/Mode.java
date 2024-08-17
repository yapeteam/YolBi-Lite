package cn.yapeteam.yolbi.value;


import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.utils.interfaces.Accessor;
import cn.yapeteam.yolbi.utils.interfaces.ThreadAccess;
import cn.yapeteam.yolbi.utils.interfaces.Toggleable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Rewritten from Patricks old version
 *
 * @author Hazsi
 * @since 10/10/2022
 */
@Getter
@RequiredArgsConstructor
public abstract class Mode<T> implements Accessor, Toggleable, ThreadAccess {
    private final String name;
    private final T parent;
    private final List<Value<?>> values = new ArrayList<>();

    public void register() {
        YolBi.instance.getEventManager().register(this);
        this.onEnable();
    }

    public void unregister() {
        YolBi.instance.getEventManager().unregister(this);
        this.onDisable();
    }

    @Override
    public void toggle() {
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }
}
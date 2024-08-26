package cn.yapeteam.yolbi.managers;


import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.module.api.Bindable;
import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.game.EventKey;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class BindableManager {
    
    public void init() {
        // Has to be a listener to handle the key presses
        YolBi.instance.getEventManager().register(this);
    }

    public List<Bindable> getBinds() {
        List<Bindable> bindableList = new ArrayList<>();

        bindableList.addAll(YolBi.instance.getModuleManager().getAll());

        return bindableList;
    }

    @Listener
    public void onkey(EventKey event) {
        Minecraft mc = Minecraft.getMinecraft();
        if(mc.currentScreen != null || event.isCancelled()) return;
        getBinds().stream()
                .filter(bind -> bind.getKey() == event.getKey())
                .forEach(Bindable::onKey);
    };

    public <T extends Bindable> T get(final String name) {
        return getBinds().stream()
                .filter(bindable -> bindable.getName().replace(" ", "").equalsIgnoreCase(name.replace(" ", "")))
                .map(bindable -> (T) bindable)
                .findAny()
                .orElse(null);
    }
}

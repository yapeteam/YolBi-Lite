package cn.yapeteam.yolbi.module.impl.render;

import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.module.Module;
import org.lwjgl.input.Keyboard;

public class ClickGui extends Module {
    public ClickGui() {
        super("ClickGui", Module.category.visual, Keyboard.KEY_RCONTROL);
    }

    @Override
    public void onEnable() {
        YolBi.instance.getEventManager().register(YolBi.instance.getClickGui());
        mc.displayGuiScreen(YolBi.instance.getClickGui());
    }

    @Override
    public void onDisable() {
        Keyboard.enableRepeatEvents(false);
        YolBi.instance.getEventManager().unregister(YolBi.instance.getClickGui());
        this.mc.displayGuiScreen(null);

        if (this.mc.currentScreen == null) {
            this.mc.setIngameFocus();
        }
    }


}

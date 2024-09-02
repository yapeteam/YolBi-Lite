package cn.yapeteam.yolbi.module.impl.render;

import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.game.EventKey;
import cn.yapeteam.yolbi.event.impl.render.EventRender2D;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.api.Category;
import cn.yapeteam.yolbi.module.api.ModuleInfo;
import cn.yapeteam.yolbi.utils.StopWatch;
import cn.yapeteam.yolbi.utils.layer.Layers;
import org.lwjgl.input.Keyboard;

@ModuleInfo(aliases = "module.render.clickgui.name", description = "module.render.clickgui.description", category = Category.RENDER, keyBind = Keyboard.KEY_RCONTROL)
public final class ClickGUI extends Module {
    private final StopWatch stopWatch = new StopWatch();

    @Override
    public void onEnable() {
        YolBi.instance.getEventManager().register(YolBi.instance.getClickGUI());
        mc.displayGuiScreen(YolBi.instance.getClickGUI());
        stopWatch.reset();
    }

    @Override
    public void onDisable() {
        mc.setIngameFocus();
        Keyboard.enableRepeatEvents(false);
        YolBi.instance.getEventManager().unregister(YolBi.instance.getClickGUI());
    }

    @Listener
    public void onRender2D(EventRender2D eventRender2D){
        getLayer(Layers.REGULAR, 2).add(() -> YolBi.instance.getClickGUI().render());
        getLayer(Layers.BLOOM, 3).add(() -> YolBi.instance.getClickGUI().bloom());
    }

    @Listener
    public void onKey(EventKey event){
        if (!stopWatch.finished(50)) return;

        if (event.getKey() == this.getKey()) {
            this.mc.displayGuiScreen(null);

            if (this.mc.currentScreen == null) {
                this.mc.setIngameFocus();
                this.toggle();
            }
        }
    }

//    @Listener
//    public void onRender2D(EventRender2D eventRender2D){
//        getLayer(Layers.BLOOM, 3).add(() -> rectangle());
//    }
//
//    public void rectangle(){
//        RenderManager.roundedRectangle(0, 0, 100, 100, 5, new Color(0, 0, 0, 255));
//    }


}

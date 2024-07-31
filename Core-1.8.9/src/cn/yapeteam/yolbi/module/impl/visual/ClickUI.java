package cn.yapeteam.yolbi.module.impl.visual;

import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.ModuleCategory;
import cn.yapeteam.yolbi.ui.YolbiRiseUI;
import org.lwjgl.input.Keyboard;

public class ClickUI extends Module {

    //public  final BooleanValue notiff = notification;
    public ClickUI() {
        super("ClickGUI", ModuleCategory.VISUAL, Keyboard.KEY_RCONTROL);
    }

    @Override
    public void onEnable(){
        YolbiRiseUI.initGui();
    }
}

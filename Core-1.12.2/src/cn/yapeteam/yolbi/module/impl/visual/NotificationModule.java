package cn.yapeteam.yolbi.module.impl.visual;

import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.ModuleCategory;

public class NotificationModule extends Module {
    public static NotificationModule instance;

    public NotificationModule() {
        super("Notification", ModuleCategory.VISUAL);
        instance = this;
    }
}

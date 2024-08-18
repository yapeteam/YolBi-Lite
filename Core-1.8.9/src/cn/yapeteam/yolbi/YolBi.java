package cn.yapeteam.yolbi;

import cn.yapeteam.loader.VersionInfo;
import cn.yapeteam.loader.logger.Logger;
import cn.yapeteam.yolbi.bindable.BindableManager;
import cn.yapeteam.yolbi.config.ConfigManager;
import cn.yapeteam.yolbi.event.EventManager;
import cn.yapeteam.yolbi.event.impl.client.EventClientShutdown;
import cn.yapeteam.yolbi.layer.LayerManager;
import cn.yapeteam.yolbi.managers.BotManager;
import cn.yapeteam.yolbi.managers.RotationManager;
import cn.yapeteam.yolbi.managers.TargetManager;
import cn.yapeteam.yolbi.module.api.manager.ModuleManager;
import cn.yapeteam.yolbi.server.WebServer;
import cn.yapeteam.yolbi.ui.standard.RiseClickGUI;
import cn.yapeteam.yolbi.ui.theme.ThemeManager;
import cn.yapeteam.yolbi.utils.interfaces.IMinecraft;
import lombok.Getter;

import java.io.File;
import java.io.IOException;

@Getter
public class YolBi implements IMinecraft {
    public static YolBi instance = new YolBi();
    public static final String name = "YolBi";
    public static final String version = VersionInfo.version;
    public static final File YOLBI_DIR = new File(System.getProperty("user.home"), ".yolbi");
    public static boolean DEVELOPMENT = false;
    public static boolean initialized = false;
    private EventManager eventManager;
    private ConfigManager configManager;
    private ModuleManager moduleManager;
    private BotManager botManager;
    private TargetManager targetManager;
    private RotationManager rotationManager;
    private LayerManager layerManager;
    private ThemeManager themeManager;
    private RiseClickGUI clickGUI;
    private BindableManager bindableManager;

    @Getter
    private static final long startMillisTime = System.currentTimeMillis();

    public EventManager getEventManager() {
        if (eventManager == null)
            eventManager = new EventManager();
        return eventManager;
    }

    public RotationManager getRotationManager() {
        if (rotationManager == null)
            rotationManager = new RotationManager();
        return rotationManager;
    }

    public static void initialize() {
        if (initialized || instance == null) return;
        initialized = true;
        boolean ignored = YOLBI_DIR.mkdirs();
        if (instance.eventManager == null)
            instance.eventManager = new EventManager();
        if (instance.rotationManager == null)
            instance.rotationManager = new RotationManager();
        mc.addScheduledTask(() -> instance.clickGUI = new RiseClickGUI());
        mc.addScheduledTask(() -> instance.layerManager = new LayerManager());
        instance.bindableManager = new BindableManager();
        instance.configManager = new ConfigManager();
        instance.moduleManager = new ModuleManager();
        instance.botManager = new BotManager();
        instance.targetManager = new TargetManager();
        instance.themeManager = new ThemeManager();
        instance.eventManager.register(instance.moduleManager);
        instance.eventManager.register(instance.botManager);
        instance.eventManager.register(instance.targetManager);
        instance.eventManager.register(instance.rotationManager);
        instance.eventManager.register(instance.themeManager);
        instance.moduleManager.init();
        instance.bindableManager.init();
        try {
            instance.getConfigManager().load();
            WebServer.start();
        } catch (Throwable e) {
            Logger.exception(e);
        }
    }

    public void shutdown() {
        try {
            Logger.info("Shutting down Yolbi Lite");
            eventManager.post(new EventClientShutdown());
            configManager.save();
            WebServer.stop();
            instance = new YolBi();
            System.gc();
        } catch (IOException e) {
            Logger.exception(e);
        }
    }
}

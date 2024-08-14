package cn.yapeteam.yolbi;

import cn.yapeteam.loader.VersionInfo;
import cn.yapeteam.loader.logger.Logger;
import cn.yapeteam.yolbi.command.CommandManager;
import cn.yapeteam.yolbi.config.ConfigManager;
import cn.yapeteam.yolbi.event.EventManager;
import cn.yapeteam.yolbi.event.impl.client.EventClientShutdown;
import cn.yapeteam.yolbi.font.FontManager;
import cn.yapeteam.yolbi.managers.*;
import cn.yapeteam.yolbi.module.ModuleManager;
import cn.yapeteam.yolbi.notification.Notification;
import cn.yapeteam.yolbi.notification.NotificationManager;
import cn.yapeteam.yolbi.notification.NotificationType;
import cn.yapeteam.yolbi.server.WebServer;
import cn.yapeteam.yolbi.shader.Shader;
import cn.yapeteam.yolbi.ui.YolbiClickGui;
import cn.yapeteam.yolbi.utils.animation.Easing;
import cn.yapeteam.yolbi.utils.render.ESPUtil;
import lombok.Getter;

import java.io.File;
import java.io.IOException;

@Getter
public class YolBi {
    public static YolBi instance = new YolBi();
    public static final String name = "YolBi Lite";
    public static final String version = VersionInfo.version;
    public static final File YOLBI_DIR = new File(System.getProperty("user.home"), ".yolbi");
    public static boolean initialized = false;

    public EventManager getEventManager() {
        if (eventManager == null)
            eventManager = new EventManager();
        return eventManager;
    }

    public FontManager getFontManager() {
        if (fontManager == null)
            fontManager = new FontManager();
        return fontManager;
    }

    public RotationManager getRotationManager() {
        if (rotationManager == null)
            rotationManager = new RotationManager();
        return rotationManager;
    }

    private EventManager eventManager;

    private CommandManager commandManager;

    private ConfigManager configManager;

    private ModuleManager moduleManager;

    private FontManager fontManager;

    private NotificationManager notificationManager;

    private BotManager botManager;

    private TargetManager targetManager;

    private RenderManager renderManager;

    private RotationManager rotationManager;

    private ColorManager colorManager;

    private YolbiClickGui clickGui;

    public static void initialize() {
        if (initialized || instance == null) return;
        initialized = true;
        boolean ignored = YOLBI_DIR.mkdirs();
        if (instance.eventManager == null)
            instance.eventManager = new EventManager();
        if (instance.rotationManager == null)
            instance.rotationManager = new RotationManager();
        instance.commandManager = new CommandManager();
        instance.configManager = new ConfigManager();
        instance.moduleManager = new ModuleManager();
        instance.fontManager = new FontManager();
        instance.colorManager = new ColorManager();
        instance.clickGui = new YolbiClickGui();
        instance.botManager = new BotManager();
        instance.targetManager = new TargetManager();
        instance.notificationManager = new NotificationManager();
        instance.renderManager = new RenderManager();
        instance.eventManager.register(instance.commandManager);
        instance.eventManager.register(instance.moduleManager);
        instance.eventManager.register(instance.botManager);
        instance.eventManager.register(instance.targetManager);
        instance.eventManager.register(instance.rotationManager);
        instance.eventManager.register(instance.notificationManager);
        instance.eventManager.register(instance.renderManager);
        instance.eventManager.register(instance.fontManager);
        instance.eventManager.register(Shader.class);
        instance.eventManager.register(ESPUtil.class);
        instance.moduleManager.register();
        try {
            instance.getConfigManager().load();
            WebServer.start();
        } catch (Throwable e) {
            Logger.exception(e);
        }
        instance.getNotificationManager().post(
                new Notification(
                        "Injected Yolbi successfully",
                        Easing.EASE_IN_OUT_QUAD,
                        Easing.EASE_IN_OUT_QUAD,
                        15000, NotificationType.INIT
                )
        );
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

package cn.yapeteam.yolbi.module;


import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.module.setting.Setting;
import cn.yapeteam.yolbi.module.setting.impl.ButtonSetting;
import cn.yapeteam.yolbi.module.setting.impl.ModeValue;
import cn.yapeteam.yolbi.utils.i18n.I18nModule;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.Iterator;

@Getter
public class Module {
    @Getter
    @Setter
    private @Nullable I18nModule i18nObject = null;

    @Getter
    protected final ArrayList<Setting> settings;
    private final String moduleName;
    private String prettyName;
    private String prettyInfo = "";
    private final category moduleCategory;
    @Getter
    @Setter
    private boolean enabled;
    @Getter
    private int keycode;
    private final @Nullable String toolTip;
    protected static Minecraft mc;
    private boolean isToggled = false;
    public boolean canBeEnabled = true;
    public boolean ignoreOnSave = false;
    @Setter
    @Getter
    public boolean hidden = false;

    public Module(String moduleName, category moduleCategory, int keycode) {
        this(moduleName, moduleCategory, keycode, null);
    }

    public Module(String moduleName, category moduleCategory, int keycode, @Nullable String toolTip) {
        this.moduleName = moduleName;
        this.prettyName = moduleName;
        this.moduleCategory = moduleCategory;
        this.keycode = keycode;
        this.toolTip = toolTip;
        this.enabled = false;
        mc = Minecraft.getMinecraft();
        this.settings = new ArrayList<>();
    }

    public static Module getModule(Class<? extends Module> a) {
        Iterator<Module> var1 = ModuleManager.modules.iterator();

        Module module;
        do {
            if (!var1.hasNext()) {
                return null;
            }

            module = var1.next();
        } while (module.getClass() != a);

        return module;
    }

    public Module(String name, category moduleCategory) {
        this(name, moduleCategory, null);
    }

    public Module(String name, category moduleCategory, String toolTip) {
        this(name, moduleCategory, 0, toolTip);
    }

    public void keybind() {
        if (this.keycode != 0) {
            try {
                if (!this.isToggled && (this.keycode >= 1000 ? Mouse.isButtonDown(this.keycode - 1000) : Keyboard.isKeyDown(this.keycode))) {
                    this.toggle();
                    this.isToggled = true;
                } else if ((this.keycode >= 1000 ? !Mouse.isButtonDown(this.keycode - 1000) : !Keyboard.isKeyDown(this.keycode))) {
                    this.isToggled = false;
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                this.keycode = 0;
            }
        }
    }

    public boolean canBeEnabled() {
        return this.canBeEnabled;
    }

    public void enable() {
        if (!this.canBeEnabled() || this.isEnabled()) {
            return;
        }
        this.setEnabled(true);
        ModuleManager.organizedModules.add(this);
        YolBi.instance.getEventManager().register(this);
        this.onEnable();
    }

    public void disable() {
        if (!this.isEnabled()) {
            return;
        }
        this.setEnabled(false);
        ModuleManager.organizedModules.remove(this);

        YolBi.instance.getEventManager().unregister(this);
        this.onDisable();
    }

    public String getInfo() {
        return "";
    }

    public String getPrettyInfo() {
        return getInfo();
    }

    public String getName() {
        return this.moduleName;
    }

    public String getPrettyName() {
        return i18nObject != null ? i18nObject.getName() : getName();
    }

    public @Nullable String getToolTip() {
        return toolTip;
    }

    public @Nullable String getPrettyToolTip() {
        return i18nObject != null ? i18nObject.getToolTip() : getToolTip();
    }

    public String getRawPrettyName() {
        return prettyName;
    }

    public String getRawPrettyInfo() {
        return prettyInfo.isEmpty() ? getInfo() : prettyInfo;
    }

    public void setPrettyName(String name) {
        this.prettyName = name;
        ModuleManager.sort();
    }

    public void setPrettyInfo(String name) {
        this.prettyInfo = name;
        ModuleManager.sort();
    }

    public void registerSetting(Setting setting) {
        synchronized (settings) {
            if (settings.contains(setting))
                throw new RuntimeException("Setting '" + setting.getName() + "' is already registered in module '" + this.getName() + "'!");

            setting.setParent(this);
            if (setting instanceof ModeValue) {
                this.settings.add(0, setting);
            } else {
                this.settings.add(setting);
            }
        }
    }

    public void registerSetting(Setting @NotNull ... setting) {
        for (Setting set : setting) {
            registerSetting(set);
        }
    }

    public void unregisterSetting(Setting setting) {
        synchronized (settings) {
            this.settings.remove(setting);
        }
    }

    public category moduleCategory() {
        return this.moduleCategory;
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public void toggle() {
        if (this.isEnabled()) {
            this.disable();
//            if (Settings.toggleSound.getInput() != 0) mc.thePlayer.playSound(Settings.getToggleSound(false), 1, 1);
//            if (Notifications.moduleToggled.isToggled() && !(this instanceof Gui))
//                Notifications.sendNotification(Notifications.NotificationTypes.INFO, "§4Disabled " + this.getPrettyName());
        } else {
            this.enable();
//            if (Settings.toggleSound.getInput() != 0) mc.thePlayer.playSound(Settings.getToggleSound(true), 1, 1);
//            if (Notifications.moduleToggled.isToggled() && !(this instanceof Gui))
//                Notifications.sendNotification(Notifications.NotificationTypes.INFO, "§2Enabled " + this.getPrettyName());
        }

    }

    public void onUpdate() {
    }

    public void guiUpdate() {
    }

    public void guiButtonToggled(ButtonSetting b) {
    }

    public void setBind(int keybind) {
        this.keycode = keybind;
    }


    public enum category {
        combat,
        movement,
        player,
        world,
        visual,
        minigames,
        fun,
        other,
        client,
        profiles,
        scripts,
        exploit,
        experimental,
        search
    }
}

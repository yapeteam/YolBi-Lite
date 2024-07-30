package cn.yapeteam.yolbi.module;

import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.game.EventKey;
import cn.yapeteam.yolbi.module.impl.combat.*;
import cn.yapeteam.yolbi.module.impl.misc.*;
import cn.yapeteam.yolbi.module.impl.movement.*;
import cn.yapeteam.yolbi.module.impl.player.ChestStealer;
import cn.yapeteam.yolbi.module.impl.player.MurdererFinder;
import cn.yapeteam.yolbi.module.impl.player.AutoArmor;
import cn.yapeteam.yolbi.module.impl.visual.*;
import cn.yapeteam.yolbi.module.impl.world.FastPlace;
import cn.yapeteam.yolbi.module.values.impl.BooleanValue;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Getter
@SuppressWarnings({"unchecked", "unused"})
public class ModuleManager {
    private final List<Module> modules = new CopyOnWriteArrayList<>();
   // private final BooleanValue notif = ClickUI.notification.getValue();
    public void load() {
        modules.add(new AimAssist());
        modules.add(new AntiBot());
        modules.add(new AutoClicker());
        modules.add(new ClickUI());
        // modules.add(new Backtrack());
        //modules.add(new IRC()); // To be tested
        // modules.add(new BlatantVelocity());
        // modules.add(new Criticals());
        // modules.add(new FakeLag());
        modules.add(new KillAura());
        // modules.add(new Reach());
        modules.add(new CombatSettings());
        modules.add(new Velocity());
        modules.add(new WTap());
        modules.add(new AntiInvisible());
        modules.add(new AutoArmor());
        modules.add(new ChestStealer());
        modules.add(new ClientSpoof());
        modules.add(new NoteBot());
        modules.add(new SelfDestruct());
        modules.add(new Eagle());
        modules.add(new NoSlow());
        modules.add(new MoveFix());
        //modules.add(new Scaffold());
        modules.add(new Sprint());
        modules.add(new StrafeFix());
        modules.add(new ESP());
        modules.add(new MurdererFinder());
        modules.add(new FastPlace());


        modules.sort((m1, m2) -> -Integer.compare(m2.getName().charAt(0), m1.getName().charAt(0)));
    }

    @Listener
    private void onKey(EventKey e) {
        modules.stream().filter(m -> m.getKey() == e.getKey()).collect(Collectors.toList()).forEach(module -> {
            module.toggle();
        });
    }

    public <T extends Module> T getModule(Class<T> clazz) {
        return (T) modules.stream().filter(m -> m.getClass().equals(clazz)).findFirst().orElse(null);
    }

    public <T extends Module> T getModuleByName(String name) {
        return (T) modules.stream().filter(m -> m.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public List<Module> getModulesByCategory(ModuleCategory category) {
        return modules.stream().filter(m -> m.getCategory() == category).collect(Collectors.toList());
    }
}
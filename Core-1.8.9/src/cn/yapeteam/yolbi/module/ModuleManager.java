package cn.yapeteam.yolbi.module;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ModuleManager {
    static List<Module> modules = new ArrayList<>();
    public static List<Module> organizedModules = new ArrayList<>();

    public void register() {

        modules.sort(Comparator.comparing(Module::getPrettyName));
    }

    public void addModule(Module m) {
        modules.add(m);
    }

    public List<Module> getModules() {
        return modules;
    }

    public List<Module> inCategory(Module.category category) {
        ArrayList<Module> categoryML = new ArrayList<>();

        for (Module mod : this.getModules()) {
            if (mod.moduleCategory().equals(category)) {
                categoryML.add(mod);
            }
        }

        return categoryML;
    }

    public Module getModule(String moduleName) {
        for (Module module : modules) {
            if (module.getName().equals(moduleName)) {
                return module;
            }
        }
        return null;
    }

    public static void sort() {
//        if (HUD.alphabeticalSort.isToggled()) {
//            organizedModules.sort(Comparator.comparing(Module::getPrettyName));
//        } else {
//            organizedModules.sort((o1, o2) -> Utils.mc.fontRendererObj.getStringWidth(o2.getPrettyName() + ((HUD.showInfo.isToggled() && !o2.getInfo().isEmpty()) ? " " + o2.getInfo() : "")) - Utils.mc.fontRendererObj.getStringWidth(o1.getPrettyName() + (HUD.showInfo.isToggled() && !(o1.getInfo().isEmpty()) ? " " + o1.getInfo() : "")));
//        }
    }
}
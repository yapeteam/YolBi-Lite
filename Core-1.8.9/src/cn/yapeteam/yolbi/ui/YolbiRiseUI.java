package cn.yapeteam.yolbi.ui;
import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.managers.FontManager;
import cn.yapeteam.yolbi.managers.RenderManager;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.utils.IMinecraft;
import cn.yapeteam.yolbi.utils.vector.Vector2f;
import io.github.humbleui.skija.Font;
import lombok.experimental.UtilityClass;


import java.text.Collator;
import java.util.concurrent.ConcurrentLinkedQueue;

@UtilityClass
public class YolbiRiseUI implements IMinecraft {

    public Vector2f position = new Vector2f(0, 0);

    public ConcurrentLinkedQueue<Module> moduleList = new ConcurrentLinkedQueue<>();

    public void initGui() {
        moduleList.clear();
        java.util.List<Module> sortedModules = YolBi.instance.getModuleManager().getModules();
        sortedModules.sort((o1, o2) -> Collator.getInstance().compare(o1.getName(), o2.getName()));
        sortedModules.forEach(module -> moduleList.add(module));
        DrawUI();
    }


    public void onGuiClosed() {

    }

    private void DrawUI(){
        // Draw a rounded rectangle with clipping
        RenderManager.drawRoundedRect(position.x, position.y, 800, 600, 15, 0xFF171A21);

        RenderManager.drawRect(position.x, position.y, 200, 600, 0xFF121419);

        RenderManager.drawRoundedRect(position.x + 225, position.y + 50, 550, 100, 15, 0xFF121419);

        Font Nunito20 = FontManager.instance.getNunito20();
        RenderManager.drawText("type here to search", Nunito20, position.x + 225, position.y + 30, 0xFFA0A0A0);

        RenderManager.drawText("Yolbi", FontManager.instance.getSans32(), position.x + 28, position.y + 36 + 16, 0xFFFFFFFF);

        RenderManager.drawText("1.0beta", FontManager.instance.getSans16(), position.x + 102, position.y + 32, 0xFFA0A0A0);
    }

}

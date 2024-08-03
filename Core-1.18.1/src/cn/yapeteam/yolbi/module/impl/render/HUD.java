package cn.yapeteam.yolbi.module.impl.render;

import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.render.EventRender2D;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.ModuleCategory;
import cn.yapeteam.yolbi.module.values.impl.NumberValue;
import cn.yapeteam.yolbi.utils.font.FontManager;
import cn.yapeteam.yolbi.utils.render.ColorUtils;
import cn.yapeteam.yolbi.utils.render.RenderManager;
import cn.yapeteam.yolbi.utils.vector.Vector2d;
import cn.yapeteam.yolbi.wrappers.ScaledResolutionWrapper;
import com.mojang.blaze3d.platform.InputConstants;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class HUD extends Module {
    public HUD() {
        super("HUD", ModuleCategory.RENDER, InputConstants.KEY_H);
    }
    public NumberValue<Integer> mixColor1=new NumberValue<>("MixColor1",0xffffff,0x000000,0xffffff,1);
    public NumberValue<Integer> mixColor2=new NumberValue<>("MixColor2",0xffffff,0x000000,0xffffff,1);

    @Listener
    public void onRender2D(EventRender2D e){

    }
    private void renderArrayList() {
        int yCount = 0;
        int index = 0;
        long x = 0;
        List<Module> mods = YolBi.instance.getModuleManager().getModules();
        ArrayList<Module> running=new ArrayList<Module>();

        for (Module m : mods) {
            if (m.isEnabled())
                running.add(m);

        }
        for(Module ignored :running)
            for (int i = 0, runningSize = running.size(); i < runningSize; i++) {
                Module m = running.get(i);
                if (i<runningSize-1&& FontManager.tenacity.getStringWidth((m.getName()))<FontManager.tenacity.getStringWidth(running.get(i+1).getName())){
                    running.set(i,running.get(i+1));
                    running.set(i+1,m);

                }
            }

        for(Module m:running){
            ScaledResolutionWrapper sr=new ScaledResolutionWrapper(mc);
            double offset = yCount * (FontManager.tenacity.getFontHeight("F") + 2);//Minecraft.getInstance().font.width(

            if (m.isEnabled()) {
                int color= ColorUtils.mixColors(new Color((int) mixColor1.getValue()),new Color((int) mixColor2.getValue()),ColorUtils.getBlendFactor(new Vector2d(sr.getScaledWidth() -FontManager.tenacity.getStringWidth(m.getName())-6, (int) offset))).getRGB();
                RenderManager.drawRoundedRect(sr.getScaledWidth(), (int) (6 + offset),sr.getScaledWidth()+2,(int) (2 + offset+FontManager.tenacity.getFontHeight("F")),3,new Color(color).getRGB());
                RenderManager.drawRoundedRect((int) (sr.getScaledWidth() - FontManager.tenacity.getStringWidth(m.getName())-6), (int) offset+6, sr.getScaledWidth(), (int) (FontManager.tenacity.getFontHeight("F") + offset+4),5,new Color(225, 242, 255, 105).getRGB());
                FontManager.tenacity.drawStringWithShadow(RenderManager.currentPoseStack,m.getName(),
                        (float) (sr.getScaledWidth() -FontManager.tenacity.getStringWidth(m.getName())-4), (int) (offset+8),color);//FontManager.tenacity.drawStringWithShadow( m.getName(), sr.getScaledWidth() -FontManager.tenacity.getStringWidth(m.getName())-4, (int) (4 + offset),color);
                yCount++;
                index++;
                x++;
            }
        }
        //System.out.println("hud");
    }


}

package cn.yapeteam.yolbi.module.impl.render;

import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.render.EventRender2D;
import cn.yapeteam.yolbi.managers.RenderManager;
import cn.yapeteam.yolbi.managers.TargetManager;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.api.Category;
import cn.yapeteam.yolbi.module.api.ModuleInfo;
import cn.yapeteam.yolbi.module.api.value.impl.BooleanValue;
import cn.yapeteam.yolbi.module.api.value.impl.ModeValue;
import cn.yapeteam.yolbi.module.api.value.impl.NumberValue;
import cn.yapeteam.yolbi.module.api.value.impl.SubMode;
import cn.yapeteam.yolbi.utils.math.vector.Vector4d;
import cn.yapeteam.yolbi.utils.player.PlayerUtil;
import cn.yapeteam.yolbi.utils.render.ColorUtil;
import cn.yapeteam.yolbi.utils.render.ProjectionUtil;
import net.minecraft.entity.EntityLivingBase;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


@ModuleInfo(aliases = {"module.render.esp2d.name"}, description = "module.render.esp2d.description",category = Category.RENDER)
public class ESP2D extends Module {

    NumberValue range = new NumberValue("Range", this, 30, 10, 90, 1);

    BooleanValue invis = new BooleanValue("Show Invis", this, true);

    BooleanValue teamatecolor = new BooleanValue("Teamate Color", this, true);

    ModeValue mode = new ModeValue("Mode", this)
            .add(new SubMode("Box"))
            .add(new SubMode("Corners"))
            .setDefault("Box");

    BooleanValue healthbar = new BooleanValue("Health Bar", this, true);

    ModeValue healthmode = new ModeValue("Health Mode", this, () -> healthbar.getValue())
            .add(new SubMode("Line"))
            .add(new SubMode("Dot"))
            .setDefault("Normal");

    public static List<EntityLivingBase> collectedEntities = new ArrayList();

    @Override
    public void onDisable() {
        collectedEntities.clear();
    }

    @Listener
    public void onRender2D(EventRender2D eventRender2D){
        collectEntities();
        for (EntityLivingBase entity : collectedEntities) {

            Color color = PlayerUtil.sameTeam(entity) && teamatecolor.getValue() ? new Color(79, 195, 247) : new Color(255, 112, 67);

            if(!invis.getValue() && entity.isInvisible()) continue;

            // Use ProjectionUtil to project the entity
            Vector4d position = ProjectionUtil.get(entity);
            if (position == null) continue;

            // Extract the projected 2D coordinates from the projection
            double posX = position.x;
            double posY = position.y;
            double endPosX = position.z;
            double endPosY = position.w;

            if (mode.getValue().toString().equals("Corners")) {
                RenderManager.rectangle(posX - 1.0, posY, 1.5, endPosY - posY + 0.5, color);
                RenderManager.rectangle(posX - 1.0, posY - 0.5, endPosX - posX + 1.5, 1.0, color);
                RenderManager.rectangle(endPosX - 1.0, posY, 1.5, endPosY - posY + 0.5, color);
                RenderManager.rectangle(posX - 1.0, endPosY - 1.0, endPosX - posX + 1.5, 1.0, color);

                RenderManager.rectangle(posX, posY, 1.0, endPosY - posY, color);
                RenderManager.rectangle(posX, endPosY - 0.5, endPosX - posX, 0.5, color);
                RenderManager.rectangle(posX, posY, endPosX - posX, 0.5, color);
                RenderManager.rectangle(endPosX - 0.5, posY, 0.5, endPosY - posY, color);
            } else {
                RenderManager.rectangle(posX - 1.0, posY, 1.5, (endPosY - posY) / 4.0 + 0.5, color);
                RenderManager.rectangle(posX - 1.0, endPosY - (endPosY - posY) / 4.0 - 0.5, 1.5, (endPosY - posY) / 4.0 + 0.5, color);
                RenderManager.rectangle(posX - 1.0, posY - 0.5, (endPosX - posX) / 3.0 + 1.5, 1.5, color);
                RenderManager.rectangle(endPosX - (endPosX - posX) / 3.0 - 0.5, posY - 0.5, (endPosX - posX) / 3.0 + 1.5, 1.5, color);
                RenderManager.rectangle(endPosX - 1.0, posY, 1.5, (endPosY - posY) / 4.0 + 0.5, color);
                RenderManager.rectangle(endPosX - 1.0, endPosY - (endPosY - posY) / 4.0 - 0.5, 1.5, (endPosY - posY) / 4.0 + 0.5, color);
                RenderManager.rectangle(posX - 1.0, endPosY - 1.0, (endPosX - posX) / 3.0 + 1.5, 1.5, color);
                RenderManager.rectangle(endPosX - (endPosX - posX) / 3.0 - 0.5, endPosY - 1.0, (endPosX - posX) / 3.0 + 1.5, 1.5, color);

                RenderManager.rectangle(posX - 0.5, posY, 1.0, (endPosY - posY) / 4.0, color);
                RenderManager.rectangle(posX - 0.5, endPosY - (endPosY - posY) / 4.0, 1.0, (endPosY - posY) / 4.0, color);
                RenderManager.rectangle(posX, posY, (endPosX - posX) / 3.0, 0.5, color);
                RenderManager.rectangle(endPosX - (endPosX - posX) / 3.0, posY, (endPosX - posX) / 3.0, 0.5, color);
                RenderManager.rectangle(endPosX - 0.5, posY, 0.5, (endPosY - posY) / 4.0, color);
                RenderManager.rectangle(endPosX - 0.5, endPosY - (endPosY - posY) / 4.0, 0.5, (endPosY - posY) / 4.0, color);
                RenderManager.rectangle(posX, endPosY - 0.5, (endPosX - posX) / 3.0, 0.5, color);
                RenderManager.rectangle(endPosX - (endPosX - posX) / 3.0, endPosY - 0.5, (endPosX - posX) / 3.0, 0.5, color);
            }

            if (healthbar.getValue()) {
                float EntityHealth = entity.getHealth();
                float MaxHealth = entity.getMaxHealth();

                double barheight = (endPosY - posY - 0.4 * (MaxHealth - 1)) / (MaxHealth);
                double healthratio = EntityHealth / MaxHealth;
                Color healthcolor = getHealthColor(EntityHealth, MaxHealth);
                double yvalue = posY - 0.4;

                if (healthmode.getValue().equals("Line") || EntityHealth > 40) {
                    RenderManager.roundedRectangle(posX - 3.5, posY -0.5, 1, (endPosY - posY) * healthratio,4, healthcolor);
                }else{
                    for (int i = 0; i <= MaxHealth; i++) {
                        if(i > 20){
//                        they must have gapped so gold color
                            healthcolor = new Color(255, 215, 0);
                        }
                        // we render a rectangle according to barheight and the gap is 0.4
                        RenderManager.rectangle(posX - 3.5, yvalue, 1, barheight, healthcolor);

                        yvalue += barheight + 0.4;
                    }
                }
            }

        }
    }

    private void collectEntities() {
        collectedEntities.clear();
        collectedEntities = TargetManager.getTargets(range.getValue().doubleValue());
    }

    public static Color getHealthColor(float health, float maxHealth) {
        float[] fractions = new float[]{0.0f, 0.5f, 1.0f};
        Color[] colors = new Color[]{new Color(108, 0, 0), new Color(255, 51, 0), Color.GREEN};
        float progress = health / maxHealth;
        return ColorUtil.blendColors(fractions, colors, progress).brighter();
    }

}

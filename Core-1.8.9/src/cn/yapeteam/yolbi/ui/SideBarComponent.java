package cn.yapeteam.yolbi.ui;

import cn.yapeteam.yolbi.YolBi;

import java.awt.*;

public class SideBarComponent {

    /* Information */
    public double sidebarWidth = 100;
    private double opacity, fadeOpacity;
    private long lastTime = 0;

    public void preRenderYolbiClickGui() {
        
        final Color color = new Color(YolbiClickGui.sidebarColor.getRed(), YolbiClickGui.sidebarColor.getGreen(), YolbiClickGui.sidebarColor.getBlue(), (int) Math.min(opacity, YolbiClickGui.sidebarColor.getAlpha()));

        YolBi.instance.getRenderManager().roundedRectangle(YolbiClickGui.position.x, YolbiClickGui.position.y, sidebarWidth + 20, YolbiClickGui.scale.y, YolbiClickGui.round, YolBi.instance.getColorManager().withAlpha(color, 240));
    }
}

package cn.yapeteam.yolbi.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

public interface IMinecraft {
    Minecraft mc = Minecraft.getMinecraft();

    ScaledResolution ScaledResolution = new ScaledResolution(mc);
}

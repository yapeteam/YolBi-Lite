package cn.yapeteam.yolbi.managers;

import cn.yapeteam.loader.logger.Logger;
import cn.yapeteam.ymixin.utils.Mapper;
import net.minecraft.client.Minecraft;

import java.lang.reflect.Field;

public class ReflectionManager {
    private static Field Minecraft$instance;

    static {
        try {
            Minecraft$instance = Minecraft.class.getDeclaredField(Mapper.map("net/minecraft/client/Minecraft", "instance", null, Mapper.Type.Field));
            Minecraft$instance.setAccessible(true);
        } catch (Throwable throwable) {
            Logger.exception(throwable);
        }
    }

    public static Minecraft Minecraft$getInstance() {
        try {
            return (Minecraft) Minecraft$instance.get(null);
        } catch (IllegalAccessException e) {
            Logger.exception(e);
            return null;
        }
    }
}

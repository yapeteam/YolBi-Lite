package cn.yapeteam.yolbi.managers;

import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.game.EventLoadWorld;
import cn.yapeteam.yolbi.utils.interfaces.Accessor;
import net.minecraft.entity.Entity;

import java.util.ArrayList;

public class BotManager implements Accessor {
    public static ArrayList<Entity> bots = new ArrayList<>();

    @Listener
    public void onWorldChange(EventLoadWorld event) {
        bots.clear();
    }

    public static void addBot(Entity entity) {
        if (!bots.contains(entity))
            bots.add(entity);
    }

    public static void removeBot(Entity entity) {
        if (bots.contains(entity))
            bots.remove(entity);
    }
}

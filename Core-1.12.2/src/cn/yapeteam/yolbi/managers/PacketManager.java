package cn.yapeteam.yolbi.managers;

import cn.yapeteam.loader.logger.Logger;
import cn.yapeteam.ymixin.utils.Mapper;
import cn.yapeteam.yolbi.utils.interfaces.Accessor;
import io.netty.channel.Channel;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.INetHandlerPlayServer;

import java.lang.reflect.Method;
import java.util.ArrayList;

;

@SuppressWarnings("unused")
public class PacketManager implements Accessor {
    public static void sendPacket(Packet<INetHandlerPlayServer> packet) {
        mc.getConnection().sendPacket(packet);
    }

    public static Method flushOutboundQueue = null, dispatchPacket = null;

    public static Channel channel;

    public static INetHandlerPlayClient packetListener;

    static {
        try {
            flushOutboundQueue = NetworkManager.class.getDeclaredMethod(Mapper.map("net.minecraft.network.NetworkManager", "flushOutboundQueue", "()V", Mapper.Type.Method));
            dispatchPacket = NetworkManager.class.getDeclaredMethod(Mapper.map("net.minecraft.network.NetworkManager", "dispatchPacket", "(Lnet/minecraft/network/Packet;[Lio/netty/util/concurrent/GenericFutureListener;)V", Mapper.Type.Method), Packet.class, GenericFutureListener[].class);
            flushOutboundQueue.setAccessible(true);
            dispatchPacket.setAccessible(true);
        } catch (NoSuchMethodException e) {
            Logger.exception(e);
        }
    }

    public static void receivePacket(Packet<INetHandlerPlayClient> packet){
        if (channel.isOpen()){
            packet.processPacket(packetListener);
        }
    }

    public static void sendPacketNoEvent(Packet<INetHandlerPlayServer> packet) {
        PacketManager.skip(packet);
        mc.getConnection().getNetworkManager().sendPacket(packet);
    }

    public static ArrayList<Packet<? extends INetHandler>> skip_list = new ArrayList<>();

    public static void skip(Packet<? extends INetHandler> packet) {
        if (!skip_list.contains(packet)) {
            skip_list.add(packet);
        }
    }

    public static void remove(Packet<? extends INetHandler> packet) {
        skip_list.remove(packet);
    }

    public static boolean shouldSkip(Packet<? extends INetHandler> packet) {
        return skip_list.contains(packet);
    }
}

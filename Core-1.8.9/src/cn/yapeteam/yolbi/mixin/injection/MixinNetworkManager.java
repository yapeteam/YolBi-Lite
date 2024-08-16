package cn.yapeteam.yolbi.mixin.injection;

import cn.yapeteam.loader.logger.Logger;
import cn.yapeteam.ymixin.annotations.*;
import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.event.impl.network.EventPacket;
import cn.yapeteam.yolbi.event.impl.network.EventPacketReceive;
import cn.yapeteam.yolbi.event.impl.network.EventPacketSend;
import cn.yapeteam.yolbi.managers.PacketManager;
import io.netty.channel.Channel;
import lombok.Getter;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.INetHandlerPlayServer;

@Mixin(NetworkManager.class)
@SuppressWarnings("UnnecessaryReturnStatement")
public class MixinNetworkManager {

    @Shadow
    @Getter
    private static Channel channel;

    @Shadow
    @Getter
    private static INetHandler packetListener;

    static{
        try{
            PacketManager.channel = channel;
            PacketManager.packetListener = packetListener;
        }catch (Exception e){
            Logger.exception(e);
        }

    }

    @Inject(
            method = "sendPacket",
            desc = "(Lnet/minecraft/network/Packet;)V",
            target = @Target(
                    value = "INVOKESPECIAL",
                    target = "net/minecraft/network/NetworkManager.flushOutboundQueue()V",
                    shift = Target.Shift.BEFORE
            )
    )
    public void onPacketSend(@Local(source = "packet", index = 1) Packet<INetHandlerPlayServer> packet) {
        if (!PacketManager.shouldSkip(packet)) {
            EventPacketSend eventPacketSend = new EventPacketSend(packet);
            YolBi.instance.getEventManager().post(eventPacketSend);
            packet = eventPacketSend.getPacket();
            EventPacket eventPacket = new EventPacket(packet, EventPacket.Type.Send);
            YolBi.instance.getEventManager().post(eventPacket);
            if (eventPacketSend.isCancelled() || eventPacket.isCancelled()) return;
        } else PacketManager.remove(packet);
    }

    @Inject(
            method = "channelRead0",
            desc = "(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/Packet;)V",
            target = @Target(
                    value = "INVOKEINTERFACE",
                    target = "net/minecraft/network/Packet.processPacket(Lnet/minecraft/network/INetHandler;)V",
                    shift = Target.Shift.BEFORE
            )
    )
    public void onPacketReceive(@Local(source = "packet", index = 2) Packet<INetHandlerPlayClient> packet) {
        if (!PacketManager.shouldSkip(packet)) {
            EventPacketReceive event = new EventPacketReceive(packet);
            YolBi.instance.getEventManager().post(event);
            EventPacket eventPacket = new EventPacket(packet, EventPacket.Type.Receive);
            YolBi.instance.getEventManager().post(eventPacket);
            if (event.isCancelled() || eventPacket.isCancelled()) return;
        } else PacketManager.remove(packet);
    }
}

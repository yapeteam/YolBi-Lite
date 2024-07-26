package cn.yapeteam.yolbi.mixin.transformer;

import cn.yapeteam.ymixin.ASMTransformer;
import com.fun.eventapi.EventManager;
import com.fun.eventapi.event.events.EventPacket;
import com.fun.inject.injection.asm.api.Inject;
import com.fun.inject.injection.asm.api.Transformer;
import net.minecraft.client.Minecraft;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.RETURN;
import org.objectweb.asm.tree.*;

public class NetworkHandlerTransformer extends ASMTransformer {
    public NetworkHandlerTransformer() {
        super("net/minecraft/client/multiplayer/ClientPacketListener");
    }


    @Inject(method = "send",descriptor = "(Lnet/minecraft/network/protocol/Packet;)V")
    public void sendPacket(MethodNode mn) {
        InsnList list = new InsnList();
        LabelNode label = new LabelNode();

        list.add(new VarInsnNode(ALOAD, 1));
        //list.add(new InsnNode(ACONST));
        list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(NetworkHandlerTransformer.class),"onPacket","(Ljava/lang/Object;)Z"));
        list.add(new JumpInsnNode(IFEQ, label));
        list.add(new InsnNode(RETURN));
        list.add(label);
        mn.instructions.insert(list);
    }
    public static boolean onPacket(Object packet){
        //Agent.logger.info(Mappings.getUnobfClass(packet.getClass().getName()));
        return EventManager.call(new EventPacket(packet)).cancel;
    }
}

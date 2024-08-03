package cn.yapeteam.yolbi.mixin.transformer;

import cn.yapeteam.ymixin.ASMTransformer;
import cn.yapeteam.ymixin.utils.DescParser;
import cn.yapeteam.ymixin.utils.Mapper;
import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.event.impl.game.EventTick;
import net.minecraft.client.Minecraft;
import org.objectweb.asm_9_2.Opcodes;
import org.objectweb.asm_9_2.Type;
import org.objectweb.asm_9_2.tree.FieldInsnNode;
import org.objectweb.asm_9_2.tree.InsnNode;
import org.objectweb.asm_9_2.tree.MethodInsnNode;
import org.objectweb.asm_9_2.tree.MethodNode;


public class MinecraftTransformer extends ASMTransformer {
    public MinecraftTransformer() {
        super(Minecraft.class);
    }
    @Inject(method = "runTick",desc = "(Z)V")//Minecraft/runTick (Z)V
    public void runTick(MethodNode methodNode){
        methodNode.instructions.insert(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(MinecraftTransformer.class),"onRunTick","()V"));
    }
    @Inject(method = "getInstance",desc = "()Lnet/minecraft/client/Minecraft;")
    public void getInstance(MethodNode methodNode){
        methodNode.instructions.clear();
        methodNode.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, Mapper.getObfClass("net/minecraft/client/Minecraft"), Mapper.map("instance","net/minecraft/client/Minecraft","Lnet/minecraft/client/Minecraft;", Mapper.Type.Field),DescParser.mapDesc("Lnet/minecraft/client/Minecraft;")));
        methodNode.instructions.add(new InsnNode(Opcodes.ARETURN));
    }
    public static void onRunTick(){
        YolBi.instance.getEventManager().post(new EventTick());
    }
}

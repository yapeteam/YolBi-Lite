package cn.yapeteam.yolbi.mixin.transformer;

import cn.yapeteam.ymixin.ASMTransformer;
import cn.yapeteam.ymixin.utils.DescParser;
import cn.yapeteam.ymixin.utils.Mapper;
import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.event.impl.game.EventTick;
import net.minecraft.client.Minecraft;
import org.objectweb.asm_9_2.Opcodes;
import org.objectweb.asm_9_2.Type;
import org.objectweb.asm_9_2.tree.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


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
        List<AbstractInsnNode> removeList = new ArrayList<>();
        Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();

        int i=0;
        while(iterator.hasNext()){
            AbstractInsnNode a=iterator.next();
            if(a instanceof MethodInsnNode||a instanceof VarInsnNode){iterator.remove();}
        }
    }
    public static void onRunTick(){
        YolBi.instance.getEventManager().post(new EventTick());
    }
}

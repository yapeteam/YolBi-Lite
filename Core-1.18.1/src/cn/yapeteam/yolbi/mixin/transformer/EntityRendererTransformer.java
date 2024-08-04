package cn.yapeteam.yolbi.mixin.transformer;


import cn.yapeteam.ymixin.ASMTransformer;
import cn.yapeteam.ymixin.utils.Mapper;
import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.event.impl.game.EventAttackReach;
import cn.yapeteam.yolbi.event.impl.game.EventBlockReach;
import cn.yapeteam.yolbi.event.impl.render.EventRender3D;
import cn.yapeteam.yolbi.utils.render.RenderManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import org.objectweb.asm_9_2.Type;
import org.objectweb.asm_9_2.tree.*;

import static org.objectweb.asm_9_2.Opcodes.*;


public class EntityRendererTransformer extends ASMTransformer {
    public EntityRendererTransformer() {
        super(GameRenderer.class);
    }

    @Inject(method = "renderLevel", desc = "(FJLcom/mojang/blaze3d/vertex/PoseStack;)V")
    public void renderWorldPass(MethodNode methodNode) {
        AbstractInsnNode ldcNode = null;
        for (int i = 0; i < methodNode.instructions.size(); ++i) {
            AbstractInsnNode a = methodNode.instructions.get(i);
            if (a instanceof MethodInsnNode m) {
                if (m.owner.equals(Mapper.getObfClass("net/minecraft/util/profiling/ProfilerFiller"))
                        && m.name.equals(Mapper.map("net/minecraft/util/profiling/ProfilerFiller", "popPush", "(Ljava/lang/String;)V", Mapper.Type.Method))) {
                    //MD: net/minecraft/util/profiling/ProfilerFiller/m_6182_ (Ljava/lang/String;)V net/minecraft/util/profiling/ProfilerFiller/popPush (Ljava/lang/String;)V
                    ldcNode = a;
                }
            }
        }

        InsnList list = new InsnList();

        list.add(new VarInsnNode(FLOAD, 1));
        list.add(new VarInsnNode(ALOAD, 3));
        list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(EntityRendererTransformer.class), "onRender3D", "(FLjava/lang/Object;)V"));

        methodNode.instructions.insert(ldcNode, list);
    }

    public static void onRender3D(float f, Object pose) {
        RenderManager.currentPoseStack = (PoseStack) pose;
        YolBi.instance.getEventManager().post(new EventRender3D(f));

    }

    @Inject(method = "pick", desc = "(F)V")
    public void getMouseOver(MethodNode methodNode) {

        InsnList list = new InsnList();
        LdcInsnNode ldc = null;
        MethodInsnNode min = null;
        for (int i = 0; i < methodNode.instructions.size(); ++i) {
            AbstractInsnNode x = methodNode.instructions.get(i);
            if (x instanceof MethodInsnNode) {
                //MD: net/minecraft/client/multiplayer/MultiPlayerGameMode/m_105286_ ()F net/minecraft/client/multiplayer/MultiPlayerGameMode/getPickRange ()F
                if (Mapper.map("net/minecraft/client/multiplayer/MultiPlayerGameMode", "getPickRange", "()F", Mapper.Type.Method).equals(((MethodInsnNode) x).name) &&
                        ((MethodInsnNode) x).owner.equals(Mapper.getObfClass("net/minecraft/client/multiplayer/MultiPlayerGameMode")))
                    min = (MethodInsnNode) x;
            }
            if (x instanceof LdcInsnNode) {
                LdcInsnNode t = (LdcInsnNode) x;

                if (t.cst instanceof Double && ((Double) t.cst) == 3.0) {
                    ldc = t;
                }

            }
        }

        if (ldc == null) return;
        methodNode.instructions.insert(min, new MethodInsnNode(INVOKESTATIC, Type.getInternalName(EntityRendererTransformer.class), "onBlockReach", "(F)F"));
        methodNode.instructions.insert(ldc, new MethodInsnNode(INVOKESTATIC, Type.getInternalName(EntityRendererTransformer.class), "onAttackReach", "()D"));
        methodNode.instructions.remove(ldc);


    }

    public static double onAttackReach() {
        EventAttackReach e = new EventAttackReach(3.0d);
        YolBi.instance.getEventManager().post(e);
        return e.getReach();
    }

    public static float onBlockReach(float f) {
        EventBlockReach e = new EventBlockReach(f);
        YolBi.instance.getEventManager().post(e);
        return e.getReach();
    }
}

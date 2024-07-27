package cn.yapeteam.yolbi.mixin.transformer;


import cn.yapeteam.ymixin.ASMTransformer;
import cn.yapeteam.ymixin.utils.Mapper;

import cn.yapeteam.yolbi.utils.render.RenderManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Gui;

import org.objectweb.asm.tree.*;
import org.objectweb.asm_9_2.Opcodes;
import org.objectweb.asm_9_2.Type;
import org.objectweb.asm_9_2.tree.*;

public class GuiIngameTransformer extends ASMTransformer {
    //private static final String GL_STATE_MANAGER_NAME = "net/minecraft/client/renderer/GlStateManager";


    public GuiIngameTransformer() {
        super(Gui.class);
    }


    @Inject(method = "render", desc = "(Lcom/mojang/blaze3d/vertex/PoseStack;F)V")
    public void render(MethodNode methodNode) {
        InsnList list = new InsnList();

        AbstractInsnNode point = null;
        for (int i = 0; i < methodNode.instructions.size(); ++i) {
            AbstractInsnNode aisn = methodNode.instructions.get(i);
            if (aisn instanceof MethodInsnNode) {
                // we're looking for GlStateManager.color(...)
                // we dont break because we want to do it after .color is invoked

                MethodInsnNode meth = (MethodInsnNode) aisn; // hehe

                if (meth.owner.equals(Mapper.getObfClass("com/mojang/blaze3d/systems/RenderSystem"))
                //MD: com/mojang/blaze3d/systems/RenderSystem/m_157429_ (FFFF)V com/mojang/blaze3d/systems/RenderSystem/setShaderColor (FFFF)V
                        && meth.name.equals(Mapper.map("com/mojang/blaze3d/systems/RenderSystem","setShaderColor","(FFFF)V", Mapper.Type.Method))
                        && meth.desc.equals("(FFFF)V")) {
                    point = aisn;

                }
            }
        }

        if (point == null) {
            throw new RuntimeException("Failed to find last GlStateManager#color call in GuiInGame");//Transformers.logger.error("Failed to find last GlStateManager#color call in GuiInGame");

        }
        list.add(new VarInsnNode(Opcodes.ALOAD, 1));
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(GuiIngameTransformer.class),"onRender2D","(Ljava/lang/Object;)V"));


        methodNode.instructions.insert(point, list);

    }
    public static void onRender2D(Object poseStack){
        if(!(poseStack instanceof PoseStack)) throw new RuntimeException("invalid poseStack");
        RenderManager.currentPoseStack= (PoseStack) poseStack;
    }
}

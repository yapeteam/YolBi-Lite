package cn.yapeteam.yolbi.mixin;

import cn.yapeteam.loader.JVMTIWrapper;
import cn.yapeteam.loader.SocketSender;
import cn.yapeteam.loader.logger.Logger;
import cn.yapeteam.loader.utils.ClassUtils;
import cn.yapeteam.ymixin.ASMTransformer;
import cn.yapeteam.ymixin.MixinTransformer;
import cn.yapeteam.ymixin.annotations.Mixin;
import cn.yapeteam.ymixin.utils.ASMUtils;
import cn.yapeteam.yolbi.mixin.transformer.*;
import org.objectweb.asm_9_2.tree.ClassNode;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class MixinManager {
    public static final ArrayList<ClassNode> mixins = new ArrayList<>();
    public static final ArrayList<ASMTransformer> transformers = new ArrayList<>();
    public static MixinTransformer mixinTransformer;
    public static final String MIXIN_PACKAGE = "cn.yapeteam.yolbi.mixin.injection";

    public static void init() throws Throwable {
        mixinTransformer = new MixinTransformer(JVMTIWrapper.instance::getClassBytes);
        addMixin("MixinFirstPersonRenderer");
        addTransformer(new EntityPlayerSPTransformer());
        addTransformer(new EntityRendererTransformer());
        addTransformer(new EntityTransformer());
        addTransformer(new GuiIngameTransformer());
        addTransformer(new GuiScreenTransformer());
        addTransformer(new KeyBindTransformer());
        addTransformer(new MinecraftTransformer());
        addTransformer(new NetworkHandlerTransformer());
        addTransformer(new NetworkManagerTransFormer());
    }

    public static void destroyClient() throws IOException {
        Map<String, byte[]> map = mixinTransformer.getOldBytes();
        for (ClassNode mixin : mixins) {
            Class<?> targetClass = Objects.requireNonNull(Mixin.Helper.getAnnotation(mixin)).value();
            if (targetClass != null) {
                byte[] bytes = map.get(targetClass.getName());
                Files.write(new File(dir, targetClass.getName()).toPath(), bytes);
                int code = JVMTIWrapper.instance.redefineClass(targetClass, bytes);
                Logger.success("Redefined {}, Return Code {}.", targetClass, code);
            }
        }
    }

    //for debug
    private static final File dir = new File("generated");

    public static void transform() throws Throwable {
        boolean ignored = dir.mkdirs();
        Map<String, byte[]> map = mixinTransformer.transform();
        SocketSender.send("S2");
        int total = mixins.size() + transformers.size();
        ArrayList<String> failed = new ArrayList<>();
        for (int i = 0; i < mixins.size(); i++) {
            ClassNode mixin = mixins.get(i);
            Class<?> targetClass = Objects.requireNonNull(Mixin.Helper.getAnnotation(mixin)).value();
            if (targetClass != null) {
                byte[] bytes = map.get(targetClass.getName());
                if (bytes == null) {
                    failed.add(mixin.name.replace('/', '.'));
                    continue;
                }
                Files.write(new File(dir, targetClass.getName()).toPath(), bytes);
                int code = JVMTIWrapper.instance.redefineClass(targetClass, bytes);
                SocketSender.send("P2" + " " + (float) (i + 1) / total * 100f);
                if (code != 0)
                    failed.add(mixin.name.replace('/', '.'));
                Logger.success("Redefined {}, Return Code {}.", targetClass, code);
                Thread.sleep(200);
            }
        }
        for (int i = 0; i < transformers.size(); i++) {
            ASMTransformer asmTransformer = transformers.get(i);
            Class<?> targetClass = asmTransformer.getTarget();
            byte[] bytes = map.get(targetClass.getName());
            if (bytes == null) {
                failed.add(asmTransformer.getClass().getName());
                continue;
            }
            Files.write(new File(dir, targetClass.getName()).toPath(), bytes);
            int code = JVMTIWrapper.instance.redefineClass(targetClass, bytes);
            SocketSender.send("P2" + " " + (float) (i + mixins.size() + 1) / total * 100f);
            if (code != 0)
                failed.add(asmTransformer.getClass().getName());
            Logger.success("Redefined {}, Return Code {}.", targetClass, code);
            Thread.sleep(200);
        }
        SocketSender.send("E2");
        if (!failed.isEmpty()) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Failed to transform").append(' ').append(failed.size() == 1 ? "class" : "classes").append(' ').append('\n');
            for (String s : failed)
                stringBuilder.append(s).append('\n');
            stringBuilder.deleteCharAt(stringBuilder.lastIndexOf("\n"));
            JOptionPane.showMessageDialog(null, stringBuilder, "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private static void addMixin(String name) throws Throwable {
        ClassNode node = ASMUtils.node(ClassUtils.getClassBytes(MIXIN_PACKAGE + "." + name));
        mixins.add(node);
        mixinTransformer.addMixin(node);
    }

    private static void addTransformer(ASMTransformer transformer) {
        transformers.add(transformer);
        mixinTransformer.addTransformer(transformer);
    }
}

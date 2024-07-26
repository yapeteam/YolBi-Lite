package cn.yapeteam.agent;

import java.io.File;
import java.lang.instrument.Instrumentation;

public class Agent {
    private static native void loadNative();

    public static void agentmain(String args, Instrumentation inst) {
        String suffix;
        if (OS.isFamilyWindows()) suffix = ".dll";
        else if (OS.isFamilyMac()) suffix = ".dylib";
        else suffix = ".so";
        System.out.println("[YolBi Lite] 开始加载Agentaaaa库");
        try {
            ///Users/yuxiangll/.yolbi/libagent.dylib
            //System.load(new File(System.getProperty("user.home"), "/.yolbi/libagent" + suffix).getAbsolutePath());
            //System.setProperty("java.library.path","/Users/yuxiangll/.yolbi");
            System.loadLibrary("libagent");
            //System.load(new File("Users/yuxiangll/.yolbi/libagent.dylib").getAbsolutePath());
            System.out.println("[YolBi Lite] 开始加载Native");
            loadNative();
            System.out.println("[YolBi Lite] 加载完成");
        }catch (Exception e){
            System.out.println("[YolBi Lite] 加载失败");
        }

    }
}

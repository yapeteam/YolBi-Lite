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
        System.out.println("[YolBi Lite] 开始加载Agent库");
        try {

            System.load("/Users/yuxiangll/Documents/YolBi/YolBi-Lite/Loader/dll/build/libagent"+suffix);

            System.out.println("[YolBi Lite] 开始加载Native");
            loadNative();
            System.out.println("[YolBi Lite] 加载完成");
        }catch (Exception e){
            System.out.println("[YolBi Lite] 加载失败");
        }

    }
}

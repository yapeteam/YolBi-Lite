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
        System.load(new File(System.getProperty("user.home"), "libagent" + suffix).getAbsolutePath());
        loadNative();
    }
}

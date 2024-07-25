package cn.yapeteam.agent;

import java.io.File;
import java.lang.instrument.Instrumentation;

public class Agent {
    private static native void loadNative();

    private static native void loadJar2URL(String path, ClassLoader loader);

    private static native Class<?> findClass(String name, ClassLoader loader);

    private static Thread getThreadByName(String name) {
        for (Object o : Thread.getAllStackTraces().keySet().toArray()) {
            Thread thread = (Thread) o;
            if (thread.getName().equals(name))
                return thread;
        }
        return null;
    }

    public static void agentmain(String agent, Instrumentation inst) {
        String suffix;
        if (OS.isFamilyWindows()) suffix = ".dll";
        else if (OS.isFamilyMac()) suffix = ".dylib";
        else suffix = ".so";
        System.load("libagent" + suffix);
        loadNative();
        File yolbiPath = new File(System.getProperty("user.home"), ".yolbi");
        Thread thread = getThreadByName("Client thread");
        if (thread == null) thread = getThreadByName("Render thread");
        if (thread == null) return;
        ClassLoader classLoader = thread.getContextClassLoader();
        ClassLoader classLoaderLoader = classLoader.getClass().getClassLoader();

    }
}

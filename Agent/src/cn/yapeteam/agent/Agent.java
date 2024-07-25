package cn.yapeteam.agent;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.URLClassLoader;
import java.util.jar.JarFile;

public class Agent {
    private static Instrumentation instrumentation;

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

    private static void loadJar(String path, ClassLoader loader) {
        if (loader instanceof URLClassLoader)
            loadJar2URL(path, loader);
        else {
            try {
                instrumentation.appendToSystemClassLoaderSearch(new JarFile(path));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void agentmain(String agent, Instrumentation inst) {
        instrumentation = inst;
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
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        String classLoaderName = classLoaderLoader.getClass().getName();
        boolean shouldHook = !(classLoaderName.startsWith("java.lang.") || classLoaderName.startsWith("com.sun."));
        String asmPath = new File(yolbiPath, "dependencies/asm-all-9.2.jar").getAbsolutePath();
        loadJar(asmPath, systemClassLoader);
        if (shouldHook)
            loadJar(asmPath, classLoaderLoader);

    }
}

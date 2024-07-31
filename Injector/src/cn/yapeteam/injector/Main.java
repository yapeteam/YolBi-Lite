package cn.yapeteam.injector;

import cn.yapeteam.loader.VersionInfo;
import com.formdev.flatlaf.intellijthemes.FlatXcodeDarkIJTheme;

import javax.swing.*;
import java.io.File;

public class Main {
    public static final String version = VersionInfo.version;
    public static final File YolBi_Dir = new File(System.getProperty("user.home"), ".yolbi");
    public static final String dllName = "libinjection.dll";
    public static final String agentName = "agent.jar";
    public static final int port = 20181;
    public static int pid;

    public static void main(String[] args) throws Exception {
        Utils.unzip(Main.class.getResourceAsStream("/injection.zip"), YolBi_Dir);
        if (OS.isFamilyWindows())
            System.load(new File(Main.YolBi_Dir, "libapi.dll").getAbsolutePath());
        UIManager.setLookAndFeel(new FlatXcodeDarkIJTheme());
        VerificationGUI frame = new VerificationGUI();
        new Thread(() -> frame.setVisible(true)).start();
        pid = Integer.parseInt(args[1]);
    }
}

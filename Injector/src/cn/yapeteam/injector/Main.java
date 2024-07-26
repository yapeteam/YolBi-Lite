package cn.yapeteam.injector;

import com.formdev.flatlaf.intellijthemes.FlatXcodeDarkIJTheme;

import javax.swing.*;
import java.io.File;

public class Main {
    public static final String version = "0.3.6";
    public static final File YolBi_Dir = new File(System.getProperty("user.home"), ".yolbi");
    public static final String dllName = "libinjection.dll";
    public static final String agentName = "agent.jar";
    public static final int port = 20181;

    public static void main(String[] args) throws Exception {
        Utils.unzip(Main.class.getResourceAsStream("/injection.zip"), YolBi_Dir);
        if (OS.isFamilyWindows())
            System.load(new File(Main.YolBi_Dir, "libapi.dll").getAbsolutePath());
        UIManager.setLookAndFeel(new FlatXcodeDarkIJTheme());
        MainFrame frame = new MainFrame();
        new Thread(() -> frame.setVisible(true)).start();
        if (args.length == 2) {
            switch (args[0]) {
                case "dll":
                    frame.inject_dll(Integer.parseInt(args[1]));
                case "agent":
                    frame.inject_agent(args[1]);
            }
            frame.inject_ui();
        }
    }
}

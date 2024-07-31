package cn.yapeteam.injector;

import com.formdev.flatlaf.intellijthemes.FlatXcodeDarkIJTheme;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class VerificationGUI extends JFrame{
    private JButton loginButton;
    private JPanel panel1;
    private JTextField usernameTextField;
    private JCheckBox saveCredentialsCheckBox;
    private JPasswordField passwordPasswordField;
    private JProgressBar progressBar1;
    private JTextPane loggingInTextPane;
    private JLabel YolbiLoginLabel;

    private String server = "127.0.0.1";

    public VerificationGUI() {
        add(panel1);
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                verify(usernameTextField.getText(), passwordPasswordField.getText());
            }
        });
    }

    public void verify(String username, String password) {
        MainFrame frame = new MainFrame();
        new Thread(() -> frame.setVisible(true)).start();
        frame.inject_dll(Main.pid);
        frame.inject_ui();
    }
}

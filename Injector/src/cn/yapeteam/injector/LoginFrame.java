package cn.yapeteam.injector;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class LoginFrame extends JFrame {
    private JPanel panel;
    private JButton loginButton;
    private JTextField UsernameField;
    private JPasswordField PasswordField;

    public LoginFrame(LoginCallBack callBack) {
        super("LoginYourAccount");
        add(panel);
        float width = 500;
        float height = width * 0.618f;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = (int) screenSize.getWidth();
        int screenHeight = (int) screenSize.getHeight();
        int[] size = {(int) (width / 1920 * screenWidth), (int) (height / 1080 * screenHeight)};
        setSize(size[0], size[1]);
        setResizable(false);
        getRootPane().setDefaultButton(loginButton);
        setAlwaysOnTop(true);
        setLocationRelativeTo(null);
        loginButton.addActionListener(e -> {
            if (callBack.run(UsernameField.getText(), String.valueOf(PasswordField.getPassword()))) {
                setVisible(false);
            }
        });
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                setVisible(false);
                System.exit(0);
            }
        });
    }

    public JButton getLoginButton() {
        return loginButton;
    }

    @Override
    public void setVisible(boolean b) {
        this.UsernameField.setText("");
        this.PasswordField.setText("");
        super.setVisible(b);
    }
}

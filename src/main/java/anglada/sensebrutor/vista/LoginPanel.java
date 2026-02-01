package anglada.sensebrutor.vista;

import anglada.dimedianetpollingcomponent.DIMediaNetPollingComponent;
import anglada.sensebrutor.SenseBrutor;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class LoginPanel extends JPanel {

    private final SenseBrutor main;
    private final DIMediaNetPollingComponent component;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JCheckBox rememberCheck;
    private JButton loginButton;
    private JLabel statusLabel;
    private JLabel logoLabel;
    private JLabel spinnerLabel; // JLabel para el GIF de carga

    public LoginPanel(SenseBrutor main) {
        this.main = main;
        this.component = main.getDiMediaPolling();

        initUI();

        SwingUtilities.invokeLater(() -> {
            JRootPane root = SwingUtilities.getRootPane(this);
            if (root != null) {
                root.setDefaultButton(loginButton);
            }
        });
    }

    private void initUI() {
        setLayout(null);
        setBackground(new Color(240, 240, 240));
        setBounds(0, 0, 550, 420);

        JLabel title = new JLabel("Iniciar sesión");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setBounds(205, 20, 300, 40);
        add(title);

        JLabel lblMail = new JLabel("Email:");
        lblMail.setBounds(100, 80, 100, 25);
        add(lblMail);

        emailField = new JTextField();
        emailField.setBounds(150, 80, 250, 25);
        add(emailField);

        JLabel lblPass = new JLabel("Contraseña:");
        lblPass.setBounds(70, 120, 100, 25);
        add(lblPass);

        passwordField = new JPasswordField();
        passwordField.setBounds(150, 120, 250, 25);
        add(passwordField);

        rememberCheck = new JCheckBox("Recordarme");
        rememberCheck.setBounds(210, 155, 150, 25);
        add(rememberCheck);

        loginButton = new JButton("Entrar");
        loginButton.setBounds(150, 195, 250, 30);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Cursor de ma
        loginButton.addActionListener(e -> intentarLogin());
        add(loginButton);

        // --- SPINNER (GIF DE CARGA) ---
        spinnerLabel = new JLabel();
        URL spinnerURL = getClass().getResource("/images/loading.gif");
        if (spinnerURL != null) {
            spinnerLabel.setIcon(new ImageIcon(spinnerURL));
        }
        spinnerLabel.setBounds(410, 195, 30, 30); // Al lado del botón
        spinnerLabel.setVisible(false); // Oculto al inicio
        add(spinnerLabel);

        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setForeground(Color.RED);
        statusLabel.setBounds(0, 230, 550, 30);
        add(statusLabel);

        URL imgURL = getClass().getResource("/images/titulosensebruto1r.png");
        if (imgURL != null) {
            ImageIcon icon = new ImageIcon(imgURL);
            int logoWidth = 200;
            int logoHeight = (int) ((double) icon.getIconHeight() / icon.getIconWidth() * logoWidth);
            Image scaledImage = icon.getImage().getScaledInstance(logoWidth, logoHeight, Image.SCALE_SMOOTH);
            logoLabel = new JLabel(new ImageIcon(scaledImage));

            int logoX = loginButton.getX() + (loginButton.getWidth() - logoWidth) / 2;
            int logoY = loginButton.getY() + loginButton.getHeight() + 10;
            logoLabel.setBounds(logoX, logoY, logoWidth, logoHeight);
            add(logoLabel);
        }
    }

    private void intentarLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        
        statusLabel.setText("");

        if (email.isEmpty() || password.isEmpty()) {
            statusLabel.setForeground(Color.RED);
            statusLabel.setText("Por favor, introduce tu email y contraseña.");
            return;
        }

        // --- INICIO CARGA: Feedback visual ---
        loginButton.setEnabled(false);
        spinnerLabel.setVisible(true); // Mostrar el spinner
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); // Ratón en espera

        new Thread(() -> {
            try {
                String token = component.login(email, password);

                if (token != null && !token.isEmpty()) {
                    if (rememberCheck.isSelected()) {
                        main.guardarSesion(token);
                    } else {
                        main.borrarSesion();
                    }
                    main.jwt = token;
                    component.setRunning(true);

                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setForeground(new Color(0, 130, 0));
                        statusLabel.setText("Login correcto. ¡Bienvenido!");
                        main.loginCorrecto();
                    });
                }
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setForeground(Color.RED);
                    String exMsg = ex.getMessage() != null ? ex.getMessage() : "";
                    statusLabel.setText(exMsg.contains("401") ? "Usuario o contraseña incorrectos." : "Error: " + exMsg);
                });
            } finally {
                // --- FIN CARGA: Restaurar UI ---
                SwingUtilities.invokeLater(() -> {
                    loginButton.setEnabled(true);
                    spinnerLabel.setVisible(false); // Ocultar spinner
                    this.setCursor(Cursor.getDefaultCursor()); // Ratón normal
                });
            }
        }).start();
    }

    public void limpiar() {
        emailField.setText("");
        passwordField.setText("");
        statusLabel.setText("");
        rememberCheck.setSelected(false);
        spinnerLabel.setVisible(false);
    }
}
package anglada.sensebrutor.vista;

import anglada.dimedianetpollingcomponent.DIMediaNetPollingComponent;
import anglada.sensebrutor.SenseBrutor;

import javax.swing.*;
import java.awt.*;
/**
 * Panell per iniciar sesió a l'aplicación.
 * Permet introduir usuari i contrasenya
 * Si esta el check de recordar i es tanca sessió es mantenen les dades.
 * @author Andreu
 */
public class LoginPanel extends JPanel {

    private final SenseBrutor main;
    private final DIMediaNetPollingComponent component;

    private JTextField emailField;
    private JPasswordField passwordField;
    private JCheckBox rememberCheck;
    private JButton loginButton;
    private JLabel statusLabel;

    public LoginPanel(SenseBrutor main) {
        this.main = main;
        this.component = main.getDiMediaPolling();
        initUI();
    }

    private void initUI() {
        setLayout(null);
        setBackground(new Color(240, 240, 240));
        setBounds(0, 0, 1050, 340);

        JLabel title = new JLabel("Iniciar sesión");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setBounds(490, 20, 300, 40);
        add(title);

        JLabel lblMail = new JLabel("Email:");
        lblMail.setBounds(385, 80, 100, 25);
        add(lblMail);

        emailField = new JTextField();
        emailField.setBounds(430, 80, 250, 25);
        add(emailField);

        JLabel lblPass = new JLabel("Contraseña:");
        lblPass.setBounds(350, 120, 100, 25);
        add(lblPass);

        passwordField = new JPasswordField();
        passwordField.setBounds(430, 120, 250, 25);
        add(passwordField);

        rememberCheck = new JCheckBox("Recordarme");
        rememberCheck.setBounds(430, 155, 150, 25);
        add(rememberCheck);

        loginButton = new JButton("Entrar");
        loginButton.setBounds(430, 195, 250, 30);
        loginButton.addActionListener(e -> intentarLogin());
        add(loginButton);

        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setForeground(Color.RED);
        statusLabel.setBounds(350, 230, 400, 30);
        add(statusLabel);
    }

    private void intentarLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        //Llevar mensatges previs
        statusLabel.setText("");
        // Validació camps buits
        if (email.isBlank() || password.isBlank()) {
            statusLabel.setText("Introduce email y contraseña.");
            return;
        }
        //Desactivar boto per evitar multiples clicks
        loginButton.setEnabled(false);
        //Utilitzam thread apres a l'assignatura PSP per fer llamada a API for des fil de UI
        new Thread(() -> {
            try {
                String token = component.login(email, password);
                //Es guarda token a un fitxer
                if (rememberCheck.isSelected()) {
                    main.guardarSesion(token);
                } else {
                    main.borrarSesion();
                }

                // Guardar jwt dins main
                main.jwt = token;
                component.setRunning(true); // activa polling
                //Actualitzar UI
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setForeground(new Color(0, 130, 0));
                    statusLabel.setText("Login correcto.");
                    main.loginCorrecto(); //Mostra els panels posteriors al login
                });

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setForeground(Color.RED);
                    statusLabel.setText("Error: " + ex.getMessage());
                });
            } finally {
                //Reactivar boto si hi ha error entra perfectament
                SwingUtilities.invokeLater(() -> loginButton.setEnabled(true));
            }
        }).start();
    }

    /** Utilitzat desde el menu cerrar sesión */
    public void limpiar() {
        emailField.setText("");
        passwordField.setText("");
        statusLabel.setText("");
        rememberCheck.setSelected(false);
    }
}

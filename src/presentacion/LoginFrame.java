package presentacion;

import negocio.EmpresaTransporte;
import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final EmpresaTransporte empresa;
    private JTextField usuarioField;
    private JPasswordField passwordField;

    public LoginFrame(EmpresaTransporte empresa) {
        this.empresa = empresa;
        setTitle("Copetran — Inicio de sesi\u00f3n");
        setSize(400, 520);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(Colores.FONDO_GENERAL);
        setLayout(new GridBagLayout());

        initComponents();
    }

    private void initComponents() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Colores.AZUL_PRIMARIO);
                g.fillRoundRect(0, 0, 60, 60, 10, 10);
            }
        };
        iconPanel.setPreferredSize(new Dimension(60, 60));
        iconPanel.setMaximumSize(new Dimension(60, 60));
        iconPanel.setOpaque(false);
        JLabel icono = new JLabel("\uD83D\uDE8C", SwingConstants.CENTER);
        icono.setFont(new Font("SansSerif", Font.PLAIN, 28));
        icono.setForeground(Color.WHITE);
        iconPanel.setLayout(new BorderLayout());
        iconPanel.add(icono, BorderLayout.CENTER);
        JPanel iconWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        iconWrapper.setOpaque(false);
        iconWrapper.add(iconPanel);
        panel.add(iconWrapper);

        panel.add(Box.createVerticalStrut(15));

        JLabel titulo = new JLabel("Copetran", SwingConstants.CENTER);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        titulo.setForeground(Colores.TEXTO_PRIMARIO);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titulo);

        JLabel subtitulo = new JLabel("Sistema de gesti\u00f3n de flota", SwingConstants.CENTER);
        subtitulo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitulo.setForeground(Colores.TEXTO_SECUNDARIO);
        subtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(subtitulo);

        panel.add(Box.createVerticalStrut(25));

        panel.add(crearLabel("Usuario"));
        usuarioField = new JTextField();
        usuarioField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        usuarioField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        panel.add(usuarioField);
        panel.add(Box.createVerticalStrut(10));

        panel.add(crearLabel("Contrase\u00f1a"));
        passwordField = new JPasswordField();
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        passwordField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        panel.add(passwordField);
        panel.add(Box.createVerticalStrut(10));

        panel.add(crearLabel("Perfil de acceso"));
        JComboBox<String> perfilCombo = new JComboBox<>(new String[]{"Administrador", "Cajero", "Supervisor"});
        perfilCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        perfilCombo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        panel.add(perfilCombo);
        panel.add(Box.createVerticalStrut(20));

        JButton ingresarBtn = new JButton("Ingresar al sistema");
        ingresarBtn.setBackground(Colores.AZUL_PRIMARIO);
        ingresarBtn.setForeground(Color.WHITE);
        ingresarBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        ingresarBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        ingresarBtn.setFocusPainted(false);
        ingresarBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        ingresarBtn.addActionListener(e -> login());
        panel.add(ingresarBtn);

        panel.add(Box.createVerticalStrut(8));

        JLabel olvido = new JLabel("\u00bfOlvidaste tu contrase\u00f1a?", SwingConstants.CENTER);
        olvido.setFont(new Font("SansSerif", Font.PLAIN, 12));
        olvido.setForeground(Colores.AZUL_MEDIO);
        olvido.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(olvido);

        panel.add(Box.createVerticalStrut(10));

        panel.add(new JSeparator());

        panel.add(Box.createVerticalStrut(8));

        JLabel version = new JLabel("v1.0.0 — Sistema de venta de pasajes", SwingConstants.CENTER);
        version.setFont(new Font("SansSerif", Font.PLAIN, 11));
        version.setForeground(Colores.TEXTO_SECUNDARIO);
        version.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(version);

        add(panel);
    }

    private JLabel crearLabel(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        label.setForeground(Colores.TEXTO_PRIMARIO);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void login() {
        String usuario = usuarioField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (usuario.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Usuario y contrase\u00f1a son obligatorios.",
                "Error de inicio de sesi\u00f3n",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        dispose();
        SwingUtilities.invokeLater(() -> new MainFrame(empresa, usuario).setVisible(true));
    }
}

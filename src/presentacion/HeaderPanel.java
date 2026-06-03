package presentacion;

import javax.swing.*;
import java.awt.*;

public class HeaderPanel extends JPanel {

    public HeaderPanel(String usuario) {
        setBackground(Colores.AZUL_PRIMARIO);
        setPreferredSize(new Dimension(0, 60));
        setLayout(new BorderLayout());

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        leftPanel.setOpaque(false);

        JLabel iconLabel = new JLabel("\uD83D\uDE8C");
        iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 22));
        iconLabel.setForeground(Color.WHITE);
        leftPanel.add(iconLabel);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel titulo = new JLabel("Copetran — Sistema de gesti\u00f3n");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 17));
        titulo.setForeground(Color.WHITE);
        titlePanel.add(titulo);

        JLabel subtitulo = new JLabel("Transporte Intermunicipal de Colombia");
        subtitulo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitulo.setForeground(Colores.SUBTITULO_HEADER);
        titlePanel.add(subtitulo);

        leftPanel.add(titlePanel);
        add(leftPanel, BorderLayout.WEST);

        JLabel userLabel = new JLabel(usuario != null ? usuario : "");
        userLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        userLabel.setForeground(new Color(255, 255, 255, 180));
        userLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
        add(userLabel, BorderLayout.EAST);
    }
}

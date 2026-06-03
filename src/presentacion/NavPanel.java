package presentacion;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class NavPanel extends JPanel {

    private final List<NavButton> botones = new ArrayList<>();
    private String activeKey;
    private final List<NavegacionListener> listeners = new ArrayList<>();

    public NavPanel() {
        setBackground(Colores.AZUL_OSCURO);
        setPreferredSize(new Dimension(0, 40));
        setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

        addBoton("dashboard", "Dashboard");
        addBoton("parametrizacion", "Parametrizaci\u00f3n");
        addBoton("ventas", "Ventas");
        addBoton("cancelaciones", "Cancelaciones");
        addBoton("reportes", "Reportes");

        setActive("dashboard");
    }

    private void addBoton(String key, String texto) {
        NavButton btn = new NavButton(texto);
        btn.addActionListener(e -> {
            setActive(key);
            for (NavegacionListener l : listeners) {
                l.onNavegar(key);
            }
        });
        botones.add(btn);
        add(btn);
    }

    public void setActive(String key) {
        this.activeKey = key;
        for (NavButton b : botones) {
            b.setActivo(b.getTexto().equals(getTextoPorKey(key)));
        }
        repaint();
    }

    private String getTextoPorKey(String key) {
        for (NavButton b : botones) {
            if (key.equals("dashboard") && b.getTexto().equals("Dashboard")) return "Dashboard";
            if (key.equals("parametrizacion") && b.getTexto().equals("Parametrizaci\u00f3n")) return "Parametrizaci\u00f3n";
            if (key.equals("ventas") && b.getTexto().equals("Ventas")) return "Ventas";
            if (key.equals("cancelaciones") && b.getTexto().equals("Cancelaciones")) return "Cancelaciones";
            if (key.equals("reportes") && b.getTexto().equals("Reportes")) return "Reportes";
        }
        return "";
    }

    public void addNavegacionListener(NavegacionListener l) {
        listeners.add(l);
    }

    public interface NavegacionListener {
        void onNavegar(String key);
    }

    private static class NavButton extends JButton {
        private final String texto;
        private boolean activo = false;

        NavButton(String texto) {
            super(texto);
            this.texto = texto;
            setBorderPainted(false);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setFont(new Font("SansSerif", Font.PLAIN, 13));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(140, 40));
            setActivo(false);
        }

        String getTexto() { return texto; }

        void setActivo(boolean activo) {
            this.activo = activo;
            setForeground(activo ? Color.WHITE : new Color(255, 255, 255, 180));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (activo) {
                g.setColor(Colores.AMBAR_ACENTO);
                g.fillRect(0, getHeight() - 3, getWidth(), 3);
            }
        }
    }
}

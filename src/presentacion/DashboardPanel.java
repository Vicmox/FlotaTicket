package presentacion;

import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import negocio.Bus;
import negocio.CajaVenta;
import negocio.EmpresaTransporte;
import negocio.PasajeTicket;
import negocio.Ruta;
import negocio.Salida;

public class DashboardPanel extends JPanel {

    private final EmpresaTransporte empresa;
    private final JLabel vendidosLabel;
    private final JLabel ingresoLabel;
    private final JLabel reembolsosLabel;
    private final JLabel busesLabel;
    private final DefaultTableModel ocupacionModel;
    private final DefaultTableModel salidasModel;
    private java.util.function.Consumer<String> navListener;

    public DashboardPanel(EmpresaTransporte empresa) {
        this.empresa = empresa;
        setBackground(Colores.FONDO_GENERAL);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel topMetrics = new JPanel(new GridLayout(1, 4, 10, 0));
        topMetrics.setOpaque(false);

        vendidosLabel = new JLabel("0");
        ingresoLabel = new JLabel("$0");
        reembolsosLabel = new JLabel("0");
        busesLabel = new JLabel("0");

        topMetrics.add(crearTarjetaMetrica("Tiquetes vendidos", vendidosLabel, Colores.TEXTO_PRIMARIO, new Color(200, 220, 240)));
        topMetrics.add(crearTarjetaMetrica("Ingreso neto", ingresoLabel, Colores.ESTADO_VERDE_TX, new Color(234, 243, 222)));
        topMetrics.add(crearTarjetaMetrica("Reembolsos", reembolsosLabel, Colores.ESTADO_ROJO_TX, new Color(252, 235, 235)));
        topMetrics.add(crearTarjetaMetrica("Buses activos", busesLabel, Colores.TEXTO_PRIMARIO, new Color(230, 241, 251)));

        add(topMetrics, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        centerPanel.setOpaque(false);

        ocupacionModel = new DefaultTableModel(new String[]{"Ruta", "Vendidos", "Capacidad", "Ocupaci\u00f3n %"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable ocupacionTable = new JTable(ocupacionModel);
        ocupacionTable.setRowHeight(28);
        ocupacionTable.getTableHeader().setBackground(Colores.FONDO_SUPERFICIE);
        ocupacionTable.getTableHeader().setForeground(Colores.TEXTO_SECUNDARIO);
        ocupacionTable.setShowGrid(false);
        ocupacionTable.setIntercellSpacing(new Dimension(0, 0));
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < ocupacionTable.getColumnCount(); i++) {
            ocupacionTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JPanel ocupacionPanel = crearPanelTabla("Ocupaci\u00f3n por ruta", new JScrollPane(ocupacionTable));

        salidasModel = new DefaultTableModel(new String[]{"Hora", "Ruta", "Bus", "Estado"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable salidasTable = new JTable(salidasModel);
        salidasTable.setRowHeight(28);
        salidasTable.getTableHeader().setBackground(Colores.FONDO_SUPERFICIE);
        salidasTable.getTableHeader().setForeground(Colores.TEXTO_SECUNDARIO);
        salidasTable.setShowGrid(false);
        salidasTable.setIntercellSpacing(new Dimension(0, 0));
        for (int i = 0; i < salidasTable.getColumnCount(); i++) {
            salidasTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JPanel salidasPanel = crearPanelTabla("Pr\u00f3ximas salidas", new JScrollPane(salidasTable));

        centerPanel.add(ocupacionPanel);
        centerPanel.add(salidasPanel);
        add(centerPanel, BorderLayout.CENTER);

        JPanel quickAccess = new JPanel(new GridLayout(1, 4, 10, 0));
        quickAccess.setOpaque(false);
        quickAccess.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        quickAccess.add(crearBotonAcceso("Nueva venta", "ventas"));
        quickAccess.add(crearBotonAcceso("Ida y vuelta", "ventas"));
        quickAccess.add(crearBotonAcceso("Cancelar salida", "cancelaciones"));
        quickAccess.add(crearBotonAcceso("Ver reportes", "reportes"));

        add(quickAccess, BorderLayout.SOUTH);

        actualizarDatos();
    }

    private JPanel crearTarjetaMetrica(String titulo, JLabel valor, Color colorTexto, Color iconBg) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(Colores.FONDO_TARJETA);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Colores.BORDE, 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        JPanel iconPanel = new JPanel();
        iconPanel.setPreferredSize(new Dimension(32, 32));
        iconPanel.setBackground(iconBg);
        iconPanel.setLayout(new BorderLayout());
        JLabel icono = new JLabel("\u25A0", SwingConstants.CENTER);
        icono.setForeground(colorTexto);
        iconPanel.add(icono, BorderLayout.CENTER);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        JLabel tituloLabel = new JLabel(titulo);
        tituloLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        tituloLabel.setForeground(Colores.TEXTO_SECUNDARIO);
        valor.setFont(new Font("SansSerif", Font.BOLD, 20));
        valor.setForeground(colorTexto);
        textPanel.add(tituloLabel);
        textPanel.add(valor);

        card.add(iconPanel, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel crearPanelTabla(String titulo, JScrollPane scroll) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Colores.FONDO_TARJETA);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Colores.BORDE, 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        JLabel tituloLabel = new JLabel(titulo);
        tituloLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        tituloLabel.setForeground(Colores.TEXTO_PRIMARIO);
        tituloLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        panel.add(tituloLabel, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JButton crearBotonAcceso(String texto, String navKey) {
        JButton btn = new JButton(texto);
        btn.setBackground(Colores.FONDO_SUPERFICIE);
        btn.setForeground(Colores.TEXTO_PRIMARIO);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btn.setBorder(new LineBorder(Colores.BORDE, 1));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            if (navListener != null) navListener.accept(navKey);
        });
        return btn;
    }

    public void setNavegacionListener(java.util.function.Consumer<String> listener) {
        this.navListener = listener;
    }

    public void actualizarDatos() {
        CajaVenta caja = empresa.getCajaVenta();
        int totalTiquetes = 0;
        for (PasajeTicket t : empresa.getMyTickets()) {
            if (PasajeTicket.VIGENTE.equals(t.getEstado())) totalTiquetes++;
        }
        vendidosLabel.setText(String.valueOf(totalTiquetes));
        ingresoLabel.setText(String.format("$%,.0f", caja.getIngresoNeto()));
        reembolsosLabel.setText(String.format("$%,.0f", caja.getTotalReembolsado()));

        int activos = 0;
        for (Bus b : empresa.listarBuses()) {
            if (Bus.DISPONIBLE.equals(b.getEstado())) activos++;
        }
        busesLabel.setText(String.valueOf(activos));

        ocupacionModel.setRowCount(0);
        Map<String, int[]> rutaStats = new LinkedHashMap<>();
        for (Ruta r : empresa.listarRutas()) {
            rutaStats.put(r.getCodigo(), new int[]{0, 0});
        }
        for (PasajeTicket t : empresa.getMyTickets()) {
            if (PasajeTicket.VIGENTE.equals(t.getEstado())) {
                String cod = t.getMySalida().getMyRuta().getCodigo();
                int[] stats = rutaStats.get(cod);
                if (stats != null) stats[0]++;
            }
        }
        for (Salida s : empresa.listarSalidas()) {
            String cod = s.getMyRuta().getCodigo();
            int[] stats = rutaStats.get(cod);
            if (stats != null) stats[1] += s.getMyBus().getCapacidad();
        }
        for (Map.Entry<String, int[]> e : rutaStats.entrySet()) {
            int vend = e.getValue()[0];
            int cap = e.getValue()[1];
            String pct = cap > 0 ? String.format("%.0f%%", (vend * 100.0 / cap)) : "0%";
            ocupacionModel.addRow(new Object[]{e.getKey(), vend, cap, pct});
        }

        salidasModel.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
        List<Salida> salidas = new ArrayList<>();
        for (Salida s : empresa.listarSalidas()) {
            if (Salida.PROGRAMADA.equals(s.getEstado())) salidas.add(s);
        }
        salidas.sort((a, b) -> a.getFecha().compareTo(b.getFecha()));
        for (Salida s : salidas) {
            salidasModel.addRow(new Object[]{
                s.getFecha().format(fmt),
                s.getMyRuta().getCodigo(),
                s.getMyBus().getPlaca(),
                s.getEstado()
            });
        }
    }
}

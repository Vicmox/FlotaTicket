package presentacion;

import negocio.*;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReportesPanel extends JPanel {

    private final EmpresaTransporte empresa;

    public ReportesPanel(EmpresaTransporte empresa) {
        this.empresa = empresa;
        setLayout(new BorderLayout());
        setBackground(Colores.FONDO_GENERAL);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tabs.addTab("Ventas por ruta", crearPanelVentasPorRuta());
        tabs.addTab("Totales del d\u00eda", crearPanelTotalesDia());
        tabs.addTab("Ventas por mes / rango", crearPanelVentasRango());

        add(tabs, BorderLayout.CENTER);
    }

    // ============================================================
    // TAB VENTAS POR RUTA
    // ============================================================

    private JPanel crearPanelVentasPorRuta() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Colores.FONDO_GENERAL);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filtros.setOpaque(false);

        SpinnerDateModel desdeModel = new SpinnerDateModel();
        JSpinner desdeSpinner = new JSpinner(desdeModel);
        desdeSpinner.setEditor(new JSpinner.DateEditor(desdeSpinner, "dd/MM/yyyy"));

        SpinnerDateModel hastaModel = new SpinnerDateModel();
        JSpinner hastaSpinner = new JSpinner(hastaModel);
        hastaSpinner.setEditor(new JSpinner.DateEditor(hastaSpinner, "dd/MM/yyyy"));

        JComboBox<String> rutaCombo = new JComboBox<>();
        rutaCombo.addItem("Todas");
        for (Ruta r : empresa.listarRutas()) {
            rutaCombo.addItem(r.getCodigo() + " — " + r.getOrigen() + " \u2192 " + r.getDestino());
        }

        filtros.add(new JLabel("Desde:"));
        filtros.add(desdeSpinner);
        filtros.add(new JLabel("Hasta:"));
        filtros.add(hastaSpinner);
        filtros.add(new JLabel("Ruta:"));
        filtros.add(rutaCombo);

        JButton generarBtn = new JButton("Generar");
        generarBtn.setBackground(Colores.AZUL_PRIMARIO);
        generarBtn.setForeground(Color.WHITE);
        generarBtn.setFocusPainted(false);
        filtros.add(generarBtn);

        panel.add(filtros, BorderLayout.NORTH);

        JPanel metricas = new JPanel(new GridLayout(1, 4, 10, 0));
        metricas.setOpaque(false);

        JLabel totalVendido = new JLabel("$0");
        JLabel reembolsado = new JLabel("$0");
        JLabel ingresoNeto = new JLabel("$0");
        JLabel ocupacion = new JLabel("0%");

        metricas.add(crearTarjetaSimple("Total vendido", totalVendido, Colores.ESTADO_VERDE_TX));
        metricas.add(crearTarjetaSimple("Reembolsado", reembolsado, Colores.ESTADO_ROJO_TX));
        metricas.add(crearTarjetaSimple("Ingreso neto", ingresoNeto, Colores.TEXTO_PRIMARIO));
        metricas.add(crearTarjetaSimple("Ocupaci\u00f3n promedio", ocupacion, Colores.ESTADO_AZUL_TX));

        generarBtn.addActionListener(e -> {
            CajaVenta caja = empresa.getCajaVenta();
            totalVendido.setText(String.format("$%,.0f", caja.getTotalVendido()));
            reembolsado.setText(String.format("$%,.0f", caja.getTotalReembolsado()));
            ingresoNeto.setText(String.format("$%,.0f", caja.getIngresoNeto()));
            int totalCap = 0;
            for (Bus b : empresa.listarBuses()) totalCap += b.getCapacidad();
            int totalVend = (int)caja.getTotalVendido();
            ocupacion.setText(totalCap > 0 ? String.format("%.0f%%", totalVend * 100.0 / totalCap) : "0%");
        });

        DefaultTableModel model = new DefaultTableModel(new String[]{"Ruta", "Tiquetes", "Reembolsos", "Total neto", "Ocupaci\u00f3n %"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(28);

        JButton exportarBtn = new JButton("Exportar");
        exportarBtn.addActionListener(e ->
            JOptionPane.showMessageDialog(panel, "Funcionalidad de exportaci\u00f3n pr\u00f3ximamente disponible.")
        );

        panel.add(metricas, BorderLayout.CENTER);

        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setOpaque(false);
        tableContainer.add(new JScrollPane(table), BorderLayout.CENTER);
        tableContainer.add(exportarBtn, BorderLayout.SOUTH);
        panel.add(tableContainer, BorderLayout.SOUTH);

        return panel;
    }

    // ============================================================
    // TAB TOTALES DEL DIA
    // ============================================================

    private JPanel crearPanelTotalesDia() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Colores.FONDO_GENERAL);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel metricas = new JPanel(new GridLayout(1, 4, 15, 0));
        metricas.setOpaque(false);

        CajaVenta caja = empresa.getCajaVenta();

        metricas.add(crearTarjetaGrande("Monto en caja", String.format("$%,.0f", caja.getMontoCaja()), Colores.AZUL_CLARO, Colores.AZUL_PRIMARIO));
        metricas.add(crearTarjetaGrande("Total vendido", String.format("$%,.0f", caja.getTotalVendido()), Colores.ESTADO_VERDE, Colores.ESTADO_VERDE_TX));
        metricas.add(crearTarjetaGrande("Reembolsado", String.format("$%,.0f", caja.getTotalReembolsado()), Colores.ESTADO_ROJO, Colores.ESTADO_ROJO_TX));
        metricas.add(crearTarjetaGrande("Ingreso neto", String.format("$%,.0f", caja.getIngresoNeto()), new Color(200,230,200), new Color(30,100,30)));

        panel.add(metricas, BorderLayout.CENTER);

        JLabel fechaReporte = new JLabel("Reporte generado: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        fechaReporte.setFont(new Font("SansSerif", Font.PLAIN, 11));
        fechaReporte.setForeground(Colores.TEXTO_SECUNDARIO);
        fechaReporte.setHorizontalAlignment(SwingConstants.CENTER);
        fechaReporte.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        panel.add(fechaReporte, BorderLayout.SOUTH);

        return panel;
    }

    // ============================================================
    // TAB VENTAS POR MES / RANGO
    // ============================================================

    private JPanel crearPanelVentasRango() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Colores.FONDO_GENERAL);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filtros.setOpaque(false);

        SpinnerDateModel desdeModel = new SpinnerDateModel();
        JSpinner desdeSpinner = new JSpinner(desdeModel);
        desdeSpinner.setEditor(new JSpinner.DateEditor(desdeSpinner, "dd/MM/yyyy"));

        SpinnerDateModel hastaModel = new SpinnerDateModel();
        JSpinner hastaSpinner = new JSpinner(hastaModel);
        hastaSpinner.setEditor(new JSpinner.DateEditor(hastaSpinner, "dd/MM/yyyy"));

        filtros.add(new JLabel("Desde:"));
        filtros.add(desdeSpinner);
        filtros.add(new JLabel("Hasta:"));
        filtros.add(hastaSpinner);

        JButton generarBtn = new JButton("Generar");
        generarBtn.setBackground(Colores.AZUL_PRIMARIO);
        generarBtn.setForeground(Color.WHITE);
        generarBtn.setFocusPainted(false);
        filtros.add(generarBtn);

        panel.add(filtros, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(new String[]{"Fecha", "Ruta", "Tiquetes vendidos", "Reembolsos", "Total"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(28);

        generarBtn.addActionListener(e -> {
            model.setRowCount(0);
            for (PasajeTicket t : empresa.getMyTickets()) {
                model.addRow(new Object[]{
                    t.getMySalida().getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    t.getMySalida().getMyRuta().getCodigo(),
                    PasajeTicket.VIGENTE.equals(t.getEstado()) ? 1 : 0,
                    PasajeTicket.REEMBOLSADO.equals(t.getEstado()) ? 1 : 0,
                    String.format("$%,.0f", t.getValorPagar())
                });
            }
        });

        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private JPanel crearTarjetaSimple(String titulo, JLabel valor, Color colorTexto) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Colores.FONDO_TARJETA);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Colores.BORDE, 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        JLabel tituloLabel = new JLabel(titulo);
        tituloLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        tituloLabel.setForeground(Colores.TEXTO_SECUNDARIO);
        valor.setFont(new Font("SansSerif", Font.BOLD, 18));
        valor.setForeground(colorTexto);
        card.add(tituloLabel, BorderLayout.NORTH);
        card.add(valor, BorderLayout.CENTER);
        return card;
    }

    private JPanel crearTarjetaGrande(String titulo, String valor, Color bgColor, Color txColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(bgColor);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Colores.BORDE, 1),
            BorderFactory.createEmptyBorder(25, 20, 25, 20)
        ));
        JLabel tituloLabel = new JLabel(titulo, SwingConstants.CENTER);
        tituloLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        tituloLabel.setForeground(txColor);
        JLabel valorLabel = new JLabel(valor, SwingConstants.CENTER);
        valorLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        valorLabel.setForeground(txColor);
        card.add(tituloLabel, BorderLayout.NORTH);
        card.add(valorLabel, BorderLayout.CENTER);
        return card;
    }
}

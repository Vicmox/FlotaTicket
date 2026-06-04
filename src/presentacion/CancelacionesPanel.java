package presentacion;

import negocio.*;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CancelacionesPanel extends JPanel {

    private final EmpresaTransporte empresa;
    private final JTextField idField;
    private final JLabel rutaLabel;
    private final JLabel fechaLabel;
    private final JLabel busLabel;
    private final JLabel ticketsLabel;
    private final DefaultTableModel ticketsModel;
    private final JPanel alertaPanel;
    private final JRadioButton reprogramarRadio;
    private final JRadioButton reembolsarRadio;
    private final JButton confirmarBtn;
    private Salida salidaEncontrada;

    public CancelacionesPanel(EmpresaTransporte empresa) {
        this.empresa = empresa;
        setLayout(new BorderLayout(10, 10));
        setBackground(Colores.FONDO_GENERAL);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel buscarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        buscarPanel.setOpaque(false);

        buscarPanel.add(new JLabel("ID de salida:"));
        idField = new JTextField(10);
        buscarPanel.add(idField);

        JComboBox<String> rutaCombo = new JComboBox<>();
        rutaCombo.addItem("Todas");
        for (Ruta r : empresa.listarRutas()) {
            rutaCombo.addItem(r.getCodigo());
        }
        buscarPanel.add(new JLabel("Ruta:"));
        buscarPanel.add(rutaCombo);

        JButton buscarBtn = new JButton("Buscar");
        buscarBtn.setBackground(Colores.AZUL_PRIMARIO);
        buscarBtn.setForeground(Color.WHITE);
        buscarBtn.setFocusPainted(false);
        buscarPanel.add(buscarBtn);

        add(buscarPanel, BorderLayout.NORTH);

        alertaPanel = new JPanel();
        alertaPanel.setBackground(Colores.ESTADO_ROJO);
        alertaPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Colores.ESTADO_ROJO_TX, 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        alertaPanel.setLayout(new BoxLayout(alertaPanel, BoxLayout.Y_AXIS));
        alertaPanel.setVisible(false);

        JLabel alertaTitulo = new JLabel("Salida encontrada");
        alertaTitulo.setFont(new Font("SansSerif", Font.BOLD, 13));
        alertaTitulo.setForeground(Colores.ESTADO_ROJO_TX);
        alertaPanel.add(alertaTitulo);
        JLabel alertaMsg = new JLabel("Al cambiar el estado se afectar\u00e1n los tiquetes asociados.");
        alertaMsg.setFont(new Font("SansSerif", Font.PLAIN, 11));
        alertaMsg.setForeground(Colores.ESTADO_ROJO_TX);
        alertaPanel.add(alertaMsg);

        add(alertaPanel, BorderLayout.CENTER);

        JPanel centralPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        centralPanel.setOpaque(false);

        JPanel datosPanel = new JPanel();
        datosPanel.setLayout(new BoxLayout(datosPanel, BoxLayout.Y_AXIS));
        datosPanel.setBackground(Colores.FONDO_TARJETA);
        datosPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Colores.BORDE, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        datosPanel.add(new JLabel("Datos de la salida"));
        datosPanel.add(Box.createVerticalStrut(8));

        rutaLabel = new JLabel("Ruta: —");
        fechaLabel = new JLabel("Fecha y hora: —");
        busLabel = new JLabel("Bus asignado: —");
        ticketsLabel = new JLabel("Tiquetes VIGENTES: 0");

        datosPanel.add(rutaLabel);
        datosPanel.add(Box.createVerticalStrut(4));
        datosPanel.add(fechaLabel);
        datosPanel.add(Box.createVerticalStrut(4));
        datosPanel.add(busLabel);
        datosPanel.add(Box.createVerticalStrut(4));
        datosPanel.add(ticketsLabel);

        ticketsModel = new DefaultTableModel(new String[]{"Tiquete", "Pasajero", "Silla", "Estado"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable ticketsTable = new JTable(ticketsModel);
        ticketsTable.setRowHeight(28);

        JPanel ticketsContainer = new JPanel(new BorderLayout());
        ticketsContainer.setBackground(Colores.FONDO_TARJETA);
        ticketsContainer.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Colores.BORDE, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        ticketsContainer.add(new JLabel("Tiquetes afectados"), BorderLayout.NORTH);
        ticketsContainer.add(new JScrollPane(ticketsTable), BorderLayout.CENTER);

        centralPanel.add(datosPanel);
        centralPanel.add(ticketsContainer);

        add(centralPanel, BorderLayout.CENTER);

        JPanel accionesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
        accionesPanel.setBackground(Colores.FONDO_SUPERFICIE);
        accionesPanel.setBorder(new LineBorder(Colores.BORDE, 1));

        reprogramarRadio = new JRadioButton("Reprogramar autom\u00e1ticamente (mismo d\u00eda hora posterior o m\u00e1ximo 1 d\u00eda despu\u00e9s)");
        reprogramarRadio.setFont(new Font("SansSerif", Font.PLAIN, 12));
        reprogramarRadio.setOpaque(false);
        reembolsarRadio = new JRadioButton("Marcar como REEMBOLSADO");
        reembolsarRadio.setFont(new Font("SansSerif", Font.PLAIN, 12));
        reembolsarRadio.setOpaque(false);
        ButtonGroup grupo = new ButtonGroup();
        grupo.add(reprogramarRadio);
        grupo.add(reembolsarRadio);
        reprogramarRadio.setSelected(true);

        accionesPanel.add(reprogramarRadio);
        accionesPanel.add(reembolsarRadio);

        JButton cancelarBtn = new JButton("Cancelar");
        JButton confirmarBtn = new JButton("Confirmar cancelaci\u00f3n y generar reporte");
        confirmarBtn.setBackground(Colores.ESTADO_ROJO_TX);
        confirmarBtn.setForeground(Color.WHITE);
        confirmarBtn.setFocusPainted(false);
        this.confirmarBtn = confirmarBtn;

        accionesPanel.add(cancelarBtn);
        accionesPanel.add(confirmarBtn);
        add(accionesPanel, BorderLayout.SOUTH);

        buscarBtn.addActionListener(e -> buscarSalida());
        confirmarBtn.addActionListener(e -> confirmarCancelacion());
        cancelarBtn.addActionListener(e -> limpiarBusqueda());
    }

    private void buscarSalida() {
        String id = idField.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese un ID de salida");
            return;
        }
        salidaEncontrada = empresa.getSalidaPorId(id);
        if (salidaEncontrada == null) {
            JOptionPane.showMessageDialog(this, "Salida no encontrada", "Error", JOptionPane.ERROR_MESSAGE);
            alertaPanel.setVisible(false);
            return;
        }

        alertaPanel.setVisible(true);
        rutaLabel.setText("Ruta: " + salidaEncontrada.getMyRuta().getCodigo() + " — " + salidaEncontrada.getMyRuta().getOrigen() + " \u2192 " + salidaEncontrada.getMyRuta().getDestino());
        fechaLabel.setText("Fecha y hora: " + salidaEncontrada.getFecha().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        busLabel.setText("Bus asignado: " + salidaEncontrada.getMyBus().getPlaca());

        ticketsModel.setRowCount(0);
        int vigentes = 0;
        for (PasajeTicket t : empresa.getMyTickets()) {
            if (t.getMySalida().getIdSalida().equals(id)) {
                ticketsModel.addRow(new Object[]{
                    "TKT-" + t.hashCode(),
                    t.getMyPasajero().getNombre(),
                    t.getPuesto(),
                    t.getEstado()
                });
                if (PasajeTicket.VIGENTE.equals(t.getEstado())) vigentes++;
            }
        }
        ticketsLabel.setText("Tiquetes VIGENTES: " + vigentes);
    }

    private void confirmarCancelacion() {
        if (salidaEncontrada == null) return;
        boolean reembolsar = reembolsarRadio.isSelected();
        java.util.List<String> resultados = empresa.cancelarSalida(salidaEncontrada.getIdSalida(), reembolsar);

        Salida s = salidaEncontrada;
        String encabezado = "CANCELACION DE SALIDA: " + s.getIdSalida() + "  (" +
            s.getMyRuta().getCodigo() + " " + s.getMyRuta().getOrigen() + " -> " + s.getMyRuta().getDestino() + ") " +
            s.getFecha().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\n\n" +
            "Tiquetes procesados:\n";

        StringBuilder sb = new StringBuilder(encabezado);
        int reprogramados = 0;
        int reembolsadosCount = 0;

        for (String r : resultados) {
            sb.append("- ").append(r).append("\n");
            if (r.contains("REPROGRAMADO")) reprogramados++;
            else if (r.contains("REEMBOLSADO")) reembolsadosCount++;
        }

        sb.append("\nTOTAL REPROGRAMADOS: ").append(reprogramados).append("\n");
        sb.append("TOTAL REEMBOLSADOS: ").append(reembolsadosCount);

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setPreferredSize(new java.awt.Dimension(600, 350));
        JOptionPane.showMessageDialog(this, scroll, "Reporte de cancelaci\u00f3n", JOptionPane.INFORMATION_MESSAGE);

        limpiarBusqueda();
    }

    private void limpiarBusqueda() {
        idField.setText("");
        alertaPanel.setVisible(false);
        rutaLabel.setText("Ruta: —");
        fechaLabel.setText("Fecha y hora: —");
        busLabel.setText("Bus asignado: —");
        ticketsLabel.setText("Tiquetes VIGENTES: 0");
        ticketsModel.setRowCount(0);
        salidaEncontrada = null;
    }
}

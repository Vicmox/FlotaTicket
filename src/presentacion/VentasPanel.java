package presentacion;

import negocio.*;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class VentasPanel extends JPanel {

    private final EmpresaTransporte empresa;
    private final CardLayout subCardLayout;
    private final JPanel subPanel;

    public VentasPanel(EmpresaTransporte empresa) {
        this.empresa = empresa;
        setLayout(new BorderLayout(10, 10));
        setBackground(Colores.FONDO_GENERAL);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel tabBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabBar.setOpaque(false);

        JButton unTicketBtn = crearTabBtn("1 o m\u00e1s tiquetes", true);
        JButton idaVueltaBtn = crearTabBtn("Ida y vuelta", false);

        subCardLayout = new CardLayout();
        subPanel = new JPanel(subCardLayout);
        subPanel.setOpaque(false);

        subPanel.add(crearPanelUnTicket(), "unTicket");
        subPanel.add(crearPanelIdaVuelta(), "idaVuelta");

        unTicketBtn.addActionListener(e -> {
            subCardLayout.show(subPanel, "unTicket");
            unTicketBtn.setBackground(Colores.AZUL_PRIMARIO);
            unTicketBtn.setForeground(Color.WHITE);
            idaVueltaBtn.setBackground(Colores.FONDO_SUPERFICIE);
            idaVueltaBtn.setForeground(Colores.TEXTO_PRIMARIO);
        });
        idaVueltaBtn.addActionListener(e -> {
            subCardLayout.show(subPanel, "idaVuelta");
            idaVueltaBtn.setBackground(Colores.AZUL_PRIMARIO);
            idaVueltaBtn.setForeground(Color.WHITE);
            unTicketBtn.setBackground(Colores.FONDO_SUPERFICIE);
            unTicketBtn.setForeground(Colores.TEXTO_PRIMARIO);
        });

        tabBar.add(unTicketBtn);
        tabBar.add(idaVueltaBtn);
        add(tabBar, BorderLayout.NORTH);
        add(subPanel, BorderLayout.CENTER);
    }

    private JButton crearTabBtn(String texto, boolean activo) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btn.setBorder(new LineBorder(Colores.BORDE, 1));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(160, 35));
        if (activo) {
            btn.setBackground(Colores.AZUL_PRIMARIO);
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(Colores.FONDO_SUPERFICIE);
            btn.setForeground(Colores.TEXTO_PRIMARIO);
        }
        return btn;
    }

    // ============================================================
    // PANEL 1 O MÁS TIQUETES (RF2)
    // ============================================================

    private JComboBox<String> salidaComboUnTicket;
    private JPanel sillasGridUnTicket;
    private JSpinner cantidadSpinner;
    private JPanel pasajerosPanel;
    private List<PasajeroForm> pasajeroForms = new ArrayList<>();
    private int[] selectedBlock; // puestos consecutivos seleccionados
    private String selectedSalidaIdUnTicket;

    private JLabel resumenRuta, resumenSalida, resumenBus, resumenPuestos, resumenTotal;

    private JPanel crearPanelUnTicket() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);

        leftPanel.add(crearPasoSeleccionSalida());
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(crearPanelCantidadYSillas());
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(crearPanelDatosPasajeros());

        JPanel rightPanel = crearPanelResumen();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        split.setDividerLocation(520);
        split.setBorder(null);
        panel.add(split, BorderLayout.CENTER);

        return panel;
    }

    private JPanel crearPasoSeleccionSalida() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Colores.FONDO_TARJETA);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Colores.BORDE, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        JLabel titulo = new JLabel("Paso 1: Seleccionar salida");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 12));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(titulo, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Origen:"), gbc);
        gbc.gridx = 1;
        JTextField origenField = new JTextField("C\u00facuta");
        origenField.setEditable(false);
        panel.add(origenField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Destino:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> destinoCombo = new JComboBox<>();
        for (Ruta r : empresa.listarRutas()) {
            destinoCombo.addItem(r.getDestino());
        }
        panel.add(destinoCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Fecha:"), gbc);
        gbc.gridx = 1;
        JSpinner fechaSpinner = new JSpinner(new SpinnerDateModel());
        fechaSpinner.setEditor(new JSpinner.DateEditor(fechaSpinner, "dd/MM/yyyy"));
        panel.add(fechaSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Salida:"), gbc);
        gbc.gridx = 1;
        salidaComboUnTicket = new JComboBox<>();
        cargarSalidasEnCombo(salidaComboUnTicket, null);
        destinoCombo.addActionListener(e -> cargarSalidasEnCombo(salidaComboUnTicket, (String) destinoCombo.getSelectedItem()));
        salidaComboUnTicket.addActionListener(e -> onSalidaUnTicketChanged());
        panel.add(salidaComboUnTicket, gbc);

        return panel;
    }

    private JPanel crearPanelCantidadYSillas() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Colores.FONDO_TARJETA);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Colores.BORDE, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel titulo = new JLabel("Paso 2: Seleccionar cantidad y puestos");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 12));
        panel.add(titulo, BorderLayout.NORTH);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);
        top.add(new JLabel("Cantidad de puestos:"));
        cantidadSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 40, 1));
        cantidadSpinner.addChangeListener(e -> onCantidadChanged());
        top.add(cantidadSpinner);
        panel.add(top, BorderLayout.NORTH);
        // Lo reposicionamos al norte del panel central
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);

        sillasGridUnTicket = new JPanel();
        sillasGridUnTicket.setOpaque(false);
        centerWrapper.add(sillasGridUnTicket, BorderLayout.CENTER);

        JPanel leyenda = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        leyenda.setOpaque(false);
        leyenda.add(crearLeyenda(Colores.ESTADO_VERDE, "Disponible"));
        leyenda.add(crearLeyenda(Colores.AZUL_PRIMARIO, "Seleccionada"));
        leyenda.add(crearLeyenda(Colores.FONDO_SUPERFICIE, "Ocupada"));
        centerWrapper.add(leyenda, BorderLayout.SOUTH);

        panel.add(centerWrapper, BorderLayout.CENTER);

        // Mejorar layout
        panel.remove(top);
        panel.add(top, BorderLayout.NORTH);
        panel.add(centerWrapper, BorderLayout.CENTER);

        return panel;
    }

    private JPanel crearPanelDatosPasajeros() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Colores.FONDO_TARJETA);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Colores.BORDE, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel titulo = new JLabel("Paso 3: Datos de los pasajeros");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 12));
        panel.add(titulo, BorderLayout.NORTH);

        pasajerosPanel = new JPanel();
        pasajerosPanel.setLayout(new BoxLayout(pasajerosPanel, BoxLayout.Y_AXIS));
        pasajerosPanel.setOpaque(false);
        panel.add(new JScrollPane(pasajerosPanel), BorderLayout.CENTER);

        return panel;
    }

    private JPanel crearPanelResumen() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Colores.FONDO_SUPERFICIE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Colores.BORDE, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titulo = new JLabel("Resumen de compra");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 13));
        titulo.setForeground(Colores.TEXTO_PRIMARIO);
        panel.add(titulo);
        panel.add(Box.createVerticalStrut(10));

        resumenRuta = new JLabel("Ruta: —");
        resumenSalida = new JLabel("Salida: —");
        resumenBus = new JLabel("Bus: —");
        resumenPuestos = new JLabel("Puestos: —");
        panel.add(resumenRuta);
        panel.add(resumenSalida);
        panel.add(resumenBus);
        panel.add(resumenPuestos);
        panel.add(Box.createVerticalStrut(15));
        panel.add(new JSeparator());
        panel.add(Box.createVerticalStrut(10));

        resumenTotal = new JLabel("Total: $0");
        resumenTotal.setFont(new Font("SansSerif", Font.BOLD, 15));
        resumenTotal.setForeground(Colores.TEXTO_PRIMARIO);
        panel.add(resumenTotal);
        panel.add(Box.createVerticalStrut(15));

        panel.add(new JLabel("Forma de pago:"));
        panel.add(new JComboBox<>(new String[]{"Efectivo", "Tarjeta d\u00e9bito", "Tarjeta cr\u00e9dito"}));
        panel.add(Box.createVerticalStrut(15));

        JButton generarBtn = new JButton("Generar tiquetes");
        generarBtn.setBackground(Colores.AZUL_PRIMARIO);
        generarBtn.setForeground(Color.WHITE);
        generarBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        generarBtn.setFocusPainted(false);
        generarBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        generarBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        generarBtn.addActionListener(e -> generarUnTicket());
        panel.add(generarBtn);

        return panel;
    }

    private void cargarSalidasEnCombo(JComboBox<String> combo, String destino) {
        combo.removeAllItems();
        for (Salida s : empresa.listarSalidas()) {
            if (!Salida.PROGRAMADA.equals(s.getEstado())) continue;
            if (destino == null || s.getMyRuta().getDestino().equalsIgnoreCase(destino)) {
                combo.addItem(s.getIdSalida() + " — " + s.getMyRuta().getOrigen() + " \u2192 " + s.getMyRuta().getDestino()
                    + " (" + s.getFecha().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")) + ")");
            }
        }
    }

    private void onSalidaUnTicketChanged() {
        String sel = (String) salidaComboUnTicket.getSelectedItem();
        if (sel == null) return;
        selectedSalidaIdUnTicket = sel.split(" —")[0].trim();
        selectedBlock = null;
        rebuildSeatGridUnTicket();
        onCantidadChanged();
        actualizarResumenUnTicket();
    }

    private void onCantidadChanged() {
        int cantidad = (Integer) cantidadSpinner.getValue();
        if (selectedSalidaIdUnTicket == null) return;
        Salida salida = empresa.getSalidaPorId(selectedSalidaIdUnTicket);
        if (salida == null) return;
        int[] bloque = empresa.verificarPuestosConsecutivos(salida, cantidad);
        if (bloque != null) {
            selectedBlock = bloque;
            rebuildSeatGridUnTicket();
            rebuildPasajeroForms(cantidad);
        } else {
            selectedBlock = null;
            rebuildSeatGridUnTicket();
            pasajerosPanel.removeAll();
            pasajeroForms.clear();
            pasajerosPanel.revalidate();
            pasajerosPanel.repaint();
            JOptionPane.showMessageDialog(this, "No hay " + cantidad + " puestos consecutivos disponibles en esta salida.", "Sin cupo", JOptionPane.WARNING_MESSAGE);
        }
        actualizarResumenUnTicket();
    }

    private void rebuildSeatGridUnTicket() {
        sillasGridUnTicket.removeAll();
        if (selectedSalidaIdUnTicket == null) return;
        Salida salida = empresa.getSalidaPorId(selectedSalidaIdUnTicket);
        if (salida == null) return;
        Bus bus = salida.getMyBus();
        if (bus == null) return;
        Puesto[] puestos = bus.getMyPuestos();
        int cols = 4;
        int rows = (int) Math.ceil(puestos.length / (double) cols);
        sillasGridUnTicket.setLayout(new GridLayout(rows, cols, 4, 4));

        boolean[] enBloque = new boolean[puestos.length];
        if (selectedBlock != null) {
            for (int p : selectedBlock) {
                if (p >= 1 && p <= puestos.length) enBloque[p - 1] = true;
            }
        }

        for (int i = 0; i < puestos.length; i++) {
            final int numPuesto = i + 1;
            JButton btn = new JButton(String.valueOf(numPuesto));
            btn.setPreferredSize(new Dimension(40, 40));
            btn.setFont(new Font("SansSerif", Font.BOLD, 10));
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            if (puestos[i].estaLibre()) {
                if (enBloque[i]) {
                    btn.setBackground(Colores.AZUL_PRIMARIO);
                    btn.setForeground(Color.WHITE);
                } else {
                    btn.setBackground(Colores.ESTADO_VERDE);
                    btn.setForeground(Colores.ESTADO_VERDE_TX);
                }
                btn.addActionListener(e -> {
                    int cantidad = (Integer) cantidadSpinner.getValue();
                    // Intentar anclar bloque desde este puesto
                    boolean puede = true;
                    for (int j = 0; j < cantidad; j++) {
                        int idx = numPuesto - 1 + j;
                        if (idx >= puestos.length || !puestos[idx].estaLibre()) {
                            puede = false; break;
                        }
                    }
                    if (puede) {
                        selectedBlock = new int[cantidad];
                        for (int j = 0; j < cantidad; j++) selectedBlock[j] = numPuesto + j;
                        rebuildSeatGridUnTicket();
                        rebuildPasajeroForms(cantidad);
                        actualizarResumenUnTicket();
                    } else {
                        JOptionPane.showMessageDialog(this, "No hay suficientes puestos consecutivos desde esta posici\u00f3n.", "Error", JOptionPane.WARNING_MESSAGE);
                    }
                });
            } else {
                btn.setBackground(Colores.FONDO_SUPERFICIE);
                btn.setForeground(Colores.TEXTO_SECUNDARIO);
                btn.setEnabled(false);
            }
            sillasGridUnTicket.add(btn);
        }
        sillasGridUnTicket.revalidate();
        sillasGridUnTicket.repaint();
    }

    private void rebuildPasajeroForms(int cantidad) {
        pasajerosPanel.removeAll();
        pasajeroForms.clear();
        for (int i = 0; i < cantidad; i++) {
            int puesto = selectedBlock != null && i < selectedBlock.length ? selectedBlock[i] : (i + 1);
            PasajeroForm form = new PasajeroForm(puesto, empresa, pasajerosPanel);
            pasajeroForms.add(form);
            pasajerosPanel.add(form);
            pasajerosPanel.add(Box.createVerticalStrut(8));
        }
        pasajerosPanel.revalidate();
        pasajerosPanel.repaint();
    }

    private void generarUnTicket() {
        if (selectedSalidaIdUnTicket == null || selectedBlock == null) {
            JOptionPane.showMessageDialog(this, "Seleccione una salida y un bloque de puestos");
            return;
        }
        int cantidad = (Integer) cantidadSpinner.getValue();
        if (pasajeroForms.size() != cantidad) {
            JOptionPane.showMessageDialog(this, "Datos de pasajeros incompletos");
            return;
        }

        Pasajero[] pasajeros = new Pasajero[cantidad];
        for (int i = 0; i < cantidad; i++) {
            PasajeroForm form = pasajeroForms.get(i);
            String cedula = form.cedulaField.getText().trim();
            String nombre = form.nombreField.getText().trim();
            String correo = form.correoField.getText().trim();
            String telefono = form.telefonoField.getText().trim();
            if (cedula.isEmpty() || nombre.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Complete c\u00e9dula y nombre para el pasajero del puesto " + form.puesto);
                return;
            }
            pasajeros[i] = empresa.buscarOCrearPasajero(cedula, nombre, "", correo, telefono);
        }

        PasajeTicket[] tickets = empresa.generarTickets(selectedSalidaIdUnTicket, selectedBlock, pasajeros, false);
        if (tickets != null) {
            float total = 0f;
            StringBuilder sb = new StringBuilder("Tiquetes generados exitosamente:\n");
            for (PasajeTicket t : tickets) {
                total += t.getValorPagar();
                sb.append("Puesto ").append(t.getPuesto()).append(" — ").append(t.getMyPasajero().getNombre())
                  .append(" — $").append(String.format("%,.0f", t.getValorPagar())).append("\n");
            }
            sb.append("Total: $").append(String.format("%,.0f", total));
            JOptionPane.showMessageDialog(this, sb.toString());
            limpiarFormularioUnTicket();
        } else {
            JOptionPane.showMessageDialog(this, "Error al generar los tiquetes.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarFormularioUnTicket() {
        selectedBlock = null;
        selectedSalidaIdUnTicket = null;
        pasajeroForms.clear();
        pasajerosPanel.removeAll();
        pasajerosPanel.revalidate();
        pasajerosPanel.repaint();
        cantidadSpinner.setValue(1);
        if (salidaComboUnTicket.getItemCount() > 0) salidaComboUnTicket.setSelectedIndex(0);
        rebuildSeatGridUnTicket();
        actualizarResumenUnTicket();
    }

    private void actualizarResumenUnTicket() {
        if (selectedSalidaIdUnTicket == null) {
            resumenRuta.setText("Ruta: —");
            resumenSalida.setText("Salida: —");
            resumenBus.setText("Bus: —");
            resumenPuestos.setText("Puestos: —");
            resumenTotal.setText("Total: $0");
            return;
        }
        Salida salida = empresa.getSalidaPorId(selectedSalidaIdUnTicket);
        if (salida == null) return;
        resumenRuta.setText("Ruta: " + salida.getMyRuta().getCodigo() + " — " + salida.getMyRuta().getOrigen() + " \u2192 " + salida.getMyRuta().getDestino());
        resumenSalida.setText("Salida: " + salida.getIdSalida() + " (" + salida.getFecha().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")) + ")");
        resumenBus.setText("Bus: " + salida.getMyBus().getPlaca() + " (" + (salida.getMyBus() instanceof BusTipoEjecutivo ? "Ejecutivo" : "Normal") + ")");
        if (selectedBlock != null) {
            StringBuilder sb = new StringBuilder("Puestos: ");
            for (int p : selectedBlock) sb.append(p).append(" ");
            resumenPuestos.setText(sb.toString());
            float total = 0f;
            for (int i = 0; i < selectedBlock.length; i++) {
                float valor = salida.getMyRuta().getTarifa();
                PasajeroForm form = i < pasajeroForms.size() ? pasajeroForms.get(i) : null;
                if (form != null && !form.cedulaField.getText().trim().isEmpty()) {
                    Pasajero p = empresa.getPasajeroPorCedula(form.cedulaField.getText().trim());
                    if (p != null && p.esPreferencial()) {
                        valor = valor * 0.9f;
                    }
                }
                total += valor;
            }
            resumenTotal.setText("Total: $" + String.format("%,.0f", total));
        } else {
            resumenPuestos.setText("Puestos: —");
            resumenTotal.setText("Total: $0");
        }
    }

    // ============================================================
    // PANEL IDA Y VUELTA (RF3)
    // ============================================================

    private JComboBox<String> idaCombo, vueltaCombo;
    private JPanel sillasGridIda, sillasGridVuelta;
    private JSpinner cantidadIdaSpinner, cantidadVueltaSpinner;
    private int[] selectedBlockIda, selectedBlockVuelta;
    private String selectedSalidaIdIda, selectedSalidaIdVuelta;
    private JLabel resumenIyVRuta, resumenIyVIda, resumenIyVVuelta, resumenIyVPuestos, resumenIyVTotal, validacionRutaLabel;

    private List<PassengerRoundTripForm> pasajeroFormsIda = new ArrayList<>();
    private List<PassengerRoundTripForm> pasajeroFormsVuelta = new ArrayList<>();

    private JPanel crearPanelIdaVuelta() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);

        leftPanel.add(crearPanelSeleccionIdaVuelta());
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(crearPanelSillasIdaVuelta());
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(crearPanelDatosPasajerosIyV());

        JPanel resumenPanel = crearPanelResumenIyV();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, resumenPanel);
        split.setDividerLocation(520);
        split.setBorder(null);
        panel.add(split, BorderLayout.CENTER);

        return panel;
    }

    private JPanel crearPanelSeleccionIdaVuelta() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Colores.FONDO_TARJETA);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Colores.BORDE, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        JLabel titulo = new JLabel("Seleccionar salidas (ida y vuelta)");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 12));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(titulo, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Ida:"), gbc);
        gbc.gridx = 1;
        idaCombo = new JComboBox<>();
        cargarSalidasEnCombo(idaCombo, null);
        idaCombo.addActionListener(e -> onIdaChanged());
        panel.add(idaCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Vuelta:"), gbc);
        gbc.gridx = 1;
        vueltaCombo = new JComboBox<>();
        cargarSalidasEnCombo(vueltaCombo, null);
        vueltaCombo.addActionListener(e -> onVueltaChanged());
        panel.add(vueltaCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        validacionRutaLabel = new JLabel(" ");
        validacionRutaLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        panel.add(validacionRutaLabel, gbc);

        return panel;
    }

    private JPanel crearPanelSillasIdaVuelta() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 0));
        panel.setOpaque(false);

        // Ida
        JPanel idaPanel = new JPanel(new BorderLayout());
        idaPanel.setBackground(Colores.FONDO_TARJETA);
        idaPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Colores.BORDE, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        JPanel topIda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topIda.setOpaque(false);
        topIda.add(new JLabel("Cantidad:"));
        cantidadIdaSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 40, 1));
        cantidadIdaSpinner.addChangeListener(e -> onCantidadIdaChanged());
        topIda.add(cantidadIdaSpinner);
        idaPanel.add(topIda, BorderLayout.NORTH);
        sillasGridIda = new JPanel();
        sillasGridIda.setOpaque(false);
        idaPanel.add(sillasGridIda, BorderLayout.CENTER);
        JPanel leyIda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        leyIda.setOpaque(false);
        leyIda.add(crearLeyenda(Colores.ESTADO_VERDE, "Libre"));
        leyIda.add(crearLeyenda(Colores.AZUL_PRIMARIO, "Sel."));
        leyIda.add(crearLeyenda(Colores.FONDO_SUPERFICIE, "Ocup."));
        idaPanel.add(leyIda, BorderLayout.SOUTH);

        // Vuelta
        JPanel vueltaPanel = new JPanel(new BorderLayout());
        vueltaPanel.setBackground(Colores.FONDO_TARJETA);
        vueltaPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Colores.BORDE, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        JPanel topVuelta = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topVuelta.setOpaque(false);
        topVuelta.add(new JLabel("Cantidad:"));
        cantidadVueltaSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 40, 1));
        cantidadVueltaSpinner.addChangeListener(e -> onCantidadVueltaChanged());
        topVuelta.add(cantidadVueltaSpinner);
        vueltaPanel.add(topVuelta, BorderLayout.NORTH);
        sillasGridVuelta = new JPanel();
        sillasGridVuelta.setOpaque(false);
        vueltaPanel.add(sillasGridVuelta, BorderLayout.CENTER);
        JPanel leyVuelta = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        leyVuelta.setOpaque(false);
        leyVuelta.add(crearLeyenda(Colores.ESTADO_VERDE, "Libre"));
        leyVuelta.add(crearLeyenda(Colores.AZUL_PRIMARIO, "Sel."));
        leyVuelta.add(crearLeyenda(Colores.FONDO_SUPERFICIE, "Ocup."));
        vueltaPanel.add(leyVuelta, BorderLayout.SOUTH);

        panel.add(idaPanel);
        panel.add(vueltaPanel);

        return panel;
    }

    private JPanel crearPanelDatosPasajerosIyV() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Colores.FONDO_TARJETA);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Colores.BORDE, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        panel.add(new JLabel("Datos de los pasajeros (aplica para ida y vuelta)"), BorderLayout.NORTH);
        // Para simplificar, asumimos que los pasajeros de ida son los mismos de vuelta y usamos los mismos forms
        // Mostramos una nota
        return panel;
    }

    private JPanel crearPanelResumenIyV() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Colores.FONDO_SUPERFICIE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Colores.BORDE, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titulo = new JLabel("Resumen ida y vuelta");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 13));
        titulo.setForeground(Colores.TEXTO_PRIMARIO);
        panel.add(titulo);
        panel.add(Box.createVerticalStrut(10));

        resumenIyVRuta = new JLabel("Ruta: —");
        resumenIyVIda = new JLabel("Ida: —");
        resumenIyVVuelta = new JLabel("Vuelta: —");
        resumenIyVPuestos = new JLabel("Puestos: —");
        panel.add(resumenIyVRuta);
        panel.add(resumenIyVIda);
        panel.add(resumenIyVVuelta);
        panel.add(resumenIyVPuestos);
        panel.add(Box.createVerticalStrut(5));
        panel.add(new JSeparator());
        panel.add(Box.createVerticalStrut(5));

        resumenIyVTotal = new JLabel("Total: $0");
        resumenIyVTotal.setFont(new Font("SansSerif", Font.BOLD, 15));
        resumenIyVTotal.setForeground(Colores.TEXTO_PRIMARIO);
        panel.add(new JLabel("Descuento 10% sobre total (ida + vuelta)"));
        panel.add(resumenIyVTotal);
        panel.add(Box.createVerticalStrut(15));

        JButton generarBtn = new JButton("Generar tiquetes ida y vuelta");
        generarBtn.setBackground(Colores.AZUL_PRIMARIO);
        generarBtn.setForeground(Color.WHITE);
        generarBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        generarBtn.setFocusPainted(false);
        generarBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        generarBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        generarBtn.addActionListener(e -> generarIdaYVuelta());
        panel.add(generarBtn);

        return panel;
    }

    private void onIdaChanged() {
        String sel = (String) idaCombo.getSelectedItem();
        if (sel == null) return;
        selectedSalidaIdIda = sel.split(" —")[0].trim();
        selectedBlockIda = null;
        rebuildSeatGridIyV(sillasGridIda, selectedSalidaIdIda, true);
        onCantidadIdaChanged();
        validarMismaRuta();
        actualizarResumenIyV();
    }

    private void onVueltaChanged() {
        String sel = (String) vueltaCombo.getSelectedItem();
        if (sel == null) return;
        selectedSalidaIdVuelta = sel.split(" —")[0].trim();
        selectedBlockVuelta = null;
        rebuildSeatGridIyV(sillasGridVuelta, selectedSalidaIdVuelta, false);
        onCantidadVueltaChanged();
        validarMismaRuta();
        actualizarResumenIyV();
    }

    private void validarMismaRuta() {
        if (selectedSalidaIdIda == null || selectedSalidaIdVuelta == null) {
            validacionRutaLabel.setText(" ");
            return;
        }
        Salida ida = empresa.getSalidaPorId(selectedSalidaIdIda);
        Salida vuelta = empresa.getSalidaPorId(selectedSalidaIdVuelta);
        if (ida != null && vuelta != null) {
            if (ida.getMyRuta().getCodigo().equals(vuelta.getMyRuta().getCodigo())) {
                validacionRutaLabel.setText("Misma ruta validada (" + ida.getMyRuta().getCodigo() + ")");
                validacionRutaLabel.setForeground(Colores.ESTADO_VERDE_TX);
            } else {
                validacionRutaLabel.setText("Error: las salidas deben ser de la misma ruta");
                validacionRutaLabel.setForeground(Colores.ESTADO_ROJO_TX);
            }
        }
    }

    private void onCantidadIdaChanged() {
        int cantidad = (Integer) cantidadIdaSpinner.getValue();
        if (selectedSalidaIdIda == null) return;
        Salida salida = empresa.getSalidaPorId(selectedSalidaIdIda);
        if (salida == null) return;
        int[] bloque = empresa.verificarPuestosConsecutivos(salida, cantidad);
        if (bloque != null) {
            selectedBlockIda = bloque;
            rebuildSeatGridIyV(sillasGridIda, selectedSalidaIdIda, true);
        } else {
            selectedBlockIda = null;
            rebuildSeatGridIyV(sillasGridIda, selectedSalidaIdIda, true);
            JOptionPane.showMessageDialog(this, "No hay " + cantidad + " puestos consecutivos en la salida de ida.", "Sin cupo", JOptionPane.WARNING_MESSAGE);
        }
        actualizarResumenIyV();
    }

    private void onCantidadVueltaChanged() {
        int cantidad = (Integer) cantidadVueltaSpinner.getValue();
        if (selectedSalidaIdVuelta == null) return;
        Salida salida = empresa.getSalidaPorId(selectedSalidaIdVuelta);
        if (salida == null) return;
        int[] bloque = empresa.verificarPuestosConsecutivos(salida, cantidad);
        if (bloque != null) {
            selectedBlockVuelta = bloque;
            rebuildSeatGridIyV(sillasGridVuelta, selectedSalidaIdVuelta, false);
        } else {
            selectedBlockVuelta = null;
            rebuildSeatGridIyV(sillasGridVuelta, selectedSalidaIdVuelta, false);
            JOptionPane.showMessageDialog(this, "No hay " + cantidad + " puestos consecutivos en la salida de vuelta.", "Sin cupo", JOptionPane.WARNING_MESSAGE);
        }
        actualizarResumenIyV();
    }

    private void rebuildSeatGridIyV(JPanel grid, String salidaId, boolean esIda) {
        grid.removeAll();
        if (salidaId == null) return;
        Salida salida = empresa.getSalidaPorId(salidaId);
        if (salida == null) return;
        Bus bus = salida.getMyBus();
        Puesto[] puestos = bus.getMyPuestos();
        int cols = 4;
        int rows = (int) Math.ceil(puestos.length / (double) cols);
        grid.setLayout(new GridLayout(rows, cols, 4, 4));

        int[] bloque = esIda ? selectedBlockIda : selectedBlockVuelta;
        boolean[] enBloque = new boolean[puestos.length];
        if (bloque != null) {
            for (int p : bloque) {
                if (p >= 1 && p <= puestos.length) enBloque[p - 1] = true;
            }
        }

        for (int i = 0; i < puestos.length; i++) {
            final int numPuesto = i + 1;
            JButton btn = new JButton(String.valueOf(numPuesto));
            btn.setPreferredSize(new Dimension(40, 40));
            btn.setFont(new Font("SansSerif", Font.BOLD, 10));
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            if (puestos[i].estaLibre()) {
                if (enBloque[i]) {
                    btn.setBackground(Colores.AZUL_PRIMARIO);
                    btn.setForeground(Color.WHITE);
                } else {
                    btn.setBackground(Colores.ESTADO_VERDE);
                    btn.setForeground(Colores.ESTADO_VERDE_TX);
                }
                btn.addActionListener(e -> {
                    int cantidad = esIda ? (Integer) cantidadIdaSpinner.getValue() : (Integer) cantidadVueltaSpinner.getValue();
                    boolean puede = true;
                    for (int j = 0; j < cantidad; j++) {
                        int idx = numPuesto - 1 + j;
                        if (idx >= puestos.length || !puestos[idx].estaLibre()) {
                            puede = false; break;
                        }
                    }
                    if (puede) {
                        int[] nuevoBloque = new int[cantidad];
                        for (int j = 0; j < cantidad; j++) nuevoBloque[j] = numPuesto + j;
                        if (esIda) selectedBlockIda = nuevoBloque; else selectedBlockVuelta = nuevoBloque;
                        rebuildSeatGridIyV(grid, salidaId, esIda);
                        actualizarResumenIyV();
                    }
                });
            } else {
                btn.setBackground(Colores.FONDO_SUPERFICIE);
                btn.setForeground(Colores.TEXTO_SECUNDARIO);
                btn.setEnabled(false);
            }
            grid.add(btn);
        }
        grid.revalidate();
        grid.repaint();
    }

    private void generarIdaYVuelta() {
        if (selectedSalidaIdIda == null || selectedSalidaIdVuelta == null || selectedBlockIda == null || selectedBlockVuelta == null) {
            JOptionPane.showMessageDialog(this, "Seleccione salidas de ida y vuelta y bloques de puestos");
            return;
        }
        Salida salidaIda = empresa.getSalidaPorId(selectedSalidaIdIda);
        Salida salidaVuelta = empresa.getSalidaPorId(selectedSalidaIdVuelta);
        if (salidaIda == null || salidaVuelta == null) return;
        if (!salidaIda.getMyRuta().getCodigo().equals(salidaVuelta.getMyRuta().getCodigo())) {
            JOptionPane.showMessageDialog(this, "Las salidas deben ser de la misma ruta");
            return;
        }

        int cantidad = selectedBlockIda.length;
        if (cantidad != selectedBlockVuelta.length) {
            JOptionPane.showMessageDialog(this, "La cantidad de puestos de ida y vuelta debe coincidir");
            return;
        }

        // Solicitar datos de pasajeros mediante un diálogo simple por cada uno
        Pasajero[] pasajeros = new Pasajero[cantidad];
        for (int i = 0; i < cantidad; i++) {
            JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
            JTextField cedulaF = new JTextField();
            JTextField nombreF = new JTextField();
            JTextField correoF = new JTextField();
            JTextField telefonoF = new JTextField();
            form.add(new JLabel("C\u00e9dula:")); form.add(cedulaF);
            form.add(new JLabel("Nombre:")); form.add(nombreF);
            form.add(new JLabel("Correo:")); form.add(correoF);
            form.add(new JLabel("Tel\u00e9fono:")); form.add(telefonoF);
            int opt = JOptionPane.showConfirmDialog(this, form, "Pasajero puesto " + selectedBlockIda[i] + " (ida) / " + selectedBlockVuelta[i] + " (vuelta)", JOptionPane.OK_CANCEL_OPTION);
            if (opt != JOptionPane.OK_OPTION) return;
            if (cedulaF.getText().trim().isEmpty() || nombreF.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "C\u00e9dula y nombre son obligatorios");
                return;
            }
            pasajeros[i] = empresa.buscarOCrearPasajero(cedulaF.getText().trim(), nombreF.getText().trim(), "", correoF.getText().trim(), telefonoF.getText().trim());
        }

        boolean ok = empresa.ventaIdaYVuelta(selectedSalidaIdIda, selectedBlockIda, pasajeros,
                selectedSalidaIdVuelta, selectedBlockVuelta, pasajeros);
        if (ok) {
            float total = 0f;
            for (int i = 0; i < cantidad; i++) {
                float base = salidaIda.getMyRuta().getTarifa();
                if (pasajeros[i].esPreferencial()) base = base * 0.9f;
                total += base * 2f * 0.9f; // ida+vuelta con 10% descuento total
            }
            JOptionPane.showMessageDialog(this, "Tiquetes ida y vuelta generados con \u00e9xito.\nTotal: $" + String.format("%,.0f", total));
            limpiarFormularioIyV();
        } else {
            JOptionPane.showMessageDialog(this, "Error al generar los tiquetes ida y vuelta.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarFormularioIyV() {
        selectedBlockIda = null;
        selectedBlockVuelta = null;
        selectedSalidaIdIda = null;
        selectedSalidaIdVuelta = null;
        cantidadIdaSpinner.setValue(1);
        cantidadVueltaSpinner.setValue(1);
        if (idaCombo.getItemCount() > 0) idaCombo.setSelectedIndex(0);
        if (vueltaCombo.getItemCount() > 0) vueltaCombo.setSelectedIndex(0);
        rebuildSeatGridIyV(sillasGridIda, null, true);
        rebuildSeatGridIyV(sillasGridVuelta, null, false);
        validacionRutaLabel.setText(" ");
        actualizarResumenIyV();
    }

    private void actualizarResumenIyV() {
        Salida salidaIda = selectedSalidaIdIda != null ? empresa.getSalidaPorId(selectedSalidaIdIda) : null;
        Salida salidaVuelta = selectedSalidaIdVuelta != null ? empresa.getSalidaPorId(selectedSalidaIdVuelta) : null;
        if (salidaIda != null) {
            resumenIyVRuta.setText("Ruta: " + salidaIda.getMyRuta().getCodigo() + " — " + salidaIda.getMyRuta().getOrigen() + " \u2192 " + salidaIda.getMyRuta().getDestino());
            resumenIyVIda.setText("Ida: " + salidaIda.getIdSalida() + " (" + salidaIda.getFecha().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")) + ")");
        } else {
            resumenIyVRuta.setText("Ruta: —");
            resumenIyVIda.setText("Ida: —");
        }
        if (salidaVuelta != null) {
            resumenIyVVuelta.setText("Vuelta: " + salidaVuelta.getIdSalida() + " (" + salidaVuelta.getFecha().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")) + ")");
        } else {
            resumenIyVVuelta.setText("Vuelta: —");
        }
        if (selectedBlockIda != null && selectedBlockVuelta != null && salidaIda != null && salidaVuelta != null) {
            StringBuilder sb = new StringBuilder("Puestos ida: ");
            for (int p : selectedBlockIda) sb.append(p).append(" ");
            sb.append("| vuelta: ");
            for (int p : selectedBlockVuelta) sb.append(p).append(" ");
            resumenIyVPuestos.setText(sb.toString());
            float total = 0f;
            for (int i = 0; i < selectedBlockIda.length; i++) {
                float base = salidaIda.getMyRuta().getTarifa();
                total += base * 2f * 0.9f;
            }
            resumenIyVTotal.setText("Total: $" + String.format("%,.0f", total));
        } else {
            resumenIyVPuestos.setText("Puestos: —");
            resumenIyVTotal.setText("Total: $0");
        }
    }

    private JPanel crearLeyenda(Color color, String texto) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        p.setOpaque(false);
        JLabel cuadro = new JLabel("  ");
        cuadro.setOpaque(true);
        cuadro.setBackground(color);
        cuadro.setPreferredSize(new Dimension(14, 14));
        JLabel label = new JLabel(texto);
        label.setFont(new Font("SansSerif", Font.PLAIN, 10));
        label.setForeground(Colores.TEXTO_SECUNDARIO);
        p.add(cuadro);
        p.add(label);
        return p;
    }

    public void refreshData() {
        cargarSalidasEnCombo(salidaComboUnTicket, null);
        cargarSalidasEnCombo(idaCombo, null);
        cargarSalidasEnCombo(vueltaCombo, null);
        limpiarFormularioUnTicket();
        limpiarFormularioIyV();
    }

    // ============================================================
    // FORMULARIO DINÁMICO DE PASAJERO
    // ============================================================

    private static class PasajeroForm extends JPanel {
        final int puesto;
        final JTextField cedulaField;
        final JTextField nombreField;
        final JTextField correoField;
        final JTextField telefonoField;
        final JLabel tipoLabel;

        PasajeroForm(int puesto, EmpresaTransporte empresa, JPanel parent) {
            this.puesto = puesto;
            setLayout(new GridBagLayout());
            setBackground(new Color(248, 248, 248));
            setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Colores.BORDE, 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
            ));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(2, 2, 2, 2);

            gbc.gridx = 0; gbc.gridy = 0;
            add(new JLabel("Puesto " + puesto + " | C\u00e9dula:"), gbc);
            gbc.gridx = 1;
            cedulaField = new JTextField(10);
            add(cedulaField, gbc);

            gbc.gridx = 0; gbc.gridy = 1;
            add(new JLabel("Nombre:"), gbc);
            gbc.gridx = 1;
            nombreField = new JTextField(10);
            add(nombreField, gbc);

            gbc.gridx = 0; gbc.gridy = 2;
            add(new JLabel("Correo:"), gbc);
            gbc.gridx = 1;
            correoField = new JTextField(10);
            add(correoField, gbc);

            gbc.gridx = 0; gbc.gridy = 3;
            add(new JLabel("Tel\u00e9fono:"), gbc);
            gbc.gridx = 1;
            telefonoField = new JTextField(10);
            add(telefonoField, gbc);

            gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
            tipoLabel = new JLabel(" ");
            tipoLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
            add(tipoLabel, gbc);

            cedulaField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    String cedula = cedulaField.getText().trim();
                    if (!cedula.isEmpty()) {
                        Pasajero p = empresa.getPasajeroPorCedula(cedula);
                        if (p != null) {
                            nombreField.setText(p.getNombre());
                            correoField.setText(p.getCorreo());
                            telefonoField.setText(p.getTelefono());
                            nombreField.setEnabled(false);
                            if (p.esPreferencial()) {
                                tipoLabel.setText("CLIENTE PREFERENCIAL — 10% descuento");
                                tipoLabel.setForeground(Colores.ESTADO_VERDE_TX);
                            } else {
                                tipoLabel.setText("Cliente frecuente");
                                tipoLabel.setForeground(Colores.TEXTO_SECUNDARIO);
                            }
                        } else {
                            nombreField.setEnabled(true);
                            tipoLabel.setText("Nuevo cliente");
                            tipoLabel.setForeground(Colores.AZUL_MEDIO);
                        }
                    }
                }
            });
        }
    }

    // Helper vacío para compatibilidad de estructura ida/vuelta (no se usa en la UI actual)
    private static class PassengerRoundTripForm {}
}

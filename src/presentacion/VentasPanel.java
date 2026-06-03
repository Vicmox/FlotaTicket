package presentacion;

import negocio.*;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;
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

        JButton unTicketBtn = crearTabBtn("1 tiquete", true);
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
        btn.setPreferredSize(new Dimension(140, 35));
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
    // PANEL 1 TIQUETE
    // ============================================================

    private JComboBox<String> salidaComboUnTicket;
    private JPanel sillasGridUnTicket;
    private int selectedSeatUnTicket = -1;
    private String selectedSalidaIdUnTicket;
    private JLabel resumenRuta, resumenSalida, resumenBus, resumenSilla, resumenValor, resumenTotal;
    private JComboBox<String> tipoDocCombo;
    private JTextField numeroDocField, nombreField, telefonoField;
    private JButton generarUnTicketBtn;

    private JPanel crearPanelUnTicket() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);

        leftPanel.add(crearPasoSeleccionSalida());
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(crearPanelSillas());
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(crearPanelDatosPasajero());

        JPanel rightPanel = crearPanelResumen();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        split.setDividerLocation(480);
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
        panel.add(new JLabel("Salida:"), gbc);
        gbc.gridx = 1;
        salidaComboUnTicket = new JComboBox<>();
        cargarSalidasEnCombo(salidaComboUnTicket);
        salidaComboUnTicket.addActionListener(e -> onSalidaUnTicketChanged());
        panel.add(salidaComboUnTicket, gbc);

        return panel;
    }

    private void onSalidaUnTicketChanged() {
        String sel = (String) salidaComboUnTicket.getSelectedItem();
        if (sel == null) return;
        String id = sel.split(" —")[0].trim();
        selectedSalidaIdUnTicket = id;
        selectedSeatUnTicket = -1;
        rebuildSeatGridUnTicket();
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
        for (int i = 0; i < puestos.length; i++) {
            final int numPuesto = i + 1;
            JButton btn = new JButton(String.valueOf(numPuesto));
            btn.setPreferredSize(new Dimension(40, 40));
            btn.setFont(new Font("SansSerif", Font.BOLD, 10));
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            if (puestos[i].estaLibre()) {
                btn.setBackground(Colores.ESTADO_VERDE);
                btn.setForeground(Colores.ESTADO_VERDE_TX);
                btn.addActionListener(e -> seleccionarSillaUnTicket(numPuesto, sillasGridUnTicket));
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

    private void seleccionarSillaUnTicket(int numPuesto, JPanel grid) {
        selectedSeatUnTicket = numPuesto;
        for (Component c : grid.getComponents()) {
            if (c instanceof JButton) {
                JButton btn = (JButton) c;
                int n = Integer.parseInt(btn.getText());
                if (n == numPuesto) {
                    btn.setBackground(Colores.AZUL_PRIMARIO);
                    btn.setForeground(Color.WHITE);
                } else if (btn.isEnabled()) {
                    btn.setBackground(Colores.ESTADO_VERDE);
                    btn.setForeground(Colores.ESTADO_VERDE_TX);
                }
            }
        }
        actualizarResumenUnTicket();
    }

    private JPanel crearPanelSillas() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Colores.FONDO_TARJETA);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Colores.BORDE, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        JLabel titulo = new JLabel("Paso 2: Seleccionar silla");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 12));
        panel.add(titulo, BorderLayout.NORTH);

        sillasGridUnTicket = new JPanel();
        sillasGridUnTicket.setOpaque(false);
        rebuildSeatGridUnTicket();
        panel.add(sillasGridUnTicket, BorderLayout.CENTER);

        JPanel leyenda = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        leyenda.setOpaque(false);
        leyenda.add(crearLeyenda(Colores.ESTADO_VERDE, "Disponible"));
        leyenda.add(crearLeyenda(Colores.AZUL_PRIMARIO, "Seleccionada"));
        leyenda.add(crearLeyenda(Colores.FONDO_SUPERFICIE, "Ocupada"));
        panel.add(leyenda, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel crearPanelDatosPasajero() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Colores.FONDO_TARJETA);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Colores.BORDE, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        JLabel titulo = new JLabel("Paso 3: Datos del pasajero");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 12));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(titulo, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Tipo doc.:"), gbc);
        gbc.gridx = 1;
        tipoDocCombo = new JComboBox<>(new String[]{"CC", "CE", "Pasaporte"});
        panel.add(tipoDocCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("N\u00famero:"), gbc);
        gbc.gridx = 1;
        numeroDocField = new JTextField(10);
        panel.add(numeroDocField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1;
        nombreField = new JTextField(10);
        panel.add(nombreField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Tel\u00e9fono:"), gbc);
        gbc.gridx = 1;
        telefonoField = new JTextField(10);
        panel.add(telefonoField, gbc);

        return panel;
    }

    private void generarUnTicket() {
        if (selectedSalidaIdUnTicket == null || selectedSeatUnTicket < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una salida y una silla");
            return;
        }
        String numDoc = numeroDocField.getText().trim();
        String nombre = nombreField.getText().trim();
        String telefono = telefonoField.getText().trim();
        if (numDoc.isEmpty() || nombre.isEmpty() || telefono.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Complete todos los datos del pasajero");
            return;
        }
        String tipoDoc = (String) tipoDocCombo.getSelectedItem();
        String idPasajero = tipoDoc + "-" + numDoc;
        Pasajero pasajero = new Pasajero(numDoc, nombre, telefono, idPasajero);
        PasajeTicket ticket = empresa.generarTicket(selectedSalidaIdUnTicket, selectedSeatUnTicket, pasajero);
        if (ticket != null) {
            JOptionPane.showMessageDialog(this, "Tiquete generado exitosamente\n" +
                "Salida: " + selectedSalidaIdUnTicket + "\n" +
                "Silla: " + selectedSeatUnTicket + "\n" +
                "Valor: $" + String.format("%,.0f", ticket.getValorPagar()));
            limpiarFormularioUnTicket();
        } else {
            JOptionPane.showMessageDialog(this, "Error al generar el tiquete. Verifique los datos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarFormularioUnTicket() {
        selectedSeatUnTicket = -1;
        numeroDocField.setText("");
        nombreField.setText("");
        telefonoField.setText("");
        selectedSalidaIdUnTicket = null;
        if (salidaComboUnTicket.getItemCount() > 0) salidaComboUnTicket.setSelectedIndex(0);
        rebuildSeatGridUnTicket();
        actualizarResumenUnTicket();
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
        resumenSilla = new JLabel("Silla: —");
        panel.add(resumenRuta);
        panel.add(resumenSalida);
        panel.add(resumenBus);
        panel.add(resumenSilla);
        panel.add(Box.createVerticalStrut(15));
        panel.add(new JSeparator());
        panel.add(Box.createVerticalStrut(10));
        resumenValor = new JLabel("Valor tiquete: $0");
        panel.add(resumenValor);
        panel.add(new JLabel("Cargo servicio: $2.500"));

        resumenTotal = new JLabel("Total: $0");
        resumenTotal.setFont(new Font("SansSerif", Font.BOLD, 15));
        resumenTotal.setForeground(Colores.TEXTO_PRIMARIO);
        panel.add(Box.createVerticalStrut(5));
        panel.add(resumenTotal);

        panel.add(Box.createVerticalStrut(15));
        panel.add(new JLabel("Forma de pago:"));
        panel.add(new JComboBox<>(new String[]{"Efectivo", "Tarjeta d\u00e9bito", "Tarjeta cr\u00e9dito"}));
        panel.add(Box.createVerticalStrut(15));

        generarUnTicketBtn = new JButton("Generar tiquete");
        generarUnTicketBtn.setBackground(Colores.AZUL_PRIMARIO);
        generarUnTicketBtn.setForeground(Color.WHITE);
        generarUnTicketBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        generarUnTicketBtn.setFocusPainted(false);
        generarUnTicketBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        generarUnTicketBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        generarUnTicketBtn.addActionListener(e -> generarUnTicket());
        panel.add(generarUnTicketBtn);

        return panel;
    }

    private void actualizarResumenUnTicket() {
        if (selectedSalidaIdUnTicket == null) {
            resumenRuta.setText("Ruta: —");
            resumenSalida.setText("Salida: —");
            resumenBus.setText("Bus: —");
            resumenSilla.setText("Silla: —");
            resumenValor.setText("Valor tiquete: $0");
            resumenTotal.setText("Total: $0");
            return;
        }
        Salida salida = empresa.getSalidaPorId(selectedSalidaIdUnTicket);
        if (salida == null) return;
        resumenRuta.setText("Ruta: " + salida.getMyRuta().getCodigo() + " — " + salida.getMyRuta().getOrigen() + " \u2192 " + salida.getMyRuta().getDestino());
        resumenSalida.setText("Salida: " + salida.getIdSalida() + " (" + salida.getFecha().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")) + ")");
        resumenBus.setText("Bus: " + salida.getMyBus().getPlaca() + " (" + (salida.getMyBus() instanceof BusTipoEjecutivo ? "Ejecutivo" : "Normal") + ")");
        if (selectedSeatUnTicket > 0) {
            resumenSilla.setText("Silla: " + selectedSeatUnTicket);
        } else {
            resumenSilla.setText("Silla: —");
        }
        float valor = salida.getMyRuta().getTarifa();
        resumenValor.setText("Valor tiquete: $" + String.format("%,.0f", valor));
        resumenTotal.setText("Total: $" + String.format("%,.0f", valor + 2500));
    }

    // ============================================================
    // PANEL IDA Y VUELTA
    // ============================================================

    private JComboBox<String> idaCombo, vueltaCombo;
    private JPanel sillasGridIda, sillasGridVuelta;
    private int selectedSeatIda = -1, selectedSeatVuelta = -1;
    private String selectedSalidaIdIda, selectedSalidaIdVuelta;
    private JLabel resumenIyVRuta, resumenIyVIda, resumenIyVVuelta, resumenIyVSillaIda, resumenIyVSillaVuelta, resumenIyVValor, resumenIyVTotal;
    private JTextField numeroDocIyVField, nombreIyVField, telefonoIyVField;
    private JComboBox<String> tipoDocIyVCombo;
    private JButton generarIyVBtn;

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
        leftPanel.add(crearPanelDatosPasajeroIyV());
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(crearPanelGenerarIyV());

        JPanel resumenPanel = crearPanelResumenIyV();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, resumenPanel);
        split.setDividerLocation(480);
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
        cargarSalidasEnCombo(idaCombo);
        idaCombo.addActionListener(e -> onIdaChanged());
        panel.add(idaCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Vuelta:"), gbc);
        gbc.gridx = 1;
        vueltaCombo = new JComboBox<>();
        cargarSalidasEnCombo(vueltaCombo);
        vueltaCombo.addActionListener(e -> onVueltaChanged());
        panel.add(vueltaCombo, gbc);

        return panel;
    }

    private void cargarSalidasEnCombo(JComboBox<String> combo) {
        combo.removeAllItems();
        for (Salida s : empresa.listarSalidas()) {
            if (Salida.PROGRAMADA.equals(s.getEstado())) {
                combo.addItem(s.getIdSalida() + " — " + s.getMyRuta().getOrigen() + " \u2192 " + s.getMyRuta().getDestino()
                    + " (" + s.getFecha().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")) + ")");
            }
        }
    }

    private void onIdaChanged() {
        String sel = (String) idaCombo.getSelectedItem();
        if (sel == null || sel.startsWith("Seleccionar")) return;
        selectedSalidaIdIda = sel.split(" —")[0].trim();
        selectedSeatIda = -1;
        rebuildSeatGrid(idaCombo, sillasGridIda, true);
        actualizarResumenIyV();
    }

    private void onVueltaChanged() {
        String sel = (String) vueltaCombo.getSelectedItem();
        if (sel == null || sel.startsWith("Seleccionar")) return;
        selectedSalidaIdVuelta = sel.split(" —")[0].trim();
        selectedSeatVuelta = -1;
        rebuildSeatGrid(vueltaCombo, sillasGridVuelta, false);
        actualizarResumenIyV();
    }

    private void rebuildSeatGrid(JComboBox<String> combo, JPanel grid, boolean esIda) {
        grid.removeAll();
        String sel = (String) combo.getSelectedItem();
        if (sel == null || sel.startsWith("Seleccionar")) {
            grid.revalidate();
            grid.repaint();
            return;
        }
        String id = sel.split(" —")[0].trim();
        Salida salida = empresa.getSalidaPorId(id);
        if (salida == null) return;
        Puesto[] puestos = salida.getMyBus().getMyPuestos();
        int cols = 4;
        int rows = (int) Math.ceil(puestos.length / (double) cols);
        grid.setLayout(new GridLayout(rows, cols, 4, 4));
        for (int i = 0; i < puestos.length; i++) {
            final int numPuesto = i + 1;
            JButton btn = new JButton(String.valueOf(numPuesto));
            btn.setPreferredSize(new Dimension(40, 40));
            btn.setFont(new Font("SansSerif", Font.BOLD, 10));
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            if (puestos[i].estaLibre()) {
                btn.setBackground(Colores.ESTADO_VERDE);
                btn.setForeground(Colores.ESTADO_VERDE_TX);
                btn.addActionListener(e -> seleccionarSillaIyV(numPuesto, esIda, grid));
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

    private void seleccionarSillaIyV(int numPuesto, boolean esIda, JPanel grid) {
        if (esIda) {
            selectedSeatIda = numPuesto;
        } else {
            selectedSeatVuelta = numPuesto;
        }
        for (Component c : grid.getComponents()) {
            if (c instanceof JButton) {
                JButton btn = (JButton) c;
                int n = Integer.parseInt(btn.getText());
                if (n == numPuesto) {
                    btn.setBackground(Colores.AZUL_PRIMARIO);
                    btn.setForeground(Color.WHITE);
                } else if (btn.isEnabled()) {
                    btn.setBackground(Colores.ESTADO_VERDE);
                    btn.setForeground(Colores.ESTADO_VERDE_TX);
                }
            }
        }
        actualizarResumenIyV();
    }

    private JPanel crearPanelSillasIdaVuelta() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 0));
        panel.setOpaque(false);

        JPanel idaPanel = new JPanel(new BorderLayout());
        idaPanel.setBackground(Colores.FONDO_TARJETA);
        idaPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Colores.BORDE, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        idaPanel.add(new JLabel("Sillas — Ida", SwingConstants.CENTER), BorderLayout.NORTH);
        sillasGridIda = new JPanel();
        sillasGridIda.setOpaque(false);
        idaPanel.add(sillasGridIda, BorderLayout.CENTER);

        JPanel vueltaPanel = new JPanel(new BorderLayout());
        vueltaPanel.setBackground(Colores.FONDO_TARJETA);
        vueltaPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Colores.BORDE, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        vueltaPanel.add(new JLabel("Sillas — Vuelta", SwingConstants.CENTER), BorderLayout.NORTH);
        sillasGridVuelta = new JPanel();
        sillasGridVuelta.setOpaque(false);
        vueltaPanel.add(sillasGridVuelta, BorderLayout.CENTER);

        panel.add(idaPanel);
        panel.add(vueltaPanel);

        return panel;
    }

    private JPanel crearPanelDatosPasajeroIyV() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Colores.FONDO_TARJETA);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Colores.BORDE, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        JLabel titulo = new JLabel("Datos del pasajero");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 12));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(titulo, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Tipo doc.:"), gbc);
        gbc.gridx = 1;
        tipoDocIyVCombo = new JComboBox<>(new String[]{"CC", "CE", "Pasaporte"});
        panel.add(tipoDocIyVCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("N\u00famero:"), gbc);
        gbc.gridx = 1;
        numeroDocIyVField = new JTextField(10);
        panel.add(numeroDocIyVField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1;
        nombreIyVField = new JTextField(10);
        panel.add(nombreIyVField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Tel\u00e9fono:"), gbc);
        gbc.gridx = 1;
        telefonoIyVField = new JTextField(10);
        panel.add(telefonoIyVField, gbc);

        return panel;
    }

    private JPanel crearPanelGenerarIyV() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setOpaque(false);
        generarIyVBtn = new JButton("Generar 2 tiquetes");
        generarIyVBtn.setBackground(Colores.AZUL_PRIMARIO);
        generarIyVBtn.setForeground(Color.WHITE);
        generarIyVBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        generarIyVBtn.setFocusPainted(false);
        generarIyVBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        generarIyVBtn.addActionListener(e -> generarIdaYVuelta());
        panel.add(generarIyVBtn);
        return panel;
    }

    private void generarIdaYVuelta() {
        if (selectedSalidaIdIda == null || selectedSalidaIdVuelta == null || selectedSeatIda < 0 || selectedSeatVuelta < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione salidas de ida y vuelta y sus sillas");
            return;
        }
        Salida salidaIda = empresa.getSalidaPorId(selectedSalidaIdIda);
        Salida salidaVuelta = empresa.getSalidaPorId(selectedSalidaIdVuelta);
        if (salidaIda == null || salidaVuelta == null) return;
        if (!salidaIda.getMyRuta().getCodigo().equals(salidaVuelta.getMyRuta().getCodigo())) {
            JOptionPane.showMessageDialog(this, "Las salidas de ida y vuelta deben ser de la misma ruta");
            return;
        }
        String numDoc = numeroDocIyVField.getText().trim();
        String nombre = nombreIyVField.getText().trim();
        String telefono = telefonoIyVField.getText().trim();
        if (numDoc.isEmpty() || nombre.isEmpty() || telefono.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Complete todos los datos del pasajero");
            return;
        }
        String tipoDoc = (String) tipoDocIyVCombo.getSelectedItem();
        String idPasajero = tipoDoc + "-" + numDoc;
        Pasajero pasajero = new Pasajero(numDoc, nombre, telefono, idPasajero);

        boolean ok = empresa.ventaIdaYVuelta(selectedSalidaIdIda, selectedSeatIda, pasajero,
            selectedSalidaIdVuelta, selectedSeatVuelta, pasajero);
        if (ok) {
            float total = (salidaIda.getMyRuta().getTarifa() + salidaVuelta.getMyRuta().getTarifa()) * 0.9f;
            JOptionPane.showMessageDialog(this, "2 tiquetes generados con descuento del 10%\nTotal: $" + String.format("%,.0f", total));
            limpiarFormularioIyV();
        } else {
            JOptionPane.showMessageDialog(this, "Error al generar los tiquetes", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarFormularioIyV() {
        selectedSeatIda = -1;
        selectedSeatVuelta = -1;
        selectedSalidaIdIda = null;
        selectedSalidaIdVuelta = null;
        numeroDocIyVField.setText("");
        nombreIyVField.setText("");
        telefonoIyVField.setText("");
        if (idaCombo.getItemCount() > 0) idaCombo.setSelectedIndex(0);
        if (vueltaCombo.getItemCount() > 0) vueltaCombo.setSelectedIndex(0);
        rebuildSeatGrid(idaCombo, sillasGridIda, true);
        rebuildSeatGrid(vueltaCombo, sillasGridVuelta, false);
        actualizarResumenIyV();
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
        resumenIyVSillaIda = new JLabel("Silla ida: —");
        resumenIyVSillaVuelta = new JLabel("Silla vuelta: —");
        panel.add(resumenIyVRuta);
        panel.add(resumenIyVIda);
        panel.add(resumenIyVVuelta);
        panel.add(resumenIyVSillaIda);
        panel.add(resumenIyVSillaVuelta);
        panel.add(Box.createVerticalStrut(5));
        panel.add(new JSeparator());
        panel.add(Box.createVerticalStrut(5));
        resumenIyVValor = new JLabel("Descuento 10%: aplicado");
        panel.add(resumenIyVValor);
        resumenIyVTotal = new JLabel("Total: $0");
        resumenIyVTotal.setFont(new Font("SansSerif", Font.BOLD, 15));
        resumenIyVTotal.setForeground(Colores.TEXTO_PRIMARIO);
        panel.add(resumenIyVTotal);

        return panel;
    }

    private void actualizarResumenIyV() {
        Salida salidaIda = selectedSalidaIdIda != null ? empresa.getSalidaPorId(selectedSalidaIdIda) : null;
        Salida salidaVuelta = selectedSalidaIdVuelta != null ? empresa.getSalidaPorId(selectedSalidaIdVuelta) : null;

        if (salidaIda != null) {
            resumenIyVRuta.setText("Ruta: " + salidaIda.getMyRuta().getCodigo() + " — " + salidaIda.getMyRuta().getOrigen() + " \u2192 " + salidaIda.getMyRuta().getDestino());
            resumenIyVIda.setText("Ida: " + salidaIda.getIdSalida() + " (" + salidaIda.getFecha().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")) + ")");
            resumenIyVSillaIda.setText("Silla ida: " + (selectedSeatIda > 0 ? String.valueOf(selectedSeatIda) : "—"));

            float valorIda = salidaIda.getMyRuta().getTarifa();
            if (salidaVuelta != null) {
                resumenIyVVuelta.setText("Vuelta: " + salidaVuelta.getIdSalida() + " (" + salidaVuelta.getFecha().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")) + ")");
                resumenIyVSillaVuelta.setText("Silla vuelta: " + (selectedSeatVuelta > 0 ? String.valueOf(selectedSeatVuelta) : "—"));
                float valorVuelta = salidaVuelta.getMyRuta().getTarifa();
                float totalSinDesc = valorIda + valorVuelta;
                float totalConDesc = totalSinDesc * 0.9f;
                resumenIyVValor.setText("Descuento 10%: $" + String.format("%,.0f", totalSinDesc) + " \u2192 $" + String.format("%,.0f", totalConDesc));
                resumenIyVTotal.setText("Total: $" + String.format("%,.0f", totalConDesc));
            }
        } else {
            resumenIyVRuta.setText("Ruta: —");
            resumenIyVIda.setText("Ida: —");
            resumenIyVVuelta.setText("Vuelta: —");
            resumenIyVSillaIda.setText("Silla ida: —");
            resumenIyVSillaVuelta.setText("Silla vuelta: —");
            resumenIyVValor.setText("Descuento 10%: aplicado");
            resumenIyVTotal.setText("Total: $0");
        }
    }

    public void refreshData() {
        cargarSalidasEnCombo(salidaComboUnTicket);
        cargarSalidasEnCombo(idaCombo);
        cargarSalidasEnCombo(vueltaCombo);

        selectedSalidaIdUnTicket = null;
        selectedSalidaIdIda = null;
        selectedSalidaIdVuelta = null;
        selectedSeatUnTicket = -1;
        selectedSeatIda = -1;
        selectedSeatVuelta = -1;
        selectedSeatUnTicket = -1;

        rebuildSeatGridUnTicket();
        rebuildSeatGrid(idaCombo, sillasGridIda, true);
        rebuildSeatGrid(vueltaCombo, sillasGridVuelta, false);
        actualizarResumenUnTicket();
        actualizarResumenIyV();
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
}

package presentacion;

import negocio.*;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ParametrizacionPanel extends JPanel {

    private final EmpresaTransporte empresa;
    private String editingRutaCodigo;
    private String editingConductorCedula;

    // Referencias para refrescar combos de Salidas desde Rutas/Buses
    private JComboBox<String> rutaComboSalidas;
    private JComboBox<String> busComboSalidas;

    public ParametrizacionPanel(EmpresaTransporte empresa) {
        this.empresa = empresa;
        setLayout(new BorderLayout());
        setBackground(Colores.FONDO_GENERAL);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tabs.addTab("Rutas", crearPanelRutas());
        tabs.addTab("Buses", crearPanelBuses());
        tabs.addTab("Salidas", crearPanelSalidas());
        tabs.addTab("Conductores", crearPanelConductores());

        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 2) { // pestaña Salidas
                refrescarCombosSalidas();
            }
        });

        add(tabs, BorderLayout.CENTER);
    }

    private void refrescarCombosSalidas() {
        if (rutaComboSalidas == null || busComboSalidas == null) return;
        rutaComboSalidas.removeAllItems();
        for (Ruta r : empresa.listarRutas()) {
            rutaComboSalidas.addItem(r.getCodigo() + " — " + r.getOrigen() + " \u2192 " + r.getDestino());
        }
        busComboSalidas.removeAllItems();
        for (Bus b : empresa.listarBuses()) {
            if (Bus.DISPONIBLE.equals(b.getEstado())) {
                busComboSalidas.addItem(b.getPlaca() + " (Cap: " + b.getCapacidad() + ")");
            }
        }
    }

    // ============================================================
    // TAB RUTAS
    // ============================================================

    private JPanel crearPanelRutas() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(Colores.FONDO_GENERAL);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Colores.FONDO_TARJETA);
        form.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Colores.BORDE, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        JLabel codigoLabel = new JLabel("(Auto)");
        JTextField origenField = new JTextField("C\u00facuta");
        origenField.setEditable(false);
        JTextField destinoField = new JTextField(10);
        JFormattedTextField tarifaField = new JFormattedTextField(NumberFormat.getNumberInstance());
        tarifaField.setColumns(10);

        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("C\u00f3digo:"), gbc);
        gbc.gridx = 1;
        form.add(codigoLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        form.add(new JLabel("Origen:"), gbc);
        gbc.gridx = 1;
        form.add(origenField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        form.add(new JLabel("Destino:"), gbc);
        gbc.gridx = 1;
        form.add(destinoField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        form.add(new JLabel("Tarifa:"), gbc);
        gbc.gridx = 1;
        form.add(tarifaField, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.setOpaque(false);

        JButton limpiarBtn = new JButton("Limpiar");
        JButton guardarBtn = new JButton("Guardar ruta");
        guardarBtn.setBackground(Colores.AZUL_PRIMARIO);
        guardarBtn.setForeground(Color.WHITE);
        btnPanel.add(limpiarBtn);
        btnPanel.add(guardarBtn);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        form.add(btnPanel, gbc);

        DefaultTableModel model = new DefaultTableModel(new String[]{"C\u00f3digo", "Origen \u2192 Destino", "Tarifa", "Acciones"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return col == 3; }
        };

        guardarBtn.addActionListener(e -> {
            String dest = destinoField.getText().trim();
            float tarifa;
            try {
                tarifa = Float.parseFloat(tarifaField.getText().replaceAll("[.,]", ""));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Tarifa inv\u00e1lida");
                return;
            }
            if (dest.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Destino es obligatorio");
                return;
            }
            if (editingRutaCodigo != null) {
                empresa.editarRuta(editingRutaCodigo, "C\u00facuta", dest, tarifa);
                editingRutaCodigo = null;
                codigoLabel.setText("(Auto)");
            } else {
                // Código autogenerado
                empresa.crearRuta(null, "C\u00facuta", dest, tarifa);
            }
            actualizarTablaRutas(model);
            limpiarCampos(destinoField, tarifaField);
            refrescarCombosSalidas();
        });

        limpiarBtn.addActionListener(e -> {
            limpiarCampos(destinoField, tarifaField);
            codigoLabel.setText("(Auto)");
            editingRutaCodigo = null;
        });

        JTable table = new JTable(model) {
            @Override
            public Class<?> getColumnClass(int col) {
                if (col == 3) return JButton.class;
                return super.getColumnClass(col);
            }
        };
        table.setRowHeight(28);

        actualizarTablaRutas(model);

        table.getColumn("Acciones").setCellRenderer(new ButtonRenderer());
        table.getColumn("Acciones").setCellEditor(new ButtonEditor(new JButton("Editar"), e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                editingRutaCodigo = model.getValueAt(row, 0).toString();
                codigoLabel.setText(editingRutaCodigo);
                String rutaStr = model.getValueAt(row, 1).toString();
                String[] parts = rutaStr.split(" \u2192 ");
                if (parts.length == 2) {
                    destinoField.setText(parts[1]);
                }
                tarifaField.setText(model.getValueAt(row, 2).toString().replaceAll("[^0-9]", ""));
            }
        }));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, form, new JScrollPane(table));
        split.setDividerLocation(300);
        panel.add(split, BorderLayout.CENTER);

        return panel;
    }

    private void actualizarTablaRutas(DefaultTableModel model) {
        model.setRowCount(0);
        for (Ruta r : empresa.listarRutas()) {
            model.addRow(new Object[]{
                r.getCodigo(),
                r.getOrigen() + " \u2192 " + r.getDestino(),
                String.format("$%,.0f", r.getTarifa()),
                "Editar"
            });
        }
    }

    // ============================================================
    // TAB BUSES
    // ============================================================

    private int getCapacidadPorTipo(String tipo) {
        return "EJECUTIVO".equalsIgnoreCase(tipo) ? 40 : 30;
    }

    private JPanel crearPanelBuses() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(Colores.FONDO_GENERAL);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Colores.FONDO_TARJETA);
        form.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Colores.BORDE, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        JTextField placaField = new JTextField(10);
        JComboBox<String> tipoCombo = new JComboBox<>(new String[]{"NORMAL", "EJECUTIVO"});
        JLabel capLabel = new JLabel("30 (Normal)");
        capLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        capLabel.setForeground(Colores.TEXTO_SECUNDARIO);

        tipoCombo.addActionListener(e -> {
            String tipo = (String) tipoCombo.getSelectedItem();
            capLabel.setText(getCapacidadPorTipo(tipo) + " (" + (tipo.equals("EJECUTIVO") ? "Ejecutivo" : "Normal") + ")");
        });

        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("Placa:"), gbc);
        gbc.gridx = 1;
        form.add(placaField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        form.add(new JLabel("Tipo:"), gbc);
        gbc.gridx = 1;
        form.add(tipoCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        form.add(new JLabel("Capacidad:"), gbc);
        gbc.gridx = 1;
        form.add(capLabel, gbc);

        JLabel infoEstado = new JLabel("Estado inicial: DISPONIBLE");
        infoEstado.setFont(new Font("SansSerif", Font.PLAIN, 11));
        infoEstado.setForeground(Colores.ESTADO_VERDE_TX);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        form.add(infoEstado, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.setOpaque(false);
        JButton limpiarBtn = new JButton("Limpiar");
        JButton guardarBtn = new JButton("Guardar bus");
        guardarBtn.setBackground(Colores.AZUL_PRIMARIO);
        guardarBtn.setForeground(Color.WHITE);
        btnPanel.add(limpiarBtn);
        btnPanel.add(guardarBtn);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        form.add(btnPanel, gbc);

        JPanel listaPanel = new JPanel();
        listaPanel.setLayout(new BoxLayout(listaPanel, BoxLayout.Y_AXIS));
        listaPanel.setBackground(Colores.FONDO_GENERAL);

        guardarBtn.addActionListener(e -> {
            String placa = placaField.getText().trim().toUpperCase();
            if (placa.isEmpty()) { JOptionPane.showMessageDialog(this, "Placa obligatoria"); return; }
            String tipo = (String) tipoCombo.getSelectedItem();
            int capacidad = getCapacidadPorTipo(tipo);
            empresa.crearBus(placa, Bus.DISPONIBLE, tipo, capacidad);
            actualizarListaBuses(listaPanel);
            limpiarCampos(placaField);
            refrescarCombosSalidas();
        });

        limpiarBtn.addActionListener(e -> limpiarCampos(placaField));
        actualizarListaBuses(listaPanel);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, form, new JScrollPane(listaPanel));
        split.setDividerLocation(300);
        panel.add(split, BorderLayout.CENTER);

        return panel;
    }

    private void actualizarListaBuses(JPanel listaPanel) {
        listaPanel.removeAll();
        for (Bus b : empresa.listarBuses()) {
            JPanel card = new JPanel(new BorderLayout(10, 0));
            card.setBackground(Colores.FONDO_TARJETA);
            card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Colores.BORDE, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

            JLabel icono = new JLabel("\uD83D\uDE8C", SwingConstants.CENTER);
            icono.setPreferredSize(new Dimension(40, 40));
            icono.setBackground(Colores.AZUL_CLARO);
            icono.setOpaque(true);

            JPanel info = new JPanel();
            info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
            info.setOpaque(false);
            JLabel placaLabel = new JLabel(b.getPlaca());
            placaLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
            String tipo = b instanceof BusTipoEjecutivo ? "Ejecutivo" : "Normal";
            JLabel detalle = new JLabel(tipo + " | Cap: " + b.getCapacidad());
            detalle.setFont(new Font("SansSerif", Font.PLAIN, 11));
            detalle.setForeground(Colores.TEXTO_SECUNDARIO);
            info.add(placaLabel);
            info.add(detalle);

            // Badge de estado ahora es un JComboBox editable
            JComboBox<String> estadoCombo = new JComboBox<>(new String[]{Bus.DISPONIBLE, Bus.EN_RUTA, Bus.MANTENIMIENTO});
            estadoCombo.setSelectedItem(b.getEstado());
            estadoCombo.setFont(new Font("SansSerif", Font.PLAIN, 11));
            estadoCombo.setMaximumSize(new Dimension(140, 28));
            // Color de fondo según estado
            actualizarColorEstadoCombo(estadoCombo);
            estadoCombo.addActionListener(ev -> {
                String nuevoEstado = (String) estadoCombo.getSelectedItem();
                empresa.editarBus(b.getPlaca(), nuevoEstado);
                actualizarColorEstadoCombo(estadoCombo);
                refrescarCombosSalidas();
            });

            card.add(icono, BorderLayout.WEST);
            card.add(info, BorderLayout.CENTER);
            JPanel eastWrapper = new JPanel(new BorderLayout());
            eastWrapper.setOpaque(false);
            eastWrapper.add(estadoCombo, BorderLayout.CENTER);
            card.add(eastWrapper, BorderLayout.EAST);

            listaPanel.add(card);
            listaPanel.add(Box.createVerticalStrut(5));
        }
        listaPanel.revalidate();
        listaPanel.repaint();
    }

    private void actualizarColorEstadoCombo(JComboBox<String> combo) {
        String estado = (String) combo.getSelectedItem();
        if (Bus.DISPONIBLE.equals(estado)) {
            combo.setBackground(Colores.ESTADO_VERDE);
            combo.setForeground(Colores.ESTADO_VERDE_TX);
        } else if (Bus.MANTENIMIENTO.equals(estado)) {
            combo.setBackground(Colores.ESTADO_AMBAR);
            combo.setForeground(Colores.ESTADO_AMBAR_TX);
        } else {
            combo.setBackground(Colores.FONDO_SUPERFICIE);
            combo.setForeground(Colores.TEXTO_SECUNDARIO);
        }
    }

    // ============================================================
    // TAB SALIDAS
    // ============================================================

    private JPanel crearPanelSalidas() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Colores.FONDO_GENERAL);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Colores.FONDO_TARJETA);
        form.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Colores.BORDE, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        DefaultTableModel model = new DefaultTableModel(new String[]{"ID Salida", "Ruta", "Fecha", "Hora", "Bus", "Estado", "Pasajes vendidos", "Acciones"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return col == 5 || col == 7; }
            @Override
            public Class<?> getColumnClass(int col) {
                if (col == 5) return String.class;
                if (col == 7) return JButton.class;
                return super.getColumnClass(col);
            }
        };

        rutaComboSalidas = new JComboBox<>();
        busComboSalidas = new JComboBox<>();
        cargarItemsCombosSalidas();

        SpinnerDateModel dateModel = new SpinnerDateModel();
        JSpinner fechaSpinner = new JSpinner(dateModel);
        fechaSpinner.setEditor(new JSpinner.DateEditor(fechaSpinner, "dd/MM/yyyy"));

        SpinnerDateModel timeModel = new SpinnerDateModel();
        JSpinner horaSpinner = new JSpinner(timeModel);
        horaSpinner.setEditor(new JSpinner.DateEditor(horaSpinner, "HH:mm"));

        JTextField precioField = new JTextField(10);
        precioField.setEditable(false);

        rutaComboSalidas.addActionListener(e -> {
            String selected = (String) rutaComboSalidas.getSelectedItem();
            if (selected != null) {
                String cod = selected.split(" —")[0].trim();
                Ruta r = empresa.getRutaPorCodigo(cod);
                if (r != null) precioField.setText(String.format("$%,.0f", r.getTarifa()));
            }
        });

        JButton limpiarBtn = new JButton("Limpiar");
        JButton programarBtn = new JButton("Programar salida");
        programarBtn.setBackground(Colores.AZUL_PRIMARIO);
        programarBtn.setForeground(Color.WHITE);

        JButton finalizarBtn = new JButton("Estado Finalizado Salida");
        finalizarBtn.setBackground(Colores.ESTADO_AMBAR);
        finalizarBtn.setForeground(Colores.ESTADO_AMBAR_TX);
        finalizarBtn.setToolTipText("Cambia estado a FINALIZADA solo si la salida tiene m\u00ednimo 2 d\u00edas de antig\u00fcedad");

        programarBtn.addActionListener(e -> {
            String rutaSel = (String) rutaComboSalidas.getSelectedItem();
            String busSel = (String) busComboSalidas.getSelectedItem();
            if (rutaSel == null || busSel == null) return;
            String codRuta = rutaSel.split(" —")[0].trim();
            String placa = busSel.split(" \\(")[0].trim();

            java.util.Date fechaDate = (java.util.Date) fechaSpinner.getValue();
            java.util.Date horaDate = (java.util.Date) horaSpinner.getValue();
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(fechaDate);
            int dia = cal.get(java.util.Calendar.DAY_OF_MONTH);
            int mes = cal.get(java.util.Calendar.MONTH) + 1;
            int anio = cal.get(java.util.Calendar.YEAR);
            cal.setTime(horaDate);
            int hora = cal.get(java.util.Calendar.HOUR_OF_DAY);
            int min = cal.get(java.util.Calendar.MINUTE);

            LocalDateTime fechaHora = LocalDateTime.of(anio, mes, dia, hora, min);
            empresa.crearSalida(codRuta, placa, fechaHora);
            JOptionPane.showMessageDialog(panel, "Salida programada exitosamente");
            actualizarTablaSalidas(empresa.listarSalidas(), model);
        });

        finalizarBtn.addActionListener(e -> {
            String id = JOptionPane.showInputDialog(panel, "Ingrese el ID de la salida a finalizar:");
            if (id == null || id.trim().isEmpty()) return;
            boolean ok = empresa.finalizarSalida(id.trim());
            if (ok) {
                JOptionPane.showMessageDialog(panel, "Salida finalizada exitosamente.");
            } else {
                JOptionPane.showMessageDialog(panel, "No se puede finalizar la salida. Verifique que est\u00e9 en estado PROGRAMADA o EN_RUTA y que tenga m\u00ednimo 2 d\u00edas de antig\u00fcedad.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            actualizarTablaSalidas(empresa.listarSalidas(), model);
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.setOpaque(false);
        btnPanel.add(limpiarBtn);
        btnPanel.add(programarBtn);
        btnPanel.add(finalizarBtn);

        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("Ruta:"), gbc);
        gbc.gridx = 1;
        form.add(rutaComboSalidas, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        form.add(new JLabel("Bus:"), gbc);
        gbc.gridx = 1;
        form.add(busComboSalidas, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        form.add(new JLabel("Fecha:"), gbc);
        gbc.gridx = 1;
        form.add(fechaSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        form.add(new JLabel("Hora:"), gbc);
        gbc.gridx = 1;
        form.add(horaSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        form.add(new JLabel("Tarifa base:"), gbc);
        gbc.gridx = 1;
        form.add(precioField, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        form.add(btnPanel, gbc);

        actualizarTablaSalidas(empresa.listarSalidas(), model);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setOpaque(false);
        JComboBox<String> estadoFiltro = new JComboBox<>(new String[]{"Todos", Salida.PROGRAMADA, Salida.EN_RUTA, Salida.FINALIZADA, Salida.CANCELADA});
        JButton filtrarBtn = new JButton("Filtrar");
        filterPanel.add(new JLabel("Estado:"));
        filterPanel.add(estadoFiltro);
        filterPanel.add(filtrarBtn);

        filtrarBtn.addActionListener(e -> {
            String filtro = (String) estadoFiltro.getSelectedItem();
            if ("Todos".equals(filtro)) {
                actualizarTablaSalidas(empresa.listarSalidas(), model);
            } else {
                java.util.List<Salida> filtradas = new java.util.ArrayList<>();
                for (Salida s : empresa.listarSalidas()) {
                    if (s.getEstado().equals(filtro)) filtradas.add(s);
                }
                actualizarTablaSalidas(filtradas, model);
            }
        });

        JTable table = new JTable(model);
        table.setRowHeight(28);

        JComboBox<String> estadoComboEditor = new JComboBox<>(new String[]{Salida.PROGRAMADA, Salida.EN_RUTA, Salida.FINALIZADA, Salida.CANCELADA});
        table.getColumn("Estado").setCellRenderer(new EstadoRenderer());
        table.getColumn("Estado").setCellEditor(new EstadoComboEditor(estadoComboEditor, empresa, model, this));

        table.getColumn("Acciones").setCellRenderer(new ButtonRenderer());
        table.getColumn("Acciones").setCellEditor(new ButtonEditor(new JButton("Editar"), e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                String idSalida = model.getValueAt(row, 0).toString();
                Salida salida = empresa.getSalidaPorId(idSalida);
                if (salida != null) {
                    JTextField rutaField = new JTextField(salida.getMyRuta().getCodigo());
                    JTextField busField = new JTextField(salida.getMyBus().getPlaca());
                    JTextField fechaField = new JTextField(salida.getFecha().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                    Object[] fields = { "Ruta:", rutaField, "Bus:", busField, "Fecha (dd/MM/yyyy HH:mm):", fechaField };
                    int opt = JOptionPane.showConfirmDialog(ParametrizacionPanel.this, fields, "Editar salida " + idSalida, JOptionPane.OK_CANCEL_OPTION);
                    if (opt == JOptionPane.OK_OPTION) {
                        try {
                            String nuevaRuta = rutaField.getText().trim();
                            String nuevoBus = busField.getText().trim();
                            String nuevaFecha = fechaField.getText().trim();
                            LocalDateTime nuevaFH = LocalDateTime.parse(nuevaFecha, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                            salida.setMyRuta(empresa.getRutaPorCodigo(nuevaRuta));
                            salida.setMyBus(empresa.getBusPorPlaca(nuevoBus));
                            salida.setFecha(nuevaFH);
                            actualizarTablaSalidas(empresa.listarSalidas(), model);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(ParametrizacionPanel.this, "Error al editar: " + ex.getMessage());
                        }
                    }
                }
            }
        }));

        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setOpaque(false);
        tableContainer.add(filterPanel, BorderLayout.NORTH);
        tableContainer.add(new JScrollPane(table), BorderLayout.CENTER);

        panel.add(form, BorderLayout.NORTH);
        panel.add(tableContainer, BorderLayout.CENTER);

        return panel;
    }

    private void cargarItemsCombosSalidas() {
        rutaComboSalidas.removeAllItems();
        for (Ruta r : empresa.listarRutas()) {
            rutaComboSalidas.addItem(r.getCodigo() + " — " + r.getOrigen() + " \u2192 " + r.getDestino());
        }
        busComboSalidas.removeAllItems();
        for (Bus b : empresa.listarBuses()) {
            if (Bus.DISPONIBLE.equals(b.getEstado())) {
                busComboSalidas.addItem(b.getPlaca() + " (Cap: " + b.getCapacidad() + ")");
            }
        }
    }

    private void actualizarTablaSalidas(java.util.List<Salida> salidas, DefaultTableModel model) {
        if (model == null) return;
        model.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter horaFmt = DateTimeFormatter.ofPattern("HH:mm");
        for (Salida s : salidas) {
            int vendidos = s.totalPasajesVendidos(empresa.getMyTickets());
            model.addRow(new Object[]{
                s.getIdSalida(),
                s.getMyRuta().getCodigo(),
                s.getFecha().format(fmt),
                s.getFecha().format(horaFmt),
                s.getMyBus().getPlaca(),
                s.getEstado(),
                vendidos,
                "Editar"
            });
        }
    }

    // ============================================================
    // TAB CONDUCTORES
    // ============================================================

    private JPanel crearPanelConductores() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(Colores.FONDO_GENERAL);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Colores.FONDO_TARJETA);
        form.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Colores.BORDE, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        JTextField cedulaField = new JTextField(10);
        JTextField nombreField = new JTextField(10);
        JTextField direccionField = new JTextField(10);
        JTextField correoField = new JTextField(10);
        JTextField telefonoField = new JTextField(10);
        JFormattedTextField sueldoField = new JFormattedTextField(NumberFormat.getNumberInstance());
        sueldoField.setColumns(10);

        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("C\u00e9dula:"), gbc);
        gbc.gridx = 1;
        form.add(cedulaField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        form.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1;
        form.add(nombreField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        form.add(new JLabel("Direcci\u00f3n:"), gbc);
        gbc.gridx = 1;
        form.add(direccionField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        form.add(new JLabel("Correo:"), gbc);
        gbc.gridx = 1;
        form.add(correoField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        form.add(new JLabel("Tel\u00e9fono:"), gbc);
        gbc.gridx = 1;
        form.add(telefonoField, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        form.add(new JLabel("Sueldo:"), gbc);
        gbc.gridx = 1;
        form.add(sueldoField, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.setOpaque(false);
        JButton limpiarBtn = new JButton("Limpiar");
        JButton guardarBtn = new JButton("Guardar conductor");
        guardarBtn.setBackground(Colores.AZUL_PRIMARIO);
        guardarBtn.setForeground(Color.WHITE);
        btnPanel.add(limpiarBtn);
        btnPanel.add(guardarBtn);

        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        form.add(btnPanel, gbc);

        DefaultTableModel model = new DefaultTableModel(new String[]{"C\u00e9dula", "Nombre", "Correo", "Tel\u00e9fono", "Sueldo", "Acciones"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return col == 5; }
        };

        guardarBtn.addActionListener(e -> {
            String cedula = cedulaField.getText().trim();
            String nombre = nombreField.getText().trim();
            String direccion = direccionField.getText().trim();
            String correo = correoField.getText().trim();
            String telefono = telefonoField.getText().trim();
            float sueldo;
            try {
                sueldo = Float.parseFloat(sueldoField.getText().replaceAll("[.,]", ""));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Sueldo inv\u00e1lido");
                return;
            }
            if (cedula.isEmpty() || nombre.isEmpty()) {
                JOptionPane.showMessageDialog(this, "C\u00e9dula y nombre son obligatorios");
                return;
            }
            if (editingConductorCedula != null) {
                empresa.editarConductor(editingConductorCedula, nombre, direccion, correo, telefono, sueldo);
                editingConductorCedula = null;
                cedulaField.setEditable(true);
            } else {
                empresa.crearConductor(cedula, nombre, direccion, correo, telefono, sueldo);
            }
            actualizarTablaConductores(model);
            limpiarCampos(cedulaField, nombreField, direccionField, correoField, telefonoField, sueldoField);
        });

        limpiarBtn.addActionListener(e -> {
            limpiarCampos(cedulaField, nombreField, direccionField, correoField, telefonoField, sueldoField);
            editingConductorCedula = null;
            cedulaField.setEditable(true);
        });

        JTable table = new JTable(model) {
            @Override
            public Class<?> getColumnClass(int col) {
                if (col == 5) return JButton.class;
                return super.getColumnClass(col);
            }
        };
        table.setRowHeight(28);
        actualizarTablaConductores(model);

        table.getColumn("Acciones").setCellRenderer(new ButtonRenderer());
        table.getColumn("Acciones").setCellEditor(new ButtonEditor(new JButton("Editar"), e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                editingConductorCedula = model.getValueAt(row, 0).toString();
                // Buscar el conductor para obtener la dirección (no está en la tabla)
                Conductor c = empresa.getConductorPorCedula(editingConductorCedula);
                cedulaField.setText(editingConductorCedula);
                cedulaField.setEditable(false);
                nombreField.setText(model.getValueAt(row, 1).toString());
                correoField.setText(model.getValueAt(row, 2).toString());
                telefonoField.setText(model.getValueAt(row, 3).toString());
                sueldoField.setText(model.getValueAt(row, 4).toString().replaceAll("[^0-9]", ""));
                if (c != null) {
                    direccionField.setText(c.getDireccion());
                }
            }
        }));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, form, new JScrollPane(table));
        split.setDividerLocation(300);
        panel.add(split, BorderLayout.CENTER);

        return panel;
    }

    private void actualizarTablaConductores(DefaultTableModel model) {
        model.setRowCount(0);
        for (Conductor c : empresa.listarConductores()) {
            model.addRow(new Object[]{
                c.getCedula(),
                c.getNombre(),
                c.getCorreo(),
                c.getTelefono(),
                String.format("$%,.0f", c.getSueldo()),
                "Editar"
            });
        }
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private void limpiarCampos(JTextField... campos) {
        for (JTextField c : campos) c.setText("");
    }

    private static class EstadoRenderer extends JLabel implements javax.swing.table.TableCellRenderer {
        public EstadoRenderer() {
            setOpaque(true);
            setFont(new Font("SansSerif", Font.PLAIN, 11));
            setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            setText(value != null ? value.toString() : "");
            String estado = value != null ? value.toString() : "";
            if (Salida.PROGRAMADA.equals(estado)) {
                setBackground(Colores.ESTADO_VERDE);
                setForeground(Colores.ESTADO_VERDE_TX);
            } else if (Salida.EN_RUTA.equals(estado)) {
                setBackground(Colores.AZUL_MEDIO);
                setForeground(Color.WHITE);
            } else if (Salida.FINALIZADA.equals(estado)) {
                setBackground(Colores.FONDO_SUPERFICIE);
                setForeground(Colores.TEXTO_SECUNDARIO);
            } else if (Salida.CANCELADA.equals(estado)) {
                setBackground(Colores.ESTADO_ROJO);
                setForeground(Colores.ESTADO_ROJO_TX);
            } else {
                setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
            }
            return this;
        }
    }

    private static class EstadoComboEditor extends javax.swing.AbstractCellEditor implements javax.swing.table.TableCellEditor {
        private final JComboBox<String> combo;
        private final EmpresaTransporte empresa;
        private final DefaultTableModel model;
        private final JPanel parent;
        private int editingRow;
        private String valorAnterior;

        public EstadoComboEditor(JComboBox<String> combo, EmpresaTransporte empresa, DefaultTableModel model, JPanel parent) {
            this.combo = combo;
            this.empresa = empresa;
            this.model = model;
            this.parent = parent;
            combo.addActionListener(e -> fireEditingStopped());
        }
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
            this.editingRow = row;
            valorAnterior = value != null ? value.toString() : "";
            combo.setSelectedItem(valorAnterior);
            return combo;
        }
        @Override
        public Object getCellEditorValue() {
            String nuevoEstado = (String) combo.getSelectedItem();
            if (nuevoEstado != null && !nuevoEstado.equals(valorAnterior) && editingRow >= 0) {
                String idSalida = model.getValueAt(editingRow, 0).toString();
                if (Salida.EN_RUTA.equals(nuevoEstado)) {
                    boolean efectiva = empresa.esSalidaEfectiva(idSalida);
                    if (!efectiva) {
                        JOptionPane.showMessageDialog(parent, "La salida debe tener m\u00ednimo 5 pasajes vendidos para pasar a EN_RUTA.", "Validaci\u00f3n", JOptionPane.WARNING_MESSAGE);
                        return valorAnterior;
                    }
                }
                empresa.editarSalida(idSalida, nuevoEstado);
            }
            return nuevoEstado != null ? nuevoEstado : valorAnterior;
        }
    }

    private static class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setText("Editar");
            setBackground(Colores.AZUL_CLARO);
            setForeground(Colores.AZUL_PRIMARIO);
            setFont(new Font("SansSerif", Font.PLAIN, 11));
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            return this;
        }
    }

    private static class ButtonEditor extends javax.swing.AbstractCellEditor implements javax.swing.table.TableCellEditor {
        private final JButton button;
        public ButtonEditor(JButton btn, java.awt.event.ActionListener listener) {
            this.button = btn;
            btn.addActionListener(listener);
            btn.addActionListener(e -> fireEditingStopped());
        }
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
            return button;
        }
        @Override
        public Object getCellEditorValue() { return "Editar"; }
    }
}

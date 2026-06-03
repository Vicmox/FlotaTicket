package presentacion;

import negocio.*;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ParametrizacionPanel extends JPanel {

    private final EmpresaTransporte empresa;

    private String editingRutaCodigo;

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

        add(tabs, BorderLayout.CENTER);
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

        JTextField origenField = new JTextField(10);
        JTextField destinoField = new JTextField(10);
        JFormattedTextField tarifaField = new JFormattedTextField(NumberFormat.getNumberInstance());
        tarifaField.setColumns(10);

        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("Origen:"), gbc);
        gbc.gridx = 1;
        form.add(origenField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        form.add(new JLabel("Destino:"), gbc);
        gbc.gridx = 1;
        form.add(destinoField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
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
            String orig = origenField.getText().trim();
            String dest = destinoField.getText().trim();
            float tarifa;
            try {
                tarifa = Float.parseFloat(tarifaField.getText().replaceAll("[.,]", ""));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Tarifa inv\u00e1lida");
                return;
            }
            if (orig.isEmpty() || dest.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios");
                return;
            }
            if (editingRutaCodigo != null) {
                empresa.editarRuta(editingRutaCodigo, orig, dest, tarifa);
                editingRutaCodigo = null;
            } else {
                empresa.crearRuta("", orig, dest, tarifa);
            }
            actualizarTablaRutas(model);
            limpiarCampos(origenField, destinoField, tarifaField);
        });

        limpiarBtn.addActionListener(e -> {
            limpiarCampos(origenField, destinoField, tarifaField);
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
                String rutaStr = model.getValueAt(row, 1).toString();
                String[] parts = rutaStr.split(" \u2192 ");
                if (parts.length == 2) {
                    origenField.setText(parts[0]);
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

        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("Placa:"), gbc);
        gbc.gridx = 1;
        form.add(placaField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        form.add(new JLabel("Tipo:"), gbc);
        gbc.gridx = 1;
        form.add(tipoCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        JLabel capFijaLabel = new JLabel("Capacidad: " + getCapacidadPorTipo((String) tipoCombo.getSelectedItem()));
        capFijaLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        capFijaLabel.setForeground(Colores.TEXTO_SECUNDARIO);
        form.add(capFijaLabel, gbc);

        tipoCombo.addActionListener(e ->
            capFijaLabel.setText("Capacidad: " + getCapacidadPorTipo((String) tipoCombo.getSelectedItem()))
        );

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.setOpaque(false);
        JButton limpiarBtn = new JButton("Limpiar");
        JButton guardarBtn = new JButton("Guardar bus");
        guardarBtn.setBackground(Colores.AZUL_PRIMARIO);
        guardarBtn.setForeground(Color.WHITE);
        btnPanel.add(limpiarBtn);
        btnPanel.add(guardarBtn);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        form.add(btnPanel, gbc);

        JPanel listaPanel = new JPanel();

        guardarBtn.addActionListener(e -> {
            String placa = placaField.getText().trim().toUpperCase();
            if (placa.isEmpty()) { JOptionPane.showMessageDialog(this, "Placa obligatoria"); return; }
            String tipo = (String) tipoCombo.getSelectedItem();
            empresa.crearBus(placa, Bus.DISPONIBLE, tipo);
            actualizarListaBuses(listaPanel);
            limpiarCampos(placaField);
        });

        limpiarBtn.addActionListener(e -> limpiarCampos(placaField));
        listaPanel.setLayout(new BoxLayout(listaPanel, BoxLayout.Y_AXIS));
        listaPanel.setBackground(Colores.FONDO_GENERAL);

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

            JLabel badge = new JLabel(b.getEstado());
            badge.setFont(new Font("SansSerif", Font.BOLD, 11));
            badge.setOpaque(true);
            badge.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            if (Bus.DISPONIBLE.equals(b.getEstado())) {
                badge.setBackground(Colores.ESTADO_VERDE);
                badge.setForeground(Colores.ESTADO_VERDE_TX);
            } else if (Bus.MANTENIMIENTO.equals(b.getEstado())) {
                badge.setBackground(Colores.ESTADO_AMBAR);
                badge.setForeground(Colores.ESTADO_AMBAR_TX);
            } else {
                badge.setBackground(Colores.FONDO_SUPERFICIE);
                badge.setForeground(Colores.TEXTO_SECUNDARIO);
            }

            card.add(icono, BorderLayout.WEST);
            card.add(info, BorderLayout.CENTER);
            card.add(badge, BorderLayout.EAST);

            listaPanel.add(card);
            listaPanel.add(Box.createVerticalStrut(5));
        }
        listaPanel.revalidate();
        listaPanel.repaint();
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

        DefaultTableModel model = new DefaultTableModel(new String[]{"ID Salida", "Ruta", "Fecha", "Hora", "Bus", "Estado", "Acciones"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return col == 5 || col == 6; }
            @Override
            public Class<?> getColumnClass(int col) {
                if (col == 5) return String.class;
                if (col == 6) return JButton.class;
                return super.getColumnClass(col);
            }
        };

        JComboBox<String> rutaCombo = new JComboBox<>();
        for (Ruta r : empresa.listarRutas()) {
            rutaCombo.addItem(r.getCodigo() + " — " + r.getOrigen() + " \u2192 " + r.getDestino());
        }
        JComboBox<String> busCombo = new JComboBox<>();
        for (Bus b : empresa.listarBuses()) {
            if (Bus.DISPONIBLE.equals(b.getEstado())) {
                busCombo.addItem(b.getPlaca() + " (Cap: " + b.getCapacidad() + ")");
            }
        }

        SpinnerDateModel dateModel = new SpinnerDateModel();
        JSpinner fechaSpinner = new JSpinner(dateModel);
        fechaSpinner.setEditor(new JSpinner.DateEditor(fechaSpinner, "dd/MM/yyyy"));

        SpinnerDateModel timeModel = new SpinnerDateModel();
        JSpinner horaSpinner = new JSpinner(timeModel);
        horaSpinner.setEditor(new JSpinner.DateEditor(horaSpinner, "HH:mm"));

        JTextField precioField = new JTextField(10);
        precioField.setEditable(false);

        rutaCombo.addActionListener(e -> {
            String selected = (String) rutaCombo.getSelectedItem();
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

        programarBtn.addActionListener(e -> {
            String rutaSel = (String) rutaCombo.getSelectedItem();
            String busSel = (String) busCombo.getSelectedItem();
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

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.setOpaque(false);
        btnPanel.add(limpiarBtn);
        btnPanel.add(programarBtn);

        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("Ruta:"), gbc);
        gbc.gridx = 1;
        form.add(rutaCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        form.add(new JLabel("Bus:"), gbc);
        gbc.gridx = 1;
        form.add(busCombo, gbc);

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
        JComboBox<String> estadoFiltro = new JComboBox<>(new String[]{"Todos", Salida.PROGRAMADA, Salida.EN_RUTA, Salida.COMPLETADA, Salida.CANCELADA});
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

        JComboBox<String> estadoComboEditor = new JComboBox<>(new String[]{Salida.PROGRAMADA, Salida.EN_RUTA, Salida.COMPLETADA, Salida.CANCELADA});
        table.getColumn("Estado").setCellRenderer(new EstadoRenderer());
        table.getColumn("Estado").setCellEditor(new EstadoComboEditor(estadoComboEditor, empresa, model));

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

    private void actualizarTablaSalidas(java.util.List<Salida> salidas, DefaultTableModel model) {
        if (model == null) return;
        model.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter horaFmt = DateTimeFormatter.ofPattern("HH:mm");
        for (Salida s : salidas) {
            model.addRow(new Object[]{
                s.getIdSalida(),
                s.getMyRuta().getCodigo(),
                s.getFecha().format(fmt),
                s.getFecha().format(horaFmt),
                s.getMyBus().getPlaca(),
                s.getEstado(),
                "Editar"
            });
        }
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private int getCapacidadPorTipo(String tipo) {
        return "EJECUTIVO".equalsIgnoreCase(tipo) ? 40 : 30;
    }

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
                setBackground(Colores.ESTADO_AZUL_TX);
                setForeground(Color.WHITE);
            } else if (Salida.COMPLETADA.equals(estado)) {
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
        private int editingRow;
        private String valorAnterior;

        public EstadoComboEditor(JComboBox<String> combo, EmpresaTransporte empresa, DefaultTableModel model) {
            this.combo = combo;
            this.empresa = empresa;
            this.model = model;
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

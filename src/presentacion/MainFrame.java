package presentacion;

import negocio.EmpresaTransporte;
import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private final EmpresaTransporte empresa;
    private final JPanel contentPanel;
    private final CardLayout cardLayout;
    private final NavPanel navPanel;
    private DashboardPanel dashboardPanel;
    private VentasPanel ventasPanel;
    private ReportesPanel reportesPanel;
    private ParametrizacionPanel parametrizacionPanel;

    public MainFrame(EmpresaTransporte empresa, String usuario) {
        this.empresa = empresa;
        setTitle("Copetran — Sistema de gesti\u00f3n");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        northPanel.add(new HeaderPanel(usuario));

        navPanel = new NavPanel();
        northPanel.add(navPanel);

        add(northPanel, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        dashboardPanel = new DashboardPanel(empresa);
        ventasPanel = new VentasPanel(empresa);
        reportesPanel = new ReportesPanel(empresa);
        parametrizacionPanel = new ParametrizacionPanel(empresa);

        dashboardPanel.setNavegacionListener(key -> {
            cardLayout.show(contentPanel, key);
            navPanel.setActive(key);
            if ("ventas".equals(key)) ventasPanel.refreshData();
            if ("reportes".equals(key)) reportesPanel.refreshData();
        });
        contentPanel.add(dashboardPanel, "dashboard");
        contentPanel.add(parametrizacionPanel, "parametrizacion");
        contentPanel.add(ventasPanel, "ventas");
        contentPanel.add(new CancelacionesPanel(empresa), "cancelaciones");
        contentPanel.add(reportesPanel, "reportes");

        add(contentPanel, BorderLayout.CENTER);

        navPanel.addNavegacionListener(key -> {
            cardLayout.show(contentPanel, key);
            navPanel.setActive(key);
            if ("dashboard".equals(key)) {
                dashboardPanel.actualizarDatos();
            }
            if ("ventas".equals(key)) {
                ventasPanel.refreshData();
            }
            if ("reportes".equals(key)) {
                reportesPanel.refreshData();
            }
            if ("parametrizacion".equals(key)) {
                parametrizacionPanel.refreshData();
            }
        });
    }

    public EmpresaTransporte getEmpresa() {
        return empresa;
    }
}

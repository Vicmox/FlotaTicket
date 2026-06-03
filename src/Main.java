import negocio.EmpresaTransporte;
import presentacion.LoginFrame;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        EmpresaTransporte empresa = new EmpresaTransporte();
        SwingUtilities.invokeLater(() -> new LoginFrame(empresa).setVisible(true));
    }
}

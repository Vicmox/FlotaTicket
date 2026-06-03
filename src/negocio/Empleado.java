package negocio;

public class Empleado extends Persona {
    private String idEmpleado;
    private TipoEmpleado rol;

    public Empleado(String cedula, String nombre, String direccion, String idEmpleado, TipoEmpleado rol) {
        super(cedula, nombre, direccion);
        this.idEmpleado = idEmpleado;
        this.rol = rol;
    }

    public String getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(String idEmpleado) { this.idEmpleado = idEmpleado; }

    public TipoEmpleado getRol() { return rol; }
    public void setRol(TipoEmpleado rol) { this.rol = rol; }
}

package negocio;

public class Conductor extends Persona {
    private String correo;
    private String telefono;
    private float sueldo;

    public Conductor(String cedula, String nombre, String direccion, String correo, String telefono, float sueldo) {
        super(cedula, nombre, direccion);
        this.correo = correo;
        this.telefono = telefono;
        this.sueldo = sueldo;
    }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public float getSueldo() { return sueldo; }
    public void setSueldo(float sueldo) { this.sueldo = sueldo; }
}

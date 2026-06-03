package negocio;

public class Pasajero extends Persona {
    private String idPasajero;

    public Pasajero(String cedula, String nombre, String direccion, String idPasajero) {
        super(cedula, nombre, direccion);
        this.idPasajero = idPasajero;
    }

    public String getIdPasajero() { return idPasajero; }
    public void setIdPasajero(String idPasajero) { this.idPasajero = idPasajero; }
}

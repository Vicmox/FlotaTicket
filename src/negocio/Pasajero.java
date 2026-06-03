package negocio;

public class Pasajero extends Persona {
    private String idPasajero;
    private String correo;
    private String telefono;
    private TipoPasajero tipo;
    private int totalPasajesComprados;

    public Pasajero(String cedula, String nombre, String direccion, String idPasajero,
                    String correo, String telefono) {
        super(cedula, nombre, direccion);
        this.idPasajero = idPasajero;
        this.correo = correo;
        this.telefono = telefono;
        this.totalPasajesComprados = 0;
        this.tipo = TipoPasajero.NO_FRECUENTE;
    }

    public String getIdPasajero() { return idPasajero; }
    public void setIdPasajero(String idPasajero) { this.idPasajero = idPasajero; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public TipoPasajero getTipo() { return tipo; }
    public void setTipo(TipoPasajero tipo) { this.tipo = tipo; }

    public int getTotalPasajesComprados() { return totalPasajesComprados; }
    public void setTotalPasajesComprados(int totalPasajesComprados) { this.totalPasajesComprados = totalPasajesComprados; }

    public void incrementarPasajes() {
        this.totalPasajesComprados++;
        if (this.totalPasajesComprados >= 5) {
            this.tipo = TipoPasajero.PREFERENCIAL;
        }
    }

    public boolean esPreferencial() {
        return this.tipo == TipoPasajero.PREFERENCIAL;
    }
}

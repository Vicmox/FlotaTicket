package negocio;

public class PasajeTicket {
    public static final String VIGENTE = "VIGENTE";
    public static final String REEMBOLSADO = "REEMBOLSADO";

    private Salida mySalida;
    private int puesto;
    private float valorPagar;
    private String estado;
    private Pasajero myPasajero;
    private boolean idaYVuelta;

    public PasajeTicket(Salida mySalida, int puesto, float valorPagar, String estado, Pasajero myPasajero, boolean idaYVuelta) {
        this.mySalida = mySalida;
        this.puesto = puesto;
        this.valorPagar = valorPagar;
        this.estado = estado;
        this.myPasajero = myPasajero;
        this.idaYVuelta = idaYVuelta;
    }

    public PasajeTicket(Salida mySalida, int puesto, float valorPagar, String estado, Pasajero myPasajero) {
        this(mySalida, puesto, valorPagar, estado, myPasajero, false);
    }

    public Salida getMySalida() { return mySalida; }
    public void setMySalida(Salida mySalida) { this.mySalida = mySalida; }

    public int getPuesto() { return puesto; }
    public void setPuesto(int puesto) { this.puesto = puesto; }

    public float getValorPagar() { return valorPagar; }
    public void setValorPagar(float valorPagar) { this.valorPagar = valorPagar; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Pasajero getMyPasajero() { return myPasajero; }
    public void setMyPasajero(Pasajero myPasajero) { this.myPasajero = myPasajero; }

    public boolean isIdaYVuelta() { return idaYVuelta; }
    public void setIdaYVuelta(boolean idaYVuelta) { this.idaYVuelta = idaYVuelta; }
}

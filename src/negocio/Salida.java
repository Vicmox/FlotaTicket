package negocio;

import java.time.LocalDateTime;

public class Salida {
    public static final String PROGRAMADA = "PROGRAMADA";
    public static final String EN_RUTA = "EN_RUTA";
    public static final String COMPLETADA = "COMPLETADA";
    public static final String CANCELADA = "CANCELADA";

    private String idSalida;
    private Ruta myRuta;
    private LocalDateTime fecha;
    private Bus myBus;
    private String estado;

    public Salida(String idSalida, Ruta myRuta, LocalDateTime fecha, Bus myBus, String estado) {
        this.idSalida = idSalida;
        this.myRuta = myRuta;
        this.fecha = fecha;
        this.myBus = myBus;
        this.estado = estado;
    }

    public String getIdSalida() { return idSalida; }
    public void setIdSalida(String idSalida) { this.idSalida = idSalida; }

    public Ruta getMyRuta() { return myRuta; }
    public void setMyRuta(Ruta myRuta) { this.myRuta = myRuta; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public Bus getMyBus() { return myBus; }
    public void setMyBus(Bus myBus) { this.myBus = myBus; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}

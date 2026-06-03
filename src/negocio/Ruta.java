package negocio;

public class Ruta {
    private String codigo;
    private String origen;
    private String destino;
    private float tarifa;

    public Ruta(String codigo, String origen, String destino, float tarifa) {
        this.codigo = codigo;
        if (origen == null || origen.isEmpty()) {
            this.origen = "Cúcuta";
        } else {
            this.origen = origen;
        }
        this.destino = destino;
        this.tarifa = tarifa;
    }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }

    public String getDestino() { return destino; }
    public void setDestino(String destino) { this.destino = destino; }

    public float getTarifa() { return tarifa; }
    public void setTarifa(float tarifa) { this.tarifa = tarifa; }
}

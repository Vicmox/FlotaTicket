package negocio;

import java.util.List;

public class CajaVenta {
    private float montoCaja;
    private float totalVendido;
    private float totalReembolsado;

    public CajaVenta() {
        this.montoCaja = 0f;
        this.totalVendido = 0f;
        this.totalReembolsado = 0f;
    }

    public void registrarVenta(float monto) {
        this.montoCaja += monto;
        this.totalVendido += monto;
    }

    public void registrarReembolso(float monto) {
        this.montoCaja -= monto;
        this.totalReembolsado += monto;
    }

    public float getMontoCaja() { return montoCaja; }

    public float getTotalVendido() { return totalVendido; }

    public float getTotalReembolsado() { return totalReembolsado; }

    public float getIngresoNeto() {
        return totalVendido - totalReembolsado;
    }

    public CajaVenta reportesDelDia() {
        return this;
    }

    public int getVentasPorRuta(List<PasajeTicket> tickets, String codigoRuta) {
        return (int) tickets.stream()
            .filter(t -> t.getEstado().equals(PasajeTicket.VIGENTE))
            .filter(t -> t.getMySalida().getMyRuta().getCodigo().equals(codigoRuta))
            .count();
    }

    public int getVentasEnRango(List<PasajeTicket> tickets, java.time.LocalDateTime desde, java.time.LocalDateTime hasta) {
        return (int) tickets.stream()
            .filter(t -> t.getEstado().equals(PasajeTicket.VIGENTE))
            .filter(t -> !t.getMySalida().getFecha().isBefore(desde) && !t.getMySalida().getFecha().isAfter(hasta))
            .count();
    }
}

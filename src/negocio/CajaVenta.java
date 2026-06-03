package negocio;

import java.util.List;

public class CajaVenta {
    private float montoCaja;
    private int totalVendidos;
    private int totalReembolsado;
    private float ingresoNeto;

    public CajaVenta() {
        this.montoCaja = 0f;
        this.totalVendidos = 0;
        this.totalReembolsado = 0;
        this.ingresoNeto = 0f;
    }

    public void registrarVenta(float monto) {
        this.montoCaja += monto;
        this.totalVendidos++;
        this.ingresoNeto += monto;
    }

    public void registrarReembolso(float monto) {
        this.montoCaja -= monto;
        this.totalReembolsado++;
        this.ingresoNeto -= monto;
    }

    public float getMontoCaja() { return montoCaja; }

    public int getTotalVendidos() { return totalVendidos; }

    public int getTotalReembolsos() { return totalReembolsado; }

    public float getIngresoNeto() { return ingresoNeto; }

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

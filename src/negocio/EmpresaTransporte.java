package negocio;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EmpresaTransporte {

    private List<Bus> myBuses;
    private List<Ruta> myRutas;
    private List<Salida> mySalidas;
    private List<PasajeTicket> myTickets;
    private CajaVenta myCaja;
    private List<Empleado> myEmpleado;
    private int secuencialSalida;
    private int secuencialRuta;

    public EmpresaTransporte() {
        this.myBuses = new ArrayList<>();
        this.myRutas = new ArrayList<>();
        this.mySalidas = new ArrayList<>();
        this.myTickets = new ArrayList<>();
        this.myEmpleado = new ArrayList<>();
        this.myCaja = new CajaVenta();
        this.secuencialSalida = 1;
        this.secuencialRuta = 5;
        cargarDatosBase();
    }

    // ============================================================
    // Getters
    // ============================================================

    public List<Bus> getMyBuses() { return myBuses; }
    public List<Ruta> getMyRutas() { return myRutas; }
    public List<Salida> getMySalidas() { return mySalidas; }
    public List<PasajeTicket> getMyTickets() { return myTickets; }
    public CajaVenta getMyCaja() { return myCaja; }
    public List<Empleado> getMyEmpleado() { return myEmpleado; }

    // ============================================================
    // RF1 — CRUD Buses
    // ============================================================

    public boolean crearBus(String placa, String estado, String tipo) {
        if (busExiste(placa)) return false;
        Bus bus;
        if ("EJECUTIVO".equalsIgnoreCase(tipo)) {
            bus = new BusTipoEjecutivo(placa, estado);
        } else {
            bus = new BusTipoNormal(placa, estado);
        }
        myBuses.add(bus);
        return true;
    }

    public List<Bus> listarBuses() {
        return new ArrayList<>(myBuses);
    }

    public boolean editarBus(String placa, String nuevoEstado) {
        Bus bus = getBusPorPlaca(placa);
        if (bus == null) return false;
        bus.setEstado(nuevoEstado);
        return true;
    }

    public boolean eliminarBus(String placa) {
        Bus bus = getBusPorPlaca(placa);
        if (bus == null) return false;
        myBuses.remove(bus);
        return true;
    }

    // ============================================================
    // RF1 — CRUD Rutas
    // ============================================================

    public boolean crearRuta(String codigo, String origen, String destino, float tarifa) {
        if (codigo == null || codigo.isEmpty()) {
            codigo = generarIdRuta();
        }
        if (rutaExiste(codigo)) return false;
        myRutas.add(new Ruta(codigo, origen, destino, tarifa));
        return true;
    }

    public String generarIdRuta() {
        return "R" + String.format("%02d", secuencialRuta++);
    }

    public List<Ruta> listarRutas() {
        return new ArrayList<>(myRutas);
    }

    public boolean editarRuta(String codigo, String nuevoOrigen, String nuevoDestino, float nuevaTarifa) {
        Ruta ruta = getRutaPorCodigo(codigo);
        if (ruta == null) return false;
        ruta.setOrigen(nuevoOrigen);
        ruta.setDestino(nuevoDestino);
        ruta.setTarifa(nuevaTarifa);
        return true;
    }

    public boolean eliminarRuta(String codigo) {
        Ruta ruta = getRutaPorCodigo(codigo);
        if (ruta == null) return false;
        myRutas.remove(ruta);
        return true;
    }

    // ============================================================
    // RF1 — CRUD Salidas
    // ============================================================

    public boolean crearSalida(String codigoRuta, String placaBus, LocalDateTime fecha) {
        Ruta ruta = getRutaPorCodigo(codigoRuta);
        Bus bus = getBusPorPlaca(placaBus);
        if (ruta == null || bus == null) return false;

        String idSalida = generarIdSalida();
        if (salidaExiste(placaBus, codigoRuta, idSalida)) return false;

        Salida salida = new Salida(idSalida, ruta, fecha, bus, Salida.PROGRAMADA);
        mySalidas.add(salida);
        return true;
    }

    public List<Salida> listarSalidas() {
        return new ArrayList<>(mySalidas);
    }

    public boolean editarSalida(String idSalida, String nuevoEstado) {
        Salida salida = getSalidaPorId(idSalida);
        if (salida == null) return false;
        salida.setEstado(nuevoEstado);
        return true;
    }

    // ============================================================
    // RF2 — Venta de pasaje individual
    // ============================================================

    public boolean registrarPasajeroYSilla(String idSalida, int numeroPuesto, Pasajero pasajero) {
        Salida salida = getSalidaPorId(idSalida);
        if (salida == null) return false;
        if (!Salida.PROGRAMADA.equals(salida.getEstado())) return false;

        Puesto[] puestos = salida.getMyBus().getMyPuestos();
        if (numeroPuesto < 1 || numeroPuesto > puestos.length) return false;
        if (!puestos[numeroPuesto - 1].estaLibre()) return false;

        puestos[numeroPuesto - 1].setMyPasajero(pasajero);
        return true;
    }

    public float calcularValorPasaje(String idSalida) {
        Salida salida = getSalidaPorId(idSalida);
        if (salida == null) return 0f;
        return salida.getMyRuta().getTarifa();
    }

    public PasajeTicket generarTicket(String idSalida, int numeroPuesto, Pasajero pasajero) {
        if (!registrarPasajeroYSilla(idSalida, numeroPuesto, pasajero)) return null;

        Salida salida = getSalidaPorId(idSalida);
        float valor = salida.getMyRuta().getTarifa();
        PasajeTicket ticket = new PasajeTicket(salida, numeroPuesto, valor, PasajeTicket.VIGENTE, pasajero);
        myTickets.add(ticket);
        myCaja.registrarVenta(valor);
        return ticket;
    }

    // ============================================================
    // RF3 — Venta ida y vuelta
    // ============================================================

    public boolean ventaIdaYVuelta(String idSalidaIda, int puestoIda, Pasajero pasajeroIda,
                                   String idSalidaVuelta, int puestoVuelta, Pasajero pasajeroVuelta) {
        Salida salidaIda = getSalidaPorId(idSalidaIda);
        Salida salidaVuelta = getSalidaPorId(idSalidaVuelta);

        if (salidaIda == null || salidaVuelta == null) return false;
        if (!salidaIda.getMyRuta().getCodigo().equals(salidaVuelta.getMyRuta().getCodigo())) return false;
        if (!Salida.PROGRAMADA.equals(salidaIda.getEstado()) || !Salida.PROGRAMADA.equals(salidaVuelta.getEstado())) return false;

        float tarifaIda = salidaIda.getMyRuta().getTarifa();
        float tarifaVuelta = salidaVuelta.getMyRuta().getTarifa();
        float totalSinDescuento = tarifaIda + tarifaVuelta;
        float totalConDescuento = totalSinDescuento * 0.9f;

        if (!registrarPasajeroYSilla(idSalidaIda, puestoIda, pasajeroIda)) return false;
        if (!registrarPasajeroYSilla(idSalidaVuelta, puestoVuelta, pasajeroVuelta)) return false;

        float valorIda = tarifaIda * 0.9f;
        float valorVuelta = tarifaVuelta * 0.9f;

        PasajeTicket ticketIda = new PasajeTicket(salidaIda, puestoIda, valorIda, PasajeTicket.VIGENTE, pasajeroIda);
        PasajeTicket ticketVuelta = new PasajeTicket(salidaVuelta, puestoVuelta, valorVuelta, PasajeTicket.VIGENTE, pasajeroVuelta);

        myTickets.add(ticketIda);
        myTickets.add(ticketVuelta);
        myCaja.registrarVenta(totalConDescuento);
        return true;
    }

    // ============================================================
    // RF4 — Cancelación de salida
    // ============================================================

    public void cancelarSalida(String idSalida, boolean reembolsar) {
        Salida salida = getSalidaPorId(idSalida);
        if (salida == null) return;

        salida.setEstado(Salida.CANCELADA);

        List<PasajeTicket> ticketsAfectados = new ArrayList<>();
        for (PasajeTicket t : myTickets) {
            if (t.getMySalida().getIdSalida().equals(idSalida) && PasajeTicket.VIGENTE.equals(t.getEstado())) {
                ticketsAfectados.add(t);
            }
        }

        Puesto[] puestosSalida = salida.getMyBus().getMyPuestos();

        for (PasajeTicket ticket : ticketsAfectados) {
            int numPuesto = ticket.getPuesto();
            if (numPuesto >= 1 && numPuesto <= puestosSalida.length) {
                puestosSalida[numPuesto - 1].setMyPasajero(null);
            }

            if (reembolsar) {
                ticket.setEstado(PasajeTicket.REEMBOLSADO);
                myCaja.registrarReembolso(ticket.getValorPagar());
            } else {
                reprogramarTicket(ticket);
            }
        }
    }

    private void reprogramarTicket(PasajeTicket ticket) {
        Salida salidaOriginal = ticket.getMySalida();
        String codigoRuta = salidaOriginal.getMyRuta().getCodigo();

        Salida nuevaSalida = null;
        for (Salida s : mySalidas) {
            if (s.getMyRuta().getCodigo().equals(codigoRuta)
                && Salida.PROGRAMADA.equals(s.getEstado())
                && s.getFecha().isAfter(salidaOriginal.getFecha())) {

                for (Puesto p : s.getMyBus().getMyPuestos()) {
                    if (p.estaLibre()) {
                        nuevaSalida = s;
                        break;
                    }
                }
                if (nuevaSalida != null) break;
            }
        }

        if (nuevaSalida != null) {
            Puesto[] puestos = nuevaSalida.getMyBus().getMyPuestos();
            for (int i = 0; i < puestos.length; i++) {
                if (puestos[i].estaLibre()) {
                    puestos[i].setMyPasajero(ticket.getMyPasajero());
                    ticket.setMySalida(nuevaSalida);
                    ticket.setPuesto(i + 1);
                    break;
                }
            }
        } else {
            ticket.setEstado(PasajeTicket.REEMBOLSADO);
            myCaja.registrarReembolso(ticket.getValorPagar());
        }
    }

    // ============================================================
    // RF5 — Reportes (delegados a CajaVenta)
    // ============================================================

    public CajaVenta getCajaVenta() {
        return myCaja;
    }

    public int getVentasPorRuta(String codigoRuta) {
        return myCaja.getVentasPorRuta(myTickets, codigoRuta);
    }

    public int getVentasEnRango(LocalDateTime desde, LocalDateTime hasta) {
        return myCaja.getVentasEnRango(myTickets, desde, hasta);
    }

    // ============================================================
    // Helpers
    // ============================================================

    private String generarIdSalida() {
        int anio = LocalDateTime.now().getYear();
        return "SAL-" + anio + "-" + String.format("%03d", secuencialSalida++);
    }

    private boolean busExiste(String placa) {
        return getBusPorPlaca(placa) != null;
    }

    private boolean rutaExiste(String codigo) {
        return getRutaPorCodigo(codigo) != null;
    }

    private boolean salidaExiste(String placa, String codigoRuta, String idSalida) {
        for (Salida s : mySalidas) {
            if (s.getMyBus().getPlaca().equals(placa)
                && s.getMyRuta().getCodigo().equals(codigoRuta)
                && s.getIdSalida().equals(idSalida)) {
                return true;
            }
        }
        return false;
    }

    public Ruta getRutaPorCodigo(String codigo) {
        for (Ruta r : myRutas) {
            if (r.getCodigo().equals(codigo)) return r;
        }
        return null;
    }

    public Bus getBusPorPlaca(String placa) {
        for (Bus b : myBuses) {
            if (b.getPlaca().equals(placa)) return b;
        }
        return null;
    }

    public Salida getSalidaPorId(String idSalida) {
        for (Salida s : mySalidas) {
            if (s.getIdSalida().equals(idSalida)) return s;
        }
        return null;
    }

    private LocalDateTime parseFecha(String fechaHora) {
        return LocalDateTime.parse(fechaHora, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    // ============================================================
    // Datos base
    // ============================================================

    private void cargarDatosBase() {
        myRutas.add(new Ruta("R01", "Cúcuta", "Bucaramanga", 80000f));
        myRutas.add(new Ruta("R02", "Cúcuta", "Bogotá", 160000f));
        myRutas.add(new Ruta("R03", "Cúcuta", "Medellín", 180000f));
        myRutas.add(new Ruta("R04", "Cúcuta", "Cartagena", 220000f));

        myBuses.add(new BusTipoNormal("KAA-101", "DISPONIBLE"));
        myBuses.add(new BusTipoEjecutivo("KBB-202", "DISPONIBLE"));
        myBuses.add(new BusTipoNormal("KCC-303", "DISPONIBLE"));
        myBuses.add(new BusTipoEjecutivo("KDD-404", "DISPONIBLE"));
        myBuses.add(new BusTipoNormal("KEE-505", "MANTENIMIENTO"));
        myBuses.add(new BusTipoNormal("KFF-606", "DISPONIBLE"));

        mySalidas.add(new Salida("S001", getRutaPorCodigo("R01"), parseFecha("15/03/2026 06:00"), getBusPorPlaca("KAA-101"), "PROGRAMADA"));
        mySalidas.add(new Salida("S002", getRutaPorCodigo("R01"), parseFecha("15/03/2026 14:00"), getBusPorPlaca("KBB-202"), "PROGRAMADA"));
        mySalidas.add(new Salida("S003", getRutaPorCodigo("R02"), parseFecha("16/03/2026 07:00"), getBusPorPlaca("KCC-303"), "PROGRAMADA"));
        mySalidas.add(new Salida("S004", getRutaPorCodigo("R02"), parseFecha("16/03/2026 20:00"), getBusPorPlaca("KDD-404"), "PROGRAMADA"));
        mySalidas.add(new Salida("S005", getRutaPorCodigo("R03"), parseFecha("17/03/2026 05:30"), getBusPorPlaca("KFF-606"), "PROGRAMADA"));
        mySalidas.add(new Salida("S006", getRutaPorCodigo("R03"), parseFecha("17/03/2026 18:00"), getBusPorPlaca("KAA-101"), "PROGRAMADA"));
        mySalidas.add(new Salida("S007", getRutaPorCodigo("R04"), parseFecha("18/03/2026 06:30"), getBusPorPlaca("KCC-303"), "PROGRAMADA"));
        mySalidas.add(new Salida("S008", getRutaPorCodigo("R04"), parseFecha("18/03/2026 19:30"), getBusPorPlaca("KBB-202"), "PROGRAMADA"));
    }
}

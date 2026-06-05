package negocio;

import java.time.LocalDate;
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
    private List<Conductor> myConductores;
    private List<Pasajero> myPasajeros;
    private int secuencialSalida;
    private int secuencialRuta;

    public EmpresaTransporte() {
        this.myBuses = new ArrayList<>();
        this.myRutas = new ArrayList<>();
        this.mySalidas = new ArrayList<>();
        this.myTickets = new ArrayList<>();
        this.myEmpleado = new ArrayList<>();
        this.myConductores = new ArrayList<>();
        this.myPasajeros = new ArrayList<>();
        this.myCaja = new CajaVenta();
        this.secuencialSalida = 1;
        this.secuencialRuta = 9;
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
    public List<Conductor> getMyConductores() { return myConductores; }
    public List<Pasajero> getMyPasajeros() { return myPasajeros; }

    // ============================================================
    // RF1 — CRUD Buses
    // ============================================================

    public boolean crearBus(String placa, String estado, String tipo, int capacidad) {
        if (busExiste(placa)) return false;
        Bus bus;
        if ("EJECUTIVO".equalsIgnoreCase(tipo)) {
            bus = new BusTipoEjecutivo(placa, estado, capacidad);
        } else {
            bus = new BusTipoNormal(placa, estado, capacidad);
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

    public boolean asignarConductorABus(String placa, String cedulaConductor) {
        Bus bus = getBusPorPlaca(placa);
        if (bus == null) return false;
        if (cedulaConductor == null || cedulaConductor.isEmpty()) {
            bus.setMyConductor(null);
            return true;
        }
        Conductor c = getConductorPorCedula(cedulaConductor);
        if (c == null) return false;
        bus.setMyConductor(c);
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

        // Validar que el mismo bus no tenga otra salida a la misma fecha y hora
        for (Salida s : mySalidas) {
            if (s.getMyBus().getPlaca().equals(placaBus) && s.getFecha().equals(fecha)) {
                return false;
            }
        }

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
        // Validar mínimo 5 tiquetes VIGENTES para activar la salida (EN_RUTA)
        if (Salida.EN_RUTA.equals(nuevoEstado)) {
            if (!salida.esSalidaEfectiva(myTickets)) {
                return false;
            }
        }
        salida.setEstado(nuevoEstado);
        return true;
    }

    // ============================================================
    // RF1 — CRUD Conductores (NUEVO)
    // ============================================================

    public boolean crearConductor(String cedula, String nombre, String direccion, String correo, String telefono, float sueldo) {
        if (conductorExiste(cedula)) return false;
        myConductores.add(new Conductor(cedula, nombre, direccion, correo, telefono, sueldo));
        return true;
    }

    public List<Conductor> listarConductores() {
        return new ArrayList<>(myConductores);
    }

    public boolean editarConductor(String cedula, String nuevoNombre, String nuevaDireccion, String nuevoCorreo, String nuevoTelefono, float nuevoSueldo) {
        Conductor c = getConductorPorCedula(cedula);
        if (c == null) return false;
        c.setNombre(nuevoNombre);
        c.setDireccion(nuevaDireccion);
        c.setCorreo(nuevoCorreo);
        c.setTelefono(nuevoTelefono);
        c.setSueldo(nuevoSueldo);
        return true;
    }

    public boolean eliminarConductor(String cedula) {
        Conductor c = getConductorPorCedula(cedula);
        if (c == null) return false;
        myConductores.remove(c);
        return true;
    }

    // ============================================================
    // RF2 — Venta de pasaje (1 o más tiquetes en puestos consecutivos)
    // ============================================================

    public Pasajero buscarOCrearPasajero(String cedula, String nombre, String direccion, String correo, String telefono) {
        for (Pasajero p : myPasajeros) {
            if (p.getCedula().equals(cedula)) {
                return p;
            }
        }
        Pasajero nuevo = new Pasajero(cedula, nombre, direccion, "P-" + cedula, correo, telefono);
        myPasajeros.add(nuevo);
        return nuevo;
    }

    public int[] verificarPuestosConsecutivos(Salida salida, int cantidad) {
        Puesto[] puestos = salida.getMyPuestos();
        for (int i = 0; i <= puestos.length - cantidad; i++) {
            boolean bloqueLibre = true;
            for (int j = 0; j < cantidad; j++) {
                if (!puestos[i + j].estaLibre()) {
                    bloqueLibre = false;
                    break;
                }
            }
            if (bloqueLibre) {
                int[] bloque = new int[cantidad];
                for (int j = 0; j < cantidad; j++) {
                    bloque[j] = i + j + 1; // 1-based
                }
                return bloque;
            }
        }
        return null;
    }

    public boolean registrarPasajerosYSillas(String idSalida, int[] numerosPuestos, Pasajero[] pasajeros) {
        Salida salida = getSalidaPorId(idSalida);
        if (salida == null) return false;
        if (!Salida.PROGRAMADA.equals(salida.getEstado())) return false;

        Puesto[] puestos = salida.getMyPuestos();
        for (int num : numerosPuestos) {
            if (num < 1 || num > puestos.length) return false;
            if (!puestos[num - 1].estaLibre()) return false;
        }

        for (int i = 0; i < numerosPuestos.length; i++) {
            puestos[numerosPuestos[i] - 1].setMyPasajero(pasajeros[i]);
        }
        return true;
    }

    private PasajeTicket[] crearTicketsInterno(String idSalida, int[] numerosPuestos, Pasajero[] pasajeros, boolean idaYVuelta) {
        if (!registrarPasajerosYSillas(idSalida, numerosPuestos, pasajeros)) return null;

        Salida salida = getSalidaPorId(idSalida);
        float tarifaBase = salida.getMyRuta().getTarifa();
        PasajeTicket[] tickets = new PasajeTicket[numerosPuestos.length];

        for (int i = 0; i < numerosPuestos.length; i++) {
            Pasajero p = pasajeros[i];
            float valor = tarifaBase;
            if (p.esPreferencial()) {
                valor = tarifaBase * 0.9f; // 10% descuento preferencial
            }
            PasajeTicket ticket = new PasajeTicket(salida, numerosPuestos[i], valor, PasajeTicket.VIGENTE, p, idaYVuelta);
            tickets[i] = ticket;
            myTickets.add(ticket);
            p.incrementarPasajes();
        }

        return tickets;
    }

    public PasajeTicket[] generarTickets(String idSalida, int[] numerosPuestos, Pasajero[] pasajeros, boolean idaYVuelta) {
        PasajeTicket[] tickets = crearTicketsInterno(idSalida, numerosPuestos, pasajeros, idaYVuelta);
        if (tickets == null) return null;
        float total = 0f;
        for (PasajeTicket t : tickets) total += t.getValorPagar();
        myCaja.registrarVenta(total);
        return tickets;
    }

    // ============================================================
    // RF3 — Venta ida y vuelta
    // ============================================================

    public boolean ventaIdaYVuelta(String idSalidaIda, int[] puestosIda, Pasajero[] pasajerosIda,
                                   String idSalidaVuelta, int[] puestosVuelta, Pasajero[] pasajerosVuelta) {
        Salida salidaIda = getSalidaPorId(idSalidaIda);
        Salida salidaVuelta = getSalidaPorId(idSalidaVuelta);

        if (salidaIda == null || salidaVuelta == null) return false;
        // Validar ruta inversa: ida.origen == vuelta.destino && ida.destino == vuelta.origen
        if (!salidaIda.getMyRuta().getOrigen().equals(salidaVuelta.getMyRuta().getDestino())
            || !salidaIda.getMyRuta().getDestino().equals(salidaVuelta.getMyRuta().getOrigen())) return false;
        if (!Salida.PROGRAMADA.equals(salidaIda.getEstado()) || !Salida.PROGRAMADA.equals(salidaVuelta.getEstado())) return false;

        // Verificar consecutividad y disponibilidad en ambas salidas
        if (verificarPuestosConsecutivos(salidaIda, puestosIda.length) == null) return false;
        if (verificarPuestosConsecutivos(salidaVuelta, puestosVuelta.length) == null) return false;

        // Generar tickets ida (sin tocar caja aún)
        PasajeTicket[] ticketsIda = crearTicketsInterno(idSalidaIda, puestosIda, pasajerosIda, true);
        if (ticketsIda == null) return false;

        // Generar tickets vuelta (sin tocar caja aún)
        PasajeTicket[] ticketsVuelta = crearTicketsInterno(idSalidaVuelta, puestosVuelta, pasajerosVuelta, true);
        if (ticketsVuelta == null) {
            // No hacer rollback: los tickets de ida quedan vigentes
            // El usuario puede usarlos individualmente o cancelarlos
            return false;
        }

        // Calcular total con descuento del 10% sobre el valor total (ida + vuelta)
        float totalIda = 0f;
        for (PasajeTicket t : ticketsIda) totalIda += t.getValorPagar();
        float totalVuelta = 0f;
        for (PasajeTicket t : ticketsVuelta) totalVuelta += t.getValorPagar();
        float totalGlobal = (totalIda + totalVuelta) * 0.9f;
        myCaja.registrarVenta(totalGlobal);
        return true;
    }

    // ============================================================
    // RF4 — Cancelación de salida
    // ============================================================

    public java.util.List<String> cancelarSalida(String idSalida, boolean reembolsar) {
        Salida salida = getSalidaPorId(idSalida);
        if (salida == null) return new java.util.ArrayList<>();

        salida.setEstado(Salida.CANCELADA);

        java.util.List<PasajeTicket> ticketsAfectados = new java.util.ArrayList<>();
        for (PasajeTicket t : myTickets) {
            if (t.getMySalida().getIdSalida().equals(idSalida) && PasajeTicket.VIGENTE.equals(t.getEstado())) {
                ticketsAfectados.add(t);
            }
        }

        Puesto[] puestosSalida = salida.getMyPuestos();
        java.util.List<String> resultados = new java.util.ArrayList<>();
        int tktIndex = 1;

        for (PasajeTicket ticket : ticketsAfectados) {
            int numPuesto = ticket.getPuesto();
            if (numPuesto >= 1 && numPuesto <= puestosSalida.length) {
                puestosSalida[numPuesto - 1].setMyPasajero(null);
            }

            StringBuilder r = new StringBuilder();
            String id = "TKT-" + String.format("%05d", tktIndex++);
            String pasajero = ticket.getMyPasajero().getCedula().length() > 6
                ? ticket.getMyPasajero().getCedula().substring(0, 6) + "..."
                : ticket.getMyPasajero().getCedula();

            if (reembolsar) {
                ticket.setEstado(PasajeTicket.REEMBOLSADO);
                myCaja.registrarReembolso(ticket.getValorPagar());
                r.append(id).append("  Pasajero ").append(pasajero)
                 .append("  Silla ").append(String.format("%02d", ticket.getPuesto()))
                 .append("  -> REEMBOLSADO");
            } else {
                String res = reprogramarTicketConResultado(ticket);
                r.append(id).append("  Pasajero ").append(pasajero)
                 .append("  Silla ").append(String.format("%02d", ticket.getPuesto()));
                if (res != null) {
                    r.append("  -> ").append(res);
                } else {
                    r.append("  -> REEMBOLSADO (sin cupo)");
                }
            }
            resultados.add(r.toString());
        }

        return resultados;
    }

    private String reprogramarTicketConResultado(PasajeTicket ticket) {
        Salida salidaOriginal = ticket.getMySalida();
        Salida nuevaSalida = buscarSalidaProxima(salidaOriginal);

        if (nuevaSalida != null) {
            Puesto[] puestos = nuevaSalida.getMyPuestos();
            for (int i = 0; i < puestos.length; i++) {
                if (puestos[i].estaLibre()) {
                    puestos[i].setMyPasajero(ticket.getMyPasajero());
                    ticket.setMySalida(nuevaSalida);
                    ticket.setPuesto(i + 1);
                    return "REPROGRAMADO a " + nuevaSalida.getIdSalida() + " Silla " + String.format("%02d", i + 1);
                }
            }
        }

        // Si no se encontró salida válida o no hay cupo: reembolso automático
        ticket.setEstado(PasajeTicket.REEMBOLSADO);
        myCaja.registrarReembolso(ticket.getValorPagar());
        return null; // señal de reembolso
    }

    public Salida buscarSalidaProxima(Salida salidaCancelada) {
        String codigoRuta = salidaCancelada.getMyRuta().getCodigo();
        LocalDateTime fechaRef = salidaCancelada.getFecha();

        Salida candidata = null;
        for (Salida s : mySalidas) {
            if (s.getMyRuta().getCodigo().equals(codigoRuta)
                && Salida.PROGRAMADA.equals(s.getEstado())
                && s.getFecha().isAfter(fechaRef)) {

                for (Puesto p : s.getMyPuestos()) {
                    if (p.estaLibre()) {
                        if (candidata == null || s.getFecha().isBefore(candidata.getFecha())) {
                            candidata = s;
                        }
                        break;
                    }
                }
            }
        }
        return candidata;
    }

    // ============================================================
    // RF5 — Finalización de salida
    // ============================================================

    public boolean finalizarSalida(String idSalida) {
        Salida salida = getSalidaPorId(idSalida);
        if (salida == null) return false;
        if (!Salida.PROGRAMADA.equals(salida.getEstado()) && !Salida.EN_RUTA.equals(salida.getEstado())) {
            return false;
        }
        if (!salida.puedeFinalizarse()) {
            return false;
        }
        salida.setEstado(Salida.FINALIZADA);
        return true;
    }

    // ============================================================
    // RF6 — Validación de salida efectiva
    // ============================================================

    public boolean esSalidaEfectiva(String idSalida) {
        Salida salida = getSalidaPorId(idSalida);
        if (salida == null) return false;
        return salida.esSalidaEfectiva(myTickets);
    }

    // ============================================================
    // RF7 — Reportes (delegados a CajaVenta)
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

    private boolean conductorExiste(String cedula) {
        return getConductorPorCedula(cedula) != null;
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

    public Conductor getConductorPorCedula(String cedula) {
        for (Conductor c : myConductores) {
            if (c.getCedula().equals(cedula)) return c;
        }
        return null;
    }

    public Pasajero getPasajeroPorCedula(String cedula) {
        for (Pasajero p : myPasajeros) {
            if (p.getCedula().equals(cedula)) return p;
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
        // Rutas de retorno (inversas)
        myRutas.add(new Ruta("R05", "Bucaramanga", "Cúcuta", 80000f));
        myRutas.add(new Ruta("R06", "Bogotá", "Cúcuta", 160000f));
        myRutas.add(new Ruta("R07", "Medellín", "Cúcuta", 180000f));
        myRutas.add(new Ruta("R08", "Cartagena", "Cúcuta", 220000f));

        myBuses.add(new BusTipoNormal("KAA-101", "DISPONIBLE", 40));
        myBuses.add(new BusTipoEjecutivo("KBB-202", "DISPONIBLE", 30));
        myBuses.add(new BusTipoNormal("KCC-303", "DISPONIBLE", 40));
        myBuses.add(new BusTipoEjecutivo("KDD-404", "DISPONIBLE", 30));
        myBuses.add(new BusTipoNormal("KEE-505", "MANTENIMIENTO", 40));
        myBuses.add(new BusTipoNormal("KFF-606", "DISPONIBLE", 30));

        mySalidas.add(new Salida("S001", getRutaPorCodigo("R01"), parseFecha("15/03/2026 06:00"), getBusPorPlaca("KAA-101"), "PROGRAMADA"));
        mySalidas.add(new Salida("S002", getRutaPorCodigo("R01"), parseFecha("15/03/2026 14:00"), getBusPorPlaca("KBB-202"), "PROGRAMADA"));
        mySalidas.add(new Salida("S003", getRutaPorCodigo("R02"), parseFecha("16/03/2026 07:00"), getBusPorPlaca("KCC-303"), "PROGRAMADA"));
        mySalidas.add(new Salida("S004", getRutaPorCodigo("R02"), parseFecha("16/03/2026 20:00"), getBusPorPlaca("KDD-404"), "PROGRAMADA"));
        mySalidas.add(new Salida("S005", getRutaPorCodigo("R03"), parseFecha("17/03/2026 05:30"), getBusPorPlaca("KFF-606"), "PROGRAMADA"));
        mySalidas.add(new Salida("S006", getRutaPorCodigo("R03"), parseFecha("17/03/2026 18:00"), getBusPorPlaca("KAA-101"), "PROGRAMADA"));
        mySalidas.add(new Salida("S007", getRutaPorCodigo("R04"), parseFecha("18/03/2026 06:30"), getBusPorPlaca("KCC-303"), "PROGRAMADA"));
        mySalidas.add(new Salida("S008", getRutaPorCodigo("R04"), parseFecha("18/03/2026 19:30"), getBusPorPlaca("KBB-202"), "PROGRAMADA"));
        // Salidas de retorno
        mySalidas.add(new Salida("S009", getRutaPorCodigo("R05"), parseFecha("16/03/2026 08:00"), getBusPorPlaca("KCC-303"), "PROGRAMADA"));
        mySalidas.add(new Salida("S010", getRutaPorCodigo("R05"), parseFecha("16/03/2026 16:00"), getBusPorPlaca("KBB-202"), "PROGRAMADA"));
        mySalidas.add(new Salida("S011", getRutaPorCodigo("R06"), parseFecha("17/03/2026 09:00"), getBusPorPlaca("KDD-404"), "PROGRAMADA"));
        mySalidas.add(new Salida("S012", getRutaPorCodigo("R06"), parseFecha("17/03/2026 22:00"), getBusPorPlaca("KAA-101"), "PROGRAMADA"));
        mySalidas.add(new Salida("S013", getRutaPorCodigo("R07"), parseFecha("18/03/2026 07:30"), getBusPorPlaca("KFF-606"), "PROGRAMADA"));
        mySalidas.add(new Salida("S014", getRutaPorCodigo("R08"), parseFecha("19/03/2026 08:00"), getBusPorPlaca("KDD-404"), "PROGRAMADA"));

        // Conductores base
        myConductores.add(new Conductor("1001", "Juan P\u00e9rez", "Calle 10 #5-20", "juan@copetran.com", "3001112233", 2500000f));
        myConductores.add(new Conductor("1002", "Carlos G\u00f3mez", "Av. 3 #12-45", "carlos@copetran.com", "3102223344", 2300000f));
        myConductores.add(new Conductor("1003", "Andr\u00e9s L\u00f3pez", "Cra. 7 #8-15", "andres@copetran.com", "3203334455", 2400000f));
    }
}

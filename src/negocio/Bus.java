package negocio;

public abstract class Bus {
    public static final String DISPONIBLE = "DISPONIBLE";
    public static final String EN_RUTA = "EN_RUTA";
    public static final String MANTENIMIENTO = "MANTENIMIENTO";

    protected String placa;
    protected String estado;
    protected Puesto[] myPuestos;
    protected Conductor myConductor;

    public Bus(String placa, String estado, int capacidad) {
        this.placa = placa;
        this.estado = estado;
        this.myPuestos = new Puesto[capacidad];
        inicializarPuestos();
    }

    private void inicializarPuestos() {
        char fila = 'A';
        int puestosPorFila = 4;
        for (int i = 0; i < myPuestos.length; i++) {
            int numFila = i / puestosPorFila;
            if (numFila > 0 && numFila % 10 == 0) {
                fila++;
            }
            myPuestos[i] = new Puesto(fila, i + 1);
        }
    }

    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Puesto[] getMyPuestos() { return myPuestos; }

    public int getCapacidad() {
        return myPuestos.length;
    }

    public Conductor getMyConductor() { return myConductor; }
    public void setMyConductor(Conductor myConductor) { this.myConductor = myConductor; }

    public abstract int[] mostrarPuestosLibres();
}

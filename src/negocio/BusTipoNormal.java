package negocio;

public class BusTipoNormal extends Bus {

    public BusTipoNormal(String placa, String estado, int capacidad) {
        super(placa, estado, capacidad);
    }

    @Override
    public int[] mostrarPuestosLibres() {
        int count = 0;
        for (Puesto p : myPuestos) {
            if (p.estaLibre()) count++;
        }
        int[] libres = new int[count];
        int idx = 0;
        for (int i = 0; i < myPuestos.length; i++) {
            if (myPuestos[i].estaLibre()) {
                libres[idx++] = i + 1;
            }
        }
        return libres;
    }
}

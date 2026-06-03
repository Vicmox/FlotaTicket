package negocio;

public class Puesto {
    private char fila;
    private int numero;
    private Pasajero myPasajero;

    public Puesto(char fila, int numero) {
        this.fila = fila;
        this.numero = numero;
        this.myPasajero = null;
    }

    public char getFila() { return fila; }
    public void setFila(char fila) { this.fila = fila; }

    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }

    public Pasajero getMyPasajero() { return myPasajero; }
    public void setMyPasajero(Pasajero myPasajero) { this.myPasajero = myPasajero; }

    public boolean estaLibre() {
        return myPasajero == null;
    }
}

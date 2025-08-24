public class Salon {
    public enum TipoSalon { PEQUENO, MEDIANO, GRANDE }
//identificador 
    private final int numero;                
    private final TipoSalon tipo;
    private final int capacidadMaxima;
    private final double costoHora;

    public Salon(int numero, TipoSalon tipo, int capacidadMaxima, double costoHora) {
        this.numero = numero;
        this.tipo = tipo;
        this.capacidadMaxima = capacidadMaxima;
        this.costoHora = costoHora;
    }

    public int getNumero() { return numero; }
    public TipoSalon getTipo() { return tipo; }
    public int getCapacidadMaxima() { return capacidadMaxima; }
    public double getCostoHora() { return costoHora; }
}

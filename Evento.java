public class Evento {
    public enum TipoEvento { SOCIAL, EMPRESARIAL, CONFERENCIA, GALA, OTRO }

    private final String encargado;
    private final String nombreEvento;
    private final TipoEvento tipoEvento;
    private final boolean esVIP;          
    private final int asistentes;

    public Evento(String encargado, String nombreEvento, TipoEvento tipoEvento, boolean esVIP, int asistentes) {
        this.encargado = encargado;
        this.nombreEvento = nombreEvento;
        this.tipoEvento = tipoEvento;
        this.esVIP = esVIP;
        this.asistentes = asistentes;
    }

    public String getEncargado() { return encargado; }
    public String getNombreEvento() { return nombreEvento; }
    public TipoEvento getTipoEvento() { return tipoEvento; }
    public boolean isVIP() { return esVIP; }
    public int getAsistentes() { return asistentes; }
}

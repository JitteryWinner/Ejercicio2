import java.time.LocalDate;
import java.time.LocalTime;

public class SolicitudReserva {
    private final Evento evento;
    private final LocalDate fecha;
    private final LocalTime horaInicio;
    private final LocalTime horaFin;
    private final boolean depositoPagado;

    public SolicitudReserva(Evento evento, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, boolean depositoPagado) {
        this.evento = evento;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.depositoPagado = depositoPagado;
    }

    public Evento getEvento() { return evento; }
    public LocalDate getFecha() { return fecha; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    public boolean isDepositoPagado() { return depositoPagado; }
}

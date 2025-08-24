public class Reserva {
    private final SolicitudReserva solicitud;
    private final Salon salonAsignado;

    public Reserva(SolicitudReserva solicitud, Salon salonAsignado) {
        this.solicitud = solicitud;
        this.salonAsignado = salonAsignado;
    }

    public SolicitudReserva getSolicitud() { return solicitud; }
    public Salon getSalonAsignado() { return salonAsignado; }
}

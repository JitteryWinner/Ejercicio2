import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Scanner;

public class Principal {
    private static final Scanner sc = new Scanner(System.in);

    //arreglos
    private static final RepositorioSalones repoSalones = new RepositorioSalones(50);
    private static final RepositorioSolicitudes repoSolicitudes = new RepositorioSolicitudes(200);
    private static final RepositorioReservas repoReservas = new RepositorioReservas(200);
    private static final ListaEspera listaEspera = new ListaEspera(200);

    private static final MotorAsignacion motor = new MotorAsignacion(new ReglaAsignacion[] {
            new ReglaDepositoPagado(),
            new ReglaCapacidadSuficiente(),
            new ReglaCompatibilidadTipo(),
            new ReglaNoSolapamiento()
    });

    public static void main(String[] args) {
        cargarSalonesIniciales();

        boolean continuar = true;
        do {
            mostrarMenu();
            int opcion = leerEntero("Seleccione una opcion: ");
            switch (opcion) {
                case 1 -> registrarSolicitud();
                case 2 -> asignarSalon();
                case 3 -> listarSalones();
                case 4 -> listarSolicitudes();
                case 5 -> listarReservas();
                case 6 -> listarEspera();
                case 0 -> {
                    System.out.println("Gracias por usar nuestro programa");
                    continuar = false;
                }
                default -> System.out.println("Opcion invalida");
            }
        } while (continuar);
    }

    //Ayuda para ingresar datos
    private static int leerEntero(String msg) {
        System.out.print(msg);
        while (!sc.hasNextInt()) { System.out.print("Ingrese un numero valido: "); sc.next(); }
        int val = sc.nextInt(); sc.nextLine(); return val;
    }
    private static String leerLinea(String msg) { System.out.print(msg); return sc.nextLine().trim(); }
    private static LocalDate leerFecha(String msg) {
        System.out.print(msg + " (YYYY-MM-DD): ");
        while (true) { try { return LocalDate.parse(sc.nextLine().trim()); } catch (Exception e) { System.out.print("Fecha invalida. Formato YYYY-MM-DD: "); } }
    }
    private static LocalTime leerHora(String msg) {
        System.out.print(msg + " (HH:MM 24h): ");
        while (true) { try { return LocalTime.parse(sc.nextLine().trim()); } catch (Exception e) { System.out.print("Hora invalida. Formato HH:MM: "); } }
    }

    //Cargos
    private static void cargarSalonesIniciales() {
        repoSalones.agregar(new Salon(101, Salon.TipoSalon.PEQUENO, 40, 30.0));
        repoSalones.agregar(new Salon(102, Salon.TipoSalon.MEDIANO, 100, 55.0));
        repoSalones.agregar(new Salon(201, Salon.TipoSalon.GRANDE, 300, 120.0));
        repoSalones.agregar(new Salon(202, Salon.TipoSalon.MEDIANO, 120, 60.0));
        System.out.println("Se cargaron 4 salones iniciales.");
    }

    //Menu
    private static void mostrarMenu() {
        System.out.println("\n Centro de Eventos - Sistema de Reservas ");
        System.out.println("1) Registrar solicitud de reserva");
        System.out.println("2) Asignar salon a una solicitud");
        System.out.println("3) Listar salones");
        System.out.println("4) Listar solicitudes");
        System.out.println("5) Listar reservas confirmadas");
        System.out.println("6) Listar solicitudes en espera");
        System.out.println("0) Salir");
    }

    //Opciones 
    private static void registrarSolicitud() {
        System.out.println("\nNueva Solicitud ");
        String encargado = leerLinea("Encargado: ");
        String nombre = leerLinea("Nombre del evento: ");

        System.out.println("Tipos de evento: 1) SOCIAL  2) EMPRESARIAL  3) CONFERENCIA  4) GALA  5) OTRO");
        int te = leerEntero("Seleccione tipo: ");
        Evento.TipoEvento tipoEvento = switch (te) {
            case 1 -> Evento.TipoEvento.SOCIAL;
            case 2 -> Evento.TipoEvento.EMPRESARIAL;
            case 3 -> Evento.TipoEvento.CONFERENCIA;
            case 4 -> Evento.TipoEvento.GALA;
            default -> Evento.TipoEvento.OTRO;
        };
        boolean esVIP = leerLinea("Es un evento VIP? (s/n): ").equalsIgnoreCase("s");
        int asistentes = leerEntero("Cantidad de asistentes: ");

        LocalDate fecha = leerFecha("Fecha del evento");
        LocalTime hIni = leerHora("Hora de inicio");
        LocalTime hFin = leerHora("Hora de fin");
        while (!hFin.isAfter(hIni)) { System.out.println("La hora de fin debe ser posterior a la de inicio."); hFin = leerHora("Hora de fin"); }

        boolean deposito = leerLinea("Deposito pagado? (s/n): ").equalsIgnoreCase("s");

        Evento evento = new Evento(encargado, nombre, tipoEvento, esVIP, asistentes);
        SolicitudReserva solicitud = new SolicitudReserva(evento, fecha, hIni, hFin, deposito);

        if (repoSolicitudes.agregar(solicitud)) {
            System.out.println("Solicitud registrada (pendiente de asignacion).");
        } else {
            System.out.println("No se pudo registrar la solicitud (capacidad llena).");
        }
    }

    private static void asignarSalon() {
        if (repoSolicitudes.size() == 0) { System.out.println("No hay solicitudes registradas."); return; }

        System.out.println("\n Asignar Salon ");
        for (int i = 0; i < repoSolicitudes.size(); i++) {
            SolicitudReserva s = repoSolicitudes.getTodos()[i];
            System.out.printf("%d) %s - %s - %s %s-%s  VIP:%s  Deposito:%s  Asist:%d%n",
                    i, s.getEvento().getEncargado(), s.getEvento().getNombreEvento(),
                    s.getFecha(), s.getHoraInicio(), s.getHoraFin(),
                    s.getEvento().isVIP() ? "Si" : "No",
                    s.isDepositoPagado() ? "Si" : "No",
                    s.getEvento().getAsistentes());
        }

        int idx = leerEntero("Seleccione el indice de la solicitud: ");
        if (idx < 0 || idx >= repoSolicitudes.size()) { System.out.println("indice invalido."); return; }

        SolicitudReserva solicitud = repoSolicitudes.getTodos()[idx];
        String[] outError = new String[1];
        Reserva reserva = motor.intentarAsignar(solicitud, repoSalones, repoReservas, outError);

        if (reserva != null) {
            if (repoReservas.agregar(reserva)) {
                System.out.println("Reserva confirmada");
                System.out.println("Salon asignado: " + reserva.getSalonAsignado().getNumero() +
                        " (" + reserva.getSalonAsignado().getTipo() + "), Fecha: " + solicitud.getFecha() +
                        ", " + solicitud.getHoraInicio() + "-" + solicitud.getHoraFin());
                long horas = java.time.Duration.between(solicitud.getHoraInicio(), solicitud.getHoraFin()).toHours();
                if (horas == 0) horas = 1;
                double costo = horas * reserva.getSalonAsignado().getCostoHora();
                System.out.printf("Costo estimado: Q%.2f (%d hora(s) x Q%.2f)%n",
                        costo, horas, reserva.getSalonAsignado().getCostoHora());
            } else {
                System.out.println("No se pudo guardar la reserva (capacidad del arreglo llena).");
            }
        } else {
            System.out.println(outError[0]);
            if (listaEspera.encolar(solicitud)) {
                System.out.println("La solicitud fue añadida a la Lista de espera.");
            } else {
                System.out.println("La lista de espera esta llena. No se pudo encolar.");
            }
        }
    }

    private static void listarSalones() {
        System.out.println("\n Salones ");
        if (repoSalones.size() == 0) { System.out.println("No hay salones cargados."); return; }
        for (int i = 0; i < repoSalones.size(); i++) {
            Salon s = repoSalones.getTodos()[i];
            System.out.printf("Numero %d  | Tipo: %s | Cap: %d | Q/h: %.2f%n",
                    s.getNumero(), s.getTipo(), s.getCapacidadMaxima(), s.getCostoHora());
        }
    }

    private static void listarSolicitudes() {
        System.out.println("\n Solicitudes Registradas ");
        if (repoSolicitudes.size() == 0) { System.out.println("No hay solicitudes."); return; }
        for (int i = 0; i < repoSolicitudes.size(); i++) {
            SolicitudReserva s = repoSolicitudes.getTodos()[i];
            System.out.printf("%d) %s | %s | %s %s-%s | VIP:%s | Deposito:%s | Asist:%d%n",
                    i, s.getEvento().getEncargado(), s.getEvento().getNombreEvento(),
                    s.getFecha(), s.getHoraInicio(), s.getHoraFin(),
                    s.getEvento().isVIP() ? "Si" : "No",
                    s.isDepositoPagado() ? "Si" : "No",
                    s.getEvento().getAsistentes());
        }
    }

    private static void listarReservas() {
        System.out.println("\n Reservas Confirmadas ");
        if (repoReservas.size() == 0) { System.out.println("No hay reservas confirmadas."); return; }
        for (int i = 0; i < repoReservas.size(); i++) {
            Reserva r = repoReservas.getTodos()[i];
            SolicitudReserva s = r.getSolicitud();
            System.out.printf("Salon %d (%s) | %s | %s %s-%s | Encargado: %s | Asist:%d%n",
                    r.getSalonAsignado().getNumero(), r.getSalonAsignado().getTipo(),
                    s.getEvento().getNombreEvento(),
                    s.getFecha(), s.getHoraInicio(), s.getHoraFin(),
                    s.getEvento().getEncargado(), s.getEvento().getAsistentes());
        }
    }

    private static void listarEspera() {
        System.out.println("\n Lista de Espera ");
        if (listaEspera.size() == 0) { System.out.println("No hay solicitudes en espera."); return; }
        for (int i = 0; i < listaEspera.size(); i++) {
            SolicitudReserva s = listaEspera.getTodos()[i];
            System.out.printf("%d) %s | %s | %s %s-%s | VIP:%s | Deposito:%s | Asist:%d%n",
                    i, s.getEvento().getEncargado(), s.getEvento().getNombreEvento(),
                    s.getFecha(), s.getHoraInicio(), s.getHoraFin(),
                    s.getEvento().isVIP() ? "Si" : "No",
                    s.isDepositoPagado() ? "Si" : "No",
                    s.getEvento().getAsistentes());
        }
    }

    //Validacion

    //Reglas
    private interface ReglaAsignacion {
        String validar(Salon salon, SolicitudReserva solicitud, Reserva[] reservasActuales, int reservasCount);
    }

    private static class ReglaDepositoPagado implements ReglaAsignacion {
        public String validar(Salon salon, SolicitudReserva solicitud, Reserva[] reservasActuales, int reservasCount) {
            return solicitud.isDepositoPagado() ? null : "Debe registrar el deposito para confirmar la reserva.";
        }
    }

    private static class ReglaCapacidadSuficiente implements ReglaAsignacion {
        public String validar(Salon salon, SolicitudReserva solicitud, Reserva[] reservasActuales, int reservasCount) {
            return (solicitud.getEvento().getAsistentes() <= salon.getCapacidadMaxima()) ? null
                    : "La capacidad del salon es insuficiente.";
        }
    }

    private static class ReglaCompatibilidadTipo implements ReglaAsignacion {
        public String validar(Salon salon, SolicitudReserva solicitud, Reserva[] reservasActuales, int reservasCount) {
            if (salon.getTipo() == Salon.TipoSalon.GRANDE && !solicitud.getEvento().isVIP()) {
                return "Los salones GRANDES solo pueden reservarse para eventos VIP.";
            }
            return null;
        }
    }

    private static class ReglaNoSolapamiento implements ReglaAsignacion {
        private static boolean solapan(LocalTime aIni, LocalTime aFin, LocalTime bIni, LocalTime bFin) {
            return aIni.isBefore(bFin) && bIni.isBefore(aFin);
        }
        public String validar(Salon salon, SolicitudReserva solicitud, Reserva[] reservasActuales, int reservasCount) {
            for (int i = 0; i < reservasCount; i++) {
                Reserva r = reservasActuales[i];
                if (r == null) continue;
                if (r.getSalonAsignado().getNumero() == salon.getNumero()
                        && r.getSolicitud().getFecha().equals(solicitud.getFecha())) {
                    if (solapan(solicitud.getHoraInicio(), solicitud.getHoraFin(),
                                r.getSolicitud().getHoraInicio(), r.getSolicitud().getHoraFin())) {
                        return "El salon ya esta reservado en ese horario.";
                    }
                }
            }
            return null;
        }
    }

    //Motor
    private static class MotorAsignacion {
        private final ReglaAsignacion[] reglas;
        public MotorAsignacion(ReglaAsignacion[] reglas) { this.reglas = reglas; }

        public Reserva intentarAsignar(SolicitudReserva solicitud, RepositorioSalones repoSalones,
                                       RepositorioReservas repoReservas, String[] outError) {
            for (int i = 0; i < repoSalones.size(); i++) {
                Salon salon = repoSalones.getTodos()[i];
                String msg = validar(salon, solicitud, repoReservas.getTodos(), repoReservas.size());
                if (msg == null) return new Reserva(solicitud, salon);
            }
            if (outError != null && outError.length > 0) outError[0] = "No hay salon disponible que cumpla todas las reglas.";
            return null;
        }

        private String validar(Salon salon, SolicitudReserva solicitud, Reserva[] reservas, int rCount) {
            for (ReglaAsignacion r : reglas) {
                String e = r.validar(salon, solicitud, reservas, rCount);
                if (e != null) return e;
            }
            return null;
        }
    }

    //Repositorios
    private static class RepositorioSalones {
        private final Salon[] salones; private int count = 0;
        public RepositorioSalones(int capacidad) { salones = new Salon[capacidad]; }
        public boolean agregar(Salon s) {
            if (count >= salones.length) return false;
            for (int i = 0; i < count; i++) if (salones[i].getNumero() == s.getNumero()) return false;
            salones[count++] = s; return true;
        }
        public Salon[] getTodos() { return salones; }
        public int size() { return count; }
    }

    private static class RepositorioSolicitudes {
        private final SolicitudReserva[] solicitudes; private int count = 0;
        public RepositorioSolicitudes(int capacidad) { solicitudes = new SolicitudReserva[capacidad]; }
        public boolean agregar(SolicitudReserva s) { if (count >= solicitudes.length) return false; solicitudes[count++] = s; return true; }
        public SolicitudReserva[] getTodos() { return solicitudes; }
        public int size() { return count; }
    }

    private static class RepositorioReservas {
        private final Reserva[] reservas; private int count = 0;
        public RepositorioReservas(int capacidad) { reservas = new Reserva[capacidad]; }
        public boolean agregar(Reserva r) { if (count >= reservas.length) return false; reservas[count++] = r; return true; }
        public Reserva[] getTodos() { return reservas; }
        public int size() { return count; }
    }

    private static class ListaEspera {
        private final SolicitudReserva[] espera; private int count = 0;
        public ListaEspera(int capacidad) { espera = new SolicitudReserva[capacidad]; }
        public boolean encolar(SolicitudReserva s) { if (count >= espera.length) return false; espera[count++] = s; return true; }
        public SolicitudReserva[] getTodos() { return espera; }
        public int size() { return count; }
    }
}

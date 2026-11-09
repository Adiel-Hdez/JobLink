package sv.edu.itca.joblink.repository;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import sv.edu.itca.joblink.data.remote.SupabaseClient;
import sv.edu.itca.joblink.data.remote.dto.CategoriaOficioDto;
import sv.edu.itca.joblink.data.remote.dto.ConversacionDto;
import sv.edu.itca.joblink.data.remote.dto.MensajeDto;
import sv.edu.itca.joblink.data.remote.dto.PerfilTrabajadorDto;
import sv.edu.itca.joblink.data.remote.dto.SolicitudTrabajoDto;
import sv.edu.itca.joblink.data.remote.dto.UsuarioDto;
import sv.edu.itca.joblink.model.ChatConversation;
import sv.edu.itca.joblink.model.ChatMessage;
import sv.edu.itca.joblink.model.TradeCategory;
import sv.edu.itca.joblink.model.User;
import sv.edu.itca.joblink.model.WorkOrder;

public class JobLinkRepository {
    private static final String TAG = "JobLinkRepository";
    private static JobLinkRepository instance;

    private User currentUser;
    private final List<TradeCategory> categories = new ArrayList<>();
    private final List<User> workers = new ArrayList<>();
    private final List<WorkOrder> workOrders = new ArrayList<>();
    private final List<ChatConversation> conversations = new ArrayList<>();
    private final Map<String, List<ChatMessage>> chatMessagesMap = new HashMap<>();

    public interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    private JobLinkRepository() {
        initCategories();
        initWorkers();
        initWorkOrders();

        // Default current user: Moisés (Worker)
        currentUser = workers.get(0);

        // Sync with Supabase on startup
        fetchCategoriesFromSupabase(null);
        fetchWorkersFromSupabase(null);
    }

    public static synchronized JobLinkRepository getInstance() {
        if (instance == null) {
            instance = new JobLinkRepository();
        }
        return instance;
    }

    private void initCategories() {
        categories.add(new TradeCategory("c1", "Albañilería y Enchapado", "Construcción y Mejoras del Hogar", "Muros, aceras, cerámica y remodelaciones"));
        categories.add(new TradeCategory("c2", "Carpintería y Ebanistería", "Construcción y Mejoras del Hogar", "Muebles a medida, puertas, closets"));
        categories.add(new TradeCategory("c3", "Balconería y Soldadura", "Construcción y Mejoras del Hogar", "Portones, balcones y estructuras metálicas"));
        categories.add(new TradeCategory("c4", "Cielo Falso y Tabla Yeso", "Construcción y Mejoras del Hogar", "Gypsum, cielo falso e iluminación empotrada"));
        categories.add(new TradeCategory("c5", "Pintura e Impermeabilización", "Construcción y Mejoras del Hogar", "Pintura de fachadas, impermeabilización de techos"));

        categories.add(new TradeCategory("c6", "Fontanería", "Instalaciones y Mantenimiento Técnico", "Reparación de fugas, cañerías, sanitarios y tanques"));
        categories.add(new TradeCategory("c7", "Electricidad Residencial", "Instalaciones y Mantenimiento Técnico", "Cableado, cajas térmicas, ventiladores y duchas"));
        categories.add(new TradeCategory("c8", "Aire Acondicionado", "Instalaciones y Mantenimiento Técnico", "Instalación y mantenimiento de minisplits"));
        categories.add(new TradeCategory("c9", "Reparación de Línea Blanca", "Instalaciones y Mantenimiento Técnico", "Refrigeradoras, lavadoras y cocinas a domicilio"));
        categories.add(new TradeCategory("c10", "Cerrajería", "Instalaciones y Mantenimiento Técnico", "Aperturas de emergencia y cambio de chapas"));

        categories.add(new TradeCategory("c11", "Costura y Sastrería", "Textiles, Calzado y Tapicería", "Prendas a medida, uniformes y ajustes de ropa"));
        categories.add(new TradeCategory("c12", "Tapicería", "Textiles, Calzado y Tapicería", "Restauración de juegos de sala y asientos de autos"));
        categories.add(new TradeCategory("c13", "Zapatería Artesanal", "Textiles, Calzado y Tapicería", "Reparación de calzado, bolsos y mochilas"));

        categories.add(new TradeCategory("c14", "Jardinería y Poda", "Exteriores y Servicios del Hogar", "Corte de grama, diseño de jardines y poda"));
        categories.add(new TradeCategory("c15", "Limpieza Especializada", "Exteriores y Servicios del Hogar", "Lavado en seco de muebles, alfombras y cisternas"));
        categories.add(new TradeCategory("c16", "Control de Plagas", "Exteriores y Servicios del Hogar", "Fumigación de casas y tratamiento de termitas"));
    }

    private void initWorkers() {
        User moises = new User("w_moises", "Moisés Adiel Rivera Hernández", "adielrhernandez22@gmail.com", User.Role.WORKER, 
                "Albañilería y Enchapado", "Construcción y Mejoras del Hogar", "Santa Ana, El Salvador", 
                "Especialista profesional en construcción de muros, cerámica, enchapados y remodelaciones en Santa Ana.", 
                5.0, 15, true, "+503 7000-2222");
        moises.setPremium(true);

        User carlos = new User("w1", "Carlos G.", "carlos@joblink.sv", User.Role.WORKER, 
                "Carpintero", "Carpintería y Ebanistería", "Santa Ana, El Salvador", 
                "Experto ebanista con más de 10 años elaborando muebles finos y remodelaciones en madera.", 
                4.8, 35, true, "+503 7123-4567");

        User marta = new User("w2", "Marta L.", "marta@joblink.sv", User.Role.WORKER, 
                "Costurera", "Costura y Sastrería", "Santa Ana Centro", 
                "Descripción del Taller de Costura: Especialistas en vestidos de gala, trajes a medida y confección de alta calidad.", 
                4.8, 42, true, "+503 7234-5678");

        User pedro = new User("w3", "Pedro R.", "pedro@joblink.sv", User.Role.WORKER, 
                "Albañil y Enchapador", "Albañilería y Enchapado", "Santa Ana, Col. El Palmar", 
                "Construcción de muros, pegado de cerámica, remodelación de baños y cocinas.", 
                4.9, 50, false, "+503 7345-6789");

        User ana = new User("w4", "Ana M.", "ana@joblink.sv", User.Role.WORKER, 
                "Técnica en Línea Blanca", "Reparación de Línea Blanca", "Santa Ana, IVU", 
                "Diagnóstico y reparación a domicilio de refrigeradoras, cocinas y lavadoras.", 
                4.7, 28, true, "+503 7456-7890");

        workers.add(moises);
        workers.add(carlos);
        workers.add(marta);
        workers.add(pedro);
        workers.add(ana);
    }

    private void initWorkOrders() {
        workOrders.add(new WorkOrder("wo1", "Mesa para Pedro", "Pedro R.", "Moisés Adiel Rivera Hernández", 50, "En Curso"));
        workOrders.add(new WorkOrder("wo2", "Reparación Silla", "Ana M.", "Moisés Adiel Rivera Hernández", 80, "En Curso"));
    }

    // --- REALTIME CHAT & SUPABASE INTEGRATION ---

    public void fetchConversacionesFromSupabase(RepositoryCallback<List<ChatConversation>> callback) {
        SupabaseClient.getApi().getConversaciones().enqueue(new Callback<List<ConversacionDto>>() {
            @Override
            public void onResponse(Call<List<ConversacionDto>> call, Response<List<ConversacionDto>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    conversations.clear();
                    for (ConversacionDto dto : response.body()) {
                        String name = "Contacto JobLink";
                        String trade = "Profesional";
                        for (User u : workers) {
                            if (u.getId() != null && (u.getId().equals(dto.trabajadorUsuarioId) || u.getId().equals(dto.clienteId))) {
                                name = u.getName();
                                trade = u.getProfession();
                                break;
                            }
                        }
                        conversations.add(new ChatConversation(
                                dto.id, name, trade, "Mensaje de conversación", "Ahora", 0
                        ));
                    }
                }
                if (callback != null) callback.onSuccess(conversations);
            }

            @Override
            public void onFailure(Call<List<ConversacionDto>> call, Throwable t) {
                if (callback != null) callback.onSuccess(conversations);
            }
        });
    }

    public void fetchMensajesFromSupabase(String conversationId, RepositoryCallback<List<ChatMessage>> callback) {
        String query = "eq." + conversationId;
        SupabaseClient.getApi().getMensajesByConversacion(query).enqueue(new Callback<List<MensajeDto>>() {
            @Override
            public void onResponse(Call<List<MensajeDto>> call, Response<List<MensajeDto>> response) {
                List<ChatMessage> list = new ArrayList<>();
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    for (MensajeDto dto : response.body()) {
                        boolean isMine = currentUser != null && currentUser.getId() != null && currentUser.getId().equals(dto.remitenteId);
                        String timeStr = dto.fechaEnvio != null && dto.fechaEnvio.length() >= 16 ? dto.fechaEnvio.substring(11, 16) : "10:00 AM";
                        list.add(new ChatMessage(String.valueOf(dto.id), dto.remitenteId, dto.contenido, timeStr, isMine));
                    }
                    chatMessagesMap.put(conversationId, list);
                    if (callback != null) callback.onSuccess(list);
                } else {
                    List<ChatMessage> local = getChatMessages(conversationId);
                    if (callback != null) callback.onSuccess(local);
                }
            }

            @Override
            public void onFailure(Call<List<MensajeDto>> call, Throwable t) {
                List<ChatMessage> local = getChatMessages(conversationId);
                if (callback != null) callback.onSuccess(local);
            }
        });
    }

    public void sendMensajeToSupabase(String conversationId, String text, RepositoryCallback<Boolean> callback) {
        String myId = currentUser != null && currentUser.getId() != null ? currentUser.getId() : "a1b2c3d4-e5f6-7890-1234-567890abcdef";
        String targetConvId = conversationId != null && conversationId.length() > 10 ? conversationId : "a1b2c3d4-e5f6-7890-1234-567890abcdef";

        MensajeDto dto = new MensajeDto(targetConvId, myId, text);

        // Add locally first for instant feedback
        List<ChatMessage> list = getChatMessages(conversationId);
        list.add(new ChatMessage("m_" + System.currentTimeMillis(), myId, text, "Ahora", true));

        SupabaseClient.getApi().sendMensaje(dto).enqueue(new Callback<List<MensajeDto>>() {
            @Override
            public void onResponse(Call<List<MensajeDto>> call, Response<List<MensajeDto>> response) {
                if (callback != null) callback.onSuccess(true);
            }

            @Override
            public void onFailure(Call<List<MensajeDto>> call, Throwable t) {
                if (callback != null) callback.onSuccess(true);
            }
        });
    }

    public void loginWithSupabase(String email, String password, RepositoryCallback<User> callback) {
        String emailQuery = "eq." + email.trim();
        SupabaseClient.getApi().getUsuarioByEmail(emailQuery).enqueue(new Callback<List<UsuarioDto>>() {
            @Override
            public void onResponse(Call<List<UsuarioDto>> call, Response<List<UsuarioDto>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    UsuarioDto u = response.body().get(0);
                    User.Role role = "trabajador".equalsIgnoreCase(u.rol) ? User.Role.WORKER : User.Role.CLIENT;
                    User user = new User(
                            u.id, u.nombreCompleto, u.email, role,
                            role == User.Role.WORKER ? "Albañilería y Enchapado" : "Cliente",
                            "General", u.ubicacion != null ? u.ubicacion : "Santa Ana",
                            "Usuario de JobLink", 5.0, 10, true,
                            u.telefono != null ? u.telefono : "+503 7000-0000"
                    );
                    currentUser = user;
                    if (callback != null) callback.onSuccess(user);
                } else {
                    User fallback = findLocalUserByEmail(email);
                    currentUser = fallback;
                    if (callback != null) callback.onSuccess(fallback);
                }
            }

            @Override
            public void onFailure(Call<List<UsuarioDto>> call, Throwable t) {
                User fallback = findLocalUserByEmail(email);
                currentUser = fallback;
                if (callback != null) callback.onSuccess(fallback);
            }
        });
    }

    private User findLocalUserByEmail(String email) {
        for (User w : workers) {
            if (w.getEmail().equalsIgnoreCase(email.trim())) {
                return w;
            }
        }
        return new User("usr_local", "Moisés Adiel Rivera Hernández", email, User.Role.WORKER, "Albañilería y Enchapado", "General", "Santa Ana", "Usuario de JobLink", 5.0, 5, true, "+503 7000-0000");
    }

    public void fetchCategoriesFromSupabase(RepositoryCallback<List<TradeCategory>> callback) {
        SupabaseClient.getApi().getCategoriasOficios().enqueue(new Callback<List<CategoriaOficioDto>>() {
            @Override
            public void onResponse(Call<List<CategoriaOficioDto>> call, Response<List<CategoriaOficioDto>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    categories.clear();
                    for (CategoriaOficioDto dto : response.body()) {
                        categories.add(new TradeCategory(
                                String.valueOf(dto.id),
                                dto.nombre,
                                "General",
                                dto.descripcion != null ? dto.descripcion : ""
                        ));
                    }
                    if (callback != null) callback.onSuccess(categories);
                }
            }

            @Override
            public void onFailure(Call<List<CategoriaOficioDto>> call, Throwable t) {
                if (callback != null) callback.onError(t.getMessage());
            }
        });
    }

    public void fetchWorkersFromSupabase(RepositoryCallback<List<User>> callback) {
        SupabaseClient.getApi().getPerfilesTrabajadores().enqueue(new Callback<List<PerfilTrabajadorDto>>() {
            @Override
            public void onResponse(Call<List<PerfilTrabajadorDto>> call, Response<List<PerfilTrabajadorDto>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    List<User> loadedWorkers = new ArrayList<>();
                    for (PerfilTrabajadorDto dto : response.body()) {
                        String name = dto.usuario != null && dto.usuario.nombreCompleto != null ? dto.usuario.nombreCompleto : "Trabajador";
                        String email = dto.usuario != null && dto.usuario.email != null ? dto.usuario.email : "user@joblink.sv";
                        String phone = dto.usuario != null && dto.usuario.telefono != null ? dto.usuario.telefono : "+503 7000-0000";
                        String location = dto.usuario != null && dto.usuario.ubicacion != null ? dto.usuario.ubicacion : "Santa Ana";
                        String trade = dto.categoria != null && dto.categoria.nombre != null ? dto.categoria.nombre : "Albañilería y Enchapado";

                        User w = new User(
                                dto.usuarioId != null ? dto.usuarioId : "w_" + dto.id,
                                name, email, User.Role.WORKER, trade, trade, location,
                                dto.descripcionProfesional != null ? dto.descripcionProfesional : "Servicio profesional en Santa Ana",
                                dto.calificacionPromedio != null ? dto.calificacionPromedio : 5.0,
                                12, true, phone
                        );
                        if (Boolean.TRUE.equals(dto.esPremium)) {
                            w.setPremium(true);
                        }
                        loadedWorkers.add(w);
                    }
                    if (!loadedWorkers.isEmpty()) {
                        workers.clear();
                        workers.addAll(loadedWorkers);
                    }
                    if (callback != null) callback.onSuccess(workers);
                }
            }

            @Override
            public void onFailure(Call<List<PerfilTrabajadorDto>> call, Throwable t) {
                if (callback != null) callback.onError(t.getMessage());
            }
        });
    }

    public void registerUserInSupabase(User user, String password, RepositoryCallback<User> callback) {
        String roleStr = user.getRole() == User.Role.WORKER ? "trabajador" : "cliente";
        UsuarioDto userDto = new UsuarioDto(
                user.getName(),
                user.getEmail(),
                password != null ? password : "defaultPassword123",
                user.getPhone() != null ? user.getPhone() : "+503 7000-0000",
                user.getLocation() != null ? user.getLocation() : "Santa Ana",
                roleStr
        );

        SupabaseClient.getApi().createUsuario(userDto).enqueue(new Callback<List<UsuarioDto>>() {
            @Override
            public void onResponse(Call<List<UsuarioDto>> call, Response<List<UsuarioDto>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    UsuarioDto created = response.body().get(0);
                    user.setId(created.id);
                    currentUser = user;

                    if (user.getRole() == User.Role.WORKER) {
                        PerfilTrabajadorDto profileDto = new PerfilTrabajadorDto(created.id, 1, user.getBio());
                        SupabaseClient.getApi().createPerfilTrabajador(profileDto).enqueue(new Callback<List<PerfilTrabajadorDto>>() {
                            @Override
                            public void onResponse(Call<List<PerfilTrabajadorDto>> c, Response<List<PerfilTrabajadorDto>> r) {
                                workers.add(user);
                                if (callback != null) callback.onSuccess(user);
                            }

                            @Override
                            public void onFailure(Call<List<PerfilTrabajadorDto>> c, Throwable t) {
                                workers.add(user);
                                if (callback != null) callback.onSuccess(user);
                            }
                        });
                    } else {
                        if (callback != null) callback.onSuccess(user);
                    }
                } else {
                    currentUser = user;
                    if (user.getRole() == User.Role.WORKER) workers.add(user);
                    if (callback != null) callback.onSuccess(user);
                }
            }

            @Override
            public void onFailure(Call<List<UsuarioDto>> call, Throwable t) {
                currentUser = user;
                if (user.getRole() == User.Role.WORKER) workers.add(user);
                if (callback != null) callback.onSuccess(user);
            }
        });
    }

    // Getters & Methods
    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User user) { this.currentUser = user; }
    public List<TradeCategory> getCategories() { return categories; }
    public List<User> getWorkers() { return workers; }
    public List<WorkOrder> getWorkOrders() { return workOrders; }
    public List<ChatConversation> getConversations() { return conversations; }

    public List<ChatMessage> getChatMessages(String conversationId) {
        if (!chatMessagesMap.containsKey(conversationId)) {
            List<ChatMessage> list = new ArrayList<>();
            chatMessagesMap.put(conversationId, list);
        }
        return chatMessagesMap.get(conversationId);
    }

    public void addChatMessage(String conversationId, String text, boolean isMine) {
        sendMensajeToSupabase(conversationId, text, null);
    }

    public void addWorkOrder(WorkOrder order) {
        workOrders.add(order);

        SolicitudTrabajoDto dto = new SolicitudTrabajoDto(
                currentUser.getId() != null ? currentUser.getId() : "a1b2c3d4-e5f6-7890-1234-567890abcdef",
                1,
                order.getTitle(),
                "Detalles de la solicitud desde el chat",
                50.0
        );

        SupabaseClient.getApi().createSolicitudTrabajo(dto).enqueue(new Callback<List<SolicitudTrabajoDto>>() {
            @Override
            public void onResponse(Call<List<SolicitudTrabajoDto>> call, Response<List<SolicitudTrabajoDto>> response) {}
            @Override
            public void onFailure(Call<List<SolicitudTrabajoDto>> call, Throwable t) {}
        });
    }
}
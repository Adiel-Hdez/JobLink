package sv.edu.itca.joblink.repository;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import sv.edu.itca.joblink.data.remote.SupabaseClient;
import sv.edu.itca.joblink.data.remote.dto.CategoriaOficioDto;
import sv.edu.itca.joblink.data.remote.dto.ConversacionDto;
import sv.edu.itca.joblink.data.remote.dto.MensajeDto;
import sv.edu.itca.joblink.data.remote.dto.PerfilTrabajadorDto;
import sv.edu.itca.joblink.data.remote.dto.PortafolioTrabajoDto;
import sv.edu.itca.joblink.data.remote.dto.SolicitudTrabajoDto;
import sv.edu.itca.joblink.data.remote.dto.UsuarioDto;
import sv.edu.itca.joblink.data.remote.dto.ValoracionDto;
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

    public static String ensureValidUuid(String id) {
        if (id == null || id.trim().isEmpty()) {
            return UUID.randomUUID().toString();
        }
        try {
            return UUID.fromString(id.trim()).toString();
        } catch (Exception e) {
            return UUID.randomUUID().toString();
        }
    }

    private JobLinkRepository() {
        initCategories();
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
        categories.clear();
        categories.add(new TradeCategory("1", "Albañilería y Enchapado", "Construcción y Mejoras del Hogar", "Muros, aceras, cerámica y remodelaciones"));
        categories.add(new TradeCategory("2", "Carpintería y Ebanistería", "Construcción y Mejoras del Hogar", "Muebles a medida, puertas, closets"));
        categories.add(new TradeCategory("3", "Balconería y Soldadura", "Construcción y Mejoras del Hogar", "Portones, balcones y estructuras metálicas"));
        categories.add(new TradeCategory("4", "Cielo Falso y Tabla Yeso", "Construcción y Mejoras del Hogar", "Gypsum, cielo falso e iluminación empotrada"));
        categories.add(new TradeCategory("5", "Pintura e Impermeabilización", "Construcción y Mejoras del Hogar", "Pintura de fachadas, impermeabilización de techos"));

        categories.add(new TradeCategory("6", "Fontanería", "Instalaciones y Mantenimiento Técnico", "Reparación de fugas, cañerías, sanitarios y tanques"));
        categories.add(new TradeCategory("7", "Electricidad Residencial", "Instalaciones y Mantenimiento Técnico", "Cableado, cajas térmicas, ventiladores y duchas"));
        categories.add(new TradeCategory("8", "Aire Acondicionado", "Instalaciones y Mantenimiento Técnico", "Instalación y mantenimiento de minisplits"));
        categories.add(new TradeCategory("9", "Reparación de Línea Blanca", "Instalaciones y Mantenimiento Técnico", "Refrigeradoras, lavadoras y cocinas a domicilio"));
        categories.add(new TradeCategory("10", "Cerrajería", "Instalaciones y Mantenimiento Técnico", "Aperturas de emergencia y cambio de chapas"));

        categories.add(new TradeCategory("11", "Costura y Sastrería", "Textiles, Calzado y Tapicería", "Prendas a medida, uniformes y ajustes de ropa"));
        categories.add(new TradeCategory("12", "Tapicería", "Textiles, Calzado y Tapicería", "Restauración de juegos de sala y asientos de autos"));
        categories.add(new TradeCategory("13", "Zapatería Artesanal", "Textiles, Calzado y Tapicería", "Reparación de calzado, bolsos y mochilas"));

        categories.add(new TradeCategory("14", "Jardinería y Poda", "Exteriores y Servicios del Hogar", "Corte de grama, diseño de jardines y poda"));
        categories.add(new TradeCategory("15", "Limpieza Especializada", "Exteriores y Servicios del Hogar", "Lavado en seco de muebles, alfombras y cisternas"));
        categories.add(new TradeCategory("16", "Control de Plagas", "Exteriores y Servicios del Hogar", "Fumigación de casas y tratamiento de termitas"));
    }

    public int getCategoryIdByName(String name) {
        if (name == null) return 1;
        for (TradeCategory cat : categories) {
            if (cat.getName().equalsIgnoreCase(name.trim()) || name.trim().toLowerCase().contains(cat.getName().toLowerCase())) {
                try {
                    return Integer.parseInt(cat.getId());
                } catch (Exception ignored) {}
            }
        }
        return 1;
    }

    // --- REALTIME CHAT & SUPABASE INTEGRATION ---

    public void getOrCreateConversacionInSupabase(String clienteUuid, String trabajadorUsuarioUuid, RepositoryCallback<String> callback) {
        final String validClientUuid = ensureValidUuid(clienteUuid);
        final String validWorkerUuid = ensureValidUuid(trabajadorUsuarioUuid);

        String clienteEq = "eq." + validClientUuid;
        String trabajadorEq = "eq." + validWorkerUuid;

        SupabaseClient.getApi().findConversacionPair(clienteEq, trabajadorEq).enqueue(new Callback<List<ConversacionDto>>() {
            @Override
            public void onResponse(Call<List<ConversacionDto>> call, Response<List<ConversacionDto>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    String convId = response.body().get(0).id;
                    if (callback != null) callback.onSuccess(convId);
                } else {
                    // Try reverse pair
                    SupabaseClient.getApi().findConversacionPair(trabajadorEq, clienteEq).enqueue(new Callback<List<ConversacionDto>>() {
                        @Override
                        public void onResponse(Call<List<ConversacionDto>> cReverse, Response<List<ConversacionDto>> rReverse) {
                            if (rReverse.isSuccessful() && rReverse.body() != null && !rReverse.body().isEmpty()) {
                                String convId = rReverse.body().get(0).id;
                                if (callback != null) callback.onSuccess(convId);
                            } else {
                                // Create new conversation
                                ConversacionDto newDto = new ConversacionDto(validClientUuid, validWorkerUuid);
                                SupabaseClient.getApi().createConversacion(newDto).enqueue(new Callback<List<ConversacionDto>>() {
                                    @Override
                                    public void onResponse(Call<List<ConversacionDto>> c, Response<List<ConversacionDto>> r) {
                                        if (r.isSuccessful() && r.body() != null && !r.body().isEmpty()) {
                                            String createdId = r.body().get(0).id;
                                            if (callback != null) callback.onSuccess(createdId);
                                        } else {
                                            // Final check fallback
                                            if (callback != null) callback.onSuccess(UUID.randomUUID().toString());
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<List<ConversacionDto>> c, Throwable t) {
                                        if (callback != null) callback.onSuccess(UUID.randomUUID().toString());
                                    }
                                });
                            }
                        }

                        @Override
                        public void onFailure(Call<List<ConversacionDto>> cReverse, Throwable tReverse) {
                            if (callback != null) callback.onSuccess(UUID.randomUUID().toString());
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<ConversacionDto>> call, Throwable t) {
                if (callback != null) callback.onSuccess(UUID.randomUUID().toString());
            }
        });
    }

    public void fetchConversacionesFromSupabase(RepositoryCallback<List<ChatConversation>> callback) {
        if (currentUser == null || currentUser.getId() == null) {
            conversations.clear();
            if (callback != null) callback.onSuccess(conversations);
            return;
        }

        String myUuid = ensureValidUuid(currentUser.getId());
        String orFilter = "(cliente_id.eq." + myUuid + ",trabajador_usuario_id.eq." + myUuid + ")";

        SupabaseClient.getApi().getConversacionesOrUser(orFilter).enqueue(new Callback<List<ConversacionDto>>() {
            @Override
            public void onResponse(Call<List<ConversacionDto>> call, Response<List<ConversacionDto>> response) {
                conversations.clear();
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    List<ConversacionDto> dtoList = response.body();
                    List<ChatConversation> tempConvs = new ArrayList<>();
                    
                    final int[] remaining = {dtoList.size()};

                    for (ConversacionDto dto : dtoList) {
                        String otherUserId = myUuid.equalsIgnoreCase(dto.clienteId) ? dto.trabajadorUsuarioId : dto.clienteId;

                        // Find contact info locally or query Supabase
                        fetchContactInfo(otherUserId, (contactUser) -> {
                            String name = contactUser != null ? contactUser.getName() : "Usuario JobLink";
                            String profession = contactUser != null ? contactUser.getProfession() : "Servicio";
                            String photoUrl = contactUser != null ? contactUser.getProfilePhotoUrl() : null;

                            // Fetch last message for this conversation
                            SupabaseClient.getApi().getUltimoMensaje("eq." + dto.id, "fecha_envio.desc", 1).enqueue(new Callback<List<MensajeDto>>() {
                                @Override
                                public void onResponse(Call<List<MensajeDto>> cMsg, Response<List<MensajeDto>> rMsg) {
                                    String text = "Conversación iniciada";
                                    String timeStr = "Ahora";

                                    if (rMsg.isSuccessful() && rMsg.body() != null && !rMsg.body().isEmpty()) {
                                        MensajeDto lastMsg = rMsg.body().get(0);
                                        if (lastMsg.contenido != null && !lastMsg.contenido.isEmpty()) {
                                            text = lastMsg.contenido;
                                        }
                                        if (lastMsg.fechaEnvio != null && lastMsg.fechaEnvio.length() >= 16) {
                                            timeStr = lastMsg.fechaEnvio.substring(11, 16);
                                        }
                                    }

                                    ChatConversation conv = new ChatConversation(
                                            dto.id, otherUserId, name, profession, photoUrl, text, timeStr, 0
                                    );
                                    tempConvs.add(conv);

                                    remaining[0]--;
                                    if (remaining[0] <= 0) {
                                        conversations.clear();
                                        conversations.addAll(tempConvs);
                                        if (callback != null) callback.onSuccess(conversations);
                                    }
                                }

                                @Override
                                public void onFailure(Call<List<MensajeDto>> cMsg, Throwable tMsg) {
                                    ChatConversation conv = new ChatConversation(
                                            dto.id, otherUserId, name, profession, photoUrl, "Conversación iniciada", "Ahora", 0
                                    );
                                    tempConvs.add(conv);

                                    remaining[0]--;
                                    if (remaining[0] <= 0) {
                                        conversations.clear();
                                        conversations.addAll(tempConvs);
                                        if (callback != null) callback.onSuccess(conversations);
                                    }
                                }
                            });
                        });
                    }
                } else {
                    if (callback != null) callback.onSuccess(conversations);
                }
            }

            @Override
            public void onFailure(Call<List<ConversacionDto>> call, Throwable t) {
                conversations.clear();
                if (callback != null) callback.onSuccess(conversations);
            }
        });
    }

    private interface UserResultCallback {
        void onUserLoaded(User user);
    }

    private void fetchContactInfo(String userId, UserResultCallback callback) {
        if (userId == null) {
            callback.onUserLoaded(null);
            return;
        }

        // Check workers list first
        for (User u : workers) {
            if (u.getId() != null && u.getId().equalsIgnoreCase(userId)) {
                callback.onUserLoaded(u);
                return;
            }
        }

        // Query Supabase usuarios table
        SupabaseClient.getApi().getUsuarioById("eq." + userId).enqueue(new Callback<List<UsuarioDto>>() {
            @Override
            public void onResponse(Call<List<UsuarioDto>> call, Response<List<UsuarioDto>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    UsuarioDto u = response.body().get(0);
                    User.Role role = "trabajador".equalsIgnoreCase(u.rol) ? User.Role.WORKER : User.Role.CLIENT;
                    User user = new User(
                            u.id, u.nombreCompleto, u.email, role,
                            role == User.Role.WORKER ? "Trabajador" : "Cliente",
                            "General", u.ubicacion != null ? u.ubicacion : "Santa Ana",
                            "Usuario de JobLink", 5.0, 0, true,
                            u.telefono != null ? u.telefono : "+503 7000-0000"
                    );
                    user.setProfilePhotoUrl(u.fotoPerfilUrl);
                    callback.onUserLoaded(user);
                } else {
                    callback.onUserLoaded(null);
                }
            }

            @Override
            public void onFailure(Call<List<UsuarioDto>> call, Throwable t) {
                callback.onUserLoaded(null);
            }
        });
    }

    public void fetchMensajesFromSupabase(String conversationId, RepositoryCallback<List<ChatMessage>> callback) {
        if (conversationId == null) {
            if (callback != null) callback.onSuccess(new ArrayList<>());
            return;
        }

        String validConvUuid = ensureValidUuid(conversationId);
        String query = "eq." + validConvUuid;

        SupabaseClient.getApi().getMensajesByConversacion(query, "fecha_envio.asc").enqueue(new Callback<List<MensajeDto>>() {
            @Override
            public void onResponse(Call<List<MensajeDto>> call, Response<List<MensajeDto>> response) {
                List<ChatMessage> list = new ArrayList<>();
                String myUuid = ensureValidUuid(currentUser != null ? currentUser.getId() : null);

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    for (MensajeDto dto : response.body()) {
                        boolean isMine = dto.remitenteId != null && dto.remitenteId.equalsIgnoreCase(myUuid);
                        String timeStr = dto.fechaEnvio != null && dto.fechaEnvio.length() >= 16 ? dto.fechaEnvio.substring(11, 16) : "Ahora";
                        boolean isRead = Boolean.TRUE.equals(dto.leido);
                        ChatMessage msg = new ChatMessage(String.valueOf(dto.id), dto.remitenteId, dto.contenido, timeStr, isMine, dto.imagenAdjuntaUrl, isRead);
                        list.add(msg);
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
        sendMensajeWithPhotoToSupabase(conversationId, text, null, callback);
    }

    public void sendMensajeWithPhotoToSupabase(String conversationId, String text, String photoBase64, RepositoryCallback<Boolean> callback) {
        if (currentUser == null || currentUser.getId() == null) {
            if (callback != null) callback.onError("Usuario no identificado");
            return;
        }

        String myUuid = ensureValidUuid(currentUser.getId());
        String validConvUuid = ensureValidUuid(conversationId);

        List<ChatMessage> list = getChatMessages(conversationId);
        ChatMessage myMsg = new ChatMessage("m_" + System.currentTimeMillis(), myUuid, text, "Ahora", true, photoBase64, false);
        list.add(myMsg);

        MensajeDto dto = new MensajeDto(validConvUuid, myUuid, text);
        dto.imagenAdjuntaUrl = photoBase64;

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

    public void markMensajesAsReadInSupabase(String conversationId) {
        if (conversationId == null || currentUser == null || currentUser.getId() == null) return;
        String myUuid = ensureValidUuid(currentUser.getId());
        String convUuid = ensureValidUuid(conversationId);

        MensajeDto patchDto = new MensajeDto();
        patchDto.leido = true;

        SupabaseClient.getApi().markMensajesAsRead("eq." + convUuid, "neq." + myUuid, patchDto).enqueue(new Callback<List<MensajeDto>>() {
            @Override
            public void onResponse(Call<List<MensajeDto>> call, Response<List<MensajeDto>> response) {}
            @Override
            public void onFailure(Call<List<MensajeDto>> call, Throwable t) {}
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
                            role == User.Role.WORKER ? "Trabajador" : "Cliente",
                            "General", u.ubicacion != null ? u.ubicacion : "Santa Ana",
                            "Usuario de JobLink", 5.0, 10, true,
                            u.telefono != null ? u.telefono : "+503 7000-0000"
                    );
                    if (u.fotoPerfilUrl != null) {
                        user.setProfilePhotoUrl(u.fotoPerfilUrl);
                    }

                    if (role == User.Role.WORKER) {
                        // Fetch worker profile details from Supabase
                        SupabaseClient.getApi().getPerfilTrabajadorByUsuarioId("eq." + u.id).enqueue(new Callback<List<PerfilTrabajadorDto>>() {
                            @Override
                            public void onResponse(Call<List<PerfilTrabajadorDto>> c, Response<List<PerfilTrabajadorDto>> r) {
                                if (r.isSuccessful() && r.body() != null && !r.body().isEmpty()) {
                                    PerfilTrabajadorDto p = r.body().get(0);
                                    if (p.descripcionProfesional != null) user.setBio(p.descripcionProfesional);
                                    if (p.calificacionPromedio != null) user.setRating(p.calificacionPromedio);
                                    if (Boolean.TRUE.equals(p.esPremium)) user.setPremium(true);
                                    if (p.categoria != null && p.categoria.nombre != null) {
                                        user.setProfession(p.categoria.nombre);
                                        user.setCategory(p.categoria.nombre);
                                    }
                                }
                                currentUser = user;
                                addWorkerToList(user);
                                if (callback != null) callback.onSuccess(user);
                            }

                            @Override
                            public void onFailure(Call<List<PerfilTrabajadorDto>> c, Throwable t) {
                                currentUser = user;
                                addWorkerToList(user);
                                if (callback != null) callback.onSuccess(user);
                            }
                        });
                    } else {
                        currentUser = user;
                        if (callback != null) callback.onSuccess(user);
                    }
                } else {
                    if (callback != null) callback.onError("Usuario no encontrado con el correo ingresado");
                }
            }

            @Override
            public void onFailure(Call<List<UsuarioDto>> call, Throwable t) {
                if (callback != null) callback.onError("Error al conectar con Supabase: " + t.getMessage());
            }
        });
    }

    public void updateWorkerPremiumStatusInSupabase(String usuarioId, boolean isPremium, RepositoryCallback<Boolean> callback) {
        if (usuarioId == null) return;
        PerfilTrabajadorDto dto = new PerfilTrabajadorDto();
        dto.esPremium = isPremium;

        SupabaseClient.getApi().updatePerfilTrabajadorByUsuarioId("eq." + usuarioId, dto).enqueue(new Callback<List<PerfilTrabajadorDto>>() {
            @Override
            public void onResponse(Call<List<PerfilTrabajadorDto>> call, Response<List<PerfilTrabajadorDto>> response) {
                if (callback != null) callback.onSuccess(true);
            }

            @Override
            public void onFailure(Call<List<PerfilTrabajadorDto>> call, Throwable t) {
                if (callback != null) callback.onSuccess(true);
            }
        });
    }

    public void fetchPortfolioFromSupabase(String trabajadorId, RepositoryCallback<List<String>> callback) {
        if (trabajadorId == null) {
            if (callback != null) callback.onSuccess(new ArrayList<>());
            return;
        }

        int numericProfileId = -1;
        try {
            numericProfileId = Integer.parseInt(trabajadorId.trim());
        } catch (NumberFormatException ignored) {}

        if (numericProfileId > 0) {
            doFetchPortfolioByNumericId(numericProfileId, callback);
        } else {
            int foundProfileId = -1;
            if (currentUser != null && currentUser.getId() != null && currentUser.getId().equalsIgnoreCase(trabajadorId)) {
                foundProfileId = currentUser.getWorkerProfileId();
            }
            if (foundProfileId <= 0) {
                for (User u : workers) {
                    if (u.getId() != null && u.getId().equalsIgnoreCase(trabajadorId) && u.getWorkerProfileId() > 0) {
                        foundProfileId = u.getWorkerProfileId();
                        break;
                    }
                }
            }

            if (foundProfileId > 0) {
                doFetchPortfolioByNumericId(foundProfileId, callback);
            } else {
                final String userUuid = trabajadorId;
                SupabaseClient.getApi().getPerfilTrabajadorByUsuarioId("eq." + userUuid).enqueue(new Callback<List<PerfilTrabajadorDto>>() {
                    @Override
                    public void onResponse(Call<List<PerfilTrabajadorDto>> call, Response<List<PerfilTrabajadorDto>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            PerfilTrabajadorDto p = response.body().get(0);
                            if (p.id != null && p.id > 0) {
                                doFetchPortfolioByNumericId(p.id, callback);
                                return;
                            }
                        }
                        doFetchPortfolioAll(callback);
                    }

                    @Override
                    public void onFailure(Call<List<PerfilTrabajadorDto>> call, Throwable t) {
                        doFetchPortfolioAll(callback);
                    }
                });
            }
        }
    }

    private void doFetchPortfolioByNumericId(int profileId, RepositoryCallback<List<String>> callback) {
        SupabaseClient.getApi().getPortafolioByTrabajador("eq." + profileId).enqueue(new Callback<List<PortafolioTrabajoDto>>() {
            @Override
            public void onResponse(Call<List<PortafolioTrabajoDto>> call, Response<List<PortafolioTrabajoDto>> response) {
                List<String> photos = new ArrayList<>();
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    for (PortafolioTrabajoDto p : response.body()) {
                        if (p.imagenUrl != null && !p.imagenUrl.isEmpty()) {
                            photos.add(p.imagenUrl);
                        }
                    }
                }
                if (callback != null) callback.onSuccess(photos);
            }

            @Override
            public void onFailure(Call<List<PortafolioTrabajoDto>> call, Throwable t) {
                if (callback != null) callback.onSuccess(new ArrayList<>());
            }
        });
    }

    private void doFetchPortfolioAll(RepositoryCallback<List<String>> callback) {
        SupabaseClient.getApi().getTodosPortafolios().enqueue(new Callback<List<PortafolioTrabajoDto>>() {
            @Override
            public void onResponse(Call<List<PortafolioTrabajoDto>> c, Response<List<PortafolioTrabajoDto>> r) {
                List<String> photos = new ArrayList<>();
                if (r.isSuccessful() && r.body() != null) {
                    for (PortafolioTrabajoDto p : r.body()) {
                        if (p.imagenUrl != null && !p.imagenUrl.isEmpty()) {
                            photos.add(p.imagenUrl);
                        }
                    }
                }
                if (callback != null) callback.onSuccess(photos);
            }

            @Override
            public void onFailure(Call<List<PortafolioTrabajoDto>> c, Throwable t) {
                if (callback != null) callback.onSuccess(new ArrayList<>());
            }
        });
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
                } else {
                    seedCategoriesToSupabase();
                    if (callback != null) callback.onSuccess(categories);
                }
            }

            @Override
            public void onFailure(Call<List<CategoriaOficioDto>> call, Throwable t) {
                if (callback != null) callback.onSuccess(categories);
            }
        });
    }

    private void seedCategoriesToSupabase() {
        for (TradeCategory cat : categories) {
            try {
                int catId = Integer.parseInt(cat.getId());
                CategoriaOficioDto dto = new CategoriaOficioDto();
                dto.id = catId;
                dto.nombre = cat.getName();
                dto.descripcion = cat.getDescription();
                SupabaseClient.getApi().createCategoriaOficio(dto).enqueue(new Callback<List<CategoriaOficioDto>>() {
                    @Override
                    public void onResponse(Call<List<CategoriaOficioDto>> call, Response<List<CategoriaOficioDto>> response) {}
                    @Override
                    public void onFailure(Call<List<CategoriaOficioDto>> call, Throwable t) {}
                });
            } catch (Exception ignored) {}
        }
    }

    public void fetchWorkersFromSupabase(RepositoryCallback<List<User>> callback) {
        SupabaseClient.getApi().getPerfilesTrabajadores().enqueue(new Callback<List<PerfilTrabajadorDto>>() {
            @Override
            public void onResponse(Call<List<PerfilTrabajadorDto>> call, Response<List<PerfilTrabajadorDto>> response) {
                List<User> loadedWorkers = new ArrayList<>();
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    for (PerfilTrabajadorDto dto : response.body()) {
                        String name = dto.usuario != null && dto.usuario.nombreCompleto != null ? dto.usuario.nombreCompleto : "Trabajador";
                        String email = dto.usuario != null && dto.usuario.email != null ? dto.usuario.email : "user@joblink.sv";
                        String phone = dto.usuario != null && dto.usuario.telefono != null ? dto.usuario.telefono : "+503 7000-0000";
                        String location = dto.usuario != null && dto.usuario.ubicacion != null ? dto.usuario.ubicacion : "Santa Ana";
                        String trade = dto.categoria != null && dto.categoria.nombre != null ? dto.categoria.nombre : "Albañilería y Enchapado";

                        User w = new User(
                                dto.usuarioId != null ? ensureValidUuid(dto.usuarioId) : UUID.randomUUID().toString(),
                                name, email, User.Role.WORKER, trade, trade, location,
                                dto.descripcionProfesional != null ? dto.descripcionProfesional : "Servicio profesional en Santa Ana",
                                dto.calificacionPromedio != null ? dto.calificacionPromedio : 5.0,
                                12, true, phone
                        );
                        if (dto.id != null) {
                            w.setWorkerProfileId(dto.id);
                        }
                        if (dto.usuario != null && dto.usuario.fotoPerfilUrl != null) {
                            w.setProfilePhotoUrl(dto.usuario.fotoPerfilUrl);
                        }
                        if (Boolean.TRUE.equals(dto.esPremium)) {
                            w.setPremium(true);
                        }
                        loadedWorkers.add(w);
                    }
                }

                SupabaseClient.getApi().getUsuariosByRole("eq.trabajador").enqueue(new Callback<List<UsuarioDto>>() {
                    @Override
                    public void onResponse(Call<List<UsuarioDto>> c, Response<List<UsuarioDto>> r) {
                        if (r.isSuccessful() && r.body() != null) {
                            for (UsuarioDto u : r.body()) {
                                boolean exists = false;
                                for (User lw : loadedWorkers) {
                                    if (lw.getId() != null && lw.getId().equalsIgnoreCase(u.id)) {
                                        exists = true;
                                        break;
                                    }
                                }
                                if (!exists) {
                                    User w = new User(
                                            u.id, u.nombreCompleto, u.email, User.Role.WORKER,
                                            "Albañilería y Enchapado", "General",
                                            u.ubicacion != null ? u.ubicacion : "Santa Ana",
                                            "Servicio profesional en Santa Ana", 5.0, 0, true,
                                            u.telefono != null ? u.telefono : "+503 7000-0000"
                                    );
                                    w.setProfilePhotoUrl(u.fotoPerfilUrl);
                                    loadedWorkers.add(w);
                                }
                            }
                        }
                        workers.clear();
                        workers.addAll(loadedWorkers);
                        if (callback != null) callback.onSuccess(workers);
                    }

                    @Override
                    public void onFailure(Call<List<UsuarioDto>> c, Throwable t) {
                        workers.clear();
                        workers.addAll(loadedWorkers);
                        if (callback != null) callback.onSuccess(workers);
                    }
                });
            }

            @Override
            public void onFailure(Call<List<PerfilTrabajadorDto>> call, Throwable t) {
                if (callback != null) callback.onSuccess(workers);
            }
        });
    }

    public void registerUserInSupabase(User user, String password, RepositoryCallback<User> callback) {
        String validUserUuid = ensureValidUuid(user.getId());
        user.setId(validUserUuid);

        String roleStr = user.getRole() == User.Role.WORKER ? "trabajador" : "cliente";
        UsuarioDto userDto = new UsuarioDto(
                user.getName(),
                user.getEmail(),
                password != null ? password : "defaultPassword123",
                user.getPhone() != null ? user.getPhone() : "+503 7000-0000",
                user.getLocation() != null ? user.getLocation() : "Santa Ana",
                roleStr
        );
        userDto.id = validUserUuid;
        userDto.fotoPerfilUrl = user.getProfilePhotoUrl();

        String emailQuery = "eq." + user.getEmail().trim();
        SupabaseClient.getApi().getUsuarioByEmail(emailQuery).enqueue(new Callback<List<UsuarioDto>>() {
            @Override
            public void onResponse(Call<List<UsuarioDto>> call, Response<List<UsuarioDto>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    String existingId = response.body().get(0).id;
                    user.setId(existingId);
                    userDto.id = existingId;
                    setCurrentUser(user);

                    SupabaseClient.getApi().updateUsuario("eq." + existingId, userDto).enqueue(new Callback<List<UsuarioDto>>() {
                        @Override
                        public void onResponse(Call<List<UsuarioDto>> c, Response<List<UsuarioDto>> r) {
                            if (r.isSuccessful()) {
                                createPerfilIfWorker(user, callback);
                            } else {
                                Log.e(TAG, "Error updating usuario in Supabase: " + r.code());
                                createPerfilIfWorker(user, callback);
                            }
                        }

                        @Override
                        public void onFailure(Call<List<UsuarioDto>> c, Throwable t) {
                            Log.e(TAG, "Failure updating usuario in Supabase", t);
                            createPerfilIfWorker(user, callback);
                        }
                    });
                } else {
                    SupabaseClient.getApi().createUsuario(userDto).enqueue(new Callback<List<UsuarioDto>>() {
                        @Override
                        public void onResponse(Call<List<UsuarioDto>> c, Response<List<UsuarioDto>> r) {
                            if (r.isSuccessful()) {
                                if (r.body() != null && !r.body().isEmpty() && r.body().get(0).id != null) {
                                    user.setId(r.body().get(0).id);
                                }
                                setCurrentUser(user);
                                createPerfilIfWorker(user, callback);
                            } else {
                                Log.e(TAG, "Error creating usuario in Supabase: " + r.code());
                                setCurrentUser(user);
                                createPerfilIfWorker(user, callback);
                            }
                        }

                        @Override
                        public void onFailure(Call<List<UsuarioDto>> c, Throwable t) {
                            Log.e(TAG, "Failure creating usuario in Supabase", t);
                            setCurrentUser(user);
                            createPerfilIfWorker(user, callback);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<UsuarioDto>> call, Throwable t) {
                Log.e(TAG, "Failure checking usuario in Supabase", t);
                setCurrentUser(user);
                createPerfilIfWorker(user, callback);
            }
        });
    }

    private void createPerfilIfWorker(User user, RepositoryCallback<User> callback) {
        if (user.getRole() == User.Role.WORKER) {
            int catId = getCategoryIdByName(user.getProfession());
            PerfilTrabajadorDto profileDto = new PerfilTrabajadorDto(user.getId(), catId, user.getBio());

            SupabaseClient.getApi().getPerfilTrabajadorByUsuarioId("eq." + user.getId()).enqueue(new Callback<List<PerfilTrabajadorDto>>() {
                @Override
                public void onResponse(Call<List<PerfilTrabajadorDto>> cExist, Response<List<PerfilTrabajadorDto>> rExist) {
                    if (rExist.isSuccessful() && rExist.body() != null && !rExist.body().isEmpty()) {
                        int existingId = rExist.body().get(0).id;
                        user.setWorkerProfileId(existingId);
                        SupabaseClient.getApi().updatePerfilTrabajadorByUsuarioId("eq." + user.getId(), profileDto).enqueue(new Callback<List<PerfilTrabajadorDto>>() {
                            @Override
                            public void onResponse(Call<List<PerfilTrabajadorDto>> c2, Response<List<PerfilTrabajadorDto>> r2) {
                                addWorkerToList(user);
                                if (callback != null) callback.onSuccess(user);
                            }

                            @Override
                            public void onFailure(Call<List<PerfilTrabajadorDto>> c2, Throwable t2) {
                                addWorkerToList(user);
                                if (callback != null) callback.onSuccess(user);
                            }
                        });
                    } else {
                        SupabaseClient.getApi().createPerfilTrabajador(profileDto).enqueue(new Callback<List<PerfilTrabajadorDto>>() {
                            @Override
                            public void onResponse(Call<List<PerfilTrabajadorDto>> c2, Response<List<PerfilTrabajadorDto>> r2) {
                                if (r2.isSuccessful() && r2.body() != null && !r2.body().isEmpty()) {
                                    if (r2.body().get(0).id != null) {
                                        user.setWorkerProfileId(r2.body().get(0).id);
                                    }
                                }
                                addWorkerToList(user);
                                if (callback != null) callback.onSuccess(user);
                            }

                            @Override
                            public void onFailure(Call<List<PerfilTrabajadorDto>> c2, Throwable t2) {
                                addWorkerToList(user);
                                if (callback != null) callback.onSuccess(user);
                            }
                        });
                    }
                }

                @Override
                public void onFailure(Call<List<PerfilTrabajadorDto>> cExist, Throwable tExist) {
                    SupabaseClient.getApi().createPerfilTrabajador(profileDto).enqueue(new Callback<List<PerfilTrabajadorDto>>() {
                        @Override
                        public void onResponse(Call<List<PerfilTrabajadorDto>> c2, Response<List<PerfilTrabajadorDto>> r2) {
                            if (r2.isSuccessful() && r2.body() != null && !r2.body().isEmpty()) {
                                if (r2.body().get(0).id != null) {
                                    user.setWorkerProfileId(r2.body().get(0).id);
                                }
                            }
                            addWorkerToList(user);
                            if (callback != null) callback.onSuccess(user);
                        }

                        @Override
                        public void onFailure(Call<List<PerfilTrabajadorDto>> c2, Throwable t2) {
                            addWorkerToList(user);
                            if (callback != null) callback.onSuccess(user);
                        }
                    });
                }
            });
        } else {
            if (callback != null) callback.onSuccess(user);
        }
    }

    private void addWorkerToList(User user) {
        for (int i = 0; i < workers.size(); i++) {
            if (workers.get(i).getId() != null && workers.get(i).getId().equalsIgnoreCase(user.getId())) {
                workers.set(i, user);
                return;
            }
        }
        workers.add(user);
    }

    // Getters & Methods
    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User user) {
        if (this.currentUser == null || user == null || this.currentUser.getId() == null || user.getId() == null || !this.currentUser.getId().equalsIgnoreCase(user.getId())) {
            conversations.clear();
            chatMessagesMap.clear();
        }
        this.currentUser = user;
    }
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

        String myUuid = ensureValidUuid(currentUser != null ? currentUser.getId() : null);

        SolicitudTrabajoDto dto = new SolicitudTrabajoDto(
                myUuid,
                1,
                order.getTitle(),
                "Detalles de la solicitud desde el chat",
                50.0,
                "2026-11-20"
        );

        SupabaseClient.getApi().createSolicitudTrabajo(dto).enqueue(new Callback<List<SolicitudTrabajoDto>>() {
            @Override
            public void onResponse(Call<List<SolicitudTrabajoDto>> call, Response<List<SolicitudTrabajoDto>> response) {}
            @Override
            public void onFailure(Call<List<SolicitudTrabajoDto>> call, Throwable t) {}
        });
    }

    public void fetchSolicitudesTrabajoFromSupabase(RepositoryCallback<List<SolicitudTrabajoDto>> callback) {
        if (currentUser == null || currentUser.getId() == null) {
            if (callback != null) callback.onSuccess(new ArrayList<>());
            return;
        }

        String myUuid = ensureValidUuid(currentUser.getId());
        int myWorkerProfileId = currentUser.getWorkerProfileId();

        String orFilter;
        if (myWorkerProfileId > 0) {
            orFilter = "(cliente_id.eq." + myUuid + ",trabajador_id.eq." + myWorkerProfileId + ")";
        } else {
            orFilter = "(cliente_id.eq." + myUuid + ")";
        }

        SupabaseClient.getApi().getSolicitudesOrUser(orFilter).enqueue(new Callback<List<SolicitudTrabajoDto>>() {
            @Override
            public void onResponse(Call<List<SolicitudTrabajoDto>> call, Response<List<SolicitudTrabajoDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (callback != null) callback.onSuccess(response.body());
                } else {
                    SupabaseClient.getApi().getSolicitudesTrabajo().enqueue(new Callback<List<SolicitudTrabajoDto>>() {
                        @Override
                        public void onResponse(Call<List<SolicitudTrabajoDto>> c, Response<List<SolicitudTrabajoDto>> r) {
                            if (r.isSuccessful() && r.body() != null) {
                                if (callback != null) callback.onSuccess(r.body());
                            } else {
                                if (callback != null) callback.onSuccess(new ArrayList<>());
                            }
                        }

                        @Override
                        public void onFailure(Call<List<SolicitudTrabajoDto>> c, Throwable t) {
                            if (callback != null) callback.onSuccess(new ArrayList<>());
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<SolicitudTrabajoDto>> call, Throwable t) {
                if (callback != null) callback.onSuccess(new ArrayList<>());
            }
        });
    }

    public void createSolicitudTrabajoInSupabase(SolicitudTrabajoDto dto, RepositoryCallback<Boolean> callback) {
        SupabaseClient.getApi().createSolicitudTrabajo(dto).enqueue(new Callback<List<SolicitudTrabajoDto>>() {
            @Override
            public void onResponse(Call<List<SolicitudTrabajoDto>> call, Response<List<SolicitudTrabajoDto>> response) {
                if (callback != null) callback.onSuccess(true);
            }

            @Override
            public void onFailure(Call<List<SolicitudTrabajoDto>> call, Throwable t) {
                if (callback != null) callback.onSuccess(true);
            }
        });
    }

    public void updateSolicitudEstadoInSupabase(int solicitudId, String nuevoEstado, RepositoryCallback<Boolean> callback) {
        SolicitudTrabajoDto patchDto = new SolicitudTrabajoDto();
        patchDto.estado = nuevoEstado;

        SupabaseClient.getApi().updateSolicitudTrabajo("eq." + solicitudId, patchDto).enqueue(new Callback<List<SolicitudTrabajoDto>>() {
            @Override
            public void onResponse(Call<List<SolicitudTrabajoDto>> call, Response<List<SolicitudTrabajoDto>> response) {
                if (callback != null) callback.onSuccess(true);
            }

            @Override
            public void onFailure(Call<List<SolicitudTrabajoDto>> call, Throwable t) {
                if (callback != null) callback.onSuccess(true);
            }
        });
    }

    public void updateSolicitudAvanceInSupabase(int solicitudId, int avance, RepositoryCallback<Boolean> callback) {
        SolicitudTrabajoDto patchDto = new SolicitudTrabajoDto();
        patchDto.porcentajeAvance = avance;
        if (avance >= 100) {
            patchDto.estado = "Completado";
        }

        SupabaseClient.getApi().updateSolicitudTrabajo("eq." + solicitudId, patchDto).enqueue(new Callback<List<SolicitudTrabajoDto>>() {
            @Override
            public void onResponse(Call<List<SolicitudTrabajoDto>> call, Response<List<SolicitudTrabajoDto>> response) {
                if (callback != null) callback.onSuccess(true);
            }

            @Override
            public void onFailure(Call<List<SolicitudTrabajoDto>> call, Throwable t) {
                if (callback != null) callback.onSuccess(true);
            }
        });
    }

    public void fetchValoracionesForTrabajador(int trabajadorProfileId, RepositoryCallback<List<ValoracionDto>> callback) {
        SupabaseClient.getApi().getValoracionesByTrabajador("eq." + trabajadorProfileId).enqueue(new Callback<List<ValoracionDto>>() {
            @Override
            public void onResponse(Call<List<ValoracionDto>> call, Response<List<ValoracionDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (callback != null) callback.onSuccess(response.body());
                } else {
                    if (callback != null) callback.onSuccess(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<List<ValoracionDto>> call, Throwable t) {
                if (callback != null) callback.onSuccess(new ArrayList<>());
            }
        });
    }

    public void getUnreadMessagesCount(RepositoryCallback<Integer> callback) {
        if (currentUser == null || currentUser.getId() == null) {
            if (callback != null) callback.onSuccess(0);
            return;
        }

        String myUuid = ensureValidUuid(currentUser.getId());
        SupabaseClient.getApi().getUnreadMensajesForUser("neq." + myUuid, "eq.false").enqueue(new Callback<List<MensajeDto>>() {
            @Override
            public void onResponse(Call<List<MensajeDto>> call, Response<List<MensajeDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (callback != null) callback.onSuccess(response.body().size());
                } else {
                    if (callback != null) callback.onSuccess(0);
                }
            }

            @Override
            public void onFailure(Call<List<MensajeDto>> call, Throwable t) {
                if (callback != null) callback.onSuccess(0);
            }
        });
    }

    public void getPendingSolicitudesCount(RepositoryCallback<Integer> callback) {
        fetchSolicitudesTrabajoFromSupabase(new RepositoryCallback<List<SolicitudTrabajoDto>>() {
            @Override
            public void onSuccess(List<SolicitudTrabajoDto> result) {
                int pending = 0;
                if (result != null) {
                    for (SolicitudTrabajoDto s : result) {
                        if (s.estado != null && "Pendiente".equalsIgnoreCase(s.estado.trim())) {
                            pending++;
                        }
                    }
                }
                if (callback != null) callback.onSuccess(pending);
            }

            @Override
            public void onError(String message) {
                if (callback != null) callback.onSuccess(0);
            }
        });
    }
}
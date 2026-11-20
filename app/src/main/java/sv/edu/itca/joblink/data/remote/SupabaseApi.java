package sv.edu.itca.joblink.data.remote;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;
import sv.edu.itca.joblink.data.remote.dto.CategoriaOficioDto;
import sv.edu.itca.joblink.data.remote.dto.ConversacionDto;
import sv.edu.itca.joblink.data.remote.dto.MensajeDto;
import sv.edu.itca.joblink.data.remote.dto.PerfilTrabajadorDto;
import sv.edu.itca.joblink.data.remote.dto.PortafolioTrabajoDto;
import sv.edu.itca.joblink.data.remote.dto.SolicitudTrabajoDto;
import sv.edu.itca.joblink.data.remote.dto.UsuarioDto;
import sv.edu.itca.joblink.data.remote.dto.ValoracionDto;

public interface SupabaseApi {

    // 1. Categorias de Oficios
    @GET("rest/v1/categorias_oficios?select=*")
    Call<List<CategoriaOficioDto>> getCategoriasOficios();

    @POST("rest/v1/categorias_oficios")
    Call<List<CategoriaOficioDto>> createCategoriaOficio(@Body CategoriaOficioDto dto);

    // 2. Usuarios
    @GET("rest/v1/usuarios")
    Call<List<UsuarioDto>> getUsuarioByEmail(@Query("email") String emailEq);

    @GET("rest/v1/usuarios")
    Call<List<UsuarioDto>> getUsuarioById(@Query("id") String idEq);

    @GET("rest/v1/usuarios")
    Call<List<UsuarioDto>> getUsuariosByRole(@Query("rol") String rolEq);

    @POST("rest/v1/usuarios")
    Call<List<UsuarioDto>> createUsuario(@Body UsuarioDto dto);

    @PATCH("rest/v1/usuarios")
    Call<List<UsuarioDto>> updateUsuario(@Query("id") String idEq, @Body UsuarioDto dto);

    // 3. Perfiles de Trabajadores
    @GET("rest/v1/perfiles_trabajadores?select=*,usuarios(*),categorias_oficios(*)")
    Call<List<PerfilTrabajadorDto>> getPerfilesTrabajadores();

    @GET("rest/v1/perfiles_trabajadores?select=*,usuarios(*),categorias_oficios(*)")
    Call<List<PerfilTrabajadorDto>> getPerfilTrabajadorByUsuarioId(@Query("usuario_id") String usuarioIdEq);

    @POST("rest/v1/perfiles_trabajadores")
    Call<List<PerfilTrabajadorDto>> createPerfilTrabajador(@Body PerfilTrabajadorDto dto);

    @PATCH("rest/v1/perfiles_trabajadores")
    Call<List<PerfilTrabajadorDto>> updatePerfilTrabajador(@Query("id") String idEq, @Body PerfilTrabajadorDto dto);

    @PATCH("rest/v1/perfiles_trabajadores")
    Call<List<PerfilTrabajadorDto>> updatePerfilTrabajadorByUsuarioId(@Query("usuario_id") String usuarioIdEq, @Body PerfilTrabajadorDto dto);

    // 4. Portafolio de Trabajos
    @GET("rest/v1/portafolio_trabajos?select=*")
    Call<List<PortafolioTrabajoDto>> getPortafolioByTrabajador(@Query("trabajador_id") String trabajadorIdEq);

    @GET("rest/v1/portafolio_trabajos?select=*")
    Call<List<PortafolioTrabajoDto>> getTodosPortafolios();

    @POST("rest/v1/portafolio_trabajos")
    Call<List<PortafolioTrabajoDto>> addPortafolioItem(@Body PortafolioTrabajoDto dto);

    // 5. Solicitudes de Trabajo
    @GET("rest/v1/solicitudes_trabajo?select=*")
    Call<List<SolicitudTrabajoDto>> getSolicitudesTrabajo();

    @GET("rest/v1/solicitudes_trabajo?select=*")
    Call<List<SolicitudTrabajoDto>> getSolicitudesOrUser(@Query("or") String orFilter);

    @GET("rest/v1/solicitudes_trabajo?select=*")
    Call<List<SolicitudTrabajoDto>> getSolicitudesByCliente(@Query("cliente_id") String clienteEq);

    @GET("rest/v1/solicitudes_trabajo?select=*")
    Call<List<SolicitudTrabajoDto>> getSolicitudesByTrabajador(@Query("trabajador_id") String trabajadorIdEq);

    @POST("rest/v1/solicitudes_trabajo")
    Call<List<SolicitudTrabajoDto>> createSolicitudTrabajo(@Body SolicitudTrabajoDto dto);

    @PATCH("rest/v1/solicitudes_trabajo")
    Call<List<SolicitudTrabajoDto>> updateSolicitudTrabajo(@Query("id") String idEq, @Body SolicitudTrabajoDto dto);

    // 6. Conversaciones
    @GET("rest/v1/conversaciones?select=*")
    Call<List<ConversacionDto>> getConversaciones();

    @GET("rest/v1/conversaciones?select=*")
    Call<List<ConversacionDto>> getConversacionesOrUser(@Query("or") String orFilter);

    @GET("rest/v1/conversaciones?select=*")
    Call<List<ConversacionDto>> findConversacionPair(@Query("cliente_id") String clienteEq, @Query("trabajador_usuario_id") String trabajadorEq);

    @POST("rest/v1/conversaciones")
    Call<List<ConversacionDto>> createConversacion(@Body ConversacionDto dto);

    // 7. Mensajes
    @GET("rest/v1/mensajes?select=*")
    Call<List<MensajeDto>> getMensajesByConversacion(@Query("conversacion_id") String conversacionIdEq, @Query("order") String orderClause);

    @GET("rest/v1/mensajes?select=*")
    Call<List<MensajeDto>> getUnreadMensajesForUser(@Query("remitente_id") String remitenteNeq, @Query("leido") String leidoEq);

    @GET("rest/v1/mensajes?select=*")
    Call<List<MensajeDto>> getUltimoMensaje(@Query("conversacion_id") String conversacionIdEq, @Query("order") String orderClause, @Query("limit") int limit);

    @POST("rest/v1/mensajes")
    Call<List<MensajeDto>> sendMensaje(@Body MensajeDto dto);

    @PATCH("rest/v1/mensajes")
    Call<List<MensajeDto>> markMensajesAsRead(@Query("conversacion_id") String conversacionIdEq, @Query("remitente_id") String remitenteNeq, @Body MensajeDto dto);

    // 8. Valoraciones y Reseñas
    @GET("rest/v1/valoraciones?select=*")
    Call<List<ValoracionDto>> getValoraciones();

    @GET("rest/v1/valoraciones?select=*")
    Call<List<ValoracionDto>> getValoracionesByTrabajador(@Query("trabajador_id") String trabajadorIdEq);

    @POST("rest/v1/valoraciones")
    Call<List<ValoracionDto>> createValoracion(@Body ValoracionDto dto);
}

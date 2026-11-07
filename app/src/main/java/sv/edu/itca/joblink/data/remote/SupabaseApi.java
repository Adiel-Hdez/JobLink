package sv.edu.itca.joblink.data.remote;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;
import sv.edu.itca.joblink.data.remote.dto.CategoriaOficioDto;
import sv.edu.itca.joblink.data.remote.dto.ConversacionDto;
import sv.edu.itca.joblink.data.remote.dto.MensajeDto;
import sv.edu.itca.joblink.data.remote.dto.PerfilTrabajadorDto;
import sv.edu.itca.joblink.data.remote.dto.PortafolioTrabajoDto;
import sv.edu.itca.joblink.data.remote.dto.SolicitudTrabajoDto;
import sv.edu.itca.joblink.data.remote.dto.UsuarioDto;

public interface SupabaseApi {

    // 1. Categorias de Oficios
    @GET("rest/v1/categorias_oficios?select=*")
    Call<List<CategoriaOficioDto>> getCategoriasOficios();

    @Headers({"Prefer: return=representation"})
    @POST("rest/v1/categorias_oficios")
    Call<List<CategoriaOficioDto>> createCategoriaOficio(@Body CategoriaOficioDto dto);

    // 2. Usuarios
    @GET("rest/v1/usuarios")
    Call<List<UsuarioDto>> getUsuarioByEmail(@Query("email") String emailEq);

    @Headers({"Prefer: return=representation"})
    @POST("rest/v1/usuarios")
    Call<List<UsuarioDto>> createUsuario(@Body UsuarioDto dto);

    // 3. Perfiles de Trabajadores
    @GET("rest/v1/perfiles_trabajadores?select=*,usuarios(*),categorias_oficios(*)")
    Call<List<PerfilTrabajadorDto>> getPerfilesTrabajadores();

    @Headers({"Prefer: return=representation"})
    @POST("rest/v1/perfiles_trabajadores")
    Call<List<PerfilTrabajadorDto>> createPerfilTrabajador(@Body PerfilTrabajadorDto dto);

    // 4. Portafolio de Trabajos
    @GET("rest/v1/portafolio_trabajos?select=*")
    Call<List<PortafolioTrabajoDto>> getPortafolioByTrabajador(@Query("trabajador_id") String trabajadorIdEq);

    @Headers({"Prefer: return=representation"})
    @POST("rest/v1/portafolio_trabajos")
    Call<List<PortafolioTrabajoDto>> addPortafolioItem(@Body PortafolioTrabajoDto dto);

    // 5. Solicitudes de Trabajo
    @GET("rest/v1/solicitudes_trabajo?select=*")
    Call<List<SolicitudTrabajoDto>> getSolicitudesTrabajo();

    @Headers({"Prefer: return=representation"})
    @POST("rest/v1/solicitudes_trabajo")
    Call<List<SolicitudTrabajoDto>> createSolicitudTrabajo(@Body SolicitudTrabajoDto dto);

    // 6. Conversaciones
    @GET("rest/v1/conversaciones?select=*")
    Call<List<ConversacionDto>> getConversaciones();

    @Headers({"Prefer: return=representation"})
    @POST("rest/v1/conversaciones")
    Call<List<ConversacionDto>> createConversacion(@Body ConversacionDto dto);

    // 7. Mensajes
    @GET("rest/v1/mensajes?select=*")
    Call<List<MensajeDto>> getMensajesByConversacion(@Query("conversacion_id") String conversacionIdEq);

    @Headers({"Prefer: return=representation"})
    @POST("rest/v1/mensajes")
    Call<List<MensajeDto>> sendMensaje(@Body MensajeDto dto);
}
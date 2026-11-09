package sv.edu.itca.joblink.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class ValoracionDto {
    @SerializedName("id")
    public Integer id;

    @SerializedName("solicitud_id")
    public Integer solicitudId;

    @SerializedName("cliente_id")
    public String clienteId;

    @SerializedName("trabajador_id")
    public Integer trabajadorId;

    @SerializedName("calificacion_calidad")
    public Integer calificacionCalidad;

    @SerializedName("calificacion_puntualidad")
    public Integer calificacionPuntualidad;

    @SerializedName("calificacion_comunicacion")
    public Integer calificacionComunicacion;

    @SerializedName("comentario")
    public String comentario;

    @SerializedName("foto_resultado_url")
    public String fotoResultadoUrl;

    public ValoracionDto() {}

    public ValoracionDto(Integer solicitudId, String clienteId, Integer trabajadorId, Integer calificacion, String comentario) {
        this.solicitudId = solicitudId;
        this.clienteId = clienteId;
        this.trabajadorId = trabajadorId;
        this.calificacionCalidad = calificacion;
        this.calificacionPuntualidad = calificacion;
        this.calificacionComunicacion = calificacion;
        this.comentario = comentario;
    }
}
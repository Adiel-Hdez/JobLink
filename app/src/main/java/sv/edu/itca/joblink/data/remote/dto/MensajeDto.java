package sv.edu.itca.joblink.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class MensajeDto {
    @SerializedName("id")
    public Long id;

    @SerializedName("conversacion_id")
    public String conversacionId;

    @SerializedName("remitente_id")
    public String remitenteId;

    @SerializedName("contenido")
    public String contenido;

    @SerializedName("imagen_adjunta_url")
    public String imagenAdjuntaUrl;

    @SerializedName("leido")
    public Boolean leido;

    @SerializedName("fecha_envio")
    public String fechaEnvio;

    public MensajeDto() {}

    public MensajeDto(String conversacionId, String remitenteId, String contenido) {
        this.conversacionId = conversacionId;
        this.remitenteId = remitenteId;
        this.contenido = contenido;
        this.leido = false;
    }
}
package sv.edu.itca.joblink.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class ConversacionDto {
    @SerializedName("id")
    public String id;

    @SerializedName("cliente_id")
    public String clienteId;

    @SerializedName("trabajador_usuario_id")
    public String trabajadorUsuarioId;

    @SerializedName("fecha_creacion")
    public String fechaCreacion;

    public ConversacionDto() {}

    public ConversacionDto(String clienteId, String trabajadorUsuarioId) {
        this.clienteId = clienteId;
        this.trabajadorUsuarioId = trabajadorUsuarioId;
    }
}
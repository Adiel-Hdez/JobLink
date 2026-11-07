package sv.edu.itca.joblink.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class PortafolioTrabajoDto {
    @SerializedName("id")
    public Integer id;

    @SerializedName("trabajador_id")
    public Integer trabajadorId;

    @SerializedName("titulo")
    public String titulo;

    @SerializedName("descripcion_breve")
    public String descripcionBreve;

    @SerializedName("imagen_url")
    public String imagenUrl;

    public PortafolioTrabajoDto() {}

    public PortafolioTrabajoDto(Integer trabajadorId, String titulo, String descripcionBreve, String imagenUrl) {
        this.trabajadorId = trabajadorId;
        this.titulo = titulo;
        this.descripcionBreve = descripcionBreve;
        this.imagenUrl = imagenUrl;
    }
}
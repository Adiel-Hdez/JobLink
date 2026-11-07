package sv.edu.itca.joblink.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class PerfilTrabajadorDto {
    @SerializedName("id")
    public Integer id;

    @SerializedName("usuario_id")
    public String usuarioId;

    @SerializedName("categoria_id")
    public Integer categoriaId;

    @SerializedName("descripcion_profesional")
    public String descripcionProfesional;

    @SerializedName("disponible")
    public Boolean disponible;

    @SerializedName("calificacion_promedio")
    public Double calificacionPromedio;

    // Optional joined object when using select=*,usuarios(*),categorias_oficios(*)
    @SerializedName("usuarios")
    public UsuarioDto usuario;

    @SerializedName("categorias_oficios")
    public CategoriaOficioDto categoria;

    public PerfilTrabajadorDto() {}

    public PerfilTrabajadorDto(String usuarioId, Integer categoriaId, String descripcionProfesional) {
        this.usuarioId = usuarioId;
        this.categoriaId = categoriaId;
        this.descripcionProfesional = descripcionProfesional;
        this.disponible = true;
        this.calificacionPromedio = 5.0;
    }
}
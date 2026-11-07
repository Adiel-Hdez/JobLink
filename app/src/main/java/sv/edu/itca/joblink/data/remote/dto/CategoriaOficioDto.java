package sv.edu.itca.joblink.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class CategoriaOficioDto {
    @SerializedName("id")
    public Integer id;

    @SerializedName("nombre")
    public String nombre;

    @SerializedName("descripcion")
    public String descripcion;

    @SerializedName("icono_url")
    public String iconoUrl;

    public CategoriaOficioDto() {}

    public CategoriaOficioDto(String nombre, String descripcion, String iconoUrl) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.iconoUrl = iconoUrl;
    }
}
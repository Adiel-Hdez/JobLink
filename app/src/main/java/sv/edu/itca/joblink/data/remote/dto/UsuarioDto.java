package sv.edu.itca.joblink.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class UsuarioDto {
    @SerializedName("id")
    public String id;

    @SerializedName("nombre_completo")
    public String nombreCompleto;

    @SerializedName("email")
    public String email;

    @SerializedName("password_hash")
    public String passwordHash;

    @SerializedName("telefono")
    public String telefono;

    @SerializedName("ubicacion")
    public String ubicacion;

    @SerializedName("foto_perfil_url")
    public String fotoPerfilUrl;

    @SerializedName("rol")
    public String rol; // 'cliente' or 'trabajador'

    @SerializedName("fecha_registro")
    public String fechaRegistro;

    public UsuarioDto() {}

    public UsuarioDto(String nombreCompleto, String email, String passwordHash, String telefono, String ubicacion, String rol) {
        this.nombreCompleto = nombreCompleto;
        this.email = email;
        this.passwordHash = passwordHash;
        this.telefono = telefono;
        this.ubicacion = ubicacion;
        this.rol = rol;
    }
}
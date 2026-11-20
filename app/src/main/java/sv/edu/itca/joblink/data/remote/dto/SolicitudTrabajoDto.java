package sv.edu.itca.joblink.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class SolicitudTrabajoDto {
    @SerializedName("id")
    public Integer id;

    @SerializedName("cliente_id")
    public String clienteId;

    @SerializedName("trabajador_id")
    public Integer trabajadorId;

    @SerializedName("titulo_trabajo")
    public String tituloTrabajo;

    @SerializedName("descripcion_problema")
    public String descripcionProblema;

    @SerializedName("presupuesto_estimado")
    public Double presupuestoEstimado;

    @SerializedName("fecha_inicio_deseada")
    public String fechaInicioDeseada;

    @SerializedName("estado")
    public String estado; // 'Pendiente', 'En Curso', 'Rechazada', 'Completado'

    @SerializedName("porcentaje_avance")
    public Integer porcentajeAvance;

    public SolicitudTrabajoDto() {}

    public SolicitudTrabajoDto(String clienteId, Integer trabajadorId, String tituloTrabajo, String descripcionProblema, Double presupuestoEstimado, String fechaInicioDeseada) {
        this.clienteId = clienteId;
        this.trabajadorId = trabajadorId;
        this.tituloTrabajo = tituloTrabajo;
        this.descripcionProblema = descripcionProblema;
        this.presupuestoEstimado = presupuestoEstimado;
        this.fechaInicioDeseada = fechaInicioDeseada;
        this.estado = "Pendiente";
        this.porcentajeAvance = 0;
    }
}

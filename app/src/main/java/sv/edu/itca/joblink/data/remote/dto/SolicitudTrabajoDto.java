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

    @SerializedName("estado")
    public String estado; // 'pendiente', 'cotizado', 'en_curso', 'completado', 'rechazado'

    @SerializedName("porcentaje_avance")
    public Integer porcentajeAvance;

    public SolicitudTrabajoDto() {}

    public SolicitudTrabajoDto(String clienteId, Integer trabajadorId, String tituloTrabajo, String descripcionProblema, Double presupuestoEstimado) {
        this.clienteId = clienteId;
        this.trabajadorId = trabajadorId;
        this.tituloTrabajo = tituloTrabajo;
        this.descripcionProblema = descripcionProblema;
        this.presupuestoEstimado = presupuestoEstimado;
        this.estado = "en_curso";
        this.porcentajeAvance = 10;
    }
}
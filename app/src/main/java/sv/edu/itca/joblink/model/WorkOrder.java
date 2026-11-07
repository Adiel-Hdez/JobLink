package sv.edu.itca.joblink.model;

public class WorkOrder {
    private String id;
    private String title;
    private String clientName;
    private String workerName;
    private int progressPercentage;
    private String status; // "En Curso", "Confirmado", "Completado"

    public WorkOrder(String id, String title, String clientName, String workerName, int progressPercentage, String status) {
        this.id = id;
        this.title = title;
        this.clientName = clientName;
        this.workerName = workerName;
        this.progressPercentage = progressPercentage;
        this.status = status;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getClientName() { return clientName; }
    public String getWorkerName() { return workerName; }
    public int getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(int progressPercentage) { this.progressPercentage = progressPercentage; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
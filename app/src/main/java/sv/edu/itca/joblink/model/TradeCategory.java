package sv.edu.itca.joblink.model;

public class TradeCategory {
    private String id;
    private String name;
    private String groupName; // e.g. "Construcción y Mejoras", "Instalaciones y Mantenimiento", etc.
    private String description;

    public TradeCategory(String id, String name, String groupName, String description) {
        this.id = id;
        this.name = name;
        this.groupName = groupName;
        this.description = description;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getGroupName() { return groupName; }
    public String getDescription() { return description; }
}
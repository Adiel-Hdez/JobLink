package sv.edu.itca.joblink.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sv.edu.itca.joblink.model.ChatConversation;
import sv.edu.itca.joblink.model.ChatMessage;
import sv.edu.itca.joblink.model.TradeCategory;
import sv.edu.itca.joblink.model.User;
import sv.edu.itca.joblink.model.WorkOrder;

public class JobLinkRepository {
    private static JobLinkRepository instance;

    private User currentUser;
    private final List<TradeCategory> categories = new ArrayList<>();
    private final List<User> workers = new ArrayList<>();
    private final List<WorkOrder> workOrders = new ArrayList<>();
    private final List<ChatConversation> conversations = new ArrayList<>();
    private final Map<String, List<ChatMessage>> chatMessagesMap = new HashMap<>();

    private JobLinkRepository() {
        initCategories();
        initWorkers();
        initWorkOrders();
        initConversations();
        
        // Default current user: Carlos G. (Worker)
        currentUser = workers.get(0);
    }

    public static synchronized JobLinkRepository getInstance() {
        if (instance == null) {
            instance = new JobLinkRepository();
        }
        return instance;
    }

    private void initCategories() {
        // Construcción y Mejoras del Hogar
        categories.add(new TradeCategory("c1", "Albañilería y Enchapado", "Construcción y Mejoras del Hogar", "Muros, aceras, cerámica y remodelaciones"));
        categories.add(new TradeCategory("c2", "Carpintería y Ebanistería", "Construcción y Mejoras del Hogar", "Muebles a medida, puertas, closets"));
        categories.add(new TradeCategory("c3", "Balconería y Soldadura", "Construcción y Mejoras del Hogar", "Portones, balcones y estructuras metálicas"));
        categories.add(new TradeCategory("c4", "Cielo Falso y Tabla Yeso", "Construcción y Mejoras del Hogar", "Gypsum, cielo falso e iluminación empotrada"));
        categories.add(new TradeCategory("c5", "Pintura e Impermeabilización", "Construcción y Mejoras del Hogar", "Pintura de fachadas, impermeabilización de techos"));

        // Instalaciones y Mantenimiento Técnico
        categories.add(new TradeCategory("c6", "Fontanería", "Instalaciones y Mantenimiento Técnico", "Reparación de fugas, cañerías, sanitarios y tanques"));
        categories.add(new TradeCategory("c7", "Electricidad Residencial", "Instalaciones y Mantenimiento Técnico", "Cableado, cajas térmicas, ventiladores y duchas"));
        categories.add(new TradeCategory("c8", "Aire Acondicionado", "Instalaciones y Mantenimiento Técnico", "Instalación y mantenimiento de minisplits"));
        categories.add(new TradeCategory("c9", "Reparación de Línea Blanca", "Instalaciones y Mantenimiento Técnico", "Refrigeradoras, lavadoras y cocinas a domicilio"));
        categories.add(new TradeCategory("c10", "Cerrajería", "Instalaciones y Mantenimiento Técnico", "Aperturas de emergencia y cambio de chapas"));

        // Textiles, Calzado y Tapicería
        categories.add(new TradeCategory("c11", "Costura y Sastrería", "Textiles, Calzado y Tapicería", "Prendas a medida, uniformes y ajustes de ropa"));
        categories.add(new TradeCategory("c12", "Tapicería", "Textiles, Calzado y Tapicería", "Restauración de juegos de sala y asientos de autos"));
        categories.add(new TradeCategory("c13", "Zapatería Artesanal", "Textiles, Calzado y Tapicería", "Reparación de calzado, bolsos y mochilas"));

        // Exteriores y Servicios del Hogar
        categories.add(new TradeCategory("c14", "Jardinería y Poda", "Exteriores y Servicios del Hogar", "Corte de grama, diseño de jardines y poda"));
        categories.add(new TradeCategory("c15", "Limpieza Especializada", "Exteriores y Servicios del Hogar", "Lavado en seco de muebles, alfombras y cisternas"));
        categories.add(new TradeCategory("c16", "Control de Plagas", "Exteriores y Servicios del Hogar", "Fumigación de casas y tratamiento de termitas"));
    }

    private void initWorkers() {
        User carlos = new User("w1", "Carlos G.", "carlos@joblink.sv", User.Role.WORKER, 
                "Carpintero", "Carpintería y Ebanistería", "Santa Ana, El Salvador", 
                "Experto ebanista con más de 10 años elaborando muebles finos y remodelaciones en madera.", 
                4.8, 35, true, "+503 7123-4567");
        carlos.getPortfolioPhotos().add("Mesa de comedor de caoba");
        carlos.getPortfolioPhotos().add("Restauración de silla antigua");
        carlos.getPortfolioPhotos().add("Closet a medida con espejo");
        carlos.getPortfolioPhotos().add("Gabinete de cocina moderno");
        carlos.getPortfolioPhotos().add("Puerta principal de cedro");
        carlos.getPortfolioPhotos().add("Estante flotante de madera");

        User marta = new User("w2", "Marta L.", "marta@joblink.sv", User.Role.WORKER, 
                "Costurera", "Costura y Sastrería", "Santa Ana Centro", 
                "Descripción del Taller de Costura: Especialistas en vestidos de gala, trajes a medida y confección de alta calidad.", 
                4.8, 42, true, "+503 7234-5678");
        marta.getPortfolioPhotos().add("Vestido de noche elegante");
        marta.getPortfolioPhotos().add("Traje de saco y pantalón");
        marta.getPortfolioPhotos().add("Ajuste de vestidos de novia");
        marta.getPortfolioPhotos().add("Uniformes escolares");

        User pedro = new User("w3", "Pedro R.", "pedro@joblink.sv", User.Role.WORKER, 
                "Albañil y Enchapador", "Albañilería y Enchapado", "Santa Ana, Col. El Palmar", 
                "Construcción de muros, pegado de cerámica, remodelación de baños y cocinas.", 
                4.9, 50, false, "+503 7345-6789");

        User ana = new User("w4", "Ana M.", "ana@joblink.sv", User.Role.WORKER, 
                "Técnica en Línea Blanca", "Reparación de Línea Blanca", "Santa Ana, IVU", 
                "Diagnóstico y reparación a domicilio de refrigeradoras, cocinas y lavadoras.", 
                4.7, 28, true, "+503 7456-7890");

        workers.add(carlos);
        workers.add(marta);
        workers.add(pedro);
        workers.add(ana);
    }

    private void initWorkOrders() {
        workOrders.add(new WorkOrder("wo1", "Mesa para Pedro", "Pedro R.", "Carlos G.", 50, "En Curso"));
        workOrders.add(new WorkOrder("wo2", "Reparación Silla", "Ana M.", "Carlos G.", 80, "En Curso"));
        workOrders.add(new WorkOrder("wo3", "Confección de Cortinas", "Marta L.", "Carlos G.", 100, "Completado"));
    }

    private void initConversations() {
        conversations.add(new ChatConversation("conv1", "Marta L.", "Costurera", "¡Hola! ¿Cuándo puedo enviar las medidas?", "3h", 3));
        conversations.add(new ChatConversation("conv2", "Pedro R.", "Carpintería", "¿Confirmamos el horario de entrega?", "1d", 0));
        conversations.add(new ChatConversation("conv3", "Ana M.", "Reparación", "Detalles del servicio recibido", "3d", 1));

        List<ChatMessage> martaMessages = new ArrayList<>();
        martaMessages.add(new ChatMessage("m1", "w1", "Hola, le envío la propuesta para el proyecto.", "10:00 AM", true));
        martaMessages.add(new ChatMessage("m2", "w2", "¡Hola! ¿Cuándo puedo enviar las medidas?", "10:15 AM", false));
        martaMessages.add(new ChatMessage("m3", "w1", "Por medio de este chat me puede mandar la información.", "10:20 AM", true));
        chatMessagesMap.put("conv1", martaMessages);
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public List<TradeCategory> getCategories() {
        return categories;
    }

    public List<User> getWorkers() {
        return workers;
    }

    public List<WorkOrder> getWorkOrders() {
        return workOrders;
    }

    public List<ChatConversation> getConversations() {
        return conversations;
    }

    public List<ChatMessage> getChatMessages(String conversationId) {
        if (!chatMessagesMap.containsKey(conversationId)) {
            chatMessagesMap.put(conversationId, new ArrayList<>());
        }
        return chatMessagesMap.get(conversationId);
    }

    public void addChatMessage(String conversationId, String text, boolean isMine) {
        List<ChatMessage> messages = getChatMessages(conversationId);
        String id = "m" + (messages.size() + 1);
        messages.add(new ChatMessage(id, currentUser.getId(), text, "Ahora", isMine));
        
        for (ChatConversation conversation : conversations) {
            if (conversation.getId().equals(conversationId)) {
                conversation.setLastMessage(text);
                break;
            }
        }
    }

    public void addWorkOrder(WorkOrder order) {
        workOrders.add(order);
    }
}
package sv.edu.itca.joblink.data.remote;

public class SupabaseConfig {
    public static final String SUPABASE_URL = "https://mffntpooyfehakwpzqoo.supabase.co/";
    
    // Anon Key supplied or updated from Supabase Dashboard -> Project Settings -> API
    private static String anonKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1mZm50cG9veWZlaGFrd3B6cW9vIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MDAwMDAwMDAsImV4cCI6MjAxNTAwMDAwMH0.placeholder";

    public static String getAnonKey() {
        return anonKey;
    }

    public static void setAnonKey(String key) {
        if (key != null && !key.trim().isEmpty()) {
            anonKey = key.trim();
        }
    }
}
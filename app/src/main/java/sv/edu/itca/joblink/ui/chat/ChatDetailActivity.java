package sv.edu.itca.joblink.ui.chat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import sv.edu.itca.joblink.databinding.ActivityChatDetailBinding;
import sv.edu.itca.joblink.model.ChatMessage;
import sv.edu.itca.joblink.model.User;
import sv.edu.itca.joblink.model.WorkOrder;
import sv.edu.itca.joblink.repository.JobLinkRepository;
import sv.edu.itca.joblink.ui.adapter.ChatMessageAdapter;

public class ChatDetailActivity extends AppCompatActivity {

    private ActivityChatDetailBinding binding;
    private ChatMessageAdapter adapter;
    private final List<ChatMessage> messageList = new ArrayList<>();

    private String conversationId = null;
    private String contactId = null;
    private String contactName = "Moisés Adiel Rivera Hernández";
    private String contactProfession = "Albañilería y Enchapado";

    private final Handler pollHandler = new Handler(Looper.getMainLooper());
    private final Runnable pollRunnable = new Runnable() {
        @Override
        public void run() {
            if (conversationId != null) {
                loadMessagesFromSupabase();
            }
            pollHandler.postDelayed(this, 2500); // Auto-refresh messages every 2.5s
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityChatDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.rootChatDetail, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (getIntent().hasExtra("contact_id")) {
            contactId = getIntent().getStringExtra("contact_id");
        }
        if (getIntent().hasExtra("contact_name")) {
            contactName = getIntent().getStringExtra("contact_name");
        }
        if (getIntent().hasExtra("contact_profession")) {
            contactProfession = getIntent().getStringExtra("contact_profession");
        }
        if (getIntent().hasExtra("conversation_id")) {
            conversationId = getIntent().getStringExtra("conversation_id");
        }

        binding.tvChatContactName.setText(contactName + " / " + contactProfession);
        binding.btnChatBack.setOnClickListener(v -> finish());

        adapter = new ChatMessageAdapter(messageList);
        binding.rvMessages.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMessages.setAdapter(adapter);

        User currentUser = JobLinkRepository.getInstance().getCurrentUser();
        String myUuid = currentUser != null && currentUser.getId() != null ? currentUser.getId() : "client_uuid";
        String targetWorkerUuid = contactId != null ? contactId : "w_moises";

        // Step A: Get or Create Conversation Room in Supabase
        if (conversationId == null) {
            JobLinkRepository.getInstance().getOrCreateConversacionInSupabase(myUuid, targetWorkerUuid, new JobLinkRepository.RepositoryCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    conversationId = result;
                    loadMessagesFromSupabase();
                }

                @Override
                public void onError(String message) {
                    conversationId = "conv_fallback";
                    loadMessagesFromSupabase();
                }
            });
        } else {
            loadMessagesFromSupabase();
        }

        binding.btnSendMessage.setOnClickListener(v -> {
            String text = binding.etMessageInput.getText() != null ? binding.etMessageInput.getText().toString().trim() : "";
            if (!text.isEmpty()) {
                binding.etMessageInput.setText("");
                String convToUse = conversationId != null ? conversationId : "conv_fallback";
                
                // Post message with exact conversacion_id & sender_id
                JobLinkRepository.getInstance().sendMensajeToSupabase(convToUse, text, new JobLinkRepository.RepositoryCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        loadMessagesFromSupabase();
                    }

                    @Override
                    public void onError(String message) {
                        loadMessagesFromSupabase();
                    }
                });
            }
        });

        binding.btnCrearOrdenTrabajo.setOnClickListener(v -> showCreateWorkOrderDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        pollHandler.post(pollRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        pollHandler.removeCallbacks(pollRunnable);
    }

    private void loadMessagesFromSupabase() {
        if (conversationId == null) return;

        JobLinkRepository.getInstance().fetchMensajesFromSupabase(conversationId, new JobLinkRepository.RepositoryCallback<List<ChatMessage>>() {
            @Override
            public void onSuccess(List<ChatMessage> result) {
                if (result != null) {
                    messageList.clear();
                    messageList.addAll(result);
                    adapter.notifyDataSetChanged();
                    if (!messageList.isEmpty()) {
                        binding.rvMessages.scrollToPosition(messageList.size() - 1);
                    }
                }
            }

            @Override
            public void onError(String message) {}
        });
    }

    private void showCreateWorkOrderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Crear Orden de Trabajo / Cita");
        
        final EditText input = new EditText(this);
        input.setHint("Ej. Cotización Mueble de Cocina");
        input.setPadding(32, 32, 32, 32);
        builder.setView(input);

        builder.setPositiveButton("CREAR ORDEN", (dialog, which) -> {
            String title = input.getText().toString().trim();
            if (title.isEmpty()) title = "Orden con " + contactName;

            WorkOrder order = new WorkOrder("wo_" + System.currentTimeMillis(), title, "Cliente", contactName, 10, "En Curso");
            JobLinkRepository.getInstance().addWorkOrder(order);
            loadMessagesFromSupabase();

            Toast.makeText(this, "¡Orden de trabajo creada con éxito!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}
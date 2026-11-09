package sv.edu.itca.joblink.ui.chat;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;

import sv.edu.itca.joblink.databinding.ActivityChatDetailBinding;
import sv.edu.itca.joblink.model.ChatMessage;
import sv.edu.itca.joblink.model.WorkOrder;
import sv.edu.itca.joblink.repository.JobLinkRepository;
import sv.edu.itca.joblink.ui.adapter.ChatMessageAdapter;

public class ChatDetailActivity extends AppCompatActivity {

    private ActivityChatDetailBinding binding;
    private ChatMessageAdapter adapter;
    private List<ChatMessage> messageList;
    private String conversationId = "conv1";
    private String contactName = "Carlos G.";
    private String contactProfession = "Carpintería";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityChatDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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

        messageList = JobLinkRepository.getInstance().getChatMessages(conversationId);
        adapter = new ChatMessageAdapter(messageList);

        binding.rvMessages.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMessages.setAdapter(adapter);

        binding.btnSendMessage.setOnClickListener(v -> {
            String text = binding.etMessageInput.getText() != null ? binding.etMessageInput.getText().toString().trim() : "";
            if (!text.isEmpty()) {
                JobLinkRepository.getInstance().addChatMessage(conversationId, text, true);
                binding.etMessageInput.setText("");
                adapter.notifyItemInserted(messageList.size() - 1);
                binding.rvMessages.smoothScrollToPosition(messageList.size() - 1);
            }
        });

        // "CREAR ORDEN DE TRABAJO / CITA" Dialog matching Figma design
        binding.btnCrearOrdenTrabajo.setOnClickListener(v -> showCreateWorkOrderDialog());
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

            JobLinkRepository.getInstance().addChatMessage(conversationId, "📋 Orden de trabajo creada: " + title, true);
            adapter.notifyItemInserted(messageList.size() - 1);

            Toast.makeText(this, "¡Orden de trabajo creada con éxito!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}
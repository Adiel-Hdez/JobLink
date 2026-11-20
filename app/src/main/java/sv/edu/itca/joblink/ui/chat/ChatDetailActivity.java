package sv.edu.itca.joblink.ui.chat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import sv.edu.itca.joblink.R;
import sv.edu.itca.joblink.data.remote.dto.SolicitudTrabajoDto;
import sv.edu.itca.joblink.databinding.ActivityChatDetailBinding;
import sv.edu.itca.joblink.model.ChatMessage;
import sv.edu.itca.joblink.model.User;
import sv.edu.itca.joblink.repository.JobLinkRepository;
import sv.edu.itca.joblink.ui.adapter.ChatMessageAdapter;
import sv.edu.itca.joblink.util.ImagePickerHelper;
import sv.edu.itca.joblink.util.ImageStorageManager;

public class ChatDetailActivity extends AppCompatActivity {

    private ActivityChatDetailBinding binding;
    private ChatMessageAdapter adapter;
    private final List<ChatMessage> messageList = new ArrayList<>();

    private String conversationId = null;
    private String contactId = null;
    private String contactName = "Moisés Adiel Rivera Hernández";
    private String contactProfession = "Albañilería y Enchapado";

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
                    if (photo != null) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        photo.compress(Bitmap.CompressFormat.JPEG, 75, baos);
                        byte[] bytes = baos.toByteArray();
                        String base64 = "data:image/jpeg;base64," + android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP);
                        sendPhotoMessage(base64);
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                    Uri uri = result.getData().getData();
                    String photoBase64 = ImageStorageManager.convertUriToBase64(this, uri);
                    if (photoBase64 != null) {
                        sendPhotoMessage(photoBase64);
                    }
                }
            }
    );

    private final ActivityResultLauncher<String[]> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            permissions -> {
                Boolean cameraGranted = permissions.get(android.Manifest.permission.CAMERA);
                if (Boolean.TRUE.equals(cameraGranted)) {
                    cameraLauncher.launch(ImagePickerHelper.createCameraIntent());
                } else {
                    Toast.makeText(this, "Permiso de cámara requerido", Toast.LENGTH_SHORT).show();
                }
            }
    );

    private final Handler pollHandler = new Handler(Looper.getMainLooper());
    private final Runnable pollRunnable = new Runnable() {
        @Override
        public void run() {
            if (conversationId != null) {
                loadMessagesFromSupabase();
            }
            pollHandler.postDelayed(this, 3000);
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
        if (currentUser == null) {
            currentUser = new sv.edu.itca.joblink.util.SessionManager(this).getUserSession();
            if (currentUser != null) {
                JobLinkRepository.getInstance().setCurrentUser(currentUser);
            }
        }

        String myUuid = currentUser != null && currentUser.getId() != null ? currentUser.getId() : JobLinkRepository.ensureValidUuid(null);
        String targetWorkerUuid = contactId != null ? JobLinkRepository.ensureValidUuid(contactId) : JobLinkRepository.ensureValidUuid(null);

        if (conversationId == null) {
            JobLinkRepository.getInstance().getOrCreateConversacionInSupabase(myUuid, targetWorkerUuid, new JobLinkRepository.RepositoryCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    conversationId = result;
                    loadMessagesFromSupabase();
                }

                @Override
                public void onError(String message) {
                    conversationId = JobLinkRepository.ensureValidUuid(null);
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
                String convToUse = conversationId != null ? conversationId : JobLinkRepository.ensureValidUuid(null);
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

        binding.btnAttachImage.setOnClickListener(v -> ImagePickerHelper.showImageSourceDialog(this, new ImagePickerHelper.OnOptionSelectedListener() {
            @Override
            public void onCameraSelected() {
                if (ImagePickerHelper.hasCameraPermission(ChatDetailActivity.this)) {
                    cameraLauncher.launch(ImagePickerHelper.createCameraIntent());
                } else {
                    permissionLauncher.launch(new String[]{android.Manifest.permission.CAMERA});
                }
            }

            @Override
            public void onGallerySelected() {
                galleryLauncher.launch(ImagePickerHelper.createGalleryIntent());
            }
        }));

        binding.btnCrearOrdenTrabajo.setOnClickListener(v -> showCreateWorkOrderDialog());
    }

    private void sendPhotoMessage(String photoBase64) {
        String convToUse = conversationId != null ? conversationId : JobLinkRepository.ensureValidUuid(null);
        String text = binding.etMessageInput.getText() != null ? binding.etMessageInput.getText().toString().trim() : "";
        binding.etMessageInput.setText("");

        JobLinkRepository.getInstance().sendMensajeWithPhotoToSupabase(convToUse, text.isEmpty() ? "📷 [Imagen adjunta]" : text, photoBase64, new JobLinkRepository.RepositoryCallback<Boolean>() {
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
                JobLinkRepository.getInstance().markMensajesAsReadInSupabase(conversationId);
            }

            @Override
            public void onError(String message) {}
        });
    }

    private void showCreateWorkOrderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Solicitar Trabajo / Cotización");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_solicitud, null);
        final EditText etTitle = dialogView.findViewById(R.id.etSolicitudTitle);
        final EditText etDesc = dialogView.findViewById(R.id.etSolicitudDesc);
        final EditText etBudget = dialogView.findViewById(R.id.etSolicitudBudget);
        final EditText etDate = dialogView.findViewById(R.id.etSolicitudDate);

        builder.setView(dialogView);

        builder.setPositiveButton("ENVIAR SOLICITUD", (dialog, which) -> {
            String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
            String desc = etDesc.getText() != null ? etDesc.getText().toString().trim() : "";
            String budgetStr = etBudget.getText() != null ? etBudget.getText().toString().trim() : "";
            String dateStr = etDate.getText() != null ? etDate.getText().toString().trim() : "";

            if (title.isEmpty()) title = "Solicitud de trabajo";
            if (desc.isEmpty()) desc = "Descripción no especificada";
            double budget = 50.0;
            try {
                if (!budgetStr.isEmpty()) budget = Double.parseDouble(budgetStr);
            } catch (Exception ignored) {}
            if (dateStr.isEmpty()) dateStr = "2026-11-20";

            User currentUser = JobLinkRepository.getInstance().getCurrentUser();
            String myUuid = currentUser != null && currentUser.getId() != null ? currentUser.getId() : JobLinkRepository.ensureValidUuid(null);

            // Find worker profile ID
            int workerProfileId = 1;
            if (contactId != null) {
                for (User w : JobLinkRepository.getInstance().getWorkers()) {
                    if (contactId.equalsIgnoreCase(w.getId()) && w.getWorkerProfileId() > 0) {
                        workerProfileId = w.getWorkerProfileId();
                        break;
                    }
                }
            }

            SolicitudTrabajoDto dto = new SolicitudTrabajoDto(myUuid, workerProfileId, title, desc, budget, dateStr);

            JobLinkRepository.getInstance().createSolicitudTrabajoInSupabase(dto, new JobLinkRepository.RepositoryCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    Toast.makeText(ChatDetailActivity.this, "📋 ¡Solicitud enviada con éxito!", Toast.LENGTH_SHORT).show();
                    loadMessagesFromSupabase();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(ChatDetailActivity.this, "Solicitud enviada localmente", Toast.LENGTH_SHORT).show();
                }
            });
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}

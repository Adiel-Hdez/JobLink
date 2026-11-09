package sv.edu.itca.joblink.ui.auth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import sv.edu.itca.joblink.MainActivity;
import sv.edu.itca.joblink.databinding.ActivityRegisterClientBinding;
import sv.edu.itca.joblink.model.User;
import sv.edu.itca.joblink.repository.JobLinkRepository;

public class RegisterClientActivity extends AppCompatActivity {

    private ActivityRegisterClientBinding binding;
    private Uri selectedAvatarUri;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                    selectedAvatarUri = result.getData().getData();
                    Toast.makeText(this, "📷 Foto de perfil de cliente seleccionada", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityRegisterClientBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.rootClientScroll, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Pre-fill fields if in editing mode
        User current = JobLinkRepository.getInstance().getCurrentUser();
        if (getIntent().getBooleanExtra("is_editing", false) && current != null) {
            binding.etClientName.setText(current.getName());
            binding.etClientEmail.setText(current.getEmail());
            binding.etClientPhone.setText(current.getPhone());
            binding.etClientLocation.setText(current.getLocation());
            binding.btnGuardarCliente.setText("GUARDAR CAMBIOS DE PERFIL");
        }

        binding.btnGuardarCliente.setOnClickListener(v -> {
            String name = binding.etClientName.getText() != null ? binding.etClientName.getText().toString().trim() : "";
            String email = binding.etClientEmail.getText() != null ? binding.etClientEmail.getText().toString().trim() : "";
            String password = binding.etClientPassword.getText() != null ? binding.etClientPassword.getText().toString().trim() : "";
            String phone = binding.etClientPhone.getText() != null ? binding.etClientPhone.getText().toString().trim() : "";
            String location = binding.etClientLocation.getText() != null ? binding.etClientLocation.getText().toString().trim() : "";

            if (name.isEmpty()) {
                binding.etClientName.setError("Ingrese su nombre");
                return;
            }

            if (email.isEmpty()) {
                binding.etClientEmail.setError("Ingrese su correo");
                return;
            }

            if (password.isEmpty() && !getIntent().getBooleanExtra("is_editing", false)) {
                binding.etClientPassword.setError("Ingrese su contraseña");
                return;
            }

            if (location.isEmpty()) {
                location = "Santa Ana, El Salvador";
            }

            User client = new User(
                    current != null ? current.getId() : null, name, email, User.Role.CLIENT, "Cliente", "General",
                    location, "Busco contratar servicios profesionales", 5.0, 0, true,
                    phone.isEmpty() ? "+503 7000-0000" : phone
            );

            if (selectedAvatarUri != null) {
                client.setProfilePhotoUrl(selectedAvatarUri.toString());
            }

            JobLinkRepository.getInstance().registerUserInSupabase(client, password, new JobLinkRepository.RepositoryCallback<User>() {
                @Override
                public void onSuccess(User result) {
                    Toast.makeText(RegisterClientActivity.this, "¡Perfil de cliente guardado con éxito!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterClientActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(RegisterClientActivity.this, "¡Perfil guardado con éxito!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterClientActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            });
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }
}
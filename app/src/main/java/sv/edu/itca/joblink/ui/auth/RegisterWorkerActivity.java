package sv.edu.itca.joblink.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

import sv.edu.itca.joblink.MainActivity;
import sv.edu.itca.joblink.databinding.ActivityRegisterWorkerBinding;
import sv.edu.itca.joblink.model.TradeCategory;
import sv.edu.itca.joblink.model.User;
import sv.edu.itca.joblink.repository.JobLinkRepository;

public class RegisterWorkerActivity extends AppCompatActivity {

    private ActivityRegisterWorkerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityRegisterWorkerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.rootWorkerScroll, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupProfessionDropdown();

        binding.btnGuardarPerfil.setOnClickListener(v -> {
            String name = binding.etWorkerName.getText() != null ? binding.etWorkerName.getText().toString().trim() : "";
            String email = binding.etWorkerEmail.getText() != null ? binding.etWorkerEmail.getText().toString().trim() : "";
            String password = binding.etWorkerPassword.getText() != null ? binding.etWorkerPassword.getText().toString().trim() : "";
            String profession = binding.actvProfession.getText() != null ? binding.actvProfession.getText().toString().trim() : "";
            String location = binding.etWorkerLocation.getText() != null ? binding.etWorkerLocation.getText().toString().trim() : "";
            String bio = binding.etWorkerBio.getText() != null ? binding.etWorkerBio.getText().toString().trim() : "";

            if (name.isEmpty()) {
                binding.etWorkerName.setError("Ingrese su nombre");
                return;
            }

            if (email.isEmpty()) {
                email = name.toLowerCase().replace(" ", "") + "@joblink.sv";
            }

            if (password.isEmpty()) {
                password = "password123";
            }

            if (profession.isEmpty()) {
                profession = "Carpintero";
            }

            if (location.isEmpty()) {
                location = "Santa Ana, El Salvador";
            }

            User newWorker = new User(null, name, email,
                    User.Role.WORKER, profession, "General", location, bio, 5.0, 0, true, "+503 7000-1111");

            JobLinkRepository.getInstance().registerUserInSupabase(newWorker, password, new JobLinkRepository.RepositoryCallback<User>() {
                @Override
                public void onSuccess(User result) {
                    Toast.makeText(RegisterWorkerActivity.this, "¡Perfil de trabajador creado con éxito!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterWorkerActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(RegisterWorkerActivity.this, "¡Perfil guardado en modo local!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterWorkerActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            });
        });
    }

    private void setupProfessionDropdown() {
        List<String> professionNames = new ArrayList<>();
        for (TradeCategory cat : JobLinkRepository.getInstance().getCategories()) {
            professionNames.add(cat.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, professionNames);
        binding.actvProfession.setAdapter(adapter);
    }
}
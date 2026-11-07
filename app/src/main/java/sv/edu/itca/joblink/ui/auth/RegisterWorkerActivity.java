package sv.edu.itca.joblink.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
        binding = ActivityRegisterWorkerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupProfessionDropdown();

        binding.btnGuardarPerfil.setOnClickListener(v -> {
            String name = binding.etWorkerName.getText() != null ? binding.etWorkerName.getText().toString().trim() : "";
            String profession = binding.actvProfession.getText() != null ? binding.actvProfession.getText().toString().trim() : "";
            String location = binding.etWorkerLocation.getText() != null ? binding.etWorkerLocation.getText().toString().trim() : "";
            String bio = binding.etWorkerBio.getText() != null ? binding.etWorkerBio.getText().toString().trim() : "";

            if (name.isEmpty()) {
                binding.etWorkerName.setError("Ingrese su nombre");
                return;
            }

            if (profession.isEmpty()) {
                profession = "Carpintero";
            }

            if (location.isEmpty()) {
                location = "Santa Ana, El Salvador";
            }

            User newWorker = new User("w_new", name, name.toLowerCase().replace(" ", "") + "@joblink.sv",
                    User.Role.WORKER, profession, "General", location, bio, 5.0, 0, true, "+503 7000-1111");

            JobLinkRepository.getInstance().setCurrentUser(newWorker);
            Toast.makeText(this, "Perfil Guardado Con Éxito", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(RegisterWorkerActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
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
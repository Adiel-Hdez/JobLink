package sv.edu.itca.joblink.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import sv.edu.itca.joblink.R;
import sv.edu.itca.joblink.databinding.ActivityAuthBinding;
import sv.edu.itca.joblink.model.User;
import sv.edu.itca.joblink.repository.JobLinkRepository;
import sv.edu.itca.joblink.MainActivity;

public class AuthActivity extends AppCompatActivity {

    private ActivityAuthBinding binding;
    private User.Role selectedRole = User.Role.WORKER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Role Selector logic matching Figma Image 2
        binding.cardRoleWorker.setOnClickListener(v -> selectRole(User.Role.WORKER));
        binding.cardRoleClient.setOnClickListener(v -> selectRole(User.Role.CLIENT));

        binding.btnIngresar.setOnClickListener(v -> {
            String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
            if (email.isEmpty()) {
                binding.etEmail.setError("Ingrese su correo");
                return;
            }
            Toast.makeText(this, "¡Bienvenido a JobLink!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AuthActivity.this, MainActivity.class));
            finish();
        });

        binding.btnCrearCuenta.setOnClickListener(v -> {
            if (selectedRole == User.Role.WORKER) {
                startActivity(new Intent(AuthActivity.this, RegisterWorkerActivity.class));
            } else {
                // Client login/register -> navigate to main app
                User client = new User("c_user", "Cliente Nuevo", "cliente@joblink.sv", 
                        User.Role.CLIENT, "Cliente", "General", "Santa Ana", "Buscando servicios", 5.0, 0, true, "+503 7000-0000");
                JobLinkRepository.getInstance().setCurrentUser(client);
                Toast.makeText(this, "Cuenta de Cliente Creada", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(AuthActivity.this, MainActivity.class));
                finish();
            }
        });

        binding.tvForgotPassword.setOnClickListener(v -> 
            Toast.makeText(this, "Instrucciones enviadas a su correo", Toast.LENGTH_SHORT).show()
        );
    }

    private void selectRole(User.Role role) {
        this.selectedRole = role;
        if (role == User.Role.WORKER) {
            binding.cardRoleWorker.setBackgroundResource(R.drawable.bg_role_card_selected);
            binding.cardRoleClient.setBackgroundResource(R.drawable.bg_role_card_unselected);
            binding.ivRoleWorkerIcon.setColorFilter(getColor(R.color.teal_primary));
            binding.ivRoleClientIcon.setColorFilter(getColor(R.color.navy_primary));
        } else {
            binding.cardRoleWorker.setBackgroundResource(R.drawable.bg_role_card_unselected);
            binding.cardRoleClient.setBackgroundResource(R.drawable.bg_role_card_selected);
            binding.ivRoleWorkerIcon.setColorFilter(getColor(R.color.navy_primary));
            binding.ivRoleClientIcon.setColorFilter(getColor(R.color.teal_primary));
        }
    }
}
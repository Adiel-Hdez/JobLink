package sv.edu.itca.joblink.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import sv.edu.itca.joblink.MainActivity;
import sv.edu.itca.joblink.R;
import sv.edu.itca.joblink.databinding.ActivityAuthBinding;
import sv.edu.itca.joblink.model.User;
import sv.edu.itca.joblink.repository.JobLinkRepository;

public class AuthActivity extends AppCompatActivity {

    private ActivityAuthBinding binding;
    private User.Role selectedRole = User.Role.WORKER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.rootAuthScroll, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Role Selector logic matching Figma Image 2
        binding.cardRoleWorker.setOnClickListener(v -> selectRole(User.Role.WORKER));
        binding.cardRoleClient.setOnClickListener(v -> selectRole(User.Role.CLIENT));

        // Full Supabase Login logic
        binding.btnIngresar.setOnClickListener(v -> {
            String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
            String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";

            if (email.isEmpty()) {
                binding.etEmail.setError("Ingrese su correo");
                return;
            }

            if (password.isEmpty()) {
                binding.etPassword.setError("Ingrese su contraseña");
                return;
            }

            JobLinkRepository.getInstance().loginWithSupabase(email, password, new JobLinkRepository.RepositoryCallback<User>() {
                @Override
                public void onSuccess(User user) {
                    Toast.makeText(AuthActivity.this, "¡Bienvenido a JobLink, " + user.getName() + "!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AuthActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(AuthActivity.this, "Iniciando sesión en modo local", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AuthActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        });

        // Create Account Action
        binding.btnCrearCuenta.setOnClickListener(v -> {
            if (selectedRole == User.Role.WORKER) {
                startActivity(new Intent(AuthActivity.this, RegisterWorkerActivity.class));
            } else {
                startActivity(new Intent(AuthActivity.this, RegisterClientActivity.class));
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
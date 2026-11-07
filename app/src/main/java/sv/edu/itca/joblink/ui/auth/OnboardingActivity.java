package sv.edu.itca.joblink.ui.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import sv.edu.itca.joblink.databinding.ActivityOnboardingBinding;

public class OnboardingActivity extends AppCompatActivity {

    private ActivityOnboardingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnComenzar.setOnClickListener(v -> {
            Intent intent = new Intent(OnboardingActivity.this, AuthActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
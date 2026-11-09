package sv.edu.itca.joblink.ui.profile;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import sv.edu.itca.joblink.databinding.ActivityPremiumSubscriptionBinding;
import sv.edu.itca.joblink.model.User;
import sv.edu.itca.joblink.repository.JobLinkRepository;

public class PremiumSubscriptionActivity extends AppCompatActivity {

    private ActivityPremiumSubscriptionBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityPremiumSubscriptionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.rootPremiumScroll, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.btnSubscribePremium.setOnClickListener(v -> {
            User user = JobLinkRepository.getInstance().getCurrentUser();
            if (user != null) {
                user.setPremium(true);
                Toast.makeText(this, "⭐ ¡Felicidades! Ahora eres un Profesional PREMIUM por $2.99/mes", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }
}
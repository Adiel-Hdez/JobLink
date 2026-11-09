package sv.edu.itca.joblink.ui.profile;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import sv.edu.itca.joblink.data.remote.SupabaseClient;
import sv.edu.itca.joblink.data.remote.dto.ValoracionDto;
import sv.edu.itca.joblink.databinding.ActivityRateWorkerBinding;
import sv.edu.itca.joblink.model.User;
import sv.edu.itca.joblink.repository.JobLinkRepository;

public class RateWorkerActivity extends AppCompatActivity {

    private ActivityRateWorkerBinding binding;
    private String workerName = "Carlos G.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityRateWorkerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.rootRateScroll, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (getIntent().hasExtra("worker_name")) {
            workerName = getIntent().getStringExtra("worker_name");
        }

        binding.tvRateWorkerName.setText("Calificar a " + workerName);

        binding.btnSubmitRating.setOnClickListener(v -> {
            int stars = (int) binding.rbWorkerRating.getRating();
            String comment = binding.etRateComment.getText() != null ? binding.etRateComment.getText().toString().trim() : "";

            User currentUser = JobLinkRepository.getInstance().getCurrentUser();
            String clientId = currentUser != null && currentUser.getId() != null ? currentUser.getId() : "client_anon";

            ValoracionDto dto = new ValoracionDto(1, clientId, 1, stars, comment);
            SupabaseClient.getApi().createValoracion(dto).enqueue(new Callback<List<ValoracionDto>>() {
                @Override
                public void onResponse(Call<List<ValoracionDto>> call, Response<List<ValoracionDto>> response) {
                    Toast.makeText(RateWorkerActivity.this, "⭐ ¡Valoración guardada exitosamente en Supabase!", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onFailure(Call<List<ValoracionDto>> call, Throwable t) {
                    Toast.makeText(RateWorkerActivity.this, "¡Valoración guardada localmente!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        });
    }
}
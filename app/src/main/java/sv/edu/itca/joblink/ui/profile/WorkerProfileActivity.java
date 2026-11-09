package sv.edu.itca.joblink.ui.profile;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import sv.edu.itca.joblink.databinding.ActivityWorkerProfileBinding;
import sv.edu.itca.joblink.model.User;
import sv.edu.itca.joblink.repository.JobLinkRepository;
import sv.edu.itca.joblink.ui.chat.ChatDetailActivity;

public class WorkerProfileActivity extends AppCompatActivity {

    private ActivityWorkerProfileBinding binding;
    private User worker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityWorkerProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String workerId = getIntent().getStringExtra("worker_id");
        if (workerId != null) {
            for (User u : JobLinkRepository.getInstance().getWorkers()) {
                if (u.getId().equals(workerId)) {
                    worker = u;
                    break;
                }
            }
        }

        if (worker == null) {
            worker = JobLinkRepository.getInstance().getWorkers().get(1); // Default Marta L. Costurera
        }

        binding.tvPublicProfileName.setText(worker.getName());
        binding.tvPublicProfileTrade.setText(worker.getProfession());
        binding.tvPublicProfileRating.setText(String.format(java.util.Locale.getDefault(), "%.1f ★", worker.getRating()));
        binding.tvPublicProfileBio.setText(worker.getBio());

        binding.btnContactarChat.setOnClickListener(v -> openChat());
        binding.btnOpenFullChat.setOnClickListener(v -> openChat());
    }

    private void openChat() {
        Intent intent = new Intent(WorkerProfileActivity.this, ChatDetailActivity.class);
        intent.putExtra("contact_name", worker.getName());
        intent.putExtra("contact_profession", worker.getProfession());
        startActivity(intent);
    }
}
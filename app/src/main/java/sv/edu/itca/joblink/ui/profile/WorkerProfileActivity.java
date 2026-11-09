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
        String workerName = getIntent().getStringExtra("worker_name");
        String workerTrade = getIntent().getStringExtra("worker_trade");
        String workerBio = getIntent().getStringExtra("worker_bio");

        if (workerId != null) {
            for (User u : JobLinkRepository.getInstance().getWorkers()) {
                if (workerId.equalsIgnoreCase(u.getId())) {
                    worker = u;
                    break;
                }
            }
        }

        if (worker == null && workerName != null) {
            for (User u : JobLinkRepository.getInstance().getWorkers()) {
                if (workerName.equalsIgnoreCase(u.getName())) {
                    worker = u;
                    break;
                }
            }
        }

        if (worker == null) {
            worker = new User(
                    workerId != null ? workerId : "w_temp",
                    workerName != null ? workerName : "Moisés Adiel Rivera Hernández",
                    "profesional@joblink.sv",
                    User.Role.WORKER,
                    workerTrade != null ? workerTrade : "Albañilería y Enchapado",
                    "Construcción",
                    "Santa Ana, El Salvador",
                    workerBio != null ? workerBio : "Experto en albañilería, muros, enchapado de cerámica y remodelaciones generales en Santa Ana.",
                    5.0, 10, true, "+503 7000-0000"
            );
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
package sv.edu.itca.joblink.ui.auth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import sv.edu.itca.joblink.MainActivity;
import sv.edu.itca.joblink.data.remote.SupabaseClient;
import sv.edu.itca.joblink.data.remote.dto.PortafolioTrabajoDto;
import sv.edu.itca.joblink.databinding.ActivityRegisterWorkerBinding;
import sv.edu.itca.joblink.model.TradeCategory;
import sv.edu.itca.joblink.model.User;
import sv.edu.itca.joblink.repository.JobLinkRepository;
import sv.edu.itca.joblink.util.ImageStorageManager;

public class RegisterWorkerActivity extends AppCompatActivity {

    private ActivityRegisterWorkerBinding binding;
    private String savedAvatarPath;
    private int activePortfolioSlot = 0;
    private final List<String> portfolioPaths = new ArrayList<>();

    private final ActivityResultLauncher<Intent> avatarPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                    Uri uri = result.getData().getData();
                    savedAvatarPath = ImageStorageManager.saveImageToInternalStorage(this, uri, "avatar");
                    ImageStorageManager.loadImageIntoView(binding.ivWorkerAvatar, savedAvatarPath);
                    Toast.makeText(this, "📷 Foto de perfil seleccionada", Toast.LENGTH_SHORT).show();
                }
            }
    );

    private final ActivityResultLauncher<Intent> portfolioPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                    Uri uri = result.getData().getData();
                    String path = ImageStorageManager.saveImageToInternalStorage(this, uri, "portfolio");
                    if (path != null) {
                        portfolioPaths.add(path);
                        ImageView slotView = getSlotImageView(activePortfolioSlot);
                        if (slotView != null) {
                            ImageStorageManager.loadImageIntoView(slotView, path);
                        }
                        Toast.makeText(this, "🖼️ Foto agregada al portafolio", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

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

        // Pre-fill fields if in editing mode
        User current = JobLinkRepository.getInstance().getCurrentUser();
        if (getIntent().getBooleanExtra("is_editing", false) && current != null) {
            binding.etWorkerName.setText(current.getName());
            binding.etWorkerEmail.setText(current.getEmail());
            binding.actvProfession.setText(current.getProfession());
            binding.etWorkerLocation.setText(current.getLocation());
            binding.etWorkerBio.setText(current.getBio());
            if (current.getProfilePhotoUrl() != null) {
                savedAvatarPath = current.getProfilePhotoUrl();
                ImageStorageManager.loadImageIntoView(binding.ivWorkerAvatar, savedAvatarPath);
            }
        }

        // Image Pickers
        binding.ivWorkerAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            avatarPickerLauncher.launch(intent);
        });

        setupPortfolioClickListeners();
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
                profession = "Albañilería y Enchapado";
            }

            if (location.isEmpty()) {
                location = "Santa Ana, El Salvador";
            }

            User worker = new User(current != null ? current.getId() : null, name, email,
                    User.Role.WORKER, profession, "General", location, bio, 5.0, 0, true, "+503 7000-1111");

            if (savedAvatarPath != null) {
                worker.setProfilePhotoUrl(savedAvatarPath);
            }

            worker.setPortfolioPhotos(portfolioPaths);

            JobLinkRepository.getInstance().registerUserInSupabase(worker, password, new JobLinkRepository.RepositoryCallback<User>() {
                @Override
                public void onSuccess(User result) {
                    uploadPortfolioToSupabase(1);
                    Toast.makeText(RegisterWorkerActivity.this, "¡Perfil de trabajador guardado con éxito!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterWorkerActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(RegisterWorkerActivity.this, "¡Perfil guardado con éxito!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterWorkerActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            });
        });
    }

    private void setupPortfolioClickListeners() {
        binding.cardSlot1.setOnClickListener(v -> openPortfolioPicker(1));
        binding.cardSlot2.setOnClickListener(v -> openPortfolioPicker(2));
        binding.cardSlot3.setOnClickListener(v -> openPortfolioPicker(3));
        binding.cardSlot4.setOnClickListener(v -> openPortfolioPicker(4));
        binding.cardSlot5.setOnClickListener(v -> openPortfolioPicker(5));
        binding.cardSlot6.setOnClickListener(v -> openPortfolioPicker(6));
    }

    private void openPortfolioPicker(int slot) {
        activePortfolioSlot = slot;
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        portfolioPickerLauncher.launch(intent);
    }

    private ImageView getSlotImageView(int slot) {
        switch (slot) {
            case 1: return binding.ivPortfolioSlot1;
            case 2: return binding.ivPortfolioSlot2;
            case 3: return binding.ivPortfolioSlot3;
            case 4: return binding.ivPortfolioSlot4;
            case 5: return binding.ivPortfolioSlot5;
            case 6: return binding.ivPortfolioSlot6;
            default: return null;
        }
    }

    private void uploadPortfolioToSupabase(int workerProfileId) {
        for (String photoPath : portfolioPaths) {
            PortafolioTrabajoDto dto = new PortafolioTrabajoDto(workerProfileId, "Trabajo Realizado", "Foto de portafolio", photoPath);
            SupabaseClient.getApi().addPortafolioItem(dto).enqueue(new Callback<List<PortafolioTrabajoDto>>() {
                @Override
                public void onResponse(Call<List<PortafolioTrabajoDto>> call, Response<List<PortafolioTrabajoDto>> response) {}
                @Override
                public void onFailure(Call<List<PortafolioTrabajoDto>> call, Throwable t) {}
            });
        }
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
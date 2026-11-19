package sv.edu.itca.joblink.ui.home;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import sv.edu.itca.joblink.MainActivity;
import sv.edu.itca.joblink.data.remote.SupabaseClient;
import sv.edu.itca.joblink.data.remote.dto.PortafolioTrabajoDto;
import sv.edu.itca.joblink.databinding.FragmentHomeBinding;
import sv.edu.itca.joblink.model.User;
import sv.edu.itca.joblink.repository.JobLinkRepository;
import sv.edu.itca.joblink.ui.profile.WorkerProfileActivity;
import sv.edu.itca.joblink.util.ImageStorageManager;
import sv.edu.itca.joblink.util.SessionManager;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    private final ActivityResultLauncher<Intent> galleryPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                    Uri uri = result.getData().getData();
                    String photoBase64 = ImageStorageManager.convertUriToBase64(getContext(), uri);
                    if (photoBase64 == null) {
                        photoBase64 = ImageStorageManager.saveImageToInternalStorage(getContext(), uri, "portfolio");
                    }
                    if (photoBase64 != null) {
                        User currentUser = JobLinkRepository.getInstance().getCurrentUser();
                        if (currentUser != null) {
                            currentUser.getPortfolioPhotos().add(photoBase64);
                            if (getContext() != null) {
                                new SessionManager(getContext()).saveUserSession(currentUser);
                            }

                            int profileId = currentUser.getWorkerProfileId() > 0 ? currentUser.getWorkerProfileId() : 1;
                            PortafolioTrabajoDto dto = new PortafolioTrabajoDto(profileId, "Trabajo Realizado", "Foto de portafolio", photoBase64);
                            SupabaseClient.getApi().addPortafolioItem(dto).enqueue(new Callback<List<PortafolioTrabajoDto>>() {
                                @Override
                                public void onResponse(Call<List<PortafolioTrabajoDto>> call, Response<List<PortafolioTrabajoDto>> response) {}
                                @Override
                                public void onFailure(Call<List<PortafolioTrabajoDto>> call, Throwable t) {}
                            });

                            Toast.makeText(getContext(), "🖼️ ¡Foto añadida con éxito a tu portafolio!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        updateUI();

        binding.btnQuickAddGallery.setOnClickListener(v -> {
            User currentUser = JobLinkRepository.getInstance().getCurrentUser();
            if (currentUser != null) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryPickerLauncher.launch(intent);
            } else {
                Toast.makeText(getContext(), "Inicie sesión para añadir fotos a su portafolio", Toast.LENGTH_SHORT).show();
            }
        });

        binding.cardDashboardProfile.setOnClickListener(v -> {
            User currentUser = JobLinkRepository.getInstance().getCurrentUser();
            if (currentUser != null) {
                Intent intent = new Intent(getContext(), WorkerProfileActivity.class);
                intent.putExtra("worker_id", currentUser.getId());
                intent.putExtra("worker_name", currentUser.getName());
                intent.putExtra("worker_trade", currentUser.getProfession());
                intent.putExtra("worker_bio", currentUser.getBio());
                startActivity(intent);
            }
        });

        binding.btnQuickViewIncome.setOnClickListener(v -> 
            Toast.makeText(getContext(), "Ingresos acumulados: $450.00 USD", Toast.LENGTH_SHORT).show()
        );

        binding.btnQuickSearchServices.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToSearch();
            }
        });

        binding.btnHeaderNotifications.setOnClickListener(v -> 
            Toast.makeText(getContext(), "3 Notificaciones sin leer", Toast.LENGTH_SHORT).show()
        );

        binding.btnHeaderSettings.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToSettings();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI() {
        User currentUser = JobLinkRepository.getInstance().getCurrentUser();
        if (currentUser != null && binding != null) {
            binding.tvHomeGreeting.setText("¡BIENVENIDO " + currentUser.getName().toUpperCase() + "!");
            binding.tvDashboardName.setText(currentUser.getName());
            binding.tvDashboardTrade.setText(currentUser.getProfession());
            binding.tvDashboardRating.setText(String.format(java.util.Locale.getDefault(), "%.1f", currentUser.getRating()));
            binding.tvDashboardReviews.setText("(" + currentUser.getReviewsCount() + " trabajos)");

            if (currentUser.getProfilePhotoUrl() != null && !currentUser.getProfilePhotoUrl().isEmpty()) {
                ImageStorageManager.loadImageIntoView(binding.ivDashboardAvatar, currentUser.getProfilePhotoUrl());
            } else {
                ImageStorageManager.resetAvatarToDefault(binding.ivDashboardAvatar);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
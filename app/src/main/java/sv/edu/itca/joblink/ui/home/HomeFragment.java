package sv.edu.itca.joblink.ui.home;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import sv.edu.itca.joblink.MainActivity;
import sv.edu.itca.joblink.data.remote.SupabaseClient;
import sv.edu.itca.joblink.data.remote.dto.PortafolioTrabajoDto;
import sv.edu.itca.joblink.data.remote.dto.SolicitudTrabajoDto;
import sv.edu.itca.joblink.databinding.FragmentHomeBinding;
import sv.edu.itca.joblink.model.User;
import sv.edu.itca.joblink.repository.JobLinkRepository;
import sv.edu.itca.joblink.ui.profile.WorkerProfileActivity;
import sv.edu.itca.joblink.util.ImagePickerHelper;
import sv.edu.itca.joblink.util.ImageStorageManager;
import sv.edu.itca.joblink.util.SessionManager;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private final List<SolicitudTrabajoDto> jobsInProgressList = new ArrayList<>();

    private final ActivityResultLauncher<Intent> cameraPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
                    if (photo != null) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        photo.compress(Bitmap.CompressFormat.JPEG, 75, baos);
                        byte[] bytes = baos.toByteArray();
                        String photoBase64 = "data:image/jpeg;base64," + android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP);
                        addPhotoToPortfolio(photoBase64);
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> galleryPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                    Uri uri = result.getData().getData();
                    String photoBase64 = ImageStorageManager.convertUriToBase64(getContext(), uri);
                    if (photoBase64 != null) {
                        addPhotoToPortfolio(photoBase64);
                    }
                }
            }
    );

    private final ActivityResultLauncher<String[]> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            permissions -> {
                Boolean cameraGranted = permissions.get(android.Manifest.permission.CAMERA);
                if (Boolean.TRUE.equals(cameraGranted)) {
                    cameraPickerLauncher.launch(ImagePickerHelper.createCameraIntent());
                } else {
                    Toast.makeText(getContext(), "Permiso de cámara requerido", Toast.LENGTH_SHORT).show();
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
        loadDashboardDataFromSupabase();

        binding.btnQuickAddGallery.setOnClickListener(v -> {
            User currentUser = JobLinkRepository.getInstance().getCurrentUser();
            if (currentUser != null) {
                ImagePickerHelper.showImageSourceDialog(getContext(), new ImagePickerHelper.OnOptionSelectedListener() {
                    @Override
                    public void onCameraSelected() {
                        if (ImagePickerHelper.hasCameraPermission(getContext())) {
                            cameraPickerLauncher.launch(ImagePickerHelper.createCameraIntent());
                        } else {
                            permissionLauncher.launch(new String[]{android.Manifest.permission.CAMERA});
                        }
                    }

                    @Override
                    public void onGallerySelected() {
                        galleryPickerLauncher.launch(ImagePickerHelper.createGalleryIntent());
                    }
                });
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
            Toast.makeText(getContext(), "💰 Ingresos acumulados: $450.00 USD", Toast.LENGTH_SHORT).show()
        );

        binding.btnQuickSearchServices.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToSearch();
            }
        });

        binding.layoutUnreadMessages.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToChat();
            }
        });

        binding.cardWorkInProgress1.setOnClickListener(v -> {
            if (jobsInProgressList.size() > 0) {
                showUpdateProgressDialog(jobsInProgressList.get(0));
            }
        });

        binding.cardWorkInProgress2.setOnClickListener(v -> {
            if (jobsInProgressList.size() > 1) {
                showUpdateProgressDialog(jobsInProgressList.get(1));
            }
        });

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
        loadDashboardDataFromSupabase();
    }

    private void addPhotoToPortfolio(String photoBase64) {
        User currentUser = JobLinkRepository.getInstance().getCurrentUser();
        if (currentUser == null || photoBase64 == null) return;

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

    private void loadDashboardDataFromSupabase() {
        if (binding == null) return;

        // Dynamic Unread Messages Count
        JobLinkRepository.getInstance().getUnreadMessagesCount(new JobLinkRepository.RepositoryCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                if (binding != null) {
                    binding.tvUnreadMessagesCount.setText(result + " Mensajes Nuevos");
                }
            }

            @Override
            public void onError(String message) {}
        });

        // Dynamic Pending Requests Count
        JobLinkRepository.getInstance().getPendingSolicitudesCount(new JobLinkRepository.RepositoryCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                if (binding != null) {
                    binding.tvPendingRequestsCount.setText(result + " Nuevas Solicitudes de Cotización");
                }
            }

            @Override
            public void onError(String message) {}
        });

        // Real Jobs in Progress
        JobLinkRepository.getInstance().fetchSolicitudesTrabajoFromSupabase(new JobLinkRepository.RepositoryCallback<List<SolicitudTrabajoDto>>() {
            @Override
            public void onSuccess(List<SolicitudTrabajoDto> result) {
                if (binding == null) return;
                jobsInProgressList.clear();

                if (result != null) {
                    for (SolicitudTrabajoDto s : result) {
                        if (s.estado != null && ("En Curso".equalsIgnoreCase(s.estado.trim()) || "en_curso".equalsIgnoreCase(s.estado.trim()))) {
                            jobsInProgressList.add(s);
                        }
                    }
                }

                if (jobsInProgressList.isEmpty()) {
                    binding.tvNoJobsInProgress.setVisibility(View.VISIBLE);
                    binding.layoutJobsInProgressCards.setVisibility(View.GONE);
                } else {
                    binding.tvNoJobsInProgress.setVisibility(View.GONE);
                    binding.layoutJobsInProgressCards.setVisibility(View.VISIBLE);

                    // Work 1
                    SolicitudTrabajoDto w1 = jobsInProgressList.get(0);
                    binding.tvWorkTitle1.setText(w1.tituloTrabajo != null ? w1.tituloTrabajo : "Trabajo en Curso");
                    int p1 = w1.porcentajeAvance != null ? w1.porcentajeAvance : 0;
                    binding.tvWorkProgress1.setText("al " + p1 + "% completado");
                    binding.pbWork1.setProgress(p1);
                    binding.cardWorkInProgress1.setVisibility(View.VISIBLE);

                    // Work 2
                    if (jobsInProgressList.size() > 1) {
                        SolicitudTrabajoDto w2 = jobsInProgressList.get(1);
                        binding.tvWorkTitle2.setText(w2.tituloTrabajo != null ? w2.tituloTrabajo : "Trabajo en Curso");
                        int p2 = w2.porcentajeAvance != null ? w2.porcentajeAvance : 0;
                        binding.tvWorkProgress2.setText("al " + p2 + "% completado");
                        binding.pbWork2.setProgress(p2);
                        binding.cardWorkInProgress2.setVisibility(View.VISIBLE);
                    } else {
                        binding.cardWorkInProgress2.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void onError(String message) {}
        });
    }

    private void showUpdateProgressDialog(SolicitudTrabajoDto dto) {
        if (dto == null || dto.id == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Actualizar Avance de Trabajo");

        final EditText etProgress = new EditText(getContext());
        etProgress.setHint("Porcentaje de avance (0 a 100)");
        etProgress.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etProgress.setText(String.valueOf(dto.porcentajeAvance != null ? dto.porcentajeAvance : 0));
        etProgress.setPadding(32, 32, 32, 32);

        builder.setView(etProgress);

        builder.setPositiveButton("GUARDAR AVANCE", (dialog, which) -> {
            String valStr = etProgress.getText().toString().trim();
            int val = 0;
            try {
                val = Integer.parseInt(valStr);
            } catch (Exception ignored) {}
            if (val > 100) val = 100;
            if (val < 0) val = 0;

            final int newVal = val;
            JobLinkRepository.getInstance().updateSolicitudAvanceInSupabase(dto.id, newVal, new JobLinkRepository.RepositoryCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    Toast.makeText(getContext(), "📈 Avance actualizado al " + newVal + "%", Toast.LENGTH_SHORT).show();
                    loadDashboardDataFromSupabase();
                }

                @Override
                public void onError(String message) {}
            });
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

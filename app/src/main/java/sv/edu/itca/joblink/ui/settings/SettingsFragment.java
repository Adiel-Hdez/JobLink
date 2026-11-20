package sv.edu.itca.joblink.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import sv.edu.itca.joblink.databinding.FragmentSettingsBinding;
import sv.edu.itca.joblink.model.User;
import sv.edu.itca.joblink.repository.JobLinkRepository;
import sv.edu.itca.joblink.ui.auth.AuthActivity;
import sv.edu.itca.joblink.ui.auth.RegisterClientActivity;
import sv.edu.itca.joblink.ui.auth.RegisterWorkerActivity;
import sv.edu.itca.joblink.ui.profile.PremiumSubscriptionActivity;
import sv.edu.itca.joblink.ui.profile.RateWorkerActivity;
import sv.edu.itca.joblink.util.ImageStorageManager;
import sv.edu.itca.joblink.util.LocaleHelper;
import sv.edu.itca.joblink.util.SessionManager;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        updateUserUI();

        binding.btnChangeLanguage.setOnClickListener(v -> showLanguageSelectionDialog());

        binding.btnOpenPremium.setOnClickListener(v -> {
            User user = JobLinkRepository.getInstance().getCurrentUser();
            if (user != null) {
                if (user.isPremium()) {
                    user.setPremium(false);
                    new SessionManager(getContext()).saveUserSession(user);
                    JobLinkRepository.getInstance().updateWorkerPremiumStatusInSupabase(user.getId(), false, null);
                    Toast.makeText(getContext(), "Suscripción Premium cancelada", Toast.LENGTH_SHORT).show();
                    updateUserUI();
                } else {
                    Intent intent = new Intent(getContext(), PremiumSubscriptionActivity.class);
                    startActivity(intent);
                }
            }
        });

        binding.btnRateWorker.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), RateWorkerActivity.class);
            startActivity(intent);
        });

        binding.btnSwitchRole.setOnClickListener(v -> {
            User currentUser = JobLinkRepository.getInstance().getCurrentUser();
            if (currentUser != null) {
                if (currentUser.getRole() == User.Role.WORKER) {
                    currentUser.setRole(User.Role.CLIENT);
                    Toast.makeText(getContext(), "Cambiado a Modo Cliente", Toast.LENGTH_SHORT).show();
                } else {
                    currentUser.setRole(User.Role.WORKER);
                    Toast.makeText(getContext(), "Cambiado a Modo Trabajador", Toast.LENGTH_SHORT).show();
                }
                new SessionManager(getContext()).saveUserSession(currentUser);
                updateUserUI();
            }
        });

        binding.btnEditProfile.setOnClickListener(v -> {
            User user = JobLinkRepository.getInstance().getCurrentUser();
            if (user != null && user.getRole() == User.Role.CLIENT) {
                Intent intent = new Intent(getContext(), RegisterClientActivity.class);
                intent.putExtra("is_editing", true);
                startActivity(intent);
            } else {
                Intent intent = new Intent(getContext(), RegisterWorkerActivity.class);
                intent.putExtra("is_editing", true);
                startActivity(intent);
            }
        });

        binding.btnLogout.setOnClickListener(v -> {
            new SessionManager(getContext()).clearSession();
            Intent intent = new Intent(getContext(), AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUserUI();
    }

    private void showLanguageSelectionDialog() {
        if (getContext() == null) return;

        CharSequence[] languages = new CharSequence[]{
                "🇪🇸 Español",
                "🇺🇸 English",
                "🇵🇹 Português",
                "🇫🇷 Français",
                "🇪🇸 Català"
        };

        String currentCode = LocaleHelper.getLanguage(getContext());
        int defaultIndex = 0;
        if ("en".equalsIgnoreCase(currentCode)) defaultIndex = 1;
        else if ("pt".equalsIgnoreCase(currentCode)) defaultIndex = 2;
        else if ("fr".equalsIgnoreCase(currentCode)) defaultIndex = 3;
        else if ("ca".equalsIgnoreCase(currentCode)) defaultIndex = 4;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Seleccionar Idioma / Select Language");
        builder.setSingleChoiceItems(languages, defaultIndex, (dialog, which) -> {
            String selectedCode = "es";
            switch (which) {
                case 1: selectedCode = "en"; break;
                case 2: selectedCode = "pt"; break;
                case 3: selectedCode = "fr"; break;
                case 4: selectedCode = "ca"; break;
                case 0:
                default:
                    selectedCode = "es";
                    break;
            }

            LocaleHelper.setLocale(getContext(), selectedCode);
            Toast.makeText(getContext(), "🌐 Idioma actualizado a " + LocaleHelper.getLanguageDisplayName(selectedCode), Toast.LENGTH_SHORT).show();
            dialog.dismiss();

            if (getActivity() != null) {
                getActivity().recreate();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void updateUserUI() {
        User user = JobLinkRepository.getInstance().getCurrentUser();
        if (binding != null) {
            String currentLang = LocaleHelper.getLanguage(getContext());
            binding.tvCurrentLanguage.setText("Idioma actual: " + LocaleHelper.getLanguageDisplayName(currentLang));

            if (user != null) {
                binding.tvSettingsName.setText(user.getName());
                binding.tvSettingsEmail.setText(user.getEmail());

                if (user.getProfilePhotoUrl() != null && !user.getProfilePhotoUrl().isEmpty()) {
                    ImageStorageManager.loadImageIntoView(binding.ivSettingsAvatar, user.getProfilePhotoUrl());
                } else {
                    ImageStorageManager.resetAvatarToDefault(binding.ivSettingsAvatar);
                }

                if (user.isPremium()) {
                    binding.tvSettingsName.setText(user.getName() + " ⭐ VIP");
                }

                if (user.getRole() == User.Role.WORKER) {
                    binding.tvSettingsRole.setText("Rol Activo: TRABAJADOR (" + user.getProfession() + ")");
                    binding.btnSwitchRole.setText("CAMBIAR A MODO CLIENTE");
                    binding.btnEditProfile.setText("EDITAR PERFIL DE TRABAJADOR");

                    binding.cardPremium.setVisibility(View.VISIBLE);
                    if (user.isPremium()) {
                        binding.btnOpenPremium.setText("CANCELAR SUSCRIPCIÓN PREMIUM");
                    } else {
                        binding.btnOpenPremium.setText("VER BENEFICIOS Y SUSCRIBIRME");
                    }
                } else {
                    binding.tvSettingsRole.setText("Rol Activo: CLIENTE");
                    binding.btnSwitchRole.setText("CAMBIAR A MODO TRABAJADOR");
                    binding.btnEditProfile.setText("EDITAR PERFIL DE CLIENTE");

                    binding.cardPremium.setVisibility(View.GONE);
                }
                binding.btnEditProfile.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

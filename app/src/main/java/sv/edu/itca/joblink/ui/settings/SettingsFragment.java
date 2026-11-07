package sv.edu.itca.joblink.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import sv.edu.itca.joblink.databinding.FragmentSettingsBinding;
import sv.edu.itca.joblink.model.User;
import sv.edu.itca.joblink.repository.JobLinkRepository;
import sv.edu.itca.joblink.ui.auth.AuthActivity;
import sv.edu.itca.joblink.ui.auth.RegisterWorkerActivity;

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
                updateUserUI();
            }
        });

        binding.btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), RegisterWorkerActivity.class);
            startActivity(intent);
        });

        binding.btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void updateUserUI() {
        User user = JobLinkRepository.getInstance().getCurrentUser();
        if (user != null) {
            binding.tvSettingsName.setText(user.getName());
            binding.tvSettingsEmail.setText(user.getEmail());

            if (user.getRole() == User.Role.WORKER) {
                binding.tvSettingsRole.setText("Rol Activo: TRABAJADOR (" + user.getProfession() + ")");
                binding.btnSwitchRole.setText("CAMBIAR A MODO CLIENTE");
                binding.btnEditProfile.setVisibility(View.VISIBLE);
            } else {
                binding.tvSettingsRole.setText("Rol Activo: CLIENTE");
                binding.btnSwitchRole.setText("CAMBIAR A MODO TRABAJADOR");
                binding.btnEditProfile.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
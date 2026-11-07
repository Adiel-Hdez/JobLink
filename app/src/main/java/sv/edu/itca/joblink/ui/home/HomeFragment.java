package sv.edu.itca.joblink.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import sv.edu.itca.joblink.MainActivity;
import sv.edu.itca.joblink.databinding.FragmentHomeBinding;
import sv.edu.itca.joblink.model.User;
import sv.edu.itca.joblink.repository.JobLinkRepository;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        User currentUser = JobLinkRepository.getInstance().getCurrentUser();
        if (currentUser != null) {
            binding.tvHomeGreeting.setText("¡BIENVENIDO " + currentUser.getName().toUpperCase() + "!");
            binding.tvDashboardName.setText(currentUser.getName());
            binding.tvDashboardTrade.setText(currentUser.getProfession());
            binding.tvDashboardRating.setText(String.format(java.util.Locale.getDefault(), "%.1f", currentUser.getRating()));
            binding.tvDashboardReviews.setText("(" + currentUser.getReviewsCount() + " trabajos)");
        }

        binding.btnQuickAddGallery.setOnClickListener(v -> 
            Toast.makeText(getContext(), "Función para añadir foto al portafolio", Toast.LENGTH_SHORT).show()
        );

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
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
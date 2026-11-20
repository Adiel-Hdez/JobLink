package sv.edu.itca.joblink;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sv.edu.itca.joblink.data.remote.dto.SolicitudTrabajoDto;
import sv.edu.itca.joblink.databinding.ActivityMainBinding;
import sv.edu.itca.joblink.model.User;
import sv.edu.itca.joblink.repository.JobLinkRepository;
import sv.edu.itca.joblink.ui.chat.ChatListFragment;
import sv.edu.itca.joblink.ui.home.HomeFragment;
import sv.edu.itca.joblink.ui.search.SearchFragment;
import sv.edu.itca.joblink.ui.settings.SettingsFragment;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_POST_NOTIFICATIONS = 101;
    private ActivityMainBinding binding;

    private final Handler notificationPollHandler = new Handler(Looper.getMainLooper());
    private final Set<String> notifiedMessageIds = new HashSet<>();
    private final Set<Integer> notifiedSolicitudIds = new HashSet<>();

    private final Runnable notificationPollRunnable = new Runnable() {
        @Override
        public void run() {
            checkForNewNotifications();
            notificationPollHandler.postDelayed(this, 7000); // Poll every 7s
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        if (JobLinkRepository.getInstance().getCurrentUser() == null) {
            User savedUser = new sv.edu.itca.joblink.util.SessionManager(this).getUserSession();
            if (savedUser != null) {
                JobLinkRepository.getInstance().setCurrentUser(savedUser);
            }
        }

        checkNotificationPermissions();

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                loadFragment(new HomeFragment());
                return true;
            } else if (itemId == R.id.nav_search) {
                loadFragment(new SearchFragment());
                return true;
            } else if (itemId == R.id.nav_chat) {
                loadFragment(new ChatListFragment());
                return true;
            } else if (itemId == R.id.nav_settings) {
                loadFragment(new SettingsFragment());
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        notificationPollHandler.post(notificationPollRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        notificationPollHandler.removeCallbacks(notificationPollRunnable);
    }

    private void checkNotificationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_POST_NOTIFICATIONS);
            }
        }
    }

    private void checkForNewNotifications() {
        User currentUser = JobLinkRepository.getInstance().getCurrentUser();
        if (currentUser == null) return;

        // Check for new pending solicitudes
        JobLinkRepository.getInstance().fetchSolicitudesTrabajoFromSupabase(new JobLinkRepository.RepositoryCallback<List<SolicitudTrabajoDto>>() {
            @Override
            public void onSuccess(List<SolicitudTrabajoDto> result) {
                if (result != null) {
                    for (SolicitudTrabajoDto dto : result) {
                        if (dto.id != null && "Pendiente".equalsIgnoreCase(dto.estado) && !notifiedSolicitudIds.contains(dto.id)) {
                            notifiedSolicitudIds.add(dto.id);
                            Intent targetIntent = new Intent(MainActivity.this, MainActivity.class);
                            sv.edu.itca.joblink.util.NotificationHelper.showNotification(
                                    MainActivity.this,
                                    "🔔 Nueva Solicitud de Trabajo",
                                    "Tienes una nueva solicitud: " + (dto.tituloTrabajo != null ? dto.tituloTrabajo : "Trabajo"),
                                    targetIntent,
                                    dto.id
                            );
                        }
                    }
                }
            }

            @Override
            public void onError(String message) {}
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    public void navigateToSearch() {
        binding.bottomNavigationView.setSelectedItemId(R.id.nav_search);
    }

    public void navigateToSettings() {
        binding.bottomNavigationView.setSelectedItemId(R.id.nav_settings);
    }

    public void navigateToChat() {
        binding.bottomNavigationView.setSelectedItemId(R.id.nav_chat);
    }
}

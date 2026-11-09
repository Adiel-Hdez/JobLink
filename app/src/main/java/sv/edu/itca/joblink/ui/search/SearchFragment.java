package sv.edu.itca.joblink.ui.search;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sv.edu.itca.joblink.databinding.FragmentSearchBinding;
import sv.edu.itca.joblink.model.User;
import sv.edu.itca.joblink.repository.JobLinkRepository;
import sv.edu.itca.joblink.ui.adapter.WorkerAdapter;
import sv.edu.itca.joblink.ui.chat.ChatDetailActivity;
import sv.edu.itca.joblink.ui.profile.WorkerProfileActivity;

public class SearchFragment extends Fragment implements WorkerAdapter.OnWorkerClickListener {

    private FragmentSearchBinding binding;
    private WorkerAdapter adapter;
    private List<User> allWorkers;
    private String selectedCategoryGroup = "Todos";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        allWorkers = JobLinkRepository.getInstance().getWorkers();
        adapter = new WorkerAdapter(new ArrayList<>(), this);

        binding.rvWorkersList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvWorkersList.setAdapter(adapter);

        setupSearchFilter();
        setupCategoryChips();

        filterWorkers("");
    }

    private void setupSearchFilter() {
        binding.etSearchQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterWorkers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupCategoryChips() {
        binding.chipGroupCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(binding.chipConstruccion.getId())) {
                selectedCategoryGroup = "Construcción";
            } else if (checkedIds.contains(binding.chipMantenimiento.getId())) {
                selectedCategoryGroup = "Mantenimiento";
            } else if (checkedIds.contains(binding.chipTextiles.getId())) {
                selectedCategoryGroup = "Textiles";
            } else if (checkedIds.contains(binding.chipExteriores.getId())) {
                selectedCategoryGroup = "Exteriores";
            } else {
                selectedCategoryGroup = "Todos";
            }

            String currentQuery = binding.etSearchQuery.getText() != null ? binding.etSearchQuery.getText().toString() : "";
            filterWorkers(currentQuery);
        });
    }

    private void filterWorkers(String query) {
        List<User> filtered = new ArrayList<>();
        String lowerQuery = query.toLowerCase().trim();

        for (User worker : allWorkers) {
            boolean matchesQuery = lowerQuery.isEmpty() ||
                    worker.getName().toLowerCase().contains(lowerQuery) ||
                    worker.getProfession().toLowerCase().contains(lowerQuery) ||
                    worker.getLocation().toLowerCase().contains(lowerQuery);

            boolean matchesCategory = selectedCategoryGroup.equals("Todos") ||
                    worker.getCategory().toLowerCase().contains(selectedCategoryGroup.toLowerCase()) ||
                    worker.getProfession().toLowerCase().contains(selectedCategoryGroup.toLowerCase());

            if (matchesQuery && matchesCategory) {
                filtered.add(worker);
            }
        }

        // Sort: PREMIUM workers FIRST, then highest rating
        Collections.sort(filtered, (w1, w2) -> {
            if (w1.isPremium() != w2.isPremium()) {
                return w1.isPremium() ? -1 : 1;
            }
            return Double.compare(w2.getRating(), w1.getRating());
        });

        adapter.updateList(filtered);
    }

    @Override
    public void onWorkerClick(User worker) {
        Intent intent = new Intent(getContext(), WorkerProfileActivity.class);
        intent.putExtra("worker_id", worker.getId());
        startActivity(intent);
    }

    @Override
    public void onContactClick(User worker) {
        Intent intent = new Intent(getContext(), ChatDetailActivity.class);
        intent.putExtra("contact_name", worker.getName());
        intent.putExtra("contact_profession", worker.getProfession());
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
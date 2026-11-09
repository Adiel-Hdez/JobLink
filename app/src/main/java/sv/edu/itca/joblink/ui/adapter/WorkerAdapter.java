package sv.edu.itca.joblink.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import sv.edu.itca.joblink.databinding.ItemWorkerCardBinding;
import sv.edu.itca.joblink.model.User;

public class WorkerAdapter extends RecyclerView.Adapter<WorkerAdapter.WorkerViewHolder> {

    public interface OnWorkerClickListener {
        void onWorkerClick(User worker);
        void onContactClick(User worker);
    }

    private List<User> workerList;
    private final OnWorkerClickListener listener;

    public WorkerAdapter(List<User> workerList, OnWorkerClickListener listener) {
        this.workerList = workerList;
        this.listener = listener;
    }

    public void updateList(List<User> newList) {
        this.workerList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WorkerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWorkerCardBinding binding = ItemWorkerCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new WorkerViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkerViewHolder holder, int position) {
        holder.bind(workerList.get(position));
    }

    @Override
    public int getItemCount() {
        return workerList.size();
    }

    class WorkerViewHolder extends RecyclerView.ViewHolder {
        private final ItemWorkerCardBinding binding;

        public WorkerViewHolder(ItemWorkerCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(User worker) {
            binding.tvWorkerCardName.setText(worker.getName());
            binding.tvWorkerCardTrade.setText(worker.getProfession());
            binding.tvWorkerCardLocation.setText(worker.getLocation());
            binding.tvWorkerCardBio.setText(worker.getBio());
            binding.tvWorkerCardRating.setText(String.format(java.util.Locale.getDefault(), "%.1f", worker.getRating()));

            if (worker.isPremium()) {
                binding.tvWorkerCardPremiumBadge.setVisibility(View.VISIBLE);
            } else {
                binding.tvWorkerCardPremiumBadge.setVisibility(View.GONE);
            }

            binding.btnWorkerCardProfile.setOnClickListener(v -> {
                if (listener != null) listener.onWorkerClick(worker);
            });

            binding.btnWorkerCardChat.setOnClickListener(v -> {
                if (listener != null) listener.onContactClick(worker);
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onWorkerClick(worker);
            });
        }
    }
}
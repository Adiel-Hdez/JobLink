package sv.edu.itca.joblink.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import sv.edu.itca.joblink.databinding.ItemChatConversationBinding;
import sv.edu.itca.joblink.model.ChatConversation;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {

    public interface OnConversationClickListener {
        void onConversationClick(ChatConversation conversation);
    }

    private final List<ChatConversation> conversationList;
    private final OnConversationClickListener listener;

    public ConversationAdapter(List<ChatConversation> conversationList, OnConversationClickListener listener) {
        this.conversationList = conversationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemChatConversationBinding binding = ItemChatConversationBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ConversationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        holder.bind(conversationList.get(position));
    }

    @Override
    public int getItemCount() {
        return conversationList.size();
    }

    class ConversationViewHolder extends RecyclerView.ViewHolder {
        private final ItemChatConversationBinding binding;

        public ConversationViewHolder(ItemChatConversationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ChatConversation conversation) {
            binding.tvConvName.setText(conversation.getContactName() + " (" + conversation.getContactProfession() + ")");
            binding.tvConvLastMessage.setText(conversation.getLastMessage());
            binding.tvConvTime.setText(conversation.getTime());

            if (conversation.getUnreadCount() > 0) {
                binding.tvConvBadge.setVisibility(View.VISIBLE);
                binding.tvConvBadge.setText("cite: " + conversation.getUnreadCount());
            } else {
                binding.tvConvBadge.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onConversationClick(conversation);
            });
        }
    }
}
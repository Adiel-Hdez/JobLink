package sv.edu.itca.joblink.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import sv.edu.itca.joblink.databinding.ItemChatBubbleReceivedBinding;
import sv.edu.itca.joblink.databinding.ItemChatBubbleSentBinding;
import sv.edu.itca.joblink.model.ChatMessage;

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;

    private final List<ChatMessage> messageList;

    public ChatMessageAdapter(List<ChatMessage> messageList) {
        this.messageList = messageList;
    }

    @Override
    public int getItemViewType(int position) {
        if (messageList.get(position).isMine()) {
            return TYPE_SENT;
        } else {
            return TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SENT) {
            ItemChatBubbleSentBinding binding = ItemChatBubbleSentBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new SentViewHolder(binding);
        } else {
            ItemChatBubbleReceivedBinding binding = ItemChatBubbleReceivedBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new ReceivedViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage msg = messageList.get(position);
        if (holder instanceof SentViewHolder) {
            ((SentViewHolder) holder).bind(msg);
        } else if (holder instanceof ReceivedViewHolder) {
            ((ReceivedViewHolder) holder).bind(msg);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class SentViewHolder extends RecyclerView.ViewHolder {
        private final ItemChatBubbleSentBinding binding;

        public SentViewHolder(ItemChatBubbleSentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ChatMessage msg) {
            binding.tvMessageSentText.setText(msg.getText());
            binding.tvMessageSentTime.setText(msg.getTime());
        }
    }

    static class ReceivedViewHolder extends RecyclerView.ViewHolder {
        private final ItemChatBubbleReceivedBinding binding;

        public ReceivedViewHolder(ItemChatBubbleReceivedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ChatMessage msg) {
            binding.tvMessageReceivedText.setText(msg.getText());
            binding.tvMessageReceivedTime.setText(msg.getTime());
        }
    }
}
package sv.edu.itca.joblink.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;

import sv.edu.itca.joblink.databinding.FragmentChatListBinding;
import sv.edu.itca.joblink.model.ChatConversation;
import sv.edu.itca.joblink.repository.JobLinkRepository;
import sv.edu.itca.joblink.ui.adapter.ConversationAdapter;

public class ChatListFragment extends Fragment implements ConversationAdapter.OnConversationClickListener {

    private FragmentChatListBinding binding;
    private List<ChatConversation> conversationList;
    private ConversationAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChatListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        conversationList = JobLinkRepository.getInstance().getConversations();
        adapter = new ConversationAdapter(conversationList, this);

        binding.rvChatConversations.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvChatConversations.setAdapter(adapter);
    }

    @Override
    public void onConversationClick(ChatConversation conversation) {
        Intent intent = new Intent(getContext(), ChatDetailActivity.class);
        intent.putExtra("conversation_id", conversation.getId());
        intent.putExtra("contact_name", conversation.getContactName());
        intent.putExtra("contact_profession", conversation.getContactProfession());
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
package com.example.fashionstoreapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fashionstoreapp.R;
import com.example.fashionstoreapp.models.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {
    private List<ChatMessage> messages;
    private SimpleDateFormat timeFormat;

    public ChatAdapter() {
        this.messages = new ArrayList<>();
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    public void updateLastMessage(ChatMessage message) {
        if (!messages.isEmpty()) {
            int lastIndex = messages.size() - 1;
            messages.set(lastIndex, message);
            notifyItemChanged(lastIndex);
        }
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages.clear();
        this.messages.addAll(messages);
        notifyDataSetChanged();
    }

    public void clearMessages() {
        messages.clear();
        notifyDataSetChanged();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {
        private TextView userMessageText;
        private TextView aiMessageText;
        private TextView userTimeText;
        private TextView aiTimeText;
        private View userMessageContainer;
        private View aiMessageContainer;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            userMessageContainer = itemView.findViewById(R.id.userMessageContainer);
            aiMessageContainer = itemView.findViewById(R.id.aiMessageContainer);
            userMessageText = itemView.findViewById(R.id.userMessageText);
            aiMessageText = itemView.findViewById(R.id.aiMessageText);
            userTimeText = itemView.findViewById(R.id.userTimeText);
            aiTimeText = itemView.findViewById(R.id.aiTimeText);
        }

        public void bind(ChatMessage message) {
            String timeString = timeFormat.format(new Date(message.getTimestamp()));

            if (message.isUser()) {
                userMessageContainer.setVisibility(View.VISIBLE);
                aiMessageContainer.setVisibility(View.GONE);
                userMessageText.setText(message.getContent());
                userTimeText.setText(timeString);
            } else {
                userMessageContainer.setVisibility(View.GONE);
                aiMessageContainer.setVisibility(View.VISIBLE);
                aiMessageText.setText(message.getContent());
                aiTimeText.setText(timeString);
            }
        }
    }
}
package com.example.fashionstoreapp;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fashionstoreapp.adapters.ChatAdapter;
import com.example.fashionstoreapp.models.ChatMessage;
import com.example.fashionstoreapp.services.ChatHistoryManager;
import com.example.fashionstoreapp.services.GeminiAIService;

import java.util.ArrayList;
import java.util.List;

public class ChatbotActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private EditText messageInput;
    private ImageButton sendButton;
    private ProgressBar loadingIndicator;

    private GeminiAIService aiService;
    private ChatHistoryManager historyManager;
    private List<String> conversationHistory;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        // Lấy userId (có thể từ Firebase Auth hoặc SharedPreferences)
        userId = getUserId();

        initViews();
        setupRecyclerView();
        setupServices();
        loadChatHistory();
        setupListeners();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        loadingIndicator = findViewById(R.id.loadingIndicator);
    }

    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(chatAdapter);
    }

    private void setupServices() {
        aiService = new GeminiAIService(this);
        historyManager = new ChatHistoryManager(userId);
        conversationHistory = new ArrayList<>();
    }

    private void loadChatHistory() {
        showLoading(true);
        historyManager.loadChatHistory(new ChatHistoryManager.LoadHistoryCallback() {
            @Override
            public void onLoaded(List<ChatMessage> messages) {
                runOnUiThread(() -> {
                    chatAdapter.setMessages(messages);

                    // Build conversation history cho AI
                    conversationHistory.clear();
                    for (ChatMessage msg : messages) {
                        String role = msg.isUser() ? "User: " : "Assistant: ";
                        conversationHistory.add(role + msg.getContent());
                    }

                    scrollToBottom();
                    showLoading(false);

                    // Nếu không có history, hiển thị welcome message
                    if (messages.isEmpty()) {
                        sendWelcomeMessage();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(ChatbotActivity.this,
                            "Lỗi tải lịch sử: " + error, Toast.LENGTH_SHORT).show();
                    showLoading(false);
                });
            }
        });
    }

    private void setupListeners() {
        sendButton.setOnClickListener(v -> sendMessage());

        messageInput.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();

        if (messageText.isEmpty()) {
            return;
        }

        // Disable input
        setInputEnabled(false);
        messageInput.setText("");

        // Tạo user message
        ChatMessage userMessage = new ChatMessage(messageText, true);
        chatAdapter.addMessage(userMessage);
        conversationHistory.add("User: " + messageText);
        scrollToBottom();

        // Lưu user message vào Firestore
        historyManager.saveMessage(userMessage, new ChatHistoryManager.SaveMessageCallback() {
            @Override
            public void onSuccess(ChatMessage message) {
                // Message đã được lưu
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(ChatbotActivity.this,
                        "Lỗi lưu tin nhắn: " + error, Toast.LENGTH_SHORT).show());
            }
        });

        // Hiển thị loading
        showLoading(true);

        // Gọi AI
        aiService.sendMessage(messageText, conversationHistory, new GeminiAIService.AIResponseCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    showLoading(false);
                    setInputEnabled(true);

                    // Tạo AI message
                    ChatMessage aiMessage = new ChatMessage(response, false);
                    chatAdapter.addMessage(aiMessage);
                    conversationHistory.add("Assistant: " + response);
                    scrollToBottom();

                    // Lưu AI message vào Firestore
                    historyManager.saveMessage(aiMessage, new ChatHistoryManager.SaveMessageCallback() {
                        @Override
                        public void onSuccess(ChatMessage message) {
                            // Message đã được lưu
                        }

                        @Override
                        public void onError(String error) {
                            // Log error nhưng không hiển thị cho user
                        }
                    });
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    setInputEnabled(true);

                    Toast.makeText(ChatbotActivity.this, error, Toast.LENGTH_LONG).show();

                    // Hiển thị error message
                    ChatMessage errorMessage = new ChatMessage(
                            "Xin lỗi, đã có lỗi xảy ra. Vui lòng thử lại.",
                            false);
                    chatAdapter.addMessage(errorMessage);
                    scrollToBottom();
                });
            }
        });
    }

    private void sendWelcomeMessage() {
        ChatMessage welcomeMessage = new ChatMessage(
                "Xin chào! Tôi là trợ lý ảo của FashionStore. " +
                        "Tôi có thể giúp bạn:\n" +
                        "• Tư vấn chọn quần áo phù hợp\n" +
                        "• Gợi ý cách phối đồ\n" +
                        "• Tư vấn size và chất liệu\n" +
                        "• Trả lời các câu hỏi về sản phẩm\n\n" +
                        "Bạn cần tôi giúp gì ạ?",
                false);
        chatAdapter.addMessage(welcomeMessage);

        // Lưu welcome message
        historyManager.saveMessage(welcomeMessage, new ChatHistoryManager.SaveMessageCallback() {
            @Override
            public void onSuccess(ChatMessage message) {
                // Saved
            }

            @Override
            public void onError(String error) {
                // Ignore
            }
        });
    }

    private void showLoading(boolean show) {
        loadingIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void setInputEnabled(boolean enabled) {
        messageInput.setEnabled(enabled);
        sendButton.setEnabled(enabled);
    }

    private void scrollToBottom() {
        if (chatAdapter.getItemCount() > 0) {
            recyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
        }
    }

    private String getUserId() {
        // TODO: Implement proper user ID retrieval
        // Ví dụ: từ Firebase Auth hoặc SharedPreferences
        return "user_" + System.currentTimeMillis();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (aiService != null) {
            aiService.clearCache();
        }
    }
}
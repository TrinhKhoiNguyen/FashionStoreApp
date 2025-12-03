package com.example.fashionstoreapp.services;

import android.util.Log;

import com.example.fashionstoreapp.models.ChatMessage;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatHistoryManager {
    private static final String TAG = "ChatHistoryManager";
    private static final String COLLECTION_CHATS = "chats";
    private static final String COLLECTION_MESSAGES = "messages";

    private FirebaseFirestore db;
    private String userId;
    private String currentChatId;

    public ChatHistoryManager(String userId) {
        this.db = FirebaseFirestore.getInstance();
        this.userId = userId;
    }

    // Tạo chat session mới
    public void createChatSession(ChatSessionCallback callback) {
        Map<String, Object> chatData = new HashMap<>();
        chatData.put("userId", userId);
        chatData.put("createdAt", System.currentTimeMillis());
        chatData.put("updatedAt", System.currentTimeMillis());

        db.collection(COLLECTION_CHATS)
                .add(chatData)
                .addOnSuccessListener(documentReference -> {
                    currentChatId = documentReference.getId();
                    Log.d(TAG, "Chat session created: " + currentChatId);
                    callback.onSuccess(currentChatId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating chat session", e);
                    callback.onError(e.getMessage());
                });
    }

    // Lưu message vào Firestore
    public void saveMessage(ChatMessage message, SaveMessageCallback callback) {
        if (currentChatId == null) {
            createChatSession(new ChatSessionCallback() {
                @Override
                public void onSuccess(String chatId) {
                    saveMessageToFirestore(chatId, message, callback);
                }

                @Override
                public void onError(String error) {
                    callback.onError(error);
                }
            });
        } else {
            saveMessageToFirestore(currentChatId, message, callback);
        }
    }

    private void saveMessageToFirestore(String chatId, ChatMessage message, SaveMessageCallback callback) {
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("content", message.getContent());
        messageData.put("isUser", message.isUser());
        messageData.put("timestamp", message.getTimestamp());
        messageData.put("userId", userId);

        db.collection(COLLECTION_CHATS)
                .document(chatId)
                .collection(COLLECTION_MESSAGES)
                .add(messageData)
                .addOnSuccessListener(documentReference -> {
                    message.setMessageId(documentReference.getId());

                    // Update chat's updatedAt
                    Map<String, Object> updateData = new HashMap<>();
                    updateData.put("updatedAt", System.currentTimeMillis());
                    db.collection(COLLECTION_CHATS)
                            .document(chatId)
                            .update(updateData);

                    Log.d(TAG, "Message saved: " + documentReference.getId());
                    callback.onSuccess(message);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving message", e);
                    callback.onError(e.getMessage());
                });
    }

    // Load chat history
    public void loadChatHistory(LoadHistoryCallback callback) {
        if (currentChatId == null) {
            callback.onLoaded(new ArrayList<>());
            return;
        }

        db.collection(COLLECTION_CHATS)
                .document(currentChatId)
                .collection(COLLECTION_MESSAGES)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ChatMessage> messages = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        ChatMessage msg = new ChatMessage(
                                doc.getId(),
                                doc.getString("content"),
                                doc.getBoolean("isUser") != null ? doc.getBoolean("isUser") : false,
                                doc.getLong("timestamp") != null ? doc.getLong("timestamp") : 0,
                                doc.getString("userId")
                        );
                        messages.add(msg);
                    }
                    Log.d(TAG, "Loaded " + messages.size() + " messages");
                    callback.onLoaded(messages);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading history", e);
                    callback.onError(e.getMessage());
                });
    }

    // Load recent chats cho user
    public void loadRecentChats(int limit, LoadChatsCallback callback) {
        db.collection(COLLECTION_CHATS)
                .whereEqualTo("userId", userId)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> chatIds = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        chatIds.add(doc.getId());
                    }
                    callback.onLoaded(chatIds);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading recent chats", e);
                    callback.onError(e.getMessage());
                });
    }

    // Delete chat
    public void deleteChat(String chatId, DeleteCallback callback) {
        db.collection(COLLECTION_CHATS)
                .document(chatId)
                .collection(COLLECTION_MESSAGES)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Delete all messages first
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        doc.getReference().delete();
                    }

                    // Then delete chat
                    db.collection(COLLECTION_CHATS)
                            .document(chatId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Chat deleted: " + chatId);
                                callback.onSuccess();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error deleting chat", e);
                                callback.onError(e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting messages", e);
                    callback.onError(e.getMessage());
                });
    }

    public void setCurrentChatId(String chatId) {
        this.currentChatId = chatId;
    }

    public String getCurrentChatId() {
        return currentChatId;
    }

    // Callbacks
    public interface ChatSessionCallback {
        void onSuccess(String chatId);
        void onError(String error);
    }

    public interface SaveMessageCallback {
        void onSuccess(ChatMessage message);
        void onError(String error);
    }

    public interface LoadHistoryCallback {
        void onLoaded(List<ChatMessage> messages);
        void onError(String error);
    }

    public interface LoadChatsCallback {
        void onLoaded(List<String> chatIds);
        void onError(String error);
    }

    public interface DeleteCallback {
        void onSuccess();
        void onError(String error);
    }
}
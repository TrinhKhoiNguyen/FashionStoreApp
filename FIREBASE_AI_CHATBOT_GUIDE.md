# 🤖 FIREBASE AI CHATBOT - HƯỚNG DẪN

## ✅ ĐÃ HOÀN THÀNH

### **Tích hợp Firebase Vertex AI (Gemini)**
- ✅ Sử dụng Gemini 1.5 Flash (Miễn phí)
- ✅ Chat context-aware với conversation history
- ✅ Không cần API key riêng (dùng Firebase project)
- ✅ UI chat đẹp, thân thiện mobile

---

## 📁 CẤU TRÚC CODE

### **1. Models**
- `ChatMessage.java` - Model tin nhắn chat

### **2. Services**
- `FirebaseAIService.java` - Service gọi Vertex AI

### **3. Adapters**
- `ChatAdapter.java` - RecyclerView adapter

### **4. Activities**
- `ChatbotActivity.java` - Màn hình chat chính

### **5. Layouts**
- `activity_chatbot.xml` - Layout chat
- `item_chat_user.xml` - User message bubble
- `item_chat_bot.xml` - Bot message bubble

### **6. Drawables**
- `bg_chat_user.xml` - Background đen cho user
- `bg_chat_bot.xml` - Background trắng cho bot
- `bg_bot_avatar.xml` - Avatar vàng cho bot

---

## 🚀 CÁCH SỬ DỤNG

### **Bước 1: Kích hoạt Vertex AI trong Firebase**

1. Truy cập: https://console.firebase.google.com/
2. Chọn project: **fashionstoreapp-59e3f**
3. Menu bên trái → **Build** → **Vertex AI in Firebase**
4. Click **"Get started"**
5. Chọn location: **us-central1** (recommended)
6. Click **"Continue"** → **"Enable"**

### **Bước 2: Chạy app**

1. Build và install app
2. Click FAB vàng ở góc dưới-phải MainActivity
3. Chat với bot!

---

## 💬 CÂU HỎI MẪU

### **Tìm sản phẩm:**
- "Tìm áo thun nam giá rẻ"
- "Có quần jean nữ màu xanh không?"
- "Váy đang giảm giá"

### **Tư vấn phối đồ:**
- "Gợi ý outfit đi làm"
- "Phối đồ đi tiệc"
- "Trang phục mùa hè"

### **Hướng dẫn app:**
- "Cách đặt hàng"
- "Làm sao xem giỏ hàng?"
- "Dùng voucher như thế nào?"

---

## 🎯 TÍNH NĂNG CHATBOT

### **AI thông minh:**
✅ Hiểu ngữ cảnh conversation
✅ Trả lời tự nhiên, ngắn gọn
✅ Giọng điệu thân thiện
✅ Không trả lời nội dung không liên quan

### **Hỗ trợ đa dạng:**
✅ Tìm sản phẩm theo yêu cầu
✅ Tư vấn phong cách
✅ Hướng dẫn sử dụng app
✅ Giải đáp thắc mắc

### **UI/UX:**
✅ Chat bubbles đẹp
✅ User (đen) vs Bot (trắng)
✅ Avatar bot 🤖
✅ Timestamp
✅ Auto-scroll
✅ Loading indicator

---

## 🔧 CẤU HÌNH

### **System Prompt (có thể tùy chỉnh):**

File: `FirebaseAIService.java`

```java
private String buildSystemInstruction() {
    return "Bạn là trợ lý AI của FashionStoreApp...\n" +
           "YÊU CẦU:\n" +
           "- Trả lời tiếng Việt\n" +
           "- Ngắn gọn (3-4 câu)\n" +
           "- Thân thiện\n" +
           "...";
}
```

### **Model Configuration:**

Gemini 1.5 Flash:
- ✅ Miễn phí
- ✅ Nhanh
- ✅ Phù hợp chatbot

Có thể đổi sang Gemini Pro:
```java
GenerativeModel gm = FirebaseVertexAI.getInstance()
    .generativeModel("gemini-1.5-pro");
```

---

## ⚙️ TÙY CHỈNH

### **1. Thay đổi màu sắc chat:**

**User bubble** - `bg_chat_user.xml`:
```xml
<solid android:color="#000000" /> <!-- Đen -->
```

**Bot bubble** - `bg_chat_bot.xml`:
```xml
<solid android:color="#FFFFFF" /> <!-- Trắng -->
```

### **2. Thay đổi FAB position:**

`activity_main.xml`:
```xml
<com.google.android.material.floatingactionbutton.FloatingActionButton
    android:layout_gravity="bottom|end" <!-- Góc dưới-phải -->
    android:layout_margin="16dp"
    ...
/>
```

### **3. Thêm quick replies:**

Trong `ChatbotActivity.java`:
```java
private void showQuickReplies() {
    // Add chips: "Tìm áo", "Gợi ý outfit", "Hướng dẫn"
}
```

---

## 📊 GIỚI HẠN MIỄN PHÍ

### **Vertex AI Free Tier:**
- ✅ 1,500 requests/day (Gemini Flash)
- ✅ 15 requests/minute
- ✅ Không cần thẻ tín dụng

### **Nếu vượt quota:**
- Upgrade lên Blaze plan (pay-as-you-go)
- Hoặc implement rate limiting

---

## 🐛 XỬ LÝ LỖI

### **Lỗi: "Vertex AI not enabled"**
→ Làm theo Bước 1 ở trên để enable

### **Lỗi: "Location not supported"**
→ Chọn location: **us-central1**

### **Bot không trả lời:**
→ Check Logcat: Tag = "FirebaseAIService"

### **Crash khi mở chat:**
→ Kiểm tra internet connection

---

## 🚀 NÂNG CAO

### **1. Lưu chat history:**
```java
// Save to Firestore
db.collection("chats")
  .document(userId)
  .collection("messages")
  .add(chatMessage);
```

### **2. Typing indicator:**
```java
private void showTypingIndicator() {
    ChatMessage typing = new ChatMessage("...", false);
    chatAdapter.addMessage(typing);
}
```

### **3. Voice input:**
```java
// Add SpeechRecognizer
Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
startActivityForResult(intent, SPEECH_REQUEST_CODE);
```

### **4. Product recommendations:**
```java
// Parse bot response for product IDs
// Show product cards in chat
```

---

## 📝 TESTING

### **Test cases:**

1. **Basic chat:**
   - User: "Xin chào"
   - Bot: Welcome message

2. **Product search:**
   - User: "Áo thun giá rẻ"
   - Bot: Gợi ý sản phẩm

3. **Style advice:**
   - User: "Outfit đi làm"
   - Bot: Tư vấn phối đồ

4. **App guide:**
   - User: "Cách đặt hàng"
   - Bot: Hướng dẫn step-by-step

5. **Off-topic:**
   - User: "Thời tiết hôm nay"
   - Bot: "Mình chỉ tư vấn thời trang thôi ạ"

---

## 💡 TIPS

1. **System prompt rất quan trọng:**
   - Chi tiết, rõ ràng
   - Có ví dụ cụ thể
   - Giới hạn scope

2. **Keep responses short:**
   - Mobile screen nhỏ
   - User đọc nhanh
   - Max 3-4 câu

3. **Error handling:**
   - Always have fallback
   - Friendly error messages
   - Retry mechanism

4. **Context management:**
   - Chat history tự động
   - Reset chat if needed
   - Limit conversation length

---

## 📚 TÀI LIỆU

- [Firebase Vertex AI Docs](https://firebase.google.com/docs/vertex-ai)
- [Gemini API Guide](https://ai.google.dev/gemini-api/docs)
- [Android Chat UI Best Practices](https://developer.android.com/guide/topics/ui)

---

**Chúc bạn thành công! 🎉**

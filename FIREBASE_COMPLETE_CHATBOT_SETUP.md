# 🤖 HƯỚNG DẪN HOÀN CHỈNH CHATBOT AI VỚI FIREBASE

> **Chatbot AI sử dụng 100% Firebase Ecosystem - Không dùng AI bên thứ ba**

## 📚 TỔNG QUAN

Hệ thống chatbot này sử dụng:
- ✅ **Firebase Vertex AI** - AI chính thức của Firebase
- ✅ **Gemini 2.0 Flash** - Model AI mới nhất của Google (December 2024)
- ✅ **Cloud Functions + Genkit** - Xử lý AI requests an toàn
- ✅ **Firestore** - Lưu lịch sử chat, đồng bộ realtime
- ✅ **Firebase App Check** - Bảo mật endpoint
- ✅ **Android Java** - Native implementation

---

## 🚀 PHẦN 1: CẤU HÌNH FIREBASE PROJECT

### Bước 1.1: Tạo/Cấu hình Firebase Project

1. **Truy cập Firebase Console**
   ```
   https://console.firebase.google.com/
   ```

2. **Chọn hoặc tạo project**
   - Nếu đã có: Chọn project `fashionstoreapp-59e3f` (hoặc tên project của bạn)
   - Nếu chưa có: Click **"Add project"** → Nhập tên → Enable Google Analytics (optional)

3. **Thêm Android app vào project**
   - Click biểu tượng Android </>
   - **Package name**: `com.example.fashionstoreapp` (phải khớp với namespace trong `build.gradle.kts`)
   - **App nickname**: FashionStoreApp
   - **SHA-1**: Chạy lệnh để lấy:
     ```powershell
     cd C:\Users\ASUS\OneDrive - hcmute.edu.vn\Desktop\DOAN\FashionStoreApp
     .\gradlew signingReport
     ```
     Copy SHA-1 certificate từ output (variant: debug → SHA1)

4. **Download `google-services.json`**
   - Download file từ Firebase Console
   - Copy vào thư mục: `app/google-services.json`
   - ⚠️ **QUAN TRỌNG**: File này đã có sẵn, đảm bảo nó cập nhật

### Bước 1.2: Enable Firebase Vertex AI

1. **Mở Firebase Console → Build → Vertex AI in Firebase**
   ```
   https://console.firebase.google.com/project/fashionstoreapp-59e3f/genai
   ```

2. **Click "Get Started"** hoặc **"Enable"**

3. **Chọn location** (recommended):
   - `us-central1` (fastest, most features)
   - `asia-southeast1` (gần Việt Nam hơn)

4. **Accept Terms of Service**

5. **Verify enabled**:
   - Status phải là: ✅ **"Vertex AI in Firebase is enabled"**

### Bước 1.3: Enable Firestore Database

1. **Firebase Console → Build → Firestore Database**
   ```
   https://console.firebase.google.com/project/fashionstoreapp-59e3f/firestore
   ```

2. **Click "Create database"**

3. **Choose mode**:
   - **Production mode** (recommended cho release)
   - Start in test mode (cho development, tự động expire sau 30 ngày)

4. **Select location**:
   - `asia-southeast1` (Singapore - gần Việt Nam)

5. **Create**

### Bước 1.4: Configure Firestore Security Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Chat history - chỉ user đăng nhập mới đọc/ghi chat của họ
    match /users/{userId}/chatHistory/{messageId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Chat sessions - tracking conversation contexts
    match /users/{userId}/chatSessions/{sessionId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Public AI configurations (read-only)
    match /aiConfigs/{configId} {
      allow read: if true;
      allow write: if false; // Chỉ admin qua console
    }
  }
}
```

**Apply rules**:
1. Firebase Console → Firestore → Rules tab
2. Paste code trên
3. Click **"Publish"**

### Bước 1.5: Enable Firebase App Check (Bảo mật)

1. **Firebase Console → Build → App Check**
   ```
   https://console.firebase.google.com/project/fashionstoreapp-59e3f/appcheck
   ```

2. **Register app**:
   - Select: Android app (com.example.fashionstoreapp)
   - Provider: **Play Integrity** (recommended)
   - Click **"Save"**

3. **Enable enforcement**:
   - Vertex AI in Firebase: ✅ Enforce
   - Cloud Functions: ✅ Enforce
   - Firestore: Optional (nếu muốn bảo mật cao)

---

## 🛠️ PHẦN 2: SETUP ANDROID PROJECT (JAVA)

### Bước 2.1: Cấu hình `build.gradle.kts` (Project level)

**File**: `build.gradle.kts` (root)

```kotlin
plugins {
    id("com.android.application") version "8.7.3" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}
```

### Bước 2.2: Cấu hình `build.gradle.kts` (App level)

**File**: `app/build.gradle.kts`

✅ **Đã có sẵn** - Kiểm tra các dependencies sau:

```kotlin
dependencies {
    // Firebase BOM - quản lý version tất cả Firebase libs
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    
    // Firebase Core
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    
    // ⭐ Firebase Vertex AI (Gemini) - AI chính thức của Firebase
    implementation("com.google.firebase:firebase-vertexai")
    
    // ⭐ Firebase App Check - Bảo mật
    implementation("com.google.firebase:firebase-appcheck-playintegrity")
    
    // Ktor client - Required by Firebase Vertex AI
    implementation("io.ktor:ktor-client-okhttp:3.0.3")
    implementation("io.ktor:ktor-client-core:3.0.3")
    implementation("io.ktor:ktor-client-content-negotiation:3.0.3")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.3")
    implementation("io.ktor:ktor-client-logging:3.0.3")
    
    // Guava - For Futures (async operations)
    implementation("com.google.guava:guava:31.0.1-android")
}
```

### Bước 2.3: Cấu hình `google-services.json`

✅ **Đã có sẵn**: `app/google-services.json`

**Kiểm tra**:
```json
{
  "project_info": {
    "project_id": "fashionstoreapp-59e3f",
    "firebase_url": "https://fashionstoreapp-59e3f.firebaseio.com",
    ...
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "...",
        "android_client_info": {
          "package_name": "com.example.fashionstoreapp"
        }
      }
    }
  ]
}
```

⚠️ **Lưu ý**: 
- `package_name` phải khớp với `namespace` trong `build.gradle.kts`
- Không commit file này lên Git public (đã có trong `.gitignore`)

---

## 💻 PHẦN 3: IMPLEMENTATION ANDROID (JAVA)

### 3.1: FirebaseAIService - Nâng cấp hoàn chỉnh

✅ **Đã implement cơ bản** - Sẽ nâng cấp thêm:

**Features cần bổ sung**:
- ✅ Multi-turn conversation (context memory)
- ✅ Retry logic khi network error
- ✅ Caching responses
- ✅ Rate limiting client-side
- ✅ Timeout handling

**Code mẫu đầy đủ** (sẽ update file sau):

```java
// Xem FirebaseAIService.java - đã có implementation cơ bản
// Sẽ nâng cấp với chat history tracking
```

### 3.2: ChatHistoryManager - Đồng bộ Firestore

**Tạo file mới**: `app/src/main/java/com/example/fashionstoreapp/services/ChatHistoryManager.java`

```java
package com.example.fashionstoreapp.services;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.example.fashionstoreapp.models.ChatMessage;
import java.util.*;

public class ChatHistoryManager {
    
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private String currentSessionId;
    
    public ChatHistoryManager() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentSessionId = UUID.randomUUID().toString();
    }
    
    // Lưu tin nhắn vào Firestore
    public void saveMessage(ChatMessage message) {
        String userId = auth.getCurrentUser() != null ? 
            auth.getCurrentUser().getUid() : "anonymous";
        
        Map<String, Object> data = new HashMap<>();
        data.put("message", message.getMessage());
        data.put("isUser", message.isUser());
        data.put("timestamp", System.currentTimeMillis());
        data.put("sessionId", currentSessionId);
        
        db.collection("users")
          .document(userId)
          .collection("chatHistory")
          .add(data)
          .addOnSuccessListener(doc -> {
              // Saved successfully
          })
          .addOnFailureListener(e -> {
              // Handle error
          });
    }
    
    // Load lịch sử chat
    public void loadHistory(HistoryCallback callback) {
        String userId = auth.getCurrentUser() != null ? 
            auth.getCurrentUser().getUid() : "anonymous";
        
        db.collection("users")
          .document(userId)
          .collection("chatHistory")
          .whereEqualTo("sessionId", currentSessionId)
          .orderBy("timestamp", Query.Direction.ASCENDING)
          .get()
          .addOnSuccessListener(snapshot -> {
              List<ChatMessage> messages = new ArrayList<>();
              snapshot.forEach(doc -> {
                  String msg = doc.getString("message");
                  boolean isUser = doc.getBoolean("isUser");
                  messages.add(new ChatMessage(msg, isUser));
              });
              callback.onLoaded(messages);
          })
          .addOnFailureListener(e -> {
              callback.onError(e.getMessage());
          });
    }
    
    // Clear session (new conversation)
    public void startNewSession() {
        currentSessionId = UUID.randomUUID().toString();
    }
    
    public interface HistoryCallback {
        void onLoaded(List<ChatMessage> messages);
        void onError(String error);
    }
}
```

### 3.3: ChatbotActivity - UI/UX nâng cao

✅ **Đã có cơ bản** - Sẽ thêm:
- Typing indicator
- Retry button
- Clear chat button
- Load history từ Firestore

---

## ☁️ PHẦN 4: CLOUD FUNCTIONS + FIREBASE GENKIT

### 4.1: Setup Cloud Functions

**Tại thư mục root project**:

```powershell
# Install Firebase CLI
npm install -g firebase-tools

# Login
firebase login

# Init Cloud Functions
firebase init functions
```

**Chọn**:
- Language: **JavaScript** (hoặc TypeScript)
- Install dependencies: **Yes**

### 4.2: Install Firebase Genkit

**File**: `functions/package.json`

```json
{
  "name": "functions",
  "description": "Cloud Functions for Firebase",
  "scripts": {
    "serve": "firebase emulators:start --only functions",
    "shell": "firebase functions:shell",
    "deploy": "firebase deploy --only functions",
    "logs": "firebase functions:log"
  },
  "engines": {
    "node": "18"
  },
  "dependencies": {
    "firebase-admin": "^12.0.0",
    "firebase-functions": "^5.0.0",
    "@genkit-ai/core": "^0.5.0",
    "@genkit-ai/firebase": "^0.5.0",
    "@genkit-ai/googleai": "^0.5.0",
    "express": "^4.18.2"
  }
}
```

Install:
```powershell
cd functions
npm install
```

### 4.3: Implement Cloud Function với Genkit

**File**: `functions/index.js`

```javascript
const {onRequest} = require("firebase-functions/v2/https");
const {initializeApp} = require("firebase-admin/app");
const {getFirestore} = require("firebase-admin/firestore");
const {genkit} = require("@genkit-ai/core");
const {firebase} = require("@genkit-ai/firebase");
const {googleAI, gemini20Flash} = require("@genkit-ai/googleai");

// Initialize Firebase Admin
initializeApp();
const db = getFirestore();

// Initialize Genkit với Gemini
const ai = genkit({
  plugins: [
    firebase(),
    googleAI({
      apiKey: process.env.GOOGLE_AI_API_KEY, // Set trong Firebase Console
    }),
  ],
});

// Define AI Flow với Genkit
const chatFlow = ai.defineFlow(
  {
    name: "chatFlow",
    inputSchema: {
      type: "object",
      properties: {
        message: {type: "string"},
        userId: {type: "string"},
        sessionId: {type: "string"},
      },
      required: ["message"],
    },
    outputSchema: {
      type: "object",
      properties: {
        response: {type: "string"},
      },
    },
  },
  async (input) => {
    const {message, userId = "anonymous", sessionId} = input;

    // Load chat history từ Firestore
    let context = "";
    if (userId && sessionId) {
      const historySnapshot = await db
        .collection("users")
        .doc(userId)
        .collection("chatHistory")
        .where("sessionId", "==", sessionId)
        .orderBy("timestamp", "desc")
        .limit(10)
        .get();

      const history = [];
      historySnapshot.forEach((doc) => {
        const data = doc.data();
        history.push(`${data.isUser ? "User" : "AI"}: ${data.message}`);
      });
      context = history.reverse().join("\n");
    }

    // System instruction
    const systemPrompt = `Bạn là trợ lý AI của FashionStoreApp.
Luôn trả lời bằng tiếng Việt, ngắn gọn, thân thiện.
Hỗ trợ: tìm sản phẩm, tư vấn phối đồ, hướng dẫn app.

${context ? `Lịch sử chat:\n${context}\n` : ""}

User: ${message}`;

    // Generate response với Gemini 2.0 Flash
    const llmResponse = await ai.generate({
      model: gemini20Flash,
      prompt: systemPrompt,
      config: {
        temperature: 0.7,
        maxOutputTokens: 500,
      },
    });

    // Save to Firestore
    if (userId && sessionId) {
      await db
        .collection("users")
        .doc(userId)
        .collection("chatHistory")
        .add({
          message: message,
          isUser: true,
          timestamp: Date.now(),
          sessionId: sessionId,
        });

      await db
        .collection("users")
        .doc(userId)
        .collection("chatHistory")
        .add({
          message: llmResponse.text(),
          isUser: false,
          timestamp: Date.now(),
          sessionId: sessionId,
        });
    }

    return {response: llmResponse.text()};
  }
);

// Export Cloud Function
exports.chatbot = onRequest(
  {
    cors: true,
    maxInstances: 10,
    timeoutSeconds: 60,
    memory: "256MiB",
  },
  async (req, res) => {
    // Validate request
    if (req.method !== "POST") {
      return res.status(405).json({error: "Method not allowed"});
    }

    const {message, userId, sessionId} = req.body;

    if (!message) {
      return res.status(400).json({error: "Message is required"});
    }

    try {
      // Call AI flow
      const result = await chatFlow({message, userId, sessionId});
      res.json(result);
    } catch (error) {
      console.error("Error:", error);
      res.status(500).json({error: error.message});
    }
  }
);

// Rate limiting middleware (simple implementation)
const requestCounts = new Map();
const RATE_LIMIT = 20; // requests per minute
const RATE_WINDOW = 60000; // 1 minute

function rateLimitMiddleware(req, res, next) {
  const userId = req.body.userId || req.ip;
  const now = Date.now();

  if (!requestCounts.has(userId)) {
    requestCounts.set(userId, {count: 1, resetTime: now + RATE_WINDOW});
    return next();
  }

  const userLimit = requestCounts.get(userId);

  if (now > userLimit.resetTime) {
    requestCounts.set(userId, {count: 1, resetTime: now + RATE_WINDOW});
    return next();
  }

  if (userLimit.count >= RATE_LIMIT) {
    return res.status(429).json({
      error: "Rate limit exceeded. Please try again later.",
    });
  }

  userLimit.count++;
  next();
}
```

### 4.4: Configure Environment Variables

```powershell
# Set API key cho Gemini (nếu dùng Google AI Studio)
firebase functions:config:set googleai.apikey="YOUR_API_KEY"

# Hoặc dùng Firebase Vertex AI (không cần API key)
# Vertex AI tự động authenticate qua Firebase project
```

### 4.5: Deploy Cloud Functions

```powershell
cd functions
firebase deploy --only functions
```

**Output**:
```
✔  functions[chatbot(us-central1)]: Successful create operation.
Function URL: https://us-central1-fashionstoreapp-59e3f.cloudfunctions.net/chatbot
```

---

## 🔐 PHẦN 5: BẢO MẬT & TỐI ƯU

### 5.1: Firebase App Check Integration (Android)

**File**: `app/src/main/java/com/example/fashionstoreapp/FashionStoreApplication.java`

```java
package com.example.fashionstoreapp;

import android.app.Application;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;

public class FashionStoreApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        
        // Initialize App Check with Play Integrity
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        );
    }
}
```

**Update `AndroidManifest.xml`**:
```xml
<application
    android:name=".FashionStoreApplication"
    ...>
    ...
</application>
```

### 5.2: Rate Limiting Client-Side

**File**: `FirebaseAIService.java` - Thêm rate limiting

```java
private static final int MAX_REQUESTS_PER_MINUTE = 15;
private final Queue<Long> requestTimestamps = new LinkedList<>();

private boolean checkRateLimit() {
    long now = System.currentTimeMillis();
    
    // Remove timestamps older than 1 minute
    while (!requestTimestamps.isEmpty() && 
           requestTimestamps.peek() < now - 60000) {
        requestTimestamps.poll();
    }
    
    if (requestTimestamps.size() >= MAX_REQUESTS_PER_MINUTE) {
        return false; // Rate limit exceeded
    }
    
    requestTimestamps.offer(now);
    return true;
}
```

### 5.3: Response Caching

```java
private final Map<String, CachedResponse> responseCache = new HashMap<>();

private static class CachedResponse {
    String response;
    long timestamp;
    
    CachedResponse(String response) {
        this.response = response;
        this.timestamp = System.currentTimeMillis();
    }
    
    boolean isExpired() {
        return System.currentTimeMillis() - timestamp > 300000; // 5 minutes
    }
}

// Check cache before calling API
public void sendMessageWithCache(String message, ChatCallback callback) {
    String cacheKey = message.toLowerCase().trim();
    
    if (responseCache.containsKey(cacheKey)) {
        CachedResponse cached = responseCache.get(cacheKey);
        if (!cached.isExpired()) {
            callback.onSuccess(cached.response);
            return;
        }
    }
    
    // Call API and cache response
    sendMessage(message, new ChatCallback() {
        @Override
        public void onSuccess(String response) {
            responseCache.put(cacheKey, new CachedResponse(response));
            callback.onSuccess(response);
        }
        
        @Override
        public void onError(String error) {
            callback.onError(error);
        }
    });
}
```

---

## 📱 PHẦN 6: ANDROID IMPLEMENTATION HOÀN CHỈNH

### 6.1: Call Cloud Function từ Android

**Tạo file**: `app/src/main/java/com/example/fashionstoreapp/services/CloudFunctionService.java`

```java
package com.example.fashionstoreapp.services;

import android.util.Log;
import okhttp3.*;
import org.json.JSONObject;
import java.io.IOException;

public class CloudFunctionService {
    private static final String TAG = "CloudFunctionService";
    private static final String FUNCTION_URL = 
        "https://us-central1-fashionstoreapp-59e3f.cloudfunctions.net/chatbot";
    
    private final OkHttpClient client;
    
    public CloudFunctionService() {
        client = new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build();
    }
    
    public void sendMessage(String message, String userId, String sessionId, 
                          ChatCallback callback) {
        try {
            JSONObject json = new JSONObject();
            json.put("message", message);
            json.put("userId", userId);
            json.put("sessionId", sessionId);
            
            RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json")
            );
            
            Request request = new Request.Builder()
                .url(FUNCTION_URL)
                .post(body)
                .build();
            
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String responseBody = response.body().string();
                            JSONObject jsonResponse = new JSONObject(responseBody);
                            String aiResponse = jsonResponse.getString("response");
                            callback.onSuccess(aiResponse);
                        } catch (Exception e) {
                            callback.onError("Error parsing response: " + e.getMessage());
                        }
                    } else {
                        callback.onError("Request failed: " + response.code());
                    }
                }
                
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError("Network error: " + e.getMessage());
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error creating request", e);
            callback.onError("Error: " + e.getMessage());
        }
    }
    
    public interface ChatCallback {
        void onSuccess(String response);
        void onError(String error);
    }
}
```

---

## 🎯 PHẦN 7: TESTING & DEPLOYMENT

### 7.1: Local Testing với Emulator

```powershell
# Start Firebase emulators
firebase emulators:start

# Output:
# ✔  functions[chatbot]: http function initialized (http://localhost:5001/...)
# ✔  firestore: Emulator running on http://localhost:8080
```

**Test trong Android**:
- Update `FUNCTION_URL` thành local: `http://10.0.2.2:5001/fashionstoreapp-59e3f/us-central1/chatbot`
- Run app trên emulator
- Test chatbot

### 7.2: Production Deployment

```powershell
# Build Android app
.\gradlew clean assembleRelease

# Deploy Cloud Functions
firebase deploy --only functions

# Deploy Firestore rules
firebase deploy --only firestore:rules
```

### 7.3: Monitoring & Logging

**Firebase Console**:
1. **Functions → Logs**: Xem logs Cloud Functions
2. **Vertex AI → Usage**: Monitor AI requests
3. **Firestore → Usage**: Check database reads/writes
4. **App Check → Metrics**: Verify app authenticity

---

## 📊 PHẦN 8: GIỚI HẠN & PRICING (FREE TIER)

### Firebase Vertex AI (Gemini 2.0 Flash)

**Free tier**:
- ✅ **1,500 requests/day**
- ✅ **15 requests/minute**
- ✅ No credit card required

**Paid pricing** (nếu vượt free tier):
- Input: $0.075 / 1M tokens
- Output: $0.30 / 1M tokens

### Cloud Functions

**Free tier**:
- ✅ 2M invocations/month
- ✅ 400,000 GB-seconds compute
- ✅ 200,000 CPU-seconds compute
- ✅ 5GB outbound networking

### Firestore

**Free tier**:
- ✅ 50,000 reads/day
- ✅ 20,000 writes/day
- ✅ 1GB storage

---

## 🐛 PHẦN 9: TROUBLESHOOTING

### Lỗi: "Vertex AI API not enabled"

**Solution**:
```
1. Firebase Console → Build → Vertex AI
2. Click "Enable"
3. Wait 2-5 minutes for activation
4. Rebuild app: .\gradlew clean installDebug
```

### Lỗi: "PERMISSION_DENIED"

**Solution**:
```
1. Check Firestore rules
2. Ensure user is authenticated (FirebaseAuth)
3. Verify App Check is configured
```

### Lỗi: "Rate limit exceeded"

**Solution**:
```
- Free tier: 1,500 requests/day, 15/minute
- Implement caching (đã có trong code)
- Add retry with exponential backoff
- Consider upgrading to Blaze plan
```

### Cloud Function không response

**Solution**:
```powershell
# Check logs
firebase functions:log

# Test locally
firebase emulators:start
```

---

## ✅ CHECKLIST CẤU HÌNH HOÀN CHỈNH

### Firebase Console
- [ ] Project created
- [ ] Android app registered với correct package name
- [ ] `google-services.json` downloaded
- [ ] Vertex AI in Firebase enabled
- [ ] Firestore database created
- [ ] Firestore security rules configured
- [ ] Firebase App Check enabled
- [ ] Cloud Functions deployed

### Android Project
- [ ] `google-services.json` in `app/` folder
- [ ] Firebase BOM dependency added
- [ ] `firebase-vertexai` dependency added
- [ ] `firebase-appcheck-playintegrity` dependency added
- [ ] App Check initialized in Application class
- [ ] FirebaseAIService implemented
- [ ] ChatHistoryManager implemented
- [ ] ChatbotActivity UI implemented

### Cloud Functions
- [ ] Firebase CLI installed
- [ ] Functions initialized
- [ ] Genkit dependencies installed
- [ ] `index.js` implemented
- [ ] Environment variables configured
- [ ] Functions deployed successfully

### Testing
- [ ] Local emulator testing passed
- [ ] Chat sends và receives messages
- [ ] Firestore saves chat history
- [ ] Rate limiting works
- [ ] App Check validates requests
- [ ] Error handling works correctly

---

## 📞 HỖ TRỢ & TÀI LIỆU

### Official Documentation
- [Firebase Vertex AI](https://firebase.google.com/docs/vertex-ai)
- [Firebase Genkit](https://firebase.google.com/docs/genkit)
- [Gemini API](https://ai.google.dev/gemini-api/docs)
- [Cloud Functions](https://firebase.google.com/docs/functions)
- [Firestore](https://firebase.google.com/docs/firestore)

### Community
- [Firebase Discord](https://discord.gg/firebase)
- [Stack Overflow - Firebase](https://stackoverflow.com/questions/tagged/firebase)

---

**🎉 Hoàn thành! Chatbot AI 100% Firebase Ecosystem**


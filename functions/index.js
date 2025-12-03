const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { GoogleGenerativeAI } = require("@google/generative-ai");

// TODO: Thêm API key vào Firebase Config bằng lệnh:
// firebase functions:config:set gemini.api_key="YOUR_API_KEY"
// Hoặc tạm thời hardcode ở đây để test (không an toàn cho production)
const GEMINI_API_KEY = "AIzaSyD_TwmieQwowirVOv974ctT6ZFyvlkr25M";

const genAI = new GoogleGenerativeAI(GEMINI_API_KEY);

// System instruction cho chatbot
const SYSTEM_INSTRUCTION = `Bạn là trợ lý AI của ứng dụng bán hàng thời trang FashionStoreApp.

YÊU CẦU:
- Luôn trả lời bằng tiếng Việt
- Trả lời ngắn gọn, rõ ràng (tối đa 3-4 câu)
- Giọng điệu thân thiện, chuyên nghiệp
- Không dùng quá 2 emoji mỗi câu trả lời

NHIỆM VỤ:
1. Hỗ trợ tìm sản phẩm: áo, quần, váy, phụ kiện
2. Tư vấn phối đồ theo: phong cách, thời tiết, vóc dáng
3. Hướng dẫn: đặt hàng, giỏ hàng, voucher, đăng nhập
4. Không trả lời câu hỏi ngoài phạm vi thời trang và app

Ví dụ:
- User: "áo nữ giá rẻ" → Trả lời: "Dưới đây là gợi ý áo nữ giá tốt..."
- User: "outfit đi làm" → Trả lời: "Bạn có thể thử phối..."
- User: "cách đặt hàng" → Trả lời: "Bạn thực hiện theo bước..."`;

/**
 * Cloud Function để xử lý chat với Gemini AI
 * 
 * Input: { message: string, chatHistory: Array<{role, content}> }
 * Output: { response: string, success: boolean }
 */
exports.chatWithGemini = onCall({
    maxInstances: 10,
    timeoutSeconds: 60,
    memory: "256MiB",
}, async (request) => {
    try {
        const { message, chatHistory } = request.data;

        // Validate input
        if (!message || typeof message !== "string") {
            throw new HttpsError("invalid-argument", "Message is required");
        }

        console.log("Processing message:", message);

        // Initialize Gemini model
        const model = genAI.getGenerativeModel({
            model: "gemini-1.5-flash",
            systemInstruction: SYSTEM_INSTRUCTION,
            generationConfig: {
                temperature: 0.7,
                maxOutputTokens: 500,
            },
        });

        // Build chat history for context
        const history = [];
        if (chatHistory && Array.isArray(chatHistory)) {
            // Take last 10 messages to avoid token limit
            const recentHistory = chatHistory.slice(-10);
            for (const msg of recentHistory) {
                history.push({
                    role: msg.role === "user" ? "user" : "model",
                    parts: [{ text: msg.content }],
                });
            }
        }

        // Start chat session with history
        const chat = model.startChat({ history });

        // Send message and get response
        const result = await chat.sendMessage(message);
        const response = result.response;
        const aiResponse = response.text();

        console.log("AI Response:", aiResponse);

        return {
            success: true,
            response: aiResponse.trim(),
        };
    } catch (error) {
        console.error("Error in chatWithGemini:", error);

        // Handle specific errors
        if (error.message?.includes("quota")) {
            throw new HttpsError(
                "resource-exhausted",
                "Đã vượt quá giới hạn sử dụng. Vui lòng thử lại sau!",
            );
        }

        if (error.message?.includes("API_KEY")) {
            throw new HttpsError(
                "failed-precondition",
                "Cấu hình API key chưa đúng. Vui lòng liên hệ quản trị viên!",
            );
        }

        throw new HttpsError(
            "internal",
            "Xin lỗi, tôi đang gặp sự cố. Vui lòng thử lại!",
        );
    }
});

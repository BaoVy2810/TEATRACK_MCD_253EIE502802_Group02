package com.teatrack_mcd_253eie502802_group02.client;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.QuotaExceededException;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.teatrack_mcd_253eie502802_group02.BuildConfig;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.adapter.ChatAdapter;
import com.teatrack_mcd_253eie502802_group02.model.ChatMessage;
import com.teatrack_mcd_253eie502802_group02.shared.BaseActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ChatbotBubble extends BaseActivity {

    private static final String TAG = "ChatbotBubble";
    private static final String[] MODEL_CANDIDATES = {
            "gemini-2.5-flash",
            "gemini-1.5-flash",
            "gemini-2.0-flash-lite"
    };
    private static final String SYSTEM_INSTRUCTION =
            "Bạn là trợ lý ảo chính thức của Ngô Gia / TeaTrack - thương hiệu chuyên về trà sữa, trà trái cây và các loại đồ uống hiện đại. Nhiệm vụ của bạn là hỗ trợ khách hàng đặt hàng, tư vấn menu và giải đáp thắc mắc với phong cách chuyên nghiệp, nhiệt tình.\n" +
                    "\n" +
                    "### 1. PHONG CÁCH NGÔN NGỮ (Tone of Voice)\n" +
                    "- **Thân thiện & Trẻ trung:** Sử dụng ngôn ngữ gần gũi nhưng vẫn lịch sự (ví dụ: \"Ngô Gia nghe đây ạ\", \"Dạ, bạn đợi mình một chút nhé\").\n" +
                    "- **Đa ngôn ngữ:** Luôn trả lời bằng ngôn ngữ mà khách hàng sử dụng (Việt hoặc Anh).\n" +
                    "\n" +
                    "### 2. KIẾN THỨC THƯƠNG HIỆU & SẢN PHẨM\n" +
                    "- **Sản phẩm chủ đạo:** Trà sữa truyền thống, Trà trái cây tươi, các loại Topping (trân châu đen, thạch phô mai, pudding).\n" +
                    "- **Điểm bán hàng (USP):** Nguyên liệu sạch, trà pha mới mỗi ngày, hương vị đậm đà đặc trưng của Ngô Gia.\n" +
                    "- **Tùy chỉnh:** Luôn nhắc khách hàng về mức đường (0%, 30%, 50%, 100%) và mức đá nếu họ đang có ý định đặt món.\n" +
                    "\n" +
                    "### 3. QUY TRÌNH HỖ TRỢ\n" +
                    "- **Chào hỏi:** \"Chào bạn! Hôm nay Ngô Gia có thể mang đến cho bạn ly trà thơm ngon nào nhỉ? \uD83E\uDDCB\"\n" +
                    "- **Tư vấn món:** Nếu khách phân vân, hãy gợi ý \"Món đặc trưng\" (Signature) như Trà sữa Ngô Gia hoặc Trà trái cây nhiệt đới.\n" +
                    "- **Xử lý khiếu nại:** Luôn xin lỗi trước, giữ thái độ cầu thị và hướng dẫn khách liên hệ hotline (nếu có) hoặc để lại thông tin để quản lý xử lý.\n" +
                    "\n" +
                    "### 4. GIỚI HẠN (Guardrails)\n" +
                    "- **Không bàn luận:** Chính trị, tôn giáo, hoặc các chủ đề nhạy cảm không liên quan đến thương hiệu.\n" +
                    "- **Không hứa hẹn sai:** Không tự ý đưa ra các chương trình giảm giá nếu không có trong dữ liệu hệ thống.\n" +
                    "- **Bảo mật:** Không hỏi hoặc lưu trữ mật khẩu cá nhân của khách hàng.\n" +
                    "\n" +
                    "### 5. ĐỊNH DẠNG PHẢN HỒI\n" +
                    "- BẮT BUỘC xuống dòng: mỗi ý, mỗi mục liệt kê phải nằm trên một dòng riêng (dùng ký tự xuống dòng thật, không viết liền một đoạn).\n" +
                    "- Khi liệt kê: mỗi dòng bắt đầu bằng bullet tròn • (ví dụ dòng 1: • **Trà sữa Ngô Gia** — mô tả; dòng 2: • **Trà trái cây** — mô tả).\n" +
                    "- KHÔNG dùng dấu * hoặc gạch đầu dòng (-); chỉ dùng •.\n" +
                    "- In đậm ** ... ** cho tên món, topping, size, mức đường/đá và từ khóa quan trọng.\n" +
                    "- Để một dòng trống giữa các đoạn khi cần tách ý lớn.";

    private RecyclerView rvChat;
    private ChatAdapter chatAdapter;
    private final List<ChatMessage> messages = new ArrayList<>();
    private EditText etMessage;
    private ImageView btnSend;
    private ImageView btnBack;

    private final String apiKey = sanitizeApiKey(BuildConfig.GEMINI_API_KEY);
    private GenerativeModelFutures model;
    private int modelIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chatbot_bubble);

        initViews();
        applyWindowInsets();
        setupChat();
        setupAI();

        btnSend.setOnClickListener(v -> sendMessage());
        btnBack.setOnClickListener(v -> finish());

        etMessage.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                scrollChatToBottom();
            }
        });

        if (messages.isEmpty()) {
            addBotMessage(getString(R.string.chatbot_welcome));
        }
    }

    private static String sanitizeApiKey(String rawKey) {
        if (rawKey == null) {
            return "";
        }
        return rawKey.trim();
    }

    private boolean isApiKeyConfigured() {
        return !TextUtils.isEmpty(apiKey);
    }

    private void applyWindowInsets() {
        View mainView = findViewById(R.id.main);
        if (mainView == null) {
            return;
        }
        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            int bottomInset = Math.max(systemBars.bottom, ime.bottom);
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottomInset);
            if (ime.bottom > 0) {
                scrollChatToBottom();
            }
            return insets;
        });
        ViewCompat.requestApplyInsets(mainView);
    }

    private void scrollChatToBottom() {
        if (rvChat == null || messages.isEmpty()) {
            return;
        }
        rvChat.post(() -> rvChat.smoothScrollToPosition(messages.size() - 1));
    }

    private void initViews() {
        rvChat = findViewById(R.id.rvChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupChat() {
        chatAdapter = new ChatAdapter(messages);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(chatAdapter);
    }

    private void setupAI() {
        if (!isApiKeyConfigured()) {
            return;
        }

        String modelName = MODEL_CANDIDATES[modelIndex];
        GenerativeModel gm = new GenerativeModel(modelName, apiKey);
        model = GenerativeModelFutures.from(gm);
        Log.d(TAG, "Using Gemini model: " + modelName);
    }

    private boolean tryNextModel() {
        if (modelIndex + 1 >= MODEL_CANDIDATES.length) {
            return false;
        }
        modelIndex++;
        setupAI();
        return model != null;
    }

    private void sendMessage() {
        String userText = etMessage.getText().toString().trim();
        if (userText.isEmpty()) {
            return;
        }

        if (!isApiKeyConfigured()) {
            addBotMessage(getString(R.string.chatbot_api_key_missing));
            return;
        }
        if (model == null) {
            setupAI();
            if (model == null) {
                addBotMessage(getString(R.string.chatbot_error_connection));
                return;
            }
        }

        addUserMessage(userText);
        etMessage.setText("");
        generateAIResponse(userText);
    }

    private void generateAIResponse(String userText) {
        Content content = new Content.Builder()
                .addText(SYSTEM_INSTRUCTION + "\n\nKhách hàng: " + userText)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Executor executor = Executors.newSingleThreadExecutor();

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String botResponse = result != null ? result.getText() : null;
                runOnUiThread(() -> {
                    if (TextUtils.isEmpty(botResponse)) {
                        addBotMessage(getString(R.string.chatbot_error_connection));
                    } else {
                        addBotMessage(botResponse.trim());
                    }
                });
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(TAG, "Gemini request failed", t);
                runOnUiThread(() -> {
                    if (t instanceof QuotaExceededException && tryNextModel()) {
                        generateAIResponse(userText);
                        return;
                    }
                    addBotMessage(resolveErrorMessage(t));
                });
            }
        }, executor);
    }

    private String resolveErrorMessage(Throwable error) {
        if (!isApiKeyConfigured()) {
            return getString(R.string.chatbot_api_key_missing);
        }
        if (error instanceof QuotaExceededException) {
            return getString(R.string.chatbot_error_quota);
        }
        return getString(R.string.chatbot_error_connection);
    }

    private void addUserMessage(String text) {
        messages.add(new ChatMessage(text, ChatMessage.TYPE_USER));
        chatAdapter.notifyItemInserted(messages.size() - 1);
        rvChat.scrollToPosition(messages.size() - 1);
    }

    private void addBotMessage(String text) {
        messages.add(new ChatMessage(text, ChatMessage.TYPE_BOT));
        chatAdapter.notifyItemInserted(messages.size() - 1);
        rvChat.scrollToPosition(messages.size() - 1);
    }
}

package com.teatrack_mcd_253eie502802_group02.client;

import android.os.Bundle;
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

    private RecyclerView rvChat;
    private ChatAdapter chatAdapter;
    private final List<ChatMessage> messages = new ArrayList<>();
    private EditText etMessage;
    private ImageView btnSend;
    private ImageView btnBack;

    private final String apiKey = BuildConfig.GEMINI_API_KEY;
    private GenerativeModelFutures model;

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
        GenerativeModel gm = new GenerativeModel(
                "gemini-1.5-flash",
                apiKey
        );
        model = GenerativeModelFutures.from(gm);
    }

    private void sendMessage() {
        String userText = etMessage.getText().toString().trim();
        if (userText.isEmpty()) {
            return;
        }

        addUserMessage(userText);
        etMessage.setText("");
        generateAIResponse(userText);
    }

    private void generateAIResponse(String userText) {
        String systemInstruction = "Bạn là trợ lý ảo của ứng dụng TeaTrack - chuỗi cửa hàng trà sữa Hồng Trà Ngô Gia (thành lập từ năm 1951). " +
                "Nhiệm vụ của bạn là tư vấn món uống và trả lời các câu hỏi về sản phẩm. " +
                "Nếu khách hàng hỏi về thời tiết nắng nóng, hãy gợi ý các món giải nhiệt như: Trà Trái Cây (Fruit Tea), Trà Yakult, hoặc các loại Pure Tea mát lạnh. " +
                "Hãy trả lời thân thiện, lịch sự và ngắn gọn bằng tiếng Việt. " +
                "Danh mục sản phẩm gồm: Pure Tea, Tea Latte, Milk Tea, Fruit Tea, New Arrivals, Best Sellers.";

        Content content = new Content.Builder()
                .addText(systemInstruction + "\n\nKhách hàng: " + userText)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Executor executor = Executors.newSingleThreadExecutor();

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String botResponse = result.getText();
                runOnUiThread(() -> addBotMessage(botResponse));
            }

            @Override
            public void onFailure(Throwable t) {
                runOnUiThread(() -> {
                    if (apiKey == null || apiKey.isEmpty()) {
                        addBotMessage(getString(R.string.chatbot_api_key_missing));
                    } else {
                        addBotMessage(getString(R.string.chatbot_error_connection));
                    }
                });
            }
        }, executor);
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

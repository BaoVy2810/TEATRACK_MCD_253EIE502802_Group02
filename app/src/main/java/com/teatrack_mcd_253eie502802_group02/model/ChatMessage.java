package com.teatrack_mcd_253eie502802_group02.model;

public class ChatMessage {
    public static final int TYPE_USER = 0;
    public static final int TYPE_BOT = 1;

    private String content;
    private int type;
    private long timestamp;

    public ChatMessage(String content, int type) {
        this.content = content;
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }

    public String getContent() { return content; }
    public int getType() { return type; }
    public long getTimestamp() { return timestamp; }
}

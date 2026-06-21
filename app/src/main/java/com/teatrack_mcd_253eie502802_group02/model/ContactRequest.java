package com.teatrack_mcd_253eie502802_group02.model;

public class ContactRequest {
    private String fullName;
    private String email;
    private String phone;
    private String branch;
    private String topic;
    private String content;
    private long timestamp;

    public ContactRequest() {
    }

    public ContactRequest(String fullName, String email, String phone, String branch, String topic, String content, long timestamp) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.branch = branch;
        this.topic = topic;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}

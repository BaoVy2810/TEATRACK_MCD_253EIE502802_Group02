package com.teatrack_mcd_253eie502802_group02.model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class ContactRequest {
    private String _id;
    private String fullname;
    private String email;
    private String phone;
    private String branch;
    private String topic;
    private String content;
    private String time;
    private int status;
    private boolean read;
    private String note;

    public ContactRequest() {
    }

    public ContactRequest(String _id, String fullname, String email, String phone, String branch, String topic, String content, String time, int status, boolean read, String note) {
        this._id = _id;
        this.fullname = fullname;
        this.email = email;
        this.phone = phone;
        this.branch = branch;
        this.topic = topic;
        this.content = content;
        this.time = time;
        this.status = status;
        this.read = read;
        this.note = note;
    }

    public String get_id() { return _id; }
    public void set_id(String _id) { this._id = _id; }

    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }

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

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}

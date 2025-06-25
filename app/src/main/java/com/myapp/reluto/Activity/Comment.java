package com.myapp.reluto.Activity;

public class Comment {
    private String fullName;
    private String commentText;
    private String timestamp;

    public Comment(String fullName, String commentText, String timestamp) {
        this.fullName = fullName;
        this.commentText = commentText;
        this.timestamp = timestamp;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}


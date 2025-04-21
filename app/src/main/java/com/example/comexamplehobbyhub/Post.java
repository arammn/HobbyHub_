package com.example.comexamplehobbyhub;
public class Post {
    private String userId;
    private String userName;
    private String postTime;
    private String postContent;
    private String userAvatar;
    private String postImage;

    private String postId; // Add this field


    public Post() {}

    public Post(String userId, String userName, String postTime, String postContent, String userAvatar, String postImage, String postId) {
        this.userId = userId;
        this.userName = userName;
        this.postTime = postTime;
        this.postContent = postContent;
        this.userAvatar = userAvatar;
        this.postImage = postImage;
        this.postId = postId;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getPostTime() {
        return postTime;
    }

    public String getPostContent() {
        return postContent;
    }


    public String getPostImage() {
        return postImage;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }
}

package com.example.comexamplehobbyhub;

import java.util.ArrayList;
import java.util.List;

public class Post {
    private String userId;
    private String postTime;
    private String postContent;
    private String postImage;
    private String postId;

    private List<String> likes = new ArrayList<>();
    private int likeCount = 0;


    public Post() {}

    public Post(String userId, String postTime, String postContent, String postImage, String postId, List<String> likes, int likeCount) {
        this.userId = userId;
        this.postTime = postTime;
        this.postContent = postContent;
        this.postImage = postImage;
        this.postId = postId;
        this.likes = likes;
        this.likeCount = likeCount;
    }

    public String getUserId() {
        return userId;
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


    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setPostTime(String postTime) {
        this.postTime = postTime;
    }

    public void setPostContent(String postContent) {
        this.postContent = postContent;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public List<String> getLikes() {
        return likes;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikes(List<String> likes) {
        this.likes = likes;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }
}

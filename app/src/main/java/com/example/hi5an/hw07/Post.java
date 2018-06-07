package com.example.hi5an.hw07;

/**
 * Created by hi5an on 11/19/2017.
 */

public class Post {
    String postTitle;
    String postMsg;
    String postTime;
    String postedBy;
    String postId;
    String postedByKey;

    public String getPostedByKey() {
        return postedByKey;
    }

    public void setPostedByKey(String postedByKey) {
        this.postedByKey = postedByKey;
    }

    public String getPostTitle() {
        return postTitle;
    }

    public void setPostTitle(String postTitle) {
        this.postTitle = postTitle;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getPostMsg() {
        return postMsg;
    }

    public void setPostMsg(String postMsg) {
        this.postMsg = postMsg;
    }

    public String getPostTime() {
        return postTime;
    }

    public void setPostTime(String postTime) {
        this.postTime = postTime;
    }

    public String getPostedBy() {
        return postedBy;
    }

    public void setPostedBy(String postedBy) {
        this.postedBy = postedBy;
    }
}

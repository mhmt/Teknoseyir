package com.teknoseyir.tsapp;

/**
 * Created by razor on 30.06.2018.
 */

public class Comment {
    private int id;
    private int parent;
    private int authorId;
    private String authorName;
    private String authorUsername;
    private String date;
    private String content;
    private String authorImgURL;
    private String mediaURL;
    private long unixTime;

    public Comment(int id, int parent, int authorId, String authorName, String authorUsername, String date, String content, String authorImgURL, String mediaURL, long unixTime) {
        this.id = id;
        this.parent = parent;
        this.authorId = authorId;
        this.authorName = authorName;
        this.authorUsername = authorUsername;
        this.authorImgURL = authorImgURL;
        this.mediaURL = mediaURL;
        this.date = date;
        this.content = content;
        this.unixTime = unixTime;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
    }

    public String getAuthorImgURL() {
        return authorImgURL;
    }

    public void setAuthorImgURL(String authorImgURL) {
        this.authorImgURL = authorImgURL;
    }

    public String getMediaURL() {
        return mediaURL;
    }

    public void setMediaURL(String mediaURL) {
        this.mediaURL = mediaURL;
    }

    public int getCommentId() {
        return id;
    }

    public void setCommentId(int id) {
        this.id = id;
    }

    public int getParent() {
        return parent;
    }

    public void setParent(int parent) {
        this.parent = parent;
    }

    public int getAuthorId() {
        return authorId;
    }

    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getUnixTime() {
        return unixTime;
    }

    public void setUnixTime(long unixTime) {
        this.unixTime = unixTime;
    }
}

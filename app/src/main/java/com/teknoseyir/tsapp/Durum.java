package com.teknoseyir.tsapp;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by razor on 7.06.2018.
 */

public class Durum{
    private static final String TAG = "DurumObject";
    //private Context context;
    private int id,author_id;
    private String date;
    private String content;
    private String author_name,author_username,media_url,author_avatar_url;
    private int likes,comments;
    private Date dateTime;
    private String full_image_url="";

    public Durum(Context context,int id, int author_id, String date, String content, int likes) {

        this.id = id;
        this.author_id = author_id;
        this.date = date;
        this.content = content;
        this.likes = likes;

     //   loadUserInfo();
     //   loadMedia();
    }

    public String getFull_image_url() {
        return full_image_url;
    }

    public void setFull_image_url(String full_image_url) {
        this.full_image_url = full_image_url;
    }

    public void setMedia_url(String media_url) {
        this.media_url = media_url;
    }

    public void setAuthor_name(String author_name) {
        this.author_name = author_name;
    }

    public void setAuthor_username(String author_username) {
        this.author_username = author_username;
    }

    public void setAuthor_avatar_url(String author_avatar_url) {
        this.author_avatar_url = author_avatar_url;
    }

    public void setComments(int comments) {
        this.comments = comments;
    }

    public Durum(Context context, int id, int author_id, String date, String content, String author_name, String author_username, String media_url, String author_avatar_url, int likes, int comments) {

        this.id = id;
        this.author_id = author_id;
        this.date = date;
        this.content = content;
        this.author_name = author_name;
        this.author_username = author_username;
        this.media_url = media_url;
        this.author_avatar_url = author_avatar_url;
        this.likes = likes;
        this.comments = comments;

        SimpleDateFormat sdfIn = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdfOut = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        try {
            Date ddate = sdfIn.parse(date);
           this.dateTime = sdfOut.parse(ddate.toString());
        }catch (Exception e){

        }
    }

    public Date getDateTime() {
        return dateTime;
    }


    public String getAuthor_avatar_url() {
        return author_avatar_url;
    }

    public int getId() {
        return id;
    }

    public int getAuthor_id() {
        return author_id;
    }

    public String getMedia_url() {
        return media_url;
    }

    public String getDate() {
        return date;
    }

    public String getContent() {
        return content;
    }

    public String getAuthor_name() {
        return author_name;
    }

    public String getAuthor_username() {
        return author_username;
    }

    public int getLikes() {
        return likes;
    }

    public int getComments() {
        return comments;
    }

}

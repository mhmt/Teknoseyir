package com.teknoseyir.tsapp;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.support.v7.app.AlertDialog;

import android.support.v7.view.ContextThemeWrapper;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by razor on 2.06.2018.
 */


public class DurumListAdapter extends BaseAdapter {
    private static final String TAG = "Adapter";
    ArrayList<Durum> list;
    Activity context;
    private static LayoutInflater inflater = null;

    public  DurumListAdapter(Activity activity, ArrayList<Durum> list){
        this.context = activity;
        this.list = list;
        inflater = LayoutInflater.from(context);

    }

    @Override
    public int getCount() {
        return this.list.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class Holder{
        LinearLayout likes_holder,comment_holder,lc_holder;
        TextView date,user,username,content,like_count,comment_count;
        ImageView user_img,media;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        Holder holder = new Holder();
        View rowView;
        rowView = inflater.inflate(R.layout.durum_item,null);
        final Durum durum = this.list.get(pos);

        holder.likes_holder = (LinearLayout)rowView.findViewById(R.id.durum_like_holder);
        holder.comment_holder = (LinearLayout)rowView.findViewById(R.id.durum_comment_holder);
        holder.lc_holder = (LinearLayout)rowView.findViewById(R.id.comment_like_holder);
        holder.user = (TextView)rowView.findViewById(R.id.user_name);
        holder.username = (TextView)rowView.findViewById(R.id.user_username);
        holder.date = (TextView)rowView.findViewById(R.id.durum_date);
        holder.content = (TextView)rowView.findViewById(R.id.durum_icerik);
        holder.like_count = (TextView)rowView.findViewById(R.id.durum_likes);
        holder.comment_count = (TextView)rowView.findViewById(R.id.durum_comments);
        holder.user_img = (ImageView) rowView.findViewById(R.id.user_img);
        holder.media = (ImageView) rowView.findViewById(R.id.durum_media);



        SimpleDateFormat sdfIn = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdfOut = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        try{
            Date ddate = sdfIn.parse(durum.getDate());
            holder.date.setText(sdfOut.format(ddate));
            long time =sdfOut.parse(sdfOut.format(ddate).toString()).getTime();
            long now = System.currentTimeMillis();

            CharSequence ago =
                    DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS);
            holder.date.setText(ago);
        }catch (Exception e){
            e.printStackTrace();
        }

        if(durum.getAuthor_name().equals("")
                && durum.getAuthor_username().equals("")
                && durum.getAuthor_avatar_url().equals(""))  loadDetails(durum,holder);
        else {
            Glide.with(context).load(durum.getAuthor_avatar_url()).into(holder.user_img);

            holder.user.setText(durum.getAuthor_name());
            holder.username.setText("@"+durum.getAuthor_username());


            if(durum.getComments() > 0){
                holder.comment_holder.setVisibility(View.VISIBLE);
                holder.comment_count.setText(""+durum.getComments());

            }else  holder.comment_holder.setVisibility(View.GONE);

            if(durum.getComments() == 0 && durum.getLikes() == 0){
                holder.lc_holder.setVisibility(View.GONE);
            }else holder.lc_holder.setVisibility(View.VISIBLE);

            if(durum.getMedia_url().equals("")){
                holder.media.setVisibility(View.GONE);
            }else{
                holder.media.setVisibility(View.VISIBLE);
                Glide.with(context).load(durum.getMedia_url()).into(holder.media);
            }
            if(durum.getMedia_url().equals("")){
                holder.media.setVisibility(View.GONE);
            }else{
                holder.media.setVisibility(View.VISIBLE);
                Glide.with(context).load(durum.getMedia_url()).into(holder.media);
            }

        }

        holder.content.setText(durum.getContent());

        holder.media.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(durum.getFull_image_url().equals("")){
                    showFullImage(durum.getMedia_url());
                }else  showFullImage(durum.getFull_image_url());
            }
        });


        if(durum.getLikes() > 0){
            holder.likes_holder.setVisibility(View.VISIBLE);
            holder.like_count.setText(""+durum.getLikes());
        }else  holder.likes_holder.setVisibility(View.GONE);




        return rowView;
    }

    public void loadDetails(final Durum durum,final Holder holder){

       final String comment_url = context.getResources().getString(R.string.COMMENT_URL);
        String url = context.getResources().getString(R.string.USER_URL)+durum.getAuthor_id();


        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "onResponse: "+response);
                        try{
                            JSONObject data = new JSONObject(response);
                            final   String  author_name = data.getString("name");
                            final   String  author_username = data.getString("slug");
                            final  String  author_avatar_url = data.getJSONObject("avatar_urls").getString("48");
                            final String user_url = data.getString("link");

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try{
                                        Document doc = Jsoup.connect(user_url).get();

                                        Elements img = doc.select("div.user-avatar img[src]");
                                        final String imgSrc = img.attr("src");
                                        Log.d(TAG, "onResponse: User IMAGE: "+imgSrc);

                                        context.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if(imgSrc.length()> 10){
                                                    durum.setAuthor_avatar_url(imgSrc);
                                                    Glide.with(context).load(imgSrc).into(holder.user_img);
                                                }else{
                                                    durum.setAuthor_avatar_url(author_avatar_url);
                                                    Glide.with(context).load(author_avatar_url).into(holder.user_img);
                                                }
                                            }
                                        });

                                    }catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                }
                            }).start();






                            holder.user.setText(author_name);
                            holder.username.setText("@"+author_username);

                            durum.setAuthor_name(author_name);
                            durum.setAuthor_username(author_username);
                         //   durum.setAuthor_avatar_url(author_avatar_url);

                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse: "+error.getLocalizedMessage());
            }
        });

        queue.add(stringRequest);


        String url2 = context.getResources().getString(R.string.MEDIA_URL)+durum.getId();
        StringRequest stringRequest2 = new StringRequest(Request.Method.GET, url2,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "onResponse: "+response);
                        final String media_url;
                        try{
                            JSONArray AllData = new JSONArray(response);
                            if(AllData.length() > 0){
                                JSONObject media = AllData.getJSONObject(0);
                                media_url = media.getJSONObject("media_details").getJSONObject("sizes").getJSONObject("thumbnail").getString("source_url");
                                String full_media_url = media.getJSONObject("guid").getString("rendered");
                                durum.setFull_image_url(full_media_url);
                            }else media_url = "";

                            if(media_url.equals("")){
                                holder.media.setVisibility(View.GONE);
                            }else{
                                holder.media.setVisibility(View.VISIBLE);
                                Glide.with(context).load(media_url).into(holder.media);
                                durum.setMedia_url(media_url);
                            }

                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse: "+error.getLocalizedMessage());
            }
        });

        queue.add(stringRequest2);



        StringRequest stringRequest3 = new StringRequest(Request.Method.GET, comment_url+durum.getId(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "onResponse: "+response);
                        try{
                            JSONArray data = new JSONArray(response);
                            int comment_count = data.length();


                            if(comment_count > 0){
                                holder.comment_holder.setVisibility(View.VISIBLE);
                                holder.comment_count.setText(""+comment_count);
                                durum.setComments(comment_count);
                            }else  holder.comment_holder.setVisibility(View.GONE);

                            if(comment_count == 0 && durum.getLikes() == 0){
                                holder.lc_holder.setVisibility(View.GONE);
                            }else holder.lc_holder.setVisibility(View.VISIBLE);



                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse: "+error.getLocalizedMessage());
            }
        });

        queue.add(stringRequest3);

    }

    public void showFullImage(final String img_url){
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.myDialog));

        View dialogLayout = inflater.inflate(R.layout.fullimage_view, null);

        ImageView image = (ImageView) dialogLayout.findViewById(R.id.media_full_image);
        Glide.with(context).load(img_url).fitCenter().listener(new LoggingListener<String, GlideDrawable>()).into(image);


        builder.setView(dialogLayout);
        builder.show();

    }

    public class LoggingListener<T, R> implements RequestListener<T, R> {
        @Override public boolean onException(Exception e, Object model, Target target, boolean isFirstResource) {
            android.util.Log.d("GLIDE", String.format(Locale.ROOT,
                    "onException(%s, %s, %s, %s)", e, model, target, isFirstResource), e);
            return false;
        }
        @Override public boolean onResourceReady(Object resource, Object model, Target target, boolean isFromMemoryCache, boolean isFirstResource) {
            android.util.Log.d("GLIDE", String.format(Locale.ROOT,
                    "onResourceReady(%s, %s, %s, %s, %s)", resource, model, target, isFromMemoryCache, isFirstResource));
            return false;
        }
    }

    public void loadAvatar(final String imgSrc,final Durum durum,final Holder holder,final String author_avatar_url){


    }
}
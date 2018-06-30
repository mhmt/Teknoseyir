package com.teknoseyir.tsapp;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class DurumDetail extends AppCompatActivity {
    private static final String TAG = "DurumDetail";
    Durum durum;
    LinearLayout likes_holder,comment_holder,lc_holder;
    TextView date,user,username,content,like_count,comment_count;
    ImageView user_img,media;
    Activity context;
    ListView comments_lv;
    ArrayList<Comment> allComments = new ArrayList<>();
    CommentListAdapter adapter;

    
    private static LayoutInflater inflater = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_durum_detail);
        Gson gson = new Gson();
        String durum_json = getIntent().getStringExtra("durumJSON");
        durum = gson.fromJson(durum_json.toString(),Durum.class);
       //
        initView();
        loadData();
        loadComments();

    }

    public void loadComments(){
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = context.getResources().getString(R.string.COMMENT_URL)+durum.getId();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "onResponse Comments: "+response);

                        try{
                            JSONArray AllData = new JSONArray(response);
                            if(AllData.length() > 0){
                                comments_lv.setVisibility(View.VISIBLE);
                                for (int i = 0; i < AllData.length(); i++) {
                                    JSONObject comment = AllData.getJSONObject(i);
                                    try{
                                        int cid = comment.getInt("id");
                                        int parent = comment.getInt("parent");
                                        int authorId = comment.getInt("author");
                                        String authorName = comment.getString("author_name");
                                        String date = comment.getString("date").replace("T"," ");
                                        String content = stripHtml(comment.getJSONObject("content").getString("rendered"));

                                        SimpleDateFormat sdfIn = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                        SimpleDateFormat sdfOut = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

                                        Date ddate = sdfIn.parse(date);

                                        long time =sdfOut.parse(sdfOut.format(ddate).toString()).getTime();
                                        long now = System.currentTimeMillis();

                                        CharSequence ago =
                                                DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS);
                                        date = ago.toString();

                                        Comment commentObj = new Comment(cid,parent,authorId,authorName,"",date,content,"","",time);
                                        allComments.add(commentObj);

                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }

                                parseComments(allComments);

                            }else{
                                comments_lv.setVisibility(View.GONE);
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

        queue.add(stringRequest);
    }

    public void parseComments(ArrayList<Comment> list){
        ArrayList<Comment> parents = new ArrayList<>();
        ArrayList<Comment> finalList = new ArrayList<>();

        for (int i = 0; i < list.size() ; i++) {
            Comment current = list.get(i);
            if(current.getParent() == 0) parents.add(current);
        }

        Collections.sort(parents, new Comparator<Comment>() {
            public int compare(Comment o1, Comment o2) {
                if (o1.getUnixTime() == 0 || o2.getUnixTime() == 0)
                    return 0;
                return (int)o1.getUnixTime()-(int)(o2.getUnixTime());
            }
        });


        for (int i = 0; i < parents.size(); i++) {
            Comment parent = parents.get(i);
            ArrayList<Comment> childs = new ArrayList<>();
            for (int j = 0; j < list.size(); j++) {
                if(list.get(j).getParent() == parent.getCommentId()) childs.add(list.get(j));
            }
            Collections.sort(childs, new Comparator<Comment>() {
                public int compare(Comment o1, Comment o2) {
                    if (o1.getUnixTime() == 0 || o2.getUnixTime() == 0)
                        return 0;
                    return (int)o1.getUnixTime()-(int)(o2.getUnixTime());
                }
            });
            finalList.add(parent);
            for (int j = 0; j < childs.size(); j++) {
                finalList.add(childs.get(j));
            }

        }

        adapter = new CommentListAdapter(DurumDetail.this,finalList);
        comments_lv.setAdapter(adapter);


    }

    public void loadData(){
        SimpleDateFormat sdfIn = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdfOut = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        try{
            Date ddate = sdfIn.parse(durum.getDate());
            date.setText(sdfOut.format(ddate));
            long time =sdfOut.parse(sdfOut.format(ddate).toString()).getTime();
            long now = System.currentTimeMillis();

            CharSequence ago =
                    DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS);
            date.setText(ago);
        }catch (Exception e){
            e.printStackTrace();
        }

        loadDurumDetails(durum);

        content.setText(durum.getContent());

        media.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(durum.getFull_image_url().equals("")){
                    showFullImage(durum.getMedia_url());
                }else  showFullImage(durum.getFull_image_url());
            }
        });


        if(durum.getLikes() > 0){
            likes_holder.setVisibility(View.VISIBLE);
            like_count.setText(""+durum.getLikes());
        }else  likes_holder.setVisibility(View.GONE);



    }

    public void initView(){
        likes_holder = (LinearLayout)findViewById(R.id.durum_like_holder);
        comment_holder = (LinearLayout)findViewById(R.id.durum_comment_holder);
        lc_holder = (LinearLayout)findViewById(R.id.comment_like_holder);
        user = (TextView)findViewById(R.id.user_name);
        username = (TextView)findViewById(R.id.user_username);
        date = (TextView)findViewById(R.id.durum_date);
        content = (TextView)findViewById(R.id.durum_icerik);
        like_count = (TextView)findViewById(R.id.durum_likes);
        comment_count = (TextView)findViewById(R.id.durum_comments);
        user_img = (ImageView) findViewById(R.id.user_img);
        media = (ImageView) findViewById(R.id.durum_media);

        comments_lv = (ListView)findViewById(R.id.comments_lv);
        context = DurumDetail.this;

        inflater = LayoutInflater.from(context);
    }

    public void loadDurumDetails(final Durum durum){

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
                                                    Glide.with(context).load(imgSrc).into(user_img);
                                                }else{
                                                    durum.setAuthor_avatar_url(author_avatar_url);
                                                    Glide.with(context).load(author_avatar_url).into(user_img);
                                                }
                                            }
                                        });

                                    }catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                }
                            }).start();

                            user.setText(author_name);
                            username.setText("@"+author_username);

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
                                JSONObject mediadata = AllData.getJSONObject(0);
                               // media_url = mediadata.getJSONObject("media_details").getJSONObject("sizes").getJSONObject("thumbnail").getString("source_url");
                                String full_media_url = mediadata.getJSONObject("guid").getString("rendered");

                                durum.setFull_image_url(full_media_url);
                                durum.setMedia_url(full_media_url);

                                media.setVisibility(View.VISIBLE);
                                Glide.with(context).load(full_media_url).into(media);

                            }else{
                              //  media_url = "";
                                media.setVisibility(View.GONE);
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
                            int comment_cnt = data.length();


                            if(comment_cnt > 0){
                                comment_holder.setVisibility(View.VISIBLE);
                                comment_count.setText(""+comment_cnt);
                                durum.setComments(comment_cnt);
                            }else  comment_holder.setVisibility(View.GONE);

                            if(comment_cnt == 0 && durum.getLikes() == 0){
                                lc_holder.setVisibility(View.GONE);
                            }else lc_holder.setVisibility(View.VISIBLE);



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
        Glide.with(context).load(img_url).fitCenter().into(image);


        builder.setView(dialogLayout);
        builder.show();

    }

    public String stripHtml(String html) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString();
        } else {
            return Html.fromHtml(html).toString();
        }
    }
}

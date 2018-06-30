package com.teknoseyir.tsapp;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by razor on 2.06.2018.
 */


public class CommentListAdapter extends BaseAdapter {
    private static final String TAG = "Adapter";
    ArrayList<Comment> list;
    Activity context;
    private static LayoutInflater inflater = null;

    public CommentListAdapter(Activity activity, ArrayList<Comment> list){
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

        TextView date,user,username,content,child_spacer;
        ImageView user_img,media;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        Holder holder = new Holder();
        View rowView;
        rowView = inflater.inflate(R.layout.comment_item,null);
        final Comment comment = this.list.get(pos);

        holder.child_spacer = (TextView)rowView.findViewById(R.id.child_spacer);
        holder.user = (TextView)rowView.findViewById(R.id.comment_author_name);
        holder.username = (TextView)rowView.findViewById(R.id.comment_author_username);
        holder.date = (TextView)rowView.findViewById(R.id.comment_time);
        holder.content = (TextView)rowView.findViewById(R.id.comment_content);
        holder.user_img = (ImageView) rowView.findViewById(R.id.comment_author_img);
        holder.media = (ImageView) rowView.findViewById(R.id.comment_media);

        if(comment.getParent() != 0) holder.child_spacer.setVisibility(View.VISIBLE);
        else holder.child_spacer.setVisibility(View.GONE);

        if(comment.getAuthorUsername().equals("") && comment.getAuthorImgURL().equals("")) loadUserDetails(comment,holder);
        else{

            holder.user.setText(comment.getAuthorName());
            holder.username.setText("@"+comment.getAuthorUsername());
            Glide.with(context).load(comment.getAuthorImgURL()).into(holder.user_img);
        }
        holder.date.setText(comment.getDate());

        String content = comment.getContent();

       /* if(content.contains("@")){
            String childUser = "<font color='#337ab7'>@"+content.split("@")[1].split(" ")[0]+"</font>";
            holder.content.setText(Html.fromHtml(content.replace("@"+content.split("@")[1].split(" ")[0],childUser)));
        }else */holder.content.setText(comment.getContent());


        return rowView;
    }

    public void loadUserDetails(final Comment comment,final Holder holder){

        String url = context.getResources().getString(R.string.USER_URL)+comment.getAuthorId();


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
                                                    comment.setAuthorImgURL(imgSrc);
                                                    Glide.with(context).load(imgSrc).into(holder.user_img);
                                                }else{
                                                    comment.setAuthorImgURL(author_avatar_url);
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

                            comment.setAuthorName(author_name);
                            comment.setAuthorUsername(author_username);


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

}
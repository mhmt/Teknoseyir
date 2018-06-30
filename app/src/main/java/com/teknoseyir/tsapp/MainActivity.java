package com.teknoseyir.tsapp;

import android.content.Intent;
import android.graphics.Color;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG ="MainActivity";
    private String durum_url,user_url,media_url,comment_url;
    private Toolbar toolbar;
    private ListView durumLV ;
    private ArrayList<Durum> durumList;
    private DurumListAdapter adapter;
    private SwipeRefreshLayout swipeLayout;
    private Button btnLoadMore;
    private int durumPage = 1;
    final  ArrayList<Durum> durums = new ArrayList<>();
    private boolean flag_loading =false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Akış");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        swipeLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_container);

        swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        durum_url = getResources().getString(R.string.DURUM_URL);
        user_url = getResources().getString(R.string.USER_URL);
        media_url = getResources().getString(R.string.MEDIA_URL);
        comment_url = getResources().getString(R.string.COMMENT_URL);

        durumLV = (ListView)findViewById(R.id.durum_lv);
        durumList = new ArrayList<>();

        LoadDurumList(1);

        btnLoadMore = new Button(this);
        btnLoadMore.setBackground(getResources().getDrawable(R.drawable.rectangel_red));
        btnLoadMore.setText("Daha fazla yükle");
        btnLoadMore.setTextColor(getResources().getColor(R.color.white));
        btnLoadMore.setPadding(10,10,10,10);
        btnLoadMore.setWidth(200);
        btnLoadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoadMoreDurumList(++durumPage);

            }
        });

        durumLV.addFooterView(btnLoadMore);



        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                durumList.clear();
                durumPage = 1;
                LoadDurumList(durumPage);
            }
        });

        durumLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent detail = new Intent(MainActivity.this,DurumDetail.class);
                Durum durum = durumList.get(position);

                Gson gson = new Gson();
                try{
                    JSONObject durum_json = new JSONObject(gson.toJson(durum));
                    Log.d(TAG, "onItemClick: DurumJSON: "+durum_json);


                    detail.putExtra("durumJSON",durum_json.toString());
                    startActivity(detail);
                }catch (Exception e){
                    e.printStackTrace();
                }



            }
        });


    }


    public void LoadDurumList(int page ){

            RequestQueue queue = Volley.newRequestQueue(this);
            StringRequest stringRequest = new StringRequest(Request.Method.GET, durum_url+"?page="+page,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d(TAG, "onResponse: "+response);
                            try{
                               JSONArray data = new JSONArray(response);
                               durumList = parseDurumList(data);
                               adapter = new DurumListAdapter(MainActivity.this,durumList);
                               durumLV.setAdapter(adapter);

                                btnLoadMore.setVisibility(View.VISIBLE);
                                swipeLayout.setRefreshing(false);
                                flag_loading = false;

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

    public void LoadMoreDurumList(int page ){

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, durum_url+"?page="+page,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "onResponse: "+response);
                        try{
                            JSONArray data = new JSONArray(response);
                            durumList = parseDurumList(data);
                            adapter.notifyDataSetChanged();
                        //    durumLV.smoothScrollToPosition((10*(durumPage-1))+1);
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

    public ArrayList<Durum> parseDurumList(final JSONArray durumdata){
        btnLoadMore.setVisibility(View.GONE);
        swipeLayout.setRefreshing(true);
        flag_loading = true;

        if(durumdata != null){
            for (int i = 0; i < durumdata.length() ; i++) {
                btnLoadMore.setVisibility(View.GONE);
                swipeLayout.setRefreshing(true);
                flag_loading = true;
                try{
                    JSONObject each = durumdata.getJSONObject(i);
                    final int durum_id = each.getInt("id");
                    final String date = each.getString("date").replace("T"," ");
                    final  String content = stripHtml(each.getJSONObject("content").getString("rendered"));
                    final  int author_id = each.getInt("author");
                    final int likes = each.getJSONArray("begenenler").length();


                    Durum durum = new Durum(getApplicationContext(),durum_id,author_id,date,content,"","","","",likes,0);
                    durums.add(durum);

                    Collections.sort(durums, new Comparator<Durum>() {
                        public int compare(Durum o1, Durum o2) {
                            if (o1.getId() == 0 || o2.getId() == 0)
                                return 0;
                            return o2.getId()-(o1.getId());
                        }
                    });


                    btnLoadMore.setVisibility(View.VISIBLE);
                    swipeLayout.setRefreshing(false);
                    flag_loading = false;
                    adapter.notifyDataSetChanged();



                }catch (Exception e){

                }

            }

            return durums;
        }else return null;
    }

    public String stripHtml(String html) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString();
        } else {
            return Html.fromHtml(html).toString();
        }
    }
}

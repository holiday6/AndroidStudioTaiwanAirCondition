package com.example.taiwanaircondition;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    final String airConditionUrl = "https://data.epa.gov.tw/api/v1/aqx_p_02?limit=1000&api_key=9be7b239-557b-4c10-9775-78cadfc555e9&sort=ImportDate%20desc&format=json";
    // 存放各縣市的PM資料 key = country
    Map countryMap = new HashMap<String, ArrayList<MetaData>>();
    String countryName = "新北市";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cacheData();
    }

    private void cacheData() {
        ProgressDialog dialog = ProgressDialog.show(this, getString(R.string.wait_dialog_title), getString(R.string.wait_dialog_msg), true);
        new Thread(()->{
            try {
                URL url = new URL(airConditionUrl);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                InputStream is = connection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line = br.readLine();
                StringBuffer json = new StringBuffer();
                while(line != null) {
                    json.append(line);
                    line = br.readLine();
                }

                JSONObject jsonObjectAll = new JSONObject(String.valueOf(json));
                JSONArray jsonArray = jsonObjectAll.getJSONArray("records");
                JSONObject siteJsonObject;
                for(int i=0;i<jsonArray.length();i++) {
                    siteJsonObject = jsonArray.getJSONObject(i);
                    String county = siteJsonObject.getString("county");
                    if(!countryMap.containsKey(county)) {
                        countryMap.put(siteJsonObject.getString("county"), new ArrayList<MetaData>());
                    }
                    ArrayList list = (ArrayList) countryMap.get(county);
                    list.add(new MetaData(siteJsonObject.getString("Site"),siteJsonObject.getInt("PM25")));
                }

                runOnUiThread(()->{
                    dialog.dismiss();

                    RecyclerView recyclerView = findViewById(R.id.recyclerView);
                    MyAdapter adapter = new MyAdapter();

                    recyclerView.setLayoutManager(new LinearLayoutManager(this));
                    recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
                    recyclerView.setAdapter(adapter);
                });

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }).start();


    }

    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

        ArrayList<MetaData> siteList = (ArrayList) countryMap.get(countryName);;

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvSite, tvPm;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);

                tvSite = itemView.findViewById(R.id.site_view_text);
                tvPm = itemView.findViewById(R.id.pm_view_text);
            }
        }
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.tvSite.setText(getString(R.string.site_textview, siteList.get(position).site));
            int pm = siteList.get(position).pm;
            holder.tvPm.setText(getString(R.string.pm_textview, pm));
            int color;
            if(pm < 12) {
                color = R.color.green_0;
            }else if(pm < 23) {
                color = R.color.green_1;
            }else if(pm < 36) {
                color = R.color.green_2;
            }else if(pm < 42) {
                color = R.color.yellow_0;
            }else if(pm < 48) {
                color = R.color.yellow_1;
            }else if(pm < 54) {
                color = R.color.yellow_2;
            }else if(pm < 59) {
                color = R.color.red_0;
            }else if(pm < 65) {
                color = R.color.red_1;
            }else if(pm < 71) {
                color = R.color.red_2;
            }else {
                color = R.color.purple_700;
            }
            holder.itemView.setBackgroundColor(getResources().getColor(color));
        }

        @Override
        public int getItemCount() {
            return siteList.size();
        }
    }

    private class MetaData {
        public String site;
        public int pm;
        public MetaData(String site, int pm) {
            this.site = site;
            this.pm = pm;
        };
    }
}
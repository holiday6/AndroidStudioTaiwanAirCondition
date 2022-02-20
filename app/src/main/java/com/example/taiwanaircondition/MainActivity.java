package com.example.taiwanaircondition;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

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
    }

    private void cacheData() {
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
                for(int i=0;i<jsonArray.length();i++) {

                }

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

        ArrayList<MetaData> siteList;

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
            siteList = (ArrayList) countryMap.get(countryName);
            holder.tvSite.setText(getString(R.string.site_textview, siteList.get(position).site));
            holder.tvPm.setText(getString(R.string.pm_textview, siteList.get(position).pm));
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
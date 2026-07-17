package com.example.shoeta;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class SavedJobsActivity extends AppCompatActivity {

    private static final String BASE_URL = "https://blood-bridge.org/shoeTA/";
    private static final String PREF_NAME = "SAVED_JOBS";
    private static final String PREF_KEY  = "jobs";
    private RecyclerView recycler;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;
    private final List<JSONObject> jobList = new ArrayList<>();
    private JobAdapter adapter;
    private SessionManager sm;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_jobs);

        sm    = new SessionManager(this);
        queue = Volley.newRequestQueue(this);

        recycler    = findViewById(R.id.recyclerSavedJobs);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new JobAdapter(jobList, sm, this);
        adapter.setOnUnsaveListener(jobId -> {
            for (int i = 0; i < jobList.size(); i++) {
                if (jobList.get(i).optInt("id") == jobId) {
                    jobList.remove(i);
                    adapter.notifyItemRemoved(i);
                    break;
                }
            }
            layoutEmpty.setVisibility(jobList.isEmpty() ? View.VISIBLE : View.GONE);
        });

        recycler.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        loadSavedJobs();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSavedJobs();
    }

    private void loadSavedJobs() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        jobList.clear();
        try {
            JSONArray arr = new JSONArray(prefs.getString(PREF_KEY, "[]"));
            for (int i = 0; i < arr.length(); i++) jobList.add(arr.getJSONObject(i));
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        adapter.notifyDataSetChanged();
        layoutEmpty.setVisibility(jobList.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
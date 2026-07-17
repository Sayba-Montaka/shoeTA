package com.example.shoeta;

import android.content.Intent;
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
import java.util.*;

public class JobListActivity extends AppCompatActivity {

    private static final String BASE_URL = "https://blood-bridge.org/shoeTA/";

    private RecyclerView recycler;
    private ProgressBar  progressBar;
    private TextView     tvJobCount;
    private EditText     etFilterLocation;
    private Spinner      spinnerFilterTitle, spinnerFilterShift;

    private final List<JSONObject> jobList    = new ArrayList<>();
    private final List<String>  titleNames  = new ArrayList<>();
    private final List<Integer> titleIds    = new ArrayList<>();
    private final List<String>  shiftNames  = new ArrayList<>();
    private final List<Integer> shiftIds    = new ArrayList<>();

    private SessionManager sm;
    private JobAdapter adapter;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_list);

        sm    = new SessionManager(this);
        queue = Volley.newRequestQueue(this);

        recycler        = findViewById(R.id.recyclerJobs);
        progressBar     = findViewById(R.id.progressBar);
        tvJobCount      = findViewById(R.id.tvJobCount);
        etFilterLocation= findViewById(R.id.etFilterLocation);
        spinnerFilterTitle = findViewById(R.id.spinnerFilterTitle);
        spinnerFilterShift = findViewById(R.id.spinnerFilterShift);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new JobAdapter(jobList, sm, this);
        recycler.setAdapter(adapter);

        findViewById(R.id.btnSearch).setOnClickListener(v -> searchJobs());

        loadDropdowns();
        searchJobs();
    }

    private void loadDropdowns() {
        String url = BASE_URL + "admin_settings.php?action=get_dropdowns";

        StringRequest req = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            JSONArray titles = obj.getJSONArray("job_titles");
                            JSONArray shifts  = obj.getJSONArray("shifts");

                            titleNames.add("সব পদ / All Titles"); titleIds.add(0);
                            for (int i = 0; i < titles.length(); i++) {
                                JSONObject t = titles.getJSONObject(i);
                                titleNames.add(t.getString("name")); titleIds.add(t.getInt("id"));
                            }
                            ArrayAdapter<String> ta = new ArrayAdapter<>(JobListActivity.this,
                                    android.R.layout.simple_spinner_item, titleNames);
                            ta.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerFilterTitle.setAdapter(ta);

                            shiftNames.add("সব শিফট / All Shifts"); shiftIds.add(0);
                            for (int i = 0; i < shifts.length(); i++) {
                                JSONObject s = shifts.getJSONObject(i);
                                shiftNames.add(s.getString("name")); shiftIds.add(s.getInt("id"));
                            }
                            ArrayAdapter<String> sa = new ArrayAdapter<>(JobListActivity.this,
                                    android.R.layout.simple_spinner_item, shiftNames);
                            sa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerFilterShift.setAdapter(sa);

                        } catch (Exception ignored) {}
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {}
                });
        queue.add(req);
    }

    private void searchJobs() {
        progressBar.setVisibility(View.VISIBLE);
        String loc      = etFilterLocation.getText().toString().trim();
        int titleId     = titleIds.isEmpty() ? 0 : titleIds.get(spinnerFilterTitle.getSelectedItemPosition());
        int shiftId     = shiftIds.isEmpty()  ? 0 : shiftIds.get(spinnerFilterShift.getSelectedItemPosition());

        StringBuilder url = new StringBuilder(BASE_URL + "get_jobs.php?limit=50");
        if (!loc.isEmpty())  url.append("&location=").append(android.net.Uri.encode(loc));
        if (titleId > 0)     url.append("&job_title_id=").append(titleId);
        if (shiftId > 0)     url.append("&shift_id=").append(shiftId);
        // factory filter for factory users
        if (sm.isFactory())  url.append("&factory_id=").append(sm.getUserId());

        StringRequest req = new StringRequest(Request.Method.GET, url.toString(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressBar.setVisibility(View.GONE);
                        try {
                            JSONObject obj = new JSONObject(response);
                            JSONArray  arr = obj.getJSONArray("jobs");
                            jobList.clear();
                            for (int i = 0; i < arr.length(); i++) jobList.add(arr.getJSONObject(i));
                            adapter.notifyDataSetChanged();
                            tvJobCount.setText("চাকরি পাওয়া গেছে: " + arr.length());
                        } catch (Exception e) {
                            Toast.makeText(JobListActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(JobListActivity.this, "নেটওয়ার্ক সমস্যা", Toast.LENGTH_SHORT).show();
                    }
                });
        queue.add(req);
    }
}

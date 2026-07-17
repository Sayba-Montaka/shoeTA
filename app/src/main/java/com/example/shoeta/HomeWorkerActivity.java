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
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class HomeWorkerActivity extends AppCompatActivity {

    private static final String BASE_URL = "https://blood-bridge.org/shoeTA/";

    private SessionManager sm;
    private RecyclerView recycler;
    private final List<JSONObject> jobList = new ArrayList<>();
    private JobAdapter adapter;
    private RequestQueue queue;
    FrameLayout ad_view_container;
    AdView adView;
    private final List<JSONObject> allJobs = new ArrayList<>();
    TextView chipAll,chipFullTime,chipPartTime,chipNightShift,chipDayShift;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_worker);

        sm    = new SessionManager(this);
        queue = Volley.newRequestQueue(this);
        ad_view_container = findViewById(R.id.ad_view_container);

        TextView tvName = findViewById(R.id.tvWorkerName);
        tvName.setText(sm.getUserName());

        findViewById(R.id.navProfile).setOnClickListener(v ->
                startActivity(new Intent(this, WorkerProfileActivity.class)));

       chipAll = findViewById(R.id.chip_all);
       chipFullTime = findViewById(R.id.full_time);
       chipPartTime = findViewById(R.id.part_time);
       chipDayShift = findViewById(R.id.day_shift);
       chipNightShift = findViewById(R.id.night_shift);
       chipAll.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               applyFilter(null,chipAll);
           }
       });
       chipFullTime.setOnClickListener(view -> applyFilter("Full Time",chipFullTime));
       chipPartTime.setOnClickListener(view -> applyFilter("Part Time",chipPartTime));
       chipDayShift.setOnClickListener(view -> applyFilter("Day",chipDayShift));
       chipNightShift.setOnClickListener(view -> applyFilter("Night",chipNightShift));

        recycler = findViewById(R.id.recyclerFeaturedJobs);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new JobAdapter(jobList, sm, this);
        recycler.setAdapter(adapter);

        // Search bar → opens JobListActivity
        findViewById(R.id.tvSearchBar).setOnClickListener(v ->
                startActivity(new Intent(this, JobListActivity.class)));

        setupBottomNav();
        loadFeaturedJobs();
        loadWorkerProfile();

//-------------------------ad----------------------------------------------------
        new Thread(
                () -> {
                    // Initialize the Google Mobile Ads SDK on a background thread.
                    MobileAds.initialize(this, initializationStatus -> {});
                })
                .start();

// Create a new ad view.
        adView = new AdView(this);
        adView.setAdUnitId(getString(R.string.banner_ad_id));
// Request a large anchored adaptive banner with a width of 360.
        adView.setAdSize(AdSize.BANNER);

// Replace ad container with new ad view.
        ad_view_container.removeAllViews();
        ad_view_container.addView(adView);
        com.google.android.gms.ads.AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }
//-----------------------------------ad------------------------------------------------------
    @Override
    protected void onResume() {
        super.onResume();
        loadFeaturedJobs();
    }

    private void loadFeaturedJobs() {
        String url = BASE_URL + "get_jobs.php?limit=10";

        StringRequest req = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            JSONArray arr  = obj.getJSONArray("jobs");
                            allJobs.clear();
                            for (int i = 0; i < arr.length(); i++) {
                                allJobs.add(arr.getJSONObject(i));
                            }
                            jobList.clear();
                            jobList.addAll(allJobs);
                            adapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            Toast.makeText(HomeWorkerActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(HomeWorkerActivity.this, "নেটওয়ার্ক সমস্যা", Toast.LENGTH_SHORT).show();
                    }
                });
        req.setShouldCache(false);
        queue.add(req);
    }






    private void setupBottomNav() {
        findViewById(R.id.navHome).setOnClickListener(v -> {});
        findViewById(R.id.navJobs).setOnClickListener(v ->
                startActivity(new Intent(this, JobListActivity.class)));
        findViewById(R.id.navApplied).setOnClickListener(v ->
                startActivity(new Intent(this, MyApplicationsActivity.class)));
        findViewById(R.id.navSaved).setOnClickListener(v ->
                startActivity(new Intent(this, SavedJobsActivity.class)));
        findViewById(R.id.navNotification).setOnClickListener(v ->
                startActivity(new Intent(this, NotificationActivity.class)));
    }

    private void loadWorkerProfile() {
        String url = BASE_URL + "get_worker_profile.php?worker_id=" + sm.getUserId();

        StringRequest req = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject root = new JSONObject(response);
                        JSONObject worker = root.getJSONObject("worker"); 

                        ImageView navProfile = findViewById(R.id.navProfile);
                        TextView navProfileEmoji = findViewById(R.id.navProfileEmoji);

                        String photo = worker.optString("photo", "");
                        if (!photo.isEmpty()) {
                            String photoUrl = BASE_URL + "uploads/workers/" + photo;
                            Glide.with(this)
                                    .load(photoUrl)
                                    .placeholder(R.drawable.bg_avatar)
                                    .into(navProfile);
                            navProfileEmoji.setVisibility(View.GONE);
                        } else {
                            navProfileEmoji.setVisibility(View.VISIBLE);
                        }
                    } catch (Exception e) {
                        // Profile fetch failed to parse — just leave the emoji fallback showing
                    }
                },
                error -> {
                    // Network/profile error — emoji fallback stays visible, no need to block the UI
                });
        queue.add(req);
    }
    private void applyFilter(String filterText, TextView selectedChip) {
        // Reset all chips to idle style
        for (TextView chip : new TextView[]{chipAll, chipFullTime, chipPartTime, chipNightShift,chipDayShift}) {
            chip.setBackgroundResource(R.drawable.bg_chip_idle);
            chip.setTextColor(getResources().getColor(R.color.chip_idle_text));
        }
        // Highlight the selected chip
        selectedChip.setBackgroundResource(R.drawable.bg_chip_active);
        selectedChip.setTextColor(getResources().getColor(R.color.chip_active_text));

        jobList.clear();
        if (filterText == null) {
            jobList.addAll(allJobs);
        } else {
            for (JSONObject job : allJobs) {
                String jobType = job.optString("job_type", "");
                String shift   = job.optString("shift", "");
                if (filterText.equalsIgnoreCase(jobType) || filterText.equalsIgnoreCase(shift)) {
                    jobList.add(job);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
}

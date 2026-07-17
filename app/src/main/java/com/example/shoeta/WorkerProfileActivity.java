package com.example.shoeta;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

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

import org.json.JSONObject;

public class WorkerProfileActivity extends AppCompatActivity {

    private static final String BASE_URL = "https://blood-bridge.org/shoeTA/";

    private SessionManager sm;
    private RequestQueue queue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_profile);

        sm    = new SessionManager(this);
        queue = Volley.newRequestQueue(this);



        loadProfile();

        // Bottom nav
        findViewById(R.id.navHome).setOnClickListener(v ->
                startActivity(new Intent(this, HomeWorkerActivity.class)));
        findViewById(R.id.navJobs).setOnClickListener(v ->
                startActivity(new Intent(this, JobListActivity.class)));
        findViewById(R.id.navApplied).setOnClickListener(v ->
                startActivity(new Intent(this, MyApplicationsActivity.class)));
        findViewById(R.id.navSaved).setOnClickListener(v ->
                startActivity(new Intent(this,SavedJobsActivity.class)));

        // Logout
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            sm.logout();
            Intent i = new Intent(this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });
        // Populate from session immediately, then refine from API
        ((TextView) findViewById(R.id.tvProfileName)).setText(sm.getUserName());
        ((TextView) findViewById(R.id.tvProfilePhone)).setText(sm.getUserPhone());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfile();
    }

    private void loadProfile() {
        String url = BASE_URL + "get_worker_profile.php?worker_id=" + sm.getUserId();

        StringRequest req = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);

                            ImageView navProfile = findViewById(R.id.navProfile);
                            TextView navProfileEmoji = findViewById(R.id.navProfileEmoji);

                            if (obj.getBoolean("success")) {
                                JSONObject w = obj.getJSONObject("worker");
                                ((TextView) findViewById(R.id.tvProfileLocation))
                                        .setText(w.optString("location", "—"));
                                ((TextView) findViewById(R.id.tvProfileSkill))
                                        .setText(w.optString("skill", "—"));
                                ((TextView) findViewById(R.id.tvProfileSalary))
                                        .setText("৳" + w.optString("expected_salary", "—"));
                                ((TextView) findViewById(R.id.tvProfileExp))
                                        .setText(w.optString("experience_years", "0"));
                                ((TextView) findViewById(R.id.tvProfileAge))
                                        .setText(w.optString("age", "—"));

                                TextView tvApplied = findViewById(R.id.tvProfileApplied);
                                if (tvApplied != null) {
                                    tvApplied.setText(String.valueOf(w.optInt("applied_count", 0)));
                                }
                                String photo = w.optString("photo", "");
                                if (!photo.isEmpty()) {
                                    String photoUrl = BASE_URL + "uploads/workers/" + photo;
                                    Glide.with(WorkerProfileActivity.this)
                                            .load(photoUrl)
                                            .placeholder(R.drawable.bg_avatar)
                                            .into(navProfile);
                                    navProfileEmoji.setVisibility(View.GONE);
                                } else {
                                    navProfileEmoji.setVisibility(View.VISIBLE);
                                }
                            }
                        } catch (Exception ignored) {}
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(WorkerProfileActivity.this, "নেটওয়ার্ক সমস্যা", Toast.LENGTH_SHORT).show();
                    }
                });
        queue.add(req);
    }
}

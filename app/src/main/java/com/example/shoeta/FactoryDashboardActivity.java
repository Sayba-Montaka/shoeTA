package com.example.shoeta;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;

import org.json.JSONObject;

public class FactoryDashboardActivity extends AppCompatActivity {

    private static final String BASE_URL = "https://blood-bridge.org/shoeTA/";

    private TextView tvMyJobCount, tvMyAppCount;

    private SessionManager sm;
    private RequestQueue queue;
    FrameLayout ad_view_container;
    AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_factory_dashboard);

        sm    = new SessionManager(this);
        queue = Volley.newRequestQueue(this);
        ad_view_container = findViewById(R.id.ad_view_container);

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
        adView.setAdSize(AdSize.BANNER); // 320x50, standard
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                Log.d("ADMOB", "Ad loaded successfully");
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                Log.e("ADMOB", "Ad failed to load: " + adError.getMessage()
                        + " | code=" + adError.getCode());
            }
        });

// Replace ad container with new ad view.
        ad_view_container.removeAllViews();
        ad_view_container.addView(adView);
        com.google.android.gms.ads.AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        ((TextView) findViewById(R.id.tvCompanyName)).setText(sm.getUserName());
        tvMyJobCount = findViewById(R.id.tvMyJobCount);
        tvMyAppCount = findViewById(R.id.tvMyAppCount);

        findViewById(R.id.factoryProfile).setOnClickListener(v ->
                startActivity(new Intent(this,FactoryProfile.class)));

        findViewById(R.id.btnPostJob).setOnClickListener(v ->
                startActivity(new Intent(this, PostJobActivity.class)));

        findViewById(R.id.btnPrivacy).setOnClickListener(view ->
                startActivity(new Intent(this,PolicyTerms.class)));

        findViewById(R.id.btnMyJobs).setOnClickListener(v -> {
            Intent i = new Intent(this, JobListActivity.class);
            i.putExtra("factory_id", sm.getUserId());
            startActivity(i);
        });

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            sm.logout();
            Intent i = new Intent(this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });

        loadMiniStats();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMiniStats();
    }

    private void loadMiniStats() {
        String jobsUrl = BASE_URL + "get_jobs.php?factory_id=" + sm.getUserId() + "&limit=1";

        StringRequest jobsReq = new StringRequest(Request.Method.GET, jobsUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            tvMyJobCount.setText(String.valueOf(new JSONObject(response).getInt("total")));
                        } catch (Exception ignored) {}
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {}
                });
        queue.add(jobsReq);

        String appsUrl = BASE_URL + "factory_applications.php?factory_id=" + sm.getUserId();

        StringRequest appsReq = new StringRequest(Request.Method.GET, appsUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            tvMyAppCount.setText(String.valueOf(new JSONObject(response).getInt("total")));
                        } catch (Exception ignored) {}
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {}
                });
        queue.add(appsReq);
    }


}

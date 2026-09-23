package com.example.shoeta;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class JobDetailActivity extends AppCompatActivity {

    private static final String BASE_URL = "https://blood-bridge.org/shoeTA/";

    private SessionManager sm;
    private RequestQueue queue;
    private int jobId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_detail);

        sm    = new SessionManager(this);
        queue = Volley.newRequestQueue(this);

        String jobJson = getIntent().getStringExtra("job_json");
        if (jobJson == null) { finish(); return; }

        try {
            JSONObject j = new JSONObject(jobJson);
            jobId = j.optInt("id");

            ((TextView) findViewById(R.id.tvDetailJobTitle)).setText(j.optString("job_title", "—"));
            ((TextView) findViewById(R.id.tvDetailCompany)).setText(j.optString("company_name", "—"));
            ((TextView) findViewById(R.id.tvDetailSalary)).setText("৳" + j.optString("salary_rate", "N/A"));
            ((TextView) findViewById(R.id.tvDetailJobType)).setText(j.optString("job_type", "—"));
            ((TextView) findViewById(R.id.tvDetailLocation)).setText(j.optString("location", "—"));
            ((TextView) findViewById(R.id.tvDetailShift)).setText(j.optString("shift", "—") + " Shift");
            ((TextView) findViewById(R.id.tvDetailGender)).setText(j.optString("gender_required", "Both"));
            ((TextView) findViewById(R.id.tvDetailFood)).setText(
                    j.optInt("food_accommodation") == 1 ? "• 🍽 খাবার : আছে" : "• 🍽 খাবার : নেই");
            ((TextView) findViewById(R.id.tvDetailShelter)).setText(
                    j.optInt("shelter") == 1 ? "• \uD83C\uDFE0আবাসন : আছে" : "• \uD83C\uDFE0আবাসন : নেই");
            ((TextView) findViewById(R.id.tvDetailPayment)).setText(
                    "• বেতন পদ্ধতি: " + j.optString("payment_system", "—"));
            ((TextView) findViewById(R.id.tvDetailWorkers)).setText(
                    "• প্রয়োজনীয় কর্মী: " + j.optInt("workers_needed", 1) + " জন");
            ((TextView) findViewById(R.id.tvDetailOvertime)).setText(
                    "•  ⏰ওভারটাইম: " + (j.optInt("overtime") == 1 ? "আছে" : "নেই"));
            ((TextView) findViewById(R.id.tvDetailPhone)).setText(
                    j.optString("contact_phone", "—"));

        } catch (Exception e) {
            Toast.makeText(this, "Error loading job", Toast.LENGTH_SHORT).show();
            finish(); return;
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        Button btnApply = findViewById(R.id.btnApplyNow);
        Button btnSave  = findViewById(R.id.btnSaveJob);

        if (sm.isWorker()) {
            btnApply.setOnClickListener(v -> applyNow(btnApply));
        } else {
            btnApply.setEnabled(false);
            btnApply.setText("Worker account required");
        }
    }

    private void applyNow(Button btn) {
        btn.setEnabled(false);
        btn.setText("আবেদন হচ্ছে...");

        String url = BASE_URL + "apply.php";

        StringRequest req = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (obj.getBoolean("success")) {
                                btn.setText("✓ আবেদন সম্পন্ন!");
                                Toast.makeText(JobDetailActivity.this, "আবেদন সফল হয়েছে!", Toast.LENGTH_LONG).show();
                            } else {
                                btn.setEnabled(true);
                                btn.setText("আবেদন করুন / Apply Now");
                                Toast.makeText(JobDetailActivity.this, obj.getString("message"), Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            btn.setEnabled(true);
                            btn.setText("আবেদন করুন / Apply Now");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        btn.setEnabled(true);
                        btn.setText("আবেদন করুন / Apply Now");
                        Toast.makeText(JobDetailActivity.this, "নেটওয়ার্ক সমস্যা", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("worker_id", String.valueOf(sm.getUserId()));
                params.put("job_id",    String.valueOf(jobId));
                return params;
            }
        };
        queue.add(req);
    }
}

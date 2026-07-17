package com.example.shoeta;

import android.os.Bundle;
import android.util.Log;
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

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.*;

public class PostJobActivity extends AppCompatActivity {

    private static final String BASE_URL = "https://blood-bridge.org/shoeTA/";

    private Spinner  spinnerJobTitle, spinnerShift, spinnerJobType, spinnerPayment, spinnerGender;
    private EditText etWorkersNeeded, etSalaryRate, etContactPhone;
    private CheckBox cbFood, cbOvertime,cbSit;
    private ProgressBar progressBar;
    private RequestQueue queue;

    private final List<String>  titleNames  = new ArrayList<>();
    private final List<Integer> titleIds    = new ArrayList<>();
    private final List<String>  shiftNames  = new ArrayList<>();
    private final List<Integer> shiftIds    = new ArrayList<>();
    private final List<String>  typeNames   = new ArrayList<>();
    private final List<Integer> typeIds     = new ArrayList<>();
    private final List<String>  payNames    = new ArrayList<>();
    private final List<Integer> payIds      = new ArrayList<>();
    private final List<String>  genderOpts  = Arrays.asList("Both", "Male", "Female");

    private SessionManager sm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_job);

        sm    = new SessionManager(this);
        queue = Volley.newRequestQueue(this);

        spinnerJobTitle  = findViewById(R.id.spinnerJobTitle);
        spinnerShift     = findViewById(R.id.spinnerShift);
        spinnerJobType   = findViewById(R.id.spinnerJobType);
        spinnerPayment   = findViewById(R.id.spinnerPayment);
        spinnerGender    = findViewById(R.id.spinnerGender);
        etWorkersNeeded  = findViewById(R.id.etWorkersNeeded);
        etSalaryRate     = findViewById(R.id.etSalaryRate);
        etContactPhone   = findViewById(R.id.etContactPhone);
        cbFood           = findViewById(R.id.cbFood);
        cbSit           = findViewById(R.id.cbSit);
        cbOvertime       = findViewById(R.id.cbOvertime);
        progressBar      = findViewById(R.id.progressBar);

        etContactPhone.setText(sm.getUserPhone());

        ArrayAdapter<String> ga = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, genderOpts);
        ga.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(ga);

        loadDropdowns();
        findViewById(R.id.btnPostJob).setOnClickListener(v -> doPostJob());
    }

    private void loadDropdowns() {
        String url = BASE_URL + "admin_settings.php?action=get_dropdowns";

        StringRequest req = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            populate(obj.getJSONArray("job_titles"),      titleNames, titleIds, spinnerJobTitle);
                            populate(obj.getJSONArray("shifts"),          shiftNames, shiftIds,  spinnerShift);
                            populate(obj.getJSONArray("job_types"),       typeNames,  typeIds,   spinnerJobType);
                            populate(obj.getJSONArray("payment_systems"), payNames,   payIds,    spinnerPayment);
                        } catch (Exception ignored) {}
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(PostJobActivity.this, "নেটওয়ার্ক সমস্যা", Toast.LENGTH_SHORT).show();
                    }
                });
        queue.add(req);
    }

    private void populate(JSONArray arr, List<String> names, List<Integer> ids, Spinner s) throws Exception {
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            names.add(o.getString("name")); ids.add(o.getInt("id"));
        }
        ArrayAdapter<String> a = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(a);
    }

    private void doPostJob() {
        if (titleIds.isEmpty()) {
            Toast.makeText(this, "ড্রপডাউন লোড হয়নি, অনুগ্রহ করে আবার চেষ্টা করুন", Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);

        String url = BASE_URL + "post_job.php";

        StringRequest req = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressBar.setVisibility(View.GONE);
                        Log.d("RES_POST_JOB", response);
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (obj.getBoolean("success")) {
                                Toast.makeText(PostJobActivity.this, "✓ চাকরি পোস্ট হয়েছে!", Toast.LENGTH_LONG).show();
                                finish();
                            } else {
                                Toast.makeText(PostJobActivity.this, obj.getString("message"), Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(PostJobActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE);
                        Log.d("RES_POST_JOB", String.valueOf(error));
                        Toast.makeText(PostJobActivity.this, "নেটওয়ার্ক সমস্যা", Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> p = new HashMap<>();
                p.put("factory_id",        String.valueOf(sm.getUserId()));
                p.put("job_title_id",      String.valueOf(titleIds.get(spinnerJobTitle.getSelectedItemPosition())));
                p.put("shift_id",          String.valueOf(shiftIds.get(spinnerShift.getSelectedItemPosition())));
                p.put("job_type_id",       String.valueOf(typeIds.get(spinnerJobType.getSelectedItemPosition())));
                p.put("payment_system_id", String.valueOf(payIds.get(spinnerPayment.getSelectedItemPosition())));
                p.put("workers_needed",    etWorkersNeeded.getText().toString().trim());
                p.put("salary_rate",       etSalaryRate.getText().toString().trim());
                p.put("gender_required",   genderOpts.get(spinnerGender.getSelectedItemPosition()));
                p.put("contact_phone",     etContactPhone.getText().toString().trim());
                p.put("food_accommodation", cbFood.isChecked() ? "1" : "0");
                p.put("shelter",            cbSit.isChecked() ? "1" : "0");
                p.put("overtime",           cbOvertime.isChecked() ? "1" : "0");
                return p;
            }
        };
        queue.add(req);
    }
}

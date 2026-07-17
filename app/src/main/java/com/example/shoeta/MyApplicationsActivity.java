package com.example.shoeta;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
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

public class MyApplicationsActivity extends AppCompatActivity {

    private static final String BASE_URL = "https://blood-bridge.org/shoeTA/";

    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;
    private RecyclerView recycler;
    private final List<JSONObject> apps = new ArrayList<>();
    private AppAdapter adapter;
    private SessionManager sm;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_applications);

        sm    = new SessionManager(this);
        queue = Volley.newRequestQueue(this);

        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        recycler    = findViewById(R.id.recyclerApplications);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppAdapter(apps);
        recycler.setAdapter(adapter);

        // Bottom nav
        findViewById(R.id.navHome).setOnClickListener(v ->
                startActivity(new Intent(this, HomeWorkerActivity.class)));
        findViewById(R.id.navJobs).setOnClickListener(v ->
                startActivity(new Intent(this, JobListActivity.class)));
        findViewById(R.id.navApplied).setOnClickListener(v -> {});
        findViewById(R.id.navSaved).setOnClickListener(v ->
                startActivity(new Intent(this, SavedJobsActivity.class)));
        findViewById(R.id.navNotification).setOnClickListener(v ->
                startActivity(new Intent(this, NotificationActivity.class)));

        loadApplications();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadApplications();
    }

    private void loadApplications() {
        progressBar.setVisibility(View.VISIBLE);
        String url = BASE_URL + "apply.php?worker_id=" + sm.getUserId();

        StringRequest req = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressBar.setVisibility(View.GONE);
                        try {
                            JSONObject obj = new JSONObject(response);
                            JSONArray  arr = obj.getJSONArray("applications");
                            apps.clear();
                            for (int i = 0; i < arr.length(); i++) apps.add(arr.getJSONObject(i));
                            adapter.notifyDataSetChanged();
                            layoutEmpty.setVisibility(apps.isEmpty() ? View.VISIBLE : View.GONE);
                        } catch (Exception e) {
                            Toast.makeText(MyApplicationsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(MyApplicationsActivity.this, "নেটওয়ার্ক সমস্যা", Toast.LENGTH_SHORT).show();
                    }
                });
        queue.add(req);
    }

    // ── Inner adapter ──────────────────────────────────────
    static class AppAdapter extends RecyclerView.Adapter<AppAdapter.VH> {
        private final List<JSONObject> items;
        AppAdapter(List<JSONObject> items) { this.items = items; }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(LayoutInflater.from(p.getContext())
                    .inflate(R.layout.item_application, p, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            JSONObject a = items.get(pos);
            try {
                h.tvJobTitle.setText(a.optString("job_title", "—"));
                h.tvCompany.setText(a.optString("company_name", "—"));
                h.tvLocation.setText(a.optString("location", "—"));
                h.tvSalary.setText("৳" + a.optString("salary_rate", "N/A"));
                h.tvShift.setText(a.optString("shift", "—"));
                h.tvType.setText(a.optString("job_type", "—"));
                String date = a.optString("applied_at", "");
                if (date.length() > 10) date = date.substring(0, 10);
                h.tvDate.setText(date);
                String status = a.optString("status", "Pending");
                h.tvStatus.setText(status);
                switch (status) {
                    case "Accepted":
                        h.tvStatus.setBackgroundResource(R.drawable.bg_badge_success);
                        h.tvStatus.setTextColor(0xFF22C55E); break;
                    case "Rejected":
                        h.tvStatus.setBackgroundResource(R.drawable.bg_badge_error);
                        h.tvStatus.setTextColor(0xFFEF4444); break;
                    default:
                        h.tvStatus.setBackgroundResource(R.drawable.bg_badge_warning);
                        h.tvStatus.setTextColor(0xFFF59E0B);
                }
                // Appointment chip
                String apptDate = a.optString("appointment_date", "");
                String apptTime = a.optString("appointment_time", "");
                if (!apptDate.isEmpty() && !apptTime.isEmpty()) {
                    h.tvAppAppointment.setText("📅 " + apptDate + " " + apptTime);
                    h.tvAppAppointment.setVisibility(View.VISIBLE);
                } else {
                    h.tvAppAppointment.setVisibility(View.GONE);
                }
            } catch (Exception ignored) {}
        }

        @Override public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvJobTitle, tvCompany, tvLocation, tvSalary,
                     tvStatus, tvDate, tvShift, tvType, tvAppAppointment;
            VH(View v) {
                super(v);
                tvJobTitle = v.findViewById(R.id.tvAppJobTitle);
                tvCompany  = v.findViewById(R.id.tvAppCompany);
                tvLocation = v.findViewById(R.id.tvAppLocation);
                tvSalary   = v.findViewById(R.id.tvAppSalary);
                tvStatus   = v.findViewById(R.id.tvAppStatus);
                tvDate     = v.findViewById(R.id.tvAppDate);
                tvShift    = v.findViewById(R.id.tvAppShift);
                tvType     = v.findViewById(R.id.tvAppType);
                tvAppAppointment = v.findViewById(R.id.tvAppAppointment);
            }
        }
    }
}

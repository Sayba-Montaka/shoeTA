package com.example.shoeta;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class AdminPanelActivity extends AppCompatActivity {

    private static final String BASE_URL = "https://blood-bridge.org/shoeTA/";

    private TextView tvStatJobs, tvStatWorkers, tvStatApps,tvPendingJobs;
    private EditText etNewJobTitle;
    private RecyclerView recyclerJobTitles;
    private final List<JSONObject> titleList = new ArrayList<>();
    private JobTitleAdapter jtAdapter;
    private SessionManager sm;
    private RequestQueue queue;

    LinearLayout btnLogout, btnManageJobs;
    Button btnAddJobTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        sm = new SessionManager(this);
        queue = Volley.newRequestQueue(this);

        tvStatJobs = findViewById(R.id.tvStatJobs);
        tvStatWorkers = findViewById(R.id.tvStatWorkers);
        tvStatApps = findViewById(R.id.tvStatApps);
        etNewJobTitle = findViewById(R.id.etNewJobTitle);
        recyclerJobTitles = findViewById(R.id.recyclerJobTitles);
        btnLogout = findViewById(R.id.btnLogout);
        btnManageJobs = findViewById(R.id.btnManageJobs);
        btnAddJobTitle = findViewById(R.id.btnAddJobTitle);
        tvPendingJobs = findViewById(R.id.tvPendingJobs);

        recyclerJobTitles.setLayoutManager(new LinearLayoutManager(this));
        jtAdapter = new JobTitleAdapter(titleList, sm, this, queue, BASE_URL);
        recyclerJobTitles.setAdapter(jtAdapter);

        btnManageJobs.setOnClickListener(v ->
              startActivity(new Intent(this, PendingJobPaymentsActivity.class)));
        btnLogout.setOnClickListener(v -> {
            sm.logout();
            Intent i = new Intent(this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });
        findViewById(R.id.btnPendingFactories).setOnClickListener(v ->
                startActivity(new Intent(this, PendingFactoriesActivity.class)));

        btnAddJobTitle.setOnClickListener(v -> addJobTitle());
        tvStatJobs.setOnClickListener(view->
                startActivity(new Intent( this,JobListActivity.class)));

        loadStats();
        loadJobTitles();
        loadPendingCount();
        loadPendingJobs();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPendingCount();
    }

    // ─── Dashboard stats ──────────────────────────────────────
    private void loadStats() {
        String url = BASE_URL + "admin_settings.php?action=stats&admin_id=" + sm.getUserId();

        StringRequest req = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject o = new JSONObject(response);
                            tvStatJobs.setText(String.valueOf(o.getInt("jobs")));
                            tvStatWorkers.setText(String.valueOf(o.getInt("workers")));
                            tvStatApps.setText(String.valueOf(o.getInt("applications")));
                        } catch (Exception ignored) {
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(AdminPanelActivity.this, "নেটওয়ার্ক সমস্যা", Toast.LENGTH_SHORT).show();
                    }
                });
        queue.add(req);
    }

    // ─── Job titles list ──────────────────────────────────────
    private void loadJobTitles() {
        String url = BASE_URL + "admin_settings.php?action=list_job_titles&admin_id=" + sm.getUserId();

        StringRequest req = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray arr = new JSONObject(response).getJSONArray("job_titles");
                            titleList.clear();
                            for (int i = 0; i < arr.length(); i++)
                                titleList.add(arr.getJSONObject(i));
                            jtAdapter.notifyDataSetChanged();
                        } catch (Exception ignored) {
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(AdminPanelActivity.this, "নেটওয়ার্ক সমস্যা", Toast.LENGTH_SHORT).show();
                    }
                });
        queue.add(req);
    }

    private void loadPendingCount() {
        String url = BASE_URL + "admin_settings.php?action=list_pending_factories&admin_id=" + sm.getUserId();

        StringRequest req = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONArray arr = new JSONObject(response).getJSONArray("factories");
                        TextView tvPendingCount = findViewById(R.id.tvPendingCount);
                        tvPendingCount.setText(arr.length() + " factory pending");
                    } catch (Exception ignored) {
                    }
                },
                error -> {
                });
        queue.add(req);
    }
    private void loadPendingJobs() {
        String url = BASE_URL + "admin_settings.php?action=list_pending_payments&admin_id=" + sm.getUserId();

        StringRequest req = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONArray arr = new JSONObject(response).getJSONArray("jobs");
                        tvPendingJobs.setText(arr.length() + " jobs pending");
                    } catch (Exception ignored) {
                    }
                },
                error -> {
                });
        queue.add(req);
    }

    // ─── Add new job title ────────────────────────────────────
    private void addJobTitle() {
        String title = etNewJobTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "পদের নাম লিখুন", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + "admin_settings.php";

        StringRequest req = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (obj.getBoolean("success")) {
                                etNewJobTitle.setText("");
                                Toast.makeText(AdminPanelActivity.this, "যোগ করা হয়েছে!", Toast.LENGTH_SHORT).show();
                                loadJobTitles();
                            } else {
                                Toast.makeText(AdminPanelActivity.this, obj.getString("message"), Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception ignored) {
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(AdminPanelActivity.this, "নেটওয়ার্ক সমস্যা", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> p = new HashMap<>();
                p.put("action", "add_job_title");
                p.put("admin_id", String.valueOf(sm.getUserId()));
                p.put("title", title);
                return p;
            }
        };
        queue.add(req);
    }

    // ─── Inner adapter ────────────────────────────────────────
    static class JobTitleAdapter extends RecyclerView.Adapter<JobTitleAdapter.VH> {
        private final List<JSONObject> items;
        private final SessionManager sm;
        private final android.content.Context ctx;
        private final RequestQueue queue;
        private final String baseUrl;

        JobTitleAdapter(List<JSONObject> items, SessionManager sm, android.content.Context ctx,
                        RequestQueue queue, String baseUrl) {
            this.items = items;
            this.sm = sm;
            this.ctx = ctx;
            this.queue = queue;
            this.baseUrl = baseUrl;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(LayoutInflater.from(ctx).inflate(R.layout.item_job_title_admin, p, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            JSONObject item = items.get(pos);
            try {
                int id = item.getInt("id");
                h.tvName.setText(item.getString("title"));
                h.swActive.setOnCheckedChangeListener(null);
                h.swActive.setChecked(item.getInt("is_active") == 1);

                h.swActive.setOnCheckedChangeListener((btn, checked) -> {
                    String url = baseUrl + "admin_settings.php";
                    StringRequest req = new StringRequest(Request.Method.POST, url,
                            response -> {
                            }, error -> {
                    }) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> p = new HashMap<>();
                            p.put("action", "toggle_job_title");
                            p.put("admin_id", String.valueOf(sm.getUserId()));
                            p.put("id", String.valueOf(id));
                            p.put("is_active", checked ? "1" : "0");
                            return p;
                        }
                    };
                    queue.add(req);
                });

                h.btnDelete.setOnClickListener(v -> {
                    String url = baseUrl + "admin_settings.php";
                    StringRequest req = new StringRequest(Request.Method.POST, url,
                            response -> {
                                int idx = h.getAdapterPosition();
                                if (idx == RecyclerView.NO_POSITION) return;
                                items.remove(idx);
                                notifyItemRemoved(idx);
                                Toast.makeText(ctx, "মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show();
                            },
                            error -> Toast.makeText(ctx, "নেটওয়ার্ক সমস্যা", Toast.LENGTH_SHORT).show()) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> p = new HashMap<>();
                            p.put("action", "delete_job_title");
                            p.put("admin_id", String.valueOf(sm.getUserId()));
                            p.put("id", String.valueOf(id));
                            return p;
                        }
                    };
                    queue.add(req);
                });
            } catch (Exception ignored) {
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvName, btnDelete;
            Switch swActive;

            VH(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvTitleName);
                swActive = v.findViewById(R.id.swActive);
                btnDelete = v.findViewById(R.id.btnDeleteTitle);
            }
        }
    }
}

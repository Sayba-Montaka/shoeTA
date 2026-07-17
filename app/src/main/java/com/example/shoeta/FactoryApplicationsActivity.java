package com.example.shoeta;

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
import java.util.*;

public class FactoryApplicationsActivity extends AppCompatActivity {

    private static final String BASE_URL = "https://blood-bridge.org/shoeTA/";

    private RecyclerView recycler;
    private final List<JSONObject> appList = new ArrayList<>();
    private ApplicantAdapter adapter;
    private SessionManager sm;
    private RequestQueue queue;
    TextView appoinmentHover;
    private int filterJobId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_factory_applications);

        sm    = new SessionManager(this);
        queue = Volley.newRequestQueue(this);
        appoinmentHover = findViewById(R.id.appoinmentHover);

        filterJobId = getIntent().getIntExtra("job_id", 0);
        String jobTitle = getIntent().getStringExtra("job_title");

        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle); // see layout note below
        if (filterJobId > 0 && jobTitle != null) {
            tvHeaderTitle.setText(jobTitle + " — আবেদনকারীগণ");
        }
        recycler = findViewById(R.id.recyclerApplicants);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ApplicantAdapter(appList, queue, this, BASE_URL, appoinmentHover);
        recycler.setAdapter(adapter);

        // Single, correct place for this click listener (not inside onBindViewHolder)
        appoinmentHover.setOnClickListener(v -> openAppointmentSheet());

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        loadApplications();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadApplications();
    }

    private void openAppointmentSheet() {
        Set<Integer> selected = adapter.getSelectedIds();
        if (selected.isEmpty()) {
            Toast.makeText(this, "কমপক্ষে একজন কর্মী নির্বাচন করুন", Toast.LENGTH_SHORT).show();
            return;
        }
        AppointmentBottomSheet sheet = new AppointmentBottomSheet();
        sheet.setOnAppointmentSetListener((date, time) -> sendAppointment(selected, date, time));
        sheet.show(getSupportFragmentManager(), "appointment_sheet");
    }

    private void sendAppointment(Set<Integer> appIds, String date, String time) {
        String url = BASE_URL + "factory_applications.php";
        StringBuilder idsCsv = new StringBuilder();
        for (int id : appIds) {
            if (idsCsv.length() > 0) idsCsv.append(",");
            idsCsv.append(id);
        }

        StringRequest req = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getBoolean("success")) {
                            adapter.applyAppointment(appIds, date, time);
                            Toast.makeText(this, "অ্যাপয়েন্টমেন্ট দেওয়া হয়েছে", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, obj.optString("message", "ব্যর্থ হয়েছে"), Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> Toast.makeText(this, "নেটওয়ার্ক সমস্যা", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> p = new HashMap<>();
                p.put("action", "set_appointment");
                p.put("app_ids", idsCsv.toString()); // comma-separated
                p.put("appointment_date", date);
                p.put("appointment_time", time);
                return p;
            }
        };
        queue.add(req);
    }

    private void loadApplications() {
        String url = BASE_URL + "factory_applications.php?factory_id=" + sm.getUserId();
        if (filterJobId > 0) {
            url += "&job_id=" + filterJobId;
        }

        StringRequest req = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray arr = new JSONObject(response).getJSONArray("applications");
                            appList.clear();
                            for (int i = 0; i < arr.length(); i++) appList.add(arr.getJSONObject(i));
                            adapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            Toast.makeText(FactoryApplicationsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(FactoryApplicationsActivity.this, "নেটওয়ার্ক সমস্যা", Toast.LENGTH_SHORT).show();
                    }
                });
        req.setShouldCache(false);   // ← disable Volley's response cache for this call
        queue.add(req);
    }

    // ─── Inner adapter ────────────────────────────────────────
    static class ApplicantAdapter extends RecyclerView.Adapter<ApplicantAdapter.VH> {
        private final List<JSONObject> items;
        private final RequestQueue queue;
        private final android.content.Context ctx;
        private final String baseUrl;
        private final Set<Integer> selectedIds = new HashSet<>();
        private final TextView appoinmentHover;

        ApplicantAdapter(List<JSONObject> items, RequestQueue queue, android.content.Context ctx,
                         String baseUrl, TextView appoinmentHover) {
            this.items = items;
            this.queue = queue;
            this.ctx = ctx;
            this.baseUrl = baseUrl;
            this.appoinmentHover = appoinmentHover;
        }

        public Set<Integer> getSelectedIds() { return selectedIds; }

        /** Updates local items with the new appointment info and refreshes the UI. */
        public void applyAppointment(Set<Integer> appIds, String date, String time) {
            for (JSONObject item : items) {
                try {
                    if (appIds.contains(item.getInt("id"))) {
                        item.put("appointment_date", date);
                        item.put("appointment_time", time);
                    }
                } catch (Exception ignored) {}
            }
            selectedIds.clear();
            appoinmentHover.setVisibility(View.GONE);
            notifyDataSetChanged();
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(LayoutInflater.from(ctx).inflate(R.layout.item_applicant_card, p, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            JSONObject a = items.get(pos);
            try {
                int appId = a.getInt("id");
                h.tvName.setText(a.optString("worker_name", "—"));
                h.tvLocation.setText(a.optString("worker_location", "—"));
                h.tvExp.setText("⏳ " + a.optString("experience_years", "0") + " বছর");
                h.tvGender.setText(a.optString("gender", "—"));
                h.tvPhone.setText("📞 " + a.optString("worker_phone", "—"));
                setStatusBadge(h.tvStatus, a.optString("status", "Pending"));

                // Appointment chip
                String apptDate = a.optString("appointment_date", "");
                String apptTime = a.optString("appointment_time", "");
                if (!apptDate.isEmpty() && !apptTime.isEmpty()) {
                    h.tvAppointment.setText("📅 " + apptDate + " " + apptTime);
                    h.tvAppointment.setVisibility(View.VISIBLE);
                } else {
                    h.tvAppointment.setVisibility(View.GONE);
                }

                // avoid stale listener firing from view recycling
                h.cbSelect.setOnCheckedChangeListener(null);
                h.cbSelect.setChecked(selectedIds.contains(appId));
                h.cbSelect.setOnCheckedChangeListener((btn, checked) -> {
                    if (checked) selectedIds.add(appId);
                    else selectedIds.remove(appId);
                    appoinmentHover.setVisibility(selectedIds.isEmpty() ? View.GONE : View.VISIBLE);
                });

                h.btnAccept.setOnClickListener(v -> updateStatus(appId, "Accepted", h.tvStatus));

                h.btnReject.setOnClickListener(v -> updateStatus(appId, "Rejected", h.tvStatus, () -> {
                    int idx = h.getAdapterPosition();
                    if (idx != RecyclerView.NO_POSITION) {
                        selectedIds.remove(appId);
                        items.remove(idx);
                        notifyItemRemoved(idx);
                    }
                }));

                h.btnReview.setOnClickListener(v -> updateStatus(appId, "Reviewed", h.tvStatus));
            } catch (Exception ignored) {}
        }

        private void setStatusBadge(TextView tv, String status) {
            tv.setText(status);
            switch (status) {
                case "Accepted":
                    tv.setBackgroundResource(R.drawable.bg_badge_success);
                    tv.setTextColor(0xFF22C55E); break;
                case "Rejected":
                    tv.setBackgroundResource(R.drawable.bg_badge_error);
                    tv.setTextColor(0xFFEF4444); break;
                default:
                    tv.setBackgroundResource(R.drawable.bg_badge_warning);
                    tv.setTextColor(0xFFF59E0B);
            }
        }

        private void updateStatus(int appId, String status, TextView tvStatus) {
            updateStatus(appId, status, tvStatus, null);
        }

        private void updateStatus(int appId, String status, TextView tvStatus, Runnable onSuccess) {
            String url = baseUrl + "factory_applications.php";

            StringRequest req = new StringRequest(Request.Method.POST, url,
                    response -> {
                        try {
                            if (new JSONObject(response).getBoolean("success")) {
                                setStatusBadge(tvStatus, status);

                                // Keep the local data model in sync so re-binds (e.g. after
                                // notifyDataSetChanged from applyAppointment) don't revert the badge
                                for (JSONObject item : items) {
                                    if (item.optInt("id") == appId) {
                                        item.put("status", status);
                                        break;
                                    }
                                }

                                Toast.makeText(ctx, status + " করা হয়েছে", Toast.LENGTH_SHORT).show();
                                if (onSuccess != null) onSuccess.run();
                            }
                        } catch (Exception ignored) {}
                    },
                    error -> Toast.makeText(ctx, "নেটওয়ার্ক সমস্যা", Toast.LENGTH_SHORT).show()) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> p = new HashMap<>();
                    p.put("action", "update_status");
                    p.put("app_id", String.valueOf(appId));
                    p.put("status", status);
                    return p;
                }
            };
            queue.add(req);
        }

        @Override public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvName, tvLocation, tvStatus, tvExp, tvGender, tvPhone, tvAppointment;
            Button btnAccept, btnReject, btnReview;
            CheckBox cbSelect;
            VH(View v) {
                super(v);
                tvName        = v.findViewById(R.id.tvApplicantName);
                tvLocation    = v.findViewById(R.id.tvApplicantLocation);
                tvStatus      = v.findViewById(R.id.tvApplicantStatus);
                tvExp         = v.findViewById(R.id.tvApplicantExp);
                tvGender      = v.findViewById(R.id.tvApplicantGender);
                tvPhone       = v.findViewById(R.id.tvApplicantPhone);
                tvAppointment = v.findViewById(R.id.tvApplicantAppointment); // added earlier
                btnAccept     = v.findViewById(R.id.btnAccept);
                btnReject     = v.findViewById(R.id.btnReject);
                btnReview     = v.findViewById(R.id.btnReview);
                cbSelect      = v.findViewById(R.id.cbSelect);
            }
        }
    }
}
package com.example.shoeta;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.VH> {

    private static final String BASE_URL = "https://blood-bridge.org/shoeTA/";

    private final List<JSONObject> jobs;
    private final SessionManager sm;
    private final Context ctx;
    private final RequestQueue queue;
    private OnUnsaveListener onUnsaveListener;
    private static final String PREF_NAME = "SAVED_JOBS";
    private static final String PREF_KEY  = "jobs";
    public JobAdapter(List<JSONObject> jobs, SessionManager sm, Context ctx) {
        this.jobs = jobs;
        this.sm = sm;
        this.ctx = ctx;
        this.queue = Volley.newRequestQueue(ctx);
    }
    // Lets SavedJobsActivity pull an item out of its own list the instant
    // it's unsaved, instead of waiting for the next onResume reload.
    public interface OnUnsaveListener { void onUnsaved(int jobId); }
    public void setOnUnsaveListener(OnUnsaveListener listener)
    { this.onUnsaveListener = listener; }
    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_job_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        JSONObject j = jobs.get(pos);
        int jobId = j.optInt("id");
        try {
            h.tvTitle.setText(j.optString("job_title", "—"));
            h.tvLocation.setText(j.optString("location", "—"));
            h.tvSalary.setText("৳" + j.optString("salary_rate", "N/A"));
            h.tvJobType.setText(j.optString("job_type", "Full Time"));
            h.tvShift.setText(j.optString("shift", "Day"));
            h.tvGender.setText(j.optString("gender_required", "Both"));

            // Click card → detail
            h.itemView.setOnClickListener(v -> {
                Intent i = new Intent(ctx, JobDetailActivity.class);
                i.putExtra("job_json", j.toString());
                ctx.startActivity(i);
            });

            if (h.btnBookmark != null && sm.isWorker()) {
                h.btnBookmark.setText(
                        isSaved(jobId) ? "\uD83C\uDFF3\uFE0F" : "\uD83D\uDD16");
                h.btnBookmark.setOnClickListener(v -> toggleSave(j, h.btnBookmark));
            }
            // Apply button visible only to workers
            if (sm.isWorker()) {
                h.btnApply.setOnClickListener(v -> applyToJob(j.optInt("id"), h.btnApply));
            } else {
                h.btnApply.setText("আবেদনকারীদের দেখুন");
                h.btnApply.setOnClickListener(v -> {
                    Intent i = new Intent(ctx, FactoryApplicationsActivity.class);
                    i.putExtra("job_id", jobId);
                    i.putExtra("job_title", j.optString("job_title", ""));
                    ctx.startActivity(i);
                });
            }
        } catch (Exception ignored) {
        }
    }

    private void applyToJob(int jobId, TextView btn) {
        btn.setEnabled(false);
        btn.setText("...");

        String url = BASE_URL + "apply.php";

        StringRequest req = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (obj.getBoolean("success")) {
                                btn.setText("✓ Applied");
                                Toast.makeText(ctx, "আবেদন সম্পন্ন!", Toast.LENGTH_SHORT).show();
                            } else {
                                btn.setEnabled(true);
                                btn.setText("আবেদন করুন");
                                Toast.makeText(ctx, obj.getString("message"), Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            btn.setEnabled(true);
                            btn.setText("আবেদন করুন");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        btn.setEnabled(true);
                        btn.setText("আবেদন করুন");
                        Toast.makeText(ctx, "নেটওয়ার্ক সমস্যা", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("worker_id", String.valueOf(sm.getUserId()));
                params.put("job_id", String.valueOf(jobId));
                return params;
            }
        };
        queue.add(req);
    }

    @Override
    public int getItemCount() {
        return jobs.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvLocation, tvSalary, tvJobType, tvShift, tvGender, btnApply, btnBookmark;

        VH(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvJobTitle);
            tvLocation = v.findViewById(R.id.tvLocation);
            tvSalary = v.findViewById(R.id.tvSalary);
            tvJobType = v.findViewById(R.id.tvJobType);
            tvShift = v.findViewById(R.id.tvShift);
            tvGender = v.findViewById(R.id.tvGender);
            btnApply = v.findViewById(R.id.btnApply);
            btnBookmark = v.findViewById(R.id.btnBookmark);
        }
    }
    private boolean isSaved(int jobId) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        try {
            JSONArray arr = new JSONArray(prefs.getString(PREF_KEY, "[]"));
            for (int i = 0; i < arr.length(); i++) {
                if (arr.getJSONObject(i).optInt("id") == jobId) return true;
            }
        } catch (Exception ignored) {}
        return false;
    }
    private void toggleSave(JSONObject job, TextView btnBookmark) {
        btnBookmark.animate().scaleX(1.3f).scaleY(1.3f).setDuration(150)
                .withEndAction(() -> btnBookmark.animate().scaleX(1f).scaleY(1f).setDuration(150));
        int jobId = job.optInt("id");
        SharedPreferences prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        try {
            JSONArray arr = new JSONArray(prefs.getString(PREF_KEY, "[]"));
            boolean alreadySaved = false;
            int removeIndex = -1;

            for (int i = 0; i < arr.length(); i++) {
                if (arr.getJSONObject(i).optInt("id") == jobId) {
                    alreadySaved = true;
                    removeIndex = i;
                    break;
                }
            }
            if (alreadySaved) {
                arr.remove(removeIndex);
                btnBookmark.setText("\uD83D\uDD16");
                Toast.makeText(ctx, "সংরক্ষণ থেকে সরানো হয়েছে", Toast.LENGTH_SHORT).show();
                if (onUnsaveListener != null) onUnsaveListener.onUnsaved(jobId);
            } else {
                arr.put(job); // store the whole job object — SavedJobsActivity reads it straight back
                btnBookmark.setText("\uD83C\uDFF3\uFE0F");
                Toast.makeText(ctx, "সংরক্ষিত হয়েছে", Toast.LENGTH_SHORT).show();
            }
            prefs.edit().putString(PREF_KEY, arr.toString()).apply();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }
}

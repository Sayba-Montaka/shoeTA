package com.example.shoeta;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.dhaval2404.imagepicker.ImagePicker;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.*;

public class WorkerRegisterActivity extends AppCompatActivity {

    private static final String BASE_URL = "https://blood-bridge.org/shoeTA/";

    private EditText etName, etPhone, etPassword, etAge, etLocation, etExperience, etSalary;
    private Spinner  spinnerGender, spinnerSkill;
    private ProgressBar progressBar;
    private ImageView workerImage;
    private RequestQueue queue;

    private final List<String>  skillNames = new ArrayList<>();
    private final List<Integer> skillIds   = new ArrayList<>();
    private final List<String>  genderOpts = Arrays.asList("Male", "Female", "Other");
    private Uri selectedImageUri;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null && workerImage != null) {
                        workerImage.setImageURI(selectedImageUri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_register);

        queue = Volley.newRequestQueue(this);

        etName       = findViewById(R.id.etName);
        etPhone      = findViewById(R.id.etPhone);
        etPassword   = findViewById(R.id.etPassword);
        etAge        = findViewById(R.id.etAge);
        etLocation   = findViewById(R.id.etLocation);
        etExperience = findViewById(R.id.etExperience);
        etSalary     = findViewById(R.id.etSalary);
        spinnerGender= findViewById(R.id.spinnerGender);
        spinnerSkill = findViewById(R.id.spinnerSkill);
        progressBar  = findViewById(R.id.progressBar);
        workerImage  = findViewById(R.id.worker_image);

        ArrayAdapter<String> ga = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, genderOpts);
        ga.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(ga);

        loadSkills();

        View choosePhoto = findViewById(R.id.choose_photo);
        if (choosePhoto != null) {
            choosePhoto.setOnClickListener(v ->
                    ImagePicker.with(this)
                            .crop(1f, 1f)                 // square crop, good for avatars
                            .compress(512)                // max file size ~512KB
                            .maxResultSize(600, 600)       // resize to 600x600
                            .createIntent(intent -> {
                                imagePickerLauncher.launch(intent);
                                return null;
                            })
            );
        }

        findViewById(R.id.btnRegister).setOnClickListener(v -> doRegister());
        findViewById(R.id.tvLoginLink).setOnClickListener(v -> {
            Intent i = new Intent(this, LoginActivity.class);
            i.putExtra("user_type", "worker"); startActivity(i);
        });
    }

    private void loadSkills() {
        String url = BASE_URL + "admin_settings.php?action=get_dropdowns";

        StringRequest req = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray arr = new JSONObject(response).getJSONArray("job_titles");
                            skillNames.add("-- দক্ষতা বেছে নিন --"); skillIds.add(0);
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject o = arr.getJSONObject(i);
                                skillNames.add(o.getString("name")); skillIds.add(o.getInt("id"));
                            }
                            ArrayAdapter<String> a = new ArrayAdapter<>(WorkerRegisterActivity.this,
                                    android.R.layout.simple_spinner_item, skillNames);
                            a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerSkill.setAdapter(a);
                        } catch (Exception ignored) {}
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {}
                });
        queue.add(req);
    }

    private void doRegister() {
        String name  = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String pass  = etPassword.getText().toString().trim();

        if (!RegistrationValidator.isWorkerRegistrationValid(name, phone, pass)) {
            Toast.makeText(this, "নাম, ফোন এবং পাসওয়ার্ড আবশ্যক (পাসওয়ার্ড কমপক্ষে ৬ অক্ষর)", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        String url = BASE_URL + "register.php";

        StringRequest req = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressBar.setVisibility(View.GONE);
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (obj.getBoolean("success")) {
                                Toast.makeText(WorkerRegisterActivity.this, "নিবন্ধন সম্পন্ন! লগইন করুন", Toast.LENGTH_LONG).show();
                                Intent i = new Intent(WorkerRegisterActivity.this, LoginActivity.class);
                                i.putExtra("user_type", "worker"); startActivity(i); finish();
                            } else {
                                Toast.makeText(WorkerRegisterActivity.this, obj.getString("message"), Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(WorkerRegisterActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(WorkerRegisterActivity.this, "নেটওয়ার্ক সমস্যা", Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> p = new HashMap<>();
                p.put("user_type", "worker"); p.put("name", name); p.put("phone", phone);
                p.put("password", pass);
                p.put("age", etAge.getText().toString().trim().isEmpty() ? "0" : etAge.getText().toString().trim());
                p.put("location", etLocation.getText().toString().trim());
                p.put("gender", genderOpts.get(spinnerGender.getSelectedItemPosition()));
                p.put("job_title_id", String.valueOf(skillIds.isEmpty() ? 0 : skillIds.get(spinnerSkill.getSelectedItemPosition())));
                p.put("experience_years", etExperience.getText().toString().trim().isEmpty() ? "0" : etExperience.getText().toString().trim());
                p.put("expected_salary", etSalary.getText().toString().trim());

                if (selectedImageUri != null) {
                    try {
                        android.graphics.Bitmap bmp = android.graphics.BitmapFactory
                                .decodeStream(getContentResolver().openInputStream(selectedImageUri));
                        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
                        bmp.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, bos);
                        String encoded = android.util.Base64.encodeToString(bos.toByteArray(), android.util.Base64.NO_WRAP);
                        p.put("photo_base64", encoded);
                    } catch (Exception ignored) {}
                }

                return p;
            }
        };
        queue.add(req);
    }
}

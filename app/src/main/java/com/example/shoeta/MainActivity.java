package com.example.shoeta;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // FCM SDK (and your app) can post notifications.
                } else {

                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Auto-login if session exists
        SessionManager sm = new SessionManager(this);
        if (sm.isLoggedIn()) { routeByRole(sm.getUserType()); return; }

        setContentView(R.layout.activity_main);

        findViewById(R.id.btnWorker).setOnClickListener(v -> openLogin("worker"));
        findViewById(R.id.btnFactory).setOnClickListener(v -> openLogin("factory"));
        findViewById(R.id.btnAdmin).setOnClickListener(v -> openLogin("admin"));

        askNotificationPermission();

    }

    private void openLogin(String type) {
        Intent i = new Intent(this, LoginActivity.class);
        i.putExtra("user_type", type);
        startActivity(i);
    }

    private void routeByRole(String type) {
        Intent i;
        switch (type) {
            case "admin":   i = new Intent(this, AdminPanelActivity.class); break;
            case "factory": i = new Intent(this, FactoryDashboardActivity.class); break;
            default:        i = new Intent(this, HomeWorkerActivity.class); break;
        }
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private void askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {

            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Notification")
                        .setMessage("Click 'OKAY' so that we can send you notifications")
                        .setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
            } else {

                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

}
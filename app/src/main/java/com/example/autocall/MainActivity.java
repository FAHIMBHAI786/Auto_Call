package com.example.autocall;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.autocall.R;

public class MainActivity extends AppCompatActivity {

    private Button stopButton;
    private EditText etPhoneNumber;
    private boolean calling = false;
    private Button callButton;
    private TelephonyManager telephonyManager;
    private PhoneStateListener phoneStateListener;

    private static final int PERMISSION_REQUEST_CALL_PHONE = 1;
    private static final int PERMISSION_REQUEST_READ_PHONE_STATE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        stopButton = findViewById(R.id.stopCall);
        etPhoneNumber = findViewById(R.id.phoneNumber);
        callButton = findViewById(R.id.callButton);

        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCalling();
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopCalling();
            }
        });
        telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                super.onCallStateChanged(state, incomingNumber);
                if (state == TelephonyManager.CALL_STATE_IDLE && calling) {
                    // Call disconnected, redial the number
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (calling) {
                                String phoneNumber = etPhoneNumber.getText().toString().trim();
                                makePhoneCall(phoneNumber);
                            }
                        }
                    }, 10); // Delay in milliseconds before redialing
                }
            }
        };

        // Check if the CALL_PHONE permission is not granted

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CALL_PHONE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with your app logic
                // For example, you can initiate a call here
                // makePhoneCall();
            } else {
                // Permission denied, inform the user
                Toast.makeText(this, "Permission denied, cannot make a phone call.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void makePhoneCall(String phoneNumber) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(callIntent);
        } else {
            Toast.makeText(this, "Permission denied, cannot make a phone call.", Toast.LENGTH_SHORT).show();
        }
    }

    private void startCalling() {
        if (!calling) {
            calling = true;
            callButton.setEnabled(false);
            stopButton.setEnabled(true);
            String phoneNumber = etPhoneNumber.getText().toString().trim();
            makePhoneCall(phoneNumber);
        } else {
            Toast.makeText(this, "Already calling...", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopCalling() {
        if (calling) {
            calling = false;
            callButton.setEnabled(true);
            stopButton.setEnabled(false);
        } else {
            Toast.makeText(this, "Not currently calling.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (telephonyManager != null && phoneStateListener != null) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
            }

        } else {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            // Log an error or handle the situation where the objects are not properly initialized
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        // Unregister PhoneStateListener to stop listening for call state changes
        if (telephonyManager != null && phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
    }
    }
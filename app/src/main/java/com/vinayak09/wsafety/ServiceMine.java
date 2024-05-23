 package com.vinayak09.wsafety;

import com.github.tbouron.shakedetector.library.ShakeDetector;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.telephony.SmsManager;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

public class ServiceMine extends Service {

    boolean isRunning = false;
    FusedLocationProviderClient fusedLocationClient;
    Ringtone ringtone;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    SmsManager manager = SmsManager.getDefault();
    String myLocation;

    @Override
    public void onCreate() {
        super.onCreate();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            myLocation = "http://maps.google.com/maps?q=loc:" + location.getLatitude() + "," + location.getLongitude();
                        } else {
                            myLocation = "Unable to Find Location :(";
                        }
                    }
                });

        ShakeDetector.create(this, () -> {
            playRingtone();

            SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
            String ENUM = sharedPreferences.getString("ENUM", "NONE");
            if (!ENUM.equalsIgnoreCase("NONE")) {
                String message = "I'm in Trouble!\nSending My Location:\n" + myLocation + "\nALARM_TRIGGER";
                manager.sendTextMessage(ENUM, null, message, null, null);
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Your existing onStartCommand code...
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRingtone();
    }

    private void playRingtone() {
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        String ringtoneUriString = sharedPreferences.getString("ENUM", null);

        if (ringtoneUriString != null) {
            Uri ringtoneUri = Uri.parse(ringtoneUriString);
            if (ringtone == null) {
                ringtone = RingtoneManager.getRingtone(getApplicationContext(), ringtoneUri);
            }

            if (!ringtone.isPlaying()) {
                ringtone.play();
            }
        }
    }

    private void stopRingtone() {
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
    }
}
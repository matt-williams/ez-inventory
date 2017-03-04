package com.github.yinyee.locator;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;
import com.estimote.sdk.Utils;

import java.util.List;
import java.util.UUID;

public class Locator extends AppCompatActivity {

    private static final String GOODS_IN = "GOODS_IN";
    private static final String GOODS_OUT = "GOODS_OUT";
    // Plum beacon
    private static final Region GOODS_IN_REGION = new Region(
            GOODS_IN,
            UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"),
            33815,
            25772
    );
    // Pink beacon
    private static final Region GOODS_OUT_REGION = new Region(
            GOODS_OUT,
            UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"),
            62826,
            65526
    );
    private static String location;
    private static Utils.Proximity GOODS_IN_PROXIMITY;
    private static Utils.Proximity GOODS_OUT_PROXIMITY;
    private static int GOODS_IN_RSSI;
    private static int GOODS_OUT_RSSI;
    private BeaconManager beaconManager;

    private Button btnBarcode;
    private Button btnOCR;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locator);

        btnBarcode = (Button) findViewById(R.id.btn_barcode);
        btnBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent scanBarcode = new Intent(Locator.this, MainActivity.class);
                startActivity(scanBarcode);
            }
        });

        btnOCR = (Button) findViewById(R.id.btn_ocr);
        btnOCR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Intent readTextFromImage = new Intent(Locator.this, OcrMainActivity.class);
                startActivity(readTextFromImage);
            }
        });

        beaconManager = new BeaconManager(getApplicationContext());

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(GOODS_IN_REGION);
                beaconManager.startRanging(GOODS_OUT_REGION);
            }
        });

        beaconManager.setRangingListener(new BeaconManager.RangingListener() {

            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> list) {
                if (!list.isEmpty()) {
                    Beacon beacon = list.get(0);

                    if (region.getIdentifier().compareTo(GOODS_IN) == 0) {
                        GOODS_IN_RSSI = beacon.getRssi();
                        GOODS_IN_PROXIMITY = Utils.computeProximity(beacon);

                    }

                    if (region.getIdentifier().compareTo(GOODS_OUT) == 0) {
                        GOODS_OUT_RSSI = beacon.getRssi();
                        GOODS_OUT_PROXIMITY = Utils.computeProximity(beacon);
                    }

                    if (GOODS_IN_PROXIMITY == Utils.Proximity.IMMEDIATE && GOODS_OUT_PROXIMITY != Utils.Proximity.IMMEDIATE) {
                        location = GOODS_IN;
                    } else if (GOODS_IN_PROXIMITY != Utils.Proximity.IMMEDIATE && GOODS_OUT_PROXIMITY == Utils.Proximity.IMMEDIATE) {
                        location = GOODS_OUT;
                    } else if (GOODS_IN_PROXIMITY == Utils.Proximity.IMMEDIATE && GOODS_OUT_PROXIMITY == Utils.Proximity.IMMEDIATE) {
                        if (GOODS_IN_RSSI < GOODS_OUT_RSSI) {
                            location = GOODS_OUT;
                        } else {
                            location = GOODS_IN;
                        }
                    } else {
                        location = "NOT DETECTED";
                    }
                }

//                android.util.Log.e("Locator", "GOODS_IN_PROXIMITY: " + GOODS_IN_PROXIMITY);
//                android.util.Log.e("Locator", "GOODS_OUT_PROXIMITY: " + GOODS_OUT_PROXIMITY);
//                android.util.Log.e("Locator", "GOODS_IN_RSSI: " + GOODS_IN_RSSI);
//                android.util.Log.e("Locator", "GOODS_OUT_RSSI: " + GOODS_OUT_RSSI);

                String display = "Location is set to: " + location;
                ((TextView) findViewById(R.id.location)).setText(display);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SystemRequirementsChecker.checkWithDefaultDialogs(this);
    }

    public void showNotification(String title, String message) {

        Intent notifyIntent = new Intent(this, EZLocator.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0, new Intent[] { notifyIntent }, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();
//        notification.defaults |= Notification.DEFAULT_SOUND;

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }
}
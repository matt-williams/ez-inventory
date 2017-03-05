package com.github.yinyee.locator.estimote;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;
import com.estimote.sdk.Utils;
import com.github.yinyee.locator.AuthenticationActivity;
import com.github.yinyee.locator.barcode.BarcodeMainActivity;
import com.github.yinyee.locator.EZInventory;
import com.github.yinyee.locator.ocr.OcrCaptureActivity;
import com.github.yinyee.locator.R;
import com.github.yinyee.locator.quickbooks.Constants;
import com.github.yinyee.locator.quickbooks.Invoice;
import com.github.yinyee.locator.quickbooks.Item;
import com.github.yinyee.locator.quickbooks.QuickBooksApi;
import com.google.api.client.util.DateTime;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Locator extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

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

    private static Utils.Proximity GOODS_IN_PROXIMITY;
    private static Utils.Proximity GOODS_OUT_PROXIMITY;
    private static int GOODS_IN_RSSI;
    private static int GOODS_OUT_RSSI;
    private BeaconManager beaconManager;
    private int mode;
    private Bundle mSavedInstanceState;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mSavedInstanceState = new Bundle();
        setContentView(R.layout.activity_locator);

        Spinner spinner = (Spinner) findViewById(R.id.location_detection_mode);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.location_contexts, android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setOnItemSelectedListener(this);

        Button btnConfirmLocation = (Button) findViewById(R.id.confirm_location);
        btnConfirmLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String loc = ((TextView) findViewById(R.id.location)).getText().toString();
                Intent goToOCR = new Intent(Locator.this, OcrCaptureActivity.class);
                android.util.Log.e("Locator", "location: ");
                android.util.Log.e("Locator", loc);
                mSavedInstanceState.putString("LOCATION_CONTEXT", loc);
                mSavedInstanceState.putString("DETECT_MODE", String.valueOf(mode));
                goToOCR.putExtras(mSavedInstanceState);
                startActivity(goToOCR, mSavedInstanceState);
            }
        });

        beaconManager = new BeaconManager(getApplicationContext());
    }

    private void initializeBeacons() {
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

                String loc = "NOT DETECTED";

                if (mode == 2) {

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
                            loc = "GOODS IN";
                        } else if (GOODS_IN_PROXIMITY != Utils.Proximity.IMMEDIATE && GOODS_OUT_PROXIMITY == Utils.Proximity.IMMEDIATE) {
                            loc = "GOODS OUT";
                        } else if (GOODS_IN_PROXIMITY == Utils.Proximity.IMMEDIATE && GOODS_OUT_PROXIMITY == Utils.Proximity.IMMEDIATE) {
                            if (GOODS_IN_RSSI < GOODS_OUT_RSSI) {
                                loc = "GOODS OUT";
                            } else {
                                loc = "GOODS IN";
                            }
                        } else {
                            loc = "NOT DETECTED";
                            showNotification("Cannot detect location", "Cannot detect location - please select from list");
                        }
                    }

//                android.util.Log.e("Locator", "GOODS_IN_PROXIMITY: " + GOODS_IN_PROXIMITY);
//                android.util.Log.e("Locator", "GOODS_OUT_PROXIMITY: " + GOODS_OUT_PROXIMITY);
//                android.util.Log.e("Locator", "GOODS_IN_RSSI: " + GOODS_IN_RSSI);
//                android.util.Log.e("Locator", "GOODS_OUT_RSSI: " + GOODS_OUT_RSSI);

                    ((TextView) findViewById(R.id.location)).setText(loc);

                } else {
                    loc = ((Spinner) findViewById(R.id.location_detection_mode)).getSelectedItem().toString();
                    ((TextView) findViewById(R.id.location)).setText(loc);
                }

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SystemRequirementsChecker.checkWithDefaultDialogs(this);
        QuickBooksApi.Authenticator authenticator = new QuickBooksApi.Authenticator(this, Constants.OAUTH_CONSUMER_KEY, Constants.OAUTH_CONSUMER_SECRET);
        QuickBooksApi api = authenticator.tryExistingCredentials();
        if (api != null) {
            new QuickBooksApi.CheckConnectionTask(api) {
                @Override
                protected void onPostExecute(Boolean success) {
                    if (Boolean.TRUE.equals(success)) {
                        initializeBeacons();
                    } else {
                        startActivity(new Intent(Locator.this, AuthenticationActivity.class));
                    }
                }
            }.execute();
        } else {
            startActivity(new Intent(Locator.this, AuthenticationActivity.class));
        }
    }

    public void showNotification(String title, String message) {

        Intent notifyIntent = new Intent(this, EZInventory.class);
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mode = ((Spinner) findViewById(R.id.location_detection_mode)).getSelectedItemPosition();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // set to AUTO
        ((Spinner) findViewById(R.id.location_detection_mode)).setSelection(2);
    }
}

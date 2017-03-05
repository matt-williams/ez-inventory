package com.github.yinyee.locator.estimote;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
import com.github.yinyee.locator.barcode.BarcodeMainActivity;
import com.github.yinyee.locator.EZInventory;
import com.github.yinyee.locator.ocr.OcrCaptureActivity;
import com.github.yinyee.locator.R;
import com.github.yinyee.locator.quickbooks.Constants;
import com.github.yinyee.locator.quickbooks.Invoice;
import com.github.yinyee.locator.quickbooks.QuickBooksApi;

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
    private static String location;
    private static Utils.Proximity GOODS_IN_PROXIMITY;
    private static Utils.Proximity GOODS_OUT_PROXIMITY;
    private static int GOODS_IN_RSSI;
    private static int GOODS_OUT_RSSI;
    private BeaconManager beaconManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locator);

        Spinner spinner = (Spinner) findViewById(R.id.location_detection_mode);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.location_contexts, android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setOnItemSelectedListener(this);

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

                String loc = "NOT DETECTED";

                String mode = ((Spinner) findViewById(R.id.location_detection_mode)).getSelectedItem().toString();
                if (mode.compareTo("AUTO") == 0) {

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
                            goToOCR(loc);
                        } else if (GOODS_IN_PROXIMITY != Utils.Proximity.IMMEDIATE && GOODS_OUT_PROXIMITY == Utils.Proximity.IMMEDIATE) {
                            loc = "GOODS OUT";
                            goToOCR(loc);
                        } else if (GOODS_IN_PROXIMITY == Utils.Proximity.IMMEDIATE && GOODS_OUT_PROXIMITY == Utils.Proximity.IMMEDIATE) {
                            if (GOODS_IN_RSSI < GOODS_OUT_RSSI) {
                                loc = "GOODS OUT";
                                goToOCR(loc);
                            } else {
                                loc = "GOODS IN";
                                goToOCR(loc);
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
                    goToOCR(loc);
                }

            }
        });

//        QuickBooksApi api = new QuickBooksApi.Authenticator(Constants.OAUTH_CONSUMER_KEY, Constants.OAUTH_CONSUMER_SECRET).authenticate();
//        new QuickBooksApi.GetInvoiceTask(api, Constants.REALM_ID) {
//            @Override
//            protected void onPostExecute(Invoice invoice) {
//                android.util.Log.e("MainActivity", invoice.id + " - " + invoice.lines.get(0).description);
//            }
//        }.execute("124");
//        new QuickBooksApi.QueryInvoicesTask(api, Constants.REALM_ID) {
//            @Override
//            protected void onPostExecute(List<Invoice> invoices) {
//                android.util.Log.e("MainActivity", "How many invoices? " + invoices.size());
//            }
//        }.execute();

    }

    private void goToOCR(String loc) {
        Intent readTextFromImage = new Intent(Locator.this, OcrCaptureActivity.class);
        readTextFromImage.putExtra("LOCATION_CONTEXT", loc);
        startActivity(readTextFromImage);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SystemRequirementsChecker.checkWithDefaultDialogs(this);
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
        ((Spinner) findViewById(R.id.location_detection_mode)).setSelection(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // set to AUTO
        ((Spinner) findViewById(R.id.location_detection_mode)).setSelection(0);
    }
}
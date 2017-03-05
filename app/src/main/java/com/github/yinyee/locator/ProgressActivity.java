package com.github.yinyee.locator;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.yinyee.locator.quickbooks.Constants;
import com.github.yinyee.locator.quickbooks.Invoice;
import com.github.yinyee.locator.quickbooks.QuickBooksApi;
import com.google.api.client.util.DateTime;

import java.util.Date;
import java.util.List;

public class ProgressActivity extends AppCompatActivity {

    private QuickBooksApi mApi;
    private String invoiceNo;
    private LinearLayout ll;
    private LinearLayout.LayoutParams tvLayoutParamsLeft, tvLayoutParamsRight;
    private Bundle mSavedInstanceState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mSavedInstanceState = intent.getExtras();

        String mode = mSavedInstanceState.getString("DETECT_MODE");
        String loc = mSavedInstanceState.getString("LOCATION_CONTEXT");
        invoiceNo = mSavedInstanceState.getString("INVOICE_NO");

        ScrollView scroll = new ScrollView(this);
        ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        ll.setLayoutParams(layoutParams);

        tvLayoutParamsLeft = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        tvLayoutParamsLeft.gravity = Gravity.LEFT;

        tvLayoutParamsRight = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        tvLayoutParamsRight.gravity = Gravity.RIGHT;

        TextView tvLocDetection = new TextView(this);
        tvLocDetection.setLayoutParams(tvLayoutParamsLeft);
        tvLocDetection.setText("Location detection mode: ");
        tvLocDetection.setPadding(8,8,8,8);
        ll.addView(tvLocDetection);

        TextView tvLocDetectionMode = new TextView(this);
        tvLocDetectionMode.setLayoutParams(tvLayoutParamsRight);
        tvLocDetectionMode.setText(mode);
        tvLocDetectionMode.setPadding(8,8,8,8);
        ll.addView(tvLocDetectionMode);

        TextView tvLocContext = new TextView(this);
        tvLocContext.setLayoutParams(tvLayoutParamsLeft);
        tvLocContext.setText("Location context: ");
        tvLocContext.setPadding(8,8,8,8);
        ll.addView(tvLocContext);

        TextView tvLocation = new TextView(this);
        tvLocation.setLayoutParams(tvLayoutParamsRight);
        tvLocation.setText(loc);
        tvLocation.setPadding(8,8,8,8);
        ll.addView(tvLocation);

        TextView tvInvoice = new TextView(this);
        tvInvoice.setLayoutParams(tvLayoutParamsLeft);
        tvInvoice.setText("Invoice number: ");
        tvInvoice.setPadding(8,8,8,8);
        ll.addView(tvInvoice);

        TextView tvInvoiceNumber = new TextView(this);
        tvInvoiceNumber.setLayoutParams(tvLayoutParamsRight);
        tvInvoiceNumber.setText(invoiceNo);
        tvInvoiceNumber.setPadding(8,8,8,8);
        ll.addView(tvInvoiceNumber);

        LinearLayout.LayoutParams hLineLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 10);
        View hLine = new View(this);
        hLine.setLayoutParams(hLineLayoutParams);
        hLine.setBackgroundColor(Color.BLUE);
        ll.addView(hLine);

        LinearLayout.LayoutParams btnLayoutParams = new LinearLayout.LayoutParams(250, 80);
        btnLayoutParams.gravity = Gravity.RIGHT;
        btnLayoutParams.gravity = Gravity.BOTTOM;
        Button btnSendInvoice = new Button(this);
        btnSendInvoice.setLayoutParams(btnLayoutParams);
        btnSendInvoice.setText("Issue invoice");
        btnSendInvoice.setPadding(8,8,8,8);
        btnSendInvoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send invoice
//                QuickBooksApi.Authenticator authenticator = new QuickBooksApi.Authenticator(ProgressActivity.this, Constants.OAUTH_CONSUMER_KEY, Constants.OAUTH_CONSUMER_SECRET);
//                mApi = authenticator.tryExistingCredentials();
//                if (mApi != null) {
                    new QuickBooksApi.CheckConnectionTask(mApi) {
                        @Override
                        protected void onPostExecute(Boolean success) {
                            if (Boolean.TRUE.equals(success)) {
                                // Retrieve invoice
                                new QuickBooksApi.QueryInvoicesTask(mApi) {
                                    @Override
                                    protected void onPostExecute(List<Invoice> invoices) {
                                        if (invoices != null && !invoices.isEmpty()) {
                                            Invoice invoice = invoices.get(0);
                                            android.util.Log.e("MainActivity", invoice.id + " - " + invoice.lines.get(0).amount + " - " + invoice.emailStatus);
                                            invoice.shipDate = new DateTime(true, new Date().getTime(), 0);
                                            new QuickBooksApi.UpdateInvoiceTask(mApi) {
                                                @Override
                                                protected void onPostExecute(Boolean success) {
                                                    android.util.Log.e("MainActivity", "Updated successfully? " + success);
                                                }
                                            }.execute(invoice);

//                                            if (!"EmailSent".equals(invoice.emailStatus)) {
                                                new QuickBooksApi.SendInvoiceTask(mApi) {
                                                    @Override
                                                    protected void onPostExecute(Boolean success) {
                                                        android.util.Log.e("MainActivity", "Sent successfully? " + success);
                                                    }
                                                }.execute(invoice);
//                                            }
                                        }
                                    }
                                }.execute("SELECT * FROM Invoice WHERE DocNumber = '" + invoiceNo + "'");
                            } else {
                                mApi = null;
                                startActivity(new Intent(ProgressActivity.this, AuthenticationActivity.class));
                            }
                        }
                    }.execute();
//                } else {
//                    startActivity(new Intent(ProgressActivity.this, AuthenticationActivity.class));
//                }
                Toast.makeText(ProgressActivity.this, "Sent invoice " + invoiceNo, Toast.LENGTH_SHORT).show();
            }
        });
        ll.addView(btnSendInvoice);
        scroll.addView(ll);
        setContentView(scroll);
    }

    @Override
    protected void onResume() {
        super.onResume();
        QuickBooksApi.Authenticator authenticator = new QuickBooksApi.Authenticator(this, Constants.OAUTH_CONSUMER_KEY, Constants.OAUTH_CONSUMER_SECRET);
        mApi = authenticator.tryExistingCredentials();
        if (mApi != null) {
            new QuickBooksApi.CheckConnectionTask(mApi) {
                @Override
                protected void onPostExecute(Boolean success) {
                    if (Boolean.TRUE.equals(success)) {
                        // Retrieve invoice
                        new QuickBooksApi.QueryInvoicesTask(mApi) {
                            @Override
                            protected void onPostExecute(List<Invoice> invoices) {
                                if (invoices != null && !invoices.isEmpty()) {
                                    Invoice invoice = invoices.get(0);
                                    List<Invoice.Line> items = invoice.lines;

                                    TextView tvItemDesc = new TextView(ProgressActivity.this);
                                    tvItemDesc.setLayoutParams(tvLayoutParamsLeft);
                                    tvItemDesc.setPadding(8,8,8,8);
                                    tvItemDesc.setText("Item description");
                                    tvItemDesc.setTypeface(Typeface.DEFAULT_BOLD);
                                    ll.addView(tvItemDesc);

                                    TextView tvQty = new TextView(ProgressActivity.this);
                                    tvQty.setLayoutParams(tvLayoutParamsRight);
                                    tvQty.setPadding(8,8,8,8);
                                    tvQty.setText("Scanned / Quantity");
                                    tvQty.setTypeface(Typeface.DEFAULT_BOLD);
                                    ll.addView(tvQty);

                                    for (Invoice.Line item : items) {
                                        if ("SalesItemLineDetail".equals(item.detailType)) {
                                            TextView tvLineItem = new TextView(ProgressActivity.this);
                                            tvLineItem.setLayoutParams(tvLayoutParamsLeft);
                                            tvLineItem.setPadding(8,8,8,8);
                                            tvLineItem.setText(item.description);
                                            ll.addView(tvLineItem);

                                            TextView tvLineAmount = new TextView(ProgressActivity.this);
                                            tvLineAmount.setLayoutParams(tvLayoutParamsRight);
                                            tvLineAmount.setPadding(8,8,8,8);
                                            String[] quantities = mSavedInstanceState.getStringArray(item.description);
                                            String output = quantities[0] + " / " + quantities[1];
                                            tvLineAmount.setText(output);
                                            ll.addView(tvLineAmount);
                                        }
                                    }
                                }
                            }
                        }.execute("SELECT * FROM Invoice WHERE DocNumber = '" + invoiceNo + "'");
                    } else {
                        mApi = null;
                        startActivity(new Intent(ProgressActivity.this, AuthenticationActivity.class));
                    }
                }
            }.execute();
        } else {
            startActivity(new Intent(ProgressActivity.this, AuthenticationActivity.class));
        }
    }
}
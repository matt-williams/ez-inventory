package com.github.yinyee.locator;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.yinyee.locator.quickbooks.Constants;
import com.github.yinyee.locator.quickbooks.Invoice;
import com.github.yinyee.locator.quickbooks.Item;
import com.github.yinyee.locator.quickbooks.QuickBooksApi;
import com.google.api.client.util.DateTime;

import java.util.Date;
import java.util.List;

public class AuthenticationActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        final QuickBooksApi.Authenticator authenticator = new QuickBooksApi.Authenticator(this, Constants.OAUTH_CONSUMER_KEY, Constants.OAUTH_CONSUMER_SECRET);

        final WebView webview = new WebView(this);

        webview.getSettings().setJavaScriptEnabled(true);

        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (authenticator.isConfirmationUrl(Uri.parse(url))) {
                    setContentView(R.layout.activity_authentication);
                    new QuickBooksApi.Authenticator.AuthenticateTask(authenticator) {
                        @Override
                        protected void onPostExecute(final QuickBooksApi api) {
                            if (api != null) {
                                new QuickBooksApi.CheckConnectionTask(api) {
                                    @Override
                                    protected void onPostExecute(Boolean success) {
                                        if (!Boolean.TRUE.equals(success)) {
                                            Toast.makeText(AuthenticationActivity.this, "Quickbooks authentication failed", Toast.LENGTH_SHORT).show();
                                        }
                                        AuthenticationActivity.this.finish();
                                    }
                                }.execute();
                                /*
                                new QuickBooksApi.QueryItemsTask(api) {
                                    @Override
                                    protected void onPostExecute(List<Item> items) {
                                        android.util.Log.e("MainActivity", "Got " + items.size() + " items");
                                        android.util.Log.e("MainActivity", "First item " + items.get(0).description);
                                    }
                                }.execute("SELECT * FROM Item WHERE Type IN ('Inventory','NonInventory')");

                                new QuickBooksApi.GetInvoiceTask(api) {
                                    @Override
                                    protected void onPostExecute(Invoice invoice) {
                                        //android.util.Log.e("MainActivity", invoice.id + " - " + invoice.emailStatus);
                                    }
                                }.execute("1015");

                                new QuickBooksApi.QueryInvoicesTask(api) {
                                    @Override
                                    protected void onPostExecute(List<Invoice> invoices) {
                                        android.util.Log.e("MainActivity", "How many invoices? " + invoices.size());
                                        Invoice invoice = invoices.get(0);
                                        android.util.Log.e("MainActivity", invoice.id + " - " + invoice.lines.get(0).amount + " - " + invoice.emailStatus);
                                        invoice.shipDate = new DateTime(true, new Date().getTime(), 0);
                                        new QuickBooksApi.UpdateInvoiceTask(api) {
                                            @Override
                                            protected void onPostExecute(Boolean success) {
                                                android.util.Log.e("MainActivity", "Updated successfully? " + success);
                                            }
                                        }.execute(invoice);

                                        if (!"EmailSent".equals(invoice.emailStatus)) {
                                            new QuickBooksApi.SendInvoiceTask(api) {
                                                @Override
                                                protected void onPostExecute(Boolean success) {
                                                    android.util.Log.e("MainActivity", "Sent successfully? " + success);
                                                }
                                            }.execute(invoice);
                                        }
                                    }
                                }.execute("SELECT * FROM Invoice WHERE DocNumber = '" + "1015" + "'");
*/
                            }
                        }
                    }.execute(Uri.parse(url));
                    return true;
                } else {
                    return false;
                }
            }
        });

        new QuickBooksApi.Authenticator.GetAuthenticationURLTask(authenticator) {
            @Override
            protected void onPostExecute(String authenticationUrl) {
                webview.loadUrl(authenticationUrl);
                setContentView(webview);
            }
        }.execute();
    }
}

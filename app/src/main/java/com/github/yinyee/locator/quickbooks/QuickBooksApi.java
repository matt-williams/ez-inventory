package com.github.yinyee.locator.quickbooks;

import android.os.AsyncTask;

import com.google.api.client.auth.oauth.OAuthAuthorizeTemporaryTokenUrl;
import com.google.api.client.auth.oauth.OAuthCredentialsResponse;
import com.google.api.client.auth.oauth.OAuthGetAccessToken;
import com.google.api.client.auth.oauth.OAuthGetTemporaryToken;
import com.google.api.client.auth.oauth.OAuthHmacSigner;
import com.google.api.client.auth.oauth.OAuthParameters;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.jackson2.JacksonFactory;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

public class QuickBooksApi {
    private static final String TAG = "QuickBooksApi";
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();
    private static final HttpTransport HTTP_TRANSPORT = new ApacheHttpTransport();
    private static final String INVOICE_DEFAULT_QUERY = "SELECT * FROM Invoice";
    private static final String ITEM_DEFAULT_QUERY = "SELECT * FROM Item";

    private final HttpRequestFactory mRequestFactory;
    private final String mRealmId;

    public static class Authenticator {
        private static final String REQUEST_TOKEN_URL = "https://oauth.intuit.com/oauth/v1/get_request_token";
        private static final String AUTHORIZATION_URL = "https://appcenter.intuit.com/Connect/Begin";
        private static final String ACCESS_TOKEN_URL = "https://oauth.intuit.com/oauth/v1/get_access_token";

        private final String mConsumerKey;
        private final String mConsumerSecret;

        public Authenticator(String consumerKey, String consumerSecret) {
            mConsumerKey = consumerKey;
            mConsumerSecret = consumerSecret;
        }

        public QuickBooksApi authenticate() throws IOException {
            OAuthHmacSigner signer = new OAuthHmacSigner();
            signer.clientSharedSecret = Constants.OAUTH_CONSUMER_SECRET;

            // Get Temporary Token
            OAuthGetTemporaryToken getTemporaryToken = new OAuthGetTemporaryToken(REQUEST_TOKEN_URL);
            getTemporaryToken.signer = signer;
            getTemporaryToken.consumerKey = Constants.OAUTH_CONSUMER_KEY;
            getTemporaryToken.transport = HTTP_TRANSPORT;
            getTemporaryToken.callback = "http://www.example.com/";
            OAuthCredentialsResponse temporaryTokenResponse = null;
            temporaryTokenResponse = getTemporaryToken.execute();

            // Build Authenticate URL
            OAuthAuthorizeTemporaryTokenUrl accessTempToken = new OAuthAuthorizeTemporaryTokenUrl(AUTHORIZATION_URL);
            accessTempToken.temporaryToken = temporaryTokenResponse.token;
            String authUrl = accessTempToken.build();

            // Redirect to Authenticate URL in order to get Verifier Code
            android.util.Log.e("Invoice", authUrl);
/*
            // Get Access Token using Temporary token and Verifier Code
            OAuthGetAccessToken getAccessToken = new OAuthGetAccessToken(ACCESS_TOKEN_URL);
            getAccessToken.signer = signer;
            getAccessToken.temporaryToken = temporaryTokenResponse.token;
            getAccessToken.transport = HTTP_TRANSPORT;
            getAccessToken.verifier = "VERIFIER_CODE";
            getAccessToken.consumerKey = Constants.OAUTH_CONSUMER_KEY;
            OAuthCredentialsResponse accessTokenResponse = getAccessToken.execute();

            return new QuickBooksApi(mConsumerKey, mConsumerSecret, accessTokenResponse.token, accessTokenResponse.tokenSecret);
*/
            return new QuickBooksApi(mConsumerKey, mConsumerSecret, Constants.ACCESS_TOKEN, Constants.ACCESS_TOKEN_SECRET, Constants.REALM_ID);
        }

        public static class AuthenticateTask extends AsyncTask<Authenticator, Void, QuickBooksApi> {
            @Override
            protected QuickBooksApi doInBackground(Authenticator... authenticators) {
                try {
                    return authenticators[0].authenticate();
                } catch (IOException e) {
                    android.util.Log.e(TAG, "Caught " + e.getClass().getName() + ": " + e.getMessage());
                    return null;
                }
            }
        }
    }

    private QuickBooksApi(String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret, String realmId) {
        OAuthHmacSigner signer = new OAuthHmacSigner();
        signer.clientSharedSecret = consumerSecret;
        signer.tokenSharedSecret = accessTokenSecret;

        OAuthParameters oauthParameters = new OAuthParameters();
        oauthParameters.signer = signer;
        oauthParameters.consumerKey = consumerKey;
        oauthParameters.token = accessToken;
        oauthParameters.verifier = "VERIFIER_CODE";

        mRequestFactory = HTTP_TRANSPORT.createRequestFactory(oauthParameters);
        mRealmId = realmId;
    }

    private HttpResponse get(String url) throws IOException {
        GenericUrl genericUrl = new GenericUrl(url);
        HttpRequest request = mRequestFactory.buildGetRequest(genericUrl);
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept("application/json");
        request.setHeaders(headers);
        HttpResponse response = request.execute();
        if (!response.isSuccessStatusCode()) {
            android.util.Log.e(TAG, "POST " + url + " failed: " + response.getStatusCode() + " " + response.getStatusMessage());
        }
        return response;
    }

    private HttpResponse post(String url) throws IOException {
        GenericUrl genericUrl = new GenericUrl(url);
        HttpRequest request = mRequestFactory.buildPostRequest(genericUrl, null);
        HttpResponse response = request.execute();
        if (!response.isSuccessStatusCode()) {
            android.util.Log.e(TAG, "POST " + url + " failed: " + response.getStatusCode() + " " + response.getStatusMessage());
        }
        return response;
    }

    private HttpResponse post(String url, Object object) throws IOException {
        GenericUrl genericUrl = new GenericUrl(url);
        HttpRequest request = mRequestFactory.buildPostRequest(genericUrl, new JsonHttpContent(JSON_FACTORY, object));
        request.getContent().writeTo(System.out);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType("application/json");
        request.setHeaders(headers);
        HttpResponse response = request.execute();
        if (!response.isSuccessStatusCode()) {
            android.util.Log.e(TAG, "POST " + url + " failed: " + response.getStatusCode() + " " + response.getStatusMessage());
        }
        return response;
    }

    private <T> T getAsJson(String url, Class<T> cls) throws IOException {
        String json = get(url).parseAsString();
        try {
            return JSON_FACTORY.createJsonParser(json).parse(cls);
        } catch (RuntimeException e) {
            android.util.Log.e(TAG, "Caught " + e.getClass().getName() + ": " + e.getMessage() + " while processing " + json);
            return null;
        }
    }

    public List<Invoice> queryInvoices() throws EncoderException, IOException {
        return queryInvoices(INVOICE_DEFAULT_QUERY);
    }

    public List<Invoice> queryInvoices(String query) throws EncoderException, IOException {
        Invoice.QueryResponseWrapper queryResponseWrapper = getAsJson(Constants.BASE_URL + "/v3/company/" + mRealmId + "/query?query=" + new URLCodec().encode(query) + "&minorversion=4", Invoice.QueryResponseWrapper.class);
        return ((queryResponseWrapper != null) && (queryResponseWrapper.queryResponse != null)) ? queryResponseWrapper.queryResponse.invoices : null;
    }

    public Invoice getInvoice(String invoiceId) throws IOException {
        Invoice.Wrapper wrapper = getAsJson(Constants.BASE_URL + "/v3/company/" + mRealmId + "/invoice/" + invoiceId + "?minorversion=4", Invoice.Wrapper.class);
        return (wrapper != null) ? wrapper.invoice : null;
    }

    public boolean updateInvoice(Invoice invoice) throws IOException {
        return post(Constants.BASE_URL + "/v3/company/" + mRealmId + "/invoice?&minorversion=4", invoice).isSuccessStatusCode();
    }

    public boolean sendInvoice(Invoice invoice) throws IOException {
        return sendInvoice(invoice.id);
    }

    public boolean sendInvoice(String invoiceId) throws IOException {
        return post(Constants.BASE_URL + "/v3/company/" + mRealmId + "/invoice/" + invoiceId + "/send?minorversion=4").isSuccessStatusCode();
    }

    public List<Item> queryItems() throws EncoderException, IOException {
        return queryItems(ITEM_DEFAULT_QUERY);
    }

    public List<Item> queryItems(String query) throws EncoderException, IOException {
        Item.QueryResponseWrapper queryResponseWrapper = getAsJson(Constants.BASE_URL + "/v3/company/" + mRealmId + "/query?query=" + new URLCodec().encode(query) + "&minorversion=4", Item.QueryResponseWrapper.class);
        return ((queryResponseWrapper != null) && (queryResponseWrapper.queryResponse != null)) ? queryResponseWrapper.queryResponse.items : null;
    }

    public static class QueryInvoicesTask extends AsyncTask<String, Void, List<Invoice>> {
        private final QuickBooksApi mApi;

        public QueryInvoicesTask(QuickBooksApi api) {
            mApi = api;
        }

        public void execute() {
            execute(INVOICE_DEFAULT_QUERY);
        }

        @Override
        protected List<Invoice> doInBackground(String... queries) {
            try {
                return mApi.queryInvoices(queries[0]);
            } catch (EncoderException e) {
                android.util.Log.e(TAG, "Caught " + e.getClass().getName() + ": " + e.getMessage());
                return null;
            } catch (IOException e) {
                android.util.Log.e(TAG, "Caught " + e.getClass().getName() + ": " + e.getMessage());
                return null;
            }
        }
    }

    public static class GetInvoiceTask extends AsyncTask<String, Void, Invoice> {
        private final QuickBooksApi mApi;

        public GetInvoiceTask(QuickBooksApi api) {
            mApi = api;
        }

        @Override
        protected Invoice doInBackground(String... invoiceIds) {
            try {
                return mApi.getInvoice(invoiceIds[0]);
            } catch (IOException e) {
                android.util.Log.e(TAG, "Caught " + e.getClass().getName() + ": " + e.getMessage());
                return null;
            }
        }
    }

    public static class UpdateInvoiceTask extends AsyncTask<Invoice, Void, Boolean> {
        private final QuickBooksApi mApi;

        public UpdateInvoiceTask(QuickBooksApi api) {
            mApi = api;
        }

        @Override
        protected Boolean doInBackground(Invoice... invoices) {
            try {
                return mApi.updateInvoice(invoices[0]);
            } catch (IOException e) {
                android.util.Log.e(TAG, "Caught " + e.getClass().getName() + ": " + e.getMessage());
                return null;
            }
        }
    }

    public static class SendInvoiceTask extends AsyncTask<String, Void, Boolean> {
        private final QuickBooksApi mApi;

        public SendInvoiceTask(QuickBooksApi api) {
            mApi = api;
        }

        public void execute(Invoice invoice) {
            execute(invoice.id);
        }

        @Override
        protected Boolean doInBackground(String... invoiceIds) {
            try {
                return mApi.sendInvoice(invoiceIds[0]);
            } catch (IOException e) {
                android.util.Log.e(TAG, "Caught " + e.getClass().getName() + ": " + e.getMessage());
                return null;
            }
        }
    }

    public static class QueryItemsTask extends AsyncTask<String, Void, List<Item>> {
        private final QuickBooksApi mApi;

        public QueryItemsTask(QuickBooksApi api) {
            mApi = api;
        }

        public void execute() {
            execute(ITEM_DEFAULT_QUERY);
        }

        @Override
        protected List<Item> doInBackground(String... queries) {
            try {
                return mApi.queryItems(queries[0]);
            } catch (EncoderException e) {
                android.util.Log.e(TAG, "Caught " + e.getClass().getName() + ": " + e.getMessage());
                return null;
            } catch (IOException e) {
                android.util.Log.e(TAG, "Caught " + e.getClass().getName() + ": " + e.getMessage());
                return null;
            }
        }
    }
}

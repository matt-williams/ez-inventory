package com.github.yinyee.locator.quickbooks;

import android.os.AsyncTask;

import com.google.api.client.auth.oauth.OAuthCredentialsResponse;
import com.google.api.client.auth.oauth.OAuthHmacSigner;
import com.google.api.client.auth.oauth.OAuthParameters;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.IOException;
import java.util.List;

public class QuickBooksApi {
    private static final String TAG = "QuickBooksApi";
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();

    private final HttpRequestFactory mRequestFactory;

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

        public QuickBooksApi authenticate() {
/*
            OAuthHmacSigner signer = new OAuthHmacSigner();
            signer.clientSharedSecret = Constants.OAUTH_CONSUMER_SECRET;
            HttpTransport transport = new ApacheHttpTransport();

            // Get Temporary Token
            OAuthGetTemporaryToken getTemporaryToken = new OAuthGetTemporaryToken(REQUEST_TOKEN_URL);
            getTemporaryToken.signer = signer;
            getTemporaryToken.consumerKey = Constants.OAUTH_CONSUMER_KEY;
            getTemporaryToken.transport = transport;
            getTemporaryToken.callback = "http://www.example.com/";
            OAuthCredentialsResponse temporaryTokenResponse = null;
            temporaryTokenResponse = getTemporaryToken.execute();

            // Build Authenticate URL
            OAuthAuthorizeTemporaryTokenUrl accessTempToken = new OAuthAuthorizeTemporaryTokenUrl(AUTHORIZATION_URL);
            accessTempToken.temporaryToken = temporaryTokenResponse.token;
            String authUrl = accessTempToken.build();

            // Redirect to Authenticate URL in order to get Verifier Code
            android.util.Log.e("Invoice", authUrl);

            // Get Access Token using Temporary token and Verifier Code
            OAuthGetAccessToken getAccessToken = new OAuthGetAccessToken(ACCESS_TOKEN_URL);
            getAccessToken.signer = signer;
            getAccessToken.temporaryToken = temporaryTokenResponse.token;
            getAccessToken.transport = transport;
            getAccessToken.verifier = "VERIFIER_CODE";
            getAccessToken.consumerKey = Constants.OAUTH_CONSUMER_KEY;
            OAuthCredentialsResponse accessTokenResponse = getAccessToken.execute();

            return new QuickBooksApi(mConsumerKey, mConsumerSecret, accessTokenResponse.token, accessTokenResponse.tokenSecret);
*/
            return new QuickBooksApi(mConsumerKey, mConsumerSecret, Constants.ACCESS_TOKEN, Constants.ACCESS_TOKEN_SECRET);
        }
    }

    public QuickBooksApi(String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret) {
        OAuthHmacSigner signer = new OAuthHmacSigner();
        signer.clientSharedSecret = consumerSecret;
        signer.tokenSharedSecret = accessTokenSecret;

        OAuthParameters oauthParameters = new OAuthParameters();
        oauthParameters.signer = signer;
        oauthParameters.consumerKey = consumerKey;
        oauthParameters.token = accessToken;
        oauthParameters.verifier = "VERIFIER_CODE";

        HttpTransport httpTransport = new ApacheHttpTransport();
        mRequestFactory = httpTransport.createRequestFactory(oauthParameters);
    }

    private HttpResponse get(String url) throws IOException {
        GenericUrl genericUrl = new GenericUrl(url);
        HttpRequest request = mRequestFactory.buildGetRequest(genericUrl);
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept("application/json");
        request.setHeaders(headers);
        return request.execute();
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

    public List<Invoice> queryInvoices(String realmId) throws IOException {
        Invoice.QueryResponseWrapper queryResponseWrapper = getAsJson(Constants.BASE_URL + "/v3/company/" + realmId + "/query?query=" + "SELECT%20%2A%20FROM%20Invoice&minorversion=4", Invoice.QueryResponseWrapper.class);
        return ((queryResponseWrapper != null) && (queryResponseWrapper.queryResponse != null)) ? queryResponseWrapper.queryResponse.invoices : null;
    }

    public Invoice getInvoice(String realmId, String invoiceId) throws IOException {
        Invoice.Wrapper wrapper = getAsJson(Constants.BASE_URL + "/v3/company/" + realmId + "/invoice/" + invoiceId + "?minorversion=4", Invoice.Wrapper.class);
        return (wrapper != null) ? wrapper.invoice : null;
    }

    public static class QueryInvoicesTask extends AsyncTask<Void, Void, List<Invoice>> {
        private final QuickBooksApi mApi;
        private final String mRealmId;

        public QueryInvoicesTask(QuickBooksApi api, String realmId) {
            mApi = api;
            mRealmId = realmId;
        }

        @Override
        protected List<Invoice> doInBackground(Void... voids) {
            try {
                return mApi.queryInvoices(mRealmId);
            } catch (IOException e) {
                return null;
            }
        }
    }

    public static class GetInvoiceTask extends AsyncTask<String, Void, Invoice> {
        private final QuickBooksApi mApi;
        private final String mRealmId;

        public GetInvoiceTask(QuickBooksApi api, String realmId) {
            mApi = api;
            mRealmId = realmId;
        }

        @Override
        protected Invoice doInBackground(String... invoiceIds) {
            try {
                return mApi.getInvoice(mRealmId, invoiceIds[0]);
            } catch (IOException e) {
                return null;
            }
        }
    }
}

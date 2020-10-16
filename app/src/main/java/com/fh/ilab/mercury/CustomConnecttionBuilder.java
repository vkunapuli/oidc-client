package com.fh.ilab.mercury;

import android.net.SSLCertificateSocketFactory;
import android.net.Uri;

import net.openid.appauth.Preconditions;
import net.openid.appauth.connectivity.ConnectionBuilder;

import org.apache.http.conn.ssl.AllowAllHostnameVerifier;

import java.io.IOException;
import java.net.HttpURLConnection;
import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;
//import android.support.annotation.NonNull;



public final class CustomConnecttionBuilder implements ConnectionBuilder {

    /**
     * The singleton instance of the default connection builder.
     */
    //public static final net.openid.appauth.connectivity.DefaultConnectionBuilder INSTANCE = new net.openid.appauth.connectivity.DefaultConnectionBuilder();

    private static final int CONNECTION_TIMEOUT_MS = (int) TimeUnit.SECONDS.toMillis(15);
    private static final int READ_TIMEOUT_MS = (int) TimeUnit.SECONDS.toMillis(10);

    private static final String HTTPS_SCHEME = "https";

    public CustomConnecttionBuilder() {
        // no need to construct instances of this type
    }


    @Override
    public HttpsURLConnection openConnection(Uri uri) throws IOException {
        Preconditions.checkNotNull(uri, "url must not be null");
        Preconditions.checkArgument(HTTPS_SCHEME.equals(uri.getScheme()),
                "only https connections are permitted");
        HttpsURLConnection conn = (HttpsURLConnection) new URL(uri.toString()).openConnection();

        conn.setConnectTimeout(CONNECTION_TIMEOUT_MS);
        conn.setSSLSocketFactory(SSLCertificateSocketFactory.getInsecure(0, null));
        conn.setHostnameVerifier(new AllowAllHostnameVerifier());
        conn.setReadTimeout(READ_TIMEOUT_MS);
        conn.setInstanceFollowRedirects(false);
        return conn;
    }
}
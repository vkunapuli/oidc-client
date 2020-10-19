package com.sssystems.oidc.client;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.SSLCertificateSocketFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;

import net.openid.appauth.AppAuthConfiguration;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ClientAuthentication;
import net.openid.appauth.ClientSecretBasic;
import net.openid.appauth.ResponseTypeValues;
import net.openid.appauth.TokenRequest;
import net.openid.appauth.TokenResponse;
import net.openid.appauth.connectivity.ConnectionBuilder;
import net.openid.appauth.connectivity.DefaultConnectionBuilder;

import org.apache.http.conn.ssl.AllowAllHostnameVerifier;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.net.ssl.HttpsURLConnection;


public class AuthServiceActivity extends AppCompatActivity {

    private String issuer;
    private HashMap serviceConfiguration;
    private String  redirectUrl;
    private String  clientId;
    private String[] scopes;
    private HashMap  additionalParameterMap;
    private Boolean dangerouslyAllowInsecureHttpRequests;
    private String clientSecret;
    private String refreshToken;
    private AuthorizationService authService = null;
    private GoogleSignInClient mGoogleSignInClient;
    private String logout =  "https://accounts.google.com/Logout?continue=http://google.com";

    Thread thread = new Thread(new Runnable() {

        @Override
        public void run() {
            try  {
                Log.d("LOGOUT", "log out start");
                URL url = new URL("https://www.ilabfhlmc.tk:9031/idp/startSLO.ping");
                HttpsURLConnection connection =(HttpsURLConnection)
                        url.openConnection();
                connection.setSSLSocketFactory(SSLCertificateSocketFactory.getInsecure(0, null));
                connection.setHostnameVerifier(new AllowAllHostnameVerifier());
                //HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(connection.getInputStream());
                Log.d("LOGOUT", readStream(in));
                Log.d("LOGOUT", "log out done");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });
    private String readStream(InputStream is) {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            int i = is.read();
            while(i != -1) {
                bo.write(i);
                i = is.read();
            }
            return bo.toString();
        } catch (Exception e) {
            return "failed";
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_service);

        dangerouslyAllowInsecureHttpRequests = true;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Bundle bundle = getIntent().getExtras();
            issuer =  bundle.getString("issuer");
            clientId = bundle.getString("clientId");
            scopes  = bundle.getString("scopes").split(",");
            clientSecret = bundle.getString("clientSecret");
            redirectUrl = bundle.getString("redirectUrl");

            additionalParameterMap =  new HashMap<String, String>();
            String actionType =  bundle.getString("action");
            if(actionType.contains("authorize")) {
                authorize();
            }
            else if(actionType.contains("revoke")){
                Intent returnIntent = new Intent();
                Log.e("ERROR", "rvoke");
                ///idp/startSLO.ping
                thread.start();
                setResult(Activity.RESULT_CANCELED, returnIntent);
                finish();
            }
            else if(actionType.contains("refresh"))
            {
                refreshToken =  bundle.getString("refreshToken");
                refresh();
            }
            else {
                Intent returnIntent = new Intent();
                Log.e("ERROR", "bad command" );
                setResult(Activity.RESULT_CANCELED, returnIntent);
                finish();
            }
        } else {
            Intent returnIntent = new Intent();
            Log.e("ERROR", "extras null" );
            setResult(Activity.RESULT_CANCELED, returnIntent);
            finish();
        }
    }

    public void authorize()
    {
        final ConnectionBuilder builder = createConnectionBuilder(dangerouslyAllowInsecureHttpRequests);
        final AppAuthConfiguration appAuthConfiguration = this.createAppAuthConfiguration(builder);
        //additionalParameterMap =  new HashMap<String, String>();
        if (clientSecret != null) {
            additionalParameterMap.put("client_secret", clientSecret);
        }
        //prompt options: login, select_account, consent, none
        // additionalParameterMap.put("prompt", "login");
        //additionalParameterMap.put("login_hint", "login@www.example.com");

        // when serviceConfiguration is provided, we don't need to hit up the OpenID well-known id endpoint
        if (serviceConfiguration != null) {
            try {
                authorizeWithConfiguration(
                        createAuthorizationServiceConfiguration(serviceConfiguration),
                        appAuthConfiguration,
                        clientId,
                        scopes,
                        redirectUrl,
                        additionalParameterMap
                );
            } catch (Exception e) {
                Log.e("ERROR", "auth exception possible network error 2" + e.getLocalizedMessage());
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, returnIntent);
                finish();
            }
        } else {
            final Uri issuerUri = Uri.parse(issuer);
            AuthorizationServiceConfiguration.fetchFromUrl(
                    buildConfigurationUriFromIssuer(issuerUri),
                    new AuthorizationServiceConfiguration.RetrieveConfigurationCallback() {
                        public void onFetchConfigurationCompleted(
                                AuthorizationServiceConfiguration fetchedConfiguration,
                                AuthorizationException ex) {
                            if (ex != null) {
                                Log.e("ERROR", "auth exception possible network error 1." + ex.errorDescription);
                                //Toast.makeText(AuthServiceActivity.this, "Can not access Auth Server, check network " , Toast.LENGTH_SHORT).show();
                                Intent returnIntent = new Intent();
                                setResult(Activity.RESULT_CANCELED, returnIntent);
                                finish();
                                return;
                            }
                            authorizeWithConfiguration(
                                    fetchedConfiguration,
                                    appAuthConfiguration,
                                    clientId,
                                    scopes,
                                    redirectUrl,
                                    additionalParameterMap
                            );
                        }
                    },
                    builder
            );
        }
    }
    public void refresh()
    {
        final ConnectionBuilder builder = createConnectionBuilder(dangerouslyAllowInsecureHttpRequests);
        final AppAuthConfiguration appAuthConfiguration = createAppAuthConfiguration(builder);

        if (clientSecret != null) {
            additionalParameterMap.put("client_secret", clientSecret);
        }
        // when serviceConfiguration is provided, we don't need to hit up the OpenID well-known id endpoint
        if (serviceConfiguration != null) {
            try {
                refreshWithConfiguration(
                        createAuthorizationServiceConfiguration(serviceConfiguration),
                        appAuthConfiguration,
                        refreshToken,
                        clientId,
                        scopes,
                        redirectUrl,
                        additionalParameterMap,
                        clientSecret
                );
            } catch (Exception e) {
                Log.e("ERROR", "auth exception possible network error 5" + e.getLocalizedMessage());
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, returnIntent);
                finish();
            }
        } else {
            final Uri issuerUri = Uri.parse(issuer);
            // @TODO: Refactor to avoid hitting IDP endpoint on refresh, reuse fetchedConfiguration if possible.
            AuthorizationServiceConfiguration.fetchFromUrl(
                    buildConfigurationUriFromIssuer(issuerUri),
                    new AuthorizationServiceConfiguration.RetrieveConfigurationCallback() {
                        public void onFetchConfigurationCompleted(
                                AuthorizationServiceConfiguration fetchedConfiguration,
                                AuthorizationException ex) {
                            if (ex != null) {
                                Log.e("ERROR", "auth exception possible network error 6" +ex.errorDescription);
                                Intent returnIntent = new Intent();
                                setResult(Activity.RESULT_CANCELED, returnIntent);
                                finish();
                                return;
                            }
                            refreshWithConfiguration(
                                    fetchedConfiguration,
                                    appAuthConfiguration,
                                    refreshToken,
                                    clientId,
                                    scopes,
                                    redirectUrl,
                                    additionalParameterMap,
                                    clientSecret
                            );
                        }
                    },
                    builder);
        }

    }

    /*
     * Authorize user with the provided configuration
     */
    private void authorizeWithConfiguration(
            final AuthorizationServiceConfiguration serviceConfiguration,
            final AppAuthConfiguration appAuthConfiguration,
            final String clientId,
            final String[] scopes,
            final String redirectUrl,
            final Map<String, String> additionalParameterMap
    ) {

        String scopesString = null;

        if (scopes != null) {
            scopesString = this.arrayToString(scopes);
        }

        //final Context context = this.reactContext;
        final Activity currentActivity = this;

        AuthorizationRequest.Builder authRequestBuilder =
                new AuthorizationRequest.Builder(
                        serviceConfiguration,
                        clientId,
                        ResponseTypeValues.CODE,
                        Uri.parse(redirectUrl)
                );

        if (scopesString != null) {
            authRequestBuilder.setScope(scopesString);
        }
        authRequestBuilder.setPrompt("login");
        //authRequestBuilder.setLoginHint("vkunapuli@gmail.com");
        if (additionalParameterMap != null) {
            // handle additional parameters separately to avoid exceptions from AppAuth
            if (additionalParameterMap.containsKey("display")) {
                authRequestBuilder.setDisplay(additionalParameterMap.get("display"));
                additionalParameterMap.remove("display");
            }
            if (additionalParameterMap.containsKey("login_hint")) {
                authRequestBuilder.setLoginHint(additionalParameterMap.get("login_hint"));
                additionalParameterMap.remove("login_hint");
            }
            if (additionalParameterMap.containsKey("prompt")) {
                authRequestBuilder.setPrompt(additionalParameterMap.get("prompt"));
                additionalParameterMap.remove("prompt");
            }

            authRequestBuilder.setAdditionalParameters(additionalParameterMap);
        }

        AuthorizationRequest authRequest = authRequestBuilder.build();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            if(authService == null)
                authService = new AuthorizationService(getBaseContext(), appAuthConfiguration);
            Intent authIntent = authService.getAuthorizationRequestIntent(authRequest);

            currentActivity.startActivityForResult(authIntent, 0);
        } else {
            if(authService == null)
                authService = new AuthorizationService(currentActivity, appAuthConfiguration);
            PendingIntent pendingIntent = currentActivity.createPendingResult(0, new Intent(), 0);

            authService.performAuthorizationRequest(authRequest, pendingIntent);
        }
    }

    /*
     * Refresh authentication token with the provided configuration
     */
    private void refreshWithConfiguration(
            final AuthorizationServiceConfiguration serviceConfiguration,
            final AppAuthConfiguration appAuthConfiguration,
            final String refreshToken,
            final String clientId,
            final String[] scopes,
            final String redirectUrl,
            final Map<String, String> additionalParameterMap,
            final String clientSecret
    ) {
        String scopesString = null;

        if (scopes != null) {
            scopesString = this.arrayToString(scopes);
        }
        TokenRequest.Builder tokenRequestBuilder =
                new TokenRequest.Builder(
                        serviceConfiguration,
                        clientId
                )
                        .setRefreshToken(refreshToken)
                        .setRedirectUri(Uri.parse(redirectUrl));

        if (scopesString != null) {
            tokenRequestBuilder.setScope(scopesString);
        }

        if (additionalParameterMap != null){
            tokenRequestBuilder.setAdditionalParameters(additionalParameterMap);
        }

        TokenRequest tokenRequest = tokenRequestBuilder.build();

        authService = new AuthorizationService(getBaseContext(), appAuthConfiguration);
        AuthorizationService.TokenResponseCallback tokenResponseCallback = new AuthorizationService.TokenResponseCallback() {
            @Override
            public void onTokenRequestCompleted(TokenResponse response, AuthorizationException ex) {
                if (response != null) {
                    Bundle map = tokenResponseToMap(response);
                    Intent returnIntent = new Intent();
                    returnIntent.putExtras(map);
                    setResult(Activity.RESULT_OK,returnIntent);
                    finish();
                } else {
                    Intent returnIntent = new Intent();
                    Log.e("ERROR", ex.errorDescription );
                    setResult(Activity.RESULT_CANCELED, returnIntent);
                    finish();
                }
            }
        };
        if (clientSecret != null) {
            ClientAuthentication clientAuth = new ClientSecretBasic(clientSecret);
            authService.performTokenRequest(tokenRequest, clientAuth, tokenResponseCallback);

        } else {
            authService.performTokenRequest(tokenRequest, tokenResponseCallback);
        }
    }

    /*
     * Create a space-delimited string from an array
     */
    private String arrayToString(String[] array) {
        StringBuilder strBuilder = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (i != 0) {
                strBuilder.append(' ');
            }
            strBuilder.append(array[i]);
        }
        return strBuilder.toString();
    }

    /*
     * Read raw token response into a React Native map to be passed down the bridge
     */
    private Bundle tokenResponseToMap(TokenResponse response) {
        Bundle map = new Bundle();
        map.putString("accessToken", response.accessToken);
        if (response.accessTokenExpirationTime != null) {
            Date expirationDate = new Date(response.accessTokenExpirationTime);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            String expirationDateString = formatter.format(expirationDate);
            map.putString("accessTokenExpirationDate", expirationDateString);
        }
        HashMap additionalParameterMap = new HashMap<String, String>();
        if (!response.additionalParameters.isEmpty()) {
            Iterator<String> iterator = response.additionalParameters.keySet().iterator();
            while(iterator.hasNext()) {
                String key = iterator.next();
                additionalParameterMap.put(key, response.additionalParameters.get(key));
            }
        }
        if(response.idToken != null) {
            map.putString("idToken", response.idToken);
            Log.d("token", response.idToken);
        }
        if(response.accessToken != null) {

            Log.d("accessToken", response.accessToken);
        }
        if(response.refreshToken != null) {
            Log.d("refreshToken", response.refreshToken);
            map.putString("refreshToken", response.refreshToken);
        }
        map.putString("tokenType", response.tokenType);
        Log.d("RESPONSE", response.jsonSerializeString());

        return map;
    }

    /*
     * Create an App Auth configuration using the provided connection builder
     */
    private AppAuthConfiguration createAppAuthConfiguration(ConnectionBuilder connectionBuilder) {
        return new AppAuthConfiguration
                .Builder()
                .setConnectionBuilder(connectionBuilder)
                .build();
    }

    /*
     *  Create appropriate connection builder based on provided settings
     */
    private ConnectionBuilder createConnectionBuilder(Boolean allowInsecureConnections) {
        if (allowInsecureConnections.equals(true)) {
            return new CustomConnecttionBuilder();
        }

        return DefaultConnectionBuilder.INSTANCE;
    }

    /*
     *  Replicated private method from AuthorizationServiceConfiguration
     */
    private Uri buildConfigurationUriFromIssuer(Uri openIdConnectIssuerUri) {
        return openIdConnectIssuerUri.buildUpon()
                .appendPath(AuthorizationServiceConfiguration.WELL_KNOWN_PATH)
                .appendPath(AuthorizationServiceConfiguration.OPENID_CONFIGURATION_RESOURCE)
                .build();
    }

    private AuthorizationServiceConfiguration createAuthorizationServiceConfiguration(HashMap serviceConfiguration) throws Exception {
        if (!serviceConfiguration.containsKey("authorizationEndpoint")) {
            throw new Exception("serviceConfiguration passed without an authorizationEndpoint");
        }

        if (!serviceConfiguration.containsKey("tokenEndpoint")) {
            throw new Exception("serviceConfiguration passed without a tokenEndpoint");
        }

        Uri authorizationEndpoint = Uri.parse((String)serviceConfiguration.get("authorizationEndpoint"));
        Uri tokenEndpoint = Uri.parse((String)serviceConfiguration.get("tokenEndpoint"));
        Uri registrationEndpoint = null;
        if (serviceConfiguration.containsKey("registrationEndpoint")) {
            registrationEndpoint = Uri.parse((String)serviceConfiguration.get("registrationEndpoint"));
        }

        return new AuthorizationServiceConfiguration(
                authorizationEndpoint,
                tokenEndpoint,
                registrationEndpoint
        );
    }
    /*
     * Called when the OAuth browser activity completes
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            AuthorizationResponse response = AuthorizationResponse.fromIntent(data);
            // Log.d("accessToken", response.accessToken);f
            //Log.d("idToken", response.idToken);
            Log.d("response results", response.jsonSerializeString());
            AuthorizationException exception = AuthorizationException.fromIntent(data);
            if (exception != null) {
                Log.e("ERROR", "auth exception possible network error 34" + exception + response);
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, returnIntent);
                finish();
                return;
            }
            final AppAuthConfiguration configuration = createAppAuthConfiguration(
                    createConnectionBuilder(this.dangerouslyAllowInsecureHttpRequests)
            );
            if(authService == null)
                authService = new AuthorizationService(this, configuration);
            final HashMap addtionalMap = additionalParameterMap;
            TokenRequest tokenRequest = response.createTokenExchangeRequest(addtionalMap);

            AuthorizationService.TokenResponseCallback tokenResponseCallback = new AuthorizationService.TokenResponseCallback() {

                @Override
                public void onTokenRequestCompleted(
                        TokenResponse resp, AuthorizationException ex) {
                    if (resp != null) {
                        final Bundle map = tokenResponseToMap(resp);
                        Intent returnIntent = new Intent();
                        returnIntent.putExtras(map);
                        Log.e("OK", map.toString() );
                        setResult(Activity.RESULT_OK,returnIntent);
                        finish();
                    } else {
                        Log.e("ERROR", ex.errorDescription );
                        Intent returnIntent = new Intent();
                        setResult(Activity.RESULT_CANCELED, returnIntent);
                        finish();
                    }
                }
            };

            if (this.clientSecret != null) {
                ClientAuthentication clientAuth = new ClientSecretBasic(this.clientSecret);
                authService.performTokenRequest(tokenRequest, clientAuth, tokenResponseCallback);

            } else {
                authService.performTokenRequest(tokenRequest, tokenResponseCallback);
            }

        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(authService != null) {
            authService.dispose();
            authService = null;
        }
    }

}

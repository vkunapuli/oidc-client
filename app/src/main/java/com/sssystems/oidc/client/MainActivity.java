package com.sssystems.oidc.client;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.SSLCertificateSocketFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.apache.http.conn.ssl.AllowAllHostnameVerifier;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    private Bundle authConfig;
    private Bundle authState;

    private Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //clearCookies();
        login = findViewById(R.id.login);
    }

    public void alertOneButton(String title, String body) {

        new AlertDialog.Builder(MainActivity.this)
                .setTitle(title)
                .setMessage(body)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();
                    }
                }).show();
    }
    public void toastOnButton(String message) {
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onResume(){
        super.onResume();
    }
    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navmenu, menu);
        if(authState != null && authState.getString("accessToken") != null) {
            menu.getItem(1).setTitle("Log out");
            menu.getItem(1).setIcon(R.drawable.log_out);

        } else {
            menu.getItem(1).setTitle("Log in");
            menu.getItem(1).setIcon(R.drawable.log_in);
        }
        Drawable drawable = menu.getItem(1).getIcon();
        if(drawable != null) {
            drawable.mutate();
            drawable.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        }
        Drawable drawable1 = menu.getItem(0).getIcon();
        if(drawable1 != null) {
            drawable1.mutate();
            drawable1.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        }


        return super.onCreateOptionsMenu(menu);
    }
    public void login(View view)
    {
        if (authState != null && authState.getString("accessToken") != null) {
            authConfig.putString("action", "revoke");
            Intent i = new Intent(this, AuthServiceActivity.class);
            i.putExtras(authConfig);
            startActivityForResult(i, 2);
        } else {
            String issuer = Helper.getConfigValue(this, "issuer");
            String clientId = Helper.getConfigValue(this, "clientID");
            setupAuthConfigPING(issuer, clientId);
            Intent i = new Intent(this, AuthServiceActivity.class);
            authConfig.putString("action", "authorize");
            i.putExtras(authConfig);
            startActivityForResult(i, 2);
        }
    }
    private void setupAuthConfigPING(String issuer, String clientId)
    {
        authConfig = new Bundle();
        authConfig.putString("issuer",       issuer);
        authConfig.putString("clientId",     clientId);//googleoidc, pingclient
        authConfig.putString("redirectUrl",  Helper.getConfigValue(this, "redirectUrl"));
        authConfig.putString("scopes",       Helper.getConfigValue(this, "scopes"));
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.mybutton) {

        }
        if(id == R.id.refresh) {

        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 2) {
            if(resultCode == Activity.RESULT_OK){
                authState = data.getExtras();
                Log.d("test", authState.toString());

                Intent intent = new Intent(getBaseContext(), ResultsActivity.class);
                authConfig.putString("accessToken", authState.getString("accessToken"));
                intent.putExtras(authConfig);
                startActivityForResult(intent, 3);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                authState = null;
                //Write your code if there's no result
            }
        }
        if(requestCode == 3) {
            authConfig = data.getExtras();
            Log.d("LOG", authConfig.toString());
            authConfig.putString("action", "revoke");
            Intent i = new Intent(this, AuthServiceActivity.class);
            i.putExtras(authConfig);
            startActivityForResult(i, 2);

        }
    }
    class HttpAyncRequest extends AsyncTask<String, Void, String> {

        private Exception exception;
        private Boolean isAuth;
        public HttpAyncRequest (Boolean auth) {
            this.isAuth = auth;
        }
        protected String doInBackground(String... params) {
            String stringUrl = params[0];
            String result;
            final String REQUEST_METHOD = "GET";
            final int READ_TIMEOUT = 15000;
            final int CONNECTION_TIMEOUT = 15000;
            String inputLine;

            try {
                //Create a URL object holding our url
                URL myUrl = new URL(stringUrl);
                //Create a connection
                HttpsURLConnection connection =(HttpsURLConnection)
                        myUrl.openConnection();
                connection.setSSLSocketFactory(SSLCertificateSocketFactory.getInsecure(0, null));
                connection.setHostnameVerifier(new AllowAllHostnameVerifier());
                //Set methods and timeouts
                connection.setRequestMethod(REQUEST_METHOD);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setConnectTimeout(CONNECTION_TIMEOUT);
                Log.d("AccessToken", authState.getString("accessToken"));
                Log.d("idToken", authState.getString("idToken"));
                Log.d("refreshToken", authState.getString("refreshToken"));
                //refreshToken
                if(isAuth)
                    connection.setRequestProperty("Authorization", "Bearer " + authState.getString(("accessToken")));

                //Connect to our url
                connection.connect();
                int statusCode = connection.getResponseCode();
                Log.d("status","s: "+  statusCode);
                if(statusCode != 200) return "error response: " + statusCode;
                //Create a new InputStreamReader
                InputStreamReader streamReader = new
                        InputStreamReader(connection.getInputStream());
                //Create a new buffered reader and String Builder
                BufferedReader reader = new BufferedReader(streamReader);
                StringBuilder stringBuilder = new StringBuilder();
                //Check if the line we are reading is not null
                while((inputLine = reader.readLine()) != null){
                    stringBuilder.append(inputLine);
                }
                //Close our InputStream and Buffered reader
                reader.close();
                streamReader.close();
                //Set our result equal to our stringBuilder
                if(statusCode == 200)
                    result = "Success Response \nToken: " +  stringBuilder.toString();
                else result = "error response: " + statusCode;
            } catch (Exception ex) {result =  ex.getLocalizedMessage();}
            return result;
        }

        protected void onPostExecute(String result) {
            alertOneButton("FINICITY API DEMO", result);
        }
    }
    Thread thread = new Thread(new Runnable() {

        @Override
        public void run() {
            try  {
                Log.d("LOGOUT", "log out start");
                URL url = new URL("https://accounts.google.com/Logout");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                readStream(in);
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
            return "";
        }
    }
    private  void clearCookies()
    {
        android.webkit.CookieManager cookieManager = CookieManager.getInstance().getInstance();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.removeAllCookies(new ValueCallback<Boolean>() {
                // a callback which is executed when the cookies have been removed
                @Override
                public void onReceiveValue(Boolean aBoolean) {
                    Log.d("TAG", "Cookie removed: " + aBoolean);
                }
            });
        }
        else cookieManager.removeAllCookie();
        thread.start();
    }
}
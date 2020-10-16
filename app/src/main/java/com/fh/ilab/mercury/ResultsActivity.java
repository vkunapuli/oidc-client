package com.fh.ilab.mercury;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.SSLCertificateSocketFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class ResultsActivity extends AppCompatActivity {
    private Bundle authConfig;
    private Button logout;
    private TextView results;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        results = findViewById(R.id.results);
        results.setMovementMethod(new ScrollingMovementMethod());
        authConfig = getIntent().getExtras();
        results.setText("Token: " + authConfig.getString("accessToken"));
        //JWT jwt = new JWT(authConfig.getString("accessToken"));
        //Log.d("JWT", jwt.toString());
        try {
            String response = JWTUtils.decoded(authConfig.getString("accessToken"));
            JSONObject jsonObj = new JSONObject(response);
           //if(authConfig.getString("issuer").contains("https://secure.freddiemac.com") == false)
            if(authConfig.getString("issuer").contains("https://securedev.fmrei.com") == false)
                alertOneButton("RESULTS", jsonObj.toString(2));
        }catch (Exception ex) {
            Log.d("JWTERROR", ex.getLocalizedMessage());
        }
        if(authConfig != null && authConfig.getString("accessToken") != null)
        {
            //String myUrl = "http://www.google.copm";
            //dev/collectaiedata
            //dev/collectcredit
            String myUrl = "https://reddiebuyer.fmappsdev.fmrei.com/sit/collectCredit/status";
            //String myUrl = "https://reddiebuyer.fmapps.freddiemac.com/uat-r/collectAiedata/status";

            //String myUrl = "https://reddiebuyer.fmapps.freddiemac.com/dev/hc-client-registry/v1/actuator/health";//SIT
            //String myUrl = "https://reddiebuyer.fmapps.freddiemac.com/uat-r/hc-client-registry/v1/actuator/health";//UAT'
            //String myUrl = "https://reddiebuyer.fmapps.freddiemac.com/uat-r/collectCredit/status";//UAT'
            //String myUrl = "https://reddiebuyer.fmapps.freddiemac.com/cte/hc-client-registry/v1/actuator/info";//CTE


            if(authConfig.getString("issuer").contains("https://securedev.fmrei.com")) { //UAT/CTE
            //if(authConfig.getString("issuer").contains("https://secure.freddiemac.com")) {
                HttpAyncRequest2 getRequest = new HttpAyncRequest2(true);
                try {
                    getRequest.execute(myUrl);
                } catch (Exception ex) {
                }
            }
        }
        else toastOnButton("you can NOT access gold field until you login");

    }
    public void toastOnButton(String message) {
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }
    public void logout(View view) {
        Intent returnIntent = new Intent();
        returnIntent.putExtras(authConfig);
        Log.d("LOG" , authConfig.toString());
        setResult(Activity.RESULT_OK,returnIntent);
        finish();

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
            final String REQUEST_METHOD = "OST";
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


                /*
                 connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
                String body = "token="+ authConfig.getString("accessToken") + "&token_type_hint=access_token";

               Log.d("BODY", body);

                OutputStream os = connection.getOutputStream();
                byte[] input = body.getBytes("utf-8");
                os.write(input, 0, input.length);*/

                //refreshToken
               //if(isAuth)
                //    connection.setRequestProperty("Authorization", "Bearer " + authConfig.getString(("accessToken")));

                //Connect to our url
                connection.connect();
                int statusCode = connection.getResponseCode();
                Log.d("status","s: "+  statusCode);
                if(statusCode != 200) return "error response: " + statusCode +connection.getErrorStream().toString();
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
                else result = "error response: " + statusCode + stringBuilder.toString();
            } catch (Exception ex) {result =  ex.getLocalizedMessage();}
            return result ;
        }

        protected void onPostExecute(String result) {
            if(authConfig.getString("issuer").contains("https://secureuat.fmrei.com"))
                alertOneButton("RESULTS", result);
        }
    }
    class HttpAyncRequest2 extends AsyncTask<String, Void, String> {

        private Exception exception;
        private Boolean isAuth;
        public HttpAyncRequest2 (Boolean auth) {
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

                //refreshToken
                if(isAuth)
                    connection.setRequestProperty("Authorization", "Bearer " +  authConfig.getString(("accessToken")));

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

    public void alertOneButton(String title, String body) {

        new AlertDialog.Builder(ResultsActivity.this)
                .setTitle(title)
                .setMessage(body)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();
                    }
                }).show();
    }
}

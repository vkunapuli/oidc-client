package com.fh.ilab.mercury;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private Button googleLogin;
    private Button pingFedLogin;
    private Button googleIdpLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        googleLogin = findViewById(R.id.button1);
        googleLogin.setBackgroundResource(R.drawable.button);
        pingFedLogin =  findViewById(R.id.button2);
        pingFedLogin.setBackgroundResource(R.drawable.button);
        googleIdpLogin = findViewById(R.id.button3);
        googleIdpLogin.setBackgroundResource(R.drawable.button);

    }
    public void loginWithGoogleIdp(View view)
    {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("issuer", "https://www.ilabfhlmc.tk:9031");
        returnIntent.putExtra("clientID", "googleoidc");
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }
    public void loginWithPingFed(View view)
    {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("issuer", "https://securedev.fmrei.com");
        returnIntent.putExtra("clientID", "customer_mercury_ba0341_oidc_authz_pkce_ilab_app");

        //returnIntent.putExtra("issuer", "https://secure.freddiemac.com");
        //returnIntent.putExtra("clientID", "customer_oidc_authzpkce_ba0341_reddiebuyer-uatr_app");
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }
    public void loginWithGoogle(View view)
    {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("issuer", "https://accounts.google.com");
        returnIntent.putExtra("clientID", "800086261947-rln00ss021h4iasommmdf4if32nfrcth.apps.googleusercontent.com");
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }

    /*public void loginWithFreddie(View view)
    {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("issuer", "https://secureuat.fmrei.com");
        returnIntent.putExtra("clientID", "fmac_it_oidc_authz_mercury_uat_app");
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }*/

    public void loginWithFreddie(View view)
    {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("issuer", "https://secureuat.fmrei.com");
        returnIntent.putExtra("clientID", "fmac_it_oidc_authz_mercury_uat_app");
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }
    //800086261947-rln00ss021h4iasommmdf4if32nfrcth.apps.googleusercontent.com

}


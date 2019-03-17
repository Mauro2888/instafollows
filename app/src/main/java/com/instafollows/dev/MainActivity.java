package com.instafollows.dev;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.instafollows.dev.Utils.AppPreferences;
import com.instafollows.dev.Utils.AuthenticationDialog;
import com.instafollows.dev.Utils.AuthenticationListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AuthenticationListener {

    private Button mAuth;
    private String token = null;
    private AuthenticationDialog authenticationDialog = null;
    private View info = null;
    private AppPreferences appPreferences = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = findViewById(R.id.login_btn);
        mAuth.setOnClickListener(this);
        appPreferences = new AppPreferences(this);

        token = appPreferences.getString(AppPreferences.TOKEN);
        if (token != null) {
            getUserInfoByAccessToken(token);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mAuth){
            if(token!=null)
            {
                logout();
            }
            else {
                authenticationDialog = new AuthenticationDialog(this, this);
                authenticationDialog.setCancelable(true);
                authenticationDialog.show();
            }
        }
    }

    @Override
    public void onTokenReceived(String auth_token) {

        if (auth_token == null)
            return;
        appPreferences.putString(AppPreferences.TOKEN, auth_token);
        token = auth_token;
        getUserInfoByAccessToken(token);
    }

    private void getUserInfoByAccessToken(String token) {
        new RequestInstagramAPI().execute();
    }

    public class RequestInstagramAPI extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... strings) {
            String response = null;
            try {
                URL url = new URL(getResources().getString(R.string.get_user_info_url) + token);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setConnectTimeout(1000);
                urlConnection.setReadTimeout(15000);
                urlConnection.connect();
                if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = urlConnection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    String s;
                    while ((s = bufferedReader.readLine()) != null) {
                        response = s;

                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            if (response != null) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONObject jsonData = jsonObject.getJSONObject("data");
                    if (jsonData.has("id")) {
                        appPreferences.putString(AppPreferences.USER_ID, jsonData.getString("id"));
                        appPreferences.putString(AppPreferences.USER_NAME, jsonData.getString("username"));
                        appPreferences.putString(AppPreferences.PROFILE_PIC, jsonData.getString("profile_picture"));
                        login();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast toast = Toast.makeText(getApplicationContext(),"Login error!", Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }

    private void login() {
        View info = findViewById(R.id.info);
        ImageView pic = findViewById(R.id.pic);
        TextView id = findViewById(R.id.id);
        TextView name = findViewById(R.id.name);

        info.setVisibility(View.VISIBLE);
        mAuth.setText("LOGOUT");
        name.setText(appPreferences.getString(AppPreferences.USER_NAME));
        id.setText(appPreferences.getString(AppPreferences.USER_ID));
        Glide.with(this).
                load(appPreferences.getString(AppPreferences.PROFILE_PIC)).
                into(pic);
    }

    public void logout() {
        mAuth.setText("INSTAGRAM LOGIN");
        token = null;
        info.setVisibility(View.GONE);
        appPreferences.clear();
    }
}

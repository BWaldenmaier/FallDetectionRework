package com.example.falldetection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.pusher.pushnotifications.PushNotifications;
import com.google.android.material.textfield.TextInputEditText;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

/**
 * Login Activity to Login with an registered User
 */
public class Login extends AppCompatActivity {

    private TextInputEditText textInputEditTextUsername, textInputEditTextPassword;
    private Button buttonLogin;
    private TextView textViewSignUp;
    private ProgressBar progressBar;

    /**
     * on Create Method to set up up everything neccessary to Login
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.colorBackground));
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }

        PushNotifications.start(getApplicationContext(), "b147d8cf-58f4-4190-97bb-65410f817f68");
        PushNotifications.addDeviceInterest("hello");


        textInputEditTextUsername = findViewById(R.id.username);
        textInputEditTextPassword = findViewById(R.id.password);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewSignUp = findViewById(R.id.signUpText);
        progressBar = findViewById(R.id.progress);

        //click Listener to change UI from Login to the Registration Activity
        textViewSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignUp.class);
                startActivity(intent);
                finish();
            }
        });

        //click Listener to validate data from database and Login a User
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username, password;
                username = String.valueOf(textInputEditTextUsername.getText());
                password = String.valueOf(textInputEditTextPassword.getText());

                if (!username.equals("") && !password.equals("")) {
                    progressBar.setVisibility(View.VISIBLE);
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            String url ="http://lxvongobsthndl.ddns.net:3000/user/login";

                            // POST parameters
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("username", username);
                            params.put("password", password);

                            JSONObject jsonObj = new JSONObject(params);

                            final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                                    (Request.Method.POST, url, jsonObj, new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            try {
                                                Object success = response.get("success");
                                                if (success.toString().equals("true")){
                                                    Toast.makeText(getApplicationContext(), "Login successful", Toast.LENGTH_SHORT).show();
                                                    progressBar.setVisibility(View.GONE);
                                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                    intent.putExtra("username", username);
                                                    startActivity(intent);
                                                }
                                                else{
                                                    progressBar.setVisibility(View.GONE);
                                                    Toast.makeText(getApplicationContext(), "Login failed | wrong username or password", Toast.LENGTH_SHORT).show();
                                                }
                                            } catch (JSONException e) {
                                                progressBar.setVisibility(View.GONE);
                                                e.printStackTrace();
                                            }
                                        }
                                    }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(getApplicationContext(), "Connection to Server failed", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                            MySingleton.getInstance(Login.this).addToRequestque(jsonObjectRequest);
                        }
                    });
                }
                else{
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
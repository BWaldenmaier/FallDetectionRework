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
import com.google.android.material.textfield.TextInputEditText;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Activity to register a new User in the database
 */
public class SignUp extends AppCompatActivity {

    private TextInputEditText textInputEditTextFullname, textInputEditTextUsername, textInputEditTextPassword, textInputEditTextEmail, textInputEditTextArduinoID;
    private Button buttonSignUp;
    private TextView textViewLogin;
    private ProgressBar progressBar;

    /**
     * Method to check if String is a number
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }

    /**
     * method is used for checking valid email id format.
     *
     * @param email
     * @return boolean true for valid false for invalid
     */
    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    /**
     * create the Sign Up Instance
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.colorBackground));
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }

        textInputEditTextUsername = findViewById(R.id.username);
        textInputEditTextPassword = findViewById(R.id.password);
        textInputEditTextEmail = findViewById(R.id.email);
        textInputEditTextFullname = findViewById(R.id.fullname);
        textInputEditTextArduinoID = findViewById(R.id.arduinoID);
        buttonSignUp = findViewById(R.id.buttonSignUp);
        textViewLogin = findViewById(R.id.loginText);
        progressBar = findViewById(R.id.progress);

        // click Listener to change the UI View from the Registration to the Login
        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        // Click Listener to Register a new User with the given data
        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String fullname, username, password, email, arduinoID;
                fullname = String.valueOf(textInputEditTextFullname.getText());
                username = String.valueOf(textInputEditTextUsername.getText());
                password = String.valueOf(textInputEditTextPassword.getText());
                email = String.valueOf(textInputEditTextEmail.getText());
                arduinoID = String.valueOf(textInputEditTextArduinoID.getText());

                progressBar.setVisibility(View.VISIBLE);

                if (!fullname.equals("") && !username.equals("") && !password.equals("") && !email.equals("") && !arduinoID.equals("")) {
                    if (isEmailValid(email)) {
                        if (isNumeric(arduinoID)) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                String url = "http://lxvongobsthndl.ddns.net:3000/user/registration";

                                // POST parameters
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("fullname", fullname);
                                params.put("username", username);
                                params.put("password", password);
                                params.put("email", email);
                                params.put("arduinoID", arduinoID);


                                JSONObject jsonObj = new JSONObject(params);

                                final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                                        (Request.Method.POST, url, jsonObj, new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                try {
                                                    Object success = response.get("success");
                                                    if (success.toString().equals("true")) {
                                                        progressBar.setVisibility(View.GONE);
                                                        Toast.makeText(getApplicationContext(), "Registration was succesfull", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(getApplicationContext(), Login.class);
                                                        startActivity(intent);
                                                        finish();
                                                    } else if (success.toString().equals("doubleEntry")) {
                                                        progressBar.setVisibility(View.GONE);
                                                        Toast.makeText(getApplicationContext(), "This user is already registered! Try another Username", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        progressBar.setVisibility(View.GONE);
                                                        Toast.makeText(getApplicationContext(), "Registration failed", Toast.LENGTH_SHORT).show();
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

                                MySingleton.getInstance(SignUp.this).addToRequestque(jsonObjectRequest);
                            }
                        });
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "Please enter a valid ArduinoID", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), "Please enter a valid E-Mail", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "All fields are required", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }
}
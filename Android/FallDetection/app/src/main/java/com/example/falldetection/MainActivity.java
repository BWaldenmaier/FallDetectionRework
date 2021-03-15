package com.example.falldetection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.pusher.pushnotifications.PushNotifications;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

/**
 * Activity to show the History of detected Falls
 */
public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private ListView itemListView;
    private ArrayList<String> mWhoFell = new ArrayList<>();
    private ArrayList<String> mDate = new ArrayList<>();
    private ArrayList<String> mTime = new ArrayList<>();
    private ArrayList<Integer> images = new ArrayList<>();
    private MyAdapter adapter;
    private String loggedInUser = null;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    /**
     * creates the Main Activity for the Fall History
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.colorBackground));
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }

        PushNotifications.start(getApplicationContext(), "b147d8cf-58f4-4190-97bb-65410f817f68");
        PushNotifications.addDeviceInterest("hello");

        try {
            loadFalls();  //load Falls from Database when Login was successful

        } catch (JSONException e) {
            e.printStackTrace();
        }

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
    }

    /**
     * Refreshes the Falls when swiped down in th UI
     */
    @Override
    public void onRefresh() {
        //Toast.makeText(this, "Refresh", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
                try {
                    loadFalls();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, 1000);
    }

    /**
     * creates the Adapter for the UI data
     */
    private void makeAdapter(){
        itemListView = findViewById(R.id.itemListView);
        adapter = new MyAdapter(this, mWhoFell, mDate, mTime, images);
        itemListView.setAdapter(adapter);
        itemListView.invalidateViews();
    }

    /**
     * Load Falls from Databse whenever a User puts the App into Foreground
     */
    @Override
    protected void onResume() {
        super.onResume();
        try {
            loadFalls();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to load all falls from a specific User from the Database
     * @throws JSONException
     */
    private void loadFalls() throws JSONException {
        String url ="http://lxvongobsthndl.ddns.net:3000/updateFalls";

        loggedInUser = getIntent().getStringExtra("username");

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(loggedInUser);

        // Response Listener for data from database for a specific username
        final JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.POST, url, jsonArray, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (mWhoFell != null){
                            mWhoFell.clear();
                        }
                        if (mDate != null){
                            mDate.clear();
                        }
                        if (mTime != null){
                            mTime.clear();
                        }
                        if (images != null){
                            images.clear();
                        }

                        for (int i = response.length(); i >= 0; i--){  //show latest database entrys on top

                            try {
                                JSONObject entry = response.getJSONObject(i);
                                String dateAndTime = (String) entry.get("date");
                                String date = dateAndTime.substring(0,10);
                                String yy = date.substring(0,4);
                                String mm = date.substring(5,7);
                                String dd = date.substring(8,10);
                                String germanDate = dd +"."+ mm +"."+yy;
                                String time = dateAndTime.substring(11,19);

                                mWhoFell.add("Grandma");
                                mDate.add(germanDate);
                                mTime.add(time);
                                images.add(R.mipmap.granny);

                                makeAdapter();

                                if (mWhoFell.size() == 15){ // show only the 15 latest entrys
                                    break;
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Connection to Server failed", Toast.LENGTH_SHORT).show();
                    }
                });
        MySingleton.getInstance(MainActivity.this).addToRequestque(jsonArrayRequest);
    }

    /**
     * Adapter for the Data shown in the UI
     */
    private class MyAdapter extends ArrayAdapter<String> {

        Context context;
        ArrayList<String> rWhoFell;
        ArrayList<String> rDate;
        ArrayList<String> rTime;
        ArrayList<Integer> rImages;

        MyAdapter(Context c, ArrayList<String> whoFell, ArrayList<String> date, ArrayList<String> time, ArrayList<Integer> images) {
            super(c, R.layout.item_row, R.id.itemwhoFell, whoFell);
            this.context = c;
            this.rWhoFell = whoFell;
            this.rDate = date;
            this.rTime = time;
            this.rImages = images;
        }

        /**
         * Returns the rows for the Data shown in the UI
         * @param position
         * @param convertView
         * @param parent
         * @return
         */
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = layoutInflater.inflate(R.layout.item_row, parent, false);
            ImageView images = row.findViewById(R.id.itemIcon);
            TextView myWhoFell = row.findViewById(R.id.itemwhoFell);
            TextView myDate = row.findViewById(R.id.itemDate);
            TextView myTime = row.findViewById(R.id.itemTime);

            //set our resources on views
            images.setImageResource(rImages.get(position));
            myWhoFell.setText(rWhoFell.get(position));
            myDate.setText(rDate.get(position));
            myTime.setText(rTime.get(position));
            return row;
        }
    }
}
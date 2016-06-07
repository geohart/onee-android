package com.wearonee.oneeandroid;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.google.gson.Gson;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ConnectionActivity extends AppCompatActivity {

    private Onee mOnee;
    private ScheduledExecutorService scheduleTaskExecutor;
    private String connnectionId;
    private String username;

    private LinearLayout youStatusGood;
    private LinearLayout youStatusHelp;
    private LinearLayout youStatusInquire;
    private LinearLayout youStatusAcknowledge;

    private LinearLayout buddyStatusGood;
    private LinearLayout buddyStatusHelp;
    private LinearLayout buddyStatusInquire;
    private LinearLayout buddyStatusAcknowledge;

    private TextView youName;
    private TextView buddyName;
    private TextView youInquire;
    private TextView youAcknowledge;
    private TextView themInquire;
    private TextView themAcknowledge;

    private TextView tv;

    private Button btnUnsafe;
    private Button btnSafe;
    private Button btnInquire;
    private Button btnAcknowledge;

    protected void onCreate(Bundle savedInstanceState) {

        final ActionBar actionBar = getActionBar();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // set connection id
        mOnee = (Onee) getApplication();
        connnectionId = mOnee.getConnectionState().getActiveConnection().getConnectionId();
        username = mOnee.getUsername();

        // get ui elements
        youStatusGood = (LinearLayout) findViewById(R.id.you_status_good);
        youStatusHelp = (LinearLayout) findViewById(R.id.you_status_help);
        youStatusInquire = (LinearLayout) findViewById(R.id.you_status_inquire);
        youStatusAcknowledge = (LinearLayout) findViewById(R.id.you_status_acknowledge);

        buddyStatusGood = (LinearLayout) findViewById(R.id.them_status_good);
        buddyStatusHelp = (LinearLayout) findViewById(R.id.them_status_help);
        buddyStatusInquire = (LinearLayout) findViewById(R.id.them_status_inquire);
        buddyStatusAcknowledge = (LinearLayout) findViewById(R.id.them_status_acknowledge);

        youName = (TextView) findViewById(R.id.you_name);
        buddyName = (TextView) findViewById(R.id.them_name);
        youInquire = (TextView) findViewById(R.id.you_inquire_text);
        youAcknowledge = (TextView) findViewById(R.id.you_acknowledge_text);
        themInquire = (TextView) findViewById(R.id.them_inquire_text);
        themAcknowledge = (TextView) findViewById(R.id.them_acknowledge_text);

        btnSafe = (Button) findViewById(R.id.button_safe);
        btnUnsafe = (Button) findViewById(R.id.button_unsafe);
        btnInquire = (Button) findViewById(R.id.button_inquire);
        btnAcknowledge = (Button) findViewById(R.id.button_acknowledge);
        //tv = (TextView) findViewById(R.id.inquirystatus);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_connection, menu);
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Setup checkin thread:
        scheduleTaskExecutor = Executors.newScheduledThreadPool(5);
        scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                try {
                    // checkin with ONEE API
                    //runOnUiThread(new Runnable() {
                    //    public void run() {
                    // update UI
                    updateUI();
                    //Log.d("test", "checked in");
                    //    }
                    //});
                } catch (Exception e) {
                    Log.e("TEST", "run: error", e);
                }
            }
        }, 0, 1, TimeUnit.SECONDS);

    }

    @Override
    public void onStop() {
        super.onStop();

        // shut down check in
        scheduleTaskExecutor.shutdown();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void sendOkay(View view){
         this.sendMessage("safe");
    }

    public void sendUnsafe(View view){
        this.sendMessage("unsafe");
    }

    public void sendInquire(View view){
        this.sendMessage("inquire");
    }

    public void sendAcknowledge(View view){
        // silence ringing bracelet
        mOnee.getBracelet().pulseOnce();
        this.sendMessage("acknowledge");
    }

    public void updateUI(){
        // set UI based on connection state
        runOnUiThread(new Runnable() {
            public void run() {

                // get connection state
                Onee onee = (Onee) getApplication();
                OneeAPI.ConnectionState cs = onee.getConnectionState();

                // check to ensure connection exists?
                if(cs.getActiveConnection() != null) {
                    // set name elements
                    youName.setText(cs.getUser().getName());
                    buddyName.setText(cs.getBuddy().getName());


                    //tv.setText(String.valueOf(cs.getActiveConnection().getBuddyInquire(cs.getUser().getUsername())));

                    // set status elements
                    // first - you
                    // set username
                    SharedPreferences auth = getSharedPreferences("auth", MODE_PRIVATE);
                    String username = auth.getString("email", "no user");
                    if (cs.getActiveConnection().getUserSafe(username) != 1) {
                        // user is unsafe
                        youStatusGood.setVisibility(View.GONE);
                        youStatusHelp.setVisibility(View.VISIBLE);
                    } else {
                        // user is normal
                        youStatusGood.setVisibility(View.VISIBLE);
                        youStatusHelp.setVisibility(View.GONE);
                    }

                    if (cs.getActiveConnection().getUserInquire(username) == 1) {
                        // user wants to know how buddy is doing
                        youInquire.setText("You asked how " + cs.getBuddy().getName() + " is doing.");
                        youStatusInquire.setVisibility(View.VISIBLE);
                    } else {
                        youStatusInquire.setVisibility(View.GONE);
                    }

                    if (cs.getActiveConnection().getUserAcknowledge(username) == 1) {
                        // user acknowledges buddy's request
                        youAcknowledge.setText("You acknowledged " + cs.getBuddy().getName() + "'s request.");
                        youStatusAcknowledge.setVisibility(View.VISIBLE);
                    } else {
                        youStatusAcknowledge.setVisibility(View.GONE);
                    }

                    // now them
                    if (cs.getActiveConnection().getBuddySafe(username) != 1) {
                        // buddy is unsafe
                        buddyStatusGood.setVisibility(View.GONE);
                        buddyStatusHelp.setVisibility(View.VISIBLE);
                    } else {
                        // user is normal
                        buddyStatusGood.setVisibility(View.VISIBLE);
                        buddyStatusHelp.setVisibility(View.GONE);
                    }

                    if (cs.getActiveConnection().getBuddyInquire(username) == 1) {
                        // buddy wants to know how user is doing
                        themInquire.setText(cs.getBuddy().getName() + " asked how you are doing.");
                        buddyStatusInquire.setVisibility(View.VISIBLE);
                    } else {
                        buddyStatusInquire.setVisibility(View.GONE);
                    }

                    if (cs.getActiveConnection().getBuddyAcknowledge(username) == 1) {
                        // buddy acknowledges user's request
                        themAcknowledge.setText(cs.getBuddy().getName() + " acknowledged your request.");
                        buddyStatusAcknowledge.setVisibility(View.VISIBLE);
                    } else {
                        buddyStatusAcknowledge.setVisibility(View.GONE);
                    }

                    // hide/show buttons based on context
                    if(cs.getActiveConnection().getConnectionContext(username) == Models.Connection.CONTEXT_USER_UNSAFE){
                        btnAcknowledge.setVisibility(View.GONE);
                        btnInquire.setVisibility(View.GONE);
                        btnSafe.setVisibility(View.VISIBLE);
                        btnUnsafe.setVisibility(View.GONE);
                    } else if(cs.getActiveConnection().getConnectionContext(username) == Models.Connection.CONTEXT_BUDDY_UNSAFE){
                        btnAcknowledge.setVisibility(View.VISIBLE);
                        btnInquire.setVisibility(View.GONE);
                        btnSafe.setVisibility(View.GONE);
                        btnUnsafe.setVisibility(View.VISIBLE);
                    } else if (cs.getActiveConnection().getConnectionContext(username) == Models.Connection.CONTEXT_BUDDY_INQUIRING){
                        btnAcknowledge.setVisibility(View.GONE);
                        btnInquire.setVisibility(View.GONE);
                        btnSafe.setVisibility(View.VISIBLE);
                        btnUnsafe.setVisibility(View.VISIBLE);
                    } else {
                        btnAcknowledge.setVisibility(View.GONE);
                        btnInquire.setVisibility(View.VISIBLE);
                        btnSafe.setVisibility(View.GONE);
                        btnUnsafe.setVisibility(View.VISIBLE);
                    }

                } else {
                    // send to home
                    finish();
                }
            }
        });
    }

    private void sendMessage(String message){

        // create a new instance of ONEE API
        final OneeAPI oneeAPI = new OneeAPI(this, true);

        // get shared preferences
        SharedPreferences auth = getSharedPreferences("auth", MODE_PRIVATE);

        // send message
        oneeAPI.sendMessage(auth.getString("email", null), connnectionId, message, auth.getString("token", null),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Gson gson = new Gson();
                        OneeAPI.ConnectionState messageSent = gson.fromJson(response, OneeAPI.ConnectionState.class);
                        Toast.makeText(oneeAPI.getContext(), messageSent.getMessage(), Toast.LENGTH_SHORT).show();

                        // update application connection state
                        Onee onee = (Onee) getApplication();
                        onee.setConnectionState(messageSent);

                        // update UI
                        updateUI();

                    }
                });
    }

    public void endConnection(View view){
        // create a new instance of ONEE API
        final OneeAPI oneeAPI = new OneeAPI(this, true);

        // get shared preferences
        SharedPreferences auth = getSharedPreferences("auth", MODE_PRIVATE);

        // get recent connections
        oneeAPI.endConnection(auth.getString("email", null), auth.getString("token", null),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Gson gson = new Gson();
                        OneeAPI.ConnectionState connectionEnded = gson.fromJson(response, OneeAPI.ConnectionState.class);

                        // update application connection state
                        Onee onee = (Onee) getApplication();
                        onee.setConnectionState(connectionEnded);

                        // return to home screen
                        finish();

                    }
                });
    }

    public void bracelet(MenuItem item){
        Intent intent = new Intent(this, BraceletActivity.class);
        startActivity(intent);
    }

    public void logout(MenuItem item){
        Onee onee = (Onee) this.getApplication();
        onee.logout();
    }

}

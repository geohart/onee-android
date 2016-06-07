package com.wearonee.oneeandroid;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Response;
import com.google.gson.Gson;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by George on 4/18/2016.
 */
public class Onee extends Application {

    private OneeAPI.ConnectionState connectionState;
    private ScheduledExecutorService scheduleTaskExecutor;
    private boolean isAuthenticated = false;
    private Bracelet bracelet;

    @Override
    public void onCreate() {
        super.onCreate();

        // Setup checkin thread:
        scheduleTaskExecutor = Executors.newScheduledThreadPool(5);
        scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                try {
                    // get connection update from ONEE API
                    final OneeAPI oneeAPI = new OneeAPI(getApplicationContext(), false);

                    if(isAuthenticated){
                        updateConnection(oneeAPI);
                        //Log.d("oneeapp", "updated state");
                    } else {
                        Log.d("oneeapp", "waiting to start updating");
                    }

                } catch (Exception e) {
                    Log.e("oneeapp", "run: error", e);
                }
            }
        }, 0, 5, TimeUnit.SECONDS);

        // setup bracelet object
        this.bracelet = new Bracelet(this);

    }

    public Bracelet getBracelet(){
        return this.bracelet;
    }

    public OneeAPI.ConnectionState getConnectionState() {
        return this.connectionState;
    }

    public void setConnectionState(OneeAPI.ConnectionState update) {

        // check for an active connection
        if(update.getActiveConnection() != null){

            // get username
            final SharedPreferences auth = getSharedPreferences("auth", MODE_PRIVATE);
            String un = auth.getString("email", null);

            if(un != null) {
                // first check if buddy is safe
                if (update.getActiveConnection().getBuddySafe(un) == 0) {
                    // repeat pulse bracelet
                    bracelet.pulseRepeat();
                } else if(connectionState != null) {

                    if(connectionState.getActiveConnection() != null) {

                        // now check for changes:
                        if (connectionState.getActiveConnection().getBuddySafe(un) == 0 && update.getActiveConnection().getBuddySafe(un) == 1) {
                            // buddy is now safe - cancel repeat pulse
                            bracelet.pulseOnce();
                        } else if (connectionState.getActiveConnection().getBuddyInquire(un) == 0 && update.getActiveConnection().getBuddyInquire(un) == 1) {
                            // buddy wants to know how user is doing - send single pulse
                            bracelet.pulseOnce();
                        } else if (connectionState.getActiveConnection().getBuddyAcknowledge(un) == 0 && update.getActiveConnection().getBuddyAcknowledge(un) == 1) {
                            // buddy acknowledges state - send single pulse
                            bracelet.pulseOnce();
                        }
                        Log.d("testing123", this.connectionState.getActiveConnection().getBuddyInquire(un) + " - " + update.getActiveConnection().getBuddyInquire(un));
                    } else if (connectionState.getActiveConnection() == null && update.getActiveConnection() != null){
                        // newly established connection
                        bracelet.pulseOnce();
                    }
                }
            }
        }

        // update connection state
        this.connectionState = update;

    }

    public void setIsAuthenticated(boolean status){
        this.isAuthenticated = status;
    }

    /**
     * called to update connection
     */
    private void updateConnection(OneeAPI oneeAPI){

        // get shared preferences
        final SharedPreferences auth = getSharedPreferences("auth", MODE_PRIVATE);

        // don't make endless requests to the server if there is no token
        if(auth.getString("token", null) != null) {

            // get recent connections
            oneeAPI.getUpdate(auth.getString("email", null), auth.getString("token", null),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Gson gson = new Gson();
                        OneeAPI.ConnectionState update = gson.fromJson(response, OneeAPI.ConnectionState.class);
                        setConnectionState(update);
                    }
                });
        }
    }

    public String getUsername(){
        // get shared preferences
        SharedPreferences auth = getSharedPreferences("auth", MODE_PRIVATE);
        return auth.getString("email", null);
    }

    public void logout(){

        // get shared preferences
        SharedPreferences auth = getSharedPreferences("auth", MODE_PRIVATE);
        SharedPreferences.Editor reset = auth.edit();
        reset.remove("password");
        reset.remove("token");
        reset.commit();

        // route to view connection activity
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }

    public OneeAPI getOneeAPI(){
        OneeAPI oneeAPI = new OneeAPI(this, true);
        return oneeAPI;
    }

}

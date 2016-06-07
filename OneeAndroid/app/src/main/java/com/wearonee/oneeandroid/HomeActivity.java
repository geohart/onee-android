package com.wearonee.oneeandroid;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.google.gson.Gson;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class HomeActivity extends AppCompatActivity {

    private ScheduledExecutorService scheduleTaskExecutor;
    private String connectionId;
    private View mProgressView;
    private TextView requestedResponse;
    private TextView newRequest;
    private TextView requestedName;
    private TextView requestedEmail;
    private TextView inactiveName;
    private TextView inactiveEmail;
    private TextView activeName;
    private TextView activeEmail;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        requestedResponse = (TextView) findViewById(R.id.requested_response);
        requestedName = (TextView) findViewById(R.id.requested_name);
        requestedEmail = (TextView) findViewById(R.id.requested_email);
        newRequest = (TextView) findViewById(R.id.newRequest);
        inactiveEmail = (TextView) findViewById(R.id.inactive_email);
        inactiveName = (TextView) findViewById(R.id.inactive_name);
        activeEmail = (TextView) findViewById(R.id.active_email);
        activeName = (TextView) findViewById(R.id.active_name);

        mProgressView = findViewById(R.id.load_connection_progress);
        showProgress(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
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

    @Override
    public void onStart() {
        super.onStart();

        // Check to see if username and password on file
        final SharedPreferences auth = getSharedPreferences("auth", MODE_PRIVATE);

        // create a new instance of ONEE API to get user
        final OneeAPI oneeAPI = new OneeAPI(this, true);

        // Setup checkin thread:
        scheduleTaskExecutor = Executors.newScheduledThreadPool(5);
        scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                try {
                    // checkin with ONEE API
                    //runOnUiThread(new Runnable() {
                    //    public void run() {
                    // update UI component
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
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();

        // shut down check in
        scheduleTaskExecutor.shutdown();

    }

    /**
     * called to update connection
     */
    private void updateUI(){

        // get shared preferences
        final SharedPreferences auth = getSharedPreferences("auth", MODE_PRIVATE);

        // get current connection state
        Onee onee = (Onee) getApplication();
        final OneeAPI.ConnectionState cs = onee.getConnectionState();

        // set UI based on connection state
        runOnUiThread(new Runnable() {
            public void run() {

                // reset connectionId
                connectionId = null;

                if (cs != null) {

                    //Log.d("test", "found connection state");
                    showProgress(false);

                    // first check if user is verified
                    if (!(cs.getUser().getVerified() > 0)) {
                        // user is not verified - hide everything except prompt for verification
                        findViewById(R.id.not_verified).setVisibility(LinearLayout.VISIBLE);
                        findViewById(R.id.active_connection).setVisibility(LinearLayout.GONE);
                        findViewById(R.id.inactive_connection_creator).setVisibility(LinearLayout.GONE);
                        findViewById(R.id.inactive_connection_buddy).setVisibility(LinearLayout.GONE);
                        findViewById(R.id.connection_request_link).setVisibility(LinearLayout.GONE);
                        findViewById(R.id.no_connection).setVisibility(LinearLayout.GONE);

                    } else {

                        // hide verification information
                        findViewById(R.id.not_verified).setVisibility(LinearLayout.GONE);

                        if (cs.getActiveConnection() == null) {
                            // no connection exists -- hide
                            findViewById(R.id.active_connection).setVisibility(LinearLayout.GONE);

                        } else {
                            // active connection -- show
                            findViewById(R.id.active_connection).setVisibility(LinearLayout.VISIBLE);
                            findViewById(R.id.no_connection).setVisibility(View.GONE);
                            activeName.setText(cs.getBuddy().getName());
                            activeEmail.setText(cs.getBuddy().getUsername());
                        }

                        if (cs.getRequestedConnection() == null) {
                            // no user-initiated connection request -- hide
                            findViewById(R.id.inactive_connection_creator).setVisibility(LinearLayout.GONE);
                        } else {
                            // user-initiated connection exists -- show
                            findViewById(R.id.inactive_connection_creator).setVisibility(LinearLayout.VISIBLE);
                            findViewById(R.id.no_connection).setVisibility(View.GONE);
                            requestedResponse.setText("Waiting for " + cs.getRequestedConnection().getBuddyName() + " to accept.");
                            requestedName.setText(cs.getRequestedConnection().getBuddyName());
                            requestedEmail.setText(cs.getRequestedConnection().getBuddy());
                        }

                        if (cs.getConnectionRequests().length > 0) {
                            // pending connection request
                            /*if (cs.getConnectionRequests().length > 1) {
                                // more than one -- show link to View Connections
                                findViewById(R.id.inactive_connection_buddy).setVisibility(LinearLayout.GONE);
                                findViewById(R.id.connection_request_link).setVisibility(LinearLayout.VISIBLE);
                            } else {*/
                                // just show single request
                                connectionId = cs.getConnectionRequests()[0].getConnectionId();
                                findViewById(R.id.inactive_connection_buddy).setVisibility(LinearLayout.VISIBLE);
                                findViewById(R.id.connection_request_link).setVisibility(LinearLayout.GONE);
                                findViewById(R.id.no_connection).setVisibility(LinearLayout.GONE);
                                newRequest.setText(cs.getConnectionRequests()[0].getCreatorName() + " wants to connect:");
                                inactiveName.setText(cs.getConnectionRequests()[0].getCreatorName());
                                inactiveEmail.setText(cs.getConnectionRequests()[0].getCreator());
                            //}
                        } else {
                            findViewById(R.id.inactive_connection_buddy).setVisibility(LinearLayout.GONE);
                        }

                        if (cs.getConnectionRequests().length == 0 && cs.getRequestedConnection() == null && cs.getActiveConnection() == null) {
                            // show new connection dialog
                            findViewById(R.id.no_connection).setVisibility(View.VISIBLE);
                        }
                    }
                } else {
                    Log.d("test", "connection state is null");
                }
            }
        });
    }

    public void verify(View view){
        // get code from textbox
        TextInputEditText verify = (TextInputEditText) findViewById(R.id.verify);
        String code = verify.getText().toString();

        // call oneeapi verify method
        // create a new instance of ONEE API
        final OneeAPI oneeAPI = new OneeAPI(this, true);

        // get shared preferences
        SharedPreferences auth = getSharedPreferences("auth", MODE_PRIVATE);

        // verify account
        oneeAPI.verifyAccount(auth.getString("email", null), code,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Gson gson = new Gson();
                        OneeAPI.OneeResponse accountVerified = gson.fromJson(response, OneeAPI.OneeResponse.class);

                        // update cs object (temporary)
                        Onee onee = (Onee) getApplication();
                        onee.setIsAuthenticated(true);

                        Toast.makeText(oneeAPI.getContext(), accountVerified.getMessage(), Toast.LENGTH_LONG).show();

                        // update interface
                        updateUI();

                    }
                });
    }

    public void resendVerification(View view){
        // create a new instance of ONEE API
        final OneeAPI oneeAPI = new OneeAPI(this, true);

        // get shared preferences
        SharedPreferences auth = getSharedPreferences("auth", MODE_PRIVATE);

        // end connection
        oneeAPI.resendVerification(auth.getString("email", null),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Gson gson = new Gson();
                        OneeAPI.OneeResponse codeResent = gson.fromJson(response, OneeAPI.OneeResponse.class);

                        Toast.makeText(oneeAPI.getContext(), codeResent.getMessage(), Toast.LENGTH_LONG).show();

                    }
                });
    }

    public void viewConnection(View view){
        // route to view connection activity
        Intent intent = new Intent(this, ConnectionActivity.class);
        startActivity(intent);
    }

    public void newConnection(View view){
        // route to view connection activity
        //onSearchRequested();
        Intent intent = new Intent(this, NewConnectionActivity.class);
        startActivity(intent);
    }

    public void acceptConnection(View view){
        // create a new instance of ONEE API
        final OneeAPI oneeAPI = new OneeAPI(this, true);

        // get shared preferences
        SharedPreferences auth = getSharedPreferences("auth", MODE_PRIVATE);

        if(this.connectionId != null) {

            // accept connection
            oneeAPI.acceptConnection(auth.getString("email", null), this.connectionId, auth.getString("token", null),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            Gson gson = new Gson();
                            OneeAPI.ConnectionState connectionAccepted = gson.fromJson(response, OneeAPI.ConnectionState.class);
                            Toast.makeText(oneeAPI.getContext(), connectionAccepted.getMessage(), Toast.LENGTH_LONG).show();

                            // update application connection state
                            Onee onee = (Onee) getApplication();
                            onee.setConnectionState(connectionAccepted);

                            // route to view connection activity
                            //Intent intent = new Intent(oneeAPI.getContext(), ConnectionActivity.class);
                            //startActivity(intent);

                            // update UI
                            updateUI();
                        }
                    });
        }
    }

    public void endConnection(View view){
        // create a new instance of ONEE API
        final OneeAPI oneeAPI = new OneeAPI(this, true);

        // get shared preferences
        SharedPreferences auth = getSharedPreferences("auth", MODE_PRIVATE);

        // end connection
        oneeAPI.endConnection(auth.getString("email", null), auth.getString("token", null),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Gson gson = new Gson();
                        OneeAPI.ConnectionState connectionEnded = gson.fromJson(response, OneeAPI.ConnectionState.class);

                        // update application connection state
                        Onee onee = (Onee) getApplication();
                        onee.setConnectionState(connectionEnded);

                        // update UI
                        updateUI();
                    }
                });
    }

    public void logout(MenuItem item){
        Onee onee = (Onee) this.getApplication();
        onee.logout();
    }

    public void bracelet(MenuItem item){
        Intent intent = new Intent(this, BraceletActivity.class);
        startActivity(intent);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            /*mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });*/

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            //mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

}

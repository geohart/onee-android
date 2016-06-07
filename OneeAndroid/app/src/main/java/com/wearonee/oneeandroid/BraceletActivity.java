package com.wearonee.oneeandroid;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BraceletActivity extends AppCompatActivity {

    private Onee mOnee;
    private BluetoothAdapter mBluetoothAdapter;
    private ScheduledExecutorService scheduleTaskExecutor;
    private static final int REQUEST_ENABLE_BT = 3;
    private boolean oneeFound;

    private LinearLayout mNoOnee;
    private LinearLayout mConnect;
    private LinearLayout mDisconnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bracelet);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // create reference to parent application
        this.mOnee = (Onee) getApplication();
        this.mBluetoothAdapter = mOnee.getBracelet().getBluetoothAdapter();
        this.oneeFound = false;

        this.mNoOnee = (LinearLayout) findViewById(R.id.no_paired_onee);
        this.mConnect = (LinearLayout) findViewById(R.id.connect_onee);
        this.mDisconnect = (LinearLayout) findViewById(R.id.disconnect_onee);

    }

    @Override
    public void onStart() {
        super.onStart();

        // Setup checkin thread:
        scheduleTaskExecutor = Executors.newScheduledThreadPool(5);
        scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                try {
                    updateUI();
                } catch (Exception e) {
                    Log.e("TEST", "run: error", e);
                }
            }
        }, 0, 1, TimeUnit.SECONDS);

        if (this.mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
        } else if (!this.mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            this.oneeFound = checkForOnee();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bracelet, menu);
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

    public void refresh(View view){
        this.oneeFound = checkForOnee();
        updateUI();
    }

    public void connectOnee(View view){
        mOnee.getBracelet().connectDevice();
        updateUI();
    }

    public void disconnectOnee(View view){
        mOnee.getBracelet().disconnectDevice();
        updateUI();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up view accordingly
                    this.oneeFound = checkForOnee();
                    this.updateUI();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Toast.makeText(this, "Error: Bluetooth not enabled.", Toast.LENGTH_SHORT).show();
                    this.finish();
                }
        }
    }

    public void updateUI(){
        // set UI based on connection state
        runOnUiThread(new Runnable() {
            public void run() {

                if(!oneeFound){
                    mNoOnee.setVisibility(View.VISIBLE);
                    mConnect.setVisibility(View.GONE);
                    mDisconnect.setVisibility(View.GONE);
                } else if (mOnee.getBracelet().getConnected()){
                    mNoOnee.setVisibility(View.GONE);
                    mConnect.setVisibility(View.GONE);
                    mDisconnect.setVisibility(View.VISIBLE);
                } else {
                    mNoOnee.setVisibility(View.GONE);
                    mConnect.setVisibility(View.VISIBLE);
                    mDisconnect.setVisibility(View.GONE);
                }

            }
        });
    }

    public void logout(MenuItem item){
        Onee onee = (Onee) this.getApplication();
        onee.logout();
    }

    private boolean checkForOnee(){

        // cycle over bonded devices and check for a device matching the device we want
        for (BluetoothDevice d : mBluetoothAdapter.getBondedDevices()) {
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(d.getAddress());

            if (device.getName().equals("HC-05") || device.getName().equals("HC-06")) { // TODO: make this more sophisticated
                return true;
            }
        }

        return false;

    }

}

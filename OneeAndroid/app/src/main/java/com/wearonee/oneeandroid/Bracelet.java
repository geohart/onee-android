package com.wearonee.oneeandroid;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.android.volley.Response;
import com.google.gson.Gson;

/**
 * Created by George on 5/10/2016.
 */
public class Bracelet {

    private BluetoothAdapter mBluetoothAdapter = null;
    private OneeBluetoothService mOneeBTService = null;
    private final Handler mHandler;
    private Onee mOnee;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_RECEIVED = 2;

    public Bracelet(Onee onee){

        this.mOnee = onee;

        // Get local Bluetooth adapter
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // setup message handler
        this.mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                switch (msg.what) {
                /*case MESSAGE_STATE_CHANGE:
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    break;*/
                    case MESSAGE_RECEIVED:
                        byte[] readBuf = (byte[]) msg.obj;
                        // construct a string from the valid bytes in the buffer
                        String readMessage = new String(readBuf, 0, msg.arg1);
                        Log.d("test", readMessage);
                        String cleaned = readMessage.replaceAll("\\D", "");
                        if (cleaned.length()>0) {
                            String shortened = cleaned.substring(0, 1);

                            //Log.d("message", shortened);

                            if (shortened.equals("1")) {
                                // signal is 1
                                //Log.d("one", shortened);

                                Models.Connection active = mOnee.getConnectionState().getActiveConnection();
                                String username = mOnee.getUsername();

                                if(active.getConnectionContext(username) == Models.Connection.CONTEXT_USER_UNSAFE){
                                    sendOkay();
                                } else {
                                    if (active.getConnectionContext(username) == Models.Connection.CONTEXT_BUDDY_UNSAFE) {
                                        sendAcknowledge();
                                    } else if (active.getConnectionContext(username) == Models.Connection.CONTEXT_BUDDY_INQUIRING) {
                                        sendOkay();
                                    } else if (active.getConnectionContext(username) == Models.Connection.CONTEXT_USER_INQUIRING) {
                                        // do nothing
                                    } else {
                                        sendInquire();
                                    }
                                }

                            } else if (shortened.equals("2")) {
                                // signal is 2
                                Log.d("two", shortened);
                                sendUnsafe();
                            }
                        }
                        break;
                }
            }
        };

    }

    public BluetoothAdapter getBluetoothAdapter(){
        return this.mBluetoothAdapter;
    }

    public boolean getConnected(){
        if(this.mOneeBTService != null){
            if(this.mOneeBTService.getState() == OneeBluetoothService.STATE_CONNECTED){
                return true;
            }
        }
        return false;
    }

    public void connectDevice() {

        if (mOneeBTService == null || mOneeBTService.getState() == OneeBluetoothService.STATE_NONE) {

            // Initialize the OneeBluetoothChatService to perform bluetooth connections
            mOneeBTService = new OneeBluetoothService(mHandler);

            // cycle over bonded devices
            for (BluetoothDevice d : mBluetoothAdapter.getBondedDevices()) {
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(d.getAddress());

                // check for a device matching the device we want
                // TODO: make this more sophisticated
                if (device.getName().equals("HC-05") || device.getName().equals("HC-06")) {
                    // Attempt to connect to the device
                    mOneeBTService.connect(device);
                }
            }
        }
    }

    public void pulseOnce() {
        if (this.getConnected() == true) {
            mOneeBTService.sendOne();
        }
    }

    public void pulseRepeat(){
        if (this.getConnected() == true){
            mOneeBTService.sendFour();
        }
    }

    public void pulseStop(){
        if (this.getConnected() == true){
            mOneeBTService.sendOne();
        }
    }

    public void disconnectDevice(){
        if (mOneeBTService != null) {
            mOneeBTService.stop();
        }
    }

    public void sendOkay(){
        this.sendMessage("safe");
    }

    public void sendUnsafe(){
        this.sendMessage("unsafe");
    }

    public void sendInquire(){
        this.sendMessage("inquire");
    }

    public void sendAcknowledge(){
        this.pulseOnce();
        this.sendMessage("acknowledge");
    }

    private void sendMessage(String message){

        // create a new instance of ONEE API
        //final OneeAPI oneeAPI = new OneeAPI(mOnee.getApplicationContext(), true);

        // get shared preferences
        SharedPreferences auth = mOnee.getApplicationContext().getSharedPreferences("auth", Context.MODE_PRIVATE);

        // send message
        mOnee.getOneeAPI().sendMessage(auth.getString("email", null), mOnee.getConnectionState().getActiveConnection().getConnectionId(), message, auth.getString("token", null),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Gson gson = new Gson();
                        OneeAPI.ConnectionState messageSent = gson.fromJson(response, OneeAPI.ConnectionState.class);
                        //Toast.makeText(oneeAPI.getContext(), messageSent.getMessage(), Toast.LENGTH_SHORT).show();

                        // update application connection state
                        mOnee.setConnectionState(messageSent);

                    }
                });
    }

}

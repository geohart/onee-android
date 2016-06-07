package com.wearonee.oneeandroid;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by George on 4/12/2016.
 */
public class OneeAPI {

    private final Context context;

    //private static final String URL_BASE = "http://10.0.2.2:5000";
    private static final String URL_BASE = "http://app.wearonee.com";
    private static final String ENDPOINT_AUTH = "/auth/authenticate";
    private static final String ENDPOINT_CREATE_ACCOUNT = "/account/create";
    private static final String ENDPOINT_VERIFY = "/account/verify";
    private static final String ENDPOINT_RESEND = "/account/resend";
    private static final String ENDPOINT_RECENT = "/account/secure/connections/recent";
    private static final String ENDPOINT_FIND = "/account/secure/users/find";
    private static final String ENDPOINT_CONNECT = "/connection/create";
    private static final String ENDPOINT_ACCEPT = "/connection/accept";
    private static final String ENDPOINT_UPDATE = "/connection/update";
    private static final String ENDPOINT_MESSAGE = "/connection/message";
    private static final String ENDPOINT_END = "/connection/end";
    //private static final String ENDPOINT_CONNECTION = "/connection/connection";
    private static final Double TOKEN_LIFETIME = 86400000.0;

    public OneeAPI(Context context, boolean checkPermission) {
        this.context = context;
        if(checkPermission){
            this.checkPermission();
        }
    }

    public Context getContext(){
        return this.context;
    }

    public void checkPermission(){
        if(this.checkToken() == false){
            // get shared preferences
            SharedPreferences auth = this.context.getSharedPreferences("auth", this.context.MODE_PRIVATE);
            SharedPreferences.Editor reset = auth.edit();
            reset.remove("password");
            reset.remove("token");
            reset.commit();

            // route to view connection activity
            Intent intent = new Intent(this.context, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            this.context.startActivity(intent);
        }
    }

    public void createAccount(final String email, final String name, final String phone, final String password, Response.Listener<String> callback){
        // set url
        String url = URL_BASE + ENDPOINT_CREATE_ACCOUNT;

        // setup hashmap for post parameters
        HashMap<String,String> postParams = new HashMap<String,String>();
        postParams.put("email", email);
        postParams.put("name", name);
        postParams.put("phone", phone);
        postParams.put("password", password);

        // Request a string response from the provided URL.
        OneePostRequest request = new OneePostRequest(url, callback, new OneeResponseError(context), postParams);

        // Add request to the request queue
        MySingleton.getInstance(this.context.getApplicationContext()).addToRequestQueue(request.getRequest());

    }

    public void logIn(final String email, final String password, Response.Listener<String> callback, OneeResponseError errorHandler){

        // set url
        String url = URL_BASE + ENDPOINT_AUTH;

        // setup hashmap for post parameters
        HashMap<String,String> postParams = new HashMap<String,String>();
        postParams.put("email", email);
        postParams.put("password", password);

        // Request a string response from the provided URL.
        OneePostRequest request = new OneePostRequest(url, callback, errorHandler, postParams);

        // Add request to the request queue
        MySingleton.getInstance(this.context.getApplicationContext()).addToRequestQueue(request.getRequest());

        // TODO what happens when the authentication attempt fails on the server side?
    }

    /*public void getUserVerified(final String email, final String token, Response.Listener<String> callback){

        // validate token
        this.checkPermission();

        // setup request URL
        Uri.Builder builder = new Uri.Builder();
        builder
                .appendQueryParameter("email", email)
                .appendQueryParameter("token", token);
        String url = URL_BASE + ENDPOINT_VERIFICATION + builder.build().toString();

        // Request a string response from the provided URL.
        OneeGetRequest request = new OneeGetRequest(url, callback, new OneeResponseError(context));

        // Add request to the request queue
        MySingleton.getInstance(this.context.getApplicationContext()).addToRequestQueue(request.getRequest());

    }*/

    // verify user account
    public void verifyAccount(final String email, final String code, Response.Listener<String> callback){

        // setup request URL
        Uri.Builder builder = new Uri.Builder();
        builder
                .appendQueryParameter("email", email)
                .appendQueryParameter("code", code);
        String url = URL_BASE + ENDPOINT_VERIFY + builder.build().toString();

        // Request a string response from the provided URL.
        OneeGetRequest request = new OneeGetRequest(url, callback, new OneeResponseError(context));

        // Add request to the request queue
        MySingleton.getInstance(this.context.getApplicationContext()).addToRequestQueue(request.getRequest());

    }

    // verify user account
    public void resendVerification(final String email, Response.Listener<String> callback){

        // set url
        String url = URL_BASE + ENDPOINT_RESEND;

        // setup hashmap for post parameters
        HashMap<String,String> postParams = new HashMap<String,String>();
        postParams.put("email", email);

        // Request a string response from the provided URL.
        OneePostRequest request = new OneePostRequest(url, callback, new OneeResponseError(context), postParams);

        // Add request to the request queue
        MySingleton.getInstance(this.context.getApplicationContext()).addToRequestQueue(request.getRequest());

    }



    public boolean checkToken(){

        // get saved preferences
        final SharedPreferences auth = this.context.getSharedPreferences("auth", Context.MODE_PRIVATE);
        final String email = auth.getString("email", null);
        final String password = auth.getString("password", null);
        String token = auth.getString("token", null);
        String timeStamp = auth.getString("timeStamp", null);

        double tokenTime;
        if(timeStamp == null){
            tokenTime = 0.0;
        } else {
            tokenTime = Double.parseDouble(auth.getString("timestamp", null));
        }

        // check if email and token are non-null
        if (email != null && token != null && password != null) {

            // get current time
            final double timeNow = System.currentTimeMillis();

            // compare to token time
            double diff = timeNow - tokenTime;

            // check if token is valid
            if (diff >= TOKEN_LIFETIME) {

                // token expired -- reauthenticate
                this.logIn(email, password, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        // parse response
                        Gson gson = new Gson();
                        Login login = gson.fromJson(response, Login.class);
                        String token = login.getToken();

                        // save new token and update time
                        SharedPreferences.Editor editor = auth.edit();
                        editor.putString("token", token);
                        editor.putString("timestamp", String.valueOf(timeNow));
                        editor.commit();

                    }},
                    new OneeResponseError(context){
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            // parse response
                            if(error.networkResponse != null && error.networkResponse.data != null){
                                String response = new String(error.networkResponse.data);
                                Gson gson = new Gson();
                                OneeAPI.OneeResponse res = gson.fromJson(response, OneeResponse.class);

                                if(res.getMessage() != null){
                                    displayMessage(res.getMessage());
                                }

                            } else {
                                //displayMessage("Whoops, something went wrong. [1]");
                            }

                        }
                    }

                );

                return true;

            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    public void getUpdate(final String email, final String token, Response.Listener<String> callback){

        // validate token
        this.checkPermission();

        // setup request URL
        Uri.Builder builder = new Uri.Builder();
        builder
                .appendQueryParameter("email", email)
                .appendQueryParameter("token", token);
        String url = URL_BASE + ENDPOINT_UPDATE + builder.build().toString();

        // Request a string response from the provided URL.
        OneeGetRequest request = new OneeGetRequest(url, callback, new OneeResponseError(context));

        // Add request to the request queue
        MySingleton.getInstance(this.context.getApplicationContext()).addToRequestQueue(request.getRequest());

    }

    public void createConnection(final String email, final String buddyemail, final String token, Response.Listener<String> callback){

        // set url
        String url = URL_BASE + ENDPOINT_CONNECT;

        // setup hashmap for post parameters
        HashMap<String,String> postParams = new HashMap<String,String>();
        postParams.put("email", email);
        postParams.put("buddyemail", buddyemail);
        postParams.put("token", token);

        // Request a string response from the provided URL.
        OneePostRequest request = new OneePostRequest(url, callback, new OneeResponseError(context), postParams);

        // Add request to the request queue
        MySingleton.getInstance(this.context.getApplicationContext()).addToRequestQueue(request.getRequest());

    }

    public void acceptConnection(final String email, final String connectionId, final String token, Response.Listener<String> callback){
        // set url
        String url = URL_BASE + ENDPOINT_ACCEPT;

        // setup hashmap for post parameters
        HashMap<String,String> postParams = new HashMap<String,String>();
        postParams.put("email", email);
        postParams.put("connectionId", connectionId);
        postParams.put("token", token);

        // Request a string response from the provided URL.
        OneePostRequest request = new OneePostRequest(url, callback, new OneeResponseError(context), postParams);

        // Add request to the request queue
        MySingleton.getInstance(this.context.getApplicationContext()).addToRequestQueue(request.getRequest());
    }

    public void endConnection(final String email, final String token, Response.Listener<String> callback){
        // set url
        String url = URL_BASE + ENDPOINT_END;

        // setup hashmap for post parameters
        HashMap<String,String> postParams = new HashMap<String,String>();
        postParams.put("email", email);
        postParams.put("token", token);

        // Request a string response from the provided URL.
        OneePostRequest request = new OneePostRequest(url, callback, new OneeResponseError(context), postParams);

        // Add request to the request queue
        MySingleton.getInstance(this.context.getApplicationContext()).addToRequestQueue(request.getRequest());
    }

    /*public void getConnection(final String email, final String token, Response.Listener<String> callback){

        // setup request URL
        Uri.Builder builder = new Uri.Builder();
        builder
                .appendQueryParameter("email", email)
                .appendQueryParameter("token", token);
        String url = URL_BASE + ENDPOINT_CONNECTION + builder.build().toString();

        // Request a string response from the provided URL.
        OneeGetRequest request = new OneeGetRequest(url, callback, new OneeResponseError(context));

        // Add request to the request queue
        MySingleton.getInstance(this.context.getApplicationContext()).addToRequestQueue(request.getRequest());

    }*/

    public void sendMessage(final String email, final String connectionId, final String message, final String token, Response.Listener<String> callback){
        // set url
        String url = URL_BASE + ENDPOINT_MESSAGE;

        // setup hashmap for post parameters
        HashMap<String,String> postParams = new HashMap<String,String>();
        postParams.put("email", email);
        postParams.put("connectionId", connectionId);
        postParams.put("message", message);
        postParams.put("token", token);

        // Request a string response from the provided URL.
        OneePostRequest request = new OneePostRequest(url, callback, new OneeResponseError(context), postParams);

        // Add request to the request queue
        MySingleton.getInstance(this.context.getApplicationContext()).addToRequestQueue(request.getRequest());
    }

    public void getRecentConnections(String email, String token, Response.Listener<String> callback){

        // setup request URL
        Uri.Builder builder = new Uri.Builder();
        builder
                .appendQueryParameter("email", email)
                .appendQueryParameter("token", token);
        String url = URL_BASE + ENDPOINT_RECENT + builder.build().toString();

        // Request a string response from the provided URL.
        OneeGetRequest request = new OneeGetRequest(url, callback, new OneeResponseError(context));

        // Add request to the request queue
        MySingleton.getInstance(this.context.getApplicationContext()).addToRequestQueue(request.getRequest());

    }

    public void findUsers(String email, String token, String search, Response.Listener<String> callback){

        // setup request URL
        Uri.Builder builder = new Uri.Builder();
        builder
                .appendQueryParameter("email", email)
                .appendQueryParameter("token", token)
                .appendQueryParameter("search", search);
        String url = URL_BASE + ENDPOINT_FIND + builder.build().toString();

        // Request a string response from the provided URL.
        OneeGetRequest request = new OneeGetRequest(url, callback, new OneeResponseError(context));

        // Add request to the request queue
        MySingleton.getInstance(this.context.getApplicationContext()).addToRequestQueue(request.getRequest());

    }

    public class OneeGetRequest{

        private StringRequest req;

        public OneeGetRequest(String url, Response.Listener<String> callback, OneeResponseError errorHandler){

            this.req = new StringRequest(Request.Method.GET, url, callback, new OneeResponseError(context)){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Content-Type", "application/x-www-form-urlencoded");
                    return params;
                }
            };
        }

        public StringRequest getRequest(){
            return this.req;
        }
    }

    public class OneePostRequest{

        private StringRequest req;

        public OneePostRequest(String url, Response.Listener<String> callback, OneeResponseError errorHandler, final HashMap<String,String> postParams){
            this.req = new StringRequest(Request.Method.POST, url, callback, errorHandler){
                @Override
                protected Map<String, String> getParams(){
                    return postParams;
                }
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Content-Type", "application/x-www-form-urlencoded");
                    return params;
                }

            };
        }

        public StringRequest getRequest(){
            return this.req;
        }
    }


    // Define a error response handler class for ONEE API
    public static class OneeResponseError implements Response.ErrorListener {

        private static Context context;

        public OneeResponseError(Context context){
            this.context = context;
        }

        public void onErrorResponse(VolleyError error) {

            // parse response
            if(error.networkResponse != null && error.networkResponse.data != null){
                String response = new String(error.networkResponse.data);
                Gson gson = new Gson();
                OneeAPI.OneeResponse res = gson.fromJson(response, OneeResponse.class);

                if(res.getMessage() != null){
                    displayMessage(res.getMessage());
                }

            } else {
                //displayMessage("Whoops, something went wrong. [2]");
            }

        }

        private Context getContext(){
            return this.context;
        }

        public void displayMessage(String toastString){
            Toast.makeText(this.getContext(), toastString, Toast.LENGTH_LONG).show();
        }

    }

    public class OneeResponse {
        public Context context;
        public String message;

        public String getMessage() {
            return message;
        }
    }

    public class Login extends OneeResponse {
        private String token;

        public String getToken(){
            return this.token;
        }
    }

    /*public class Verification extends OneeResponse {
        private String message;
        private Integer verified;

        public String getMessage(){
            return this.message;
        }

        public Integer getVerified(){
            return this.verified;
        }

    }*/

    public class ConnectionState extends OneeResponse{

        private Models.User user;
        private Models.User buddy;
        private Models.Connection active;
        private Models.Connection requested;
        private Models.Connection[] requests;
        private String username;

        public String getMessage(){
            return this.message;
        }

        // return new (not yet accepted) connection requests from other people
        public Models.Connection[] getConnectionRequests(){

            if(this.requests != null){
                // remove active connection, if present
                ArrayList<Models.Connection> reqs = new ArrayList<>(Arrays.asList(this.requests));
                int forRemoval = -1;
                for(int i = 0; i < reqs.size(); i++){
                    if(reqs.get(i).getAccepted() > 0){
                        forRemoval = i;
                    }
                }
                if(forRemoval >= 0){
                    reqs.remove(forRemoval);
                }

                // remove connection requested by user, if present
                forRemoval = -1;
                for(int i = 0; i < reqs.size(); i++){
                    if(reqs.get(i).getCreator().equals(user.getUsername())){
                        forRemoval = i;
                    }
                }
                if(forRemoval >= 0){
                    reqs.remove(forRemoval);
                }

                Models.Connection[] modReqs = new Models.Connection[reqs.size()];
                modReqs = reqs.toArray(modReqs);
                return modReqs;
            } else {
                Models.Connection[] modReqs = new Models.Connection[0];
                return modReqs;
            }
        }

        public Models.User getUser(){

            return this.user;
        }

        public Models.User getBuddy(){

            return this.buddy;
        }

        public Models.Connection getActiveConnection(){
            return this.active;
        }

        public Models.Connection getRequestedConnection(){
            return this.requested;
        }
    }

    public class SearchResults extends OneeResponse{

        private SearchResult[] results;

        public String getMessage() {
            return message;
        }

        public SearchResult[] getResults() {
            return results;
        }
    }

    public class SearchResult extends OneeResponse{
        private String id;
        private String name;
        private String username;
        private String phone;
        private String photo;

        public String getId(){
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        public String getUsername() {
            return this.username;
        }

        public String getPhone() {
            return this.phone;
        }

        public Bitmap getPhoto() {

            // TODO remove this and instead get base64 encoded string from server
            String stringPhoto = "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";
            Bitmap b = null;
            //b = stringToBitMap(stringPhoto);
            Log.d("test", "stringToBitMap: we made it here");
            try{
                byte [] encodeByte = Base64.decode(stringPhoto.getBytes(), Base64.DEFAULT);
                Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
                return bitmap;
            }catch(Exception e){
                e.getMessage();
                return null;
            }
            //return b;
        }
    }

    /*public class ConnectionCreated extends OneeResponse{
        private Models.Connection connection;
        public Models.Connection getConnection(){
            return this.connection;
        }
    }

    public class ConnectionAccepted extends OneeResponse{
        private Models.Connection connection;
        public Models.Connection getConnection(){
            return this.connection;
        }
    }

    public class ConnectionReceived extends OneeResponse{
        private Models.Connection connection;
        public Models.Connection getConnection(){
            return this.connection;
        }
    }

    public class MessageSent extends OneeResponse{
        private Models.Connection connection;
        public Models.Connection getConnection(){
            return this.connection;
        }
    }*/

    private Bitmap stringToBitMap(String encodedString){
        Log.d("test", "stringToBitMap: we made it here");
        try{
            byte [] encodeByte = Base64.decode(encodedString.getBytes(), Base64.DEFAULT);
            Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        }catch(Exception e){
            e.getMessage();
            return null;
        }
    }



}

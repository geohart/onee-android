package com.wearonee.oneeandroid;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.google.gson.Gson;

/**
 * Created by George on 4/12/2016.
 */
public class SearchResultAdapter extends BaseAdapter {

    private Context context;
    private Onee onee;
    private OneeAPI.SearchResult[] searchResults;
    private LayoutInflater inflater;

    public SearchResultAdapter(Context context, Onee onee, OneeAPI.SearchResult[] searchResults){
        this.onee = onee;
        this.context = context;
        this.searchResults = searchResults;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return this.searchResults.length;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent){

        // get instance of search result layout fragment
        View result = inflater.inflate(R.layout.fragment_search_result, null);

        // set name, username, and image fields
        TextView nameField = (TextView) result.findViewById(R.id.result_name);
        nameField.setText(this.searchResults[position].getName());

        TextView emailField = (TextView) result.findViewById(R.id.result_email);
        emailField.setText(this.searchResults[position].getUsername());

        ImageView imgField = (ImageView) result.findViewById(R.id.result_image);
        //imgField.setImageBitmap(this.searchResults[position].getPhoto());

        // setup onclick event listener -- trigger new intent
        result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean proceed = true;

                // create new connection:
                SharedPreferences auth = context.getSharedPreferences("auth", v.getContext().MODE_PRIVATE);
                final OneeAPI oneeAPI = new OneeAPI(context, true);

                /*// first check if connection exists
                if(onee.getConnectionState().getConnection() != null){
                    // show dialog warning
                    // if user declines then proceed = false;

                } */

                if(proceed){
                    oneeAPI.createConnection(
                        auth.getString("email", null),
                        searchResults[position].getUsername(),
                        auth.getString("token", null),
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                                Gson gson = new Gson();
                                OneeAPI.ConnectionState connectionCreated = gson.fromJson(response, OneeAPI.ConnectionState.class);
                                Toast.makeText(context, connectionCreated.getMessage(), Toast.LENGTH_LONG).show();

                                // update application connection state
                                onee.setConnectionState(connectionCreated);

                                // route to view connection activity
                                Intent intent = new Intent(context, HomeActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);

                            }
                        });
                }
            }
        });

        // return view
        return result;

    }

}

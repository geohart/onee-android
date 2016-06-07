package com.wearonee.oneeandroid;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;

public class NewConnectionActivity extends AppCompatActivity {

    private LinearLayout history;
    private LinearLayout results;
    private TextView noresults;
    private TextView nohistory;
    private Context context;
    private Onee onee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_connection);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.context = this;

        // set onee reference
        this.onee = (Onee) getApplication();

        // instantiate the listviews
        this.history = (LinearLayout) findViewById(R.id.history);
        this.results = (LinearLayout) findViewById(R.id.results);

        // instantiate textviews
        this.nohistory = (TextView) findViewById(R.id.no_recent_connections);
        this.noresults = (TextView) findViewById(R.id.no_search_results);

        // register text changed listener on search bar
        final TextInputEditText searchBar = (TextInputEditText) findViewById(R.id.searchBar);
        if (searchBar != null) {
            searchBar.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if(s.length() > 0){
                        // hide history
                        history.setVisibility(View.GONE);

                        if(s.length() > 3) {
                            // show results
                            results.setVisibility(View.VISIBLE);
                            // search
                            findUsers(context, s.toString());
                        } else {
                            // hide stale results
                            results.setVisibility(View.GONE);
                        }
                    } else {
                        // hide search and show history
                        results.setVisibility(View.GONE);
                        history.setVisibility(View.VISIBLE);
                    }
                }
            });
        }

        loadRecentConnections(this);
    }

    private void loadRecentConnections(final Context context) {

        // create a new instance of ONEE API
        OneeAPI oneeAPI = new OneeAPI(this, true);

        // get shared preferences
        SharedPreferences auth = getSharedPreferences("auth", MODE_PRIVATE);

        // get recent connections
        oneeAPI.getRecentConnections(auth.getString("email", null), auth.getString("token", null),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        // parse response
                        Gson gson = new Gson();
                        OneeAPI.SearchResults results = gson.fromJson(response, OneeAPI.SearchResults.class);

                        // populate list view
                        ListView listView = (ListView) findViewById(R.id.history_list);
                        SearchResultAdapter sra = new SearchResultAdapter(context, onee, results.getResults());
                        listView.setAdapter(sra);

                        // show/hide "no history"
                        if (results.getResults().length > 0) {
                            nohistory.setVisibility(View.GONE);
                        } else {
                            nohistory.setVisibility(View.VISIBLE);
                        }

                    }
                });
    }

    private void findUsers(final Context context, String search){

        if(search.length() > 3) {

            // create a new instance of ONEE API
            OneeAPI oneeAPI = new OneeAPI(this, true);

            // get shared preferences
            final SharedPreferences auth = getSharedPreferences("auth", MODE_PRIVATE);

            // get recent connections
            oneeAPI.findUsers(auth.getString("email", null), auth.getString("token", null), search,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            // parse response
                            Gson gson = new Gson();
                            OneeAPI.SearchResults results = gson.fromJson(response, OneeAPI.SearchResults.class);

                            // remove self, if present
                            ArrayList<OneeAPI.SearchResult> res = new ArrayList<>(Arrays.asList(results.getResults()));
                            int forRemoval = -1;
                            for(int i = 0; i < res.size(); i++){
                                if(res.get(i).getUsername().equals(auth.getString("email", null))){
                                    forRemoval = i;
                                }
                            }
                            if(forRemoval >= 0){
                                res.remove(forRemoval);
                            }
                            OneeAPI.SearchResult[] modRes = new OneeAPI.SearchResult[res.size()];
                            modRes = res.toArray(modRes);

                            // populate built-in list view
                            ListView listView = (ListView) findViewById(R.id.results_list);
                            SearchResultAdapter sra = new SearchResultAdapter(context, onee, modRes);
                            listView.setAdapter(sra);

                            // show/hide "no results"
                            if(results.getResults().length > 0){
                                noresults.setVisibility(View.GONE);
                            } else {
                                noresults.setVisibility(View.VISIBLE);
                            }

                        }
                    });
        }
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

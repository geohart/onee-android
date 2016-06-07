package com.wearonee.oneeandroid;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity /*implements LoaderCallbacks<Cursor>*/ {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    //private static final int REQUEST_READ_CONTACTS = 0;

    // UI references.
    private TextInputEditText mEmailView;
    private TextInputEditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private LinearLayout mLoginLayout;
    private LinearLayout mSignupLayout;
    private OneeAPI oneeAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /*// Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        mPasswordView = (TextInputEditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });*/

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        mLoginLayout = (LinearLayout) findViewById(R.id.email_login_form);
        mSignupLayout = (LinearLayout) findViewById(R.id.email_signup_form);
        mEmailView = (TextInputEditText) findViewById(R.id.email);
        mPasswordView = (TextInputEditText) findViewById(R.id.password);

        // determine auth state
        oneeAPI = new OneeAPI(getApplicationContext(), false);
        final SharedPreferences auth = getSharedPreferences("auth", Context.MODE_PRIVATE);

        if(oneeAPI.checkToken()){

            // token checks out -- start home activity
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);

            Onee onee = (Onee) getApplication();
            onee.setIsAuthenticated(true);

            // setup login view for later
            mLoginLayout.setVisibility(View.GONE);
            mSignupLayout.setVisibility(View.VISIBLE);
            mPasswordView.setText("");
            showProgress(false);

        } else {

            // hide progress spinner
            showProgress(false);

            if (auth.getString("email", null) != null){
                // email saved but no password info -- display log in view

                // prepopulate email field
                mEmailView.setText(auth.getString("email", "Enter your email here"));

                // show log in fields and hide sign up fields
                mLoginLayout.setVisibility(View.VISIBLE);
                mSignupLayout.setVisibility(View.GONE);

            } else {
                // no information -- display sign up view
                // hide log in fields and show sign up fields
                mLoginLayout.setVisibility(View.GONE);
                mSignupLayout.setVisibility(View.VISIBLE);
            }
        }

    }

    /*private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }*/

    /**
     * Callback received when a permissions request has been completed.
     */
    /*@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }*/


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin(View view) {

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            signIn(email, password);
        }


    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() >= 0;
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

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

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
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /*@Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }*/

    public void signIn(final String email, final String password) {

        // get current time
        final double timeNow = System.currentTimeMillis();

        // get saved preferences
        final SharedPreferences auth = getSharedPreferences("auth", Context.MODE_PRIVATE);

        // get instance of OneeAPI


        this.oneeAPI = new OneeAPI(this, false);

        // call login method
        oneeAPI.logIn(email, password, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.d("test", "we made it!!!!");

                // parse response
                Gson gson = new Gson();
                OneeAPI.Login login = gson.fromJson(response, OneeAPI.Login.class);
                String token = login.getToken();

                // save new token and update time
                SharedPreferences.Editor editor = auth.edit();
                editor.putString("email", email);
                editor.putString("password", password);
                editor.putString("token", token);
                editor.putString("timestamp", String.valueOf(timeNow));
                editor.commit();

                Onee onee = (Onee) getApplication();
                onee.setIsAuthenticated(true);

                // launch home activity
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(intent);

                // setup login view for later
                mLoginLayout.setVisibility(View.VISIBLE);
                mSignupLayout.setVisibility(View.GONE);
                mPasswordView.setText("");
                mEmailView.setText(auth.getString("email", "Enter your email"));
                showProgress(false);

            }},
            new OneeAPI.OneeResponseError(this){
                @Override
                public void onErrorResponse(VolleyError error) {

                    // parse response
                    if(error.networkResponse != null && error.networkResponse.data != null){
                        String response = new String(error.networkResponse.data);
                        Gson gson = new Gson();
                        OneeAPI.OneeResponse res = gson.fromJson(response, OneeAPI.OneeResponse.class);

                        if(res.getMessage() != null){
                            displayMessage(res.getMessage());
                        }

                    } else {
                        displayMessage("Whoops, something went wrong.");
                    }

                    // hide progress spinner
                    showProgress(false);

                }
            }
        );
    }

    public void createAccount(View view){
        // create a new instance of ONEE API
        final OneeAPI oneeAPI = new OneeAPI(this, true);

        final View thisView = view;

        // get field values
        TextInputEditText tv_email = (TextInputEditText) findViewById(R.id.create_email);
        TextInputEditText tv_name = (TextInputEditText) findViewById(R.id.create_name);
        TextInputEditText tv_phone = (TextInputEditText) findViewById(R.id.create_phone);
        TextInputEditText tv_password = (TextInputEditText) findViewById(R.id.create_password);
        final String email = tv_email.getText().toString();
        final String name = tv_name.getText().toString();
        final String phone = tv_phone.getText().toString();
        final String password = tv_password.getText().toString();

        // create account
        oneeAPI.createAccount(email, name, phone, password,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Gson gson = new Gson();
                        OneeAPI.OneeResponse accountCreated = gson.fromJson(response, OneeAPI.OneeResponse.class);

                        // get shared preferences
                        SharedPreferences auth = getSharedPreferences("auth", MODE_PRIVATE);
                        SharedPreferences.Editor editor = auth.edit();
                        editor.putString("email", email);
                        editor.commit();

                        // switch to existing account to prompt login
                        runOnUiThread(new Runnable() {
                            public void run() {
                                // update UI component
                                mEmailView.setText(email);
                            }
                        });
                        existingAccount(thisView);

                        // send toast
                        Toast.makeText(oneeAPI.getContext(), "Welome, " + name + "! Please login to complete account setup.", Toast.LENGTH_LONG).show();

                    }
                });
    }

    public void newAccount(View view){
        // hide log in fields and show sign up fields
        mLoginLayout.setVisibility(View.GONE);
        mSignupLayout.setVisibility(View.VISIBLE);
    }

    public void existingAccount(View view) {
        // show log in fields and hide sign up fields
        mLoginLayout.setVisibility(View.VISIBLE);
        mSignupLayout.setVisibility(View.GONE);
    }
}


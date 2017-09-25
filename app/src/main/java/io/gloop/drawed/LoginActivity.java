package io.gloop.drawed;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import io.gloop.Gloop;
import io.gloop.GloopLogger;
import io.gloop.drawed.model.UserInfo;
import io.gloop.drawed.utils.SharedPreferencesStore;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements OnClickListener {

    private static final int RC_SIGN_IN = 3;

    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private GoogleApiClient mGoogleApiClient;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = (new StrictMode.ThreadPolicy.Builder()).permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        // Set up the login form.
        mEmailView = (EditText) findViewById(R.id.email);
        mEmailView.clearFocus();
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    login();
                    return true;
                }
                return false;
            }
        });

        Button signInButton = (Button) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(this);

        Button registerButton = (Button) findViewById(R.id.register_button);
        registerButton.setOnClickListener(this);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        setUpGoogleAuthentication();

        setUpFacebookAuthentication();

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    }


    private void setUpFacebookAuthentication() {
        callbackManager = CallbackManager.Factory.create();

        final LoginButton loginButton = (LoginButton) findViewById(R.id.facebook_login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile", "email"));

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                Log.d("Gloop", loginResult.toString());

                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                Log.v("LoginActivity", response.toString());

                                // Application code
                                try {
                                    String email = object.getString("email");
                                    String name = object.getString("name");


                                    String password = loginResult.getAccessToken().getUserId();

                                    UserInfo userInfo = Gloop.all(UserInfo.class).where().equalsTo("email", email).first();
                                    if (userInfo != null) {
                                        userInfo.setEmail(email);
                                        userInfo.setUserName(name);
                                        userInfo.setImageURL(Uri.parse("https://graph.facebook.com/" + loginResult.getAccessToken().getUserId() + "/picture?type=large"));
                                        userInfo.save();
                                    } else {
                                        userInfo = new UserInfo();
                                        userInfo.setEmail(email);
                                        userInfo.setUserName(name);
                                        userInfo.setImageURL(Uri.parse("https://graph.facebook.com/" + loginResult.getAccessToken().getUserId() + "/picture?type=large"));
                                        userInfo.save();
                                    }


                                    if (Gloop.login(email, password)) {
                                        // keep user logged in
                                        SharedPreferencesStore.setUser(email, password);


                                        showProgress(false);

                                        Intent i = new Intent(getApplicationContext(), BoardListActivity.class);
                                        startActivity(i);
                                        finish();
                                    } else {
                                        if (Gloop.register(email, password)) {

                                            SharedPreferencesStore.setUser(email, password);

                                            Intent i = new Intent(getApplicationContext(), BoardListActivity.class);
                                            startActivity(i);
                                            finish();
                                        }
                                    }


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email");
                request.setParameters(parameters);
                request.executeAsync();

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException e) {
                GloopLogger.e(e);
            }
        });
    }


    private void setUpGoogleAuthentication() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.e("Gloop", "Something went wrong: " + connectionResult);
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        findViewById(R.id.google_sign_in_button).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.google_sign_in_button:
                signIn();
                break;
            case R.id.register_button:
                register();
                break;
            case R.id.sign_in_button:
                login();
                break;
        }
    }


    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }

        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d("Gloop", "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();

            String email = acct.getEmail();
            String password = acct.getId();

            if (Gloop.login(email, password)) {
                // keep user logged in
                SharedPreferencesStore.setUser(email, password);

                createUserInfo(acct);

                showProgress(false);

                Intent i = new Intent(getApplicationContext(), BoardListActivity.class);
                startActivity(i);
                finish();
            } else {
                if (Gloop.register(email, password)) {

                    createUserInfo(acct);

                    SharedPreferencesStore.setUser(email, password);

                    Intent i = new Intent(getApplicationContext(), BoardListActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        } else {
            // Signed out, show unauthenticated UI.
            Log.e("Gloop", "something went wrong");
        }
    }

    private void createUserInfo(GoogleSignInAccount acct) {
        UserInfo userInfo = Gloop.all(UserInfo.class).where().equalsTo("email", acct.getEmail()).first();
        if (userInfo != null) {
            userInfo.setEmail(acct.getEmail());
            userInfo.setUserName(acct.getDisplayName());
            userInfo.setImageURL(acct.getPhotoUrl());
            userInfo.save();
        } else {
            userInfo = new UserInfo();
            userInfo.setEmail(acct.getEmail());
            userInfo.setUserName(acct.getDisplayName());
            userInfo.setImageURL(acct.getPhotoUrl());
            userInfo.save();
        }
    }

    private void login() {
        attempt(false);
    }

    private void register() {
        attempt(true);
    }

    private void attempt(boolean isRegister) {
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
            attempt(email, password, isRegister);
        }
    }

    private void attempt(String email, String password, boolean isRegister) {

        // Show a progress spinner, and kick off a background task to
        // perform the user login attempt.
        showProgress(true);

        if (isRegister) {
            // Register user using gloop
            if (Gloop.register(email, password)) {

                SharedPreferencesStore.setUser(email, password);

                Intent i = new Intent(getApplicationContext(), BoardListActivity.class);
                startActivity(i);
                finish();
            }
        } else {
            // Login using Gloop
            if (Gloop.login(email, password)) {
                // keep user logged in
                SharedPreferencesStore.setUser(email, password);

                showProgress(false);

                Intent i = new Intent(getApplicationContext(), BoardListActivity.class);
                startActivity(i);
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
                showProgress(false);
            }
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
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
}


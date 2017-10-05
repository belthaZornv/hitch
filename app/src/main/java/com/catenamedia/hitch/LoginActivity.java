package com.catenamedia.hitch;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

        private static final String TAG = "GoogleActivity";
        private static final int RC_SIGN_IN = 9001;

        // [START declare_auth]
        private FirebaseAuth mAuth;
        // [END declare_auth]

        private GoogleApiClient mGoogleApiClient;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_login);

            // Button listeners
            findViewById(R.id.sign_in_button).setOnClickListener(this);

            // [START config_signin]
            // Configure Google Sign In
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken("407235008799-c146djplsj1olitoej1jaiaj9ajh3s7u.apps.googleusercontent.com")
                    .requestEmail()
                    .build();
            // [END config_signin]

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();

            // [START initialize_auth]
            mAuth = FirebaseAuth.getInstance();
            // [END initialize_auth]
        }

        // [START on_start_check_user]
        @Override
        public void onStart() {
            super.onStart();
            // Check if user is signed in (non-null) and update UI accordingly.
            FirebaseUser currentUser = mAuth.getCurrentUser();
            updateUI(currentUser);
        }
        // [END on_start_check_user]

        // [START onactivityresult]
        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
            if (requestCode == RC_SIGN_IN) {
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                if (result.isSuccess()) {
                    // Google Sign In was successful, authenticate with Firebase
                    GoogleSignInAccount account = result.getSignInAccount();
                    Toast.makeText(LoginActivity.this, "Before firebaseauth.",
                            Toast.LENGTH_SHORT).show();
                    firebaseAuthWithGoogle(account);
                } else {
                    // Google Sign In failed, update UI appropriately
                    // [START_EXCLUDE]
                    updateUI(null);
                    // [END_EXCLUDE]
                }
            }
        }
        // [END onactivityresult]

        // [START auth_with_google]
        private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
            Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
            Toast.makeText(LoginActivity.this, "Authentication before.",
                    Toast.LENGTH_SHORT).show();
            System.out.println(acct.getIdToken());
            AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            if (!task.isSuccessful()) {
                                Log.w(TAG, "signInWithCredential", task.getException());
                                Toast.makeText(LoginActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                            // [END_EXCLUDE]
                        }
                    });

            Toast.makeText(LoginActivity.this, "Authentication finished.",
                    Toast.LENGTH_SHORT).show();
        }
        // [END auth_with_google]

        // [START signin]
        private void signIn() {
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            Toast.makeText(LoginActivity.this, "After intent.",
                    Toast.LENGTH_SHORT).show();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }
        // [END signin]

        private void signOut() {
            // Firebase sign out
            mAuth.signOut();

            // Google sign out
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            updateUI(null);
                        }
                    });
        }

        private void revokeAccess() {
            // Firebase sign out
            mAuth.signOut();

            // Google revoke access
            Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            updateUI(null);
                        }
                    });
        }

        private void updateUI(FirebaseUser user) {
//            hideProgressDialog();
            if (user != null) {
                findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            } else {
                findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
                Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            // An unresolvable error has occurred and Google APIs (including Sign-In) will not
            // be available.
            Log.d(TAG, "onConnectionFailed:" + connectionResult);
            Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onClick(View v) {
            int i = v.getId();
            if (i == R.id.sign_in_button) {
                Toast.makeText(LoginActivity.this, "Signing in.",
                        Toast.LENGTH_SHORT).show();
                signIn();
            }
        }
    }
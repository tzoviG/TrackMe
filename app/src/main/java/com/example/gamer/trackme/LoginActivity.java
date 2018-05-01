package com.example.gamer.trackme;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FirebaseAuth mAuth;
//    private String mLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

//        final String mClass = getIntent().getExtras().getString("class");
        mAuth = FirebaseAuth.getInstance();

        Button btn_signIn = findViewById(R.id.login_btn_signin);
        btn_signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText txt_email = findViewById(R.id.login_txt_email);
                EditText txt_password = findViewById(R.id.login_txt_password);
                String email = txt_email.getText().toString();
                String pass = txt_password.getText().toString();
                if (!email.isEmpty() && !pass.isEmpty()) {
                    signIn(email, pass);
                } else {
                    Toast.makeText(LoginActivity.this, "Fill the fields.", Toast.LENGTH_LONG).show();
                }
            }
        });

        Button btn_signOut = findViewById(R.id.login_btn_signout);
        btn_signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(LoginActivity.this, "Account sign out!", Toast.LENGTH_LONG).show();
                updateUI(null);
            }
        });

        Button btn_createaccount = findViewById(R.id.login_btn_createaccount);
        btn_createaccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText txt_email = findViewById(R.id.login_txt_email);
                EditText txt_password = findViewById(R.id.login_txt_password);
                String email = txt_email.getText().toString();
                String pass = txt_password.getText().toString();
                if (!email.isEmpty() && !pass.isEmpty()) {
                    createAccount(email, pass);
                } else {
                    Toast.makeText(LoginActivity.this, "Fill the fields.", Toast.LENGTH_LONG).show();
                }
            }
        });

        Button btn_exit = findViewById(R.id.login_btn_exit);
        btn_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
//                if (mClass != "MainActivity"){
//                    if (mLogin != null){
//                        Intent _intent = new Intent(LoginActivity.this, MapActivity.class);
//                        startActivity(_intent);
//                    }
//                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void createAccount (String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this, "Account created: " + user.getEmail(), Toast.LENGTH_LONG).show();
                            updateUI(user);
//                            mLogin = user.getEmail();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_LONG).show();
                            updateUI(null);
//                            mLogin = null;
                        }

                        // ...
                    }
                });
    }

    private void signIn (String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this, "Account sign in: " + user.getEmail(), Toast.LENGTH_LONG).show();
                            updateUI(user);
//                            mLogin = user.getEmail();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_LONG).show();
                            updateUI(null);
//                            mLogin = null;
                        }

                        // ...
                    }
                });
    }

    private void updateUI(FirebaseUser currentUser) {
        getCurrentUser(currentUser);
    }

    private void getCurrentUser(FirebaseUser user) {
        //FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        TextView txt_username = findViewById(R.id.login_txt_username);
        EditText txt_email = findViewById(R.id.login_txt_email);
        EditText txt_password = findViewById(R.id.login_txt_password);
        if (user != null) {
            // Name, email address, and profile photo Url
            String name = user.getDisplayName();
            String email = user.getEmail();
            Uri photoUrl = user.getPhotoUrl();

            // Check if user's email is verified
            boolean emailVerified = user.isEmailVerified();

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getToken() instead.
            String uid = user.getUid();

            txt_username.setText(email);
            txt_email.setText("");
            txt_password.setText("");
        } else {
            txt_username.setText("");
        }
    }


}

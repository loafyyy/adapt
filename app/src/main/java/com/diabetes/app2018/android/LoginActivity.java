package com.diabetes.app2018.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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

    private Context mContext;
    private EditText emailEntry;
    private EditText passwordEntry;
    private Button signInButton;
    private Button signUpButton;
    private String email;
    private String password;

    private SharedPreferences sp;

    // Firebase Authentication
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null)
        // go to MainActivity if signed in already
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    // if user logs in successfully go to Main Activity
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // remember username
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(getResources().getString(R.string.pref_user), user.getUid());
            editor.apply();
            Intent intent = new Intent(mContext, MainActivity.class);
            startActivity(intent);
        }
    }

    private void sendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // email sent
                            FirebaseAuth.getInstance().signOut();
                        } else {
                            // email not sent
                            FirebaseAuth.getInstance().signOut();
                        }
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_login);

        mContext = this;
        mAuth = FirebaseAuth.getInstance();
        sp = getSharedPreferences(getResources().getString(R.string.pref), MODE_PRIVATE);

        emailEntry = (EditText) findViewById(R.id.email_entry);
        setCloseEditTextOnEnter(emailEntry);
        passwordEntry = (EditText) findViewById(R.id.password_entry);
        setCloseEditTextOnEnter(emailEntry);
        signInButton = (Button) findViewById(R.id.signin_button);

        // handle adding new data points
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // get x and y values from edit text
                email = emailEntry.getText().toString();
                password = passwordEntry.getText().toString();

                if (email == null || password == null || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(mContext, "Email or password empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener((Activity) mContext, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {

                                    // check if email verified
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (!user.isEmailVerified()) {
                                        // email is not verified
                                        mAuth.signOut();
                                        Toast.makeText(mContext, "Email not verified", Toast.LENGTH_SHORT).show();
                                    }
                                    // email verified
                                    else {
                                        updateUI(user);
                                    }
                                } else {
                                    Toast.makeText(mContext, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                    updateUI(null);
                                }
                            }
                        });
            }
        });

        signUpButton = (Button) findViewById(R.id.signup_button);
        signUpButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                // get x and y values from edit text
                email = emailEntry.getText().toString();
                password = passwordEntry.getText().toString();

                if (email == null || password == null || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(mContext, "Email or password empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener((Activity) mContext, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign up successful
                                    Toast.makeText(mContext, "Check your email to verify your account",
                                            Toast.LENGTH_SHORT).show();
                                    sendVerificationEmail();
                                } else {
                                    // Sign up failed
                                    Toast.makeText(mContext, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

    }

    // allows keyboard to close when enter is pressed
    private void setCloseEditTextOnEnter(EditText editText) {
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (keyEvent != null && textView != null && (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(textView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    return true;
                }
                return false;
            }
        });
    }
}

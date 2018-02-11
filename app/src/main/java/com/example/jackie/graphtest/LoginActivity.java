package com.example.jackie.graphtest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    private Context mContext;
    private EditText emailEntry;
    private EditText passwordEntry;
    private Button signinButton;

    private String email;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mContext = this;
        final Activity activity = (Activity) mContext;

        emailEntry = (EditText) findViewById(R.id.email_entry);
        setCloseEditTextOnEnter(emailEntry);
        passwordEntry = (EditText) findViewById(R.id.password_entry);
        setCloseEditTextOnEnter(emailEntry);
        signinButton = (Button) findViewById(R.id.signin_button);

        Intent intent = new Intent(this, MainActivity.class);

        // handle adding new data points
        signinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // get x and y values from edit text
                email = emailEntry.getText().toString();
                password = passwordEntry.getText().toString();

                Intent intent = new Intent(mContext, MainActivity.class);
                startActivity(intent);
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

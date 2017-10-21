package com.prabhav.boss.future_g;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    //defining view objects
    private TextView textView;
    private EditText editTextEmail;
    private EditText editTextPassword, editTextRePassword;
    private Button buttonSignup;
    private TextView textViewSignin;
    private ProgressDialog progressDialog;


    //defining firebaseauth object
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //initializing firebase auth object
        firebaseAuth = FirebaseAuth.getInstance();

        //if getCurrentUser does not returns null
        if (firebaseAuth.getCurrentUser() != null) {
            //that means user is already logged in
            //so close this activity
            finish();

            //and open profile activity
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }

        //initializing views
        textView = (TextView) findViewById(R.id.textView);
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        editTextRePassword = (EditText) findViewById(R.id.editTextRePassword);
        textViewSignin = (TextView) findViewById(R.id.textViewSignin);

        buttonSignup = (Button) findViewById(R.id.buttonSignup);

        progressDialog = new ProgressDialog(this);

        //attaching listener to button
        buttonSignup.setOnClickListener(this);
        textViewSignin.setOnClickListener(this);
    }

    //method for user login
    private void userLogin() {
        editTextRePassword.setVisibility(View.GONE);
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();


        //checking if email and passwords are empty
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter email", Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_LONG).show();
            return;
        }

        //if the email and password are not empty
        //displaying a progress dialog

        progressDialog.setMessage("Logging In Please Wait...");
        progressDialog.show();

        //logging in the user
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        //if the task is successfull
                        if (task.isSuccessful()) {
                            //start the profile activity
                            finish();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        }
                    }
                });

    }

    private void registerUser() {
        editTextRePassword.setVisibility(View.VISIBLE);
        //getting email and password from edit texts
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String rePassword = editTextRePassword.getText().toString().trim();


        //checking if email and passwords are empty
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter email", Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_LONG).show();
            return;
        }

        if (password.length()<6) {
            Toast.makeText(this, "Please enter password more than 6 ", Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(rePassword)) {
            Toast.makeText(this, "Please re-enter password", Toast.LENGTH_LONG).show();
            return;
        }

        if(!password.equals(rePassword)){
            Toast.makeText(this, "Please enter same password", Toast.LENGTH_LONG).show();
            return;
        }
        //if the email and password are not empty
        //displaying a progress dialog

        progressDialog.setMessage("Registering Please Wait...");
        progressDialog.show();

        //creating a new user
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            finish();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        } else {
                            Toast.makeText(LoginActivity.this, "Registration Error", Toast.LENGTH_LONG).show();
                        }
                        progressDialog.dismiss();
                    }
                });

    }

    @Override
    public void onClick(View view) {

        if (view == buttonSignup) {
            if (buttonSignup.getText().toString().equals("Sign Up"))
                registerUser();
            else
                userLogin();
        }

        if (view == textViewSignin) {
            if (buttonSignup.getText().toString().equals("Sign In")) {
                textViewSignin.setText("Already Registered? Sign In Here");
                buttonSignup.setText("Sign Up");
                editTextRePassword.setVisibility(View.VISIBLE);
                textView.setText("User Registration");
            } else {
                //open login activity when user taps on the already registered textview
                textViewSignin.setText("Sign Up Here!");
                buttonSignup.setText("Sign In");
                editTextRePassword.setVisibility(View.GONE);
                textView.setText("User Login");
            }
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (buttonSignup.getText().toString().equals("Sign up"))
            finish();
        else {
            textViewSignin.setText("Already Registered? Sign In Here");
            buttonSignup.setText("Sign Up");
            textView.setText("User Registration");
        }
    }
}
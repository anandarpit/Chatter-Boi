package com.arpit.miraquee.Auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import com.arpit.miraquee.SharedPreferences.Preferences;
import com.arpit.miraquee.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION;

public class Register extends AppCompatActivity {
    MaterialEditText email, password, cnfpassword;
    Button register, signin;
    FirebaseAuth mAuth;
    String userId, Email;
    TextView ChatterText;
    ConstraintLayout activity;
    FirebaseFirestore db;
    Preferences prefs;
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        email = findViewById(R.id.email);
        ChatterText = findViewById(R.id.register);
        password = findViewById(R.id.newpass);
        cnfpassword = findViewById(R.id.confirmpass);
        register = findViewById(R.id.button);
        signin = findViewById(R.id.signin);
        activity = findViewById(R.id.register_activity);

        TranslateAnimation animation =new TranslateAnimation(0,0,-50,0);
        animation.setDuration(1000);
        animation.setFillAfter(false);
        ChatterText.setAnimation(animation);


        mAuth = FirebaseAuth.getInstance();
        register.setAlpha(0f);
        register.animate().alpha(1).setDuration(2000);
        prefs = new Preferences(getApplicationContext());
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri;
                Intent intent = new Intent(Register.this, Log_in.class);
                intent.addFlags(FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);

            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Email = email.getText().toString().trim();
                final String Pass = password.getText().toString().trim();
                final String cnfpass = cnfpassword.getText().toString().trim();
                if (TextUtils.isEmpty(Email)) {
                    email.setError("Email is Required");
                    return;
                } else if (!isEmailValid(Email)) {
                    email.setError("Enter a valid Email");
                } else if (TextUtils.isEmpty(Pass)) {
                    password.setError("Password is Required");
                    return;
                } else if (Pass.length() < 7) {
                    password.setError("Password must contain atleast 8 characters");
                    return;
                } else if (!Pass.equals(cnfpass)) {
                    cnfpassword.setError("Passwords Do not match");
                    password.setError("Password Do not match");
                    return;
                } else {
                    //to Close the keyboard when the user hits the register button
                    View keyview = getCurrentFocus();
                    if (keyview != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }

                    registerUser(Email, Pass);
                    Snackbar.make(activity, "Sending Email...", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            }
        });
    }

    public boolean isEmailValid(String email) {
        Pattern emailPattern = Pattern.compile(".+@.+\\.[a-z]+");
        Matcher emailMatcher = emailPattern.matcher(email);
        return emailMatcher.matches();
    }

    public void registerUser(final String uEmail, final String uPass) {
        mAuth.createUserWithEmailAndPassword(uEmail, uPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    firebaseUser = mAuth.getCurrentUser();
                    userId= mAuth.getCurrentUser().getUid();
                    prefs.setData("uid",userId);
                    db = FirebaseFirestore.getInstance();
                    DocumentReference documentReference=db.collection("All Users").document(userId);
                    Map<String,Object> user=new HashMap<>();
                    user.put("Email_Id",uEmail);
                    user.put("Pass",uPass);
                    documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.v("Tag","On Success: User Profile Created for"+userId);
                            prefs.setData("Registered","true");
                        }
                    });

                    firebaseUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Snackbar.make(activity, "Verification Mail sent! Please Verify Your Email!", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(Register.this, username_page.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.putExtra("Email", uEmail);
                                    intent.putExtra("Pass",uPass);
                                    intent.addFlags(FLAG_ACTIVITY_NO_ANIMATION);
                                    startActivity(intent);

                                    finish();
                                }
                            }, 2500);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Snackbar.make(activity, "Error Occured", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        }
                    });
                } else {
                    Snackbar.make(activity, "Error! " + task.getException().getMessage(), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
    }
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0,0);
    }
}
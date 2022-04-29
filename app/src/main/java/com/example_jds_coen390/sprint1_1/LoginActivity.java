package com.example_jds_coen390.sprint1_1;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

//User login, will get the firebase instance associated with that user.
public class LoginActivity extends AppCompatActivity {
    ActivityResultLauncher<Intent> loginActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // Login or register successful
                    Intent data = result.getData();
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                } else {
                    // Login failed
                }
            });
    Button login_btn;

    
    //Checks for existing user in firebase
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login_btn = findViewById(R.id.login_btn);
        login_btn.setOnClickListener(this::LoginRegisterClick);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }


    }

    //Code to launch register or sign in page
    public void LoginRegisterClick(View view) {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build()
        );

        Intent intent = AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setTheme(R.style.LoginTheme)
                .setLogo(R.mipmap.ic_launcher)
                .build();

        loginActivityResult.launch(intent);
    }
}
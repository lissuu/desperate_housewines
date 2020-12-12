package com.example.desperatehousewines;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    final String TAG = "LOGIN";

    TextView txtUsername;
    TextView txtPassword;
    Switch swUserMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        txtUsername = findViewById(R.id.txtUsername);
        txtPassword = findViewById(R.id.txtPassword);
        swUserMode = findViewById(R.id.swUserMode);

        swUserMode.setOnCheckedChangeListener((buttonView, isChecked) -> buttonView.setText(isChecked ? "Palveluntarjoaja" : "Kuluttaja"));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnLogin: loginCurrentUser();
                break;
            case R.id.btnNewUser: requestNewUser();
                break;
            case R.id.btnPasswordLost: requestPassword();
                break;

            default: Log.e(TAG, "no onclick listener for view: " + view.getId());
        }
    }

    private void loginCurrentUser() {

    };

    private void requestNewUser() {

    };

    private void requestPassword() {

    }
}
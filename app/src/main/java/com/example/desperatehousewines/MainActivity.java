package com.example.desperatehousewines;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    final String TAG = "MAIN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btnToBrowse).setOnClickListener(this);
        findViewById(R.id.btnToSettings).setOnClickListener(this);
        findViewById(R.id.btnToCollection).setOnClickListener(this);
        findViewById(R.id.btnLogout).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.btnToBrowse:
                startActivity(new Intent(this, BrowseActivity.class));
                break;
            case R.id.btnToSettings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.btnToCollection:
                startActivity(new Intent(this, CollectionActivity.class));
                break;
            case R.id.btnLogout:
                startActivity(new Intent(this, LoginActivity.class));
                break;

            default: Log.e(TAG, "no onclick listener for view: " + view.getId());
        }
    }
}
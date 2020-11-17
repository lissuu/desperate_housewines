package com.example.desperatehousewines;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    final String TAG = "MAIN";
    Button btnToBrowse;
    Button btnToSettings;
    Button btnToCollection;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnToBrowse = findViewById(R.id.btnToBrowse);
        btnToSettings = findViewById(R.id.btnToSettings);
        btnToCollection = findViewById(R.id.btnToCollection);

        btnToBrowse.setOnClickListener(this);
        btnToSettings.setOnClickListener(this);
        btnToCollection.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.btnToBrowse:
                startActivity(new Intent(this, Browse.class));
                break;
            case R.id.btnToSettings:
                startActivity(new Intent(this, Settings.class));
                break;
            case R.id.btnToCollection:

                break;

            default: Log.e(TAG, "no onclick listener for view: " + view.getId());
        }
    }
}
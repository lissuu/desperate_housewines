package com.example.desperatehousewines;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements VolleyCallback {
    final String TAG = "MAIN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView textView = (TextView) findViewById(R.id.helloworld);

        DataHandler data = new DataHandler(this, this);

    }

    // VolleyCallback implementation
    @Override
    public void onSuccess() {
        Log.d(TAG, "[onSuccess]");
    }

    @Override
    public void onError() {
        Log.d(TAG, "[onError]");
    }
}
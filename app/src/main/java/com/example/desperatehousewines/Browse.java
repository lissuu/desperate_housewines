package com.example.desperatehousewines;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;

public class Browse extends AppCompatActivity {
    RecyclerView recyclerView;
    Context context;
    RecyclerView.Adapter recyclerView_Adapter;
    RecyclerView.LayoutManager recyclerViewLayoutManager;

    String[] numbers = {
            "one1",
            "2two",
            "three3",
            "4four",
            "five5",
            "6six",
            "seven7",
            "eight",
            "nine",
            "ten",
            "eleven666"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);

        context = getApplicationContext();
        recyclerView = findViewById(R.id.recyclerBrowse);

        recyclerViewLayoutManager = new GridLayoutManager(context, 2);
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        recyclerView_Adapter = new RecyclerBrowseAdapter(context,numbers);
        recyclerView.setAdapter(recyclerView_Adapter);
    }
}
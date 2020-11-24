package com.example.desperatehousewines;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BrowseActivity extends AppCompatActivity implements RestClient.Callback, View.OnClickListener {
    static final String TAG = "BROWSE";

    List<Item> items = new ArrayList<Item>();

    Context context;
    RecyclerView recyclerView;
    RecyclerView.Adapter recyclerView_Adapter;
    EditText inpSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);

        context = getApplicationContext();
        recyclerView = findViewById(R.id.recyclerBrowse);
        inpSearch = findViewById(R.id.inpSearch);

        RestClient.getInstance(this).get(RestClient.API.LIST, this);
        recyclerView.setLayoutManager(new GridLayoutManager(context, 2));
    }

    private void updateRecyclerView (List<Item> tempList) {
        Log.d(TAG, "updating recycler view");

        recyclerView_Adapter = new RecyclerBrowseAdapter(context, tempList == null ? items : tempList);
        recyclerView.setAdapter(recyclerView_Adapter);
    }

    private List<Item> getItemsByName (String name) {
        Log.d(TAG, "getting items by name: " + name);
        List<Item> result = new ArrayList<Item>();

        for (Item i : items) {
            if (i.getName().toLowerCase() == name.toLowerCase())
                result.add(i);
        }

        Log.d(TAG, "found " + result.size() + " items");
        return result;
    }

    @Override
    public void onResponse(JSONArray array) {
        Log.d(TAG, "clearing " + items.size() + " items");
        items.clear();

        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject row = array.getJSONObject(i);
                Item item = new Item(row);

                if (item.isValid())
                    items.add(item);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Log.d(TAG, "has " + items.size() + " items");
        updateRecyclerView(null);
    }

    @Override
    public void onError(VolleyError err) {
        Log.e(TAG, "volley error!");
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "onclick: " + v.getId());

        switch (v.getId()) {
            case R.id.btnSearch:
                String search = inpSearch.getText().toString();
                Log.d(TAG, "search string: " + search);

                itemsToString(getItemsByName(search));
                break;

            default:
                Log.e(TAG, "no onclick case for id: " + v.getId());
        }
    }

    private void itemsToString (List<Item> itemList) {
        Log.d(TAG, "item list size: " + itemList.size());

        for (Item i : itemList)
            Log.d(TAG, i.toString());
    }
}
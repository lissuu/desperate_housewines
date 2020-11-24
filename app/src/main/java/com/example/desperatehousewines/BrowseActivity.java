package com.example.desperatehousewines;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BrowseActivity extends AppCompatActivity implements RestClient.Callback {
    static final String TAG = "BROWSE";

    List<Item> items = new ArrayList<Item>();

    Context context;
    RecyclerView recyclerView;
    EditText inpSearch;
    Button btnSearch;
    Button btnClearSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);

        context = getApplicationContext();
        recyclerView = findViewById(R.id.recyclerBrowse);
        inpSearch = findViewById(R.id.inpSearch);
        btnSearch = findViewById(R.id.btnSearch);
        btnClearSearch = findViewById(R.id.btnClearSearch);

        // Fetches data from the API and calls RestClient.Callback implementation when Volley is done.
        RestClient.getInstance(this).get(RestClient.API.LIST, this);
        recyclerView.setLayoutManager(new GridLayoutManager(context, 2));

        /*
         * ON CLICK LISTENERS
         */

        // Tries to determine from user given input what was meant to be searched. Search string is
        // always lower cased and trimmed out of leading and trailing spaces.
        btnSearch.setOnClickListener(v -> {
            closeKeyboard();
            String search = inpSearch.getText().toString().toLowerCase().trim();
            List<Item> result = new ArrayList<>();

            if (search.equals("")) {
                // Clear
                updateRecyclerView(null);
                return;
            } else if (tryParseInt(search)) {
                // Search by year
                result = getItemsByYear(Integer.parseInt(search));
            } else {
                // Search by name
                result = getItemsByName(search);
            }

            Toast.makeText(this, result.size() > 0 ? "Osumia " + result.size() + " kappaletta" : "Haulla ei löytynyt yhtään tuotetta", Toast.LENGTH_LONG).show();
            updateRecyclerView(result);
        });

        // Function for clearing search input and restoring view to its non-searched look.
        btnClearSearch.setOnClickListener(v -> {
            closeKeyboard();
            inpSearch.setText("");
            updateRecyclerView(null);
        });
    }

    // Creates and sets a new adapter for recycler view. See RecyclerBrowseAdapter class for
    // further details.
    private void updateRecyclerView (List<Item> tempList) {
        if (tempList == null) {
            Log.d(TAG, "updating recycler view with full list");
            recyclerView.setAdapter(new RecyclerBrowseAdapter(context, items));
        } else {
            Log.d(TAG, "updating recycler view with temp/partial list");
            recyclerView.setAdapter(new RecyclerBrowseAdapter(context, tempList));
        }
    }

    // Searches items list by name with 'contains' method. Case is ignored and a match can be at any
    // string position.
    private List<Item> getItemsByName (String search) {
        List<Item> result = new ArrayList<Item>();

        if (search.equals(""))
            return result;

        Log.d(TAG, "name search called with '" + search + "'");

        for (Item i : items) {
            String itemName = i.getName().toLowerCase();

            if (itemName.contains(search)) {
                Log.d(TAG, "\t\t[" + result.size() + "]\t" + itemName);
                result.add(i);
            }
        }

        Log.d(TAG, "search returning with " + result.size() + " items");
        return result;
    }

    // Searches items list by a year.
    // Note: alko's api has a lot of blanks for this information
    private List<Item> getItemsByYear (int search) {
        List<Item> result = new ArrayList<Item>();

        Log.d(TAG, "year search called with '" + search + "'");

        for (Item i : items) {
            int item = i.getYear();

            if (item == search) {
                Log.d(TAG, "\t\t[" + result.size() + "]\t" + item);
                result.add(i);
            }
        }

        Log.d(TAG, "search returning with " + result.size() + " items");
        return result;
    }

    // Closes soft keyboard via InputMethodManager.
    private void closeKeyboard() {
        View view = this.getCurrentFocus();

        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // Returns true if given string can be parsed to an integer.
    private boolean tryParseInt(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Debugging purposes only
    private void itemsToString (List<Item> itemList) {
        Log.d(TAG, "item list size: " + itemList.size());

        for (Item i : itemList)
            Log.d(TAG, i.toString());
    }

    // RestClient.Callback implementation
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

    // RestClient.Callback implementation
    @Override
    public void onError(VolleyError err) {
        Log.e(TAG, "volley error!");
    }
}
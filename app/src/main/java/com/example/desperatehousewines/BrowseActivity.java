package com.example.desperatehousewines;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.android.volley.VolleyError;

import java.util.List;

import static android.view.KeyEvent.KEYCODE_ENTER;

public class BrowseActivity extends AppCompatActivity implements RestClient.AsyncData, RestClient.Response {
    static final String TAG = "BROWSE";

    private Context context;
    private RecyclerView recyclerView;
    private EditText inpSearch;

    private ProgressDialog loadDialog;
    private ProgressDialog parsingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);

        context = getApplicationContext();
        recyclerView = findViewById(R.id.recyclerBrowse);
        inpSearch = findViewById(R.id.inpSearch);

        // Dialog inits
        loadDialog = new ProgressDialog(this);
        loadDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loadDialog.setProgressNumberFormat(null);
        loadDialog.setMessage("Ladataan tietoja...");

        parsingDialog = new ProgressDialog(this);
        parsingDialog.setMessage("Käsitellään tietoja...");
        parsingDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        parsingDialog.setProgressNumberFormat(null);
        parsingDialog.setProgress(0);
        parsingDialog.setMax(100);

        recyclerView.setLayoutManager(new GridLayoutManager(context, 2));

        // Fetches drink data from the API and calls RestClient.Callback.AsyncData implementation when Volley is done.
        RestClient.getInstance(this).fetchItemData(this);

        /* LISTENERS */
        findViewById(R.id.btnSearch).setOnClickListener(v -> doSearch());

        findViewById(R.id.btnClearSearch).setOnClickListener(v -> {
            inpSearch.setText("");
            doSearch();
        });

        inpSearch.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KEYCODE_ENTER)
                doSearch();

            return super.onKeyDown(keyCode, event);
        });
    }

    // Tries to determine from user given input what was meant to be searched. Search string is
    // always lower cased and trimmed out of leading and trailing spaces.
    private void doSearch() {
        closeKeyboard();
        String search = inpSearch.getText().toString().toLowerCase().trim();

        Log.d(TAG, "doSearch: " + search);

        if (search.equals("")) {
            // Clear searches and show all items.
            updateRecyclerView(RestClient.getInstance(this).getItems());
            Log.d(TAG, "search cleared");
        } else if (tryParseInt(search)) {
            // Search by year
            updateRecyclerView(RestClient.getInstance(this).getItemsByYear(Integer.parseInt(search)));
            Log.d(TAG, "searching by year");

        } else {
            // Search by name
            updateRecyclerView(RestClient.getInstance(this).getItemsByName(search));
            Log.d(TAG, "searching by name");

        }
    }

    // Creates and sets a new adapter for recycler view. See RecyclerBrowseAdapter class for
    // further details.
    private void updateRecyclerView (List<Item> items) {
        recyclerView.setAdapter(new RecyclerBrowseAdapter(context, items, getSupportFragmentManager()));
    }

    // Closes soft keyboard via InputMethodManager.
    private void closeKeyboard() {
        View view = this.getCurrentFocus();

        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // Returns true/false if a given string can be parsed to an integer.
    private boolean tryParseInt(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Called before parsing starts and after volley has finished downloading JSON.
    @Override
    public void onPreExecute() {
        Log.d(TAG, "onPreExecute");

        loadDialog.show();
    }

    // Called when a JSON has been downloaded.
    @Override
    public void onResponse(RestClient.API api) {
        Log.d(TAG, "onResponse: " + api.toString());

        switch (api) {
            case FETCH_ITEMS:
                // Hide data downloading dialog and show parsing dialog.
                loadDialog.dismiss();
                parsingDialog.show();
            break;

            case FETCH_DRINKS:

            break;

            default: Log.e(TAG, "onResponse api switch defaults: " + api.toString());
        }
    }

    // AsyncTask's publishProgress calls this every percent it completes itself.
    @Override
    public void onTaskUpdate(int val) {
        parsingDialog.setProgress(val);
    }

    // Called when AsyncTask has been finished.
    @Override
    public void onTaskDone() {
        Log.d(TAG, "onTaskDone");

        // Hide parsing dialog and update adapter to show data.
        parsingDialog.dismiss();
        updateRecyclerView(RestClient.getInstance(this).getItems());

        // Fetches user added drinks
        RestClient.getInstance(this).fetchUserItems(this);
    }

    // Called on volley error.
    @Override
    public void onError(RestClient.API api, VolleyError err) {
        Log.e(TAG, "volley error!\n" + err.toString());

        AlertDialog alert = new AlertDialog.Builder(this).create();
        alert.setTitle("Virhe");
        alert.setMessage("Palvelimeen ei voitu muodostaa yhteyttä");
        alert.setButton(Dialog.BUTTON_POSITIVE,"OK", (dialog, which) -> finish());

        alert.show();
    }
}

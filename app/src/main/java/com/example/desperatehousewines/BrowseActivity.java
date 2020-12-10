package com.example.desperatehousewines;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.VolleyError;

import java.util.ArrayList;
import java.util.List;

import static android.view.KeyEvent.KEYCODE_ENTER;

public class BrowseActivity extends AppCompatActivity implements RestClient.Callback {
    static final String TAG = "BROWSE";

    protected List<Item> items = new ArrayList<Item>();

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

        recyclerView.setLayoutManager(new GridLayoutManager(context, 2));

        // Fetches data from the API and calls RestClient.Callback implementation when Volley is done.
        RestClient.getInstance(this).get(RestClient.API.LIST, this, this);

        /*
         * LISTENERS
         */

        btnSearch.setOnClickListener(v -> {
            doSearch();
        });

        inpSearch.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KEYCODE_ENTER)
                doSearch();

            return super.onKeyDown(keyCode, event);
        });

        btnClearSearch.setOnClickListener(v -> {
            clearSearch();
        });
    }

    // Tries to determine from user given input what was meant to be searched. Search string is
    // always lower cased and trimmed out of leading and trailing spaces.
    private void doSearch() {
        closeKeyboard();
        String search = inpSearch.getText().toString().toLowerCase().trim();

        if (search.equals("")) {
            // Clear
            updateRecyclerView(RestClient.getInstance(this).getItems());
            return;
        } else if (tryParseInt(search)) {
            // Search by year
            updateRecyclerView(RestClient.getInstance(this).getItemsByYear(Integer.parseInt(search)));
        } else {
            // Search by name
            updateRecyclerView(RestClient.getInstance(this).getItemsByName(search));
        }
    }

    // Function for clearing search input and restoring view to its non-searched look.
    private void clearSearch() {
        closeKeyboard();
        inpSearch.setText("");
        updateRecyclerView(null);
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

    // Returns true if given string can be parsed to an integer.
    private boolean tryParseInt(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // RestClient.Callback implementation
    @Override
    public void onResponse() {
        updateRecyclerView(RestClient.getInstance(this).getItems());
    }

    // RestClient.Callback implementation
    @Override
    public void onError(VolleyError err) {
        Log.e(TAG, "volley error!\n" + err.toString());

        AlertDialog alert = new AlertDialog.Builder(this).create();
        alert.setTitle("Virhe");
        alert.setMessage("Palvelimeen ei voitu muodostaa yhteyttä");
        alert.setButton(Dialog.BUTTON_POSITIVE,"OK", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        alert.show();
    }

}

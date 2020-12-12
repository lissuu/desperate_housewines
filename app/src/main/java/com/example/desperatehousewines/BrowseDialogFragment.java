package com.example.desperatehousewines;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.android.volley.VolleyError;

public class BrowseDialogFragment extends DialogFragment implements RestClient.Response {
    private static String TAG = "FRAG-ITEM";

    private int id = 0;
    private boolean isFavorite = true;

    private Button btnFavorite;

    public BrowseDialogFragment() { }

    public static BrowseDialogFragment newInstance(Item item) {
        BrowseDialogFragment frag = new BrowseDialogFragment();
        Bundle args = new Bundle();
        args.putAll(item.getBundle());
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_item, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnFavorite = view.findViewById(R.id.btnFragFavorite);
        Bundle b = getArguments();

        id = b.getInt("id");
        String name = b.getString("name");
        String cardtitle = b.getString("cardtitle");

        ((TextView) view.findViewById(R.id.txtFragSize)).setText(b.getString("size"));
        ((TextView) view.findViewById(R.id.txtFragPrice)).setText(b.getString("price"));
        ((TextView) view.findViewById(R.id.txtFragKeywords)).setText(b.getString("keywords"));
        ((TextView) view.findViewById(R.id.txtFragAlcohol)).setText(b.getString("alcohol"));
        ((TextView) view.findViewById(R.id.txtFragName)).setText(cardtitle.equals("") ? name : cardtitle);
        ((TextView) view.findViewById(R.id.txtFragSubHeader)).setText(b.getString("subheader"));

        setThisFavorite();

        btnFavorite.setOnClickListener((v -> {
            if (isFavorite) {
                RestClient.getInstance(getContext()).removeDrink(this, id);
            } else {
                RestClient.getInstance(getContext()).addDrink(this, id, 0);
            }
        }));
    }

    private boolean setThisFavorite() {
        isFavorite = RestClient.getInstance(getContext()).isUserItem(id);
        btnFavorite.setText(isFavorite ? "Poista omista juomista" : "Lisää omiin juomiin");

        return isFavorite;
    }

    @Override
    public void onError(RestClient.API api, VolleyError err) {
        Toast.makeText(getContext(), "Palvelinvirhe", Toast.LENGTH_LONG).show();
    }

    // onResponse is called when a drink has been added or removed and a new list will be fetched
    // afterwards. It's a lazy solution.
    @Override
    public void onResponse(RestClient.API api) {
        switch (api) {
            case REMOVE_DRINK:
            case ADD_DRINK:
                RestClient.getInstance(getContext()).fetchUserItems(this);
                Toast.makeText(getContext(), setThisFavorite() ? "Lisätty" : "Poistettu", Toast.LENGTH_LONG).show();
            break;

            case FETCH_DRINKS:
                setThisFavorite();
            break;

            default:
                Log.e(TAG, "onResponse switch defaults: " + api.toString());
        }
    }
}
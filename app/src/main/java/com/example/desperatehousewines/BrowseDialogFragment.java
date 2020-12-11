package com.example.desperatehousewines;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.android.volley.VolleyError;

import org.json.JSONObject;

public class BrowseDialogFragment extends DialogFragment implements RestClient.ResponseObject {
    private static String TAG = "FRAG-ITEM";
    private int id = 0;

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

        view.findViewById(R.id.btnFragFavorite).setOnClickListener((v -> {
            RestClient.getInstance(getContext()).addFavorite(this, id, 0);
        }));
    }

    @Override
    public void onError(VolleyError err) {
        Toast.makeText(getContext(), "Palvelinvirhe", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResponse(JSONObject resp) {
        Toast.makeText(getContext(), "Lisätty", Toast.LENGTH_LONG).show();
    }
}
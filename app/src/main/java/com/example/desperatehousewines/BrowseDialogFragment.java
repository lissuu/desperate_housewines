package com.example.desperatehousewines;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class BrowseDialogFragment extends DialogFragment {
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

        String size = b.getString("size");
        String price = b.getString("price");
        String keywords = b.getString("keywords");
        String alcohol = b.getString("alcohol");
        String name = b.getString("name");
        String cardtitle = b.getString("cardtitle");
        String subheader = b.getString("subheader");

        ((TextView) view.findViewById(R.id.txtFragSize)).setText(size);
        ((TextView) view.findViewById(R.id.txtFragPrice)).setText(price);
        ((TextView) view.findViewById(R.id.txtFragKeywords)).setText(keywords);
        ((TextView) view.findViewById(R.id.txtFragAlcohol)).setText(alcohol);
        ((TextView) view.findViewById(R.id.txtFragName)).setText(cardtitle.equals("") ? name : cardtitle);
        ((TextView) view.findViewById(R.id.txtFragSubHeader)).setText(subheader);
    }
}
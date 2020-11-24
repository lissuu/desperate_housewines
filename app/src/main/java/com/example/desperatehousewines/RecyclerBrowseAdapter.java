package com.example.desperatehousewines;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecyclerBrowseAdapter extends RecyclerView.Adapter<RecyclerBrowseAdapter.ViewHolder> {
    List<Item> items;
    Context context1;
    static final String TAG = "ADAPTER";


    public RecyclerBrowseAdapter(Context c, List<Item> items) {
        this.items = items;
        context1 = c;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Context context;

        public TextView txtName;
        public TextView txtPrice;
        public TextView txtSize;
        public TextView txtAlcohol;
        public TextView txtYear;

        public ViewHolder(Context c, View v){
            super(v);
            this.context = c;

            this.txtName = v.findViewById(R.id.txtName);
            this.txtPrice = v.findViewById(R.id.txtPrice);
            this.txtSize = v.findViewById(R.id.txtSize);
            this.txtAlcohol = v.findViewById(R.id.txtAlcohol);
            this.txtYear = v.findViewById(R.id.txtYear);

            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();

            if (pos != RecyclerView.NO_POSITION) {
                Item item = items.get(pos);
                Log.d(TAG, item.toString());
                Toast.makeText(context, "position: " + pos, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public RecyclerBrowseAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view1 = LayoutInflater.from(context1).inflate(R.layout.recycler_browse_item, parent, false);
        ViewHolder viewHolder1 = new ViewHolder(context1, view1);

        return viewHolder1;
    }

    @Override
    public void onBindViewHolder(ViewHolder Vholder, int pos) {
        Item item = items.get(pos);

        Vholder.txtName.setText(item.getCardTitle());
        Vholder.txtPrice.setText(item.getPriceAsString());
        Vholder.txtSize.setText(item.getSizeAsString());
        Vholder.txtAlcohol.setText(item.getAlcoholAsString());

        String year = item.getYearAsString();
        String producer = item.getProducer();

        if (year.equals("") && producer.equals("")) {
            Vholder.txtYear.setVisibility(View.GONE);
        } else {

            Vholder.txtYear.setText((!year.equals("") && !year.equals("")) ? year + " - " + producer : year + producer );
        }
    }

    @Override
    public int getItemCount(){
        return items.size();
    }
}

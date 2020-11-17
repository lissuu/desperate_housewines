package com.example.desperatehousewines;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

public class RecyclerBrowseAdapter extends RecyclerView.Adapter<RecyclerBrowseAdapter.ViewHolder> {
    String[] values;
    Context context1;

    public RecyclerBrowseAdapter(Context context2, String[] values2){
        values = values2;
        context1 = context2;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView textView;
        private Context context;

        public ViewHolder(Context c, View v){
            super(v);

            this.textView = v.findViewById(R.id.textview1);
            this.context = c;

            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();

            if (position != RecyclerView.NO_POSITION) {
                Toast.makeText(context, "position: " + position, Toast.LENGTH_SHORT).show();
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
    public void onBindViewHolder(ViewHolder Vholder, int position){
        Vholder.textView.setText(values[position]);
    }

    @Override
    public int getItemCount(){
        return values.length;
    }
}

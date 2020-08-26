package com.levigo.Carebase;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Map;

public class UDIAdapter extends RecyclerView.Adapter<UDIAdapter.UDIHolder> {

    private static final String TAG = "udiadapter";
    private MainActivity activity;
    private Map<String,Object> iDataset;
    private String di;

    public static class UDIHolder extends RecyclerView.ViewHolder {
        public TextView itemExpiration;
        public TextView itemQuantity;


        public UDIHolder(View view){
            super(view);
            itemExpiration = view.findViewById(R.id.udis_expirationdate);
            itemQuantity = view.findViewById(R.id.udis_quantity);
        }
    }

    public UDIAdapter(MainActivity activity, Map<String, Object> di, Map<String, Object> iDataset) {
        this.activity = activity;
        this.iDataset = iDataset;
        if(di.get("di") != null) {
            this.di = di.get("di").toString();
        }
    }

    @NonNull
    @Override
    public UDIAdapter.UDIHolder onCreateViewHolder (ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.udis_item, parent, false);

        UDIHolder vh = new UDIHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(UDIHolder holder, int position){
        Object[] types = iDataset.values().toArray();

        Object object = types[position];


        if(object instanceof Map) {

            final Map<String,Object> udi = (Map<String, Object>) object;
            System.out.println("udi here is" + udi);
            if(udi.containsKey("expiration")) {
                String expiration = "EXP " + udi.get("expiration").toString();
                holder.itemExpiration.setText(expiration);
            }
            if(udi.containsKey("quantity")) {
                //TODO PLURAL
                String quantity = udi.get("quantity").toString() + " Units";
                holder.itemQuantity.setText(quantity);
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(udi.containsKey("udi")) {
                        String udiString = udi.get("udi").toString();
                        //activity.startItemView(udiString);
                        activity.startItemViewOnly(udiString, di);
                    }
                }
            });
        }
        else {
            Log.d(TAG, "ERROR");
        }


    }

    @Override
    public int getItemCount(){
        if(iDataset.isEmpty()) {
            return 0;
        }
        else {
            return iDataset.size();
        }
    }
}

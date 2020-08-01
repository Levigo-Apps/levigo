

package com.levigo.levigoapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;

public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.FilterHolder> {
    private static final String TAG = "FilterAdapter";

    private boolean sign = true;
    private ArrayList<String> mList;
    private Context mContext;
    private HashMap<String, Object> mData;

    public FilterAdapter(Context context, HashMap<String, Object>  entries, ArrayList<String> list) {
        mList = list;
        mData = entries;
        mContext = context;
    }

    @NonNull
    @Override
    public FilterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Log.d(TAG, "onCreateViewHolder is called");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.filter_row, parent, false);
        FilterHolder holder = new FilterHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final FilterHolder holder, final int position) {
        //Log.d(TAG, "onBindViewHolder is called");

        holder.rowText.setText(mList.get(position));
        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d(TAG, "clicked on: " + mList.get(position));

                if ((sign)) {
                    holder.plusSign.setImageResource(R.drawable.ic_baseline_remove_24);
                    sign = false;
                } else {
                    holder.plusSign.setImageResource(R.drawable.ic_baseline_add);
                    sign = true;
                }
                Toast.makeText(mContext, mList.get(position), Toast.LENGTH_SHORT).show();
            }
        });
        CategoryAdapter categoryAdapter = new CategoryAdapter(mContext, mList.get(position) , mData);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        holder.categoryView.setLayoutManager(layoutManager);
        holder.categoryView.setAdapter(categoryAdapter);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class FilterHolder extends RecyclerView.ViewHolder {

        public RecyclerView categoryView;
        public ImageButton plusSign;
        public TextView rowText;
        public RelativeLayout parentLayout;

        public FilterHolder(View view) {
            super(view);
            plusSign = (ImageButton) view.findViewById(R.id.plus_icon);
            rowText = (TextView) view.findViewById(R.id.row_text);
            parentLayout = (RelativeLayout) view.findViewById(R.id.parent_layout);
            categoryView = (RecyclerView) view.findViewById(R.id.category_recycler);
        }
    }
}

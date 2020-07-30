

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

public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.FilterHolder>{
    private static final String TAG = "FilterAdapter";

    private boolean sign = true;
    private ArrayList<String> mTexts;
    private Context mContext;
    private String text = "";

    public FilterAdapter(Context context, ArrayList<String> text){
        mTexts = text;
        mContext = context;
    }

    @NonNull
    @Override
    public FilterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder is called");

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.filter_row, parent, false);
        FilterHolder holder = new FilterHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final FilterHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder is called");


        text = mTexts.get(position);
        holder.rowText.setText(mTexts.get(position));
        Log.d(TAG, "text1 is"+text);
        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d(TAG, "clicked on: "+mTexts.get(position));

                if ((sign)) {
                    holder.plusSign.setImageResource(R.drawable.ic_baseline_remove_24);
                    sign = false;
                } else {
                    holder.plusSign.setImageResource(R.drawable.ic_baseline_add);
                    sign = true;
                }
                Toast.makeText(mContext, mTexts.get(position), Toast.LENGTH_SHORT).show();
                

            }
        });
        //initCategory(holder,text);
    }
    public void initCategory(FilterHolder holder, String text){
        CategoryAdapter categoryAdapter = new CategoryAdapter(mContext, text);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        holder.categoryview.setHasFixedSize(true);
        holder.categoryview.setLayoutManager(layoutManager);
        holder.categoryview.setAdapter(categoryAdapter);
    }

    @Override
    public int getItemCount() {
        return mTexts.size();
    }

    public class FilterHolder extends RecyclerView.ViewHolder{

        RecyclerView categoryview;
        ImageButton plusSign;
        TextView rowText;
        RelativeLayout parentLayout;

        public FilterHolder(View view){
            super(view);
            plusSign = (ImageButton)view.findViewById(R.id.plus_icon);
            rowText = (TextView)view.findViewById(R.id.row_text);
            parentLayout = view.findViewById(R.id.parent_layout);
            categoryview = view.findViewById(R.id.categorized);
        }
    }
}

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
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryHolder> {
    private static final String TAG = "CategoryAdapter";

    private boolean sign = true;
    private ArrayList<String> mArray = new ArrayList<>();
    private Context mContext;

    public CategoryAdapter(Context context, String text) {
        Log.d(TAG, "text to compare" + text);

        if (text.equals("Sort By")) {
            mArray.add("Expiration Date - New to Old");
            mArray.add("Expiration Date - Old to New");
            mArray.add("Accession Number");
        } else {
            mArray.add("item 1");
            mArray.add("item 2");
            mArray.add("item 3");
        }
        mContext = context;
    }

    @NonNull
    @Override
    public CategoryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder is called");

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_row, parent, false);
        CategoryHolder holder = new CategoryHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final CategoryHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder is called");


        //holder.rowText.setText(mTexts.get(position));
        holder.categoryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d(TAG, "clicked on: " + mArray.get(position));

                //holder.plusSign.setImageResource(R.drawable.ic_baseline_remove_24);
                Toast.makeText(mContext, mArray.get(position), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mArray.size();
    }

    public class CategoryHolder extends RecyclerView.ViewHolder {

        ImageButton checkSign;
        TextView categoryText;
        RelativeLayout categoryLayout;

        public CategoryHolder(View view) {
            super(view);
            checkSign = (ImageButton) view.findViewById(R.id.check_icon);
            categoryText = (TextView) view.findViewById(R.id.category_text);
            categoryLayout = view.findViewById(R.id.category_layout);

        }
    }
}

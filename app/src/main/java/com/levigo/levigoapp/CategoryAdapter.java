package com.levigo.levigoapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

import com.levigo.levigoapp.MainActivity;
import com.levigo.levigoapp.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryHolder>{
    private static final String TAG = "CategoryAdapter";

    private boolean sign = true;
    private ArrayList<String> mArray = new ArrayList<>();
    private Context mContext;
    private HashMap<String, Object> mData;
    private Object[] categories;
    private String query;
    private String name;


    public CategoryAdapter(Context context, String text, HashMap<String, Object> data){
        //Log.d(TAG,"text to compare" + text);

        mContext = context;
        mData = data;
        categories = mData.values().toArray();

        if(text.equals("Type of Equipment")){

            name = "equipment_type";

            Object object = categories[0];
            Map<String, Object> types = (Map<String, Object>) object;
            Object[] type = types.values().toArray();
            for(int i = 0; i<type.length; i++){
                Object object2 = type[i];
                Map<String,Object> dis = (Map<String, Object>) object2;
                Object[] type1 = dis.values().toArray();
                Object object3 = type1[i];
                Map<String,Object> productType;
                productType = (Map<String,Object>) object3;
                Map<String,Object> object4 = (HashMap<String,Object>) productType.get("di");
                String item = object4.get("equipment_type").toString();
                mArray.add(item);
            }
        }
        else if(text.equals("Specification")){

        }
        else if(text.equals("Location")){

        }
        else if(text.equals("Procedure Type")){

        }
        else if(text.equals("Procedure Date")){

        }
        else if(text.equals("Sort By")){
            name = "expiration";
            mArray.add("Expiration Date - New to Old");
            mArray.add("Expiration Date - Old to New");
            mArray.add("Accession Number");
        }
        else{
            mArray.add("item 1");
            mArray.add("item 2");
            mArray.add("item 3");
        }
    }

    @NonNull
    @Override
    public CategoryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Log.d(TAG, "onCreateViewHolder is called");

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_row, parent, false);
        CategoryHolder holder = new CategoryHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final CategoryHolder holder, final int position) {
        //Log.d(TAG, "onBindViewHolder is called");
        holder.checkSign.setVisibility(View.GONE);
        holder.categoryText.setText(mArray.get(position));

        holder.categoryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d(TAG, "clicked on: "+mArray.get(position));
                if ((sign)) {
                    holder.checkSign.setVisibility(view.VISIBLE);
                    sign = false;
                } else {
                    holder.checkSign.setVisibility(View.GONE);
                    sign = true;
                }
                Toast.makeText(mContext, mArray.get(position), Toast.LENGTH_SHORT).show();

                Intent intent_main = new Intent(mContext, MainActivity.class);
                intent_main.putExtra("key", name);
                intent_main.putExtra("value",mArray.get(position));
                view.getContext().startActivity(intent_main);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mArray.size();
    }

    public class CategoryHolder extends RecyclerView.ViewHolder{

        ImageButton checkSign;
        TextView categoryText;
        RelativeLayout categoryLayout;

        public CategoryHolder(View view){
            super(view);
            checkSign = (ImageButton)view.findViewById(R.id.check_icon);
            categoryText = (TextView)view.findViewById(R.id.category_text);
            categoryLayout = view.findViewById(R.id.category_layout);

        }
    }
}

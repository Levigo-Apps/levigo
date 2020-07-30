package com.levigo.levigoapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.util.ArrayList;

public class FilterActivity extends AppCompatActivity {

    private static final String TAG = "SearchActivity";

    private ImageButton exitButton;

    private FilterAdapter adapter;
    private RecyclerView searchview;
    private ArrayList<String> mTexts = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_sort);

        exitButton =  (ImageButton)findViewById(R.id.exit_filter);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        initPut();
    }


    private void initPut(){
        Log.d(TAG, "initPut here");
        mTexts.add("Type of Equipment");
        mTexts.add("Specification");
        mTexts.add("Location");
        mTexts.add("Procedure Type");
        mTexts.add("Procedure Date");
        mTexts.add("Sort By");

        initFilter();
    }

    private void initFilter(){
        Log.d(TAG, "initSearch here");
        searchview = findViewById(R.id.search_recycler);
        searchview.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FilterAdapter(this, mTexts);
        searchview.setAdapter(adapter);
    }

}

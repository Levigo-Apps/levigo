package com.levigo.levigoapp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.Nullable;

public class NetworkActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network);

        String[] OPTIONS = new String[] {"Item 1", "Item 2", "Item 3", "Item 4"};

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                       getApplicationContext(),
                        R.layout.dropdown_menu_popup_item,
                        OPTIONS);

        AutoCompleteTextView editTextFilledExposedDropdown =
                findViewById(R.id.sort_textView);
        editTextFilledExposedDropdown.setAdapter(adapter);
    }
}


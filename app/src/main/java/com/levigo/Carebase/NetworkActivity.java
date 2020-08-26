package com.levigo.Carebase;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;

public class NetworkActivity extends Activity {

    private Button exitButton;
    ArrayList<String> SITES = new ArrayList<>(Arrays.asList("Option a", "Option b"));

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network);

        exitButton =  findViewById(R.id.exit_outlinedButton);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent_inventory = new Intent(getApplicationContext(), MainActivity.class);
                intent_inventory.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent_inventory);
                finish();

            }
        });
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


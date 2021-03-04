package org.getcarebase.carebase.activities.Main.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.getcarebase.carebase.R;

public class ProcedureStartFragment extends Fragment {
    public static final String TAG = ProcedureStartFragment.class.getName();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_procedure_start, container, false);
    }
}
package org.getcarebase.carebase.activities.Main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.activities.Main.fragments.AddEquipmentFragment;
import org.getcarebase.carebase.activities.Main.fragments.ProcedureInfoFragment;
import org.getcarebase.carebase.viewmodels.ProcedureViewModel;

public class AddProcedureActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ProcedureViewModel procedureViewModel = new ViewModelProvider(this).get(ProcedureViewModel.class);
        procedureViewModel.getCurrentStep().observe(this, this::setCurrentPage);
        procedureViewModel.getUserLiveData().observe(this,userResource -> procedureViewModel.setupRepositories());
        setContentView(R.layout.activity_generic);
    }

    private void setCurrentPage(Integer step) {
        if (step == 0){
            // go to add procedure details page
            Fragment currentFragment = new ProcedureInfoFragment();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.frame_layout,currentFragment,ProcedureInfoFragment.TAG);
            fragmentTransaction.commit();
        } else if (step == 1) {
            // go to add equipment page
            Fragment currentFragment = new AddEquipmentFragment();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment infoFragment = getSupportFragmentManager().findFragmentByTag(ProcedureInfoFragment.TAG);
            fragmentTransaction.detach(infoFragment);
            fragmentTransaction.add(R.id.frame_layout,currentFragment,AddEquipmentFragment.TAG);
            fragmentTransaction.commit();
        } else if (step == 2) {
            // back to add procedure detail page from add equipment page
            Fragment currentFragment = new ProcedureInfoFragment();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment infoFragment = getSupportFragmentManager().findFragmentByTag(ProcedureInfoFragment.TAG);
            fragmentTransaction.attach(infoFragment);
            fragmentTransaction.commit();
        } else {
            // go back to main activity
            finish();
        }


    }
}
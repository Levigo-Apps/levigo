package com.levigo.levigoapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AddEquipmentFragment extends Fragment {
    private static final String TAG = ItemDetailOfflineFragment.class.getSimpleName();
    private Activity parent;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference usersRef = db.collection("users");
    private LinearLayout linearLayout;

    private String mNetworkId;
    private String mHospitalId;
    private String mHospitalName;
    private String procedureDate;
    private String procedureName;
    private String procedureTimeIn;
    private String procedureTimeOut;
    private String fluoroTime;
    private String accessionNumber;

    private MaterialButton cancelButton;
    private MaterialButton saveButton;
    private FloatingActionButton addManually;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_addequipment, container, false);
        parent = getActivity();
        cancelButton = rootView.findViewById(R.id.equipment_cancel_button);
        saveButton = rootView.findViewById(R.id.equipment_save_button);
        MaterialToolbar topToolBar = rootView.findViewById(R.id.topAppBar);


        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        final DocumentReference currentUserRef = usersRef.document(userId);
        currentUserRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                String toastMessage;
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (Objects.requireNonNull(document).exists()) {
                        try {
                            mNetworkId = Objects.requireNonNull(document.get("network_id")).toString();
                            mHospitalId = Objects.requireNonNull(document.get("hospital_id")).toString();
                            mHospitalName = Objects.requireNonNull(document.get("hospital_name")).toString();
                        } catch (NullPointerException e) {
                            toastMessage = "Error retrieving user information; Please contact support";
                            Toast.makeText(parent.getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // document for user doesn't exist
                        toastMessage = "User not found; Please contact support";
                        Toast.makeText(parent.getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    toastMessage = "User lookup failed; Please try again and contact support if issue persists";
                    Toast.makeText(parent.getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        topToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProcedureInfoFragment fragment = new ProcedureInfoFragment();
                Bundle bundle = new Bundle();
                HashMap<String, Object> procedureInfo = new HashMap<>();
                procedureInfo.put("procedure_used", procedureName);
                procedureInfo.put("procedure_date", procedureDate);
                procedureInfo.put("time_in", procedureTimeIn);
                procedureInfo.put("time_out", procedureTimeOut);
                procedureInfo.put("fluoro_time", fluoroTime);
                procedureInfo.put("accession_number", accessionNumber);
                bundle.putSerializable("procedureMap", (Serializable) procedureInfo);
                fragment.setArguments(bundle);

                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                //clears other fragments
                fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.enter_left, R.anim.fui_slide_out_left);
                fragmentTransaction.add(R.id.activity_main, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();



            }
        });

        if (getArguments() != null) {
            HashMap<String, Object> procedureInfo;
            Bundle procedureInfoBundle = this.getArguments();
            if(procedureInfoBundle.get("procedureMap") != null){
                procedureInfo = (HashMap<String, Object>) procedureInfoBundle.getSerializable("procedureMap");
                procedureDate = (String) procedureInfo.get("procedure_date");
                procedureName = (String) procedureInfo.get("procedure_used");
                procedureTimeIn = (String) procedureInfo.get("time_in");
                procedureTimeOut = (String) procedureInfo.get("time_out");
                fluoroTime = (String) procedureInfo.get("fluoro_time");
                accessionNumber= (String) procedureInfo.get("accession_number");
            }

        }

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (parent != null)
                    parent.onBackPressed();
            }
        });






        return rootView;
    }
}

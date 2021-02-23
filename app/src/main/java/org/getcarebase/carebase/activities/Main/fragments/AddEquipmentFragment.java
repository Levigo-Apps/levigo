package org.getcarebase.carebase.activities.Main.fragments;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.activities.Main.adapters.NewUsedDevicesAdapter;
import org.getcarebase.carebase.models.Procedure;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.viewmodels.ProcedureViewModel;
import java.util.Objects;

public class AddEquipmentFragment extends Fragment {
    public static final String TAG = AddEquipmentFragment.class.getSimpleName();
    private LinearLayout procedureDetailsView;
    private ProcedureViewModel procedureViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_addequipment, container, false);
        MaterialToolbar topToolBar = rootView.findViewById(R.id.topAppBar);
        FloatingActionButton addBarcode = rootView.findViewById(R.id.fragment_addScan);
        procedureDetailsView = rootView.findViewById(R.id.procedure_details);
        RecyclerView devicesUsedRecyclerView = rootView.findViewById(R.id.devices_used_recycler_view);
        Button saveButton = rootView.findViewById(R.id.procedure_save_button);

        procedureViewModel = new ViewModelProvider(requireActivity()).get(ProcedureViewModel.class);

        NewUsedDevicesAdapter newUsedDevicesAdapter = new NewUsedDevicesAdapter(procedureViewModel.getDevicesUsed());
        devicesUsedRecyclerView.setAdapter(newUsedDevicesAdapter);
        devicesUsedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        setProcedureSummary();

        procedureViewModel.getAddDeviceRequestLiveData().observe(getViewLifecycleOwner(),request -> {
            if (request.getStatus() == Request.Status.SUCCESS) {
                newUsedDevicesAdapter.notifyDataSetChanged();
            }
            else if (request.getStatus() == Request.Status.ERROR) {
                if (request.getResourceString() == R.string.error_device_lookup) {
                    offerEquipmentScan(rootView,procedureViewModel.getScannedDevice());
                } else {
                    Snackbar.make(rootView, request.getResourceString(), Snackbar.LENGTH_LONG).show();
                }
            }
        });

        procedureViewModel.getSaveProcedureRequestLiveData().observe(getViewLifecycleOwner(),request -> {
            if (request.getStatus() == Request.Status.SUCCESS) {
                // go back to main activity
                requireActivity().setResult(Activity.RESULT_OK);
                procedureViewModel.goToInventory();
            } else if (request.getStatus() == Request.Status.ERROR) {
                Snackbar.make(rootView, request.getResourceString(), Snackbar.LENGTH_LONG).show();
            }
        });

        topToolBar.setNavigationOnClickListener(view -> procedureViewModel.goToProcedureDetails());

        addBarcode.setOnClickListener(view -> startScanner());

        saveButton.setOnClickListener(view -> {
            if (devicesUsedRecyclerView.getAdapter().getItemCount() == 0) {
                Snackbar.make(rootView, R.string.error_procedure_no_devices, Snackbar.LENGTH_LONG).show();
            } else {
                procedureViewModel.saveProcedure();
            }
        });

        return rootView;
    }

    private void setProcedureSummary(){
        Procedure procedure = Objects.requireNonNull(procedureViewModel.getProcedureDetails());
        TextView newProcedureName = procedureDetailsView.findViewById(R.id.newprocedureinfoName_textview);
        TextView newProcedureDate = procedureDetailsView.findViewById(R.id.newprocedureinfoDate_textview);
        TextView newProcedureAccessionNumber = procedureDetailsView.findViewById(R.id.newprocedureinfoAccessionNumber_textview);
        newProcedureName.setText(procedure.getName());
        newProcedureDate.setText(procedure.getDate());
        newProcedureAccessionNumber.setText(procedure.getAccessionNumber());
    }

    private void startScanner() {
        IntentIntegrator.forSupportFragment(AddEquipmentFragment.this)
                .setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
                .setBarcodeImageEnabled(true)
                .setCaptureActivity(CaptureActivity.class)
                .initiateScan();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            String contents = result.getContents();
            if (contents != null) {
                checkUdi(contents);
            }
            if (result.getBarcodeImagePath() != null) {
                Log.d(TAG, "" + result.getBarcodeImagePath());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void checkUdi(final String contents){
        if (contents.equals("")) return;
        procedureViewModel.addDeviceUsed(contents.trim());
    }

    public void offerEquipmentScan(View view, final String udi){
        new MaterialAlertDialogBuilder(view.getContext())
                .setTitle("Scan device")
                .setMessage("The device with the barcode: \n"+udi+"\ncould not be found in inventory.\nWould you like to add it now?")
                .setPositiveButton("Scan", (dialogInterface, i) -> {
                    ItemDetailFragment fragment = new ItemDetailFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("barcode", udi);
                    fragment.setArguments(bundle);
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

                    //clears other fragments
                    // fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.setCustomAnimations(R.anim.fui_slide_in_right, R.anim.fui_slide_out_left);
                    fragmentTransaction.add(R.id.frame_layout, fragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                })
                .setNegativeButton("Cancel", (dialogInterface, i) -> {
                    dialogInterface.cancel();
                })
                .show();
    }
}

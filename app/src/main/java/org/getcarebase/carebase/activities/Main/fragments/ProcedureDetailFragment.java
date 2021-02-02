package org.getcarebase.carebase.activities.Main.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.activities.Main.adapters.DevicesUsedAdapter;
import org.getcarebase.carebase.models.Procedure;
import org.getcarebase.carebase.models.User;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.viewmodels.ProcedureDetailViewModel;
import org.getcarebase.carebase.views.DetailLabeledTextView;

import java.util.Objects;

public class ProcedureDetailFragment extends Fragment {
    public static final String TAG = ProcedureDetailFragment.class.getName();
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.procedure_detail_layout,container,false);
        MaterialToolbar toolbar = rootView.findViewById(R.id.toolbar);
        TextView nameView = rootView.findViewById(R.id.name_text_view);
        DetailLabeledTextView dateView = rootView.findViewById(R.id.date_text_view);
        DetailLabeledTextView timeView = rootView.findViewById(R.id.room_time_text_view);
        DetailLabeledTextView timeStartView = rootView.findViewById(R.id.time_start_text_view);
        DetailLabeledTextView timeEndView = rootView.findViewById(R.id.time_end_text_view);
        DetailLabeledTextView fluoroTimeView = rootView.findViewById(R.id.fluoro_time_text_view);
        DetailLabeledTextView accessionNumberView = rootView.findViewById(R.id.accession_number_text_view);

        Drawable divider = requireContext().getDrawable(R.drawable.divider);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(requireContext(),DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(divider);
        RecyclerView devicesUsedRecyclerView = rootView.findViewById(R.id.devices_used_recycler_view);
        devicesUsedRecyclerView.addItemDecoration(itemDecoration);
        RecyclerView.LayoutManager inventoryLayoutManager = new LinearLayoutManager(getContext());
        devicesUsedRecyclerView.setLayoutManager(inventoryLayoutManager);
        DevicesUsedAdapter devicesUsedAdapter = new DevicesUsedAdapter();
        devicesUsedRecyclerView.setAdapter(devicesUsedAdapter);

        toolbar.setNavigationOnClickListener(view -> requireActivity().onBackPressed());

        String procedureId = Objects.requireNonNull(getArguments()).getString("procedure_id");
        ProcedureDetailViewModel procedureDetailViewModel = new ViewModelProvider(this).get(ProcedureDetailViewModel.class);
        procedureDetailViewModel.getUserLiveData().observe(getViewLifecycleOwner(),userResource -> {
            if (userResource.getRequest().getStatus() == Request.Status.SUCCESS) {
                User user = userResource.getData();
                procedureDetailViewModel.setProcedureRepository(user.getNetworkId(),user.getHospitalId());
                procedureDetailViewModel.getProcedure(procedureId);
            } else if (userResource.getRequest().getStatus() == Request.Status.ERROR) {
                requireActivity().onBackPressed();
                Snackbar.make(requireActivity().findViewById(R.id.activity_main),userResource.getRequest().getResourceString(),Snackbar.LENGTH_LONG);
            }
        });

        procedureDetailViewModel.getProcedureLiveData().observe(getViewLifecycleOwner(), procedureResource -> {
            if (procedureResource.getRequest().getStatus() == Request.Status.SUCCESS) {
                Procedure procedure = procedureResource.getData();
                nameView.setText(procedure.getName());
                dateView.setTextValue(procedure.getDate());
                timeView.setTextValue(procedure.getRoomTime());
                timeStartView.setTextValue(procedure.getTimeIn());
                timeEndView.setTextValue(procedure.getTimeOut());
                fluoroTimeView.setTextValue(procedure.getFluoroTime());
                accessionNumberView.setTextValue(procedure.getAccessionNumber());
                devicesUsedAdapter.setDeviceUsages(procedure.getDeviceUsages());
            } else if (procedureResource.getRequest().getStatus() == Request.Status.ERROR) {
                requireActivity().onBackPressed();
                Snackbar.make(requireActivity().findViewById(R.id.activity_main),procedureResource.getRequest().getResourceString(),Snackbar.LENGTH_LONG);
            }
        });

        procedureDetailViewModel.getUser();

        return rootView;
    }
}

package org.getcarebase.carebase.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Query.Direction;

import org.getcarebase.carebase.R;
import org.getcarebase.carebase.models.DeviceUsage;
import org.getcarebase.carebase.models.Procedure;
import org.getcarebase.carebase.utils.Request;
import org.getcarebase.carebase.utils.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is class should handle all of the logic of saving and retrieving for procedures.
 */
public class ProcedureRepository {
    private static final String TAG = ProcedureRepository.class.getName();

    private final CollectionReference proceduresReference;
    private final CollectionReference inventoryReference;

    private final List<Procedure> procedures;
    private DocumentSnapshot lastResult; // last procedure

    public ProcedureRepository(final String networkId, final String hospitalId) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        inventoryReference = firestore.collection("networks").document(networkId)
                .collection("hospitals").document(hospitalId)
                .collection("departments").document("default_department")
                .collection("dis");
        proceduresReference = firestore.collection("networks").document(networkId)
                .collection("hospitals").document(hospitalId)
                .collection("departments").document("default_department")
                .collection("procedures");
        procedures = new ArrayList<>();
    }

    /**
     * Saves the procedure information into every device used in the procedure
     * @param procedure a representation of a medical procedure with a list of devices used in the
     *                  procedure
     * @return a Request object detailing the status of the request.
     */
    public LiveData<Request> saveProcedure(Procedure procedure) {
        MutableLiveData<Request> saveProceduresRequest = new MutableLiveData<>();
        List<Task<?>> tasks = new ArrayList<>();

        Task<DocumentReference> procedureTask = proceduresReference.add(procedure);
        // save dis and udis into an array to make procedure searchable
        procedureTask.addOnSuccessListener(documentReference -> {
            List<String> dis = procedure.getDeviceUsages().stream().map(DeviceUsage::getDeviceIdentifier).distinct().collect(Collectors.toList());
            List<String> udis = procedure.getDeviceUsages().stream().map(DeviceUsage::getUniqueDeviceIdentifier).distinct().collect(Collectors.toList());
            documentReference.update("dis", dis);
            documentReference.update("udis", udis);
        });
        tasks.add(procedureTask);

        for (DeviceUsage deviceUsage : procedure.getDeviceUsages()) {
            // update devices quantities (udi)
            DocumentReference deviceProductionReference = inventoryReference.document(deviceUsage.getDeviceIdentifier())
                    .collection("udis").document(deviceUsage.getUniqueDeviceIdentifier());
            tasks.add(deviceProductionReference.update("quantity",Integer.toString(deviceUsage.getNewProductionQuantity())));
            // update devices quantities (di)
            DocumentReference deviceProductionReferenceDI = inventoryReference.document(deviceUsage.getDeviceIdentifier());
            tasks.add(deviceProductionReferenceDI.update("quantity", Integer.toString(deviceUsage.getNewModelQuantity())));
        }

        Tasks.whenAll(tasks).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                saveProceduresRequest.setValue(new Request(null, Request.Status.SUCCESS));
            } else {
                // TODO make resource error string
                saveProceduresRequest.setValue(new Request(R.string.error_something_wrong, Request.Status.ERROR));
            }
        });
        return saveProceduresRequest;
    }

    public LiveData<Resource<List<Procedure>>> getProcedures() {
        Query queryLiveData;
        MutableLiveData<Resource<List<Procedure>>> proceduresLiveData = new MutableLiveData<>();
        // Order procedures by date and limit the number of procedures to 5
        if (lastResult == null) { //if there isn't procedures obtained
            // cannot add .orderBy("time_in", "Direction.ASCENDING") will crash app
            queryLiveData = proceduresReference.orderBy("date", Direction.DESCENDING).orderBy("time_in", Direction.DESCENDING).limit(5);
        } else {
            // Obtain the next 5 procedures
            queryLiveData = proceduresReference.orderBy("date", Direction.DESCENDING).orderBy("time_in", Direction.DESCENDING).startAfter(lastResult).limit(5);
        }
        // After getting results, add results to procedures list
        queryLiveData.get().addOnCompleteListener(task -> {
           if (task.isSuccessful()) {
               for (QueryDocumentSnapshot procedureSnapshot : task.getResult()) {
                   procedures.add(procedureSnapshot.toObject(Procedure.class));
               }
               // store the last procedure
               if (task.getResult().size() > 0) {
                   lastResult = task.getResult().getDocuments().get(task.getResult().size() -1);
               }
               proceduresLiveData.setValue(new Resource<>(procedures,new Request(null, Request.Status.SUCCESS)));
           } else {
               proceduresLiveData.setValue(new Resource<>(null,new Request(R.string.error_something_wrong,Request.Status.ERROR)));
           }
        });
        return proceduresLiveData;
    }

    public LiveData<Resource<Procedure>> getProcedure(String procedureId) {
        MutableLiveData<Resource<Procedure>> procedureLiveData = new MutableLiveData<>();
        proceduresReference.document(procedureId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Procedure procedure = task.getResult().toObject(Procedure.class);
                procedureLiveData.setValue(new Resource<>(procedure,new Request(null,Request.Status.SUCCESS)));
            }
            else {
                procedureLiveData.setValue(new Resource<>(null, new Request(R.string.error_something_wrong,Request.Status.ERROR)));
            }
        });
        return procedureLiveData;
    }
}

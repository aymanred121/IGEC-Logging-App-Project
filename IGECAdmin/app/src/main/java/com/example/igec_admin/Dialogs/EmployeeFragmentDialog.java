package com.example.igec_admin.Dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.igec_admin.Adatpers.EmployeeAdapter;
import com.example.igec_admin.Fragments.MonthSummaryDialog;
import com.example.igec_admin.R;
import com.example.igec_admin.fireBase.Project;
import com.example.igec_admin.utilites.LocationDetails;
import com.example.igec_admin.utilites.WorkingDay;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class EmployeeFragmentDialog extends DialogFragment {

    private Project project;
    private EmployeeAdapter adapter;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private String year, month;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();


    public EmployeeFragmentDialog(Project project, String year, String month) {
        this.project = project;
        this.year = year;
        this.month = month;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullscreenDialogTheme);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {


        Dialog dialog = super.onCreateDialog(savedInstanceState);
        Window window = dialog.getWindow();

        if (window != null) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        }

        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_users, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize(view);
        adapter.setOnItemClickListener(oclEmployee);
    }

    private void initialize(View view) {
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new EmployeeAdapter(project.getEmployees(), false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
//        getEmployees();

    }

    private EmployeeAdapter.OnItemClickListener oclEmployee = new EmployeeAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
            ArrayList<WorkingDay> workingDays = new ArrayList<>();
            String id = project.getEmployees().get(position).getId();
            String empName = project.getEmployees().get(position).getFirstName() + " " + project.getEmployees().get(position).getLastName();
            db.collection("summary").document(id).collection(year + "-" + month)
                    .whereEqualTo("projectId", project.getId())
                    .get().addOnSuccessListener(queryDocumentSnapshots -> {
                        if (queryDocumentSnapshots.size() == 0)
                            return;

                        for (QueryDocumentSnapshot q : queryDocumentSnapshots) {
                            String day = q.getId();
                            double hours = ((q.getData().get("workingTime") == null) ? 0 : ((long) (q.getData().get("workingTime"))) / 3600.0);
                            String checkInGeoHash = (String) ((HashMap<String, Object>) Objects.requireNonNull(q.getData().get("checkIn"))).get("geohash");
                            double checkInLat = (double) ((HashMap<String, Object>) Objects.requireNonNull(q.getData().get("checkIn"))).get("lat");
                            double checkInLng = (double) ((HashMap<String, Object>) Objects.requireNonNull(q.getData().get("checkIn"))).get("lng");
                            String checkOutGeoHash = (String) ((HashMap<String, Object>) Objects.requireNonNull(q.getData().get("checkOut"))).get("geohash");
                            double checkOutLat = (double) ((HashMap<String, Object>) Objects.requireNonNull(q.getData().get("checkOut"))).get("lat");
                            double checkOutLng = (double) ((HashMap<String, Object>) Objects.requireNonNull(q.getData().get("checkOut"))).get("lng");
                            LocationDetails checkInLocation = new LocationDetails(checkInGeoHash, checkInLat, checkInLng);
                            LocationDetails checkOutLocation = new LocationDetails(checkOutGeoHash, checkOutLat, checkOutLng);
                            String projectLocation = String.format("%s, %s, %s", project.getLocationCity(), project.getLocationArea(), project.getLocationStreet());
                            workingDays.add(new WorkingDay(day, month, year, hours, empName, checkInLocation, checkOutLocation, project.getName(), projectLocation));
                        }
                        MonthSummaryDialog monthSummaryDialog = new MonthSummaryDialog(workingDays);
                        monthSummaryDialog.show(getParentFragmentManager(), "");
                    });

        }

        @Override
        public void onCheckboxClick(int position) {

        }
    };
}

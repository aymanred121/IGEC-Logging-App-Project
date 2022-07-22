package com.example.igec_admin.Fragments;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.igec_admin.Adatpers.EmployeeAdapter;
import com.example.igec_admin.Adatpers.ProjectAdapter;
import com.example.igec_admin.Dialogs.EmployeeFragmentDialog;
import com.example.igec_admin.R;
import com.example.igec_admin.fireBase.Allowance;
import com.example.igec_admin.fireBase.Employee;
import com.example.igec_admin.fireBase.EmployeeOverview;
import com.example.igec_admin.fireBase.EmployeesGrossSalary;
import com.example.igec_admin.fireBase.Project;
import com.example.igec_admin.utilites.CsvWriter;
import com.example.igec_admin.utilites.LocationDetails;
import com.example.igec_admin.utilites.WorkingDay;
import com.example.igec_admin.utilites.allowancesEnum;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.whiteelephant.monthpicker.MonthPickerDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SummaryFragment extends Fragment {


    // Views
    private RecyclerView recyclerView;
    private TextInputLayout selectedMonthLayout;
    private TextInputEditText selectedMonthEdit;
    private FloatingActionButton createCSV;
    // Vars
    private EmployeeAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private String year, month, prevMonth, prevYear;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference employeeOverviewRef = db.collection("EmployeeOverview").document("emp");
    ArrayList<EmployeeOverview> employees = new ArrayList();
    ArrayList<Project> projects = new ArrayList();
    CollectionReference projectRef = db.collection("projects");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_summary, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Initialize(view);
        selectedMonthLayout.setEndIconOnClickListener(oclMonthPicker);
        selectedMonthLayout.setErrorIconOnClickListener(oclMonthPicker);
        selectedMonthLayout.setErrorIconDrawable(R.drawable.ic_baseline_calendar_month_24);
        adapter.setOnItemClickListener(oclEmployee);
        createCSV.setOnClickListener(oclCSV);
    }

    // Functions
    private void Initialize(View view) {
        recyclerView = view.findViewById(R.id.recyclerview);
        selectedMonthEdit = view.findViewById(R.id.TextInput_SelectedMonth);
        selectedMonthLayout = view.findViewById(R.id.textInputLayout_SelectedMonth);
        createCSV = view.findViewById(R.id.fab_createCSV);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new EmployeeAdapter(employees, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
//        getProjects();
        getEmployees();

    }

    void getProjects() {
        projectRef.addSnapshotListener((queryDocumentSnapshots, e) -> {
            projects.clear();
            for (DocumentSnapshot d : queryDocumentSnapshots) {
                projects.add(d.toObject(Project.class));
            }
        });
    }

    void getEmployees() {
        employeeOverviewRef.addSnapshotListener((documentSnapshot, e) -> {
            HashMap empMap;
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }
            if (documentSnapshot != null && documentSnapshot.exists()) {
                empMap = (HashMap) documentSnapshot.getData();
                retrieveEmployees(empMap);
            } else {
                return;
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void retrieveEmployees(Map<String, ArrayList<String>> empMap) {
        employees.clear();
        for (String key : empMap.keySet()) {
            String firstName = empMap.get(key).get(0);
            String lastName = empMap.get(key).get(1);
            String title = empMap.get(key).get(2);
            String id = (key);
            employees.add(new EmployeeOverview(firstName, lastName, title, id));
        }
        adapter.setEmployeeOverviewsList(employees);
        adapter.notifyDataSetChanged();
    }

    private final View.OnClickListener oclMonthPicker = v -> {
        final Calendar today = Calendar.getInstance();
        MonthPickerDialog.Builder builder = new MonthPickerDialog.Builder(getActivity(),
                new MonthPickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(int selectedMonth, int selectedYear) {
                        selectedMonthLayout.setError(null);
                        selectedMonthLayout.setErrorEnabled(false);
                        selectedMonthEdit.setText(String.format("%d/%d", selectedMonth + 1, selectedYear));
                        String[] selectedDate = selectedMonthEdit.getText().toString().split("/");
                        year = selectedDate[1];
                        month = selectedDate[0];
                        if (month.length() == 1) {
                            month = "0" + month;
                        }
                        if ((Integer.parseInt(month) - 1) < 1) {
                            prevMonth = "12";
                            prevYear = Integer.parseInt(year) - 1 + "";
                        } else {
                            prevMonth = (Integer.parseInt(month) - 1) + "";
                            prevYear = year;
                        }
                        if (prevMonth.length() == 1) {
                            prevMonth = "0" + prevMonth;
                        }
                    }
                }, today.get(Calendar.YEAR), today.get(Calendar.MONTH));
        builder.setActivatedMonth(today.get(Calendar.MONTH))
                .setMinYear(today.get(Calendar.YEAR) - 1)
                .setActivatedYear(today.get(Calendar.YEAR))
                .setMaxYear(today.get(Calendar.YEAR) + 1)
                .setTitle("Select Month")
                .build().show();

    };

    private final View.OnClickListener oclCSV = v -> {
        if (selectedMonthEdit.getText().toString().isEmpty()) {
            selectedMonthLayout.setError("Please select a month");
        } else {
            String[] selectedDate = selectedMonthEdit.getText().toString().split("/");
            db.collection("employees")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        String[] header = {"Name", "Basic", "over time", "Cuts", "Transportation", "accommodation", "site", "remote", "food", "other", "personal", "Next month", "current month", "previous month"};
                        CsvWriter csvWriter = new CsvWriter(header);
                        final int[] counter = new int[1];
                        for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {
                            month = String.format("%02d",Integer.parseInt(month));
                            db.collection("EmployeesGrossSalary").document(queryDocumentSnapshot.getId()).collection(prevYear).document(prevMonth).get().addOnSuccessListener(doc -> {
                                db.collection("EmployeesGrossSalary").document(queryDocumentSnapshot.getId()).collection(year).document(month).get().addOnSuccessListener(documentSnapshot1 -> {
                                    if (!documentSnapshot1.exists()) {
                                        if (counter[0] == queryDocumentSnapshots.size() - 1) {
                                            try {
                                                csvWriter.build(year + "-" + month);
                                                Toast.makeText(getActivity(), "CSV file created", Toast.LENGTH_SHORT).show();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        counter[0]++;
                                        return;
                                    }
                                    Employee emp = queryDocumentSnapshot.toObject(Employee.class);
                                    double cuts = 0;
                                    double transportation = 0;
                                    double accommodation = 0;
                                    double site = 0;
                                    double remote = 0;
                                    double food = 0;
                                    double other = 0;
                                    double overTime = 0;
                                    double personal = 0;
                                    double nextMonth = 0;
                                    double currentMonth = 0;
                                    double previousMonth = 0;
                                    for (Allowance allowance : documentSnapshot1.toObject(EmployeesGrossSalary.class).getAllTypes()) {
                                        if (allowance.getType() == allowancesEnum.NETSALARY.ordinal())
                                            continue;
                                        if (allowance.getName().trim().equalsIgnoreCase("Transportation")) {
                                            transportation += allowance.getAmount();
                                        } else if (allowance.getName().trim().equalsIgnoreCase("accommodation")) {
                                            accommodation += allowance.getAmount();
                                        } else if (allowance.getName().trim().equalsIgnoreCase("site")) {
                                            site += allowance.getAmount();
                                        } else if (allowance.getName().trim().equalsIgnoreCase("remote")) {
                                            remote += allowance.getAmount();
                                        } else if (allowance.getName().trim().equalsIgnoreCase("food")) {
                                            food += allowance.getAmount();
                                        } else if (allowance.getType() == allowancesEnum.PENALTY.ordinal()) {
                                            cuts += allowance.getAmount();
                                        } else if (allowance.getType() == allowancesEnum.GIFT.ordinal() ||
                                                allowance.getType() == allowancesEnum.BONUS.ordinal()) {
                                            personal += allowance.getAmount();
                                        } else if (allowance.getType() == allowancesEnum.OVERTIME.ordinal()) {
                                            overTime += allowance.getAmount();
                                        } else {
                                            other += allowance.getAmount();
                                        }
                                    }
                                    nextMonth = other + personal + accommodation + site + remote + food;
                                    currentMonth = transportation + emp.getSalary() + cuts + overTime;
                                    if (!doc.exists())
                                        previousMonth = 0;
                                    else {
                                        for (Allowance allowance : doc.toObject(EmployeesGrossSalary.class).getAllTypes()) {
                                            if (allowance.getType() == allowancesEnum.NETSALARY.ordinal())
                                                continue;
                                            if (allowance.getName().trim().equalsIgnoreCase("Transportation"))
                                                continue;
                                            if (allowance.getType() == allowancesEnum.PENALTY.ordinal()) {
                                                continue;
                                            } else if (allowance.getType() == allowancesEnum.OVERTIME.ordinal()) {
                                                continue;
                                            } else {
                                                previousMonth += allowance.getAmount();
                                            }
                                        }
                                    }
                                    csvWriter.addDataRow(emp.getFirstName() + " " + emp.getLastName(), String.valueOf(emp.getSalary()), String.valueOf(overTime), String.valueOf(cuts), String.valueOf(transportation), String.valueOf(accommodation), String.valueOf(site), String.valueOf(remote), String.valueOf(food), String.valueOf(other), String.valueOf(personal), String.valueOf(nextMonth), String.valueOf(currentMonth), String.valueOf(previousMonth));
                                    if (counter[0] == queryDocumentSnapshots.size() - 1) {
                                        try {
                                            csvWriter.build(year + "-" + month);
                                            Toast.makeText(getActivity(), "CSV file created", Toast.LENGTH_SHORT).show();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    counter[0]++;
                                });

                            });

                        }
                    });
        }
    };
    private EmployeeAdapter.OnItemClickListener oclEmployee = new EmployeeAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
            final Calendar today = Calendar.getInstance();
            @SuppressLint("DefaultLocale")
            MonthPickerDialog.Builder builder = new MonthPickerDialog.Builder(getActivity(),
                    (selectedMonth, selectedYear) -> {
                        selectedMonthLayout.setError(null);
                        selectedMonthLayout.setErrorEnabled(false);
                        selectedMonthEdit.setText(String.format("%d/%d", selectedMonth + 1, selectedYear));
                        String[] selectedDate = selectedMonthEdit.getText().toString().split("/");
                        year = selectedDate[1];
                        month = selectedDate[0];
                        month = String.format("%02d",Integer.parseInt(month));
                        if ((Integer.parseInt(month) - 1) < 1) {
                            prevMonth = "12";
                            prevYear = Integer.parseInt(year) - 1 + "";
                        } else {
                            prevMonth = (Integer.parseInt(month) - 1) + "";
                            prevYear = year;
                        }
                        prevMonth = String.format("%02d",Integer.parseInt(prevMonth));
                        ArrayList<WorkingDay> workingDays = new ArrayList<>();
                        EmployeeOverview employee = employees.get(position);
                        String empName = employee.getFirstName() + " " + employee.getLastName();
                        db.collection("summary").document(employee.getId()).collection(year + "-" + month)
                                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                                    if (queryDocumentSnapshots.size() == 0)
                                        return;
                                    //TODO: add project Lat and Lng when implemented
                                    for (QueryDocumentSnapshot q : queryDocumentSnapshots) {
                                        if(q.getData().get("checkOut") == null)
                                            continue;
                                        db.collection("projects").document((String) q.get("projectId")).get().addOnSuccessListener(doc->{
                                            if(!doc.exists())
                                                return;
                                            Project project = doc.toObject(Project.class);
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
                                        });
                                    }
                                    MonthSummaryDialog monthSummaryDialog = new MonthSummaryDialog(workingDays);
                                    monthSummaryDialog.show(getParentFragmentManager(), "");
                                });

                    }, today.get(Calendar.YEAR), today.get(Calendar.MONTH));
            MonthPickerDialog monthPickerDialog = builder.setActivatedMonth(today.get(Calendar.MONTH))
                    .setMinYear(today.get(Calendar.YEAR) - 1)
                    .setActivatedYear(today.get(Calendar.YEAR))
                    .setMaxYear(today.get(Calendar.YEAR) + 1)
                    .setTitle("Select Month")
                    .build();
            if (selectedMonthEdit.getText().toString().isEmpty())
                monthPickerDialog.show();
            else {
                String[] selectedDate = selectedMonthEdit.getText().toString().split("/");
                year = selectedDate[1];
                month = selectedDate[0];
                month = String.format("%02d",Integer.parseInt(month));
                ArrayList<WorkingDay> workingDays = new ArrayList<>();
                EmployeeOverview employee = employees.get(position);
                String empName = employee.getFirstName() + " " + employee.getLastName();
                db.collection("summary").document(employee.getId()).collection(year + "-" + month)
                        //.whereEqualTo("projectId", employee.getProjectId())
                        .get().addOnSuccessListener(queryDocumentSnapshots -> {
                            if (queryDocumentSnapshots.size() == 0)
                                return;
                            //TODO: add project Lat and Lng when implemented
                            for (QueryDocumentSnapshot q : queryDocumentSnapshots) {
                                if(q.getData().get("checkOut") == null)
                                    continue;
                                db.collection("projects").document((String) q.get("projectId")).get().addOnSuccessListener(doc->{
                                   if(!doc.exists())
                                   return;
                                   Project project = doc.toObject(Project.class);
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
                                });
                                }
                            MonthSummaryDialog monthSummaryDialog = new MonthSummaryDialog(workingDays);
                            monthSummaryDialog.show(getParentFragmentManager(), "");
                        });
            }

        }

        @Override
        public void onCheckboxClick(int position) {

        }
    };
}

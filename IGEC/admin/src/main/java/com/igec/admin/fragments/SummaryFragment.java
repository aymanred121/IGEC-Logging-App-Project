package com.igec.admin.fragments;

import static android.content.ContentValues.TAG;
import static com.igec.common.CONSTANTS.EMPLOYEE_COL;
import static com.igec.common.CONSTANTS.EMPLOYEE_GROSS_SALARY_COL;
import static com.igec.common.CONSTANTS.EMPLOYEE_OVERVIEW_REF;
import static com.igec.common.CONSTANTS.PROJECT_COL;
import static com.igec.common.CONSTANTS.SUMMARY_COL;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.WorkInfo;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.igec.admin.adapters.EmployeeAdapter;
import com.igec.admin.databinding.FragmentSummaryBinding;
import com.igec.admin.dialogs.MonthSummaryDialog;
import com.igec.common.firebase.Allowance;
import com.igec.common.firebase.Employee;
import com.igec.common.firebase.EmployeeOverview;
import com.igec.common.firebase.EmployeesGrossSalary;
import com.igec.common.firebase.Project;
import com.igec.common.firebase.Summary;
import com.igec.common.utilities.AllowancesEnum;
import com.igec.common.utilities.CsvWriter;
import com.igec.common.utilities.LocationDetails;
import com.igec.common.utilities.WorkingDay;
import com.whiteelephant.monthpicker.MonthPickerDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.IntStream;

public class SummaryFragment extends Fragment {
    // Vars
    private EmployeeAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private String year, month, prevMonth, prevYear;
    private ArrayList<EmployeeOverview> employees;
    private boolean opened = false;
    private final long EIGHT_HOURS = 28800;

    public void setOpened(boolean opened) {
        this.opened = opened;
    }

    private FragmentSummaryBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSummaryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize();
        binding.monthLayout.setEndIconOnClickListener(oclMonthPicker);
        binding.monthLayout.setErrorIconOnClickListener(oclMonthPicker);
        adapter.setOnItemClickListener(oclEmployee);
        binding.createFab.setOnClickListener(oclCSV);
        binding.allFab.setOnClickListener(oclAll);
    }

    // Functions
    private void initialize() {
        employees = new ArrayList<>();
        binding.recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new EmployeeAdapter(employees, EmployeeAdapter.Type.none);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);
        getEmployees();

    }

    private void generateCSV(ArrayList<WorkingDay> workingDays) {
        String month = workingDays.get(0).getMonth();
        String year = workingDays.get(0).getYear();
        String empName = workingDays.get(0).getEmpName();
        int yearNumber = Integer.parseInt(year);
        int monthNumber = Integer.parseInt(month);
        StringJoiner header = new StringJoiner(",");
        String[] dataRow;
        header.add("Day");
        /*
        we know that months with 31 days are 1 3 5 7 8 10 12
        and when we use those numbers to shift left 1 we get
        2,8,32,128,256,1024,4096
        each of them represent a single 1 at different locations when transform into binary
        0b0000000000010
        0b0000000001000
        0b0000000100000
        0b0000010000000
        0b0000100000000
        0b0010000000000
        0b1000000000000
        so we could bitwise and with
        0b0101001010101
        we will always get 0 and otherwise with other numbers
         */
        int MONTH31DAYS = 0xA55;
        if ((1 << (monthNumber) & MONTH31DAYS) == 0) {
            //create header with 31 days
            for (int i = 1; i <= 31; i++) {
                header.add(String.valueOf(i));
            }
            dataRow = new String[32];
        } else if (monthNumber == 2) {
            if (yearNumber % 400 == 0 || (yearNumber % 100 != 0) && (yearNumber % 4 == 0)) {
                //create header with 29 days
                for (int i = 1; i <= 29; i++) {
                    header.add(String.valueOf(i));
                }
                dataRow = new String[30];
            } else {
                //create header with 28 days
                for (int i = 1; i <= 28; i++) {
                    header.add(String.valueOf(i));
                }
                dataRow = new String[29];
            }

        } else {
            //create header with 30 days
            for (int i = 1; i <= 30; i++) {
                header.add(String.valueOf(i));
            }
            dataRow = new String[31];
        }
        dataRow[0] = empName;
        CsvWriter csvWriter = new CsvWriter(header.toString().split(","));
        for (WorkingDay w : workingDays) {
            dataRow[Integer.parseInt(w.getDay())] = String.valueOf(w.getHours());
        }
        IntStream.range(1, dataRow.length).filter(i -> dataRow[i] == null).forEach(i -> dataRow[i] = "0");
        csvWriter.addDataRow(dataRow);
        dataRow[0] = "project name";
        for (WorkingDay w : workingDays) {
            dataRow[Integer.parseInt(w.getDay())] = String.valueOf(w.getProjectName());
        }
        IntStream.range(1, dataRow.length).filter(i -> dataRow[i] == null).forEach(i -> dataRow[i] = "0");
        csvWriter.addDataRow(dataRow);

        try {
            csvWriter.build(empName + "-" + year + "-" + month);
            Snackbar.make(binding.getRoot(), "csv Saved!", Snackbar.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadWorkingDays(EmployeeOverview employee) {
        ArrayList<WorkingDay> workingDays = new ArrayList<>();
        String empName = employee.getFirstName() + " " + employee.getLastName();
        SUMMARY_COL.document(employee.getId()).collection(year + "-" + month)
                .get().addOnSuccessListener(docs -> {
                    if (docs.size() == 0) {
                        return;
                    }
                    for (QueryDocumentSnapshot q : docs) {
                        String day = q.getId();
                        Summary summary = q.toObject(Summary.class);
                        if (summary.getCheckOut() == null) {
                            if (Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == Integer.parseInt(day)) {
                                Snackbar.make(binding.getRoot(), "this Employee is still working", Snackbar.LENGTH_SHORT).show();
                                // continue;
                            }
                            /*
                             * if the employee forgot to checkout
                             * check the time spent on all projects
                             * add the remainder of 8 hrs to the lastProjectId
                             * set checkOut to checkIn
                             */
                            summary.setCheckOut(summary.getCheckIn());
                            long workingTimeInSeconds = summary.getWorkingTime() != null ? summary.getWorkingTime().values().stream().mapToLong(v -> (long) v).sum() : 0;
                            workingTimeInSeconds = EIGHT_HOURS - workingTimeInSeconds < 0 ? 0 : EIGHT_HOURS - workingTimeInSeconds;
                            if (summary.getWorkingTime() != null) {
                                summary.getWorkingTime().merge(summary.getLastProjectId(), workingTimeInSeconds, (a, b) -> (long) a + (long) b);
                            } else {
                                summary.setWorkingTime(new HashMap<>());
                                summary.getWorkingTime().put(summary.getLastProjectId(), workingTimeInSeconds);
                            }
                        }
                        summary.getProjectIds().keySet().forEach(pid -> PROJECT_COL.document(pid).get().addOnSuccessListener(doc -> {
                            Project project = new Project();
                            if (!doc.exists()) {
                                if (!pid.equals("HOME"))
                                    return;
                                project.setName("Home");
                                project.setReference("");
                                project.setLocationArea("");
                                project.setLocationCity("");
                                project.setLocationStreet("");
                            } else {
                                project = doc.toObject(Project.class);
                            }
                            double hours = summary.getWorkingTime() == null || !summary.getWorkingTime().containsKey(pid) ? 0.0 : (Long) summary.getWorkingTime().get(pid) / 3600.0;
                            String checkInGeoHash = (String) summary.getCheckIn().get("geohash");
                            double checkInLat = (double) summary.getCheckIn().get("lat");
                            double checkInLng = (double) summary.getCheckIn().get("lng");
                            String checkOutGeoHash = (String) summary.getCheckOut().get("geohash");
                            double checkOutLat = (double) summary.getCheckOut().get("lat");
                            double checkOutLng = (double) summary.getCheckOut().get("lng");
                            LocationDetails checkInLocation = new LocationDetails(checkInGeoHash, checkInLat, checkInLng);
                            LocationDetails checkOutLocation = new LocationDetails(checkOutGeoHash, checkOutLat, checkOutLng);
                            String projectLocation = String.format("%s, %s, %s", project.getLocationCity(), project.getLocationArea(), project.getLocationStreet());
//                            if (Calendar.getInstance().get(Calendar.DAY_OF_MONTH) != Integer.parseInt(day))
                            workingDays.add(new WorkingDay(day, month, year, hours, empName, checkInLocation, checkOutLocation, project.getName(), project.getReference(), projectLocation, summary.getProjectIds().get(pid)));
                            if (docs.getDocuments().lastIndexOf(q) == docs.getDocuments().size() - 1) {
                                //sort working days by date
                                workingDays.sort((o1, o2) -> {
                                    int o1Day = Integer.parseInt(o1.getDay());
                                    int o2Day = Integer.parseInt(o2.getDay());
                                    return o1Day - o2Day;
                                });
                                generateCSV((ArrayList<WorkingDay>) workingDays.clone());
                            }
                        }));

                    }
                });
    }

    private void openMonthSummaryDialog(int position) {
        EmployeeOverview employee = employees.get(position);
        ArrayList<WorkingDay> workingDays = new ArrayList<>();
        String empName = employee.getFirstName() + " " + employee.getLastName();
        SUMMARY_COL.document(employee.getId()).collection(year + "-" + month)
                .get().addOnSuccessListener(docs -> {
                    if (docs.size() == 0) {
                        Snackbar.make(binding.getRoot(), "No Work is registered", Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    for (QueryDocumentSnapshot q : docs) {
                        String day = q.getId();
                        Summary summary = q.toObject(Summary.class);
                        if (summary.getCheckOut() == null) {
                            if (Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == Integer.parseInt(day)) {
                                Snackbar.make(binding.getRoot(), "this Employee is still working", Snackbar.LENGTH_SHORT).show();
                                // continue;
                            }
                            /*
                             * if the employee forgot to checkout
                             * check the time spent on all projects
                             * add the remainder of 8 hrs to the lastProjectId
                             * set checkOut to checkIn
                             */
                            summary.setCheckOut(summary.getCheckIn());
                            long workingTimeInSeconds = summary.getWorkingTime() != null ? summary.getWorkingTime().values().stream().mapToLong(v -> (long) v).sum() : 0;
                            workingTimeInSeconds = EIGHT_HOURS - workingTimeInSeconds < 0 ? 0 : EIGHT_HOURS - workingTimeInSeconds;
                            if (summary.getWorkingTime() != null) {
                                summary.getWorkingTime().merge(summary.getLastProjectId(), workingTimeInSeconds, (a, b) -> (long) a + (long) b);
                            } else {
                                summary.setWorkingTime(new HashMap<>());
                                summary.getWorkingTime().put(summary.getLastProjectId(), workingTimeInSeconds);
                            }
                        }
                        summary.getProjectIds().keySet().forEach(pid -> PROJECT_COL.document(pid).get().addOnSuccessListener(doc -> {
                            Project project = new Project();
                            if (!doc.exists()) {
                                if (!pid.equals("HOME"))
                                    return;
                                project.setName("Home");
                                project.setReference("");
                                project.setLocationArea("");
                                project.setLocationCity("");
                                project.setLocationStreet("");
                            } else {
                                project = doc.toObject(Project.class);
                            }
                            double hours = summary.getWorkingTime() == null || !summary.getWorkingTime().containsKey(pid) ? 0.0 : (Long) summary.getWorkingTime().get(pid) / 3600.0;
                            String checkInGeoHash = (String) summary.getCheckIn().get("geohash");
                            double checkInLat = (double) summary.getCheckIn().get("lat");
                            double checkInLng = (double) summary.getCheckIn().get("lng");
                            String checkOutGeoHash = (String) summary.getCheckOut().get("geohash");
                            double checkOutLat = (double) summary.getCheckOut().get("lat");
                            double checkOutLng = (double) summary.getCheckOut().get("lng");
                            LocationDetails checkInLocation = new LocationDetails(checkInGeoHash, checkInLat, checkInLng);
                            LocationDetails checkOutLocation = new LocationDetails(checkOutGeoHash, checkOutLat, checkOutLng);
                            String projectLocation = String.format("%s, %s, %s", project.getLocationCity(), project.getLocationArea(), project.getLocationStreet());
                            if (Calendar.getInstance().get(Calendar.DAY_OF_MONTH) != Integer.parseInt(day))
                                workingDays.add(new WorkingDay(day, month, year, hours, empName, checkInLocation, checkOutLocation, project.getName(), project.getReference(), projectLocation, summary.getProjectIds().get(pid)));
                            if (docs.getDocuments().lastIndexOf(q) == docs.getDocuments().size() - 1) {
                                if (opened) return;
                                opened = true;
                                //sort working days by date
                                workingDays.sort((o1, o2) -> {
                                    int o1Day = Integer.parseInt(o1.getDay());
                                    int o2Day = Integer.parseInt(o2.getDay());
                                    return o1Day - o2Day;
                                });
                                MonthSummaryDialog monthSummaryDialog = new MonthSummaryDialog(workingDays);
                                monthSummaryDialog.show(getParentFragmentManager(), "");
                            }
                        }));

                    }
                });
    }

    void getEmployees() {
        EMPLOYEE_OVERVIEW_REF.addSnapshotListener((documentSnapshot, e) -> {
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
        employees.sort(Comparator.comparing(EmployeeOverview::getId));
        adapter.setEmployeeOverviewsList(employees);
        adapter.notifyDataSetChanged();
    }

    private final View.OnClickListener oclMonthPicker = v -> {
        final Calendar today = Calendar.getInstance();
        MonthPickerDialog.Builder builder = new MonthPickerDialog.Builder(getActivity(),
                (selectedMonth, selectedYear) -> {
                    selectedMonth += 1;
                    binding.monthLayout.setError(null);
                    binding.monthLayout.setErrorEnabled(false);
                    binding.monthEdit.setText(String.format("%d/%d", selectedMonth, selectedYear));
                    year = String.format("%d", selectedYear);
                    month = String.format("%02d", selectedMonth);

                    if (selectedMonth - 1 == 0) {
                        prevMonth = "12";
                        prevYear = String.format("%d", selectedYear - 1);
                    } else {
                        prevMonth = String.format("%02d", selectedMonth - 1);
                        prevYear = year;
                    }
                }, today.get(Calendar.YEAR), today.get(Calendar.MONTH));
        builder.setActivatedMonth(today.get(Calendar.MONTH))
                .setActivatedYear(today.get(Calendar.YEAR))
                .setMaxYear(today.get(Calendar.YEAR))
                .setTitle("Select Month")
                .build().show();

    };


    private View.OnClickListener oclAll = v -> {
        if (binding.monthEdit.getText().toString().isEmpty()) {
            binding.monthLayout.setError("Please select a month");
        } else {
            for (EmployeeOverview emp : employees) {
                loadWorkingDays(emp);
            }
        }

    };
    private final View.OnClickListener oclCSV = v -> {
        if (binding.monthEdit.getText().toString().isEmpty()) {
            binding.monthLayout.setError("Please select a month");
        } else {
            EMPLOYEE_COL
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        final String[] header = {"Name", "Basic", "over time", "Cuts", "Transportation", "accommodation", "site", "remote", "food", "other", "personal", "Next month", "current month", "previous month"};
                        CsvWriter csvWriter = new CsvWriter(header);
                        final int[] counter = new int[1];
                        for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {
                            month = String.format(Locale.getDefault(), "%02d", Integer.parseInt(month));
                            EMPLOYEE_GROSS_SALARY_COL.document(queryDocumentSnapshot.getId()).collection(prevYear).document(prevMonth).get().addOnSuccessListener(doc -> {
                                EMPLOYEE_GROSS_SALARY_COL.document(queryDocumentSnapshot.getId()).collection(year).document(month).get().addOnSuccessListener(documentSnapshot1 -> {
                                    if (!documentSnapshot1.exists()) {
                                        if (counter[0] == queryDocumentSnapshots.size() - 1) {
                                            try {
                                                csvWriter.build(year + "-" + month);
                                                Snackbar.make(binding.getRoot(), "CSV file created", Snackbar.LENGTH_SHORT).show();

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
                                        if (allowance.getType() != AllowancesEnum.NETSALARY.ordinal()) {
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
                                            } else if (allowance.getType() == AllowancesEnum.RETENTION.ordinal()) {
                                                cuts += allowance.getAmount();
                                            } else if (allowance.getType() == AllowancesEnum.BONUS.ordinal()) {
                                                personal += allowance.getAmount();
                                            } else if (allowance.getType() == AllowancesEnum.OVERTIME.ordinal()) {
                                                overTime += allowance.getAmount();
                                            } else {
                                                other += allowance.getAmount();
                                            }
                                        }
                                    }
                                    nextMonth = other + personal + accommodation + site + remote + food;
                                    currentMonth = transportation + emp.getSalary() + cuts + overTime;
                                    if (!doc.exists())
                                        previousMonth = 0;
                                    else {
                                        for (Allowance allowance : doc.toObject(EmployeesGrossSalary.class).getAllTypes()) {
                                            if (allowance.getType() != AllowancesEnum.NETSALARY.ordinal()) {
                                                if (allowance.getName().trim().equalsIgnoreCase("Transportation"))
                                                    continue;
                                                if (allowance.getType() == AllowancesEnum.RETENTION.ordinal()) {
                                                    continue;
                                                } else if (allowance.getType() == AllowancesEnum.OVERTIME.ordinal()) {
                                                    continue;
                                                } else {
                                                    previousMonth += allowance.getAmount();
                                                }
                                            }
                                        }
                                    }
                                    csvWriter.addDataRow(String.format("%s %s", emp.getFirstName(), emp.getLastName()), String.valueOf(emp.getSalary()), String.valueOf(overTime), String.valueOf(cuts), String.valueOf(transportation), String.valueOf(accommodation), String.valueOf(site), String.valueOf(remote), String.valueOf(food), String.valueOf(other), String.valueOf(personal), String.valueOf(nextMonth), String.valueOf(currentMonth), String.valueOf(previousMonth));
                                    if (counter[0] == queryDocumentSnapshots.size() - 1) {
                                        try {
                                            csvWriter.build(year + "-" + month);
                                            Snackbar.make(binding.getRoot(), "CSV file created", Snackbar.LENGTH_SHORT).show();
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
                        selectedMonth += 1;
                        binding.monthLayout.setError(null);
                        binding.monthLayout.setErrorEnabled(false);
                        binding.monthEdit.setText(String.format("%d/%d", selectedMonth, selectedYear));
                        String[] selectedDate = binding.monthEdit.getText().toString().split("/");
                        year = selectedDate[1];
                        month = selectedDate[0];
                        month = String.format("%02d", Integer.parseInt(month));
                        if ((Integer.parseInt(month) - 1) < 1) {
                            prevMonth = "12";
                            prevYear = Integer.parseInt(year) - 1 + "";
                        } else {
                            prevMonth = (Integer.parseInt(month) - 1) + "";
                            prevYear = year;
                        }
                        prevMonth = String.format("%02d", Integer.parseInt(prevMonth));
                        openMonthSummaryDialog(position);

                    }, today.get(Calendar.YEAR), today.get(Calendar.MONTH));
            MonthPickerDialog monthPickerDialog = builder.setActivatedMonth(today.get(Calendar.MONTH))
                    .setMinYear(today.get(Calendar.YEAR) - 1)
                    .setActivatedYear(today.get(Calendar.YEAR))
                    .setMaxYear(today.get(Calendar.YEAR) + 1)
                    .setTitle("Select Month")
                    .build();
            if (binding.monthEdit.getText().toString().isEmpty())
                monthPickerDialog.show();
            else {
                String[] selectedDate = binding.monthEdit.getText().toString().split("/");
                year = selectedDate[1];
                month = selectedDate[0];
                month = String.format("%02d", Integer.parseInt(month));
                openMonthSummaryDialog(position);
            }

        }
    };
}

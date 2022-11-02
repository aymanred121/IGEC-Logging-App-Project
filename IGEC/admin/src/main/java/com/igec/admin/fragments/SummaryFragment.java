package com.igec.admin.fragments;

import static android.content.ContentValues.TAG;
import static com.igec.common.CONSTANTS.EMPLOYEE_COL;
import static com.igec.common.CONSTANTS.EMPLOYEE_GROSS_SALARY_COL;
import static com.igec.common.CONSTANTS.EMPLOYEE_OVERVIEW_REF;
import static com.igec.common.CONSTANTS.HOLIDAYS;
import static com.igec.common.CONSTANTS.HOLIDAYS_COL;
import static com.igec.common.CONSTANTS.PROJECT_COL;
import static com.igec.common.CONSTANTS.SUMMARY_COL;
import static com.igec.common.CONSTANTS.VACATION_COL;

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

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.igec.admin.adapters.EmployeeAdapter;
import com.igec.admin.databinding.FragmentSummaryBinding;
import com.igec.admin.dialogs.MonthSummaryDialog;
import com.igec.common.firebase.Allowance;
import com.igec.common.firebase.Employee;
import com.igec.common.firebase.EmployeeOverview;
import com.igec.common.firebase.EmployeesGrossSalary;
import com.igec.common.firebase.Holiday;
import com.igec.common.firebase.Project;
import com.igec.common.firebase.Summary;
import com.igec.common.firebase.VacationRequest;
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
    private CsvWriter csvWriter;
    private String[] dataRow;
    private int dataRowSize = 0;
    int counter = 0;

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

    private void UpdateCSV(ArrayList<WorkingDay> workingDays, String empName, String id) {
        HOLIDAYS_COL.document(HOLIDAYS).get().addOnSuccessListener(doc -> {

            VACATION_COL
                    .whereEqualTo("vacationStatus", 1)
                    .whereEqualTo("employee.id", id).get().addOnSuccessListener(queryDocumentSnapshots -> {
                        dataRow = new String[dataRowSize + 1];
                        dataRow[0] = empName;
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
                        ArrayList<VacationRequest> vacationRequests = new ArrayList<>();
                        ArrayList<Holiday> holidays = new ArrayList<>();
                        addingFridays();

                        // holidays
                        holidays.clear();
                        prevYear = String.valueOf(Integer.parseInt(year) - 1);
                        prevMonth = String.valueOf(Integer.parseInt(month) - 1);
                        if (doc.exists() && (doc.contains(year) || doc.contains(prevYear))) {
                            if (doc.getData().get(year) != null) {
                                // loop over hashmap
                                for (Object o : ((ArrayList<Object>) doc.getData().get(year))) {
                                    holidays.add(new Holiday((HashMap) o));
                                }
                                if (doc.getData().get(prevYear) != null) {
                                    for (Object o : ((ArrayList<Object>) doc.getData().get(prevYear))) {
                                        holidays.add(new Holiday((HashMap) o));
                                    }
                                }
                                for (int i = 1; i < dataRow.length; i++) {
                                    Calendar thisMonthCalendar = Calendar.getInstance();
                                    Calendar prevMonthCalendar = Calendar.getInstance();
                                    if (Integer.parseInt(month) == 1) {
                                        thisMonthCalendar.set(Integer.parseInt(year), 0, i);
                                        prevMonthCalendar.set(Integer.parseInt(prevYear), 11, i);
                                    } else {
                                        thisMonthCalendar.set(Integer.parseInt(year), Integer.parseInt(month) - 1, i);
                                        prevMonthCalendar.set(Integer.parseInt(year), Integer.parseInt(prevMonth) - 1, i);
                                    }
                                    if (isHoliday(holidays, thisMonthCalendar) || isHoliday(holidays, prevMonthCalendar)) {
                                        if (dataRow[i].equals("0") || dataRow[i].equals("Home")) {
                                            dataRow[i] = "holiday";
                                        } else {
                                            dataRow[i] = dataRow[i] + " (holiday)";
                                        }
                                    }
                                }

                            }
                        }


                        // vacations
                        if (queryDocumentSnapshots.getDocuments().size() != 0) {
                            for (QueryDocumentSnapshot d : queryDocumentSnapshots) {
                                vacationRequests.add(d.toObject(VacationRequest.class));
                            }
                            Calendar it = Calendar.getInstance();
                            Calendar end = Calendar.getInstance();
                            vacationRequests.forEach(v -> {
                                String vacationLabels[] = new String[(int) v.getRequestedDays()];
                                addVacationLabels(v.getVacationDays(), vacationLabels, "vacation");
                                addVacationLabels(v.getSickDays(), vacationLabels, "sick leave");
                                addVacationLabels(v.getUnpaidDays(), vacationLabels, "unpaid");
                                int labelIndex = 0;
                                if (v.getStartDate().getMonth() != Integer.parseInt(month) - 1) {
                                    if (v.getEndDate().getMonth() != Integer.parseInt(month) - 1)
                                        return;
                                    else {
                                        // set it to be the start of the month in endDate
                                        it.setTime(v.getEndDate());
                                        // subtract days from it to reach the start of the month
                                        it.add(Calendar.DAY_OF_MONTH, -it.get(Calendar.DAY_OF_MONTH) + 1);

                                        // count days between startDate and it
                                        int days = (int) ((it.getTimeInMillis() - v.getStartDate().getTime()) / (1000 * 60 * 60 * 24));
                                        // add days to labelIndex
                                        labelIndex += days;
                                    }
                                } else {
                                    it.setTime(v.getStartDate());
                                }
                                end.setTime(v.getEndDate());
                                end.add(Calendar.DAY_OF_MONTH, 1);
                                while (it.before(end)) {
                                    // skip if it's friday
                                    int index = Integer.parseInt(String.valueOf(it.get(Calendar.DAY_OF_MONTH)));
                                    if (it.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
                                        it.add(Calendar.DAY_OF_MONTH, 1);
                                        continue;
                                    }
                                    dataRow[index] = vacationLabels[labelIndex];
                                    labelIndex++;
                                    it.add(Calendar.DAY_OF_MONTH, 1);
                                    if (it.get(Calendar.DAY_OF_MONTH) == 1) {
                                        break;
                                    }
                                }
                            });
                        }
                        csvWriter.addDataRow(dataRow);
                        counter++;
                        if (counter == employees.size()) {
                            try {
                                csvWriter.build("all_emp-" + year + "-" + month);
                                Snackbar.make(binding.getRoot(), "csv Saved!", Snackbar.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });

        });


    }

    private boolean isHoliday(ArrayList<Holiday> holidays, Calendar day) {
        boolean isHoliday = false;
        for (Holiday holiday : holidays) {// check if day is between start and end in holiday without using after and before without caring about hours
            if (day.get(Calendar.YEAR) == holiday.getStartCalendar().get(Calendar.YEAR) &&
                    day.get(Calendar.MONTH) == holiday.getStartCalendar().get(Calendar.MONTH) &&
                    day.get(Calendar.DAY_OF_MONTH) >= holiday.getStartCalendar().get(Calendar.DAY_OF_MONTH) &&
                    day.get(Calendar.YEAR) == holiday.getEndCalendar().get(Calendar.YEAR) &&
                    day.get(Calendar.MONTH) == holiday.getEndCalendar().get(Calendar.MONTH) &&
                    day.get(Calendar.DAY_OF_MONTH) <= holiday.getEndCalendar().get(Calendar.DAY_OF_MONTH)) {
                isHoliday = true;
            }
        }

        return isHoliday;
    }

    private void addingFridays() {
        for (int i = 1; i < dataRow.length; i++) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Integer.parseInt(year), Integer.parseInt(month) - 1, i);
            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
                if (!dataRow[i].equals("0") && !dataRow[i].equals("Home"))
                    dataRow[i] = dataRow[i] + " (off)";
                else
                    dataRow[i] = "off";
            }
        }
    }

    private void addVacationLabels(int days, String[] vacationLabels, String label) {
        int index = 0;
        for (int i = 0; i < vacationLabels.length; i++) {
            if (vacationLabels[i] != null)
                index++;
            else
                break;
        }
        for (int i = index; i < index + days; i++) {
            vacationLabels[i] = label;
        }
    }

    private void loadWorkingDays(EmployeeOverview employee, String empName) {
        ArrayList<WorkingDay> workingDays = new ArrayList<>();
        SUMMARY_COL.document(employee.getId()).collection(year + "-" + month)
                .get().addOnSuccessListener(docs -> {
                    if (docs.size() == 0) {
                        UpdateCSV(workingDays, empName, employee.getId());
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
                            String projectName;
                            if (summary.getProjectIds().get(pid).equals("office")) {
                                projectName = "office";
                            } else
                                projectName = project.getName();
                            workingDays.add(new WorkingDay(day, month, year, hours, empName, checkInLocation, checkOutLocation, projectName, project.getReference(), projectLocation, summary.getProjectIds().get(pid)));
                            if (docs.getDocuments().lastIndexOf(q) == docs.getDocuments().size() - 1) {
                                //sort working days by date
                                workingDays.sort((o1, o2) -> {
                                    int o1Day = Integer.parseInt(o1.getDay());
                                    int o2Day = Integer.parseInt(o2.getDay());
                                    return o1Day - o2Day;
                                });
                                UpdateCSV((ArrayList<WorkingDay>) workingDays.clone(), empName, employee.getId());
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
        counter = 0;
        if (binding.monthEdit.getText().toString().isEmpty()) {
            binding.monthLayout.setError("Please select a month");
        } else {
            StringJoiner header = new StringJoiner(",");
            header.add("Day");
            int yearNumber = Integer.parseInt(year);
            int monthNumber = Integer.parseInt(month) - 1;
            Calendar calendar = Calendar.getInstance();
            //check if month is february using calendar
            if (monthNumber == 0) {
                monthNumber = 12;
                yearNumber--;
            }
            calendar.set(yearNumber, monthNumber - 1, 1);
            dataRowSize = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            for (int i = 1; i <= dataRowSize; i++) {
                header.add(String.format("%d", i));
            }

            dataRow = new String[dataRowSize];
            csvWriter = new CsvWriter(header.toString().split(","));
            for (EmployeeOverview emp : employees) {
                String empName = emp.getFirstName() + " " + emp.getLastName();
                dataRow[0] = empName;
                loadWorkingDays(emp, empName);
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

package com.example.igecuser.Fragments;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.igecuser.Adapters.AllowanceAdapter;
import com.example.igecuser.R;
import com.example.igecuser.fireBase.Allowance;
import com.example.igecuser.fireBase.EmployeesGrossSalary;
import com.example.igecuser.utilites.allowancesEnum;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.whiteelephant.monthpicker.MonthPickerDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.stream.IntStream;

public class GrossSalaryFragment extends Fragment {


    private TextInputLayout selectedMonthLayout;
    private TextInputEditText selectedMonthEdit;
    private RecyclerView recyclerView;
    private AllowanceAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Allowance> salarySummaries;
    private EmployeesGrossSalary employeesGrossSalary;
    private TextView vGrossSalary;
    private String employeeId;
    private String year,month,prevMonth,prevYear;
    private double salarySummary = 0;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static GrossSalaryFragment newInstance(String employeeId) {

        Bundle args = new Bundle();
        args.putString("employeeId",employeeId);
        GrossSalaryFragment fragment = new GrossSalaryFragment();
        fragment.setArguments(args);
        return fragment;
    }
//    public GrossSalaryFragment(String employeeId) {
//        this.employeeId = employeeId;
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gross_salary, container, false);
    }


    AllowanceAdapter.OnItemClickListener onItemClickListener = new AllowanceAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
        }

        @Override
        public void onDeleteItem(int position) {

        }
    };

    // Functions
    private void initialize(View view) {
        employeeId = getArguments().getString("employeeId");
        selectedMonthEdit = view.findViewById(R.id.TextInput_SelectedMonth);
        selectedMonthLayout = view.findViewById(R.id.textInputLayout_SelectedMonth);
        salarySummaries = new ArrayList<>();
        recyclerView = view.findViewById(R.id.recyclerview);
        vGrossSalary = view.findViewById(R.id.TextView_GrossSalary);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new AllowanceAdapter(salarySummaries, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        String currentDateAndTime = sdf.format(new Date());
        month = currentDateAndTime.substring(3, 5);
        year = currentDateAndTime.substring(6, 10);
        selectedMonthEdit.setText(String.format("%s/%s", month, year));
        getGrossSalary();

    }
    private void getGrossSalary()
    {
        salarySummaries.clear();
        adapter.notifyDataSetChanged();
        db.collection("EmployeesGrossSalary").document(employeeId).collection(year).document(month).addSnapshotListener((value, error) -> {
            if (!value.exists())
                return;
            salarySummary = 0;
            employeesGrossSalary = value.toObject(EmployeesGrossSalary.class);
            for (Allowance allowance : employeesGrossSalary.getAllTypes()) {
                if(allowance.getAmount()==0)
                    continue;
                salarySummaries.add(allowance);
                salarySummary -= allowance.getType() == allowancesEnum.RETENTION.ordinal() ? allowance.getAmount() : -allowance.getAmount();
            }
            vGrossSalary.setText(String.format("%.2f EGP", salarySummary));
            vGrossSalary.setTextColor(Color.rgb(salarySummary > 0 ? 0 : 153, salarySummary > 0 ? 153 : 0, 0));
            adapter.setAllowances(salarySummaries);
            adapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize(view);
        adapter.setOnItemClickListener(onItemClickListener);
        selectedMonthLayout.setEndIconOnClickListener(oclMonthPicker);
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
                        getGrossSalary();
                    }
                }, today.get(Calendar.YEAR), today.get(Calendar.MONTH));
        builder.setActivatedMonth(Integer.parseInt(month)-1)
                .setActivatedYear(Integer.parseInt(year))
                .setTitle("Select Month")
                .build().show();

    };
}
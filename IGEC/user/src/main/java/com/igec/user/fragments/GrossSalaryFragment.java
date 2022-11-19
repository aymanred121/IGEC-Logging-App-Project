package com.igec.user.fragments;

import static com.igec.common.CONSTANTS.EMPLOYEE_GROSS_SALARY_COL;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.igec.common.adapters.AllowanceAdapter;
import com.igec.common.firebase.Allowance;
import com.igec.common.firebase.EmployeesGrossSalary;
import com.igec.common.utilities.AllowancesEnum;
import com.igec.user.activities.DateInaccurate;
import com.igec.user.databinding.FragmentGrossSalaryBinding;
import com.whiteelephant.monthpicker.MonthPickerDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class GrossSalaryFragment extends Fragment {



    private AllowanceAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Allowance> salarySummaries;
    private EmployeesGrossSalary employeesGrossSalary;
    private String employeeId;
    private String year, month,day;
    private double salarySummary = 0;
    private double salarySummarySAR = 0;

    public static GrossSalaryFragment newInstance(String employeeId) {

        Bundle args = new Bundle();
        args.putString("employeeId", employeeId);
        GrossSalaryFragment fragment = new GrossSalaryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private FragmentGrossSalaryBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentGrossSalaryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
    @Override
    public void onResume() {
        super.onResume();
        validateDate(getActivity());
    }
    private void validateDate(Context c) {
        if (Settings.Global.getInt(c.getContentResolver(), Settings.Global.AUTO_TIME, 0) != 1) {
            Intent intent = new Intent(getActivity(), DateInaccurate.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            getActivity().finish();
        }
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
    private void initialize() {
        employeeId = getArguments().getString("employeeId");
        salarySummaries = new ArrayList<>();
        binding.recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new AllowanceAdapter(salarySummaries, false,false);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        String currentDateAndTime = sdf.format(new Date());
        day = currentDateAndTime.substring(0, 2);
        month = currentDateAndTime.substring(3, 5);
        year = currentDateAndTime.substring(6, 10);
        binding.monthEdit.setText(String.format("%s/%s", month, year));
        getGrossSalary();

    }

    private void getGrossSalary() {
        if(Integer.parseInt(day)>25){
            if(month.equals("12")){
                month="01";
                year = String.format(Locale.getDefault(),"%d", Integer.parseInt(year) + 1);
            }else{
                month = String.format(Locale.getDefault(),"%02d", Integer.parseInt(month)+1);
            }
        }
        EMPLOYEE_GROSS_SALARY_COL.document(employeeId).collection(year).document(month).addSnapshotListener((value, error) -> {
            if (!value.exists()){
                adapter.getAllowances().clear();
                adapter.notifyDataSetChanged();
                binding.grossSalaryText.setText("No Available Salary");
                return;
            }
            salarySummaries.clear();
            salarySummary = 0;
            salarySummarySAR = 0;
            employeesGrossSalary = value.toObject(EmployeesGrossSalary.class);
            for (Allowance allowance : employeesGrossSalary.getAllTypes()) {
                if (allowance.getAmount() == 0)
                    continue;
                salarySummaries.add(allowance);
                if (allowance.getCurrency().equals("EGP"))
                    salarySummary += allowance.getType() != AllowancesEnum.RETENTION.ordinal() ? allowance.getAmount() : -allowance.getAmount();
                if (allowance.getCurrency().equals("SAR"))
                    salarySummarySAR += allowance.getType() != AllowancesEnum.RETENTION.ordinal() ? allowance.getAmount() : -allowance.getAmount();            }
            String gross;
            gross = String.format(
                    "%s%s%s",
                    salarySummary == 0 ? "" : String.format("%.2f EGP", salarySummary)
                    , (salarySummary != 0 && salarySummarySAR != 0) ? " , " : ""
                    , salarySummarySAR == 0 ? "" : String.format("%.2f SAR", salarySummarySAR)
            );
            binding.grossSalaryText.setText(gross);
            adapter.setAllowances(salarySummaries);
            adapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize();
        adapter.setOnItemClickListener(onItemClickListener);
        binding.monthLayout.setEndIconOnClickListener(oclMonthPicker);
    }

    private final View.OnClickListener oclMonthPicker = v -> {
        final Calendar today = Calendar.getInstance();
        MonthPickerDialog.Builder builder = new MonthPickerDialog.Builder(getActivity(),
                new MonthPickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(int selectedMonth, int selectedYear) {
                        binding.monthLayout.setError(null);
                        binding.monthLayout.setErrorEnabled(false);
                        binding.monthEdit.setText(String.format("%d/%d", selectedMonth + 1, selectedYear));
                        String[] selectedDate = binding.monthEdit.getText().toString().split("/");
                        year = selectedDate[1];
                        month = selectedDate[0];
                        if (month.length() == 1) {
                            month = "0" + month;
                        }
                        getGrossSalary();
                    }
                }, today.get(Calendar.YEAR), today.get(Calendar.MONTH));
        builder.setActivatedMonth(Integer.parseInt(month) - 1)
                .setActivatedYear(Integer.parseInt(year))
                .setTitle("Select Month")
                .build().show();

    };
}
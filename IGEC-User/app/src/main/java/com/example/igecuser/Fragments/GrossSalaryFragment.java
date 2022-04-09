package com.example.igecuser.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.igecuser.Adapters.AllowanceAdapter;
import com.example.igecuser.R;
import com.example.igecuser.fireBase.Allowance;
import com.example.igecuser.fireBase.EmployeesGrossSalary;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.stream.IntStream;

public class GrossSalaryFragment extends Fragment {


    private RecyclerView recyclerView;
    private AllowanceAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Allowance> salarySummaries;
    private EmployeesGrossSalary employeesGrossSalary;
    private final String employeeId;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public GrossSalaryFragment(String employeeId) {
        this.employeeId = employeeId;
    }

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
        salarySummaries = new ArrayList<>();
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new AllowanceAdapter(salarySummaries, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        db.collection("EmployeesGrossSalary").document(employeeId).addSnapshotListener((value, error) -> {
            if (!value.exists())
                return;
            salarySummaries.clear();
            employeesGrossSalary = value.toObject(EmployeesGrossSalary.class);
            IntStream.range(0, employeesGrossSalary.getPenalties().size()).forEach(i -> employeesGrossSalary.getPenalties().get(i).setAmount(employeesGrossSalary.getPenalties().get(i).getAmount() * -1));
            salarySummaries.addAll(employeesGrossSalary.getPenalties());
            salarySummaries.addAll(employeesGrossSalary.getBonuses());
            salarySummaries.addAll(employeesGrossSalary.getAllowances());
            salarySummaries.addAll(employeesGrossSalary.getProjectAllowances());
            salarySummaries.add(new Allowance("Net Salary", employeesGrossSalary.getNetSalary()));
            adapter.setAllowances(salarySummaries);
            adapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize(view);
        adapter.setOnItemClickListener(onItemClickListener);
    }


}
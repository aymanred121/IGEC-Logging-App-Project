package com.igec.admin.Fragments;

import static android.content.ContentValues.TAG;

import static com.igec.common.CONSTANTS.EMPLOYEE_COL;
import static com.igec.common.CONSTANTS.EMPLOYEE_OVERVIEW_REF;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.igec.admin.Adapters.EmployeeAdapter;
import com.igec.admin.Dialogs.UserFragmentDialog;
import com.igec.admin.R;
import com.igec.admin.databinding.FragmentUsersBinding;
import com.igec.common.firebase.Employee;
import com.igec.common.firebase.EmployeeOverview;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class UsersFragment extends Fragment {
    private ArrayList<EmployeeOverview> employees = new ArrayList<>();
    private EmployeeAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private boolean opened = false;

    private FragmentUsersBinding binding;

    public void setOpened(boolean opened) {
        this.opened = opened;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentUsersBinding.inflate(inflater, container, false);
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
        adapter.setOnItemClickListener(itclEmployeeAdapter);
    }

    void initialize() {
        binding.recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new EmployeeAdapter(employees, false);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);
        getEmployees();

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


    private final EmployeeAdapter.OnItemClickListener itclEmployeeAdapter = new EmployeeAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
                if(opened) return;
                opened = true;
                EMPLOYEE_COL.document(adapter.getEmployeeOverviewsList().get(position).getId()).get().addOnSuccessListener(documentSnapshot -> {
                    UserFragmentDialog userFragmentDialog = UserFragmentDialog.newInstance(documentSnapshot.toObject(Employee.class));
                    userFragmentDialog.show(getParentFragmentManager(), "");
                });
        }

        @Override
        public void onCheckboxClick(int position) {

        }

        @Override
        public void onRadioClick(int position) {

        }
    };

}
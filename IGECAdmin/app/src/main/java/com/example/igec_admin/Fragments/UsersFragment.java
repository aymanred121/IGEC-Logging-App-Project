package com.example.igec_admin.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.igec_admin.Adatpers.EmployeeAdapter;
import com.example.igec_admin.Dialogs.UserFragmentDialog;
import com.example.igec_admin.R;
import com.example.igec_admin.fireBase.EmployeeOverview;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UsersFragment extends Fragment {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference employeeRef = db.collection("EmployeeOverview").document("emp");
    ArrayList<EmployeeOverview> employees = new ArrayList();
    EmployeeAdapter adapter;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users,container,false);
        initialize(view);

        adapter.setOnItemClickListener(itclEmployeeAdapter);
        return view;
    }
    void initialize(View view){
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new EmployeeAdapter(employees,false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        getEmployees();
    }
    void getEmployees(){
        employeeRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Map<String, ArrayList<String>> empMap;
                empMap = (HashMap) documentSnapshot.getData();
                retrieveEmployees(empMap);
            }
        });
    }

    private void retrieveEmployees(Map<String, ArrayList<String>> empMap) {
        employees.clear();
        for (String key : empMap.keySet()) {
            String firstName = empMap.get(key).get(0);
            String lastName = empMap.get(key).get(1);
            String title = empMap.get(key).get(2);
            String managerID = empMap.get(key).get(3);
            String id = (key);
            employees.add(new EmployeeOverview(firstName, lastName, title, id));
        }
        adapter.setEmployeesList(employees);
        adapter.notifyDataSetChanged();
    }


    private EmployeeAdapter.OnItemClickListener itclEmployeeAdapter = new EmployeeAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
            UserFragmentDialog userFragmentDialog = new UserFragmentDialog(adapter.getEmployeesList().get(position));
            userFragmentDialog.show(getParentFragmentManager(),"");
        }

        @Override
        public void onCheckboxClick(int position) {

        }
    };

}
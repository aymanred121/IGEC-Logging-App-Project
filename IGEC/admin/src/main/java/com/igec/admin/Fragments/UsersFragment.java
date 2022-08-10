package com.igec.admin.Fragments;

import static android.content.ContentValues.TAG;

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

import com.igec.admin.Adapters.EmployeeAdapter;
import com.igec.admin.Dialogs.UserFragmentDialog;
import com.igec.admin.R;
import com.igec.common.firebase.Employee;
import com.igec.common.firebase.EmployeeOverview;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UsersFragment extends Fragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference employeeOverviewRef = db.collection("EmployeeOverview").document("emp");
    private ArrayList<EmployeeOverview> employees = new ArrayList<>();
    private EmployeeAdapter adapter;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

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
        employeeOverviewRef.addSnapshotListener((documentSnapshot, e) -> {
            HashMap empMap;
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }
            if(documentSnapshot != null && documentSnapshot.exists()){
                empMap = (HashMap)documentSnapshot.getData();
                retrieveEmployees(empMap);
            }else{
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


    private final EmployeeAdapter.OnItemClickListener itclEmployeeAdapter = new EmployeeAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
            db.collection("employees").document(adapter.getEmployeeOverviewsList().get(position).getId()).get().addOnSuccessListener(documentSnapshot -> {
                UserFragmentDialog userFragmentDialog = new UserFragmentDialog(documentSnapshot.toObject(Employee.class),employees,position);
                userFragmentDialog.show(getParentFragmentManager(),"");
            });
        }

        @Override
        public void onCheckboxClick(int position) {

        }

    };

}
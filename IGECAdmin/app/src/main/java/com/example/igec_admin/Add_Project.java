package com.example.igec_admin;

import static android.content.ContentValues.TAG;

import android.os.Bundle;


import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.example.igec_admin.fireBase.Employee;
import com.example.igec_admin.fireBase.EmployeeOverview;
import com.example.igec_admin.fireBase.Project;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Add_Project extends Fragment {

    // Views
    private TextInputEditText vName, vLocation, vStartTime, vEndTime, vManagerName;
    private MaterialButton vRegister;
    private AutoCompleteTextView vManagerID;
    private TextInputLayout vManagerIDLayout;
    private RecyclerView recyclerView;
    private EmployeeAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    // Vars
    MaterialDatePicker.Builder<Long> vStartDatePickerBuilder = MaterialDatePicker.Builder.datePicker();
    MaterialDatePicker vStartDatePicker;
    MaterialDatePicker.Builder<Long> vEndDatePickerBuilder = MaterialDatePicker.Builder.datePicker();
    MaterialDatePicker vEndDatePicker;
    ArrayList<EmployeeOverview> employees = new ArrayList();
    ArrayList<String> TeamID = new ArrayList<>();
    ArrayList<EmployeeOverview> Team = new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
  DocumentReference employeeOverviewRef = db.collection("EmployeeOverview")
            .document("emp");
  CollectionReference employeeCol = db.collection("employees");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_project, container, false);
        Initialize(view);

        // listeners
        vRegister.setOnClickListener(clRegister);
        vManagerID.addTextChangedListener(twManagerID);
        vStartDatePicker.addOnPositiveButtonClickListener(pclStartDatePicker);
        vEndDatePicker.addOnPositiveButtonClickListener(pclEndDatePicker);
        vStartTime.setOnFocusChangeListener(fclStartDate);
        vStartTime.setOnClickListener(clStartDate);
        vEndTime.setOnFocusChangeListener(fclEndDate);
        vEndTime.setOnClickListener(clEndDate);


        // Inflate the layout for this fragment
        return view;
    }

    // Functions
    private void Initialize(View view) {
        vName = view.findViewById(R.id.TextInput_ProjectName);
        vLocation = view.findViewById(R.id.TextInput_Location);
        vStartTime = view.findViewById(R.id.TextInput_StartTime);
        vEndTime = view.findViewById(R.id.TextInput_EndTime);
        vManagerID = view.findViewById(R.id.TextInput_ManagerID);
        vManagerIDLayout = view.findViewById(R.id.textInputLayout_ManagerID);
        vManagerName = view.findViewById(R.id.TextInput_ManagerName);
        vRegister = view.findViewById(R.id.button_register);
        vStartDatePickerBuilder.setTitleText("Start Date");
        vStartDatePicker = vStartDatePickerBuilder.build();
        vEndDatePickerBuilder.setTitleText("End Date");
        vEndDatePicker = vEndDatePickerBuilder.build();
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new EmployeeAdapter(employees);
        adapter.setOnItemClickListener(itclEmployeeAdapter);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        getEmployees();
    }

    void ChangeSelectedTeam(int position) {
        employees.get(position).setSelected(!employees.get(position).getSelected());
        if (employees.get(position).getSelected()) {
            Team.add(employees.get(position));
            TeamID.add(String.valueOf(employees.get(position).getId()));
        } else {
            if (!vManagerID.getText().toString().isEmpty() && vManagerID.getText().toString().equals(employees.get(position).getId().toString()))
                vManagerID.setText("");
            Team.remove(employees.get(position));
            TeamID.remove(String.valueOf(employees.get(position).getId()));


        }
        vManagerIDLayout.setEnabled(Team.size() >= 2);
        if (!vManagerIDLayout.isEnabled())
            vManagerID.setText("");
        if (TeamID.size() > 0) {
            ArrayAdapter<String> idAdapter = new ArrayAdapter<>(getActivity(), R.layout.dropdown_item, TeamID);
            vManagerID.setAdapter(idAdapter);

        }
        adapter.notifyItemChanged(position);
    }

   void getEmployees() {
        ArrayList<EmployeeOverview> employeeArray = new ArrayList();
      employeeOverviewRef
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    String source = documentSnapshot != null && documentSnapshot.getMetadata().hasPendingWrites()
                            ? "Local" : "Server";

                    if (documentSnapshot.exists()) {
                        Map<String, ArrayList<String>> empMap;
                        empMap = (HashMap) documentSnapshot.getData();
                        retrieveEmployees(empMap);
                }
            });
    }

    private void updateEmployeesDetails(String projectID) {
        for(String id : TeamID){
            if(id.equals(vManagerID.getText().toString())){
                employeeCol.document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Employee emp = documentSnapshot.toObject(Employee.class);
                        ArrayList<String> empInfo = new ArrayList<>();
                        empInfo.add(emp.getFirstName());
                        empInfo.add(emp.getLastName());
                        empInfo.add(emp.getTitle());
                        empInfo.add("adminID");
                        Map<String, Object> empInfoMap = new HashMap<>();
                        empInfoMap.put(id,empInfo);
                        employeeOverviewRef.update(empInfoMap);
                        employeeCol.document(id).update("managerID","adminID","projectID",projectID);
                    }
                });

                continue;
            }
            employeeCol.document(id)
                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    Employee emp = documentSnapshot.toObject(Employee.class);
                    ArrayList<String> empInfo = new ArrayList<>();
                    empInfo.add(emp.getFirstName());
                    empInfo.add(emp.getLastName());
                    empInfo.add(emp.getTitle());
                    empInfo.add(vManagerID.getText().toString());
                    Map<String, Object> empInfoMap = new HashMap<>();
                    empInfoMap.put(id,empInfo);
                    employeeOverviewRef.update(empInfoMap);
                    employeeCol.document(id).update("managerID",vManagerID.getText().toString(),"projectID",projectID);
                }
            });
        }
    }
    private void addProject() {
        Date startDate = convertStringDate(vStartTime.getText().toString());
        Date endDate = convertStringDate(vEndTime.getText().toString());
        String projectID= db.collection("projects").document().getId().substring(0,5);
        updateTeam();
        Project newProject = new Project(vManagerName.getText().toString(),vManagerID.getText().toString(),vName.getText().toString(),startDate,endDate,Team,vLocation.getText().toString());
        newProject.setId(projectID);
        db.collection("projects").document(projectID).set(newProject).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                updateEmployeesDetails(projectID);
            }
        });
    }

    private void updateTeam() {
        for(EmployeeOverview emp :Team ){
            if(emp.getId().equals(vManagerID.getText().toString())){
                emp.setManagerID("adminID");
            }else
                emp.setManagerID(vManagerID.getText().toString());
        }
    }


    Date convertStringDate(String sDate){
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date date = format.parse(sDate);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
    void retrieveEmployees(Map<String, ArrayList<String>> empMap){
        employees.clear();
        for (String key : empMap.keySet()) {
            String firstName = empMap.get(key).get(0);
            String lastName = empMap.get(key).get(1);
            String title = empMap.get(key).get(2);
            String managerID =  empMap.get(key).get(3);
            String id = (key);
            if((managerID == null))
            employees.add(new EmployeeOverview(firstName, lastName, title, id));
        }
        adapter.setEmployeesList(employees);
        adapter.notifyDataSetChanged();

    }
    // Listeners
    TextWatcher twManagerID = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (vManagerID.getText().length() > 0) {
                int position = 0;
                for (int i = 0; i < Team.size(); i++) {
                    if (String.valueOf(Team.get(i).getId()).equals(s.toString())) {
                        position = i;

                    }
                }
                vManagerName.setText(Team.get(position).getFirstName() + " " + Team.get(position).getLastName());
            } else
                vManagerName.setText(null);
        }
    };
    View.OnFocusChangeListener fclStartDate = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                vStartDatePicker.show(getFragmentManager(), "DATE_PICKER");
            }
        }
    };
    MaterialPickerOnPositiveButtonClickListener pclStartDatePicker = new MaterialPickerOnPositiveButtonClickListener() {
        @Override
        public void onPositiveButtonClick(Object selection) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis((long) selection);
            vStartTime.setText(simpleDateFormat.format(calendar.getTime()));
        }
    };
    View.OnFocusChangeListener fclEndDate = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                vEndDatePicker.show(getFragmentManager(), "DATE_PICKER");
            }
        }
    };
    MaterialPickerOnPositiveButtonClickListener pclEndDatePicker = new MaterialPickerOnPositiveButtonClickListener() {
        @Override
        public void onPositiveButtonClick(Object selection) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis((long) selection);
            vEndTime.setText(simpleDateFormat.format(calendar.getTime()));
        }
    };
    View.OnClickListener clStartDate = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!vStartDatePicker.isVisible())
                vStartDatePicker.show(getFragmentManager(), "DATE_PICKER");
        }
    };
    View.OnClickListener clEndDate = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!vEndDatePicker.isVisible())
                vEndDatePicker.show(getFragmentManager(), "DATE_PICKER");
        }
    };
    View.OnClickListener clRegister = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            addProject();
        }



    };
    EmployeeAdapter.OnItemClickListener itclEmployeeAdapter = new EmployeeAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
            ChangeSelectedTeam(position);
        }

        @Override
        public void onCheckboxClick(int position) {
            ChangeSelectedTeam(position);
        }
    };

}
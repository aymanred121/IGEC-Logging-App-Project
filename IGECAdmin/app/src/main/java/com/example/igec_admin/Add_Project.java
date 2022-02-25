package com.example.igec_admin;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.example.igec_admin.fireBase.EmployeeOverview;
import com.example.igec_admin.fireBase.Employees;
import com.google.android.gms.tasks.Task;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Add_Project extends Fragment {


    // Views
    private TextInputEditText vName, vLocation, vStartTime, vEndTime, vManagerName;
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
    ArrayList<EmployeeOverview> employees =new ArrayList();
    ArrayList<String> TeamID = new ArrayList<>();
    ArrayList<EmployeeOverview> Team = new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_project, container,false);

        vName = view.findViewById(R.id.TextInput_ProjectName);
        vLocation = view.findViewById(R.id.TextInput_Location);
        vStartTime = view.findViewById(R.id.TextInput_StartTime);
        vEndTime = view.findViewById(R.id.TextInput_EndTime);
        vManagerID = view.findViewById(R.id.TextInput_ManagerID);
        vManagerIDLayout = view.findViewById(R.id.textInputLayout_ManagerID);
        vManagerName = view.findViewById(R.id.TextInput_ManagerName);
        vStartDatePickerBuilder.setTitleText("Start Date");
        vStartDatePicker = vStartDatePickerBuilder.build();
        vEndDatePickerBuilder.setTitleText("End Date");
        vEndDatePicker = vEndDatePickerBuilder.build();
        employees=getEmployee();

        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new EmployeeAdapter(employees);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new EmployeeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
               ChangeSelectedTeam(position);
            }

            @Override
            public void onCheckboxClick(int position) {
                ChangeSelectedTeam(position);
            }
        });
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
    void ChangeSelectedTeam(int position)
    {
        employees.get(position).isSelected = !employees.get(position).isSelected;
        if(employees.get(position).isSelected)
        {
            Team.add(employees.get(position));
            TeamID.add(String.valueOf(employees.get(position).getId()));
        }
        else
        {
            if(!vManagerID.getText().toString().isEmpty() && vManagerID.getText().toString().equals(TeamID.get(position).toString()))
                vManagerID.setText("");
            Team.remove(employees.get(position));
            TeamID.remove(String.valueOf(employees.get(position).getId()));


        }
        vManagerIDLayout.setEnabled(Team.size() >= 2);
        if(!vManagerIDLayout.isEnabled())
            vManagerID.setText("");
        if(TeamID.size() > 0) {
            ArrayAdapter<String> idAdapter = new ArrayAdapter<>(getActivity(), R.layout.dropdown_item, TeamID);
            vManagerID.setAdapter(idAdapter);

        }
        adapter.notifyItemChanged(position);
    }
    ArrayList<EmployeeOverview> getEmployee(){
        /**
         * This is a dummy solution
         * */
        ArrayList<EmployeeOverview> employeeArray = new ArrayList();
        Map<String,ArrayList<String>> empMap = new HashMap();
        Task dbTask= db.collection("EmployeeOverview").document("emp").get();
        while (true){
            if(dbTask.isComplete()){
                DocumentSnapshot documentSnapshot =(DocumentSnapshot) dbTask.getResult();
                if(documentSnapshot.exists()){
                    empMap = (HashMap) documentSnapshot.getData();
                    break;
                }
            }
        }
        for(String key : empMap.keySet()){
            String firstName = empMap.get(key).get(0);
            String lastName = empMap.get(key).get(1);
            String title = empMap.get(key).get(2);
            String id =(key);
            employeeArray.add(new EmployeeOverview(firstName,lastName,title,id));
        }
        return employeeArray;
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
            if(vManagerID.getText().length() > 0)
            {
                int position = 0;
                for(int i = 0 ; i < Team.size() ; i++)
                {
                    if(String.valueOf(Team.get(i).getId()).equals(s.toString()))
                    {
                        position = i;

                    }
                }
                vManagerName.setText(Team.get(position).getFirstName() + " " + Team.get(position).getLastName());
            }
            else
                vManagerName.setText(null);
        }
    };
    View.OnFocusChangeListener fclStartDate = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if(hasFocus)
            {
                vStartDatePicker.show(getFragmentManager(),"DATE_PICKER");
            }
        }
    };
    MaterialPickerOnPositiveButtonClickListener pclStartDatePicker =  new MaterialPickerOnPositiveButtonClickListener() {
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
            if(hasFocus)
            {
                vEndDatePicker.show(getFragmentManager(),"DATE_PICKER");
            }
        }
    };
    MaterialPickerOnPositiveButtonClickListener pclEndDatePicker =  new MaterialPickerOnPositiveButtonClickListener() {
        @Override
        public void onPositiveButtonClick(Object selection) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis((long) selection);
            vEndTime.setText(simpleDateFormat.format(calendar.getTime()));
        }
    };
    View.OnClickListener clStartDate =  new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(!vStartDatePicker.isVisible())
                vStartDatePicker.show(getFragmentManager(),"DATE_PICKER");
        }
    };

    View.OnClickListener clEndDate =  new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(!vEndDatePicker.isVisible())
                vEndDatePicker.show(getFragmentManager(),"DATE_PICKER");
        }
    };

}
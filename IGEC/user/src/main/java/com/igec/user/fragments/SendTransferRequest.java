package com.igec.user.fragments;

import static com.igec.common.CONSTANTS.PROJECT_COL;
import static com.igec.common.CONSTANTS.TRANSFER_REQUESTS_COL;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.igec.user.R;
import com.igec.common.firebase.Employee;
import com.igec.common.firebase.EmployeeOverview;
import com.igec.common.firebase.Project;
import com.igec.common.firebase.TransferRequests;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.igec.user.activities.DateInaccurate;
import com.igec.user.databinding.FragmentSendTransferRequestBinding;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class SendTransferRequest extends Fragment {

    private Employee manager;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ArrayList<Project> allProjects;
    private Project toProject, fromProject;
    private ArrayList<Pair<TextInputLayout, EditText>> views;
    private ArrayList<String> employeesId;
    private ArrayAdapter<String> idAdapter;
    private EmployeeOverview selectedEmployee;
    private ArrayList<String> fromProjectsRef, toProjectsRef;
    private ArrayAdapter<String> fromRefAdapter, toRefAdapter;

    public static SendTransferRequest newInstance(Employee manager) {

        Bundle args = new Bundle();
        args.putSerializable("manager", manager);
        SendTransferRequest fragment = new SendTransferRequest();
        fragment.setArguments(args);
        return fragment;
    }

    private FragmentSendTransferRequestBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSendTransferRequestBinding.inflate(inflater, container, false);
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
            startActivity(intent);
            getActivity().finish();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize();
        binding.fromProjectReferencesAuto.addTextChangedListener(twFromProjectRef);
        binding.toProjectReferencesAuto.addTextChangedListener(twToProjectRef);
        binding.employeeIdAuto.addTextChangedListener(twEmployeeID);
        binding.noteEdit.addTextChangedListener(twTransferNote);
        binding.sendButton.setOnClickListener(oclSend);
    }

    private void initialize() {
        manager = (Employee) getArguments().getSerializable("manager");
        fromProjectsRef = new ArrayList<>();
        toProjectsRef = new ArrayList<>();
        allProjects = new ArrayList<>();
        employeesId = new ArrayList<>();
        views = new ArrayList<>();
        views.add(new Pair<>(binding.fromProjectReferencesLayout, binding.fromProjectReferencesAuto));
        views.add(new Pair<>(binding.employeeIdLayout, binding.employeeIdAuto));
        views.add(new Pair<>(binding.toProjectReferencesLayout, binding.toProjectReferencesAuto));
        views.add(new Pair<>(binding.noteLayout, binding.noteEdit));
        fromRefAdapter = new ArrayAdapter<>(getActivity(), R.layout.item_dropdown, fromProjectsRef);
        toRefAdapter = new ArrayAdapter<>(getActivity(), R.layout.item_dropdown, toProjectsRef);
        idAdapter = new ArrayAdapter<>(getActivity(), R.layout.item_dropdown, employeesId);
        getAllProjects();
    }

    private Task<Void> sendRequest(EmployeeOverview employee) {
        String transferId = TRANSFER_REQUESTS_COL.document().getId();
        TransferRequests request = new TransferRequests();
        request.setTransferId(transferId);
        request.setEmployee(employee);
        request.setNewProjectId(toProject.getId());
        request.setNewProjectName(toProject.getName());
        request.setNewProjectReference(toProject.getReference());
        request.setOldProjectId(fromProject.getId());
        request.setOldProjectName(fromProject.getName());
        request.setOldProjectReference(fromProject.getReference());
        request.setNote(binding.noteEdit.getText().toString());
        return TRANSFER_REQUESTS_COL.document(transferId).set(request);
    }

    private void invalidateViews() {
        // fromProjects 1 or more -> never freezes
        // toProjectsRef 0 or more -> freezes when 0
        boolean disableToProjectRef = toProjectsRef.size() == 0;
        if (disableToProjectRef)
            binding.toProjectReferencesAuto.setText("No Available Projects");
        // employeesId 0 or more -> freezes when 0
        boolean disableEmployeeId = employeesId.size() == 0;
        if (disableEmployeeId)
            binding.employeeIdAuto.setText("No Available Employees");

        binding.toProjectReferencesAuto.setEnabled(!disableToProjectRef);
        binding.toProjectReferencesLayout.setEnabled(!disableToProjectRef);
        binding.employeeIdAuto.setEnabled(!disableEmployeeId);
        binding.employeeIdLayout.setEnabled(!disableEmployeeId);
        binding.sendButton.setEnabled(!disableToProjectRef);
    }


    /*
     * manager ==> {1,2,3}
     * toRef = {1,2,3}
     * fromRef = {1,2,3}
     * all projects ==> {1,2,3,4,5,6}
     * fromProject = 1
     * toRef = {2,3}
     * toProject = 2
     *
     *
     *
     *
     * */


    private void getAllProjects() {
        PROJECT_COL.addSnapshotListener((queryDocumentSnapshots, error) -> {
            if (error != null || queryDocumentSnapshots == null)
                return;
            allProjects.clear();
            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                Project project = document.toObject(Project.class);
                String ref = String.format("IGEC%s | %s", project.getReference(), project.getName());
                allProjects.add(project);
                // add all to fromProjectsRef and manager Projects only to toProjectsRef
                if (manager.getProjectIds().contains(project.getId()))
                    toProjectsRef.add(ref);
                fromProjectsRef.add(ref);
            }
            // choose first project as from project
            fromProject = allProjects.get(0); // arbitrary as a placeholder
            binding.fromProjectReferencesAuto.setText(String.format("IGEC%s | %s", fromProject.getReference(), fromProject.getName()));
            binding.fromProjectReferencesAuto.setAdapter(fromRefAdapter);
            // if fromProject exists in toProjectsRef remove it
            if (toProjectsRef.contains(String.format("IGEC%s | %s", fromProject.getReference(), fromProject.getName())))
                toProjectsRef.remove(String.format("IGEC%s | %s", fromProject.getReference(), fromProject.getName()));
            // toProject might be empty because there's only one project in the company
            if (toProjectsRef.size() != 0) {
                // choose the project with first ref in toProjectsRef
                for (Project p : allProjects)
                    if (String.format("IGEC%s | %s", p.getReference(), p.getName()).equals(toProjectsRef.get(0))) {
                        toProject = p;
                        break;
                    }
                binding.toProjectReferencesAuto.setText(String.format("IGEC%s | %s", toProject.getReference(), toProject.getName()));
                binding.toProjectReferencesAuto.setAdapter(toRefAdapter);
            }
            invalidateViews();
        });
    }

    private boolean generateError() {

        for (Pair<TextInputLayout, EditText> view : views) {
            if (view.second.getText().toString().trim().isEmpty()) {
                view.first.setError("Missing");
                return true;
            }
            if (view.first.getError() != null) {
                return true;
            }
        }
        return false;
    }

    private void hideError(TextInputLayout textInputLayout) {
        textInputLayout.setErrorEnabled(textInputLayout.getError() != null);

    }

    private boolean validateInput() {

        return !generateError();
    }

    private void clearInput() {
        binding.noteEdit.setText(null);
    }

    private final TextWatcher twTransferNote = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (!binding.noteEdit.getText().toString().trim().isEmpty())
                binding.noteLayout.setError(null);
            hideError(binding.noteLayout);
        }
    };
    private final TextWatcher twEmployeeID = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            for (EmployeeOverview emp : fromProject.getEmployees()) {
                if (binding.employeeIdAuto.getText().toString().contains(emp.getId())) {
                    selectedEmployee = emp;
                    break;
                }
            }
            binding.employeeIdLayout.setErrorEnabled(binding.employeeIdAuto.getText().toString().isEmpty());
        }
    };
    private final TextWatcher twFromProjectRef = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            boolean isOldManager = manager.getProjectIds().contains(fromProject.getId());
            boolean isNewManager = false;
            Project newFromProject = null;
            for (Project p : allProjects)
                if (String.format("IGEC%s | %s", p.getReference(), p.getName()).equals(s.toString())) {
                    isNewManager = manager.getProjectIds().contains(p.getId());
                    newFromProject = new Project(p);
                    break;
                }
            // old -> new
            // manager -> manager ==> add old to ToProjectsRef && remove new from ToProjectsRef
            if (isNewManager && isOldManager) {
                toProjectsRef.add(String.format("IGEC%s | %s", fromProject.getReference(), fromProject.getName()));
                toProjectsRef.remove(String.format("IGEC%s | %s", newFromProject.getReference(), newFromProject.getName()));
            }
            // not manager -> manager ==> remove new from ToProjectsRef
            else if (!isOldManager && isNewManager) {
                toProjectsRef.remove(String.format("IGEC%s | %s", newFromProject.getReference(), newFromProject.getName()));
            }
            // manager -> not manager ==> add old to ToProjectsRef
            else if (isOldManager && !isNewManager) {
                toProjectsRef.add(String.format("IGEC%s | %s", fromProject.getReference(), fromProject.getName()));
            }
            fromProject = newFromProject;
            // not manager -> not manager ==> do nothing
            // clear employees
            employeesId.clear();
            for (EmployeeOverview employee : fromProject.getEmployees()) {
                // add project employees except for the manager
                if (!employee.isManager) {
                    employeesId.add(String.format("%s | %s %s", employee.getId(), employee.getFirstName(), employee.getLastName()));
                    selectedEmployee = employee;
                }
            }
            if (employeesId.size() != 0) {
                binding.employeeIdAuto.setText(String.format("%s | %s %s", selectedEmployee.getId(), selectedEmployee.getFirstName(), selectedEmployee.getLastName()));
                idAdapter = new ArrayAdapter<>(getActivity(), R.layout.item_dropdown, employeesId);
                binding.employeeIdAuto.setAdapter(idAdapter);
            }
            if (toProjectsRef.size() != 0) {
                binding.toProjectReferencesAuto.setText(toProjectsRef.get(0));
                toRefAdapter = new ArrayAdapter<>(getActivity(), R.layout.item_dropdown, toProjectsRef);
                binding.toProjectReferencesAuto.setAdapter(toRefAdapter);
            }
            invalidateViews();
        }
    };
    private final TextWatcher twToProjectRef = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            for (Project p : allProjects)
                if (String.format("IGEC%s | %s", p.getReference(), p.getName()).equals(editable.toString())) {
                    toProject = new Project(p);
                    break;
                }
        }
    };
    private final View.OnClickListener oclSend = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (validateInput()) {
                sendRequest(selectedEmployee).addOnSuccessListener(unused -> {
                    Snackbar.make(binding.getRoot(), "Request Sent", Snackbar.LENGTH_SHORT).show();
                    clearInput();
                });
            }
        }
    };

    //save string to txt file
    private void saveToFile(String data, String filename) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getActivity().openFileOutput(filename, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

}
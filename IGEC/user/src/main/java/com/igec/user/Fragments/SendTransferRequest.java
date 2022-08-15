package com.igec.user.Fragments;

import static com.igec.common.CONSTANTS.PROJECT_COL;
import static com.igec.common.CONSTANTS.TRANSFER_REQUESTS_COL;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.igec.user.R;
import com.igec.common.firebase.Employee;
import com.igec.common.firebase.EmployeeOverview;
import com.igec.common.firebase.Project;
import com.igec.common.firebase.TransferRequests;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.igec.user.databinding.FragmentSendTransferRequestBinding;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class SendTransferRequest extends Fragment {

    private Employee manager;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final ArrayList<Project> projects = new ArrayList<>();
    private Project thisProject, chosenProject;
    private ArrayList<Pair<TextInputLayout, EditText>> views;
    private ArrayList<String> employeesId = new ArrayList<>();
    private ArrayAdapter<String> idAdapter;
    private EmployeeOverview selectedEmployee;
    private ArrayList<String> projectsRef = new ArrayList<>();
    private ArrayAdapter<String> refAdapter;

    public static SendTransferRequest newInstance(Employee manager) {

        Bundle args = new Bundle();
        args.putSerializable("manager",manager);
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize();
        binding.projectReferencesAuto.addTextChangedListener(twProjectRef);
        binding.employeeIdAuto.addTextChangedListener(twEmployeeID);
        binding.noteEdit.addTextChangedListener(twTransferNote);
        binding.sendButton.setOnClickListener(oclSend);
    }

    private void initialize() {
        manager = (Employee) getArguments().getSerializable("manager");
        projectsRef = new ArrayList<>();
        refAdapter = new ArrayAdapter<>(getActivity(), R.layout.item_dropdown, projectsRef);
        views = new ArrayList<>();
        views.add(new Pair<>( binding.projectReferencesLayout,  binding.projectReferencesAuto));
        views.add(new Pair<>(binding.employeeIdLayout, binding.employeeIdAuto));
        views.add(new Pair<>(binding.noteLayout, binding.noteEdit));
        idAdapter = new ArrayAdapter<>(getActivity(), R.layout.item_dropdown, employeesId);
        getProject();
    }

    private Task<Void> sendRequest(EmployeeOverview employee) {
        String transferId = TRANSFER_REQUESTS_COL.document().getId();
        TransferRequests request = new TransferRequests();
        request.setTransferId(transferId);
        request.setEmployee(employee);
        request.setNewProjectId(thisProject.getId());
        request.setNewProjectName(thisProject.getName());
        request.setNewProjectReference(thisProject.getReference());
        request.setOldProjectId(chosenProject.getId());
        request.setOldProjectName(chosenProject.getName());
        request.setOldProjectReference(chosenProject.getReference());
        request.setNote(binding.noteEdit.getText().toString());
        return TRANSFER_REQUESTS_COL.document(transferId).set(request);
    }

    private void getProject() {
        PROJECT_COL.document(manager.getProjectID()).get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists())
                return;
            thisProject = documentSnapshot.toObject(Project.class);
            getAllProjects(thisProject.getId());
        });
    }

    private void freezeViews(boolean freeze) {

        binding.projectReferencesAuto.setText(freeze ? "No Available Projects" : null);
        binding.employeeIdAuto.setText(freeze ? "No Available Employee" : null);
        binding.projectReferencesLayout.setEnabled(!freeze);
        binding.noteLayout.setEnabled(!freeze);
        binding.employeeIdLayout.setEnabled(!freeze);
        binding.sendButton.setEnabled(!freeze);
    }

    private void getAllProjects(String projectId) {
        PROJECT_COL.whereNotEqualTo("id", projectId).addSnapshotListener((queryDocumentSnapshots, error) -> {

            if (queryDocumentSnapshots.size() == 0) {
                freezeViews(true);
                return;
            }
            projectsRef.clear();
            projects.clear();
            projects.addAll((queryDocumentSnapshots.toObjects(Project.class)));
            for (Project project : projects)
                if (!project.getId().equals(thisProject.getId()))
                    projectsRef.add("IGEC" + project.getReference() + " | " + project.getName());

            chosenProject = projects.get(0);
            binding.projectReferencesAuto.setText(String.format("IGEC%s | %s", chosenProject.getReference(), chosenProject.getName()));
            binding.projectReferencesAuto.setAdapter(refAdapter);


//            getAllEmployees();
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
            for (EmployeeOverview emp : chosenProject.getEmployees()) {
                if (binding.employeeIdAuto.getText().toString().contains(emp.getId())) {
                    selectedEmployee = emp;
                    break;
                }
            }
            binding.employeeIdLayout.setErrorEnabled(binding.employeeIdAuto.getText().toString().isEmpty());
        }
    };
    private final TextWatcher twProjectRef = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            for (Project project : projects) {
                if (binding.projectReferencesAuto.getText().toString().contains(project.getReference()) &&
                        binding.projectReferencesAuto.getText().toString().contains(project.getName())) {
                    chosenProject = project;

                    employeesId.clear();
                    for (EmployeeOverview employee : project.getEmployees()) {
                        // add project employees except for the manager
                        if (!employee.getId().equals(project.getManagerID())) {
                            employeesId.add(String.format("%s | %s %s", employee.getId(), employee.getFirstName(), employee.getLastName()));
                        }
                    }
                    // no employees can be requested
                    boolean isThereEmployees = employeesId.size() != 0;
                    binding.employeeIdAuto.setText(isThereEmployees ? null : "No Available Employee");
                    binding.employeeIdLayout.setEnabled(isThereEmployees);
                    binding.sendButton.setEnabled(isThereEmployees);
                    binding.noteLayout.setEnabled(isThereEmployees);
                    idAdapter = new ArrayAdapter<>(getActivity(), R.layout.item_dropdown, employeesId);
                    binding.employeeIdAuto.setAdapter(idAdapter);
                    break;
                }
            }
        }
    };
    private final View.OnClickListener oclSend = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (validateInput()) {
                sendRequest(selectedEmployee).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Snackbar.make(binding.getRoot(), "Request Sent", Snackbar.LENGTH_SHORT).show();
                        clearInput();
                    }
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
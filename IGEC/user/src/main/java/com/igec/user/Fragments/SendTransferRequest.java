package com.igec.user.Fragments;

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
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.igec.user.R;
import com.igec.user.fireBase.Employee;
import com.igec.user.fireBase.EmployeeOverview;
import com.igec.user.fireBase.Project;
import com.igec.user.fireBase.TransferRequests;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class SendTransferRequest extends Fragment {

    private Employee manager;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final ArrayList<Project> projects = new ArrayList<>();
    private Project thisProject, chosenProject;
    private AutoCompleteTextView vProjectsReference, vEmployeesId;
    private TextInputEditText vTransferNote;
    private TextInputLayout vTransferNoteLayout, vProjectReferenceLayout, vEmployeesIdLayout;
    private ArrayList<Pair<TextInputLayout, EditText>> views;
    private MaterialButton vSend;
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
//    public SendTransferRequest(Employee manager) {
//        this.manager = manager;
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_send_transfer_request, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize(view);
        vProjectsReference.addTextChangedListener(twProjectRef);
        vEmployeesId.addTextChangedListener(twEmployeeID);
        vTransferNote.addTextChangedListener(twTransferNote);
        vSend.setOnClickListener(oclSend);
    }

    private void initialize(View view) {
        manager = (Employee) getArguments().getSerializable("manager");
        vSend = view.findViewById(R.id.Button_SendRequest);
        vTransferNote = view.findViewById(R.id.TextInput_TransferNote);
        vProjectsReference = view.findViewById(R.id.TextInput_ProjectReferences);
        vEmployeesId = view.findViewById(R.id.TextInput_EmployeeId);
        vTransferNoteLayout = view.findViewById(R.id.textInputLayout_TransferNote);
        vEmployeesIdLayout = view.findViewById(R.id.textInputLayout_EmployeeId);
        vProjectReferenceLayout = view.findViewById(R.id.textInputLayout_ProjectReferences);
        projectsRef = new ArrayList<>();
        refAdapter = new ArrayAdapter<>(getActivity(), R.layout.item_dropdown, projectsRef);
        views = new ArrayList<>();
        views.add(new Pair<>(vProjectReferenceLayout, vProjectsReference));
        views.add(new Pair<>(vEmployeesIdLayout, vEmployeesId));
        views.add(new Pair<>(vTransferNoteLayout, vTransferNote));
        idAdapter = new ArrayAdapter<>(getActivity(), R.layout.item_dropdown, employeesId);
        getProject();
    }

    private Task<Void> sendRequest(EmployeeOverview employee) {
        String transferId = db.collection("TransferRequests").document().getId();
        TransferRequests request = new TransferRequests();
        request.setTransferId(transferId);
        request.setEmployee(employee);
        request.setNewProjectId(thisProject.getId());
        request.setNewProjectName(thisProject.getName());
        request.setNewProjectReference(thisProject.getReference());
        request.setOldProjectId(chosenProject.getId());
        request.setOldProjectName(chosenProject.getName());
        request.setOldProjectReference(chosenProject.getReference());
        request.setNote(vTransferNote.getText().toString());
        return db.collection("TransferRequests").document(transferId).set(request);
    }

    private void getProject() {
        db.collection("projects").document(manager.getProjectID()).get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists())
                return;
            thisProject = documentSnapshot.toObject(Project.class);
            getAllProjects(thisProject.getId());
        });
    }

    private void freezeViews(boolean freeze) {

        vProjectsReference.setText(freeze ? "No Available Projects" : null);
        vEmployeesId.setText(freeze ? "No Available Employee" : null);
        vProjectReferenceLayout.setEnabled(!freeze);
        vTransferNoteLayout.setEnabled(!freeze);
        vEmployeesIdLayout.setEnabled(!freeze);
        vSend.setEnabled(!freeze);
    }

    private void getAllProjects(String projectId) {
        db.collection("projects").whereNotEqualTo("id", projectId).addSnapshotListener((queryDocumentSnapshots, error) -> {

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
            vProjectsReference.setText(String.format("IGEC%s | %s", chosenProject.getReference(), chosenProject.getName()));
            vProjectsReference.setAdapter(refAdapter);


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
        vTransferNote.setText(null);
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
            if (!vTransferNote.getText().toString().trim().isEmpty())
                vTransferNoteLayout.setError(null);
            hideError(vTransferNoteLayout);
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
                if (vEmployeesId.getText().toString().contains(emp.getId())) {
                    selectedEmployee = emp;
                    break;
                }
            }
            vEmployeesIdLayout.setErrorEnabled(vEmployeesId.getText().toString().isEmpty());
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
                if (vProjectsReference.getText().toString().contains(project.getReference()) &&
                        vProjectsReference.getText().toString().contains(project.getName())) {
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
                    vEmployeesId.setText(isThereEmployees ? null : "No Available Employee");
                    vEmployeesId.setEnabled(isThereEmployees);
                    vSend.setEnabled(isThereEmployees);
                    idAdapter = new ArrayAdapter<>(getActivity(), R.layout.item_dropdown, employeesId);
                    vEmployeesId.setAdapter(idAdapter);
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
                        Toast.makeText(getActivity(), "Request Sent", Toast.LENGTH_SHORT).show();
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
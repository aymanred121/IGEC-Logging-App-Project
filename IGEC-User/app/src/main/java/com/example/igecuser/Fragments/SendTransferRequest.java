package com.example.igecuser.Fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.example.igecuser.R;
import com.example.igecuser.fireBase.Employee;
import com.example.igecuser.fireBase.EmployeeOverview;
import com.example.igecuser.fireBase.Project;
import com.example.igecuser.fireBase.TransferRequests;
import com.google.android.gms.common.util.ArrayUtils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class SendTransferRequest extends Fragment {

    private final Employee manager;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final ArrayList<Project> projects = new ArrayList<>();
    private Project oldProject, newProject;
    private AutoCompleteTextView vProjectsReference, vEmployeesId;
    private TextInputEditText vTransferNote;
    private TextInputLayout vTransferNoteLayout, vProjectReferenceLayout, vEmployeesIdLayout;
    private ArrayList<Pair<TextInputLayout, EditText>> views;
    private MaterialButton vSend;
    private EmployeeOverview selectedEmployee;
    ArrayList<String> projectsRef;
    private ArrayAdapter<String> RefAdapter;


    public SendTransferRequest(Employee manager) {
        this.manager = manager;
    }

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
        vSend = view.findViewById(R.id.Button_SendRequest);
        vTransferNote = view.findViewById(R.id.TextInput_TransferNote);
        vProjectsReference = view.findViewById(R.id.TextInput_ProjectReferences);
        vEmployeesId = view.findViewById(R.id.TextInput_EmployeeId);
        vTransferNoteLayout = view.findViewById(R.id.textInputLayout_TransferNote);
        vEmployeesIdLayout = view.findViewById(R.id.textInputLayout_EmployeeId);
        vProjectReferenceLayout = view.findViewById(R.id.textInputLayout_ProjectReferences);
        projectsRef = new ArrayList<>();
        RefAdapter = new ArrayAdapter<>(getActivity(), R.layout.item_dropdown, projectsRef);
        views = new ArrayList<>();
        views.add(new Pair<>(vProjectReferenceLayout, vProjectsReference));
        views.add(new Pair<>(vEmployeesIdLayout, vEmployeesId));
        views.add(new Pair<>(vTransferNoteLayout, vTransferNote));
        getProject();


    }

    private Task<Void> sendRequest(EmployeeOverview employee) {
        //?bug todo all requests goes to one document
        String transferId = db.collection("TransferRequests").getId();
        TransferRequests request = new TransferRequests();
        request.setTransferId(transferId);
        request.setEmployee(employee);
        request.setNewProjectId(newProject.getId());
        request.setNewProjectName(newProject.getName());
        request.setNewProjectReference(newProject.getReference());
        request.setOldProjectId(oldProject.getId());
        request.setOldProjectName(oldProject.getName());
        request.setOldProjectReference(oldProject.getReference());
        request.setNote(vTransferNote.getText().toString());
        return db.collection("TransferRequests").document(transferId).set(request);
    }

    private void getAllEmployees() {
        ArrayList<String> EmployeesId = new ArrayList<>();
        for (EmployeeOverview emp : oldProject.getEmployees())
            if (!emp.getId().equals(manager.getId())) {
                if (selectedEmployee == null)
                    selectedEmployee = emp;
                EmployeesId.add(emp.getId() + " | " + emp.getFirstName() + " " + emp.getLastName());
            }
        ArrayAdapter<String> IdAdapter = new ArrayAdapter<>(getActivity(), R.layout.item_dropdown, EmployeesId);
        vEmployeesId.setText(String.format("%s | %s %s", selectedEmployee.getId(), selectedEmployee.getFirstName(), selectedEmployee.getLastName()));
        vEmployeesId.setAdapter(IdAdapter);
    }

    private void getProject() {
        db.collection("projects").document(manager.getProjectID()).get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists())
                return;
            oldProject = documentSnapshot.toObject(Project.class);
            getAllProjects(oldProject.getId());
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
            freezeViews(false);
            projectsRef.clear();
            projects.clear();
            projects.addAll((queryDocumentSnapshots.toObjects(Project.class)));
            for (Project project : projects)
                if (!project.getId().equals(oldProject.getId()))
                    projectsRef.add("IGEC" + project.getReference() + " | " + project.getName());

            newProject = projects.get(0);
            vProjectsReference.setText(String.format("IGEC%s | %s", newProject.getReference(), newProject.getName()));
            vProjectsReference.setAdapter(RefAdapter);
            getAllEmployees();
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
            for (EmployeeOverview emp : oldProject.getEmployees()) {
                if (vEmployeesId.getText().toString().contains(emp.getId())) {
                    selectedEmployee = emp;
                    break;
                }
            }
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
                if (vProjectsReference.getText().toString().contains(project.getReference())) {
                    newProject = project;
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
                        Toast.makeText(getActivity(), "Sent", Toast.LENGTH_SHORT).show();
                        clearInput();
                    }
                });
            }
        }
    };

}
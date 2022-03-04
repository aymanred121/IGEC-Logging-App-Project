package com.example.igec_admin;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.igec_admin.fireBase.EmployeeOverview;
import com.example.igec_admin.fireBase.Project;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Summary extends Fragment {


    // Views
    RecyclerView recyclerView;
    // Vars
    private ProjectAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    ArrayList<Project> projects = new ArrayList();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference projectRef = db.collection("projects");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_summary, container, false);
        Initialize(view);

        return view;
    }

    // Functions
    private void Initialize(View view) {
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new ProjectAdapter(projects);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        getProjects();

    }

    void getProjects() {
        projectRef.addSnapshotListener((queryDocumentSnapshots, e) -> {
            projects.clear();
            for(DocumentSnapshot d : queryDocumentSnapshots){
                projects.add(d.toObject(Project.class));
            }
            adapter.setProjectsList(projects);
            adapter.notifyDataSetChanged();
        });
    }



}
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

import com.example.igec_admin.Adatpers.MachineAdapter;
import com.example.igec_admin.Adatpers.ProjectAdapter;
import com.example.igec_admin.R;
import com.example.igec_admin.fireBase.Project;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ProjectsFragment extends Fragment {
    //vars
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference projectRef = db.collection("projects");
    ArrayList<Project>projects = new ArrayList();
    ProjectAdapter adapter;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_projects,container,false);
        initialize(view);
        return view;
    }
    void initialize(View view){
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
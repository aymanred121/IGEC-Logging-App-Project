package com.igec.admin.fragments;

import static com.igec.common.CONSTANTS.PROJECT_COL;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.igec.admin.adapters.ProjectAdapter;
import com.igec.admin.databinding.FragmentProjectsBinding;
import com.igec.admin.dialogs.ProjectFragmentDialog;
import com.igec.common.firebase.Project;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;

public class ProjectsFragment extends Fragment {
    //vars
    ArrayList<Project> projects;
    ProjectAdapter adapter;
    RecyclerView.LayoutManager layoutManager;
    private FragmentProjectsBinding binding;
    private boolean opened = false;

    public void setOpened(boolean opened) {
        this.opened = opened;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProjectsBinding.inflate(inflater, container, false);
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
        adapter.setOnItemClickListener(itclProjectAdapter);
    }

    void initialize() {
        projects = new ArrayList<>();
        binding.recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new ProjectAdapter(projects);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);
        getProjects();
    }

    @SuppressLint("NotifyDataSetChanged")
    void getProjects() {
        PROJECT_COL.addSnapshotListener((queryDocumentSnapshots, e) -> {
            projects.clear();
            assert queryDocumentSnapshots != null;
            for (DocumentSnapshot d : queryDocumentSnapshots) {
                projects.add(d.toObject(Project.class));
            }
            adapter.setProjectsList(projects);
            adapter.notifyDataSetChanged();
        });
    }


    private final ProjectAdapter.OnItemClickListener itclProjectAdapter = new ProjectAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
            if (opened) return;
            opened = true;
            ProjectFragmentDialog projectFragmentDialog = new ProjectFragmentDialog(adapter.getProjectsList().get(position));
            projectFragmentDialog.show(getParentFragmentManager(), "");
        }
    };
}
package com.igec.admin.Dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.igec.admin.Adapters.EmployeeAdapter;
import com.igec.admin.R;
import com.igec.common.firebase.Project;
import com.google.firebase.firestore.FirebaseFirestore;

public class EmployeeFragmentDialog extends DialogFragment {

    private Project project;
    private EmployeeAdapter adapter;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private String year, month;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();


    public EmployeeFragmentDialog(Project project, String year, String month) {
        this.project = project;
        this.year = year;
        this.month = month;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullscreenDialogTheme);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {


        Dialog dialog = super.onCreateDialog(savedInstanceState);
        Window window = dialog.getWindow();

        if (window != null) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        }

        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_users, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize(view);
        adapter.setOnItemClickListener(oclEmployee);
    }

    private void initialize(View view) {
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new EmployeeAdapter(project.getEmployees(), false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
//        getEmployees();

    }

    private EmployeeAdapter.OnItemClickListener oclEmployee = new EmployeeAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position) {


        }

        @Override
        public void onCheckboxClick(int position) {

        }
    };
}

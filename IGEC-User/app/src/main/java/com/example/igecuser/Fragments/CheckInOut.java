package com.example.igecuser.Fragments;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.igecuser.MachineAdapter;
import com.example.igecuser.R;
import com.example.igecuser.VacationAdapter;
import com.example.igecuser.fireBase.Machine;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class CheckInOut extends Fragment {

    TextView vGreeting;
    MaterialButton vCheckInOut;
    FloatingActionButton vAddMachine;


    private RecyclerView recyclerView;
    private static MachineAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Machine> machines = new ArrayList<>();
    boolean isIn = false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_check_in_out, container, false);
        Initialize(view);

        vCheckInOut.setOnClickListener(oclCheckInOut);
        adapter.setOnItemClickListener(iclMachine);
        return view;
    }
    private void Initialize(View view) {
       vGreeting = view.findViewById(R.id.TextView_Greeting);
       vCheckInOut = view.findViewById(R.id.Button_CheckInOut);
       vAddMachine = view.findViewById(R.id.Button_AddMachine);
       recyclerView = view.findViewById(R.id.recyclerview);

        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new MachineAdapter(machines);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        //loadVacations();
    }

    View.OnClickListener oclCheckInOut = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
                isIn = !isIn;
                vCheckInOut.setBackgroundColor((isIn)?Color.rgb(153, 0, 0): Color.rgb(0,153,0));
                vCheckInOut.setText(isIn ? "Out" : "In");
                // TODO
        }
    };
    MachineAdapter.OnItemClickListener iclMachine = new MachineAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position) {

        }

        @Override
        public void onCheckInOutClick(int position) {

        }
    };
}
package com.example.igec_admin;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class Add_Machine extends Fragment {


    // Views
    TextInputLayout vMachineIDLayout;
    TextInputEditText vMachineID;
    ImageView vMachineIDIMG;
    MaterialButton vRegister;
    // Vars

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       View view = inflater.inflate(R.layout.fragment_add_machine, container, false);
       Initialize(view);

        vMachineIDLayout.setEndIconOnClickListener(oclMachineID);
        vRegister.setOnClickListener(oclRegister);
        return view;
    }
    // Functions
    private void Initialize(View view) {

        vMachineID = view.findViewById(R.id.TextInput_ManagerID);
        vMachineIDLayout = view.findViewById(R.id.textInputLayout_MachineID);
        vMachineIDIMG = view.findViewById(R.id.ImageView_MachineIDIMG);
        vRegister = view.findViewById(R.id.button_register);
    }

    // Listeners
    View.OnClickListener oclRegister = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(getActivity(), "Registered", Toast.LENGTH_SHORT).show();
        }
    };
    View.OnClickListener oclMachineID = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(getActivity(), "Generated", Toast.LENGTH_SHORT).show();
        }
    };



}
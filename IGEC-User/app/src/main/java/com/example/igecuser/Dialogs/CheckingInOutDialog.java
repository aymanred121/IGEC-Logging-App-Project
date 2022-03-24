package com.example.igecuser.Dialogs;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.igecuser.R;
import com.google.android.material.button.MaterialButton;


public class CheckingInOutDialog extends DialogFragment {


    //Views
    private MaterialButton vNo, vYes;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_checking_in_out_dialog, container, false);
        initialize(v);

        vYes.setOnClickListener(v1 -> {
            Bundle result = new Bundle();
            result.putString("response", "Yes");
            getParentFragmentManager().setFragmentResult("employee", result);
            dismiss();
        });
        vNo.setOnClickListener(v1 -> {
            Bundle result = new Bundle();
            result.putString("response", "No");
            getParentFragmentManager().setFragmentResult("employee", result);
            dismiss();
        });
        return v;
    }

    private void initialize(View v) {
        vNo = v.findViewById(R.id.Button_No);
        vYes = v.findViewById(R.id.Button_Yes);
    }
}
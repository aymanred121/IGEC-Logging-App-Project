package com.example.igecuser.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.igecuser.R;
import com.example.igecuser.fireBase.Employee;

public class SendTransferRequest extends Fragment {

    //TODO to be used in the transfer request sending process
    private Employee manager;
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
    }

    private void initialize(View view)
    {

    }
    //TODO: write the code to send Transfer Requests with Parameters to be ready for integration
}
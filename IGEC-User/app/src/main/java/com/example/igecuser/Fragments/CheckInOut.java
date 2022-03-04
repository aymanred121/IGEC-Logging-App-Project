package com.example.igecuser.Fragments;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.igecuser.R;
import com.google.android.material.button.MaterialButton;

public class CheckInOut extends Fragment {

    TextView vGreeting;
    MaterialButton vCheckInOut;
    boolean isIn = false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_check_in_out, container, false);
        Initialize(view);

        vCheckInOut.setOnClickListener(oclCheckInOut);
        return view;
    }
    private void Initialize(View view) {
       vGreeting = view.findViewById(R.id.TextView_Greeting);
       vCheckInOut = view.findViewById(R.id.Button_CheckInOut);
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
}
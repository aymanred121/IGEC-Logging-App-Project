package com.igec.user.Dialogs;

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

import com.budiyev.android.codescanner.CodeScanner;
import com.igec.user.R;
import com.igec.user.databinding.DialogMachineCheckInOutBinding;

public class MachineCheckInOutDialog extends DialogFragment {
    private CodeScanner mCodeScanner;
    private final boolean isItAUser;

    public MachineCheckInOutDialog(boolean isItAUser) {
        this.isItAUser = isItAUser;
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullscreenDialogTheme);
    }
    private DialogMachineCheckInOutBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding =  DialogMachineCheckInOutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mCodeScanner = new CodeScanner(getActivity(), binding.scannerView);
        mCodeScanner.setDecodeCallback(result -> getActivity().runOnUiThread(() -> {
            Bundle res = new Bundle();
            res.putString("machineID", result.getText());
            res.putBoolean("isItAUser", isItAUser);
            getParentFragmentManager().setFragmentResult("machine", res);
            dismiss();
        }));
        binding.scannerView.setOnClickListener(v -> mCodeScanner.startPreview());
    }

    @Override
    public void onResume() {
        super.onResume();
        mCodeScanner.startPreview();
    }

    @Override
    public void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }
}
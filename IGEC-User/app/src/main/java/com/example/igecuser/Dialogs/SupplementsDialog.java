package com.example.igecuser.Dialogs;

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

import com.example.igecuser.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class SupplementsDialog extends DialogFragment {


    //Views
    private TextInputEditText vSupplementsNote;
    private MaterialButton vDone;
    private boolean isItAUser;

    private final View.OnClickListener oclDone = v -> {
        if (!isItAUser) {
            ClientInfoDialog clientInfoDialog = new ClientInfoDialog(this);
            clientInfoDialog.show(getParentFragmentManager(), "");
        } else {

            Bundle bundle = new Bundle();
            bundle.putString("supplementState", vSupplementsNote.getText().toString());
            getParentFragmentManager().setFragmentResult("supplements", bundle);
            dismiss();
        }

    };

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


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_supplements, container, false);
        initialize(view);


        vDone.setOnClickListener(oclDone);
        return view;
    }

    // Functions
    private void initialize(View view) {
        vSupplementsNote = view.findViewById(R.id.TextInput_SupplementsNote);
        vDone = view.findViewById(R.id.Button_Done);
    }

    //TODO: modify this to carry machine data for registration
    public SupplementsDialog(boolean isItAUser) {
        this.isItAUser = isItAUser;
    }

}

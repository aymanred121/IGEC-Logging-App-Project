package com.example.igecuser.Fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.igecuser.R;
import com.example.igecuser.fireBase.Employee;
import com.example.igecuser.fireBase.VacationRequest;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class SendVacationRequestFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

    //Views
    private TextInputEditText vVacationDate, vVacationNote, vVacationDays;
    private TextInputLayout vVacationDateLayout, vVacationDaysLayout, vVacationNoteLayout;
    private ArrayList<Pair<TextInputLayout, TextInputEditText>> views;
    private MaterialButton vSendRequest;
    private DatePickerDialog dpd;
    //Vars
    private int remainingDays, daysAfterVacationIsTaken;
    private long days, startDate;
    private Employee currEmployee;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private VacationRequest vacationRequest;

    //Overrides
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_vacation_request, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Initialize(view);

        vVacationDateLayout.setEndIconOnClickListener(oclVacationDate);
        vVacationDateLayout.setErrorIconOnClickListener(oclVacationDate);
        vSendRequest.setOnClickListener(oclSendRequest);
        vVacationDays.addTextChangedListener(twVacationDays);
        vVacationDate.addTextChangedListener(twVacationDate);
        vVacationNote.addTextChangedListener(twVacationNote);
    }

    //Functions
    public SendVacationRequestFragment(Employee currEmployee) {
        this.currEmployee = currEmployee;
    }

    @SuppressLint("DefaultLocale")
    private void Initialize(View view) {
        vVacationDate = view.findViewById(R.id.TextInput_VacationDate);
        vVacationNote = view.findViewById(R.id.TextInput_VacationNote);
        vVacationDays = view.findViewById(R.id.TextInput_VacationDays);
        vVacationDateLayout = view.findViewById(R.id.textInputLayout_VacationDate);
        vVacationDaysLayout = view.findViewById(R.id.textInputLayout_VacationDays);
        vVacationNoteLayout = view.findViewById(R.id.textInputLayout_VacationNote);
        vSendRequest = view.findViewById(R.id.Button_SendRequest);

        views = new ArrayList<>();
        views.add(new Pair<>(vVacationDateLayout, vVacationDate));
        views.add(new Pair<>(vVacationDaysLayout, vVacationDays));
        views.add(new Pair<>(vVacationNoteLayout, vVacationNote));

        CalendarConstraints.Builder builder = new CalendarConstraints.Builder();
        builder.setValidator(DateValidatorPointForward.now());
        remainingDays = currEmployee.getTotalNumberOfVacationDays();
        vVacationDaysLayout.setHelperText(String.format("%d days Remaining", remainingDays));
        datePickerSetup();


    }

    private void datePickerSetup() {
        Calendar now = Calendar.getInstance();
        dpd = DatePickerDialog.newInstance(
                this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );
        dpd.setTitle("Vacation Date");
        dpd.setOkText("Set");
        dpd.setOkColor(getResources().getColor(R.color.green));
        dpd.setCancelColor(getResources().getColor(R.color.red));
        dpd.setMinDate(Calendar.getInstance());
        Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 2);
        nextYear.add(Calendar.MONTH, -nextYear.get(Calendar.MONTH));
        nextYear.add(Calendar.DAY_OF_YEAR, -nextYear.get(Calendar.DAY_OF_YEAR));
        dpd.setMaxDate(nextYear);
        // Calendar friday;
        Calendar friday;
        List<Calendar> weekends = new ArrayList<>();
        int weeks = 104;
        for (int i = 0; i < (weeks * 7); i += 7) {
            friday = Calendar.getInstance();
            friday.add(Calendar.DAY_OF_YEAR, (Calendar.FRIDAY - friday.get(Calendar.DAY_OF_WEEK) + i));
            weekends.add(friday);
        }
        Calendar[] disabledDays = weekends.toArray(new Calendar[weekends.size()]);
        dpd.setDisabledDays(disabledDays);
    }

    private void uploadVacationRequest() {
        String vacationID = db.collection("Vacation").document().getId().substring(0, 5);
        db.collection("employees")
                .document(currEmployee.getManagerID())
                .addSnapshotListener((value, error) -> {
                    days = ((long) Integer.parseInt(vVacationDays.getText().toString()) * 24 * 3600 * 1000) + startDate;
                    vacationRequest = new VacationRequest(
                            new Date(startDate),
                            new Date(days),
                            (new Date()),
                            value.toObject(Employee.class),
                            currEmployee,
                            vVacationNote.getText().toString()
                    );
                    vacationRequest.setId(vacationID);
                    db.collection("Vacation").document(vacationID).set(vacationRequest).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            clearInputs();
                        }
                    });

                });
    }

    private void clearInputs() {
        vVacationDate.setText(null);
        vVacationDays.setText(null);
        vVacationNote.setText(null);
    }

    private boolean generateError() {

        for (Pair<TextInputLayout, TextInputEditText> view : views) {
            if (view.second.getText().toString().trim().isEmpty()) {
                if (view.first == vVacationDateLayout)
                    view.first.setErrorIconDrawable(R.drawable.ic_baseline_calendar_month_24);

                view.first.setError("Missing");
                return true;
            }
            if (view.first.getError() != null) {
                return false;
            }
        }
        return false;
    }

    private void hideError(TextInputLayout textInputLayout) {
        textInputLayout.setErrorEnabled(textInputLayout.getError() != null);

    }


    private boolean validateInputs() {
        return !generateError();
    }

    private String convertDateToString(long selection) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(selection);
        return simpleDateFormat.format(calendar.getTime());
    }

    // Listeners
    private TextWatcher twVacationDays = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @SuppressLint("DefaultLocale")
        @Override
        public void afterTextChanged(Editable editable) {
            daysAfterVacationIsTaken = vVacationDays.getText().toString().trim().equals("") ? remainingDays : remainingDays - Integer.parseInt(vVacationDays.getText().toString());
            if (daysAfterVacationIsTaken < 0) {
                vVacationDaysLayout.setError("Exceeds remaining");
            } else if (daysAfterVacationIsTaken == remainingDays && !vVacationDays.getText().toString().trim().isEmpty()) {
                vVacationDaysLayout.setError("Invalid Value");
            } else {
                vVacationDaysLayout.setHelperText(String.format("%d days Remaining", daysAfterVacationIsTaken));
                vVacationDaysLayout.setError(null);
            }

            hideError(vVacationDaysLayout);
        }
    };
    private TextWatcher twVacationDate = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (!vVacationDate.getText().toString().trim().isEmpty())
                vVacationDateLayout.setError(null);

            hideError(vVacationDateLayout);
        }
    };
    private TextWatcher twVacationNote = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (!vVacationNote.getText().toString().trim().isEmpty())
                vVacationNoteLayout.setError(null);

            hideError(vVacationNoteLayout);
        }
    };
    private View.OnClickListener oclSendRequest = v -> {
        if (validateInputs()) {
            if(daysAfterVacationIsTaken < 0)
            {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
                builder.setTitle("days exceeded by "+daysAfterVacationIsTaken*-1+" ...")
                        .setMessage("there will be "+daysAfterVacationIsTaken*-1+ " will be considered as Unpaid")
                        .setCancelable(true)
                        .setPositiveButton("ok, send" , (dialogInterface, i) -> {
                            uploadVacationRequest();
                            dialogInterface.dismiss();
                        })
                        .setNegativeButton("No" ,  (dialogInterface, i) -> {
                            dialogInterface.dismiss();
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
            else{
                uploadVacationRequest();
            }

        }

    };
    private View.OnClickListener oclVacationDate = v -> {
        dpd.show(getParentFragmentManager(), "DATE_PICKER");
    };
    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        Calendar vacation = Calendar.getInstance();
        vacation.set(year,monthOfYear,dayOfMonth);
        vVacationDate.setText(convertDateToString(vacation.getTime().getTime()));
        startDate = vacation.getTime().getTime();
    }
}
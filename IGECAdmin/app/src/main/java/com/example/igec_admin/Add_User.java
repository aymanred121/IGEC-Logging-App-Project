package com.example.igec_admin;

import static android.content.ContentValues.TAG;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Objects;

public class Add_User extends Fragment {


    FirebaseFirestore db = FirebaseFirestore.getInstance();
    MaterialButton register;
    TextInputEditText dateFormat;
    TextInputEditText FirstName , LastName , Title , Salary , SSN ,Area , City , Street ;
    DatePickerDialog.OnDateSetListener onDateSetListener;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_user, container,false);
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        register = view.findViewById(R.id.button_register);
        FirstName = view.findViewById(R.id.TextInput_FirstName);
        LastName = view.findViewById(R.id.TextInput_SecondName);
        Title = view.findViewById(R.id.TextInput_Title);
        Salary = view.findViewById(R.id.TextInput_Salary);
        SSN = view.findViewById(R.id.TextInput_SNN);
        Area = view.findViewById(R.id.TextInput_Area);
        City = view.findViewById(R.id.TextInput_City);
        Street = view.findViewById(R.id.TextInput_Street);
        dateFormat = view.findViewById(R.id.TextInput_HireDate);

        dateFormat.setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus)
            {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), android.R.style.Theme_Holo_Dialog_MinWidth, onDateSetListener,year,month,day);
                datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                datePickerDialog.show();
            }
        });
        dateFormat.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), android.R.style.Theme_Holo_Dialog_MinWidth, onDateSetListener,year,month,day);
            datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            datePickerDialog.show();
        });
        register.setOnClickListener(RegisterListener);

        onDateSetListener = (view1, year1, month1, dayOfMonth) -> {
            month1 = month1 +1;
            String date = dayOfMonth +"/" + month1 + "/" + year1;
            dateFormat.setText(date);
        };
        return view;
    }

    View.OnClickListener RegisterListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(getActivity(), "Hi", Toast.LENGTH_SHORT).show();
            if(!TextUtils.isEmpty(Objects.requireNonNull(FirstName.getText()).toString()) && !TextUtils.isEmpty(Objects.requireNonNull(LastName.getText()).toString())) {

                AddRecord(db, "3");
            }
            else
                deleteRecord(db, "3");

        }
    };
void AddRecord (FirebaseFirestore db , String path )
{
    user newUser = new user(Objects.requireNonNull(FirstName.getText()).toString() ,
            Objects.requireNonNull(LastName.getText()).toString() , Objects.requireNonNull(Title.getText()).toString(), Objects.requireNonNull(Area.getText()).toString()
            , Objects.requireNonNull(City.getText()).toString() , Objects.requireNonNull(Street.getText()).toString(),2,
            Integer.parseInt(Objects.requireNonNull(Salary.getText()).toString()),Integer.parseInt(Objects.requireNonNull(SSN.getText()).toString()));
    db.collection("users").document(path)
            .set(newUser)
            .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully added!"))
            .addOnFailureListener(e -> Log.w(TAG, "Error adding document", e));

}
void deleteRecord(FirebaseFirestore db , String path )
{
    db.collection("users").document(path)
            .delete()
            .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully deleted!"))
            .addOnFailureListener(e -> Log.w(TAG, "Error deleting document", e));
}

}
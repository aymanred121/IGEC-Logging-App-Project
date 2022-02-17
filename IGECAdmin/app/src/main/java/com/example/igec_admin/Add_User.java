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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.igec_admin.fireBase.Employees;
import com.example.igec_admin.fireBase.operation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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

                addRecord(operation.employee);
            }
            else
                deleteRecord(db, "123");

        }

    };
void addRecord(operation op)
{
    switch (op){
        case machine:
            addMachine();
            break;
        case project:
            addProject();
            break;
        case employee:
            addEmployee();
            break;
    }

}
void addProject(){
    //TODO add project code
}
void addMachine(){
    //TODO add machine code


}
void addEmployee(){
    //TODO add account collection
    Employees newUser = new Employees((FirstName.getText()).toString(),
            (LastName.getText()).toString(), (Title.getText()).toString(), (Area.getText()).toString()
            , (City.getText()).toString() , (Street.getText()).toString(),"2",
            Double.parseDouble(Salary.getText().toString()),((SSN.getText()).toString()), convertStringDate(dateFormat.getText().toString()));
    db.collection("employees")
            .add(newUser)
            .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully added!"))
            .addOnFailureListener(e -> Log.w(TAG, "Error adding document", e));

}
void deleteRecord(FirebaseFirestore db,String ID)
{
    db.collection("employees").whereEqualTo("id",ID).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
        @Override
        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
            for(DocumentSnapshot ds : queryDocumentSnapshots){
                db.collection("employees").document(ds.getId()).delete();

            }
        }
    });

}
void updateRecord(String Collection,String id,String field,Object value){
    Task updateDB = db.collection(Collection).document(id).update(field,value).addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            Toast.makeText(getActivity(), "Error while updating record", Toast.LENGTH_SHORT).show();
        }
    });
}
Employees getEmployee(String id){
    /**
     * This is a dummy solution
     * */
   Task dbTask= db.collection("employees").document(id).get();
   while (true){
       if(dbTask.isComplete()){
           DocumentSnapshot documentSnapshot =(DocumentSnapshot) dbTask.getResult();
           if(documentSnapshot.exists())
           return documentSnapshot.toObject(Employees.class);
           else
               return null;
       }
   }
}




Date convertStringDate(String sDate){
    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
    try {
        Date date = format.parse(sDate);
       return date;
    } catch (ParseException e) {
        e.printStackTrace();
    }
    return null;
}

}
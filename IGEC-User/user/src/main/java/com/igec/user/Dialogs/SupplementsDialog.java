package com.igec.user.Dialogs;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.igec.user.Adapters.SupplementsAdapter;
import com.igec.user.R;
import com.igec.user.fireBase.Employee;
import com.igec.user.fireBase.Machine;
import com.igec.user.fireBase.Supplement;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class SupplementsDialog extends DialogFragment {


    private Machine machine;
    private Employee employee;
    private ArrayList<Supplement> supplements;
    private MaterialButton vDone;
    private boolean isItAUser;
    private Supplement machineCover = new Supplement();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    //Views
    private CircularProgressIndicator vCircularProgressIndicator;
    private TextInputEditText vComment;
    private CircleImageView vMachineCover;
    private Animation show, hide;
    private RecyclerView recyclerView;
    private SupplementsAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {


        Dialog dialog = super.onCreateDialog(savedInstanceState);
        Window window = dialog.getWindow();

        if (window != null) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
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
        return inflater.inflate(R.layout.dialog_supplements, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize(view);
        vDone.setOnClickListener(oclDone);
    }

    public SupplementsDialog(boolean isItAUser, Machine machine, Employee employee) {
        this.isItAUser = isItAUser;
        this.machine = machine;
        this.employee = employee;
    }

    // Functions
    private void initialize(View view) {
        supplements = new ArrayList<>();
        vMachineCover = view.findViewById(R.id.ImageView_MachineIMG);
        show = AnimationUtils.loadAnimation(getActivity(), R.anim.show);
        hide = AnimationUtils.loadAnimation(getActivity(), R.anim.hide);
        vCircularProgressIndicator = view.findViewById(R.id.progress_bar);
        vComment = view.findViewById(R.id.TextInput_Comment);
        vDone = view.findViewById(R.id.Button_Done);
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        layoutManager = new GridLayoutManager(getActivity(),3);
        adapter = new SupplementsAdapter(supplements);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        if (machine.getSupplementsNames().size() != 0) {
            getAllSupplements();
            vCircularProgressIndicator.setVisibility(View.VISIBLE);
            vCircularProgressIndicator.startAnimation(show);
            recyclerView.startAnimation(hide);
        } else {
            vCircularProgressIndicator.startAnimation(hide);
            recyclerView.startAnimation(show);
        }
    }

    private void getMachineCover() {
        String cover = "cover";
        StorageReference ref = FirebaseStorage.getInstance().getReference().child("/imgs/" + machine.getId() + String.format("/%s.jpg", cover));
        try {
            final File localFile = File.createTempFile(cover, "jpg");
            ref.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                vMachineCover.setImageBitmap(bitmap);
                machineCover.setPhoto(bitmap);
                machineCover.setName("cover");
            }).addOnFailureListener(e -> {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void getAllSupplements() {
        StorageReference ref;
        final int[] progress = new int[1];
        for (String name : machine.getSupplementsNames()) {
            ref = FirebaseStorage.getInstance().getReference().child("/imgs/" + machine.getId() + String.format("/%s.jpg", name));
            try {
                final File localFile = File.createTempFile(name, "jpg");
                ref.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                        supplements.add(new Supplement(name, bitmap));
                        progress[0]++;
                        if (progress[0] == machine.getSupplementsNames().size()) {
                            getMachineCover();
                            vCircularProgressIndicator.startAnimation(hide);
                            recyclerView.startAnimation(show);
                            vDone.setEnabled(true);
                            adapter.notifyDataSetChanged();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private final View.OnClickListener oclDone = v -> {
        Bundle bundle = new Bundle();
        bundle.putString("supplementState", vComment.getText().toString());
        bundle.putBoolean("isItAUser", isItAUser);
        getParentFragmentManager().setFragmentResult("supplements", bundle);
        dismiss();


    };


}

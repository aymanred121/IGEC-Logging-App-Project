package com.example.igec_admin.Dialogs;

import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.igec_admin.Adatpers.SupplementAdapter;
import com.example.igec_admin.R;
import com.example.igec_admin.fireBase.Machine;
import com.example.igec_admin.fireBase.Supplement;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class AddSupplementsDialog extends DialogFragment {

    private FloatingActionButton vAddPhoto, vDone;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private ArrayList<Supplement> supplements;
    private SupplementAdapter adapter;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private CircularProgressIndicator vCircularProgressIndicator;
    private Machine machine;
    private Animation  show, hide;


    // for machine dialog
    public AddSupplementsDialog(Machine machine) {
        this.machine = machine;
    }

    // for add machine dialog
    public AddSupplementsDialog(ArrayList<Supplement> supplements) {
        this.supplements = supplements;
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
        getParentFragmentManager().setFragmentResultListener("addSupplement", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                // We use a String here, but any type that can be put in a Bundle is supported
                supplements.add((Supplement) bundle.getSerializable("supplement"));
                // Do something with the result
                Toast.makeText(getActivity(), supplements.get(supplements.size() - 1).getName(), Toast.LENGTH_SHORT).show();
                adapter.notifyDataSetChanged();
            }
        });

        getParentFragmentManager().setFragmentResultListener("editSupplement", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                // We use a String here, but any type that can be put in a Bundle is supported
                int position = bundle.getInt("position");
                Supplement supplement = (Supplement) bundle.getSerializable("supplement");

                supplements.get(position).setName(supplement.getName());
                supplements.get(position).setPhoto(supplement.getPhoto());
                // Do something with the result
                Toast.makeText(getActivity(), supplements.get(supplements.size() - 1).getName(), Toast.LENGTH_SHORT).show();
                adapter.notifyItemChanged(position);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_supplements_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize(view);
        vAddPhoto.setOnClickListener(oclAddPhoto);
        vDone.setOnClickListener(oclDone);
        adapter.setOnItemClickListener(oclItemClickListener);
    }

    private void initialize(View view) {
        if (supplements == null)
            supplements = new ArrayList<>();
        vCircularProgressIndicator = view.findViewById(R.id.progress_bar);
        vAddPhoto = view.findViewById(R.id.Button_AddPhoto);
        vDone = view.findViewById(R.id.Button_Done);
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new SupplementAdapter(supplements);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle bundle = result.getData().getExtras();
                    Bitmap bitmap = (Bitmap) bundle.get("data");
                    Supplement supplement = new Supplement();
                    supplement.setName("");
                    supplement.setPhoto(bitmap);
                    SupplementInfoDialog supplementInfoDialog = new SupplementInfoDialog(-1, supplement, supplements);
                    supplementInfoDialog.show(getParentFragmentManager(), "");
                }
            }
        });
        show = AnimationUtils.loadAnimation(getActivity(), R.anim.show);
        hide = AnimationUtils.loadAnimation(getActivity(), R.anim.hide);
        if (machine != null) {
            getAllSupplements();
            vCircularProgressIndicator.setVisibility(View.VISIBLE);
            vCircularProgressIndicator.startAnimation(show);
            recyclerView.startAnimation(hide);
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
                            vCircularProgressIndicator.startAnimation(hide);
                            recyclerView.startAnimation(show);
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

    private final View.OnClickListener oclDone = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Bundle result = new Bundle();
            result.putSerializable("supplements", supplements);
            getParentFragmentManager().setFragmentResult("supplements", result);
            dismiss();
        }
    };
    private final View.OnClickListener oclAddPhoto = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            activityResultLauncher.launch(takePicture);
//            if (takePicture.resolveActivity(getContext().getPackageManager()) != null) {
//            } else {
//                Toast.makeText(getActivity(), "there's no activity that supports that action", Toast.LENGTH_SHORT).show();
//            }

        }
    };
    private final SupplementAdapter.OnItemClickListener oclItemClickListener = new SupplementAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
            SupplementInfoDialog supplementInfoDialog = new SupplementInfoDialog(position, supplements.get(position), supplements);
            supplementInfoDialog.show(getParentFragmentManager(), "");
        }
    };
}
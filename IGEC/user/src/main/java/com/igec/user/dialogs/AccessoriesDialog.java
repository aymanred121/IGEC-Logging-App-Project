package com.igec.user.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.Settings;
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
import com.igec.user.activities.DateInaccurate;
import com.igec.user.adapters.AccessoryAdapter;
import com.igec.user.R;
import com.igec.common.firebase.Machine;
import com.igec.common.firebase.Accessory;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.igec.user.databinding.DialogAccessoriesBinding;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
public class AccessoriesDialog extends DialogFragment {


    private final Machine machine;
    private ArrayList<Accessory> accessories;
    private boolean isItAUser;
    private final Accessory machineCover = new Accessory();

    //Views
    private Animation show, hide;
    private AccessoryAdapter adapter;

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

    private DialogAccessoriesBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogAccessoriesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
    @Override
    public void onResume() {
        super.onResume();
        validateDate(getActivity());
    }
    private void validateDate(Context c) {
        if (Settings.Global.getInt(c.getContentResolver(), Settings.Global.AUTO_TIME, 0) != 1) {
            Intent intent = new Intent(getActivity(), DateInaccurate.class);
            startActivity(intent);
            getActivity().finish();
        }
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize();
        binding.doneFab.setOnClickListener(oclDone);
    }

    public AccessoriesDialog(boolean isItAUser, Machine machine) {
        this.isItAUser = isItAUser;
        this.machine = machine;
    }

    // Functions
    private void initialize() {
        accessories = new ArrayList<>();
        show = AnimationUtils.loadAnimation(getActivity(), R.anim.show);
        hide = AnimationUtils.loadAnimation(getActivity(), R.anim.hide);
        binding.recyclerView.setHasFixedSize(true);
        adapter = new AccessoryAdapter(accessories);
        binding.recyclerView.setAdapter(adapter);
        if (machine.getSupplementsNames().size() != 0) {
            getAllSupplements();
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.progressBar.startAnimation(show);
            binding.recyclerView.startAnimation(hide);
        } else {
            binding.progressBar.startAnimation(hide);
            binding.recyclerView.startAnimation(show);
        }
    }

    private void getMachineCover() {
        String cover = "cover";
        StorageReference ref = FirebaseStorage.getInstance().getReference().child("/imgs/" + machine.getId() + String.format("/%s.jpg", cover));
        try {
            final File localFile = File.createTempFile(cover, "jpg");
            ref.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                binding.coverImageView.setImageBitmap(bitmap);
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
                        accessories.add(new Accessory(name, bitmap));
                        progress[0]++;
                        if (progress[0] == machine.getSupplementsNames().size()) {
                            getMachineCover();
                            binding.progressBar.startAnimation(hide);
                            binding.recyclerView.startAnimation(show);
                            binding.doneFab.setEnabled(true);
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
        bundle.putString("supplementState", binding.commentEdit.getText().toString());
        bundle.putBoolean("isItAUser", isItAUser);
        getParentFragmentManager().setFragmentResult("supplements", bundle);
        dismiss();


    };


}

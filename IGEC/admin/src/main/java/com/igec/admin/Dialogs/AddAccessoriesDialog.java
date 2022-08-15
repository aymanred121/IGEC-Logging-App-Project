package com.igec.admin.Dialogs;

import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.igec.admin.Adapters.AccessoryAdapter;
import com.igec.admin.R;
import com.igec.admin.databinding.DialogAddAccessoriesBinding;
import com.igec.common.firebase.Accessory;
import com.igec.common.firebase.Machine;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class AddAccessoriesDialog extends DialogFragment {

    private ActivityResultLauncher<Intent> activityResultLauncher;
    private ArrayList<com.igec.common.firebase.Accessory> accessories; // should not be modified -> not saved
    private ArrayList<com.igec.common.firebase.Accessory> updatedAccessories; // all modifications happens here -> saved
    private AccessoryAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private Machine machine;
    private Animation show, hide;
    private ArrayList<String> oldNames = new ArrayList<>();
    private ArrayList<Integer> oldNamesIndexes = new ArrayList<>();
    private boolean saveUpdated = false;
    private String currentPhotoPath;

    // for machine dialog
    public AddAccessoriesDialog(Machine machine) {
        this.machine = machine;
        accessories = new ArrayList<>();
        updatedAccessories = new ArrayList<>();
    }

    // for add machine dialog
    public AddAccessoriesDialog(ArrayList<Accessory> accessories) {
        this.accessories = accessories;
        updatedAccessories = new ArrayList<>();
        accessories.forEach(accessory -> {
            try {
                updatedAccessories.add((Accessory) accessory.clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        });
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
        getParentFragmentManager().setFragmentResultListener("addSupplement", this, (requestKey, bundle) -> {
            // We use a String here, but any type that can be put in a Bundle is supported
            updatedAccessories.add((com.igec.common.firebase.Accessory) bundle.getSerializable("supplement"));
            // Do something with the result
            adapter.notifyDataSetChanged();
        });

        getParentFragmentManager().setFragmentResultListener("editSupplement", this, (requestKey, bundle) -> {
            // We use a String here, but any type that can be put in a Bundle is supported
            int position = bundle.getInt("position");
            com.igec.common.firebase.Accessory accessory = (com.igec.common.firebase.Accessory) bundle.getSerializable("supplement");
            if (!accessory.getName().equals(updatedAccessories.get(position).getName())) {
                if (oldNamesIndexes.contains(position)) {
                    oldNamesIndexes.remove(position);
                    oldNames.remove(oldNames.get(position));
                }
                oldNamesIndexes.add(position);
                oldNames.add(updatedAccessories.get(position).getName());

            }

            updatedAccessories.get(position).setName(accessory.getName());
            updatedAccessories.get(position).setPhoto(accessory.getPhoto());
            // Do something with the result
            adapter.notifyItemChanged(position);
        });
    }

    private DialogAddAccessoriesBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DialogAddAccessoriesBinding.inflate(inflater, container, false);
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
        initialize();
        binding.addFab.setOnClickListener(oclAddPhoto);
        binding.doneFab.setOnClickListener(oclDone);
        adapter.setOnItemClickListener(oclItemClickListener);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {

        if (!saveUpdated) {
            Bundle result = new Bundle();
            result.putSerializable("supplements", accessories);
            getParentFragmentManager().setFragmentResult("supplements", result);
        }
        super.onDismiss(dialog);

    }

    private void initialize() {
        binding.recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new AccessoryAdapter(updatedAccessories);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
                com.igec.common.firebase.Accessory accessory = new com.igec.common.firebase.Accessory();
                accessory.setName("");
                accessory.setPhoto(bitmap);
                AccessoryInfoDialog accessoryInfoDialog = new AccessoryInfoDialog(-1, accessory, updatedAccessories);
                accessoryInfoDialog.show(getParentFragmentManager(), "");
            }
        });
        show = AnimationUtils.loadAnimation(getActivity(), R.anim.show);
        hide = AnimationUtils.loadAnimation(getActivity(), R.anim.hide);
        if (machine != null) {
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
    }

    private void getAllSupplements() {
        StorageReference ref;
        final int[] progress = new int[1];
        for (String name : machine.getSupplementsNames()) {
            if (name.equals("cover"))
                continue;
            ref = FirebaseStorage.getInstance().getReference().child("/imgs/" + machine.getId() + String.format("/%s.jpg", name));
            try {
                final File localFile = File.createTempFile(name, "jpg");
                ref.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                    updatedAccessories.add(new com.igec.common.firebase.Accessory(name, bitmap));
                    progress[0]++;
                    if (progress[0] == machine.getSupplementsNames().size()) {
                        binding.progressBar.startAnimation(hide);
                        binding.recyclerView.startAnimation(show);
                        adapter.notifyDataSetChanged();
                    }
                }).addOnFailureListener(e -> {

                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private final View.OnClickListener oclDone = v -> {
        binding.doneFab.setEnabled(false);
        Bundle result = new Bundle();
        result.putSerializable("supplements", updatedAccessories);
        result.putStringArrayList("oldNames", oldNames);
        getParentFragmentManager().setFragmentResult("supplements", result);
        saveUpdated = true;
        binding.doneFab.setEnabled(true);
        dismiss();

    };
    private final View.OnClickListener oclAddPhoto = v -> {

        String fileName = "photo";
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        try {
            File imageFile = File.createTempFile(fileName, ".jpg", storageDirectory);
            currentPhotoPath = imageFile.getAbsolutePath();

            Uri imageUri = FileProvider.getUriForFile(getActivity(), "com.igec.admin.fileprovider", imageFile);

            Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePicture.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            activityResultLauncher.launch(takePicture);
        } catch (IOException e) {
            e.printStackTrace();
        }

    };
    private final AccessoryAdapter.OnItemClickListener oclItemClickListener = new AccessoryAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
            AccessoryInfoDialog accessoryInfoDialog = new AccessoryInfoDialog(position, updatedAccessories.get(position), updatedAccessories);
            accessoryInfoDialog.show(getParentFragmentManager(), "");
        }

        @Override
        public void onDeleteItem(int position) {

            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
            builder.setTitle(getString(R.string.Delete))
                    .setMessage(getString(R.string.AreUSure))
                    .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    })
                    .setPositiveButton(getString(R.string.accept), (dialogInterface, i) -> {
                        oldNames.add(updatedAccessories.get(position).getName());
                        updatedAccessories.remove(position);
                        adapter.notifyItemRemoved(position);
                    })
                    .show();

        }
    };
}
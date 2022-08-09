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

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.igec.admin.Adatpers.SupplementAdapter;
import com.igec.admin.R;
import com.igec.admin.fireBase.Machine;
import com.igec.admin.fireBase.Supplement;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class AddSupplementsDialog extends DialogFragment {

    private FloatingActionButton vAddPhoto, vDone;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private ArrayList<Supplement> supplements;
    private ArrayList<Supplement> updatedSupplements;
    private SupplementAdapter adapter;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private CircularProgressIndicator vCircularProgressIndicator;
    private Machine machine;
    private Animation show, hide;
    private ArrayList<String> oldNames = new ArrayList<>();
    private ArrayList<Integer> oldNamesIndexes = new ArrayList<>();
    private boolean saveUpdated = false;
    private String currentPhotoPath;

    // for machine dialog
    public AddSupplementsDialog(Machine machine) {
        this.machine = machine;
        supplements = new ArrayList<>();
        updatedSupplements = new ArrayList<>();
    }

    // for add machine dialog
    public AddSupplementsDialog(ArrayList<Supplement> supplements) {
        this.supplements = supplements;
        updatedSupplements = new ArrayList<>();
        updatedSupplements.addAll(supplements);
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
                updatedSupplements.add((Supplement) bundle.getSerializable("supplement"));
                // Do something with the result
                adapter.notifyDataSetChanged();
            }
        });

        getParentFragmentManager().setFragmentResultListener("editSupplement", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                // We use a String here, but any type that can be put in a Bundle is supported
                int position = bundle.getInt("position");
                Supplement supplement = (Supplement) bundle.getSerializable("supplement");
                if (!supplement.getName().equals(updatedSupplements.get(position).getName())) {
                    if (oldNamesIndexes.contains(position)) {
                        oldNamesIndexes.remove(position);
                        oldNames.remove(oldNames.get(position));
                    }
                    oldNamesIndexes.add(position);
                    oldNames.add(updatedSupplements.get(position).getName());

                }

                updatedSupplements.get(position).setName(supplement.getName());
                updatedSupplements.get(position).setPhoto(supplement.getPhoto());
                // Do something with the result
                adapter.notifyItemChanged(position);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.dialog_add_supplements, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize(view);
        vAddPhoto.setOnClickListener(oclAddPhoto);
        vDone.setOnClickListener(oclDone);
        adapter.setOnItemClickListener(oclItemClickListener);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {

        if(!saveUpdated) {
            Bundle result = new Bundle();
            result.putSerializable("supplements", supplements);
            getParentFragmentManager().setFragmentResult("supplements", result);
        }
        super.onDismiss(dialog);

    }

    private void initialize(View view) {
        vCircularProgressIndicator = view.findViewById(R.id.progress_bar);
        vAddPhoto = view.findViewById(R.id.Button_AddPhoto);
        vDone = view.findViewById(R.id.Button_Done);
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new SupplementAdapter(updatedSupplements);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    //Bundle bundle = result.getData().getExtras();
                    Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
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
    }

    private void getAllSupplements() {
        StorageReference ref;
        final int[] progress = new int[1];
        for (String name : machine.getSupplementsNames()) {
            if(name.equals("cover"))
                continue;
            ref = FirebaseStorage.getInstance().getReference().child("/imgs/" + machine.getId() + String.format("/%s.jpg", name));
            try {
                final File localFile = File.createTempFile(name, "jpg");
                ref.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                    updatedSupplements.add(new Supplement(name, bitmap));
                    supplements.add(new Supplement(name,bitmap));
                    progress[0]++;
                    if (progress[0] == machine.getSupplementsNames().size()) {
                        vCircularProgressIndicator.startAnimation(hide);
                        recyclerView.startAnimation(show);
                        adapter.notifyDataSetChanged();
                    }
                }).addOnFailureListener(e -> {

                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void saveToInternalStorage(Bitmap img, String supplementName) {
        if (new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + supplementName + ".jpg").exists())
            return;
        Bitmap bitmapImage = img;
        if (bitmapImage == null)
            return;
        File path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), supplementName + ".jpg");
        FileOutputStream fos = null;
        try {
            path.createNewFile();
            fos = new FileOutputStream(path.getAbsoluteFile());
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return;
    }
    private final View.OnClickListener oclDone = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            vDone.setEnabled(false);
            Bundle result = new Bundle();
            result.putSerializable("supplements", updatedSupplements);
            result.putStringArrayList("oldNames", oldNames);
            getParentFragmentManager().setFragmentResult("supplements", result);
            saveUpdated = true;
            vDone.setEnabled(true);
            dismiss();
        }
    };
    private final View.OnClickListener oclAddPhoto = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            String fileName = "photo";
            File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

            try {
                File imageFile = File.createTempFile(fileName,".jpg", storageDirectory);
                currentPhotoPath = imageFile.getAbsolutePath();

                Uri imageUri =  FileProvider.getUriForFile(getActivity(),"com.igec.admin.fileprovider",imageFile);

                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                takePicture. putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
                activityResultLauncher.launch(takePicture);
            } catch (IOException e) {
                e.printStackTrace();
            }


//

        }
    };
    private final SupplementAdapter.OnItemClickListener oclItemClickListener = new SupplementAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
            SupplementInfoDialog supplementInfoDialog = new SupplementInfoDialog(position, updatedSupplements.get(position), updatedSupplements);
            supplementInfoDialog.show(getParentFragmentManager(), "");
        }

        @Override
        public void onDeleteItem(int position) {

            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
            builder.setTitle(getString(R.string.Delete))
                    .setMessage(getString(R.string.AreUSure))
                    .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    })
                    .setPositiveButton(getString(R.string.accept), (dialogInterface, i) -> {
                        oldNames.add(updatedSupplements.get(position).getName());
                        updatedSupplements.remove(position);
                        adapter.notifyItemRemoved(position);
                    })
                    .show();

        }
    };
}
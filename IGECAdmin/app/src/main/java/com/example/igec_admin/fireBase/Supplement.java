package com.example.igec_admin.fireBase;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class Supplement {
    private String name ;
    private Bitmap photo;
    private Context activity;

    public Context getActivity() {
        return activity;
    }

    public void setActivity(Context activity) {
        this.activity = activity;
    }

    public Supplement(String name, Bitmap photo) {
        this.name = name;
        this.photo = photo;
    }

    public Supplement() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Bitmap getPhoto() {
        return photo;
    }

    public void setPhoto(Bitmap photo) {
        this.photo = photo;
    }
    private void saveToCloudStorage(StorageReference storageRef , String file  ) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 20, baos);
        byte[] data = baos.toByteArray();
        StorageReference mountainsRef = storageRef.child("imgs/"+ file +"/"+ name+ ".jpg");

        UploadTask uploadTask = mountainsRef.putBytes(data);
        uploadTask.addOnSuccessListener(unsed->{
            Toast.makeText(getActivity(), "uploaded", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e->{
            Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
        });
    }
}

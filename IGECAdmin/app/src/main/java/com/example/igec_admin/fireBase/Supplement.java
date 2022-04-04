package com.example.igec_admin.fireBase;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

public class Supplement implements Parcelable, Serializable {
    private String name;
    private Bitmap photo;

    protected Supplement(Parcel in) {
        name = in.readString();
        photo = in.readParcelable(Bitmap.class.getClassLoader());
    }

    public static final Creator<Supplement> CREATOR = new Creator<Supplement>() {
        @Override
        public Supplement createFromParcel(Parcel in) {
            return new Supplement(in);
        }

        @Override
        public Supplement[] newArray(int size) {
            return new Supplement[size];
        }
    };

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

    public UploadTask saveToCloudStorage(StorageReference storageRef, String file) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        StorageReference mountainsRef = storageRef.child("imgs/" + file + "/" + name + ".jpg");

        return mountainsRef.putBytes(data);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeParcelable(photo, flags);
    }
}

package com.igec.common.firebase;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

public class Accessory implements Parcelable, Serializable, Cloneable {
    public static final Creator<Accessory> CREATOR = new Creator<Accessory>() {
        @Override
        public Accessory createFromParcel(Parcel in) {
            return new Accessory(in);
        }

        @Override
        public Accessory[] newArray(int size) {
            return new Accessory[size];
        }
    };

    @NonNull
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    private String name;
    private Bitmap photo;

    protected Accessory(Parcel in) {
        name = in.readString();
        photo = in.readParcelable(Bitmap.class.getClassLoader());
    }

    public Accessory(String name, Bitmap photo) {
        this.name = name;
        this.photo = photo;
    }

    public Accessory() {
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
        photo.compress(Bitmap.CompressFormat.JPEG, 40, baos);
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

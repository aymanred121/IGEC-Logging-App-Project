package com.example.igec_admin.utilites;

import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CsvWriter {
    private StringBuilder data;
    public CsvWriter(){
        data = new StringBuilder();
    }
    public CsvWriter addHeader(String... Values){
        for(String s : Values){
            data.append(s).append(",");
        }
        data = data.deleteCharAt(data.length()-1);
        data.append("\n");
        return this;
    }
    public CsvWriter addDataRow(String... Values){
        //add data to header
        for(String s:Values){
            this.data.append(s).append(",");
        }
        if(data.substring(data.length()-1).equals(",")){
            data = data.deleteCharAt(data.length()-1);
        }
        return this;
    }
    public void build(String fileName) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            File gpxfile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), String.format("%S.csv", fileName));
            FileWriter writer;
                writer = new FileWriter(gpxfile);
                writer.append(data.toString());
                writer.flush();
                writer.close();
            }

        }

    }


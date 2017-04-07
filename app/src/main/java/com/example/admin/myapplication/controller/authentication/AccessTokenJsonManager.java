package com.example.admin.myapplication.controller.authentication;

import android.os.Bundle;
import android.os.Environment;

import com.facebook.AccessToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by admin on 07/04/2017.
 */
public class AccessTokenJsonManager {
    private static final String PATH = "accessToken.txt";

    private String getFullPath() {
        return PATH;
//        return Environment.getExternalStorageDirectory() + File.separator + "/AppName/App_cache/data" + File.separator + PATH;
    }

    public String storeAccessToken(Object object) throws IOException {
        String path = getFullPath();

        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        path += "data";
        File data = new File(path);
        if (!data.createNewFile()) {
            data.delete();
            data.createNewFile();
        }
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(data));
        objectOutputStream.writeObject(object);
        objectOutputStream.close();
        return path;
    }

    public Object loadAccessToken() throws IOException, ClassNotFoundException {
        String path = getFullPath();

        Object object = null;
        File data = new File(path);
        if(data.exists()) {
            ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(data));
            object = objectInputStream.readObject();
            objectInputStream.close();
        }
        return object;
    }

    public String clearAccessToken() throws IOException {
        return storeAccessToken(null);
    }
}
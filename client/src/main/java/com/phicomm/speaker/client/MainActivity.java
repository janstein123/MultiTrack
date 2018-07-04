package com.phicomm.speaker.client;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Client-MainActivity";

    TCPHelper mTCPHelper;

    UDPHelper mUDPHelper;

    private boolean tcp = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate, " + Thread.currentThread().getId());
        setContentView(R.layout.activity_main);
        if (tcp) {
            mTCPHelper = TCPHelper.getInstance();
            mTCPHelper.connectServer("192.168.199.215");
        } else {
            mUDPHelper = UDPHelper.getInstance();
            mUDPHelper.listenRawData();
        }
//        File file = new File(Environment.getExternalStorageDirectory().getPath() + "/happiness.pcm");
//        FileInputStream inputStream = null;
//        try {
//            inputStream = new FileInputStream(file);
//
//        byte[] data = new byte[1024];
//        int size;
//        while ((size = inputStream.read(data)) > 0){
//            RawDataPlayer.getInstance().write(data, 0, size);
//        }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (inputStream != null){
//                try {
//                    inputStream.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
    }
}
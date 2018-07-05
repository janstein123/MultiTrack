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
    public static final String SERVER_IP = "192.168.2.147";

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
            mTCPHelper.connectServer(SERVER_IP);
        } else {
            mUDPHelper = UDPHelper.getInstance();
            mUDPHelper.listenRawData();
        }

    }
}
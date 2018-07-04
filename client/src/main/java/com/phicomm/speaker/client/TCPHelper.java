package com.phicomm.speaker.client;

import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPHelper {

    private static final String TAG = "Client-TCPHelper";
    private static TCPHelper sTCPHelper;

    private Socket mClientSocket;

    private List<Socket> mClients = new CopyOnWriteArrayList<>();

    private ExecutorService mThreadPool = Executors.newCachedThreadPool();

    private boolean mIsMainSpeaker = true;


    private Handler mHandler = new Handler();

    private TCPHelper() {
    }


    public static TCPHelper getInstance() {
        if (sTCPHelper == null) {
            synchronized (TCPHelper.class) {
                if (sTCPHelper == null) {
                    sTCPHelper = new TCPHelper();
                }
            }
        }
        return sTCPHelper;
    }

    File file = new File(Environment.getExternalStorageDirectory().getPath() + "/happiness.pcm");

    public void connectServer(final String ip) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "connect server ++");
                    mClientSocket = new Socket(ip, 12346);
//                    mClientSocket.setReceiveBufferSize(1024 * 1024);
//                    mClientSocket.setSendBufferSize(1024 * 1024);
                    Log.d(TAG, "connect server --");
                    InputStream inputStream = mClientSocket.getInputStream();
                    final byte[] data = new byte[5120];
                    int size;
//                    FileOutputStream fos = new FileOutputStream(file);
                    while ((size = inputStream.read(data)) > 0) {
                        Log.d(TAG, "read bytes:" + size);
//                        fos.write(data, 0, size);
                        RawDataPlayer.getInstance().write(data, 0, size);
//                        mHandler.post(new Runnable() {
//                            @Override
//                            public void run() {
//
//                            }
//                        });

//                        Thread.sleep(5);
                    }
                    inputStream.close();
//                    fos.flush();
//                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } /*catch (InterruptedException e) {
                    e.printStackTrace();
                }*/finally {

                }
            }
        });
    }

}

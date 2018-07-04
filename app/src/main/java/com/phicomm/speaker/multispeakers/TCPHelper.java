package com.phicomm.speaker.multispeakers;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.phicomm.speaker.multispeakers.MainActivity.MSG_DECODE_AUDIO;

public class TCPHelper {

    private static final String TAG = "TCPHelper";
    private static TCPHelper sTCPHelper;

    private ServerSocket mServerSocket;

    private Socket mClientSocket;

    private List<Socket> mClients = new CopyOnWriteArrayList<>();

    private ExecutorService mThreadPool = Executors.newCachedThreadPool();

    private boolean mIsMainSpeaker = true;

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

    private Handler mHandler;

    public void setHandler(Handler handler) {
        mHandler = handler;
    }

    public void acceptClient() {
        try {
            mServerSocket = new ServerSocket(12346);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Log.d(TAG, "wait client");
                        Socket socket = mServerSocket.accept();
                        Log.d(TAG, "new client connected, ip:" + socket.getInetAddress());
                        mClients.add(socket);
//                        mHandler.sendEmptyMessage(MSG_DECODE_AUDIO);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
    }

    private List<Socket> deadSockets = new ArrayList<>();

    public void sendData(byte[] data, int index) {
        Log.d(TAG, "client size:" + mClients.size());
        Log.d(TAG, "send Data:" + data.length + ", index:" + index);
        for (int i = mClients.size() - 1; i >=0; i--) {
//            Log.d(TAG, "connected:"+client.isConnected()+", "+client.isClosed()+", "+client.isBound()+", "+client.isOutputShutdown()+", "+client);
            Socket client = mClients.get(i);
            if (!client.isConnected()) {
                Log.d(TAG, "client:" + client.getInetAddress() + " is disconnected");
                deadSockets.add(client);
                continue;
            }
            try {
                OutputStream outputStream = client.getOutputStream();
                Log.d(TAG, "write to output stream ");
                outputStream.write(data);
            } catch (Exception e) {
                deadSockets.add(client);
            }
        }
        if (!deadSockets.isEmpty()) {
            mClients.removeAll(deadSockets);
            deadSockets.clear();
        }


    }

    public void connectServer(final String ip) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "connect server ++");
                    mClientSocket = new Socket(ip, 12346);
                    Log.d(TAG, "connect server --");
                    InputStream inputStream = mClientSocket.getInputStream();
                    byte[] data = new byte[5120];
                    int size ;
                    while ((size = inputStream.read(data)) > 0) {
                        Log.d(TAG, "read bytes:" + size);
                        RawDataPlayer.getInstance().write(data, 0, size);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}

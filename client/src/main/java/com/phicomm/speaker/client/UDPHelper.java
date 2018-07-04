package com.phicomm.speaker.client;

import android.util.Log;

import java.io.IOException;
import java.net.*;

public class UDPHelper {
    private static final String TAG = "Client-UDPHelper";
    //    private DatagramSocket mSendSocket;
    private DatagramSocket mSocket;

    private boolean flag = true;

    private static UDPHelper sUDPHelper;

    public static UDPHelper getInstance() {
        if (sUDPHelper == null) {
            synchronized (TCPHelper.class) {
                if (sUDPHelper == null) {
                    sUDPHelper = new UDPHelper();
                }
            }
        }
        return sUDPHelper;
    }

    private UDPHelper(){
        initSocket();
    }

    private void initSocket() {
        try {
            mSocket = new DatagramSocket(12345);
            mSocket.setSendBufferSize(1 * 1024 * 1024);
            mSocket.setReceiveBufferSize(1 * 1024 * 1024);
            Log.d(TAG, "getSendBufferSize:" + mSocket.getSendBufferSize());
            Log.d(TAG, "getReceiveBufferSize:" + mSocket.getReceiveBufferSize());
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }



    public void listenRawData() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] data = new byte[5120];
                while (flag) {
                    DatagramPacket packet = new DatagramPacket(data, data.length);
                    try {
                        Log.d(TAG, "listen...");
                        mSocket.receive(packet);
                        int len = packet.getLength();
                        packet.getData();
//                Log.d(TAG, data[0] + "," + data[1] + "," + data[2] + " -------- " + data[packet.getLength() - 1]);
                        int index = (data[len - 4] & 0xFF) | (data[len - 3] & 0xff) << 8 | (data[len - 2] & 0xff) << 16 | (data[len - 1] & 0xff) << 24;
                        Log.d(TAG, "receive data, len:" + len + ", index:" + index /*+ ", blockIndex:" + blockIndex*/);
                        RawDataPlayer.getInstance().write(data, 0, len - 4);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        t.start();

    }
}

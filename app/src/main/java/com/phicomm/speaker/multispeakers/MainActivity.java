package com.phicomm.speaker.multispeakers;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.*;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.phicomm.speaker.multispeakers.otherplayer.AudioPlayer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, Handler.Callback {

    private static final String TAG = "multi-speaker";
    private static final int MSG_SEND_RAW_DATA = 0x1001;
    public static final int MSG_DECODE_AUDIO = 0x1002;

    public static final String SERVER_IP = "192.168.2.147";

    private AudioPlayer mAudioPlayer;
    private Handler mHandler = new Handler();

    private Handler mSendHandler;
    private Handler mDecodeHandler;
    private HandlerThread mSendThread;
    private HandlerThread mDecodeThread;


    ExecutorService mExecutor = Executors.newCachedThreadPool();

    private TCPHelper mTCPHelper;
    private UDPHelper mUDPHelper;

    private RawDataPlayer mPlayer;

    private boolean tcp = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView textView = findViewById(R.id.play);
        textView.setOnClickListener(this);

        mSendThread = new HandlerThread("send");
        mSendThread.start();

        mSendHandler = new Handler(mSendThread.getLooper(), this);

        mDecodeThread = new HandlerThread("decode");
        mDecodeThread.start();
        mDecodeHandler = new Handler(mDecodeThread.getLooper(), this);


//        doWithAudioPlayer();
        if (tcp) {
            mTCPHelper = TCPHelper.getInstance();
            mTCPHelper.acceptClient();
            mTCPHelper.setHandler(mDecodeHandler);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mTCPHelper.connectServer(SERVER_IP);
                }
            }, 3000);

            mDecodeHandler.sendEmptyMessageDelayed(MSG_DECODE_AUDIO, 20000);
        } else {
            mUDPHelper = UDPHelper.getInstance();

            mUDPHelper.listenRawData();
            mDecodeHandler.sendEmptyMessageDelayed(MSG_DECODE_AUDIO, 25000);
        }
    }

    @Override
    public void onClick(View v) {

    }

    private void createMediaExtractor() {
        MediaExtractor mediaExtractor = new MediaExtractor();
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

//        String path = Environment.getExternalStorageDirectory() + "/longquan.mp3";
        String path = Environment.getExternalStorageDirectory() + "/yinzi.mp3";
//        String path = Environment.getExternalStorageDirectory() + "/qinghuaci.mp3";
//        String path = Environment.getExternalStorageDirectory() + "/hongdou.mp3";
//        String path = Environment.getExternalStorageDirectory() + "/lover.mp3";
//        String path = "/system/unisound/ringing/happiness.mp3";
        String mime = null;
        MediaFormat trackFormat = null;
        try {
            mediaExtractor.setDataSource(path);
            int trackCount = mediaExtractor.getTrackCount();
            Log.d(TAG, "track count:" + trackCount);
            for (int i = 0; i < trackCount; i++) {
                trackFormat = mediaExtractor.getTrackFormat(i);
                Log.d(TAG, "trackFormat:" + trackFormat);
                mime = trackFormat.getString(MediaFormat.KEY_MIME);
                if (mime.startsWith("audio/")) {
                    mediaExtractor.selectTrack(i);
                }
                break;
            }
            int size = 0;
            int N = 1;
            byte[][] nChunks = new byte[N][];
            if (mime != null && trackFormat != null) {
                MediaCodec mediaCodec = MediaCodec.createDecoderByType(mime);
                mediaCodec.configure(trackFormat, null, null, 0);
                mediaCodec.start();
                int i = 0;
                do {
                    int inputBufferIndex = mediaCodec.dequeueInputBuffer(100000);
                    Log.d(TAG, "inputBufferIndex:" + inputBufferIndex);
                    if (inputBufferIndex != -1) {
                        ByteBuffer inputBuffer = mediaCodec.getInputBuffer(inputBufferIndex);
                        if (inputBuffer != null) {
                            size = mediaExtractor.readSampleData(inputBuffer, 0);
                            long presentationTimeUs = mediaExtractor.getSampleTime();

                            mediaExtractor.advance();
                            Log.d(TAG, "size:" + size + ", presentationTimeUs:" + presentationTimeUs);
                            if (size > 0) {
                                mediaCodec.queueInputBuffer(inputBufferIndex, 0, size, presentationTimeUs, 0);
                                int index = mediaCodec.dequeueOutputBuffer(bufferInfo, 100000);
                                Log.d(TAG, "outputBufferIndex:" + index);
                                Log.d(TAG, "bufferInfo:" + bufferInfo.size + ", " + bufferInfo.offset + ", " + bufferInfo.presentationTimeUs);
                                if (index >= 0) {
                                    ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(index);
                                    nChunks[i % N] = new byte[bufferInfo.size];
                                    outputBuffer.get(nChunks[i % N]);

                                    i++;
                                    if (i % N == 0) {
                                        int len = 0;
                                        for (int j = 0; j < N; j++) {
                                            len += nChunks[j].length;
                                        }
                                        byte[] bigChunk = new byte[len];

                                        for (int j = 0; j < N; j++) {
                                            int pos = j == 0 ? 0 : nChunks[j - 1].length;
                                            System.arraycopy(nChunks[j], 0, bigChunk, pos, nChunks[j].length);
                                        }
                                        Log.d(TAG, "send raw data i:" + i + ", bigChunk size:" + bigChunk.length);
                                        sendRawData(bigChunk, i / N);
                                    }
                                    outputBuffer.clear();
                                    mediaCodec.releaseOutputBuffer(index, false);
                                } else if (index == -2) {
                                    MediaFormat newFormat = mediaCodec.getOutputFormat();
                                    Log.d(TAG, "new format:" + newFormat);
                                }
                            } else {

                            }
                        }

                    }
                } while (size > 0);
                Log.d(TAG, "decode end..........i=" + i);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendRawData(byte[] data, int index) {
//        Log.d(TAG, "send raw data, length:" + data.length + ", index:" + index);
//        for (int i = 0; ; i++) {
//            int size = (data.length >= (i + 1) * 1024 ? 1024 : data.length - 1024 * i);
//            byte[] block = new byte[size];
//            System.arraycopy(data, i * 1024, block, 0, size);
////            block[size] = (byte) (index & 0xFF);
////            block[size + 1] = (byte) ((index >> 8) & 0xFF);
////            block[size + 2] = (byte) ((index >> 16) & 0xFF);
////            block[size + 3] = (byte) ((index >> 24) & 0xFF);
////            block[size + 4] = (byte) i;
//            Message msg = mSendHandler.obtainMessage(MSG_SEND_RAW_DATA, index, i, block);
//            mSendHandler.sendMessageDelayed(msg, 50);
//            if (size < 1024) {
//                break;
//            }
//        }
        Message msg = mSendHandler.obtainMessage(MSG_SEND_RAW_DATA, index, -1, data);
        mSendHandler.sendMessageDelayed(msg, 0);

        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }


    private void playRawData(byte[] data, int size) {
        mPlayer.write(data, 0, size);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSendThread.quitSafely();
        mDecodeThread.quitSafely();
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == MSG_SEND_RAW_DATA) {
            byte[] rawData = (byte[]) msg.obj;
            int index = msg.arg1;
            if (tcp) {
                mTCPHelper.sendData(rawData, index);
            } else {
                mUDPHelper.broadcastRawData(rawData, index);

            }
        } else if (msg.what == MSG_DECODE_AUDIO) {
            createMediaExtractor();
        }
        return true;
    }

}

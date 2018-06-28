package com.phicomm.speaker.multispeakers;

import android.media.AudioTrack;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "multi-speaker";
    AudioTrack audioTrack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView textView = findViewById(R.id.play);
        textView.setOnClickListener(this);
        //int minBufferSize = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_STEREO,
        //    AudioFormat.ENCODING_PCM_16BIT);
        //AudioAttributes attributes = new AudioAttributes.Builder().setUsage(USAGE_MEDIA)
        //    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        //    .setLegacyStreamType(STREAM_MUSIC)
        //    .build();
        //
        //AudioFormat audioFormat = new AudioFormat.Builder().setSampleRate(44100)
        //    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
        //    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
        //    .build();
        //
        //audioTrack = new AudioTrack(attributes, audioFormat, minBufferSize, AudioTrack.MODE_STREAM,
        //    AudioManager.AUDIO_SESSION_ID_GENERATE);
        //audioTrack.play();
        createMediaExtractor();
    }

    @Override
    public void onClick(View v) {

    }

    private void createMediaExtractor() {
        MediaExtractor mediaExtractor = new MediaExtractor();
        String path = Environment.getExternalStorageDirectory() + "/lover.mp3";

        try {
            mediaExtractor.setDataSource(path);
            int trackCount = mediaExtractor.getTrackCount();
            Log.d(TAG, "track count:" + trackCount);
            for (int i = 0; i < trackCount; i++) {
                MediaFormat trackFormat = mediaExtractor.getTrackFormat(i);
                Log.d(TAG, "" + trackFormat);
                mediaExtractor.selectTrack(i);
                break;
            }
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            int size;
            while ((size = mediaExtractor.readSampleData(byteBuffer, 0)) >= 0) {
                int sampleTrackIndex = mediaExtractor.getSampleTrackIndex();
                long sampleTime = mediaExtractor.getSampleTime();
                Log.d(TAG, "size:" + size + ", sampleTrackIndex:" + sampleTrackIndex + ", sampleTime:" + sampleTime);

                mediaExtractor.advance();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

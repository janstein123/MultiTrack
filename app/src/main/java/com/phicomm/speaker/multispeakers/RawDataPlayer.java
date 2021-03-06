package com.phicomm.speaker.multispeakers;

import android.media.*;
import android.util.Log;

import static android.media.AudioAttributes.USAGE_MEDIA;
import static android.media.AudioManager.STREAM_MUSIC;

public class RawDataPlayer {

    private static final String TAG = "RawDataPlayer";
    private static RawDataPlayer sPlayer;

    private AudioTrack mAudioTrack;

    private RawDataPlayer() {
        initAudioTrack();
    }

    public static RawDataPlayer getInstance() {
        if (sPlayer == null) {
            synchronized (RawDataPlayer.class) {
                if (sPlayer == null) {
                    sPlayer = new RawDataPlayer();
                }
            }
        }
        return sPlayer;
    }

    private void initAudioTrack() {
        int minBufferSize = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT);
        Log.d(TAG, "minBufferSize" + minBufferSize);
        AudioAttributes attributes = new AudioAttributes.Builder().setUsage(USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setLegacyStreamType(STREAM_MUSIC)
                .build();

        AudioFormat audioFormat = new AudioFormat.Builder().setSampleRate(44100)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                .build();

        mAudioTrack = new AudioTrack(attributes, audioFormat, minBufferSize, AudioTrack.MODE_STREAM,
                AudioManager.AUDIO_SESSION_ID_GENERATE);
        mAudioTrack.setStereoVolume(0, 1);
        mAudioTrack.play();
    }

    public void write(byte[] audioData, int offsetInBytes, int sizeInBytes) {
        int ret = mAudioTrack.write(audioData, offsetInBytes, sizeInBytes);
        Log.d(TAG, "write ret:" + ret);
    }

    public void stop() {
        if (mAudioTrack != null) {
            mAudioTrack.stop();
            mAudioTrack.release();
        }
        sPlayer = null;
    }
}

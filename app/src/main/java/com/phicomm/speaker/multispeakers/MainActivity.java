package com.phicomm.speaker.multispeakers;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import static android.media.AudioAttributes.USAGE_MEDIA;
import static android.media.AudioManager.STREAM_MUSIC;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

  AudioTrack audioTrack;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    TextView textView = findViewById(R.id.play);
    textView.setOnClickListener(this);
    int minBufferSize = AudioTrack.getMinBufferSize(441000, AudioFormat.CHANNEL_OUT_STEREO,
        AudioFormat.ENCODING_PCM_16BIT);
    AudioAttributes attributes = new AudioAttributes.Builder().setUsage(USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .setLegacyStreamType(STREAM_MUSIC)
        .build();

    AudioFormat audioFormat = new AudioFormat.Builder().setSampleRate(441000)
        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
        .build();

    audioTrack = new AudioTrack(attributes, audioFormat, minBufferSize, AudioTrack.MODE_STREAM,
        AudioManager.AUDIO_SESSION_ID_GENERATE);
    audioTrack.play();
  }

  @Override public void onClick(View v) {

  }
}

package com.yzx.chat.mvp.view.activity;


import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.util.VoiceRecorder2;
import com.yzx.chat.widget.view.VisualizerView;


public class TestActivity extends BaseCompatActivity {

    VoiceRecorder2 mVoiceRecorder2 = new VoiceRecorder2();
    VisualizerView visualizerView;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_test;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        visualizerView = findViewById(R.id.ddd);
        visualizerView.setStrokeColor(ContextCompat.getColor(this, R.color.colorAccent));
    }

    @Override
    protected void setup(Bundle savedInstanceState) {

    }


    public void onClick(View v) {
        mVoiceRecorder2.start(Environment.getExternalStorageDirectory().getPath() + "/Chat" + "/a.pcm", new Visualizer.OnDataCaptureListener() {
            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
                visualizerView.updateWaveform(waveform);
            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
                visualizerView.updateFFT(fft);
            }
        }, true, true);
    }

    public void onClick1(View v) {
        mVoiceRecorder2.stop();
    }
}



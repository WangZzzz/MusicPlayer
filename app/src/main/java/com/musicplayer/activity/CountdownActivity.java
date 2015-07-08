package com.musicplayer.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import com.musicplayer.R;

/**
 * Created by WangZ on 2015/7/2.
 */
public class CountdownActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_countdown);
    }
}

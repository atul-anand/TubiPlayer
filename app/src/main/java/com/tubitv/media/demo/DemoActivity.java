package com.tubitv.media.demo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.exoplayer2.source.MediaSource;
import com.tubitv.media.activities.TubiPlayerActivity;
import com.tubitv.media.models.MediaModel;

public class DemoActivity extends TubiPlayerActivity {
    private final static String TAG = DemoActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPlayerReady() {

        MediaSource mediaSource = createMediaSource();

        playMedia(mediaSource);
    }

    @Override
    public void onProgress(@Nullable MediaModel mediaModel, long milliseconds, long durationMillis) {
//        if (mediaModel != null) {
//            Log.d(TAG, "playback progress media url: " + mediaModel.getVideoUrl());
//        }
//        Log.d(TAG, "playback progress millis: " + milliseconds);
//        if (milliseconds > 15000) {
//            playMedia(createMediaSource());
//        }

    }

    @Override
    public void onSeek(@Nullable MediaModel mediaModel, long oldPositionMillis, long newPositionMillis) {
//        Log.d(TAG, "playback seek : " + oldPositionMillis + " to " + newPositionMillis);
    }

    @Override
    public void onPlayToggle(@Nullable MediaModel mediaModel, boolean playing) {
        Log.d(TAG, "playing : " + playing);
    }

    @Override
    public void onLearnMoreClick(@NonNull MediaModel mediaModel) {
        if (mediaModel.getClickThroughUrl() != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mediaModel.getClickThroughUrl()));
            startActivity(intent);
        }
    }

    @Override
    public void onSubtitles(@Nullable MediaModel mediaModel, boolean enabled) {
        Log.d(TAG, "subtitles : " + enabled);
    }

    @Override
    public void onQuality(@Nullable MediaModel mediaModel) {
        Log.d(TAG, "quality");
    }


}

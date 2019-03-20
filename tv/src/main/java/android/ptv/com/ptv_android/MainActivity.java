/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package android.ptv.com.ptv_android;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.ptv.com.ptv_android.model.Program;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.exoplayer2.*;
import com.google.android.exoplayer2.source.*;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.*;
import com.google.android.exoplayer2.upstream.cache.*;
import com.google.android.exoplayer2.util.Util;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.io.File;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.List;

/*
 * MainActivity class that loads {@link MainFragment}.
 */
public class MainActivity extends Activity implements Player.EventListener {

    protected String userAgent;
    private DataSource.Factory dataSourceFactory;
    private File downloadDirectory;
    private Cache downloadCache;
    private static final String DOWNLOAD_CONTENT_DIRECTORY = "downloads";

    private static final CookieManager DEFAULT_COOKIE_MANAGER;
    static {
        DEFAULT_COOKIE_MANAGER = new CookieManager();
        DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    private Integer currentChannelId;

    private APIClient client;
    private SimpleExoPlayer player;
    private PlayerView playerView;

    private Program currentProgram;
    private List<Program> channelList;
    private List<Program> currentPrograms;
    private Integer currentProgramIndex;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OverrideSSLCertificateTrust.DisableSSLCertificateVerify();

        setContentView(R.layout.activity_main);


        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new
            StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        this.client = new APIClient();

        this.player = ExoPlayerFactory.newSimpleInstance(this);
        this.player.addListener(this);

        playerView = findViewById(R.id.player_view);
        playerView.setPlayer(player);
        player.addListener(this);
        player.setPlayWhenReady(true);
        playerView.setUseController(false);


        // Produces DataSource instances through which media data is loaded.
        this.dataSourceFactory = new DefaultDataSourceFactory(this.getApplicationContext(),
                Util.getUserAgent(this.getApplicationContext(), "ptv-androidtv"));


        List<Program> programs = client.getCurrentPlayingPrograms();
        if (programs != null && programs.size() > 0) {
            this.channelList = programs;
            this.currentChannelId = programs.get(0).getChannelId();
            prepareForChannel(this.currentChannelId);
        } else {
            Toast.makeText(this, "Error getting current programs", Toast.LENGTH_LONG).show();
        }



    }

    private void prepareForChannel(Integer channelId) {
        this.currentChannelId = channelId;
        player.stop(true);
        List<Program> programs = this.client.getNextShows(currentChannelId);
        ConcatenatingMediaSource concatenatingMediaSource = new ConcatenatingMediaSource();
        this.currentPrograms = programs;
        this.currentProgramIndex = -1;
        boolean first = true;
        for (Program program : programs) {
            String playUrl = program.getPlayUrl();
            if (first) {
                this.currentProgram = program;
                if (program.hasStarted()) {
                    // This should work but causes 400 in the app, works in VLC though...
                    playUrl += "&offset=" + program.getCurrentTimeSeconds();
                }
            }
            first = false;


            // This is the MediaSource representing the media to be played.
            // DashMediaSource for dash sources....
            Log.d("Main", "Title = " + program.getTitleName() + " Play URL = " + playUrl);
            MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(playUrl));
            if (program.hasStarted() && program.hasFillerCutSeconds()) {
                long startTime = 1000000 * program.getCurrentTimeSeconds();
                long endTime = 1000000 * program.getFillerCutSeconds();
                ClippingMediaSource clippingMediaSource = new ClippingMediaSource(videoSource, startTime, endTime);
                concatenatingMediaSource.addMediaSource(clippingMediaSource);

/*
            } else if (program.hasStarted()) {
                long startTime = 1000000 * program.getCurrentTimeSeconds();

                ClippingMediaSource clippingMediaSource = new ClippingMediaSource(videoSource, startTime, C.TIME_END_OF_SOURCE);
                concatenatingMediaSource.addMediaSource(clippingMediaSource);
*/

            } else if (program.hasFillerCutSeconds()) {
                long endTime = 1000000 * program.getFillerCutSeconds();
                ClippingMediaSource clippingMediaSource = new ClippingMediaSource(videoSource, endTime);
                concatenatingMediaSource.addMediaSource(clippingMediaSource);
            } else {
                concatenatingMediaSource.addMediaSource(videoSource);
            }

        }
        player.setPlayWhenReady(true);
        player.prepare(concatenatingMediaSource);

    }

    /** Returns a {@link DataSource.Factory}. */
    public DataSource.Factory buildDataSourceFactory() {
        DefaultDataSourceFactory upstreamFactory =
                new DefaultDataSourceFactory(this, buildHttpDataSourceFactory());
        return buildReadOnlyCacheDataSource(upstreamFactory, getDownloadCache());
    }

    private synchronized Cache getDownloadCache() {
        if (downloadCache == null) {
            File downloadContentDirectory = new File(getDownloadDirectory(), DOWNLOAD_CONTENT_DIRECTORY);
            downloadCache = new SimpleCache(downloadContentDirectory, new NoOpCacheEvictor());
        }
        return downloadCache;
    }

    private File getDownloadDirectory() {
        if (downloadDirectory == null) {
            downloadDirectory = this.getExternalFilesDir(null);
            if (downloadDirectory == null) {
                downloadDirectory = this.getFilesDir();
            }
        }
        return downloadDirectory;
    }

    private static CacheDataSourceFactory buildReadOnlyCacheDataSource(
            DefaultDataSourceFactory upstreamFactory, Cache cache) {
        return new CacheDataSourceFactory(
                cache,
                upstreamFactory,
                new FileDataSourceFactory(),
                /* cacheWriteDataSinkFactory= */ null,
                CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR,
                /* eventListener= */ null);
    }

    /** Returns a {@link HttpDataSource.Factory}. */
    public HttpDataSource.Factory buildHttpDataSourceFactory() {
        return new DefaultHttpDataSourceFactory(userAgent);
    }


    private void changeChannelUp() {
        Log.d("debug", "CHANNEL_UP - Current Channel ID: " + currentChannelId);
        int wantedIndex = getChannelListIndexForChannelId(currentChannelId);
        Log.d("debug", "CHANNEL_UP - Found index: " + wantedIndex);
        if (wantedIndex == -1) {
            // Show error
            return;
        }

        wantedIndex++; // Next channel up
        if (wantedIndex >= this.channelList.size()) {
            wantedIndex = 0;
        }
        Log.d("debug", "CHANNEL_UP - Next index: " + wantedIndex);
        this.currentChannelId = this.channelList.get(wantedIndex).getChannelId();
        Log.d("debug", "CHANNEL_UP - new channelID: " + this.currentChannelId);
        this.prepareForChannel(currentChannelId);
    }

    private void changeChannelDown() {
        Log.d("debug", "CHANNEL_DOWN - Current Channel ID: " + currentChannelId);
        int wantedIndex = getChannelListIndexForChannelId(currentChannelId);
        Log.d("debug", "CHANNEL_DOWN - Found index: " + wantedIndex);


        if (wantedIndex == -1) {
            // Show error
            return;
        }

        wantedIndex--; // Next channel up
        if (wantedIndex < 0) {
            wantedIndex = this.channelList.size() - 1;
        }
        Log.d("debug", "CHANNEL_DOWN - Next index: " + wantedIndex);
        this.currentChannelId = this.channelList.get(wantedIndex).getChannelId();
        Log.d("debug", "CHANNEL_DOWN - new channelID: " + this.currentChannelId);
        this.prepareForChannel(currentChannelId);
    }

    private int getChannelListIndexForChannelId(Integer channelId) {
        int max = this.channelList.size();
        int count = 0;

        while (count < max) {
            if (this.channelList.get(count).getChannelId().equals(this.currentChannelId)) {
                return count;
            }
            count++;
        }
        return -1;
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                changeChannelUp();
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                changeChannelDown();
                // channel down
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
                makeInfoToast();
                break;
        }
        return true;
    }

    private void makeInfoToast() {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.cust_toast_layout,
                (ViewGroup)findViewById(R.id.relativeLayout1));
        ImageView imageView = view.findViewById(R.id.imageView1);
        Picasso picasso = new Picasso.Builder(this.getApplicationContext()).loggingEnabled(true).build();
        RequestCreator a = picasso.load(currentProgram.getThumbUrl());
        a.resize(100, 100).centerCrop().into(imageView);


        TextView titleView = view.findViewById(R.id.textTitle);
        titleView.setText(currentProgram.getTitleName());
        TextView descView = view.findViewById(R.id.textDescription);
        descView.setText(currentProgram.getTitleDescription());

        Toast toast = new Toast(this);
        toast.setView(view);
        toast.show();


/*
        Toast.makeText(MainActivity.this, "key pressed! = " + key , Toast.LENGTH_SHORT).show();
*/

    }


    @Override
    public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        Log.d("Main", "Tracks changed...");
        this.currentProgramIndex++;
        if (this.channelList != null && this.currentProgramIndex < this.currentPrograms.size()) {
            this.currentProgram = this.currentPrograms.get(this.currentProgramIndex);
        }
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == Player.STATE_IDLE || playbackState == Player.STATE_ENDED ||
                !playWhenReady) {

            playerView.setKeepScreenOn(false);
        } else { // STATE_IDLE, STATE_ENDED
            // This prevents the screen from getting dim/lock
            playerView.setKeepScreenOn(true);
        }
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        Log.e("Main", "Error playing exception: " + error.getMessage());
        player.stop(true);
    }

    @Override
    public void onPositionDiscontinuity(int reason) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onSeekProcessed() {

    }
}

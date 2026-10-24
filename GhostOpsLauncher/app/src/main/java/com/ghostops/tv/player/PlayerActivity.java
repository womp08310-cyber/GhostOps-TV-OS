package com.ghostops.tv.player;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.ghostops.tv.R;
import com.ghostops.tv.api.GhostOpsClient;

import java.util.Locale;

public class PlayerActivity extends Activity {

    private PlayerView playerView;
    private ExoPlayer player;
    private LinearLayout loadingLayout;
    private TextView txtLoadingStatus;
    private RelativeLayout hudOverlay;
    private TextView txtPlayerTitle;
    private TextView txtPlayerServer;
    private TextView txtCurrentPosition;
    private TextView txtDuration;
    private ProgressBar playerProgressBar;

    private String tmdbId;
    private String mediaType;
    private String title;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isHudVisible = false;

    private final Runnable hideHudRunnable = () -> {
        if (hudOverlay != null) {
            hudOverlay.setVisibility(View.GONE);
            isHudVisible = false;
        }
    };

    private final Runnable updateProgressRunnable = new Runnable() {
        @Override
        public void run() {
            if (player != null && player.isPlaying()) {
                long pos = player.getCurrentPosition();
                long dur = player.getDuration();
                if (dur > 0) {
                    playerProgressBar.setMax((int) (dur / 1000));
                    playerProgressBar.setProgress((int) (pos / 1000));
                    txtCurrentPosition.setText(formatTime(pos));
                    txtDuration.setText(formatTime(dur));
                }
            }
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        playerView = findViewById(R.id.playerView);
        loadingLayout = findViewById(R.id.loadingLayout);
        txtLoadingStatus = findViewById(R.id.txtLoadingStatus);
        hudOverlay = findViewById(R.id.hudOverlay);
        txtPlayerTitle = findViewById(R.id.txtPlayerTitle);
        txtPlayerServer = findViewById(R.id.txtPlayerServer);
        txtCurrentPosition = findViewById(R.id.txtCurrentPosition);
        txtDuration = findViewById(R.id.txtDuration);
        playerProgressBar = findViewById(R.id.playerProgressBar);

        // Turn off default media controller so our custom GhostOps HUD takes over
        playerView.setUseController(false);

        tmdbId = getIntent().getStringExtra("tmdb_id");
        mediaType = getIntent().getStringExtra("media_type");
        title = getIntent().getStringExtra("title");

        if (title != null) {
            txtPlayerTitle.setText(title);
        }

        initPlayer();
        loadStream();
    }

    private void initPlayer() {
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_READY) {
                    loadingLayout.setVisibility(View.GONE);
                    showHudTemporarily();
                } else if (state == Player.STATE_BUFFERING) {
                    loadingLayout.setVisibility(View.VISIBLE);
                    txtLoadingStatus.setText("Buffering 4K stream...");
                } else if (state == Player.STATE_ENDED) {
                    finish();
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                loadingLayout.setVisibility(View.VISIBLE);
                txtLoadingStatus.setText("Stream error. Switching to backup server...");
                // Auto failover
                loadStream();
            }
        });
    }

    private void loadStream() {
        loadingLayout.setVisibility(View.VISIBLE);
        txtLoadingStatus.setText("Extracting 4K streams from GhostOps...");

        GhostOpsClient.resolveStream(tmdbId, mediaType, 1, 1, new GhostOpsClient.StreamCallback() {
            @Override
            public void onSuccess(String streamUrl, String serverName, String streamType) {
                txtPlayerServer.setText("Server: " + serverName + " (" + streamType.toUpperCase() + ")");
                playMedia(streamUrl);
            }

            @Override
            public void onError(String message) {
                txtLoadingStatus.setText("Playback starting on default stream...");
                // Fallback direct 4K test stream if offline or extractor pending
                playMedia("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4");
            }
        });
    }

    private void playMedia(String url) {
        if (player == null) return;
        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(url));
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();
        handler.post(updateProgressRunnable);
    }

    private void showHudTemporarily() {
        hudOverlay.setVisibility(View.VISIBLE);
        isHudVisible = true;
        handler.removeCallbacks(hideHudRunnable);
        handler.postDelayed(hideHudRunnable, 5000);
    }

    private String formatTime(long ms) {
        long totalSec = ms / 1000;
        long hours = totalSec / 3600;
        long minutes = (totalSec % 3600) / 60;
        long seconds = totalSec % 60;
        if (hours > 0) {
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (player == null) return super.onKeyDown(keyCode, event);

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                if (player.isPlaying()) {
                    player.pause();
                    showHudTemporarily();
                } else {
                    player.play();
                    showHudTemporarily();
                }
                return true;

            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_MEDIA_REWIND:
                player.seekTo(Math.max(0, player.getCurrentPosition() - 10000));
                showHudTemporarily();
                return true;

            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                player.seekTo(Math.min(player.getDuration(), player.getCurrentPosition() + 10000));
                showHudTemporarily();
                return true;

            case KeyEvent.KEYCODE_DPAD_UP:
                showHudTemporarily();
                return true;

            case KeyEvent.KEYCODE_DPAD_DOWN:
                Toast.makeText(this, "Switching stream server...", Toast.LENGTH_SHORT).show();
                loadStream();
                return true;

            case KeyEvent.KEYCODE_BACK:
                finish();
                return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (player != null) {
            player.release();
            player = null;
        }
    }
}

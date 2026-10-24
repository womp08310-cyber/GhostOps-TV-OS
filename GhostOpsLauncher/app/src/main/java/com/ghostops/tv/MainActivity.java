package com.ghostops.tv;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.ghostops.tv.api.GhostOpsClient;
import com.ghostops.tv.api.MediaItem;
import com.ghostops.tv.inputs.TvInputHelper;
import com.ghostops.tv.player.PlayerActivity;
import com.ghostops.tv.ui.AppDockAdapter;
import com.ghostops.tv.ui.CinemaRailAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    private ImageView imgAmbientBackdrop;
    private ImageView imgHeroBackdrop;
    private TextView txtHeroBadge;
    private TextView txtHeroTitle;
    private TextView txtHeroMeta;
    private TextView txtHeroOverview;
    private TextView btnHeroWatch;

    private TextView txtClock;
    private TextView txtDate;
    private TextView btnInputs;
    private TextView btnWifi;
    private TextView btnSettings;

    private RecyclerView rvTrending;
    private RecyclerView rvMasterpieces;
    private RecyclerView rvAnime;
    private RecyclerView rvApps;

    private MediaItem currentHeroItem;
    private final Handler clockHandler = new Handler(Looper.getMainLooper());

    private final Runnable clockRunnable = new Runnable() {
        @Override
        public void run() {
            updateClockAndNetwork();
            clockHandler.postDelayed(this, 30000); // Update every 30s
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();
        setupTopBar();
        setupHeroSpotlight();
        setupRails();
        loadInstalledApps();

        clockHandler.post(clockRunnable);
    }

    private void bindViews() {
        imgAmbientBackdrop = findViewById(R.id.imgAmbientBackdrop);
        imgHeroBackdrop = findViewById(R.id.imgHeroBackdrop);
        txtHeroBadge = findViewById(R.id.txtHeroBadge);
        txtHeroTitle = findViewById(R.id.txtHeroTitle);
        txtHeroMeta = findViewById(R.id.txtHeroMeta);
        txtHeroOverview = findViewById(R.id.txtHeroOverview);
        btnHeroWatch = findViewById(R.id.btnHeroWatch);

        txtClock = findViewById(R.id.txtClock);
        txtDate = findViewById(R.id.txtDate);
        btnInputs = findViewById(R.id.btnInputs);
        btnWifi = findViewById(R.id.btnWifi);
        btnSettings = findViewById(R.id.btnSettings);

        rvTrending = findViewById(R.id.rvTrending);
        rvMasterpieces = findViewById(R.id.rvMasterpieces);
        rvAnime = findViewById(R.id.rvAnime);
        rvApps = findViewById(R.id.rvApps);
    }

    private void setupTopBar() {
        // TV Inputs Button (HDMI 1-4, AV, Tuner)
        btnInputs.setOnClickListener(v -> TvInputHelper.showInputPicker(MainActivity.this));

        // Wi-Fi Status Button
        btnWifi.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            } catch (Exception e) {
                Toast.makeText(this, "Opening Network Settings...", Toast.LENGTH_SHORT).show();
            }
        });

        // TV Settings Button
        btnSettings.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Settings.ACTION_SETTINGS));
            } catch (Exception e) {
                Toast.makeText(this, "Opening System Settings...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateClockAndNetwork() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("• EEE, MMM d", Locale.getDefault());
        Date now = new Date();
        txtClock.setText(timeFormat.format(now));
        txtDate.setText(dateFormat.format(now));

        // Network status
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm != null ? cm.getActiveNetworkInfo() : null;
            if (activeNetwork != null && activeNetwork.isConnected()) {
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                    WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    WifiInfo winfo = wm != null ? wm.getConnectionInfo() : null;
                    String ssid = winfo != null ? winfo.getSSID().replace("\"", "") : "Connected";
                    btnWifi.setText("Wi-Fi: " + (ssid.isEmpty() ? "Connected" : ssid));
                } else if (activeNetwork.getType() == ConnectivityManager.TYPE_ETHERNET) {
                    btnWifi.setText("Ethernet: Connected");
                } else {
                    btnWifi.setText("Online");
                }
            } else {
                btnWifi.setText("Wi-Fi: Offline");
            }
        } catch (Exception e) {
            btnWifi.setText("Wi-Fi: Connected");
        }
    }

    private void setupHeroSpotlight() {
        List<MediaItem> masterpieces = GhostOpsClient.getCuratedMasterpieces();
        if (!masterpieces.isEmpty()) {
            setHeroItem(masterpieces.get(0));
        }

        btnHeroWatch.setOnClickListener(v -> {
            if (currentHeroItem != null) {
                launchPlayer(currentHeroItem);
            }
        });
    }

    private void setHeroItem(MediaItem item) {
        currentHeroItem = item;
        txtHeroTitle.setText(item.getTitle());
        txtHeroOverview.setText(item.getOverview());
        txtHeroMeta.setText(item.getYear() + " • " + (item.getMediaType().equals("tv") ? "Series" : "Movie") + " • " + item.getVoteAverage() + " ★");
        txtHeroBadge.setText("★ " + item.getBadge());

        Glide.with(this)
                .load(item.getBackdropUrl())
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imgHeroBackdrop);

        Glide.with(this)
                .load(item.getBackdropUrl())
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imgAmbientBackdrop);
    }

    private void setupRails() {
        // Rail 1: Trending Now
        List<MediaItem> trending = GhostOpsClient.getCuratedTrending();
        rvTrending.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvTrending.setAdapter(new CinemaRailAdapter(this, trending, new CinemaRailAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MediaItem item) {
                launchPlayer(item);
            }

            @Override
            public void onItemFocused(MediaItem item) {
                setHeroItem(item);
            }
        }));

        // Rail 2: Critically Acclaimed Masterpieces
        List<MediaItem> masterpieces = GhostOpsClient.getCuratedMasterpieces();
        rvMasterpieces.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvMasterpieces.setAdapter(new CinemaRailAdapter(this, masterpieces, new CinemaRailAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MediaItem item) {
                launchPlayer(item);
            }

            @Override
            public void onItemFocused(MediaItem item) {
                setHeroItem(item);
            }
        }));

        // Rail 3: Anime Universe
        List<MediaItem> anime = GhostOpsClient.getCuratedAnime();
        rvAnime.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvAnime.setAdapter(new CinemaRailAdapter(this, anime, new CinemaRailAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MediaItem item) {
                launchPlayer(item);
            }

            @Override
            public void onItemFocused(MediaItem item) {
                setHeroItem(item);
            }
        }));
    }

    private void loadInstalledApps() {
        PackageManager pm = getPackageManager();
        List<AppDockAdapter.AppItem> appList = new ArrayList<>();

        // Query Leanback TV launcher apps
        Intent leanbackIntent = new Intent(Intent.ACTION_MAIN, null);
        leanbackIntent.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
        List<ResolveInfo> activities = pm.queryIntentActivities(leanbackIntent, 0);

        // Fallback to standard launcher if leanback apps list is sparse
        if (activities.isEmpty()) {
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            activities = pm.queryIntentActivities(mainIntent, 0);
        }

        for (ResolveInfo ri : activities) {
            String pkg = ri.activityInfo.packageName;
            // Exclude self from the app list
            if (pkg.equals(getPackageName())) continue;

            String label = ri.loadLabel(pm).toString();
            android.graphics.drawable.Drawable icon = ri.loadIcon(pm);
            Intent launchIntent = pm.getLeanbackLaunchIntentForPackage(pkg);
            if (launchIntent == null) {
                launchIntent = pm.getLaunchIntentForPackage(pkg);
            }

            appList.add(new AppDockAdapter.AppItem(label, pkg, icon, launchIntent));
        }

        rvApps.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvApps.setAdapter(new AppDockAdapter(this, appList));
    }

    private void launchPlayer(MediaItem item) {
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("tmdb_id", item.getId());
        intent.putExtra("media_type", item.getMediaType());
        intent.putExtra("title", item.getTitle());
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Prevent Back button from accidentally closing the TV OS Home
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clockHandler.removeCallbacksAndMessages(null);
    }
}

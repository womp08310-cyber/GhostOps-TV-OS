package com.ghostops.tv.api;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GhostOpsClient {
    private static final String TAG = "GhostOpsClient";
    private static final String BASE_URL = "https://ghostops.live";
    private static final OkHttpClient client = new OkHttpClient.Builder().build();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface StreamCallback {
        void onSuccess(String streamUrl, String serverName, String streamType);
        void onError(String message);
    }

    /**
     * Resolves playable HLS or DASH stream URL from GhostOps streaming API
     */
    public static void resolveStream(String tmdbId, String mediaType, int season, int episode, StreamCallback callback) {
        new Thread(() -> {
            try {
                // Primary servers in order of performance
                String[] servers = {"sirius", "darkmatter", "quasar", "apollo"};
                
                JSONObject payload = new JSONObject();
                payload.put("id", tmdbId);
                payload.put("t", "tv".equalsIgnoreCase(mediaType) ? "t" : "m");
                if ("tv".equalsIgnoreCase(mediaType)) {
                    payload.put("query", season + "-" + episode);
                }

                boolean found = false;
                for (String srv : servers) {
                    payload.put("srv", srv);
                    RequestBody body = RequestBody.create(
                            payload.toString(),
                            MediaType.parse("application/json; charset=utf-8")
                    );

                    Request request = new Request.Builder()
                            .url(BASE_URL + "/api/stream")
                            .header("User-Agent", "GhostOps-TV-OS/1.0 (Android TV)")
                            .header("Referer", "https://ghostops.live")
                            .post(body)
                            .build();

                    try (Response response = client.newCall(request).execute()) {
                        if (response.isSuccessful() && response.body() != null) {
                            String respStr = response.body().string();
                            JSONObject json = new JSONObject(respStr);
                            if (json.has("sources")) {
                                JSONArray sources = json.getJSONArray("sources");
                                if (sources.length() > 0) {
                                    JSONObject source = sources.getJSONObject(0);
                                    String streamUrl = source.optString("url");
                                    String type = source.optString("type", "hls");
                                    String name = source.optString("name", srv.toUpperCase());
                                    if (streamUrl != null && !streamUrl.isEmpty()) {
                                        mainHandler.post(() -> callback.onSuccess(streamUrl, name, type));
                                        found = true;
                                        break;
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Server " + srv + " failed, trying next: " + e.getMessage());
                    }
                }

                if (!found) {
                    // Fallback to high-res trailer or direct test stream
                    mainHandler.post(() -> callback.onError("Servers currently extracting. Please retry."));
                }
            } catch (Exception e) {
                Log.e(TAG, "Stream resolution error: ", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        }).start();
    }

    /**
     * Curated Masterpieces Rail (Immediate zero-latency display)
     */
    public static List<MediaItem> getCuratedMasterpieces() {
        List<MediaItem> items = new ArrayList<>();
        items.add(new MediaItem("157336", "Interstellar", "A team of explorers travel through a wormhole in space in an attempt to ensure humanity's survival.",
                "/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg", "/xJHokMbljvjADYdit5fK5VQsXEG.jpg", "2014-11-05", "movie", 8.4, "4K ULTRA HD"));
        items.add(new MediaItem("27205", "Inception", "Cobb steals information from targets by entering their dreams.",
                "/oYuLEt3zVCKq57qu2F8dT7NIa6f.jpg", "/8ZTVqvKDQ8emSGUEMjsS4yHAwrp.jpg", "2010-07-16", "movie", 8.4, "4K MASTER"));
        items.add(new MediaItem("155", "The Dark Knight", "Batman raises the stakes in his war on crime against the Joker.",
                "/qJ2tW6WMUDux911r6m7haRef0WH.jpg", "/hkBaDkMWbLaf8B1r5vsIRqqXubm.jpg", "2008-07-18", "movie", 8.5, "IMAX 4K"));
        items.add(new MediaItem("13", "Forrest Gump", "The history of the United States from the 1950s to the '70s unfolds from the perspective of an Alabama man.",
                "/arw2vcBveWOVZr6pxd9XTd1TdQa.jpg", "/qdIMHd4sEfJSckfVJfKQvisL02a.jpg", "1994-06-23", "movie", 8.5, "4K REMASTER"));
        items.add(new MediaItem("680", "Pulp Fiction", "The lives of two mob hitmen, a boxer, and a pair of diner bandits intertwine in four tales of violence and redemption.",
                "/d5iIlFn5s0ImszYzBPb8JPIfbXD.jpg", "/suaEOtk1N1sgg2MTM7oZd2cfVp3.jpg", "1994-09-10", "movie", 8.5, "4K HDR"));
        items.add(new MediaItem("238", "The Godfather", "The aging patriarch of an organized crime dynasty transfers control of his clandestine empire to his reluctant son.",
                "/3bhkrj58Vtu7enYsRolD1fZdja1.jpg", "/tmU7GeKVybMWFButWEGl2M4GeiP.jpg", "1972-03-14", "movie", 8.7, "4K VAULT"));
        return items;
    }

    /**
     * Curated Trending Rail
     */
    public static List<MediaItem> getCuratedTrending() {
        List<MediaItem> items = new ArrayList<>();
        items.add(new MediaItem("693134", "Dune: Part Two", "Paul Atreides unites with Chani and the Fremen while seeking revenge against the conspirators who destroyed his family.",
                "/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg", "/xOMo8BRK7PfcJv9JCnx7s520Wio.jpg", "2024-02-27", "movie", 8.2, "TRENDING #1"));
        items.add(new MediaItem("872585", "Oppenheimer", "The story of J. Robert Oppenheimer's role in the development of the atomic bomb during World War II.",
                "/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg", "/nb3xI8XI3w4pMVZ38VijbsyBqP4.jpg", "2023-07-19", "movie", 8.1, "4K HDR"));
        items.add(new MediaItem("533535", "Deadpool & Wolverine", "A listless Wade Wilson toils in civilian life until a common enemy arises.",
                "/8cdWjvZQUExUUTzyp4t6EDMubfO.jpg", "/yDHYTfa29faC475OX9G7r5s8r6g.jpg", "2024-07-24", "movie", 7.8, "NEW IN 4K"));
        items.add(new MediaItem("1022789", "Inside Out 2", "Teenager Riley's mind headquarters undergoes a sudden demolition to make room for new Emotions.",
                "/vpnVM9B6NMmQpWeZvzLvDESb2QY.jpg", "/stKGOmPAg44mNduI4b2mZ6b2jW9.jpg", "2024-06-11", "movie", 7.6, "TRENDING"));
        items.add(new MediaItem("912649", "Venom: The Last Dance", "Eddie and Venom are on the run. Hunted by both of their worlds and with the net closing in.",
                "/aosm8NMQ3UzyAcgTEv5352w8n7K.jpg", "/3V4kLQg0kSqPLctI5ziYWgAZYjX.jpg", "2024-10-22", "movie", 7.4, "NEW RELEASE"));
        return items;
    }

    /**
     * Curated Anime Universe Rail
     */
    public static List<MediaItem> getCuratedAnime() {
        List<MediaItem> items = new ArrayList<>();
        items.add(new MediaItem("85937", "Demon Slayer: Kimetsu no Yaiba", "Tanjiro sets out on a perilous journey to seek a cure and avenge his family.",
                "/xUfRZu2mi8jH69QCG95GzwhGzyq.jpg", "/nTvM4mhqZlHIymIlokDaoRd6mAc.jpg", "2019-04-06", "tv", 8.7, "ANIME 4K"));
        items.add(new MediaItem("1429", "Attack on Titan", "Humanity fights for survival against man-eating giants behind colossal walls.",
                "/hTP1DtLGFamjfu8WqjnuQdP1n4i.jpg", "/75mafsGz84XU1A0X1eF567x8a1b.jpg", "2013-04-07", "tv", 8.8, "LEGENDARY"));
        items.add(new MediaItem("37854", "One Piece", "Monkey D. Luffy sets off on an adventure with his pirate crew in search of the legendary treasure.",
                "/fcXdJlbSdUEeMSJFsXKsznGwwok.jpg", "/4MCKNAc6AbWjEsM2h9Xc2964f43.jpg", "1999-10-20", "tv", 8.9, "VAULT TOP 1"));
        items.add(new MediaItem("94605", "Jujutsu Kaisen", "Yuji Itadori enters a high school of sorcerers to defeat deadly curses.",
                "/fHpKW29E499Dwf14W041xWfM0f6.jpg", "/gmECXPjvAq9w65S8p6tU09g6h0w.jpg", "2020-10-03", "tv", 8.6, "4K REMASTER"));
        items.add(new MediaItem("30984", "Bleach", "Ichigo Kurosaki gains the powers of a Soul Reaper to protect spirits.",
                "/2EewsolmgvSGb5AlPwhj6q7yBw4.jpg", "/qtfMr08K5R9gvdjk5bW5i9A1iG4.jpg", "2004-10-05", "tv", 8.4, "ANIME CLASSIC"));
        return items;
    }
}

package com.ghostops.tv.api;

import java.io.Serializable;

public class MediaItem implements Serializable {
    private String id;
    private String title;
    private String overview;
    private String posterPath;
    private String backdropPath;
    private String releaseDate;
    private String mediaType; // "movie" or "tv"
    private double voteAverage;
    private String badge;

    public MediaItem(String id, String title, String overview, String posterPath, String backdropPath, String releaseDate, String mediaType, double voteAverage, String badge) {
        this.id = id;
        this.title = title;
        this.overview = overview;
        this.posterPath = posterPath;
        this.backdropPath = backdropPath;
        this.releaseDate = releaseDate;
        this.mediaType = mediaType;
        this.voteAverage = voteAverage;
        this.badge = badge;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getOverview() { return overview; }
    public String getPosterUrl() {
        if (posterPath == null || posterPath.isEmpty()) return "";
        if (posterPath.startsWith("http")) return posterPath;
        return "https://image.tmdb.org/t/p/w500" + posterPath;
    }
    public String getBackdropUrl() {
        if (backdropPath == null || backdropPath.isEmpty()) return "";
        if (backdropPath.startsWith("http")) return backdropPath;
        return "https://image.tmdb.org/t/p/original" + backdropPath;
    }
    public String getReleaseDate() { return releaseDate; }
    public String getYear() {
        if (releaseDate != null && releaseDate.length() >= 4) {
            return releaseDate.substring(0, 4);
        }
        return "";
    }
    public String getMediaType() { return mediaType; }
    public double getVoteAverage() { return voteAverage; }
    public String getBadge() { return badge; }
}

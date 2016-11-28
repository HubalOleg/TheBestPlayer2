package com.oleg.hubal.thebestplayer.model;

/**
 * Created by User on 22.11.2016.
 */

public class TrackItem extends SelectableItem {

    private String path;
    private String albumImage;
    private String artist;
    private String title;
    private long duration;

    public TrackItem(String path, String albumImage, String artist, String title, long duration) {
        this.path = path;
        this.albumImage = albumImage;
        this.artist = artist;
        this.title = title;
        this.duration = duration;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbumImage() {
        return albumImage;
    }

    public void setAlbumImage(String albumImage) {
        this.albumImage = albumImage;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}

package com.oleg.hubal.thebestplayer.utility;

import android.provider.MediaStore;

/**
 * Created by User on 22.11.2016.
 */

public class Constants {
    public static final String SORT_BY_DURATION = "Duration";
    public static final String SORT_BY_ARTIST = "Artist";
    public static final String SORT_BY_TITLE = "Title";

    public static final String SEARCH_BY_ARTIST = "Artist";
    public static final String SEARCH_BY_TITLE = "Title";

    public static final String SEARCH_ARTIST_SELECTION = MediaStore.Audio.AudioColumns.ARTIST + " LIKE ?";
    public static final String SEARCH_TITLE_SELECTION = MediaStore.Audio.AudioColumns.TITLE + " LIKE ?";
}

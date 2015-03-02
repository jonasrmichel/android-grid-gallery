package com.jonas.demo.gallery;

public class Constants {
    /**
     * The Flickr API key.
     */
    public static final String FLICKR_API_KEY = "a4ea7593aa4fb860a9f7c6592f7c5855";

    /**
     * The number of photos to load per page.
     */
    public static final int PHOTOS_PER_PAGE = 20;

    /**
     * A new page load is triggered when fewer than this number
     * of photos remain in the bottom of the grid.
     */
    public static final int LOAD_MORE_WHEN_REMAINING = 60;

    /**
     * Whether or not to show a loading spinner in the action bar.
     */
    public static final boolean SHOW_SPINNER = true;
}

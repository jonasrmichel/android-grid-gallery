package com.jonas.demo.gallery;

import android.content.AsyncTaskLoader;
import android.content.Context;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mathew Kamkar (https://github.com/matkam/ImageSearch-Android).
 *
 * Modified by Jonas Michel.
 */
public class FlickrSearchLoader extends AsyncTaskLoader<List<FlickrPhoto>> {

    public static final int LOADER_ID = 0;

    /** Search URL parameters. */
    private static final String BASE_URL = "https://api.flickr.com/services/rest/?method=flickr.photos.search";
    private static final String API_KEY = "&api_key=" + Constants.FLICKR_API_KEY;
    private static final String PER_PAGE = "&per_page=" + Integer.toString(Constants.PHOTOS_PER_PAGE);
    private static final String NOJSONCALLBACK = "&nojsoncallback=1";
    private static final String FORMAT = "&format=json";
    private static final String TEXT = "&text=";
    private static final String PAGE = "&page=";

    private String searchUrl;

    public FlickrSearchLoader(Context context, String searchString, int page) {
        super(context);

        String encodedSearchString = "";
        if (searchString != null)
            try {
                encodedSearchString = URLEncoder.encode(searchString, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        searchUrl = BASE_URL + API_KEY + PER_PAGE + NOJSONCALLBACK + FORMAT + TEXT + encodedSearchString + PAGE + Integer.toString(page);
    }

    @Override
    public List<FlickrPhoto> loadInBackground() {
        List<FlickrPhoto> flickrPhotos = new ArrayList<FlickrPhoto>();

        try {
            HttpClient httpClient = new DefaultHttpClient();
            JSONObject flickrJson = getFlickrJson(httpClient);

            JSONObject photosJson = flickrJson.optJSONObject("photos");
            int page = photosJson.getInt(FlickrPhoto.PROP_PAGE);
            int pages = photosJson.getInt(FlickrPhoto.PROP_PAGES);
            JSONArray photoArray = photosJson != null ? photosJson.getJSONArray("photo") : new JSONArray();

            int start = (page - 1) * Constants.PHOTOS_PER_PAGE;
            for (int i = 0; i < photoArray.length(); i++) {
                JSONObject photoJson = (JSONObject) photoArray.get(i);

                FlickrPhoto flickrPhoto = new FlickrPhoto(photoJson, page, pages);

                if (flickrPhoto.isValid()) {
                    flickrPhoto.download(httpClient, start + i);
                    flickrPhotos.add(flickrPhoto);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return flickrPhotos;
    }

    /**
     * Executes a Flickr image search.
     *
     * @param httpClient
     * @return
     * @throws IOException
     * @throws JSONException
     */
    private JSONObject getFlickrJson(HttpClient httpClient) throws IOException, JSONException {
        HttpGet flickrGet = new HttpGet(searchUrl);

        HttpEntity httpEntity = httpClient.execute(flickrGet).getEntity();
        InputStream inputStream = httpEntity.getContent();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
        StringBuilder sb = new StringBuilder();

        String line = null;
        while ((line = reader.readLine()) != null)
            sb.append(line + "\n");

        return new JSONObject(sb.toString());
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }
}

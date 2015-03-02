package com.jonas.demo.gallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Mathew Kamkar (https://github.com/matkam/ImageSearch-Android).
 * <p/>
 * Modified by Jonas Michel.
 */
public class FlickrPhoto implements Parcelable {
    public static final String PROP_ID = "id";
    public static final String PROP_OWNER = "owner";
    public static final String PROP_SECRET = "secret";
    public static final String PROP_SERVER = "server";
    public static final String PROP_FARM = "farm";
    public static final String PROP_TITLE = "title";
    public static final String PROP_PAGE = "page";
    public static final String PROP_PAGES = "pages";

    private String id; //Required
    private String owner;
    private String secret; //Required
    private String server; //Required
    private int farm = -1; //Required
    private String title;
    private int page;
    private int pages;

    private String localImageLocation;

    public FlickrPhoto() {
        // no-arg creator
    }

    public FlickrPhoto(Parcel in) {
        id = in.readString();
        owner = in.readString();
        secret = in.readString();
        server = in.readString();
        farm = in.readInt();
        title = in.readString();
        page = in.readInt();
        pages = in.readInt();
        localImageLocation = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(id);
        out.writeString(owner);
        out.writeString(secret);
        out.writeString(server);
        out.writeInt(farm);
        out.writeString(title);
        out.writeInt(page);
        out.writeInt(pages);
        out.writeString(localImageLocation);
    }

    public static final Creator CREATOR = new Creator() {
        public FlickrPhoto createFromParcel(Parcel in) {
            return new FlickrPhoto(in);
        }

        public FlickrPhoto[] newArray(int size) {
            return new FlickrPhoto[size];
        }
    };

    public FlickrPhoto(JSONObject photoJson, int page, int pages) {
        id = photoJson.optString(PROP_ID);
        owner = photoJson.optString(PROP_OWNER);
        secret = photoJson.optString(PROP_SECRET);
        server = photoJson.optString(PROP_SERVER);
        farm = photoJson.optInt(PROP_FARM, -1);
        title = photoJson.optString(PROP_TITLE);
        this.page = page;
        this.pages = pages;
    }

    public boolean isValid() {
        return id != null && !id.isEmpty() &&
                secret != null && !secret.isEmpty() &&
                server != null && !server.isEmpty() &&
                farm != -1;
    }

    /**
     * Downloads a single image from Flickr and saves it to the device's SD card.
     *
     * @param httpClient
     * @param i
     * @throws IOException
     */
    public void download(HttpClient httpClient, int i) throws IOException {
        HttpGet getPhoto = new HttpGet(generateDownloadUrl());
        HttpEntity photoEntity = httpClient.execute(getPhoto).getEntity();
        InputStream in = photoEntity.getContent();

        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File(sdCard.getAbsolutePath() + "/imagesearch");
        dir.mkdirs();
        File file = new File(dir, i + ".jpg");

        FileOutputStream f = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        int len1 = 0;
        while ((len1 = in.read(buffer)) > 0) {
            f.write(buffer, 0, len1);
        }
        f.close();

        localImageLocation = file.getAbsolutePath();
    }

    /**
     * Generates the download URL for the Flickr image.
     *
     * @return
     */
    private String generateDownloadUrl() {
        return "https://farm" + farm + ".staticflickr.com/" + server + "/" + id + "_" + secret + ".jpg";
    }

    public Bitmap getLocalImage() {
        return BitmapFactory.decodeFile(localImageLocation);
    }

    public String getId() {
        return id;
    }

    public String getOwner() {
        return owner;
    }

    public String getSecret() {
        return secret;
    }

    public String getServer() {
        return server;
    }

    public int getFarm() {
        return farm;
    }

    public String getTitle() {
        return title;
    }

    public int getPage() {
        return page;
    }

    public int getPages() {
        return pages;
    }

    public String getLocalImageLocation() {
        return localImageLocation;
    }
}

package com.jonas.demo.gallery;

import android.app.Fragment;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class DisplayFragment extends Fragment {

    public static final String KEY_PHOTO_LOCATION = "picture";
    public static final String KEY_PHOTO_TITLE = "title";

    /** UI elements. */
    private ImageView mImageViewPicture;
    private TextView mTextViewDetails;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_display, container, false);

        mImageViewPicture = (ImageView) rootView.findViewById(R.id.imageViewPicture);
        mTextViewDetails = (TextView) rootView.findViewById(R.id.textViewDetails);

        Bundle bundle = getArguments();
        String photoLocation = bundle.getString(KEY_PHOTO_LOCATION);
        String title = bundle.getString(KEY_PHOTO_TITLE);

        mImageViewPicture.setImageBitmap(BitmapFactory.decodeFile(photoLocation));
        mTextViewDetails.setText(title);

        return rootView;
    }


}

package com.jonas.demo.gallery;

import android.app.Activity;
import android.os.Bundle;

public class DisplayImageActivity extends Activity {

    DisplayFragment mDisplayFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        mDisplayFragment = new DisplayFragment();

        // forward the intent along to the fragment
        mDisplayFragment.setArguments(getIntent().getExtras());
        getFragmentManager().beginTransaction()
                .add(R.id.container, mDisplayFragment)
                .commit();

    }

}

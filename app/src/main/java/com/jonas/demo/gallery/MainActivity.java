package com.jonas.demo.gallery;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;


public class MainActivity extends Activity {

    public static Context context;

    private ImageGridFragment mImageGridFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            mImageGridFragment = new ImageGridFragment();
            getFragmentManager().beginTransaction()
                    .add(R.id.container, mImageGridFragment, ImageGridFragment.TAG)
                    .commit();
        } else {
            mImageGridFragment = (ImageGridFragment)
                    getFragmentManager().findFragmentByTag(ImageGridFragment.TAG);
        }

        MainActivity.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return MainActivity.context;
    }

}

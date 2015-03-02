package com.jonas.demo.gallery;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Point;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ImageGridFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<List<FlickrPhoto>>, AbsListView.OnScrollListener {
    public static final String TAG = ImageGridFragment.class.getName();

    /**
     * Keys used for saving fragment state.
     */
    private static final String KEY_SAVED_PHOTOS = "flickrPhotos";
    private static final String KEY_SAVED_PAGE = "page";
    private static final String KEY_SAVED_LOADING = "isLoading";

    /**
     * UI elements.
     */
    private TextView mEditTextSearch;
    private Button mButtonSearch;
    private GridView mGridView;
    private GridAdapter mGridAdapter;

    /**
     * The user's search string.
     */
    private String mSearchString;

    /**
     * The image page counter.
     */
    private int mPage;

    /**
     * Whether or not we are loading images.
     */
    private boolean mIsLoading;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setRetainInstance(true);

        // initialize the fragment's UI elements
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mEditTextSearch = (EditText) rootView.findViewById(R.id.editTextSearch);
        mButtonSearch = (Button) rootView.findViewById(R.id.buttonSearch);
        mButtonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchString = mEditTextSearch.getText().toString().trim();
                search(searchString);
            }
        });

        // restore any previous fragment state
        ArrayList<FlickrPhoto> photos = null;
        if (savedInstanceState != null) {
            photos = savedInstanceState.getParcelableArrayList(KEY_SAVED_PHOTOS);
            mGridAdapter = new GridAdapter(getActivity(), photos);
            mPage = savedInstanceState.getInt(KEY_SAVED_PAGE);
            isLoading(savedInstanceState.getBoolean(KEY_SAVED_LOADING));
        }

        if (mGridAdapter == null)
            mGridAdapter = new GridAdapter(getActivity());

        // setup the grid view
        mGridView = (GridView) rootView.findViewById(R.id.gridView);
        mGridView.setAdapter(mGridAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FlickrPhoto photo = (FlickrPhoto) mGridAdapter.getItem(position);

                // switch to a new activity to display the clicked image full size
                Intent intent = new Intent(getActivity(), DisplayImageActivity.class);
                intent.putExtra(DisplayFragment.KEY_PHOTO_LOCATION, photo.getLocalImageLocation());
                intent.putExtra(DisplayFragment.KEY_PHOTO_TITLE, photo.getTitle());
                startActivity(intent);
            }
        });
        mGridView.setOnScrollListener(this);
        mGridView.setNumColumns(getNumColumns());

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // retrieve the loader
        if (!TextUtils.isEmpty(mSearchString))
            getLoaderManager().initLoader(FlickrSearchLoader.LOADER_ID, null, this);
    }

    @Override
    public void onSaveInstanceState(Bundle savedState) {
        super.onSaveInstanceState(savedState);

        // save fragment state
        savedState.putParcelableArrayList(KEY_SAVED_PHOTOS, mGridAdapter.getItems());
        savedState.putInt(KEY_SAVED_PAGE, mPage);
        savedState.putBoolean(KEY_SAVED_LOADING, isLoading());
    }

    @Override
    public Loader onCreateLoader(int i, Bundle bundle) {
        return new FlickrSearchLoader(getActivity(), mSearchString, mPage);
    }

    @Override
    public void onLoadFinished(Loader<List<FlickrPhoto>> listLoader, List<FlickrPhoto> flickrPhotos) {
        isLoading(false);
        mGridAdapter.addAll(flickrPhotos);
    }

    @Override
    public void onLoaderReset(Loader<List<FlickrPhoto>> cursorLoader) {
        // NOP
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // NOP
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        int lastInScreen = firstVisibleItem + visibleItemCount;
        int remainingItemCount = totalItemCount - firstVisibleItem;

        // check if more images need to be loaded
        if (totalItemCount > 0
                && remainingItemCount < Constants.LOAD_MORE_WHEN_REMAINING
                && !isLoading()) {
            FlickrPhoto lastPhoto = ((FlickrPhoto) mGridAdapter.getItem(lastInScreen - 1));
            boolean canLoadMore = lastPhoto.getPage() < lastPhoto.getPages();

            if (!canLoadMore)
                return;

            // load the next page of images
            isLoading(true);
            mPage++;
            getLoaderManager().restartLoader(FlickrSearchLoader.LOADER_ID, null, this);
        }
    }

    public synchronized boolean isLoading() {
        return mIsLoading;
    }

    public synchronized void isLoading(boolean isLoading) {
        if (Constants.SHOW_SPINNER) {
            // show or hide the action bar spinner
            if (isLoading)
                getActivity().setProgressBarIndeterminateVisibility(true);

            else
                getActivity().setProgressBarIndeterminateVisibility(false);
        }

        mIsLoading = isLoading;
    }

    /**
     * Performs a Flickr image search with the user's search string.
     *
     * @param searchString
     */
    private void search(String searchString) {
        if (TextUtils.isEmpty(searchString)) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.enter_some_text),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (searchString.equals(mSearchString) || isLoading())
            return;

        if (!Constants.SHOW_SPINNER)
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.loading),
                    Toast.LENGTH_LONG).show();

        isLoading(true);
        mGridAdapter.clear();
        mPage = 1;
        mSearchString = searchString;
        getLoaderManager().restartLoader(FlickrSearchLoader.LOADER_ID, null, this);

        hideSoftKeyboard();
    }

    /**
     * A convenience method to hide the on screen keyboard.
     */
    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager)
                getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditTextSearch.getWindowToken(), 0);
    }

    /**
     * A convenience method to determine the number of columns to show in the grid view.
     *
     * @return
     */
    private int getNumColumns() {
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        Point size = new Point();
        display.getSize(size);

        float scale = getResources().getDisplayMetrics().density * 175;
        int columns = (int) ((float) size.x / scale);

        return columns;
    }

    /**
     * An adapter that holds the Flickr image data we display in the grid.
     */
    private class GridAdapter extends BaseAdapter {
        private ArrayList<FlickrPhoto> mItems;
        private LayoutInflater mInflater;

        public GridAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
            mItems = new ArrayList<FlickrPhoto>();
        }

        public GridAdapter(Context context, ArrayList<FlickrPhoto> items) {
            this(context);

            if (items != null)
                mItems.addAll(items);
        }

        public ArrayList<FlickrPhoto> getItems() {
            return mItems;
        }

        public void addAll(List<FlickrPhoto> items) {
            mItems.addAll(items);
            notifyDataSetChanged();
        }

        public void clear() {
            mItems.clear();
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public Object getItem(int i) {
            return mItems.get(i);
        }

        @Override
        public long getItemId(int i) {
            return Long.parseLong(mItems.get(i).getId());
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ImageView imageView;

            // be wary of view recycling
            if (view == null) {
                view = mInflater.inflate(R.layout.gridview_item, viewGroup, false);
                imageView = (ImageView) view.findViewById(R.id.squareImageView);

                view.setTag(R.id.squareImageView, imageView);

            } else {
                imageView = (ImageView) view.getTag(R.id.squareImageView);
            }

            FlickrPhoto item = (FlickrPhoto) getItem(i);
            imageView.setImageBitmap(item.getLocalImage());

            return view;
        }

    }

}

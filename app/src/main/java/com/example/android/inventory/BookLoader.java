package com.example.android.inventory;

import android.content.AsyncTaskLoader;
import android.content.Context;

import com.example.android.inventory.utils.QueryUtils;

import java.util.List;

/**
 * Created by sj on 1/14/2018.
 */

public class BookLoader extends AsyncTaskLoader<List<Book>> {

    /** Tag for log messages */
    private static final String LOG_TAG = BookLoader.class.getName();

    /** Query URL */
    private String mUrl;

    public BookLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        // Trigger the loadInBackground() method to execute.
        forceLoad();
    }

    @Override
    public List<Book> loadInBackground() {
        if (mUrl == null) {
            return null;
        }

        List<Book> bookData = QueryUtils.fetchBookData(mUrl);
        return bookData;
    }


}

package com.example.android.inventory.activity;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.android.inventory.Book;
import com.example.android.inventory.BookLoader;
import com.example.android.inventory.R;
import com.example.android.inventory.utils.Constants;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * IsbnActivity implements the LoaderManager.LoaderCallbacks interface in order for Activity to be a
 * client that interacts with the LoaderManager.
 */

public class IsbnActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Book>> {

    /**Constant value for the book loader ID */
    private static final int BOOK_LOADER_ID = 1;

    /** TextView that is displayed when there is no data or when there is no internet connectivity */
    @BindView(R.id.empty_isbn_view) TextView mEmptyTextView;

    /** Loading indicator that is displayed before the first load is completed */
    @BindView(R.id.loading_indicator) View mLoadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_isbn);

        // Bind the view using ButterKnife
        ButterKnife.bind(this);

        // Check for network connectivity and initialize the loader
        initializeLoader(isConnected());

        // Navigate with the app icon in the app bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public Loader<List<Book>> onCreateLoader(int i, Bundle bundle) {
        // Parse breaks apart the URI string that is passed into its parameter
        Uri baseUri = Uri.parse(Constants.BOOK_REQUEST_URL);

        // buildUpon prepares the baseUri that we just parsed so we can add query parameters to it
        Uri.Builder uriBuilder = baseUri.buildUpon();

        // Since starting this activity with data, using getIntent to retrieve this data.
        Intent intent = getIntent();

        // Get String ISBN from the user input in the isbn dialog of the MainActivity
        String isbnStringFromDialog = intent.getStringExtra(getString(R.string.isbn_in_a_dialog));

        // To query a book by ISBN, use "isbn:"
        isbnStringFromDialog = getString(R.string.query_isbn) + isbnStringFromDialog;

        // Append query parameter and its value. (e.g. the 'q=isbn:9780553902808')
        uriBuilder.appendQueryParameter(getString(R.string.q), isbnStringFromDialog);

        // Create a new loader for the given URL
        return new BookLoader(this,  uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<Book>> loader, List<Book> booksData) {
        // Hide loading indicator because the data has been loaded
        mLoadingIndicator.setVisibility(View.GONE);



        // If there is a valid list of {@link Book}, actually in this app there will be one list item,
        // then send the data to the EditorActivity.
        if (booksData != null && !booksData.isEmpty()) {
            // Get the first list item
            Book book = booksData.get(0);
            // Get the title, author, ISBN, publisher of the book
            String title = book.getBookTitle();
            String author = book.getAuthor();
            String isbn = book.getIsbn();
            String publisher = book.getPublisher();

            // Create a new intent to open the {@link EditorActivity}
            Intent intent = new Intent(this, EditorActivity.class);
            // Send the data
            intent.putExtra(getString(R.string.title), title);
            intent.putExtra(getString(R.string.author), author);
            intent.putExtra(getString(R.string.isbn), isbn);
            intent.putExtra(getString(R.string.publisher), publisher);
            // start the new activity
            startActivity(intent);
        } else {
            // Set empty text to display "No matches found.
            // An ISBN is usually found on the back cover, near the barcode."
            mEmptyTextView.setText(getString(R.string.no_matches_found));
            mEmptyTextView.setTextColor(getResources().getColor(R.color.color_no_matches_found_text));
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Book>> loader) {

    }

    /**
     * Check for network connectivity.
     */
    private boolean isConnected() {
        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connectivityManager =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return (networkInfo != null && networkInfo.isConnected());
    }


    /**
     * If there is internet connectivity, initialize the loader as usual.
     * Otherwise, hide loading indicator and set empty state TextView to display
     * "You are offline. Please check your Internet connection."
     *
     * @param isConnected internet connection is available or not
     */
    private void initializeLoader(boolean isConnected) {
        if (isConnected) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = this.getLoaderManager();
            // Initialize the loader with the BOOK_LOADER_ID
            loaderManager.initLoader(BOOK_LOADER_ID, null, this);
        } else {
            // Otherwise, display error.
            // First, hide loading indicator so error message will be visible
            mLoadingIndicator.setVisibility(View.GONE);
            // Set empty text to display no connection error message
            mEmptyTextView.setText(getString(R.string.no_internet_connection));
            mEmptyTextView.setTextColor(getResources().getColor(R.color.color_grey_text));
            mEmptyTextView.setCompoundDrawablesWithIntrinsicBounds(Constants.DEFAULT_NUMBER,
                    R.drawable.ic_network_check,Constants.DEFAULT_NUMBER,Constants.DEFAULT_NUMBER);
        }
    }

    // Go back to the MainActivity when up button in app bar is clicked on.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

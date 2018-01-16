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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.Book;
import com.example.android.inventory.BookLoader;
import com.example.android.inventory.utils.Constants;

import java.util.List;

/**
 * Created by sj on 1/15/2018.
 */

public class IsbnActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Book>> {

    private static final String LOG_TAG = IsbnActivity.class.getName();

    /**Constant value for the book loader ID */
    private static final int BOOK_LOADER_ID = 1;



    private View mLoadingIndicator;

    private EditText mEditText;
    private TextView mTextView;
    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "TEST: Earthquake Activity onCreate() called");
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.dialog_isbn);


        initializeLoader(isConnected());



        //mEditText = findViewById(R.id.edit_dialog_isbn);
        //mTextView = findViewById(R.id.text_dialog);
        //mButton = findViewById(R.id.search_dialog);


    }

    @Override
    public Loader<List<Book>> onCreateLoader(int i, Bundle bundle) {
        Uri baseUri = Uri.parse(Constants.BOOK_REQUEST_URL);

        Uri.Builder uriBuilder = baseUri.buildUpon();


/*
        String isbnString = mEditText.getText().toString().trim();
        isbnString = "isbn:" + isbnString;
*/

        Intent intent = getIntent();
        String isbnStringFromDialog = intent.getStringExtra("ISBN in a Dialog");
        isbnStringFromDialog = "isbn:" + isbnStringFromDialog;

        Log.e(LOG_TAG, "isbnStringFromDialog: " + isbnStringFromDialog);
        uriBuilder.appendQueryParameter("q", isbnStringFromDialog);

        return new BookLoader(this,  uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<Book>> loader, List<Book> books) {



        if (books != null && !books.isEmpty()) {

            Book book = books.get(0);
            String title = book.getBookTitle();
            String author = book.getAuthor();
            String isbn = book.getIsbn();
            String publisher = book.getPublisher();
            String testString = title +"\n" + author +"\n" + isbn +"\n" +publisher;
            //mTextView.setText(testString);
            Log.e(LOG_TAG, "onLoadFinisher title: " + title);

            Intent intent = new Intent(this, EditorActivity.class);
            intent.putExtra("title", title);
            intent.putExtra("author", author);
            intent.putExtra("isbn", isbn);
            intent.putExtra("publisher", publisher);
            startActivity(intent);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Book>> loader) {

    }

    private boolean isConnected() {

        ConnectivityManager connectivityManager =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return (networkInfo != null && networkInfo.isConnected());
    }


    private void initializeLoader(boolean isConnected) {
        if (isConnected) {
            LoaderManager loaderManager = this.getLoaderManager();
            loaderManager.initLoader(BOOK_LOADER_ID, null, this);
        } else {


            // Show toast with no connection error message
            Toast.makeText(this,
                    "You are offline. Please check your Internet connection.",
                    Toast.LENGTH_SHORT).show();
        }
    }



}

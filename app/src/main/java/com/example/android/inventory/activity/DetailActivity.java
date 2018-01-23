package com.example.android.inventory.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.R;
import com.example.android.inventory.data.ProductContract.ProductEntry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * DetailActivity displays the product details which are stored in the database.
 */
public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /** Tag for the log messages */
    private static final String LOG_TAG = DetailActivity.class.getSimpleName();

    /** Identifier for the product data loader */
    private static final int EXISTING_PRODUCT_LOADER = 0;

    /** Content URI for the existing product */
    private Uri mCurrentProductUri;

    /** TextView for the product name */
    @BindView(R.id.detail_product_name) TextView mProductNameTextView;

    /** TextView field to enter the author */
    @BindView(R.id.detail_product_author) TextView mAuthorTextView;

    /** TextView field to enter the publisher */
    @BindView(R.id.detail_product_publisher) TextView mPublisherTextView;

    /** TextView field to enter the ISBN */
    @BindView(R.id.detail_product_isbn) TextView mIsbnTextView;

    /** TextView field to enter the price of the product */
    @BindView(R.id.detail_product_price) TextView mPriceTextView;

    /** TextView field to enter the quantity of the product */
    @BindView(R.id.detail_product_quantity) TextView mQuantityTextView;

    /** ImageView for the product */
    @BindView(R.id.detail_product_image) ImageView mImageView;

    /** TextView field to enter supplier's name */
    @BindView(R.id.detail_supplier_name) TextView mSupplierNameTextView;

    /** TextView field to enter supplier's email */
    @BindView(R.id.detail_supplier_email) TextView mSupplierEmailTextView;

    /** TextView field to enter supplier's phone number */
    @BindView(R.id.detail_supplier_phone) TextView mSupplierPhoneTextView;

    /** ImageButton for the supplier email */
    @BindView(R.id.detail_email_button) ImageButton mSupplierEmailButton;

    private static final int MY_PERMISSONS_REQUEST_READ_CONTACTS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Examine the intent that was used to launch this activity
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        // Bind the view using ButterKnife
        ButterKnife.bind(this);

        // Find all relevant button that we will need to increment and decrement the quantity
        Button plusButton = findViewById(R.id.detail_plus_button);
        Button minusButton = findViewById(R.id.detail_minus_button);
        ImageButton supplierPhoneButton = findViewById(R.id.detail_phone_button);

        // Set OnClickListener on the plus button. We can increment the available quantity displayed.
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                increment();
            }
        });

        // Set OnClickListener on the minus button. We can decrement the available quantity displayed.
        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decrement();
            }
        });

        mSupplierEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create order message and email to the supplier of the product.
                composeEmail();
            }
        });

        supplierPhoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Make a phone call
                call();
            }
        });

        // Initialize a loader to read the product data from the database
        // and display the current values in the editor
        getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);

        // Allow Up navigation with the app icon in the app bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the contacts-related task you need to do.
                    Toast.makeText(this, getString(R.string.permission_granted),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // permission denied, boo! Disable the functionality that depends on this permission.
                    Toast.makeText(this, getString(R.string.permission_denied),
                            Toast.LENGTH_SHORT).show();
                }
                return;
            }
            // other 'case' lines to check for other permissions this app might request
        }
    }

    /**
     * Start a phone call intent when supplier phone button is clicked.
     */
    private  void call() {
        // Read from text field
        String phoneString = mSupplierPhoneTextView.getText().toString().trim();

        Intent phoneIntent = new Intent(Intent.ACTION_CALL);
        phoneIntent.setData(Uri.parse(getString(R.string.tel_colon) + phoneString));
        // Check whether the app has a given permission
        if (ActivityCompat.checkSelfPermission(DetailActivity.this,
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(DetailActivity.this,
                    Manifest.permission.CALL_PHONE)) {

            } else {
                // Request permission to be granted to this application
                ActivityCompat.requestPermissions(DetailActivity.this,
                        new String[]{ Manifest.permission.CALL_PHONE},
                        MY_PERMISSONS_REQUEST_READ_CONTACTS);
            }
            return;
        }
        startActivity(Intent.createChooser(phoneIntent, getString(R.string.make_a_phone_call)));
    }

    /**
     * Increment the available quantity displayed by 1.
     */
    private void increment() {
        // Read from text fields
        String quantityString = mQuantityTextView.getText().toString().trim();

        // Parse the string into an Integer value.
        int quantity = Integer.parseInt(quantityString);
        quantity = quantity + 1;

        // Create a ContentValues object where column names are the keys,
        // and product attributes from the textView fields are the values.
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);

        // Update the product with content URI: mCurrentProductUri
        // and pass in the new ContentValues. Pass in null for the selection and selection args
        // because mCurrentProductUri will already identify the correct row in the database that
        // we want to modify.
        int rowsAffected = getContentResolver().update(mCurrentProductUri, values,
                null, null);
    }

    /**
     * Decrement the available quantity displayed by 1 and check that no negative quantities display.
     */
    private void decrement() {
        // Read from text fields
        String quantityString = mQuantityTextView.getText().toString().trim();

        // Parse the string into an Integer value.
        int quantity = Integer.parseInt(quantityString);
        // If the quantity is more than 0, decrement the quantity by 1.
        // If quantity is 0, show a toast message.
        if (quantity > 0) {
            quantity = quantity - 1;
        } else if (quantity == 0) {
            Toast.makeText(DetailActivity.this, getString(R.string.detail_update_zero_quantity),
                    Toast.LENGTH_SHORT).show();
        }

        // Create a ContentValues object where column names are the keys,
        // and product attributes from the textView fields are the values.
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);

        // Update the product with content URI: mCurrentProductUri
        // and pass in the new ContentValues. Pass in null for the selection and selection args
        // because mCurrentProductUri will already identify the correct row in the database that
        // we want to modify.
        int rowsAffected = getContentResolver().update(mCurrentProductUri, values,
                null, null);
    }

    /**
     * If a user clicks the email button, creates order message and email to the supplier of the product.
     */
    private void composeEmail() {
        // Read from text fields
        String productNameString = mProductNameTextView.getText().toString().trim();
        String authorString = mAuthorTextView.getText().toString().trim();
        String publisherString = mPublisherTextView.getText().toString().trim();
        String isbnString = mIsbnTextView.getText().toString().trim();
        String[] supplierEmailString = {mSupplierEmailTextView.getText().toString().trim()};

        // Create order message
        String subject = getString(R.string.email_subject) + " " + productNameString;
        String message = getString(R.string.place_an_order) + " " + getString(R.string.copies_of) +
                " " + productNameString + getString(R.string.period);
        message += getString(R.string.nn) + getString(R.string.app_product_details);
        message += getString(R.string.nn) + getString(R.string.category_product_name) +
                getString(R.string.colon) + " " + productNameString;
        message += getString(R.string.n) + getString(R.string.category_product_author) +
                getString(R.string.colon) + " " + authorString;
        message += getString(R.string.n) + getString(R.string.category_product_publisher) +
                getString(R.string.colon) + " " + publisherString;
        message += getString(R.string.n) + getString(R.string.category_product_isbn) +
                getString(R.string.colon) + " "+ isbnString;
        message += getString(R.string.nn) + getString(R.string.best);

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse(getString(R.string.mailto)));
        // Email address
        emailIntent.putExtra(Intent.EXTRA_EMAIL, supplierEmailString);
        // Email subject
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        // The body of the email
        emailIntent.putExtra(Intent.EXTRA_TEXT, message);

        if(emailIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(Intent.createChooser(emailIntent, getString(R.string.compose_email)));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_edit:
                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(DetailActivity.this, EditorActivity.class);
                // Set the current product URI on the data field of the intent
                intent.setData(mCurrentProductUri);
                // Launch the {@link EditorActivity} to display the data for the current product.
                startActivity(intent);
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Exit activity
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Perform the deletion of the product in the database.
     */
    private void deleteProduct() {
        // Only perform the delete if this is an existing product.
        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentProductUri
            // content URI already identifies the product that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null,
                    null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }

    /**
     * Prompt the user to confirm that they want to delete this product.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all product attributes, define a projection that contains
        // all columns from the product table
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_AUTHOR,
                ProductEntry.COLUMN_PRODUCT_PUBLISHER,
                ProductEntry.COLUMN_PRODUCT_ISBN,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_IMAGE,
                ProductEntry.COLUMN_SUPPLIER_NAME,
                ProductEntry.COLUMN_SUPPLIER_EMAIL,
                ProductEntry.COLUMN_SUPPLIER_PHONE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentProductUri,             // Query the content URI for the current product
                projection,                        // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,               // No selection arguments
                null);                 // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of product attributes that we're interested in
            int titleColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int authorColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_AUTHOR);
            int publisherColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PUBLISHER);
            int isbnColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_ISBN);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int imageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);
            int supplierNameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_NAME);
            int supplierEmailColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_EMAIL);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_PHONE);

            // Extract out the value from the Cursor for the given column index
            String title = cursor.getString(titleColumnIndex);
            String author = cursor.getString(authorColumnIndex);
            String publisher = cursor.getString(publisherColumnIndex);
            String isbn = cursor.getString(isbnColumnIndex);
            double price = cursor.getDouble(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            final String imageString = cursor.getString(imageColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            String supplierEmail = cursor.getString(supplierEmailColumnIndex);
            String supplierPhone = cursor.getString(supplierPhoneColumnIndex);

            // Update the views on the screen with the values from the database
            mProductNameTextView.setText(title);
            mAuthorTextView.setText(author);
            mPublisherTextView.setText(publisher);
            mIsbnTextView.setText(isbn);
            mPriceTextView.setText(String.valueOf(price));
            mQuantityTextView.setText(String.valueOf(quantity));

            if(imageString != null) {

                // Attach a ViewTreeObserver listener to ImageView.
                ViewTreeObserver viewTreeObserver = mImageView.getViewTreeObserver();
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        mImageView.setImageBitmap(getBitmapFromUri(Uri.parse(imageString)));
                    }
                });
            } else {
                mImageView.setImageResource(R.drawable.ic_image_black_24dp);
            }

            mSupplierNameTextView.setText(supplierName);
            // If supplierEmail string is empty, hide the email button
            if(TextUtils.isEmpty(supplierEmail)) {
                mSupplierEmailButton.setVisibility(View.GONE);
            }
            mSupplierEmailTextView.setText(supplierEmail);
            mSupplierPhoneTextView.setText(supplierPhone);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mProductNameTextView.setText("");
        mAuthorTextView.setText("");
        mPublisherTextView.setText("");
        mIsbnTextView.setText("");
        mPriceTextView.setText(String.valueOf(""));
        mQuantityTextView.setText(String.valueOf(""));
        mSupplierNameTextView.setText("");
        mSupplierEmailTextView.setText("");
        mSupplierPhoneTextView.setText("");
    }

    /**
     * Returns a Bitmap object from the URI which is the location of the image.
     */
    public Bitmap getBitmapFromUri(Uri uri) {
        // Check the Uri is null or empty
        if (uri == null || uri.toString().isEmpty()) {
            return null;
        }

        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        InputStream inputStream = null;
        try {
            inputStream = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, bmOptions);
            inputStream.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            inputStream = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, bmOptions);
            inputStream.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, "Failed to open the image file.", fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Problem with loading a file.");
            }
        }
    }
}

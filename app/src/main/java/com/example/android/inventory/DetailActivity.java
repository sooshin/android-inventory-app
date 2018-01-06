package com.example.android.inventory;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.data.ProductContract.ProductEntry;

/**
 * DetailActivity displays the product details which are stored in the database.
 */
public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    /** Identifier for the product data loader */
    private static final int EXISTING_PRODUCT_LOADER = 0;

    /** Content URI for the existing product */
    private Uri mCurrentProductUri;

    /** TextView for the product name */
    private TextView mProductNameTextView;

    /** TextView field to enter the author */
    private TextView mAuthorTextView;

    /** TextView field to enter the publisher */
    private TextView mPublisherTextView;

    /** TextView field to enter the ISBN */
    private TextView mIsbnTextView;

    /** TextView field to enter the price of the product */
    private TextView mPriceTextView;

    /** TextView field to enter the quantity of the product */
    private TextView mQuantityTextView;

    /** TextView field to enter supplier's name */
    private  TextView mSupplierNameTextView;

    /** TextView field to enter supplier's email */
    private TextView mSupplierEmailTextView;

    /** TextView field to enter supplier's phone number */
    private TextView mSupplierPhoneTextView;

    /** Boolean flag that keeps track of whether the product has been edited (true) or not (false) */
    private boolean mProductHasChanged = false;

    /** Button to increment quantity */
    private Button mPlusButton;

    /** Button to decrement quantity*/
    private Button mMinusButton;

    private Button mSupplierEmailButton;
    private Button mSupplierPhoneButton;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mProductHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Examine the intent that was used to launch this activity
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        // Find all relevant views that we will need to read text fields from
        mProductNameTextView = findViewById(R.id.detail_product_name);
        mAuthorTextView = findViewById(R.id.detail_product_author);
        mPublisherTextView = findViewById(R.id.detail_product_publisher);
        mIsbnTextView = findViewById(R.id.detail_product_isbn);
        mPriceTextView = findViewById(R.id.detail_product_price);
        mQuantityTextView = findViewById(R.id.detail_product_quantity);
        mSupplierNameTextView = findViewById(R.id.detail_supplier_name);
        mSupplierEmailTextView = findViewById(R.id.detail_supplier_email);
        mSupplierPhoneTextView = findViewById(R.id.detail_supplier_phone);
        // Find all relevant button that we will need to increment and decrement the quantity
        mPlusButton = findViewById(R.id.detail_plus_button);
        mMinusButton = findViewById(R.id.detail_minus_button);

        mSupplierEmailButton = findViewById(R.id.detail_email_button);
        mSupplierPhoneButton = findViewById(R.id.detail_phone_button);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mPlusButton.setOnTouchListener(mTouchListener);
        mMinusButton.setOnTouchListener(mTouchListener);

        // Set OnClickListener on the plus button. We can increment the available quantity displayed.
        mPlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                increment();
            }
        });

        // Set OnClickListener on the minus button. We can decrement the available quantity displayed.
        mMinusButton.setOnClickListener(new View.OnClickListener() {
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

        // Initialize a loader to read the product data from the database
        // and display the current values in the editor
        getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);

        // Allow Up navigation with the app icon in the app bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
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

        // Show a toast message depending on whether or not the update was successful.
        if (rowsAffected == 0) {
            // If no rows are affected, then there was an error with the update.
            Toast.makeText(DetailActivity.this, getString(R.string.editor_update_product_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the update was successful and we can display a toast.
            Toast.makeText(DetailActivity.this, getString(R.string.editor_update_product_successful),
                    Toast.LENGTH_SHORT).show();
        }
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

        // Show a toast message depending on whether or not the update was successful.
        if (rowsAffected == 0) {
            // If no rows are affected, then there was an error with the update.
            Toast.makeText(DetailActivity.this, getString(R.string.editor_update_product_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the update was successful and we can display a toast.
            Toast.makeText(DetailActivity.this, getString(R.string.editor_update_product_successful),
                    Toast.LENGTH_SHORT).show();
        }
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
                // If the product hasn't changed, continue with navigating up to parent activity
                // which is the {@link MainActivity}.
                if (!mProductHasChanged) {
                    finish();
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity
                                //NavUtils.navigateUpFromSameTask(DetailActivity.this);
                                finish();
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
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

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                        the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
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
            mSupplierNameTextView.setText(supplierName);
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
}

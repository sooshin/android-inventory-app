package com.example.android.inventory;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.inventory.data.ProductContract.ProductEntry;

/**
 * Allows user to create a new product or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    /** Identifier for the product data loader */
    private static final int EXISTING_PRODUCT_LOADER = 0;

    /** Content URI for the existing product (null if it's a new product) */
    private Uri mCurrentProductUri;

    /** EditText field to enter the product name */
    private EditText mProductNameEditText;

    /** EditText field to enter the author */
    private EditText mAuthorEditText;

    /** EditText field to enter the publisher */
    private EditText mPublisherEditText;

    /** EditText field to enter the ISBN */
    private EditText mIsbnEditText;

    /** EditText field to enter the price of the product */
    private EditText mPriceEditText;

    /** EditText field to enter the quantity of the product */
    private EditText mQuantityEditText;

    /** EditText field to enter supplier's name */
    private  EditText mSupplierNameEditText;

    /** EditText field to enter supplier's email */
    private EditText mSupplierEmailEditText;

    /** EditText field to enter supplier's phone number */
    private EditText mSupplierPhoneEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new product or editing an existing one.
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        // If the intent DOES NOT contain a product content URI, then we know that we are
        // creating a new pet.
        if (mCurrentProductUri == null) {
            // This is a new product, so change the app bar to say "Add a Product"
            setTitle(R.string.editor_activity_title_new_product);
        } else {
            // Otherwise this is an existing product, so change app bar to say "Edit Pet"
            setTitle(R.string.editor_activity_title_edit_product);

            // Initialize a loader to read the product data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mProductNameEditText = findViewById(R.id.edit_product_name);
        mAuthorEditText = findViewById(R.id.edit_product_author);
        mPublisherEditText = findViewById(R.id.edit_product_publisher);
        mIsbnEditText = findViewById(R.id.edit_product_isbn);
        mPriceEditText = findViewById(R.id.edit_product_price);
        mQuantityEditText = findViewById(R.id.edit_product_quantity);
        mSupplierNameEditText = findViewById(R.id.edit_supplier_name);
        mSupplierEmailEditText = findViewById(R.id.edit_supplier_email);
        mSupplierPhoneEditText = findViewById(R.id.edit_supplier_phone);
    }

    /**
     * Get user input from editor and save new product into database.
     */
    private void insertProduct() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String productNameString = mProductNameEditText.getText().toString().trim();
        String authorString = mAuthorEditText.getText().toString().trim();
        String publisherString = mPublisherEditText.getText().toString().trim();
        String isbnString = mIsbnEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        double price = Double.parseDouble(priceString);
        String quantityString = mQuantityEditText.getText().toString().trim();
        int quantity = Integer.parseInt(quantityString);
        String supplierNameString = mSupplierNameEditText.getText().toString().trim();
        String supplierEmailString = mSupplierEmailEditText.getText().toString().trim();
        String supplierPhoneString = mSupplierPhoneEditText.getText().toString().trim();

        // Create a ContentValues object where column names are the keys,
        // and product attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, productNameString);
        values.put(ProductEntry.COLUMN_PRODUCT_AUTHOR, authorString);
        values.put(ProductEntry.COLUMN_PRODUCT_PUBLISHER, publisherString);
        values.put(ProductEntry.COLUMN_PRODUCT_ISBN, isbnString);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, price);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        values.put(ProductEntry.COLUMN_SUPPLIER_NAME, supplierNameString);
        values.put(ProductEntry.COLUMN_SUPPLIER_EMAIL,supplierEmailString);
        values.put(ProductEntry.COLUMN_SUPPLIER_PHONE, supplierPhoneString);

        // Insert a new product into the provider, returning the content URI for the new product.
        Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

        // Show a toast message depending on whether or not the insertion was successful
        if (newUri == null) {
            // If the new content URI is null, then there was an error with insertion.
            Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the insertion was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save product to database
                insertProduct();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Do nothing for now
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (MainActivity)
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
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
            mProductNameEditText.setText(title);
            mAuthorEditText.setText(author);
            mPublisherEditText.setText(publisher);
            mIsbnEditText.setText(isbn);
            mPriceEditText.setText(String.valueOf(price));
            mQuantityEditText.setText(String.valueOf(quantity));
            mSupplierNameEditText.setText(supplierName);
            mSupplierEmailEditText.setText(supplierEmail);
            mSupplierPhoneEditText.setText(supplierPhone);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mProductNameEditText.setText("");
        mAuthorEditText.setText("");
        mPublisherEditText.setText("");
        mIsbnEditText.setText("");
        mPriceEditText.setText(String.valueOf(""));
        mQuantityEditText.setText(String.valueOf(""));
        mSupplierNameEditText.setText("");
        mSupplierEmailEditText.setText("");
        mSupplierPhoneEditText.setText("");
    }
}
